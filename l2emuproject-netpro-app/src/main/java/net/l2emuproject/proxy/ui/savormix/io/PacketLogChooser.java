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
package net.l2emuproject.proxy.ui.savormix.io;

import java.awt.Component;
import java.awt.Container;
import java.awt.GridLayout;
import java.awt.HeadlessException;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLayer;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import net.l2emuproject.network.protocol.IProtocolVersion;
import net.l2emuproject.proxy.io.definitions.VersionnedPacketTable;
import net.l2emuproject.proxy.network.ServiceType;
import net.l2emuproject.proxy.ui.savormix.EventSink;
import net.l2emuproject.proxy.ui.savormix.component.DisabledComponentUI;
import net.l2emuproject.proxy.ui.savormix.component.WatermarkPane;
import net.l2emuproject.proxy.ui.savormix.io.PacketLogFilter.LegacyLogFilter;
import net.l2emuproject.proxy.ui.savormix.loader.LoadOption;

/**
 * Allows user to select which packet logs to load.
 * 
 * @author savormix
 */
public class PacketLogChooser extends JFileChooser implements EventSink, SwingConstants
{
	private static final long serialVersionUID = -7252340594958110276L;
	
	private final BufferedImage _watermark;
	private final WatermarkPane _sink;
	private final DisabledComponentUI _blockFeedback;
	
	final PacketLogFilter _filter;
	final JCheckBox _cbLegacy;
	final JComboBox<IProtocolVersion> _cbLegacyLoginProtocol = new JComboBox<>(), _cbLegacyGameProtocol = new JComboBox<>();
	
	private JDialog _dialog;
	
	/**
	 * Constructs a file selection dialog.
	 * 
	 * @param directory initial directory to open
	 * @param watermark overlay image tile
	 */
	public PacketLogChooser(File directory, BufferedImage watermark)
	{
		super(directory);
		
		_watermark = watermark;
		_blockFeedback = new DisabledComponentUI();
		_sink = new WatermarkPane(_watermark);
		
		setAcceptAllFileFilterUsed(true);
		setFileFilter(_filter = new PacketLogFilter());
		setDialogType(OPEN_DIALOG);
		setMultiSelectionEnabled(true);
		
		final Runnable rescan = this::rescanCurrentDirectory;
		{
			final JPanel control = new JPanel();
			{
				final JPanel filter = new JPanel();
				filter.setLayout(new BoxLayout(filter, BoxLayout.Y_AXIS));
				// filter.setBorder(BorderFactory.createEtchedBorder());
				_cbLegacy = new JCheckBox("Include legacy logs", _filter.isLegacyFiltered());
				{
					final JCheckBox cbDisplayableOnly = new JCheckBox("Contains displayable packets", _filter.isDisplayable());
					cbDisplayableOnly.setEnabled(LoadOption.DISABLE_DEFS.isNotSet());
					cbDisplayableOnly.setAlignmentX(LEFT_ALIGNMENT);
					cbDisplayableOnly.addActionListener(e ->
					{
						// startIgnoringEvents();
						
						_filter.setDisplayable(cbDisplayableOnly.isSelected());
						_cbLegacy.setEnabled(cbDisplayableOnly.isSelected());
						
						SwingUtilities.invokeLater(rescan);
					});
					filter.add(cbDisplayableOnly);
					
					_cbLegacy.setEnabled(cbDisplayableOnly.isSelected());
				}
				{
					final JPanel sub = new JPanel();
					sub.setLayout(new BoxLayout(sub, BoxLayout.X_AXIS));
					sub.setAlignmentX(LEFT_ALIGNMENT);
					{
						_cbLegacy.setAlignmentX(LEFT_ALIGNMENT);
						_cbLegacy.addActionListener(e ->
						{
							final boolean legacy = _cbLegacy.isSelected();
							_cbLegacyLoginProtocol.setEnabled(legacy);
							_cbLegacyGameProtocol.setEnabled(legacy);
							
							_filter.setLegacyFilter(legacy ? new LegacyLogFilter(selectedOrDefault(_cbLegacyLoginProtocol), selectedOrDefault(_cbLegacyGameProtocol)) : null);
							
							if (legacy)
								SwingUtilities.invokeLater(rescan);
						});
						sub.add(Box.createHorizontalStrut(20));
						sub.add(_cbLegacy);
					}
					filter.add(sub);
				}
				{
					final JPanel sub = new JPanel();
					sub.setLayout(new BoxLayout(sub, BoxLayout.X_AXIS));
					sub.setAlignmentX(LEFT_ALIGNMENT);
					{
						final JPanel indented = new JPanel(new GridLayout(0, 1));
						{
							_cbLegacyLoginProtocol.setEnabled(_cbLegacy.isSelected());
							_cbLegacyGameProtocol.setEnabled(_cbLegacy.isSelected());
							
							_cbLegacyLoginProtocol.addItemListener(new ItemListener()
							{
								private Object _lastItem;
								
								@Override
								public void itemStateChanged(ItemEvent e)
								{
									if (e.getStateChange() != ItemEvent.SELECTED)
										return;
										
									final Object current = selectedOrDefault(_cbLegacyLoginProtocol);
									if (_lastItem == current)
										return;
									if (_lastItem != null && _lastItem.equals(current))
										return;
									_lastItem = current;
									
									if (_cbLegacy.isSelected())
									{
										_filter.setLegacyFilter(new LegacyLogFilter(selectedOrDefault(_cbLegacyLoginProtocol), selectedOrDefault(_cbLegacyGameProtocol)));
										SwingUtilities.invokeLater(rescan);
									}
								}
							});
							_cbLegacyGameProtocol.addItemListener(new ItemListener()
							{
								private Object _lastItem;
								
								@Override
								public void itemStateChanged(ItemEvent e)
								{
									if (e.getStateChange() != ItemEvent.SELECTED)
										return;
										
									final Object current = selectedOrDefault(_cbLegacyGameProtocol);
									if (_lastItem == current)
										return;
									if (_lastItem != null && _lastItem.equals(current))
										return;
									_lastItem = current;
									
									if (_cbLegacy.isSelected())
									{
										_filter.setLegacyFilter(new LegacyLogFilter(selectedOrDefault(_cbLegacyLoginProtocol), selectedOrDefault(_cbLegacyGameProtocol)));
										SwingUtilities.invokeLater(rescan);
									}
								}
							});
							
							indented.add(_cbLegacyLoginProtocol);
							indented.add(_cbLegacyGameProtocol);
						}
						sub.add(Box.createHorizontalStrut(20));
						sub.add(indented);
					}
					filter.add(sub);
				}
				control.add(filter);
			}
			setAccessory(control);
		}
	}
	
	@Override
	public int showDialog(Component parent, String approveButtonText) throws HeadlessException
	{
		final boolean realState = _cbLegacy.isSelected();
		if (realState)
			_cbLegacy.setSelected(false);
			
		final VersionnedPacketTable table = VersionnedPacketTable.getInstance();
		{
			IProtocolVersion prev = selectedOrDefault(_cbLegacyLoginProtocol);
			_cbLegacyLoginProtocol.removeAllItems();
			for (final IProtocolVersion pv : table.getKnownProtocols(ServiceType.LOGIN))
				_cbLegacyLoginProtocol.addItem(pv);
			if (prev == null)
				prev = _cbLegacyLoginProtocol.getItemAt(_cbLegacyLoginProtocol.getItemCount() - 1);
			_cbLegacyLoginProtocol.setSelectedItem(prev);
		}
		{
			IProtocolVersion prev = selectedOrDefault(_cbLegacyGameProtocol);
			_cbLegacyGameProtocol.removeAllItems();
			for (final IProtocolVersion pv : table.getKnownProtocols(ServiceType.GAME))
				_cbLegacyGameProtocol.addItem(pv);
			if (prev == null)
				prev = _cbLegacyGameProtocol.getItemAt(_cbLegacyGameProtocol.getItemCount() - 1);
			_cbLegacyGameProtocol.setSelectedItem(prev);
		}
		
		if (realState)
		{
			_cbLegacy.setSelected(true);
		}
		
		return super.showDialog(parent, approveButtonText);
	}
	
	@Override
	protected JDialog createDialog(Component parent) throws HeadlessException
	{
		final JDialog dlg = super.createDialog(parent);
		
		final Container root = dlg.getContentPane();
		dlg.setContentPane(new JLayer<>(root, _blockFeedback));
		
		dlg.setGlassPane(_sink);
		dlg.getGlassPane().setVisible(true);
		
		return _dialog = dlg;
	}
	
	@Override
	public void startIgnoringEvents()
	{
		_sink.startIgnoringEvents();
		_blockFeedback.setActive(true);
		
		if (_dialog != null)
			_dialog.repaint();
	}
	
	@Override
	public void stopIgnoringEvents()
	{
		_sink.stopIgnoringEvents();
		_blockFeedback.setActive(false);
		
		if (_dialog != null)
			_dialog.repaint();
	}
	
	/**
	 * Returns a protocol version to automatically select for legacy login packet log formats.
	 * 
	 * @return legacy login log protocol version
	 */
	public IProtocolVersion getDefaultLegacyLoginProtocol()
	{
		return selectedOrDefault(_cbLegacyLoginProtocol);
	}
	
	/**
	 * Returns a protocol version to automatically select for legacy game packet log formats.
	 * 
	 * @return legacy game log protocol version
	 */
	public IProtocolVersion getDefaultLegacyGameProtocol()
	{
		return selectedOrDefault(_cbLegacyGameProtocol);
	}
	
	static final <T> T selectedOrDefault(JComboBox<? extends T> comboBox)
	{
		return selectedOrDefault(comboBox, comboBox.getItemAt(comboBox.getItemCount() - 1));
	}
	
	private static final <T> T selectedOrDefault(JComboBox<? extends T> comboBox, T defaultValue)
	{
		final T selection = comboBox.getItemAt(comboBox.getSelectedIndex());
		return selection != null ? selection : defaultValue;
	}
}
