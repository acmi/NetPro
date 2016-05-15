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
package net.l2emuproject.proxy.ui.savormix.io.dialog;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.text.NumberFormat;
import java.text.ParseException;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import net.l2emuproject.network.protocol.IProtocolVersion;
import net.l2emuproject.network.protocol.ProtocolVersionManager;
import net.l2emuproject.proxy.io.IOConstants;
import net.l2emuproject.proxy.io.definitions.VersionnedPacketTable;
import net.l2emuproject.proxy.io.packetlog.LogFileHeader;
import net.l2emuproject.proxy.ui.savormix.component.GcInfoDialog.MemorySizeUnit;
import net.l2emuproject.proxy.ui.savormix.io.LogLoadOptions;
import net.l2emuproject.proxy.ui.savormix.io.task.LogLoadTask;
import net.l2emuproject.proxy.ui.savormix.loader.Loader;

/**
 * Displays basic information about a log file to be loaded.
 * 
 * @author savormix
 */
public class LogLoadHelper extends JDialog implements ActionListener, KeyListener, IOConstants, SwingConstants
{
	private static final long serialVersionUID = -2442186373243909687L;
	
	private final LogFileHeader _header;
	private final IProtocolVersion _defaultLegacyProtocol;
	
	private final JComboBox<IProtocolVersion> _protocol;
	private final JComboBox<String> _direction;
	private final NumberFormat _format;
	private final JFormattedTextField _packetOffset;
	private final JFormattedTextField _packetCount;
	private final JCheckBox _displayable;
	
	private final JButton _ok;
	
	/**
	 * Constructs this dialog.
	 * 
	 * @param owner parent window
	 * @param header associated log file header
	 * @param defaultLegacyProtocol associated [legacy] log's assumed protocol version
	 */
	public LogLoadHelper(final Window owner, LogFileHeader header, IProtocolVersion defaultLegacyProtocol)
	{
		super(owner, header.getLogFile().getFileName().toString(), ModalityType.MODELESS);
		
		_header = header;
		_defaultLegacyProtocol = defaultLegacyProtocol;
		
		setResizable(false);
		setLocationByPlatform(true);
		setAlwaysOnTop(true);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		
		_format = NumberFormat.getIntegerInstance(Loader.getLocale());
		JPanel _options = new JPanel(new BorderLayout());
		{
			final JPanel north = new JPanel(new BorderLayout());
			{
				final JPanel northWest = new JPanel(new GridLayout(0, 1));
				final JPanel northEast = new JPanel(new GridLayout(0, 1));
				{
					northWest.add(new JLabel("Log size"));
					final long size = header.getLogFile().toFile().length();
					MemorySizeUnit dm = MemorySizeUnit.GIBIBYTES;
					for (int i = MemorySizeUnit.values().length - 2; i >= 0 && size < dm.getDivider(); i--)
						dm = MemorySizeUnit.values()[i];
					northEast.add(new JLabel(dm.format(size, true), RIGHT));
				}
				{
					northWest.add(new JLabel("Log version"));
					JLabel ver = new JLabel(_format.format(header.getVersion()), RIGHT);
					if (header.getVersion() > LOG_VERSION)
						ver.setForeground(Color.RED.darker());
					northEast.add(ver);
				}
				{
					northWest.add(new JLabel("Packet count"));
					final String total = header.getPackets() < 0 ? "N/A" : _format.format(header.getPackets());
					northEast.add(new JLabel(total, RIGHT));
				}
				north.add(northWest, BorderLayout.LINE_START);
				north.add(northEast, BorderLayout.CENTER);
				north.setBorder(BorderFactory.createTitledBorder("Details"));
			}
			_options.add(north, BorderLayout.NORTH);
		}
		{
			final JPanel s = new JPanel();
			s.setLayout(new BoxLayout(s, BoxLayout.PAGE_AXIS));
			{
				final JPanel set = new JPanel(new GridLayout(0, 2));
				{
					final JLabel dir = new JLabel("Direction");
					_direction = new JComboBox<>();
					_direction.setEditable(false);
					_direction.addItem("top down");
					// TODO: support for all versions... maybe.
					if (header.getVersion() >= 5)
						_direction.addItem("bottom up");
					_direction.setSelectedIndex(0);
					dir.setLabelFor(_direction);
					//set.add(dir);
					//set.add(_direction);
				}
				{
					final JLabel off = new JLabel("Offset");
					_packetOffset = new JFormattedTextField(_format);
					_packetOffset.setValue(0);
					off.setLabelFor(_packetOffset);
					// set.add(off);
					// set.add(_packetOffset);
				}
				{
					final JLabel cnt = new JLabel("Count");
					final int count = header.getPackets() < 0 ? Integer.MAX_VALUE : header.getPackets();
					_packetCount = new JFormattedTextField(_format);
					_packetCount.setValue(count);
					cnt.setLabelFor(_packetCount);
					// set.add(cnt);
					// set.add(_packetCount);
				}
				s.add(set);
				
				final JPanel pro = new JPanel();
				{
					final VersionnedPacketTable table = VersionnedPacketTable.getInstance();
					_protocol = new JComboBox<IProtocolVersion>();
					for (final IProtocolVersion pv : table.getKnownProtocols(header.getService()))
						_protocol.addItem(pv);
					
					if (header.getProtocol() == -1 && _defaultLegacyProtocol != null)
						_protocol.setSelectedItem(_defaultLegacyProtocol);
					else
						_protocol.setSelectedItem(ProtocolVersionManager.getInstance().getProtocol(header.getProtocol(), header.getService().isLogin()));
					
					pro.add(_protocol);
				}
				s.add(pro);
				
				final JPanel ex = new JPanel();
				{
					_displayable = new JCheckBox("Displayable packets only", false);
					_displayable.setToolTipText("Only displayable packets will be loaded.");// Offset & count will only take effect on displayable packets as well.");
					ex.add(_displayable);
				}
				s.add(ex);
			}
			s.setBorder(BorderFactory.createTitledBorder("Settings"));
			
			final JPanel south = new JPanel(new GridLayout(1, 0));
			{
				{
					final JButton load = (_ok = new JButton("Load"));
					load.addActionListener(this);
					south.add(load);
					getRootPane().setDefaultButton(load);
					
					final JButton skip = new JButton("Cancel");
					skip.addActionListener(new ActionListener()
					{
						@Override
						public void actionPerformed(ActionEvent e)
						{
							setVisible(false);
							dispose();
						}
					});
					south.add(skip);
				}
			}
			_options.add(s, BorderLayout.CENTER);
			_options.add(south, BorderLayout.SOUTH);
		}
		getContentPane().add(_options);
		pack();
	}
	
	@Override
	public void actionPerformed(ActionEvent e)
	{
		// these still need tweaking
		if (getCount() <= 0)
		{
			JOptionPane.showMessageDialog(this, "Incorrect count of packets to load.", "Invalid settings", JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		if (_header.getPackets() != -1 && (getOffset() < 0 || (getOffset() >= _header.getPackets())))
		{
			JOptionPane.showMessageDialog(this, "Incorrect offset.", "Invalid settings", JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		setVisible(false);
		dispose();
		
		// load the file
		LogLoadOptions llo = new LogLoadOptions(_header, _protocol.getItemAt(_protocol.getSelectedIndex()), _displayable.isSelected(),
				/* _direction.getSelectedIndex() == 0, getOffset(), */getCount());
		new LogLoadTask(getOwner()).execute(llo);
	}
	
	private int getOffset()
	{
		return convertSafely(_packetOffset.getText());
	}
	
	private int getCount()
	{
		return convertSafely(_packetCount.getText());
	}
	
	private int convertSafely(final String s)
	{
		try
		{
			return _format.parse(s).intValue();
		}
		catch (ParseException e)
		{
			return -1;
		}
	}
	
	@Override
	public void keyTyped(KeyEvent e)
	{
		// ignore
	}
	
	@Override
	public void keyPressed(KeyEvent e)
	{
		if (e.getKeyCode() != KeyEvent.VK_ESCAPE)
			return;
		
		setVisible(false);
		dispose();
	}
	
	@Override
	public void keyReleased(KeyEvent e)
	{
		// ignore
	}
	
	@Override
	public void setVisible(boolean visible)
	{
		super.setVisible(visible);
		
		if (visible)
			_ok.requestFocusInWindow();
	}
}
