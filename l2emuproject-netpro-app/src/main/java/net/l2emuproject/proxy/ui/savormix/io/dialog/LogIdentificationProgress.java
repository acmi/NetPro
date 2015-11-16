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
import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;

import net.l2emuproject.network.protocol.IProtocolVersion;
import net.l2emuproject.network.protocol.ProtocolVersionManager;
import net.l2emuproject.proxy.network.meta.UserDefinedProtocolVersion;
import net.l2emuproject.proxy.ui.savormix.io.dialog.LogIdentificationProgress.LogIdModel.LogIdEntry;
import net.l2emuproject.proxy.ui.savormix.io.task.LogIdentifyTask;

/**
 * A dialog that displays a summary of selected historical log files.
 * 
 * @author savormix
 */
public class LogIdentificationProgress extends JDialog implements ActionListener, SwingConstants
{
	private static final long serialVersionUID = -2467194730621341558L;
	
	private SwingWorker<?, ?> _master;
	
	private final JLabel _status;
	private final JPanel _root;
	private LogIdModel _model;
	private final JButton _cancel;
	
	/**
	 * Constructs this dialog.
	 * 
	 * @param owner parent window
	 * @param master associated I/O task
	 */
	public LogIdentificationProgress(final Window owner, final LogIdentifyTask master)
	{
		super(owner, "Packet log details", ModalityType.MODELESS);
		
		_master = master;
		
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setLocationByPlatform(true);
		// setAlwaysOnTop(true);
		
		addWindowListener(new WindowAdapter()
		{
			@Override
			public void windowClosing(WindowEvent we)
			{
				onCancel();
			}
		});
		
		_root = new JPanel(new BorderLayout());
		{
			_root.add(_status = new JLabel("Identifying files...", CENTER), BorderLayout.NORTH);
			_cancel = new JButton("Cancel");
			_cancel.addActionListener(this);
			_root.add(_cancel, BorderLayout.SOUTH);
			getRootPane().setDefaultButton(_cancel);
		}
		getContentPane().add(_root);
		
		pack();
	}
	
	@Override
	public void actionPerformed(ActionEvent e)
	{
		onCancel();
	}
	
	void onCancel()
	{
		if (_master != null)
			_master.cancel(true);
		
		setVisible(false);
		dispose();
	}
	
	/**
	 * Displays a table of information about given files.
	 * 
	 * @param files paths to files
	 */
	public void assignFiles(Path[] files)
	{
		_model = new LogIdModel(files);
		JTable table = new JTable(_model);
		table.setPreferredSize(new Dimension(900, files.length * 16));
		for (int i = 0; i < LogIdModel.COL_WIDTHS.length; i++)
		{
			TableColumn tc = table.getColumnModel().getColumn(i);
			tc.setPreferredWidth(LogIdModel.COL_WIDTHS[i]);
		}
		_root.add(new JScrollPane(table), BorderLayout.CENTER);
		pack();
	}
	
	/**
	 * Sets the current summary generation status.
	 * 
	 * @param status current status
	 */
	public void setStatus(String status)
	{
		_status.setText(status);
	}
	
	/**
	 * Sets the summary for a concrete file.
	 * 
	 * @param file file table row index
	 * @param status identification result
	 * @param version file version
	 * @param login whether for login service
	 * @param protocol network protocol version
	 */
	public void setFileStatus(int file, String status, Integer version, Boolean login, Integer protocol)
	{
		LogIdEntry e = _model._entries.get(file);
		e._status = status;
		e._version = version;
		e._login = login;
		if (protocol != null)
		{
			final ProtocolVersionManager pvm = ProtocolVersionManager.getInstance();
			final IProtocolVersion pv = login ? pvm.getLoginProtocol(protocol) : pvm.getGameProtocol(protocol);
			final String ver = (protocol != -1) ? String.valueOf(protocol) : "N/A";
			if (pv instanceof UserDefinedProtocolVersion)
			{
				final StringBuilder sb = new StringBuilder(ver);
				sb.append(" (").append(((UserDefinedProtocolVersion)pv).getAlias()).append(')');
				e._protocol = sb.toString();
			}
			else
				e._protocol = ver;
		}
		_model.fireTableRowsUpdated(file, file);
	}
	
	/** Notifies that the associated summarizing task has been completed. */
	public void disableCancel()
	{
		_master = null;
		_cancel.setText("OK");
		_status.setText("Preverification completed.");
	}
	
	static class LogIdModel extends AbstractTableModel
	{
		private static final long serialVersionUID = 8468286276484687221L;
		private static final String[] COL_NAMES = { "File", "Version", "Type", "Protocol", "Result" };
		private static final Class<?>[] COL_TYPES = { String.class, Integer.class, String.class, String.class, String.class };
		static final int[] COL_WIDTHS = { 300, 90, 70, 100, 250 };
		
		final List<LogIdEntry> _entries;
		
		LogIdModel(Path[] files)
		{
			_entries = new ArrayList<>(files.length);
			for (Path f : files)
				_entries.add(new LogIdEntry(f.getFileName().toString()));
		}
		
		@Override
		public int getRowCount()
		{
			return _entries.size();
		}
		
		@Override
		public int getColumnCount()
		{
			return COL_NAMES.length;
		}
		
		@Override
		public String getColumnName(int index)
		{
			return COL_NAMES[index];
		}
		
		@Override
		public Class<?> getColumnClass(int index)
		{
			return COL_TYPES[index];
		}
		
		@Override
		public Object getValueAt(int rowIndex, int columnIndex)
		{
			LogIdEntry e = _entries.get(rowIndex);
			if (e == null)
				return null;
			switch (columnIndex)
			{
				case 0:
					return e._file;
				case 1:
					return e._version;
				case 2:
					if (e._login != null)
						return e._login ? "Login" : "Game";
					return null;
				case 3:
					return e._protocol;
				case 4:
					return e._status;
				default:
					return null;
			}
		}
		
		static class LogIdEntry
		{
			final String _file;
			Integer _version;
			Boolean _login;
			String _protocol;
			String _status;
			
			LogIdEntry(String file)
			{
				_file = file;
				_status = "Pending...";
			}
		}
	}
}
