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
package net.l2emuproject.proxy.ui.savormix.component.packet.config;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.WindowConstants;
import javax.swing.table.TableColumn;

import net.l2emuproject.network.ILoginProtocolVersion;
import net.l2emuproject.network.IProtocolVersion;
import net.l2emuproject.proxy.network.EndpointType;
import net.l2emuproject.proxy.network.ServiceType;
import net.l2emuproject.proxy.network.meta.IPacketTemplate;
import net.l2emuproject.proxy.network.meta.container.OpcodeOwnerSet;
import net.l2emuproject.proxy.ui.listener.BatchPacketDisplayConfigListener;
import net.l2emuproject.proxy.ui.savormix.component.BlockableDialog;
import net.l2emuproject.proxy.ui.savormix.io.PacketDisplayConfigManager;
import net.l2emuproject.proxy.ui.savormix.io.base.IOConstants;
import net.l2emuproject.proxy.ui.savormix.io.exception.InvalidFileException;
import net.l2emuproject.proxy.ui.savormix.io.exception.UnsupportedFileException;
import net.l2emuproject.proxy.ui.savormix.loader.LoadOption;
import net.l2emuproject.ui.AsyncTask;

import javolution.util.FastSet;

/**
 * Allows to specify which packets should [not] be displayed in packet lists.<BR>
 * <BR>
 * This class is a part of a reference GUI provided for education purposes only. These classes
 * should help to understand how to interact with the underlying proxy core.<BR>
 * <BR>
 * Creating an own GUI is the preferred way to start using this application.
 * 
 * @author savormix
 */
public class PacketDisplayConfig extends BlockableDialog implements ActionListener, IOConstants
{
	private static final long serialVersionUID = -1706427065954845853L;
	private static final int[] COLUMN_WIDTH = { 20, 80, 300 };
	
	private final IProtocolVersion _version;
	
	final PacketContainer[] _packetContainers;
	private final Map<EndpointType, JTable> _lists;
	
	private final JPanel _content;
	private final JButton _ok;
	private final JButton _cancel;
	
	private final Set<BatchPacketDisplayConfigListener> _batchListeners;
	
	private JPanel _center;
	
	boolean _exported, _unsavedStateExported;
	File _lastConfig;
	final AsyncTask<?, ?, ?> _loadTask;
	
	/**
	 * Creates a configuration dialog to specify which packets should be displayed.
	 * 
	 * @param owner
	 *            Owner window
	 * @param watermark
	 *            overlay image
	 * @param version
	 *            protocol version
	 * @param initialConfig
	 *            path to initial configuration, if applicable
	 */
	public PacketDisplayConfig(Window owner, BufferedImage watermark, IProtocolVersion version, Path initialConfig)
	{
		super(owner, "Packet display configuration", ModalityType.DOCUMENT_MODAL, watermark);
		
		_version = version;
		
		setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
		addWindowListener(new WindowAdapter()
		{
			@Override
			public void windowClosing(WindowEvent e) // we do not dispose, only hide
			{
				notifyChanges(false);
			}
		});
		
		_packetContainers = new PacketContainer[2];
		_lists = new HashMap<>();
		
		_ok = new JButton("OK");
		_ok.addActionListener(this);
		getRootPane().setDefaultButton(_ok);
		_cancel = new JButton("Cancel");
		_cancel.addActionListener(this);
		
		final JPanel south = new JPanel();
		south.setLayout(new GridLayout(1, 0));
		south.add(_ok);
		south.add(_cancel);
		
		final JPanel root = _content = new JPanel(new BorderLayout());
		root.add(south, BorderLayout.SOUTH);
		{
			final JPanel north = new JPanel(new GridLayout(1, 0));
			{
				{
					final JButton export = new JButton("Export configuration...");
					export.setEnabled(LoadOption.DISABLE_DEFS.isNotSet());
					export.addActionListener(e -> new ConfigExportBlockingTask(PacketDisplayConfig.this, _lastConfig.getParentFile()).execute(_packetContainers));
					north.add(export);
				}
				{
					final JButton export = new JButton("Import configuration...");
					export.setEnabled(LoadOption.DISABLE_DEFS.isNotSet());
					export.addActionListener(e ->
					{
						if (isUnsaved() || !_exported)
						{
							final int answer = JOptionPane.showConfirmDialog(PacketDisplayConfig.this, "This will override current configuration. Do you want to continue?", "Unsaved changes",
									JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
							if (answer != JOptionPane.YES_OPTION)
								return;
						}
						
						startImport(false);
					});
					north.add(export);
				}
				{
					final JButton export = new JButton("Reload packets...");
					export.setEnabled(LoadOption.DISABLE_DEFS.isNotSet());
					export.addActionListener(e ->
					{
						final int answer = JOptionPane.showConfirmDialog(PacketDisplayConfig.this, "This will override all linked configurations and cannot be reverted. Do you want to continue?",
								"Chain reaction warning", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
						if (answer != JOptionPane.YES_OPTION)
							return;
							
						new PacketDefinitionLoadTask(PacketDisplayConfig.this).execute((Void[])null);
					});
					north.add(export);
				}
			}
			root.add(north, BorderLayout.NORTH);
		}
		getActualContentPane().add(_content);
		
		_batchListeners = FastSet.newInstance();
		
		setPreferredSize(new Dimension(/*1024*/800, 600)); // avoid overly large size
		setLocationByPlatform(true);
		// pack();
		
		{
			final Path preload = APPLICATION_DIRECTORY.resolve(version.getVersion() + "." + DISPLAY_CONFIG_EXTENSION);
			final Path cfg = initialConfig != null ? initialConfig : preload;
			_loadTask = new PacketContainerLoadTask(this, cfg).execute(true);
			_lastConfig = cfg.toFile();
			if (cfg != preload)
				_exported = true;
		}
	}
	
	@Override
	public void setVisible(boolean visible)
	{
		if (_loadTask.isDone())
		{
			super.setVisible(visible);
			return;
		}
		
		new BlockingTask<Window, Void, Void, Void>(getOwner())
		{
			@Override
			protected Void doInBackground(Void... params)
			{
				try
				{
					_loadTask.get();
				}
				catch (Exception e)
				{
					// ignore
				}
				return null;
			}
			
			@Override
			protected void onPostExecute(Void result)
			{
				super.onPostExecute(result);
				
				setVisible(true);
			}
		}.execute((Void[])null);
	}
	
	/**
	 * Associates a packet display configuration file with this component.
	 * 
	 * @param file saved packet display configuration
	 */
	public void setLastConfig(File file)
	{
		_lastConfig = file;
		_exported = _unsavedStateExported = true;
	}
	
	/**
	 * Returns the associated packet display configuration file.
	 * 
	 * @return saved packet display configuration or {@code null}
	 */
	public Path getLastConfig()
	{
		return _exported ? _lastConfig.toPath() : null;
	}
	
	/**
	 * Attempts to load a packet display configuration.
	 * 
	 * @param skipSelect whether to reload the currently associated file
	 */
	public void startImport(boolean skipSelect)
	{
		new ConfigImportBlockingTask(this, skipSelect ? _lastConfig : _lastConfig.getParentFile(), skipSelect).execute((Void[])null);
	}
	
	void loadInBackground(Path config, boolean optional) throws IOException, InvalidFileException, UnsupportedFileException
	{
		final PacketContainer[] containers = new PacketContainer[_packetContainers.length];
		final OpcodeOwnerSet[] sets = new OpcodeOwnerSet[_packetContainers.length];
		
		final boolean login = _version instanceof ILoginProtocolVersion;
		for (final EndpointType type : EndpointType.values())
		{
			containers[type.ordinal() & 1] = new PacketContainer(_version, type);
			sets[type.ordinal() & 1] = new OpcodeOwnerSet();
			sets[type.ordinal() & 1].addAll(containers[type.ordinal() & 1].getCommitted());
		}
		
		try
		{
			PacketDisplayConfigManager.getInstance().load(config, ServiceType.valueOf(login), sets[0], sets[1]);
		}
		catch (NoSuchFileException e)
		{
			if (!optional)
				throw e;
		}
		
		for (final EndpointType proxyType : EndpointType.values())
		{
			final int idx = proxyType.ordinal() & 1;
			final PacketContainer pc = containers[idx];
			{
				pc.getSelected().addAll(pc.getCommitted());
				pc.getSelected().retainAll(sets[idx]);
			}
			{
				pc.getDeselected().addAll(pc.getCommitted());
				pc.getDeselected().removeAll(pc.getSelected());
			}
		}
		
		// silently replace containers (while UI is blocked)
		System.arraycopy(containers, 0, _packetContainers, 0, containers.length);
	}
	
	void setupUIAfterLoad()
	{
		for (final EndpointType type : EndpointType.values())
		{
			final PacketDisplayConfigListModel model = new PacketDisplayConfigListModel(_packetContainers[type.ordinal() & 1]);
			
			final JTable list = new JTable(model);
			{
				list.setColumnSelectionAllowed(false);
				list.setRowSelectionAllowed(true);
				list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
				list.getSelectionModel().addListSelectionListener(e ->
				{
					final int row = e.getFirstIndex();
					model.toggleAtRow(row);
					list.clearSelection();
				});
				for (int i = 0; i < list.getModel().getColumnCount(); i++)
				{
					final TableColumn tc = list.getColumnModel().getColumn(i);
					tc.setPreferredWidth(COLUMN_WIDTH[i]);
				}
				list.setFillsViewportHeight(true);
			}
			_lists.put(type, list);
		}
		
		final JPanel center = new JPanel(new GridLayout(1, 0));
		{
			/*
			{
				final JPanel cl = new JPanel(new GridLayout(0, 1));
				{
					cl.add(wrapList(_lists.get(LOGIN_CLIENT), "L2 client - to login"));
					cl.add(wrapList(_lists.get(LOGIN_SERVER), "Login server"));
				}
				center.add(cl);
			}
			*/
			center.add(wrapList(_lists.get(EndpointType.CLIENT), "Client"));
			center.add(wrapList(_lists.get(EndpointType.SERVER), "Server"));
		}
		
		final boolean firstTime = (_center == null);
		
		if (!firstTime)
			_content.remove(_center);
		_content.add(_center = center, BorderLayout.CENTER);
		if (firstTime)
			pack();
		else
			_content.revalidate();
	}
	
	@SuppressWarnings("static-method")
	private JPanel wrapList(final JTable list, String title)
	{
		final JPanel panel = new JPanel(new BorderLayout());
		panel.setBorder(BorderFactory.createTitledBorder(title));
		{
			panel.add(new JScrollPane(list), BorderLayout.CENTER);
			final JPanel btns = new JPanel(new GridLayout(1, 0));
			{
				final JButton all = new JButton("All");
				all.addActionListener(e ->
				{
					final PacketDisplayConfigListModel model = (PacketDisplayConfigListModel)list.getModel();
					model.setAll(true);
				});
				btns.add(all);
			}
			{
				final JButton none = new JButton("None");
				none.addActionListener(e ->
				{
					final PacketDisplayConfigListModel model = (PacketDisplayConfigListModel)list.getModel();
					model.setAll(false);
				});
				btns.add(none);
			}
			panel.add(btns, BorderLayout.SOUTH);
		}
		return panel;
	}
	
	/**
	 * Adds a configuration change listener.
	 * 
	 * @param listener
	 *            listener
	 */
	public void addListener(BatchPacketDisplayConfigListener listener)
	{
		getBatchListeners().add(listener);
		
		if (_loadTask.isDone())
			notifyCurrentState(listener);
	}
	
	/*
	void notifyCurrentState()
	{
		for (BatchPacketDisplayConfigListener l : _batchListeners)
			notifyCurrentState(l);
	}
	*/
	
	private void notifyCurrentState(BatchPacketDisplayConfigListener listener)
	{
		final Set<IPacketTemplate> client = getPackets(EndpointType.CLIENT), server = getPackets(EndpointType.SERVER);
		listener.displayConfigChanged(getVersion(), client, null, null, server, null, null);
	}
	
	@Override
	public void actionPerformed(ActionEvent e)
	{
		setVisible(false);
		
		final boolean keepExportedFlag = _unsavedStateExported;
		_unsavedStateExported = false;
		
		if (e.getSource() != _ok)
		{
			// undo changes internally
			notifyChanges(false);
			return;
		}
		
		if (!isUnsaved())
			return;
			
		if (!keepExportedFlag)
			_exported = false;
			
		propagateConfig();
	}
	
	void propagateConfig()
	{
		// propagate changes
		for (final PacketContainer pc : _packetContainers)
		{
			pc.getCommitted().removeAll(pc.getDeselected());
			pc.getCommitted().addAll(pc.getSelected());
		}
		
		// notify listeners
		final Set<IPacketTemplate> cn = getNewPackets(EndpointType.CLIENT), cr = getOldPackets(EndpointType.CLIENT);
		final Set<IPacketTemplate> sn = getNewPackets(EndpointType.SERVER), sr = getOldPackets(EndpointType.SERVER);
		if (containsNotEmpty(cn, cr, sn, sr))
		{
			for (final BatchPacketDisplayConfigListener pdcl : getBatchListeners())
			{
				pdcl.displayConfigChanged(getVersion(), getPackets(EndpointType.CLIENT), cn, cr, getPackets(EndpointType.SERVER), sn, sr);
			}
		}
		
		// accept changes internally
		notifyChanges(true);
	}
	
	private static boolean containsNotEmpty(Set<?>... array)
	{
		for (final Set<?> set : array)
			if (set != null && !set.isEmpty())
				return true;
				
		return false;
	}
	
	boolean isUnsaved()
	{
		for (final PacketContainer pc : _packetContainers)
		{
			final JTable list = _lists.get(pc.getProxyType());
			final PacketDisplayConfigListModel model = (PacketDisplayConfigListModel)list.getModel();
			if (model.isInTransaction())
				return true;
		}
		
		return false;
	}
	
	void notifyChanges(boolean apply)
	{
		for (final PacketContainer pc : _packetContainers)
		{
			final JTable list = _lists.get(pc.getProxyType());
			// initial load
			if (list == null)
				continue;
				
			final PacketDisplayConfigListModel model = (PacketDisplayConfigListModel)list.getModel();
			if (apply)
				model.commit();
			else
				model.rollback();
		}
	}
	
	private PacketContainer getPacketContainer(EndpointType type)
	{
		return _packetContainers[type.ordinal() & 1];
	}
	
	/**
	 * Returns packets selected to be displayed.
	 * 
	 * @param type client/server
	 * @return displayed packets
	 */
	public Set<IPacketTemplate> getPackets(EndpointType type)
	{
		return getPacketContainer(type).getCommitted();
	}
	
	private Set<IPacketTemplate> getNewPackets(EndpointType type)
	{
		return getPacketContainer(type).getSelected();
	}
	
	private Set<IPacketTemplate> getOldPackets(EndpointType type)
	{
		return getPacketContainer(type).getDeselected();
	}
	
	private Set<BatchPacketDisplayConfigListener> getBatchListeners()
	{
		return _batchListeners;
	}
	
	/**
	 * Returns the network protocol version associated with this component.
	 * 
	 * @return network protocol version
	 */
	public IProtocolVersion getVersion()
	{
		return _version;
	}
	
	/*
	@Deprecated
	private static void writeReport(ProxyType type)
	{
		final Set<PacketInfo> all = PacketTable.getInstance().getKnownPackets(type);
		final PacketContainer pc = new PacketContainer(type);
		// PacketDisplayConfigManager.getInstance().load(null, (type == ProxyType.LOGIN_CLIENT) ? all : null, (type == ProxyType.LOGIN_SERVER) ? all : null, (type == ProxyType.GAME_CLIENT) ? all : null, (type == ProxyType.GAME_SERVER) ? all : null)
		
		final StringBuilder sb = new StringBuilder();
		for (final PacketInfo pi : all)
		{
			sb.append(HexUtil.fillHex(pi.getMainOpcode(), 2));
			int i;
			for (i = 0; i < pi.getAdditionalOpcodes().length; i++)
				sb.append(':').append(HexUtil.fillHex(pi.getAdditionalOpcodes()[i], 2));
			for (; i < 2; i++)
				sb.append("   ");
			sb.append(' ');
			if (pc.getCommitted().contains(pi))
				sb.append("PENDING");
			else
				sb.append("TESTED ");
			sb.append(' ').append(pi.getName());
			System.out.println(sb);
			sb.setLength(0);
		}
	}
	
	public static void main(String[] args)
	{
		System.out.println("Client packets");
		writeReport(ProxyType.GAME_CLIENT);
		System.out.println("Server packets");
		writeReport(ProxyType.GAME_SERVER);
	}
	*/
}
