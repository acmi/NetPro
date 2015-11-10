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
package net.l2emuproject.proxy.ui.savormix.component;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.plaf.basic.BasicButtonUI;

import net.l2emuproject.lang.L2TextBuilder;
import net.l2emuproject.network.protocol.ClientProtocolVersion;
import net.l2emuproject.network.protocol.IProtocolVersion;
import net.l2emuproject.proxy.config.ProxyConfig;
import net.l2emuproject.proxy.network.Proxy;
import net.l2emuproject.proxy.network.ServiceType;
import net.l2emuproject.proxy.network.listener.ConnectionListener;
import net.l2emuproject.proxy.network.listener.PacketListener;
import net.l2emuproject.proxy.network.login.client.L2LoginClient;
import net.l2emuproject.proxy.network.meta.IPacketTemplate;
import net.l2emuproject.proxy.setup.IPAliasManager;
import net.l2emuproject.proxy.state.entity.context.ServerSocketID;
import net.l2emuproject.proxy.ui.ReceivedPacket;
import net.l2emuproject.proxy.ui.listener.BatchPacketDisplayConfigListener;
import net.l2emuproject.proxy.ui.savormix.IconUtils;
import net.l2emuproject.proxy.ui.savormix.component.packet.PacketList;
import net.l2emuproject.proxy.ui.savormix.component.packet.PacketList.ListCaptureState;
import net.l2emuproject.proxy.ui.savormix.io.task.HistoricalPacketLog;
import net.l2emuproject.proxy.ui.savormix.loader.Frontend.CaptureSettingAccessor;
import net.l2emuproject.proxy.ui.savormix.loader.LoadOption;
import net.l2emuproject.util.HexUtil;
import net.l2emuproject.util.L2FastSet;
import net.l2emuproject.util.concurrent.MapUtils;
import net.l2emuproject.util.logging.L2Logger;

import javolution.util.FastMap;

/**
 * Displays active and terminated connections as tabs. A placeholder tab is displayed before any
 * connections have been made. <BR>
 * <BR>
 * This class is a part of a reference GUI provided for education purposes only. These classes
 * should help to understand how to interact with the underlying proxy core. <BR>
 * <BR>
 * Creating an own GUI is the preferred way to start using this application.
 * 
 * @author savormix
 */
public final class ConnectionPane extends JTabbedPane implements ConnectionListener, PacketListener, BatchPacketDisplayConfigListener
{
	/** This flag indicates that currently open packet lists should be cleared. */
	public static final int FLAG_LM_PURGE_LISTS = 1 << 0;
	/** This flag indicates that tabs associated with terminated connections should be closed. */
	public static final int FLAG_LM_DROP_LISTS_INACTIVE = 1 << 1;
	/** This flag indicates that tabs associated with log files should be closed. */
	public static final int FLAG_LM_DROP_LISTS_LOGFILE = 1 << 2;
	
	private static final long serialVersionUID = 4044365991617331058L;
	static final L2Logger LOG = L2Logger.getLogger(ConnectionPane.class);
	private static final String IDLE = "Idle";
	private static final String CONSOLE = "Console";
	
	private final AtomicInteger _number;
	final Map<Proxy, PacketList> _live;
	private final Set<PacketList> _offline, _logFile;
	final Map<Proxy, List<ReceivedPacket>> _clientPacketsBeforeServerConnection;
	final Map<PacketList, JPopupMenu> _popups;
	
	private final DateFormat _dateTimeFormatter;
	
	private final JPanel _placeHolder;
	private final Icon _iconLoginA;
	final Icon _iconLoginI;
	private final Icon _iconLoginF;
	private final Icon _iconGameA;
	final Icon _iconGameI;
	private final Icon _iconGameF;
	
	private final Map<IProtocolVersion, Set<IPacketTemplate>> _displayedClient, _displayedServer;
	
	private final CaptureSettingAccessor _captureSettingAccessor;
	
	/**
	 * Creates a tabbed pane to display packets for each connection.
	 * 
	 * @param captureSettingAccessor link to the details panel
	 */
	public ConnectionPane(CaptureSettingAccessor captureSettingAccessor)
	{
		super(SwingConstants.TOP, JTabbedPane.WRAP_TAB_LAYOUT);
		
		_number = new AtomicInteger();
		_live = new FastMap<Proxy, PacketList>().setShared(true);
		_offline = new L2FastSet<PacketList>().setShared(true);
		_logFile = new L2FastSet<PacketList>().setShared(true);
		_clientPacketsBeforeServerConnection = new FastMap<Proxy, List<ReceivedPacket>>().setShared(true);
		_popups = new HashMap<>();
		
		_displayedClient = new HashMap<>();
		_displayedServer = new HashMap<>();
		{
			final Set<IPacketTemplate> disp = Collections.singleton(IPacketTemplate.ANY_DYNAMIC_PACKET);
			_displayedClient.put(ClientProtocolVersion.THE_FINAL_PROTOCOL_VERSION, disp);
			_displayedServer.put(ClientProtocolVersion.THE_FINAL_PROTOCOL_VERSION, disp);
		}
		
		_dateTimeFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		
		_captureSettingAccessor = captureSettingAccessor;
		
		_placeHolder = new JPanel();
		JLabel empty = new JLabel(LoadOption.DISABLE_PROXY.isNotSet() ? "No connections" : "No open packet logs");
		empty.setHorizontalAlignment(CENTER);
		empty.setVerticalAlignment(CENTER);
		getPlaceHolder().add(empty);
		
		addChangeListener(e -> requestUpdateSummary(null));
		
		if (LoadOption.HIDE_LOG_CONSOLE.isNotSet())
			addTab(CONSOLE, null, new ConsolePanel(), "View messages logged to console.");
		addTab(IDLE, null, getPlaceHolder()/*, "No connections have been made to this proxy server yet."*/);
		
		// perhaps one day move to #setTabComponentAt(int, Component)?
		addMouseListener(new MouseAdapter()
		{
			@Override
			public void mousePressed(MouseEvent e)
			{
				maybeShowPopup(e);
			}
			
			@Override
			public void mouseReleased(MouseEvent e)
			{
				maybeShowPopup(e);
			}
			
			@Override
			public void mouseClicked(MouseEvent e)
			{
				maybeShowPopup(e);
			}
			
			private void maybeShowPopup(MouseEvent e)
			{
				if (e.isPopupTrigger())
				{
					final Component c = getComponentAt(getSelectedIndex());
					if (c instanceof PacketList)
					{
						_popups.get(c).show(e.getComponent(), e.getX(), e.getY());
					}
				}
			}
		});
		
		final int w = 15, h = 15;
		_iconLoginA = new ImageIcon(IconUtils.drawRoundedRect(w, h, Color.GREEN.darker(), "LS", "ON"));
		_iconLoginI = new ImageIcon(IconUtils.drawRoundedRect(w, h, Color.RED.darker(), "LS", "OFF"));
		_iconLoginF = new ImageIcon(IconUtils.drawRoundedRect(20, h, Color.DARK_GRAY, "LS", "FILE"));
		_iconGameA = new ImageIcon(IconUtils.drawRoundedRect(w, h, Color.GREEN.darker(), "GS", "ON"));
		_iconGameI = new ImageIcon(IconUtils.drawRoundedRect(w, h, Color.RED.darker(), "GS", "OFF"));
		_iconGameF = new ImageIcon(IconUtils.drawRoundedRect(20, h, Color.DARK_GRAY, "GS", "FILE"));
	}
	
	private void requestUpdateSummary(PacketList requestor)
	{
		if (requestor == null)
		{
			final Component c = getSelectedComponent();
			requestor = c instanceof PacketList ? (PacketList)c : null;
		}
		else if (getSelectedComponent() != requestor)
			return;
			
		_captureSettingAccessor.onOpen(requestor);
	}
	
	/**
	 * Returns whether the operator has disabled packet capture for the specific connection.
	 * 
	 * @param client client connection
	 * @return whether capture is disabled
	 */
	public boolean isCaptureDisabledFor(Proxy client)
	{
		if (_captureSettingAccessor.isGlobalCaptureDisabled())
			return true;
			
		final PacketList pl = _live.get(client);
		return pl != null ? pl.isSessionCaptureDisabled() : false;
	}
	
	/**
	 * Handles the session capture checkbox events. Enables/disables packet capture for the currently open tab.
	 * 
	 * @param disabled whether to disable capture
	 * @return whether capture state was set as desired
	 */
	public boolean onSessionCaptureChanged(boolean disabled)
	{
		final Component c = getSelectedComponent();
		if (!(c instanceof PacketList))
			return false;
			
		final PacketList pl = (PacketList)c;
		pl.setCaptureState(disabled ? ListCaptureState.CAPTURE_DISABLED : ListCaptureState.CAPTURE_ENABLED);
		return true;
	}
	
	/**
	 * Takes action to evade an imminent OOME.
	 * 
	 * @param cleanupFlags allowed actions
	 */
	public void onLowMemory(int cleanupFlags)
	{
		if ((cleanupFlags & FLAG_LM_DROP_LISTS_LOGFILE) == FLAG_LM_DROP_LISTS_LOGFILE)
		{
			for (final Iterator<PacketList> it = _logFile.iterator(); it.hasNext();)
			{
				final PacketList pl = it.next();
				it.remove();
				
				removeList(pl, null);
			}
		}
		if ((cleanupFlags & FLAG_LM_DROP_LISTS_INACTIVE) == FLAG_LM_DROP_LISTS_INACTIVE)
		{
			for (final Iterator<PacketList> it = _offline.iterator(); it.hasNext();)
			{
				final PacketList pl = it.next();
				it.remove();
				
				removeList(pl, null);
			}
		}
		if ((cleanupFlags & FLAG_LM_PURGE_LISTS) == FLAG_LM_PURGE_LISTS)
		{
			for (final PacketList pl : _live.values())
			{
				pl.clear();
			}
		}
	}
	
	/** Propagates newly loaded packet definitions to all currently open tabs. */
	public void notifyDefinitionsChanged()
	{
		for (final Entry<Proxy, PacketList> e : _live.entrySet())
			e.getValue().notifyDefinitionsChanged();
		for (final PacketList list : _offline)
			list.notifyDefinitionsChanged();
		for (final PacketList list : _logFile)
			list.notifyDefinitionsChanged();
			
		requestUpdateSummary(null);
	}
	
	@Override
	public void onProtocolVersion(Proxy affected, IProtocolVersion protocol) throws RuntimeException
	{
		SwingUtilities.invokeLater(() ->
		{
			// version is assigned to client
			final PacketList pl = _live.get(affected);
			if (pl == null)
				return; // otherwise it will be set in addList
				
			pl.setProtocol(protocol);
			pl.notifyDisplayConfigChanged(getDisplayConfig(protocol, true), null, null, getDisplayConfig(protocol, false), null, null);
			
			requestUpdateSummary(pl);
		});
	}
	
	@Override
	public void onClientPacket(final Proxy sender, Proxy recipient, ByteBuffer packet, final long time)
	{
		if (_captureSettingAccessor.isGlobalCaptureDisabled())
			return;
			
		final byte[] body = new byte[packet.clear().remaining()];
		packet.get(body);
		
		final ReceivedPacket rp = new ReceivedPacket(ServiceType.valueOf(sender.getProtocol()), sender.getType(), body, time);
		
		SwingUtilities.invokeLater(() ->
		{
			final PacketList pl = _live.get(sender);
			if (pl != null)
			{
				pl.addPacket(rp);
				return;
			}
			
			List<ReceivedPacket> list = _clientPacketsBeforeServerConnection.get(sender);
			if (list == null)
				list = MapUtils.putIfAbsent(_clientPacketsBeforeServerConnection, sender, new CopyOnWriteArrayList<>());
			list.add(rp);
		});
	}
	
	@Override
	public void onServerPacket(final Proxy sender, final Proxy recipient, ByteBuffer packet, final long time)
	{
		if (_captureSettingAccessor.isGlobalCaptureDisabled())
			return;
			
		final byte[] body = new byte[packet.clear().remaining()];
		packet.get(body);
		
		SwingUtilities.invokeLater(() ->
		{
			if (recipient == null)
			{
				LOG.info("Packet without recipient: " + HexUtil.printData(body));
				return;
			}
			
			PacketList pl = _live.get(recipient);
			if (pl != null)
				pl.addPacket(new ReceivedPacket(ServiceType.valueOf(sender.getProtocol()), sender.getType(), body, time));
		});
	}
	
	@Override
	public void onServerConnection(final Proxy server)
	{
		final Proxy client = server.getClient();
		addConnection(client instanceof L2LoginClient, IPAliasManager.toUserFriendlyString(client.getHostAddress()) + "\u2794" + IPAliasManager.toUserFriendlyString(server.getHostAddress()),
				System.currentTimeMillis(), client);
	}
	
	@Override
	public void onClientConnection(Proxy client)
	{
		// ignore
	}
	
	/**
	 * Adds a new tab to the connection pane.<BR>
	 * <BR>
	 * This method must be called from the UI thread.
	 * 
	 * @param login
	 *            login connection
	 * @param packets
	 *            packet count
	 * @param logFile
	 *            a packet log file
	 * @param protocol
	 *            protocol version
	 * @return an empty packet list view
	 */
	public PacketList addConnection(boolean login, int packets, Path logFile, IProtocolVersion protocol)
	{
		final PacketList list = new PacketList(login, packets, new HistoricalPacketLog(logFile));
		addNewList(null, list, protocol, logFile.getFileName().toString(), null);
		return list;
	}
	
	/**
	 * Adds a new tab to the connection pane.<BR>
	 * <BR>
	 * This method can be called from outside of the UI thread.
	 * 
	 * @param login
	 *            login connection
	 * @param from
	 *            packet source
	 * @param time
	 *            connection time
	 * @param key
	 *            live connection
	 */
	public void addConnection(final boolean login, String from, long time, final Proxy key)
	{
		if (login && ProxyConfig.NO_TABS_FOR_LOGIN_CONNECTIONS)
			return;
			
		final String name;
		{
			final StringBuilder sb = new StringBuilder();
			sb.append(_number.incrementAndGet());
			// sb.append(login ? 'L' : 'G');
			sb.append(' ');
			sb.append(from);
			name = sb.toString();
		}
		
		final String tooltip;
		{
			final L2TextBuilder sb = new L2TextBuilder("Connected to ").append(login ? "login" : "game").append(" server on ");
			tooltip = sb.append(getDateTimeFormatter().format(time)).moveToString();
		}
		
		SwingUtilities.invokeLater(() ->
		{
			final PacketList list = new PacketList(login, login ? 10 : -1, new ServerSocketID(key.getServer().getInetSocketAddress()));
			list.setCaptureState(ListCaptureState.CAPTURE_ENABLED);
			
			final List<ReceivedPacket> clientPackets = _clientPacketsBeforeServerConnection.remove(key);
			if (clientPackets != null && !_captureSettingAccessor.isGlobalCaptureDisabled())
				list.addPackets(clientPackets, false);
				
			addNewList(key, list, key.getProtocol(), name, tooltip);
		});
	}
	
	@Override
	public void onDisconnection(Proxy client, Proxy server)
	{
		_clientPacketsBeforeServerConnection.remove(client);
		final PacketList pl = _live.get(client);
		if (pl == null)
			return;
			
		_offline.add(pl);
		pl.setCaptureState(ListCaptureState.OFFLINE);
		SwingUtilities.invokeLater(() ->
		{
			final int i = indexOfComponent(pl);
			final Icon icon = pl.isLogin() ? _iconLoginI : _iconGameI;
			setIconAt(i, icon);
			final JPanel tab = (JPanel)getTabComponentAt(i);
			final JLabel lab = (JLabel)tab.getComponent(0);
			lab.setIcon(icon);
			
			requestUpdateSummary(pl);
			
			_live.remove(client);
		});
	}
	
	/**
	 * Adds a new tab.
	 * 
	 * @param key connection endpoint or {@code null}
	 * @param list packet table
	 * @param protocol network protocol version
	 * @param name tab name
	 * @param tooltip tab tooltip
	 */
	public void addNewList(final Proxy key, final PacketList list, IProtocolVersion protocol, String name, String tooltip)
	{
		if (list.getProtocol() != protocol)
			list.setProtocol(protocol);
		list.notifyDisplayConfigChanged(getDisplayConfig(protocol, true), null, null, getDisplayConfig(protocol, false), null, null);
		
		if (key != null)
			_live.put(key, list);
		else
			_logFile.add(list);
			
		final ActionListener remover = e -> removeList(list, key);
		
		final JPopupMenu menu = new JPopupMenu();
		{
			final JMenuItem close = new JMenuItem("Close");
			close.setMnemonic(KeyEvent.VK_C);
			close.addActionListener(remover);
			menu.add(close);
			menu.addSeparator();
			final JMenuItem closeOff = new JMenuItem("Close all disconnected");
			closeOff.setMnemonic(KeyEvent.VK_O);
			closeOff.addActionListener(e ->
			{
				for (final PacketList off : _offline)
					removeList(off, null);
			});
			menu.add(closeOff);
			final JMenuItem closeAll = new JMenuItem("Close all");
			closeAll.setMnemonic(KeyEvent.VK_A);
			//menu.add(closeAll);
		}
		_popups.put(list, menu);
		
		final int limit = LoadOption.HIDE_LOG_CONSOLE.isNotSet() ? 2 : 1;
		for (int i = 0; i < limit; ++i)
			if (IDLE.equals(getTitleAt(i)))
				removeTabAt(i);
				
		final Icon icon;
		if (key != null)
			icon = list.isLogin() ? _iconLoginA : _iconGameA;
		else
			icon = list.isLogin() ? _iconLoginF : _iconGameF;
		addTab(name, icon, list, tooltip);
		
		// customize tab l&f
		final int i = indexOfComponent(list);
		JPanel tab = new JPanel();
		tab.setOpaque(false);
		{
			tab.add(new JLabel(name, icon, CENTER));
			final JButton close = new CloseButton();
			close.addActionListener(remover);
			tab.add(close);
		}
		setTabComponentAt(i, tab);
		
		setSelectedIndex(i);
	}
	
	void removeList(PacketList list, Proxy key)
	{
		if (key != null)
			_live.remove(key);
			
		_offline.remove(list);
		_logFile.remove(list);
		
		remove(list);
		_popups.remove(list);
		
		if (getTabCount() == 0 || (getTabCount() == 1 && CONSOLE.equals(getTitleAt(0))))
			addTab(IDLE, null, getPlaceHolder(), "No connections to display.");
			
		requestUpdateSummary(null);
		
		list.onRemove();
	}
	
	private Set<IPacketTemplate> getDisplayConfig(IProtocolVersion protocol, boolean client)
	{
		if (LoadOption.DISABLE_DEFS.isSet())
			return Collections.singleton(IPacketTemplate.ANY_DYNAMIC_PACKET);
			
		return (client ? _displayedClient : _displayedServer).get(protocol);
	}
	
	@Override
	public void displayConfigChanged(IProtocolVersion protocol, Set<IPacketTemplate> displayedClientPackets, Set<IPacketTemplate> addedClientPackets, Set<IPacketTemplate> removedClientPackets,
			Set<IPacketTemplate> displayedServerPackets, Set<IPacketTemplate> addedServerPackets, Set<IPacketTemplate> removedServerPackets)
	{
		_displayedClient.put(protocol, displayedClientPackets);
		_displayedServer.put(protocol, displayedServerPackets);
		
		notifyDisplayConfigChanged(protocol, displayedClientPackets, addedClientPackets, removedClientPackets, displayedServerPackets, addedServerPackets, removedServerPackets);
		requestUpdateSummary(null);
	}
	
	private void notifyDisplayConfigChanged(IProtocolVersion protocol, Set<IPacketTemplate> displayedClientPackets, Set<IPacketTemplate> addedClientPackets, Set<IPacketTemplate> removedClientPackets,
			Set<IPacketTemplate> displayedServerPackets, Set<IPacketTemplate> addedServerPackets, Set<IPacketTemplate> removedServerPackets)
	{
		for (Entry<Proxy, PacketList> e : _live.entrySet())
		{
			if (protocol.equals(e.getKey().getProtocol()))
			{
				e.getValue().notifyDisplayConfigChanged(displayedClientPackets, addedClientPackets, removedClientPackets, displayedServerPackets, addedServerPackets, removedServerPackets);
			}
		}
		
		for (PacketList pl : _offline)
		{
			if (protocol.equals(pl.getProtocol()))
			{
				pl.notifyDisplayConfigChanged(displayedClientPackets, addedClientPackets, removedClientPackets, displayedServerPackets, addedServerPackets, removedServerPackets);
			}
		}
		
		for (PacketList pl : _logFile)
		{
			if (protocol.equals(pl.getProtocol()))
			{
				pl.notifyDisplayConfigChanged(displayedClientPackets, addedClientPackets, removedClientPackets, displayedServerPackets, addedServerPackets, removedServerPackets);
			}
		}
	}
	
	private DateFormat getDateTimeFormatter()
	{
		return _dateTimeFormatter;
	}
	
	JPanel getPlaceHolder()
	{
		return _placeHolder;
	}
	
	private static class CloseButton extends JButton
	{
		private static final long serialVersionUID = 6977649245461179318L;
		
		public CloseButton()
		{
			final int size = 8;
			setPreferredSize(new Dimension(size, size));
			setToolTipText("Close tab");
			setUI(new BasicButtonUI());
			setContentAreaFilled(false);
			setFocusable(false);
			setRolloverEnabled(true);
		}
		
		@Override
		protected void paintComponent(Graphics g)
		{
			super.paintComponent(g);
			Graphics2D g2 = (Graphics2D)g.create();
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			// shift the image for pressed buttons
			if (getModel().isPressed())
			{
				g2.translate(1, 1);
			}
			g2.setStroke(new BasicStroke(2.5F, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
			g2.setColor(Color.BLACK);
			if (getModel().isRollover())
			{
				g2.setColor(Color.WHITE);
			}
			final int delta = 0;
			g2.drawLine(delta, delta, getWidth() - delta - 1, getHeight() - delta - 1);
			g2.drawLine(getWidth() - delta - 1, delta, delta, getHeight() - delta - 1);
			g2.dispose();
		}
	}
}
