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
import java.awt.Window;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.Timer;
import javax.swing.WindowConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import net.l2emuproject.lang.NotARealProxyObject;
import net.l2emuproject.network.protocol.IProtocolVersion;
import net.l2emuproject.proxy.network.EndpointType;
import net.l2emuproject.proxy.network.ServiceType;
import net.l2emuproject.proxy.state.entity.context.ICacheServerID;
import net.l2emuproject.proxy.ui.ReceivedPacket;
import net.l2emuproject.proxy.ui.savormix.io.VersionnedPacketTable;
import net.l2emuproject.util.HexUtil;

/**
 * @author _dev_
 */
public class PacketExplainDialog extends JDialog
{
	private static final long serialVersionUID = 5305447643019388605L;
	
	private final ICacheServerID _cacheContext;
	private final JComboBox<IProtocolVersion> _cbProtocols;
	private final JRadioButton _rbClient, _rbServer;
	private final JTextArea _taBody;
	private final PacketDisplay _display;
	
	final Timer _displayUpdater;
	PacketDisplayTask _displayTask;
	
	/**
	 * Constructs this dialog.
	 * 
	 * @param owner Owner window
	 */
	public PacketExplainDialog(Window owner)
	{
		super(owner, "Raw packet content interpreter", ModalityType.MODELESS);
		
		setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
		
		_cacheContext = new DialogCacheContext(this);
		
		_taBody = new JTextArea(40, 80);
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
			final ExplanationSetup setup = getCurrentSetup();
			if (setup == null)
				return;
				
			if (_displayTask != null)
				_displayTask.cancel(true);
				
			final IProtocolVersion version = setup._version;
			_display.setProtocol(version);
			_display.setCacheContext(_cacheContext);
			_displayTask = new PacketDisplayTask(_display);
			_displayTask.execute(new ReceivedPacket(ServiceType.valueOf(version), setup._type, setup._content));
		});
		_displayUpdater.setRepeats(false);
		
		_cbProtocols = new JComboBox<>();
		_cbProtocols.setEditable(false);
		_cbProtocols.addActionListener(e -> _displayUpdater.restart());
		
		{
			final ButtonGroup bg = new ButtonGroup();
			bg.add(_rbClient = new JRadioButton("Client packet", false));
			bg.add(_rbServer = new JRadioButton("Server packet", true));
			
			_rbClient.addActionListener(e -> _displayUpdater.restart());
			_rbServer.addActionListener(e -> _displayUpdater.restart());
		}
		
		final Container contentPane = getContentPane();
		contentPane.setLayout(new BorderLayout());
		
		final JPanel root = new JPanel(new BorderLayout());
		final JPanel n = new JPanel();
		n.setLayout(new BoxLayout(n, BoxLayout.Y_AXIS));
		
		final JPanel endpointPanel = new JPanel();
		endpointPanel.add(_rbClient);
		endpointPanel.add(_rbServer);
		
		n.add(endpointPanel);
		n.add(_cbProtocols);
		
		root.add(n, BorderLayout.NORTH);
		root.add(new JScrollPane(_taBody), BorderLayout.CENTER);
		
		contentPane.add(root, BorderLayout.CENTER);
		contentPane.add(_display, BorderLayout.EAST);
		
		setLocationByPlatform(true);
		pack();
	}
	
	@Override
	public void setVisible(boolean visible)
	{
		if (!visible)
		{
			super.setVisible(visible);
			return;
		}
		
		final VersionnedPacketTable table = VersionnedPacketTable.getInstance();
		{
			IProtocolVersion prev = selectedOrDefault(_cbProtocols);
			_cbProtocols.removeAllItems();
			for (final IProtocolVersion pv : table.getKnownProtocols(ServiceType.GAME))
				_cbProtocols.addItem(pv);
			if (prev == null)
				prev = _cbProtocols.getItemAt(_cbProtocols.getItemCount() - 1);
			_cbProtocols.setSelectedItem(prev);
		}
		
		super.setVisible(visible);
	}
	
	private ExplanationSetup getCurrentSetup()
	{
		final IProtocolVersion version = selectedOrDefault(_cbProtocols);
		if (version == null)
			return null;
			
		return new ExplanationSetup(version, EndpointType.valueOf(_rbClient.isSelected()), HexUtil.hexStringToBytes(_taBody.getText().replace('\r', ' ').replace('\n', ' ')));
	}
	
	private static final class ExplanationSetup
	{
		final IProtocolVersion _version;
		final EndpointType _type;
		final byte[] _content;
		
		ExplanationSetup(IProtocolVersion version, EndpointType type, byte[] content)
		{
			_version = version;
			_type = type;
			_content = content;
		}
	}
	
	private static final class DialogCacheContext extends NotARealProxyObject<JDialog>implements ICacheServerID
	{
		DialogCacheContext(PacketExplainDialog dialog)
		{
			super(dialog);
		}
		
		@Override
		public String toString()
		{
			return "Explanation dialog";
		}
	}
	
	private static final <T> T selectedOrDefault(JComboBox<? extends T> comboBox)
	{
		return selectedOrDefault(comboBox, comboBox.getItemAt(comboBox.getItemCount() - 1));
	}
	
	private static final <T> T selectedOrDefault(JComboBox<? extends T> comboBox, T defaultValue)
	{
		final T selection = comboBox.getItemAt(comboBox.getSelectedIndex());
		return selection != null ? selection : defaultValue;
	}
}
