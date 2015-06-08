/*
 * Copyright 2011-2015 L2EMU UNIQUE
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.l2emuproject.proxy.ui.savormix.component.packet;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.WindowConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import javolution.util.FastMap;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import net.l2emuproject.lang.L2TextBuilder;
import net.l2emuproject.network.mmocore.MMOBuffer;
import net.l2emuproject.network.security.LoginCipher;
import net.l2emuproject.proxy.config.ProxyConfig;
import net.l2emuproject.proxy.network.ByteBufferUtils;
import net.l2emuproject.proxy.network.Proxy;
import net.l2emuproject.proxy.network.ServiceType;
import net.l2emuproject.proxy.network.listener.ConnectionListener;
import net.l2emuproject.proxy.network.login.client.L2LoginClient;
import net.l2emuproject.proxy.network.packets.ProxyRepeatedPacket;
import net.l2emuproject.proxy.script.PpeEnabledScript;
import net.l2emuproject.proxy.setup.IPAliasManager;
import net.l2emuproject.proxy.ui.ReceivedPacket;
import net.l2emuproject.proxy.ui.savormix.loader.LoadOption;
import net.l2emuproject.util.HexUtil;

/**
 * A simple dialog that allows to inject packets specifying their body as a space-delimited hex
 * octet string. This dialog also provides a convenient data-to-octets conversion panel. <BR>
 * <BR>
 * This class is a part of a reference GUI provided for education purposes only. These classes
 * should help to understand how to interact with the underlying proxy core. <BR>
 * <BR>
 * Creating an own GUI is the preferred way to start using this application.
 * 
 * @author savormix
 */
public class PacketInject extends JDialog implements ActionListener, ConnectionListener
{
	private static final long serialVersionUID = 9103313068171896488L;
	
	// only used from the event thread
	private static final ByteBuffer CONVERSION_BUFFER = ByteBuffer.allocate(8_200).order(ByteOrder.LITTLE_ENDIAN);
	private static final MMOBuffer BUFFER_WRAPPER = new MMOBuffer().setByteBuffer(CONVERSION_BUFFER);
	
	private final Map<Proxy, ProxyWrapper> _connections;
	
	private final Map<ConvertibleDataType, JRadioButton> _radioButtons;
	private final JTextField _tfInput;
	private final JTextArea _taOutput, _taBody;
	private final JButton _btnConvert;
	private final JButton _btnMove;
	
	private final ProxyWrapper _placeholderConnection;
	private final JComboBox<ProxyWrapper> _cbConnections;
	private final JRadioButton _rbClient, _rbServer;
	private final JButton _btnSend;
	
	private final PacketDisplay _display;
	final Timer _displayUpdater;
	PacketDisplayTask _displayTask;
	
	/**
	 * Constructs a basic packet injection dialog.
	 * 
	 * @param owner
	 *            Owner window
	 */
	public PacketInject(Window owner)
	{
		super(owner, "Packet injection", ModalityType.MODELESS);
		
		setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
		
		_connections = FastMap.newInstance();
		
		{
			final ButtonGroup bg = new ButtonGroup();
			_radioButtons = new EnumMap<>(ConvertibleDataType.class);
			for (final ConvertibleDataType cdt : ConvertibleDataType.values())
			{
				final JRadioButton rb = new JRadioButton(cdt.toString(), false);
				_radioButtons.put(cdt, rb);
				bg.add(rb);
			}
		}
		
		_tfInput = new JTextField();
		//_tfInput.addActionListener(this);
		_taOutput = new JTextArea(10, 15);
		_taOutput.setEditable(false);
		_taOutput.setLineWrap(true);
		_taOutput.setWrapStyleWord(true);
		
		_taBody = new JTextArea(10, 50);
		_taBody.setLineWrap(true);
		_taBody.setWrapStyleWord(true);
		_taBody.getDocument().addDocumentListener(new DocumentListener()
		{
			@Override
			public void removeUpdate(DocumentEvent e)
			{
				_displayUpdater.restart();
			}
			
			@Override
			public void insertUpdate(DocumentEvent e)
			{
				_displayUpdater.restart();
			}
			
			@Override
			public void changedUpdate(DocumentEvent e)
			{
				// TODO Auto-generated method stub
				
			}
		});
		_display = new PacketDisplay(null)
		{
			private static final long serialVersionUID = 7912726759082659173L;
			
			@Override
			public Dimension getPreferredSize()
			{
				final Dimension natural = super.getPreferredSize();
				return new Dimension(350, natural.height);
			}
		};
		_displayUpdater = new Timer(250, e ->
		{
			final Pair<Proxy, ByteBuffer> setup = getCurrentBodyAndRecipient(false);
			if (setup == null)
				return;
			
			if (_displayTask != null)
				_displayTask.cancel(true);
			
			final Proxy target = setup.getLeft();
			_display.setProtocol(target.getProtocol());
			_display.setCacheContext(PpeEnabledScript.getEntityContext(target));
			_displayTask = new PacketDisplayTask(_display);
			_displayTask.execute(new ReceivedPacket(ServiceType.valueOf(target.getProtocol()), target.getTarget().getType(), setup.getRight().array()));
		});
		_displayUpdater.setRepeats(false);
		
		_btnConvert = new JButton("Convert");
		_btnConvert.addActionListener(event ->
		{
			_taOutput.setText(null);
			
			final ConvertibleDataType dataType = _radioButtons.entrySet().stream().filter(e -> e.getValue().isSelected()).findAny().get().getKey();
			final String input = _tfInput.getText();
			try
			{
				dataType.write(BUFFER_WRAPPER, input);
				_taOutput.setText(HexUtil.bytesToHexString(CONVERSION_BUFFER.array(), 0, CONVERSION_BUFFER.position(), " "));
			}
			catch (RuntimeException ex)
			{
				// ignore
			}
			finally
			{
				CONVERSION_BUFFER.clear();
			}
		});
		_btnMove = new JButton(">>>>");
		_btnMove.addActionListener(e ->
		{
			final String text = _taOutput.getText();
			if (text.isEmpty())
				return;
			_taOutput.setText(null);
			
			final L2TextBuilder sb = new L2TextBuilder();
			int caret = _taBody.getCaretPosition();
			if (caret > 0)
			{
				char c = _taBody.getText().charAt(caret - 1);
				if (c != ' ' && c != '\r' && c != '\n')
					sb.append(' ');
			}
			sb.append(text).append(System.getProperty("line.separator", "\r\n"));
			_taBody.insert(sb.moveToString(), caret);
		});
		
		{
			final ButtonGroup bg = new ButtonGroup();
			bg.add(_rbClient = new JRadioButton("Client", true));
			bg.add(_rbServer = new JRadioButton("Server", false));
			
			_rbClient.addActionListener(e -> _displayUpdater.restart());
			_rbServer.addActionListener(e -> _displayUpdater.restart());
		}
		
		_btnSend = new JButton("Send packet");
		_btnSend.setEnabled(false);
		_btnSend.addActionListener(this);
		
		_placeholderConnection = new ProxyWrapper();
		_cbConnections = new JComboBox<>();
		_cbConnections.setEditable(false);
		_cbConnections.addItem(_placeholderConnection);
		_cbConnections.addActionListener(e ->
		{
			_btnSend.setEnabled(_cbConnections.getSelectedIndex() > 0);
			_displayUpdater.restart();
		});
		
		final Container contentPane = getContentPane();
		contentPane.setLayout(new BorderLayout());
		
		final JPanel root = new JPanel(new BorderLayout());
		
		final JPanel n = new JPanel();
		n.add(new JLabel("Send to:"));
		n.add(_cbConnections);
		n.add(_rbClient);
		n.add(_rbServer);
		
		final JPanel right = new JPanel(); // actually, left
		right.setLayout(new BorderLayout());
		{
			JPanel rn = new JPanel();
			rn.setLayout(new BorderLayout());
			{
				JPanel rnn = new JPanel();
				rnn.setLayout(new BorderLayout(10, 0));
				JLabel lab = new JLabel("Input");
				lab.setLabelFor(_tfInput);
				rnn.add(lab, BorderLayout.LINE_START);
				rnn.add(_tfInput, BorderLayout.CENTER);
				rn.add(rnn, BorderLayout.NORTH);
			}
			{
				JPanel rns = new JPanel();
				rns.setLayout(new GridLayout(0, 2));
				for (final JRadioButton rb : _radioButtons.values())
					rns.add(rb);
				rn.add(rns, BorderLayout.SOUTH);
			}
			right.add(rn, BorderLayout.NORTH);
		}
		{
			JPanel rs = new JPanel();
			rs.setLayout(new GridLayout(1, 0));
			rs.add(_btnConvert);
			rs.add(_btnMove);
			right.add(rs, BorderLayout.SOUTH);
		}
		right.add(new JScrollPane(_taOutput), BorderLayout.CENTER);
		
		JSplitPane center = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true, right, new JScrollPane(_taBody));
		
		root.add(n, BorderLayout.NORTH);
		root.add(center, BorderLayout.CENTER);
		root.add(_btnSend, BorderLayout.SOUTH);
		
		contentPane.add(root, BorderLayout.CENTER);
		if (LoadOption.DISABLE_DEFS.isNotSet())
			contentPane.add(_display, BorderLayout.EAST);
		
		setLocationByPlatform(true);
		pack();
	}
	
	private Pair<Proxy, ByteBuffer> getCurrentBodyAndRecipient(boolean showExceptionDialog)
	{
		final ProxyWrapper pw = _cbConnections.getItemAt(_cbConnections.getSelectedIndex());
		if (pw == null || pw == _placeholderConnection)
			return null;
		
		Proxy target = pw.getProxy();
		if (_rbServer.isSelected())
			target = target.getTarget();
		
		try
		{
			// this still assumes pre-allocated checksum & its padding; this only pads as required for blowfish block cipher
			final byte[] body = HexUtil.hexStringToBytes(_taBody.getText().replace('\r', ' ').replace('\n', ' '), pw.isLogin() ? 8 : 1);
			if (body.length < 1)
				throw new IllegalArgumentException();
			final ByteBuffer buf = ByteBuffer.wrap(body).order(CONVERSION_BUFFER.order()); // backed and mutable
			if (pw.isLogin())
			{
				// prematurely inject checksum so that it would be available for notifications
				LoginCipher.injectChecksum(buf, target.getType().isClient() ? 8 : 16); // client will receive SP and server will receive CP
			}
			return ImmutablePair.of(target, buf);
		}
		catch (Exception ex)
		{
			if (showExceptionDialog)
				JOptionPane.showMessageDialog(PacketInject.this, "Invalid packet body. Only hex octets delimited by spaces are allowed.", "Invalid body!", JOptionPane.ERROR_MESSAGE);
		}
		return null;
	}
	
	@Override
	public void actionPerformed(ActionEvent e)
	{
		final Pair<Proxy, ByteBuffer> setup = getCurrentBodyAndRecipient(true);
		if (setup == null)
			return;
		
		final Proxy target = setup.getLeft();
		final ByteBuffer buf = setup.getRight();
		target.sendPacket(new ProxyRepeatedPacket(buf.array()));
		target.getTarget().notifyPacketForwarded(null, ByteBufferUtils.asReadOnly(buf), System.currentTimeMillis()); // immutable
	}
	
	@Override
	public void onClientConnection(Proxy client)
	{
		final boolean login = client instanceof L2LoginClient;
		if (login && ProxyConfig.NO_TABS_FOR_LOGIN_CONNECTIONS)
			return;
		
		final ProxyWrapper pw = new ProxyWrapper(client, login);
		_connections.put(client, pw);
		SwingUtilities.invokeLater(() -> _cbConnections.addItem(pw));
	}
	
	@Override
	public void onDisconnection(Proxy client, Proxy server)
	{
		final ProxyWrapper pw = _connections.remove(client);
		if (pw == null)
			return;
		
		SwingUtilities.invokeLater(() ->
		{
			_cbConnections.removeItem(pw);
			if (_cbConnections.getItemCount() <= 1)
			{
				_displayUpdater.stop();
				if (_displayTask != null)
				{
					_displayTask.cancel(true);
					_displayTask = null;
				}
				_display.displayPacket(null);
			}
		});
	}
	
	@Override
	public void onServerConnection(Proxy server)
	{
		// ignore
	}
	
	private static final class ProxyWrapper
	{
		private static final AtomicInteger ID = new AtomicInteger();
		
		private final Proxy _proxy;
		private final String _desc;
		private final boolean _login;
		
		public ProxyWrapper()
		{
			_proxy = null;
			_desc = "Select a connection";
			_login = false;
		}
		
		public ProxyWrapper(Proxy proxy, boolean login)
		{
			_proxy = proxy;
			_desc = ID.incrementAndGet() + ") " + (login ? "L" : "G") + " " + IPAliasManager.toUserFriendlyString(proxy.getHostAddress());
			_login = login;
		}
		
		public Proxy getProxy()
		{
			return _proxy;
		}
		
		public boolean isLogin()
		{
			return _login;
		}
		
		@Override
		public String toString()
		{
			return _desc;
		}
	}
	
	private enum ConvertibleDataType
	{
		BYTE
		{
			@Override
			void write(MMOBuffer buf, String input) throws NumberFormatException
			{
				buf.writeC(Long.parseLong(input));
			}
			
			@Override
			public String toString()
			{
				return "Byte[1]";
			}
		},
		WORD
		{
			@Override
			void write(MMOBuffer buf, String input) throws NumberFormatException
			{
				buf.writeH(Long.parseLong(input));
			}
			
			@Override
			public String toString()
			{
				return "Word[2]";
			}
		},
		DWORD
		{
			@Override
			void write(MMOBuffer buf, String input) throws NumberFormatException
			{
				buf.writeD(Long.parseLong(input));
			}
			
			@Override
			public String toString()
			{
				return "DWord[4]";
			}
		},
		QWORD
		{
			@Override
			void write(MMOBuffer buf, String input) throws NumberFormatException
			{
				buf.writeC(Long.parseLong(input));
			}
			
			@Override
			public String toString()
			{
				return "QWord[8]";
			}
		},
		SINGLE
		{
			@Override
			void write(MMOBuffer buf, String input) throws NumberFormatException
			{
				buf.writeIEEESingle(Float.parseFloat(input));
			}
			
			@Override
			public String toString()
			{
				return "Float[4]";
			}
		},
		DOUBLE
		{
			@Override
			void write(MMOBuffer buf, String input) throws NumberFormatException
			{
				buf.writeF(Double.parseDouble(input));
			}
			
			@Override
			public String toString()
			{
				return "Float[8]";
			}
		},
		STRING_T
		{
			@Override
			void write(MMOBuffer buf, String input) throws NumberFormatException
			{
				buf.writeS(input);
			}
			
			@Override
			public String toString()
			{
				return "String[2(L+1)]";
			}
		},
		STRING_L
		{
			@Override
			void write(MMOBuffer buf, String input) throws NumberFormatException
			{
				buf.writeSWithLength(input);
			}
			
			@Override
			public String toString()
			{
				return "String[2+2L]";
			}
		};
		
		abstract void write(MMOBuffer buf, String input) throws RuntimeException;
	}
}
