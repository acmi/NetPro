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
package net.l2emuproject.proxy.ui.savormix.component.conv;

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.nio.file.Path;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import net.l2emuproject.proxy.network.EndpointType;
import net.l2emuproject.proxy.ui.savormix.io.PacketLogChooser;
import net.l2emuproject.proxy.ui.savormix.io.base.IOConstants;
import net.l2emuproject.proxy.ui.savormix.io.conv.ToRawStreamVisitor;
import net.l2emuproject.proxy.ui.savormix.io.task.HistoricalLogPacketVisitor;
import net.l2emuproject.proxy.ui.savormix.io.task.LogVisitationTask;

/**
 * Defines a dialog that allows the user to specify stream output configuration.
 * 
 * @author _dev_
 */
public class StreamOptionDialog extends JDialog implements ActionListener, IOConstants
{
	private static final long serialVersionUID = -2199822948621597871L;
	
	private final JRadioButton _client, _server, _mixed;
	private final JCheckBox _timestamps, _reencipher;
	
	private final PacketLogChooser _fileChooser;
	
	/**
	 * Constructs this dialog.
	 * 
	 * @param owner associated window
	 */
	public StreamOptionDialog(Window owner)
	{
		super(owner, "Select stream options", ModalityType.DOCUMENT_MODAL);
		
		setDefaultCloseOperation(HIDE_ON_CLOSE);
		
		_client = new JRadioButton("Client", false);
		_server = new JRadioButton("Server", false);
		_mixed = new JRadioButton("Mixed", true);
		
		_timestamps = new JCheckBox("Timestamps", false);
		_reencipher = new JCheckBox("Re-encipher", false);
		
		final ButtonGroup grp = new ButtonGroup();
		grp.add(_client);
		grp.add(_server);
		grp.add(_mixed);
		
		_fileChooser = new PacketLogChooser(LOG_DIRECTORY.toFile(), null);
		
		final JButton next = new JButton("OK");
		next.addActionListener(this);
		getRootPane().setDefaultButton(next);
		
		final JPanel root = new JPanel();
		root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
		{
			final JPanel rbPanel = new JPanel();
			rbPanel.add(_client);
			rbPanel.add(_server);
			rbPanel.add(_mixed);
			root.add(rbPanel);
		}
		{
			final JPanel cbPanel = new JPanel();
			cbPanel.add(_timestamps);
			cbPanel.add(_reencipher);
			root.add(cbPanel);
		}
		{
			root.add(next);
		}
		add(root);
		
		setResizable(false);
		setLocationByPlatform(true);
		pack();
	}
	
	@Override
	public void actionPerformed(ActionEvent e)
	{
		final int result = _fileChooser.showOpenDialog(this);
		if (result != JFileChooser.APPROVE_OPTION)
			return;
			
		final File[] selected = _fileChooser.getSelectedFiles();
		final Path[] targets = new Path[selected.length];
		for (int i = 0; i < targets.length; ++i)
			targets[i] = selected[i].toPath();
			
		final HistoricalLogPacketVisitor visitor = new ToRawStreamVisitor(_client.isSelected() ? EndpointType.CLIENT : (_server.isSelected() ? EndpointType.SERVER : null), _timestamps.isSelected(),
				_reencipher.isSelected());
		new LogVisitationTask(getOwner(), "Converting", visitor).execute(targets);
		
		setVisible(false);
	}
}
