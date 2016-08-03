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

import static net.l2emuproject.proxy.network.meta.IPacketTemplate.ANY_DYNAMIC_PACKET;
import static net.l2emuproject.util.ISODateTime.ISO_DATE_TIME_ZONE_MS;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.RandomAccess;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JViewport;
import javax.swing.ListSelectionModel;
import javax.swing.RowSorter;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;

import net.l2emuproject.lang.L2TextBuilder;
import net.l2emuproject.network.mmocore.MMOBuffer;
import net.l2emuproject.network.protocol.IProtocolVersion;
import net.l2emuproject.network.protocol.ProtocolVersionManager;
import net.l2emuproject.proxy.io.conversion.ToPlaintextVisitor;
import net.l2emuproject.proxy.io.definitions.VersionnedPacketTable;
import net.l2emuproject.proxy.network.EndpointType;
import net.l2emuproject.proxy.network.login.client.packets.RequestServerList;
import net.l2emuproject.proxy.network.meta.IPacketTemplate;
import net.l2emuproject.proxy.network.meta.PacketPayloadEnumerator;
import net.l2emuproject.proxy.network.meta.RandomAccessMMOBuffer;
import net.l2emuproject.proxy.network.meta.RequiredInvasiveOperations;
import net.l2emuproject.proxy.network.meta.SimplePpeProvider;
import net.l2emuproject.proxy.network.meta.exception.InvalidPacketOpcodeSchemeException;
import net.l2emuproject.proxy.network.meta.exception.PartialPayloadEnumerationException;
import net.l2emuproject.proxy.state.entity.cache.EntityInfoCache;
import net.l2emuproject.proxy.state.entity.context.ICacheServerID;
import net.l2emuproject.proxy.ui.ReceivedPacket;
import net.l2emuproject.proxy.ui.savormix.loader.Frontend;
import net.l2emuproject.proxy.ui.savormix.loader.Frontend.PacketLogSummary;
import net.l2emuproject.proxy.ui.savormix.loader.LoadOption;
import net.l2emuproject.proxy.ui.savormix.loader.Loader;
import net.l2emuproject.ui.table.FilterableTableHeader;
import net.l2emuproject.ui.table.TriStateRowSorter;
import net.l2emuproject.util.logging.L2Logger;

/**
 * Displays sent/received packets (based on packet display configuration) in a table and enables
 * user to view a packet's content by selecting it. <BR>
 * <BR>
 * This class is a part of a reference GUI provided for education purposes only. These classes
 * should help to understand how to interact with the underlying proxy core. <BR>
 * <BR>
 * Creating an own GUI is the preferred way to start using this application.
 * 
 * @author savormix
 */
public final class PacketList extends JSplitPane implements ActionListener, RequiredInvasiveOperations
{
	static final L2Logger LOG = L2Logger.getLogger(PacketList.class);
	
	private static final long serialVersionUID = 5807551928679478786L;
	
	final boolean _login;
	
	final ICacheServerID _cacheContext;
	
	final PacketListModel _model;
	final JTable _list;
	private final JLabel _displayed;
	private final JLabel _total;
	private final JButton _clearAll;
	
	final PacketDisplay _display;
	private final NumberFormat _formatter;
	
	IProtocolVersion _version;
	
	volatile ListCaptureState _captureState;
	
	/**
	 * Creates a packet list with packet content display.
	 * 
	 * @param login
	 *            if login server/client packets
	 * @param expectedCount
	 *            expected number of packets in list
	 * @param cacheContext cache identifier
	 */
	public PacketList(boolean login, int expectedCount, ICacheServerID cacheContext)
	{
		super(JSplitPane.HORIZONTAL_SPLIT, true);
		
		setResizeWeight(0);
		
		_login = login;
		
		_cacheContext = cacheContext;
		EntityInfoCache.addSharedContext(_cacheContext);
		
		_display = new PacketDisplay(this);
		
		_model = new PacketListModel(this, expectedCount);
		_list = new JTable(_model);
		final FilterableTableHeader header;
		_list.setTableHeader(header = new FilterableTableHeader(_list.getColumnModel()));
		_list.setRowSorter(new TriStateRowSorter<>(_model));
		_list.setColumnSelectionAllowed(false);
		_list.setRowSelectionAllowed(true);
		_list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		_list.getSelectionModel().addListSelectionListener(e ->
		{
			if (e.getValueIsAdjusting())
				return;
				
			final int row = _list.getSelectedRow();
			if (row == -1)
			{
				_display.displayPacket(null);
				return;
			}
			final PacketListEntry ple = _model.getValueAt(_list.getRowSorter().convertRowIndexToModel(row));
			_display.displayPacket(ple.getPacket());
		});
		for (int i = 0; i < PacketListModel.COL_WIDTHS.length; i++)
		{
			TableColumn tc = _list.getColumnModel().getColumn(i);
			tc.setPreferredWidth(PacketListModel.COL_WIDTHS[i]);
		}
		_list.setFillsViewportHeight(true);
		_list.addComponentListener(new ComponentAdapter()
		{
			@Override
			public void componentResized(ComponentEvent e)
			{
				if (Frontend.SCROLL_LOCK)
					return;
					
				_list.scrollRectToVisible(_list.getCellRect(_list.getRowCount() - 1, 0, true));
			}
		});
		_list.setTransferHandler(new TransferHandler()
		{
			private static final long serialVersionUID = 3211306296809776535L;
			
			@Override
			protected Transferable createTransferable(JComponent c)
			{
				final L2TextBuilder sb = new L2TextBuilder();
				try
				{
					ToPlaintextVisitor.writePacket(getSelectedPacket(), _version, new MMOBuffer(), _cacheContext, new SimpleDateFormat(ISO_DATE_TIME_ZONE_MS), sb);
				}
				catch (IOException e)
				{
					// L2TB doesn't throw
				}
				return new StringSelection(sb.moveToString());
			}
			
			@Override
			public int getSourceActions(JComponent c)
			{
				return COPY;
			}
		});
		JPanel list = new JPanel();
		list.setMinimumSize(new Dimension(400, 200));
		list.setPreferredSize(new Dimension(400, 200));
		list.setLayout(new BorderLayout());
		final JScrollPane enhancedHeaderPane = new JScrollPane(_list);
		enhancedHeaderPane.setColumnHeader(new JViewport()
		{
			private static final long serialVersionUID = -6722591939160893883L;
			
			@Override
			public Dimension getPreferredSize()
			{
				Dimension d = super.getPreferredSize();
				d.height = Math.max(d.height, header.getDefaultRenderer().getPreferredSize().height);
				return d;
			}
		});
		list.add(enhancedHeaderPane, BorderLayout.CENTER);
		JPanel s = new JPanel(new GridLayout(2, 0));
		{
			_clearAll = new JButton("Clear memory");
			_clearAll.setToolTipText("Removes all packets currently cached in memory for this packet log.");
			_clearAll.addActionListener(this);
			if (expectedCount >= 0)
				_clearAll.setEnabled(false);
			final JButton clear = new JButton("Clear table");
			clear.setEnabled(LoadOption.DISABLE_DEFS.isNotSet());
			clear.setToolTipText("Removes all packets added to the table, even if they are filtered away.");
			clear.addActionListener(this);
			
			_formatter = NumberFormat.getIntegerInstance(Loader.getLocale());
			JPanel visible = new JPanel();
			{
				visible.add(new JLabel("Table (unfiltered):"));
				visible.add(_displayed = new JLabel(_formatter.format(0)));
			}
			s.add(visible);
			JPanel total = new JPanel();
			{
				total.add(new JLabel("Memory:"));
				total.add(_total = new JLabel(_formatter.format(0)));
			}
			s.add(total);
			s.add(clear);
			s.add(_clearAll);
		}
		list.add(s, BorderLayout.SOUTH);
		setLeftComponent(list);
		
		final ProtocolVersionManager pvm = ProtocolVersionManager.getInstance();
		_version = login ? pvm.getFallbackProtocolLogin() : pvm.getFallbackProtocolGame();
		
		setRightComponent(_display);
	}
	
	/**
	 * Returns the packet list accessor.
	 * 
	 * @return packet table accessor
	 */
	public PacketTableAccessor getAccessor()
	{
		return new ListExcerpt();
	}
	
	/** Removes all packets currently cached within this component. */
	public void clear()
	{
		_model.removeAll();
		
		_display.displayPacket(null);
	}
	
	@Override
	public void actionPerformed(ActionEvent e)
	{
		if (e.getSource() == _clearAll)
			clear();
		else
			_model.removeDisplayed();
	}
	
	void updateDisplayedCount(int count)
	{
		_displayed.setText(_formatter.format(count));
	}
	
	void updateCachedCount(int count)
	{
		_total.setText(_formatter.format(count));
	}
	
	/**
	 * Adds a packet to this list.<BR>
	 * <BR>
	 * This method must be called from the UI thread.
	 * 
	 * @param packet
	 *            a packet
	 */
	public void addPacket(final ReceivedPacket packet)
	{
		if (_captureState != ListCaptureState.CAPTURE_DISABLED)
			_model.addSinglePacket(packet, true);
	}
	
	/**
	 * Adds packets from this list, removing them as they are added.<BR>
	 * <BR>
	 * This method must be called from the UI thread.
	 * 
	 * @param packets
	 *            packets
	 * @param newestAtStart
	 *            if the collection was formed in reverse
	 */
	public void addPackets(List<ReceivedPacket> packets, boolean newestAtStart)
	{
		_model.addFromList(packets, newestAtStart);
	}
	
	/**
	 * Notifies that different packets should be displayed in this list.
	 * 
	 * @param displayedClientPackets
	 *            client packets to be displayed
	 * @param addedClientPackets
	 *            newly displayed client packets
	 * @param removedClientPackets
	 *            no longer displayed client packets
	 * @param displayedServerPackets
	 *            server packets to be displayed
	 * @param addedServerPackets
	 *            newly displayed server packets
	 * @param removedServerPackets
	 *            no longer displayed server packets
	 */
	public void notifyDisplayConfigChanged(Set<IPacketTemplate> displayedClientPackets, Set<IPacketTemplate> addedClientPackets, Set<IPacketTemplate> removedClientPackets,
			Set<IPacketTemplate> displayedServerPackets, Set<IPacketTemplate> addedServerPackets, Set<IPacketTemplate> removedServerPackets)
	{
		_model.configChanged(displayedClientPackets, addedClientPackets, removedClientPackets, displayedServerPackets, addedServerPackets, removedServerPackets);
	}
	
	/** Notifies this component to update the packet table. */
	public void notifyDefinitionsChanged()
	{
		_model.updateAll();
	}
	
	/** Releases resources associated with this component. */
	public void onRemove()
	{
		clear();
		EntityInfoCache.removeSharedContext(_cacheContext);
		_display.onRemove();
	}
	
	/**
	 * Returns whether this component contains login packets.
	 * 
	 * @return whether login packets are displayed
	 */
	public boolean isLogin()
	{
		return _login;
	}
	
	/**
	 * Returns the network protocol version used to identify/display packet names and opcodes in the packet table.
	 * 
	 * @return network protocol version
	 */
	public IProtocolVersion getProtocol()
	{
		return _version;
	}
	
	/**
	 * Sets the network protocol version used to identify/display packet names and opcodes in the packet table and to generate selected packet content interpretations.
	 * 
	 * @param version network protocol version
	 */
	public void setProtocol(IProtocolVersion version)
	{
		_version = version;
		
		_display.setProtocol(version);
		SwingUtilities.invokeLater(this::notifyDefinitionsChanged);
	}
	
	/**
	 * Returns whether this list is in packet refusal mode.
	 * 
	 * @return whether capture is disabled
	 */
	public boolean isSessionCaptureDisabled()
	{
		return _captureState == ListCaptureState.CAPTURE_DISABLED;
	}
	
	/**
	 * Sets the currently active capture mode.
	 * 
	 * @param captureState capture mode
	 */
	public void setCaptureState(ListCaptureState captureState)
	{
		_captureState = captureState;
	}
	
	/**
	 * Generates a summary of the connection/historical packet log associated with this component.
	 * 
	 * @return summary
	 */
	public PacketLogSummary getSummary()
	{
		final boolean unkC = _model._showFromClient.contains(ANY_DYNAMIC_PACKET), unkS = _model._showFromServer.contains(ANY_DYNAMIC_PACKET);
		final int dcp = _model._showFromClient.size(), dsp = _model._showFromServer.size();
		final long tcp = VersionnedPacketTable.getInstance().getKnownTemplates(_version, EndpointType.CLIENT).count(),
				tsp = VersionnedPacketTable.getInstance().getKnownTemplates(_version, EndpointType.SERVER).count();
		return new PacketLogSummary(_version, unkC ? dcp - 1 : dcp, (int)tcp, unkC, unkS ? dsp - 1 : dsp, (int)tsp, unkS, isSessionCaptureDisabled());
	}
	
	ReceivedPacket getSelectedPacket()
	{
		final int row = _list.getSelectedRow();
		if (row == -1)
			return null;
			
		return _model.getValueAt(_list.getRowSorter().convertRowIndexToModel(row)).getPacket();
	}
	
	ICacheServerID getCacheContext()
	{
		return _cacheContext;
	}
	
	static class PacketListModel extends AbstractTableModel
	{
		private static final long serialVersionUID = -7687391760364770042L;
		static final L2Logger _log = L2Logger.getLogger(PacketListModel.class);
		private static final String[] COLUMNS = { "Sender", "Opcodes", "Name" };
		static final int[] COL_WIDTHS = { 20, 80, 400 };
		
		private final PacketList _owner;
		
		final List<PacketListEntry> _displayed, _cached;
		
		Set<IPacketTemplate> _showFromClient, _showFromServer;
		
		protected PacketListModel(PacketList owner, int size)
		{
			_owner = owner;
			
			_displayed = new ArrayList<>(size >= 0 ? size : 100);
			if (!(_displayed instanceof RandomAccess))
				_log.error("Degraded performance.");
			_cached = size >= 0 ? new ArrayList<>(size) : new LinkedList<>();
			
			_showFromClient = _showFromServer = Collections.emptySet();
		}
		
		public void updateAll()
		{
			for (final PacketListEntry cached : _cached)
				cached.queryTemplate(_owner.getProtocol());
				
			int lastRow = getRowCount() - 1;
			if (lastRow >= 0)
				fireTableRowsUpdated(0, lastRow);
		}
		
		public void removeAll()
		{
			_cached.clear();
			removeDisplayed();
			_owner.updateCachedCount(0);
		}
		
		void removeDisplayed()
		{
			final int size = _displayed.size();
			if (size > 0)
			{
				_displayed.clear();
				fireTableRowsDeleted(0, size - 1);
				_owner.updateDisplayedCount(0);
			}
		}
		
		public void addFromList(List<ReceivedPacket> packets, boolean newestAtStart)
		{
			final int firstIndex = _displayed.size();
			addition:
			{
				if (newestAtStart)
				{
					// remove from last to first
					int size;
					while ((size = packets.size()) > 0)
						addSinglePacket(packets.remove(size - 1), false);
					break addition;
				}
				
				// remove from first to last
				if (packets instanceof ArrayList)
				{
					// removing from the start is subject to severe penalties
					// removing elements will not free memory, as internal nodes are not used
					for (final ReceivedPacket packet : packets)
						addSinglePacket(packet, false);
					packets.clear();
					break addition;
				}
				
				// nodes with high probability
				while (!packets.isEmpty())
					addSinglePacket(packets.remove(0), false);
			}
			
			final int endIndex = _displayed.size();
			if (endIndex > firstIndex)
			{
				fireTableRowsInserted(firstIndex, endIndex - 1);
				_owner.updateDisplayedCount(endIndex);
			}
			_owner.updateCachedCount(_cached.size());
		}
		
		public void addSinglePacket(ReceivedPacket packet, boolean fire)
		{
			// special case to allow PPE (custom definition) as well as a client-requested packet structure
			if (_owner._login && packet.getEndpoint().isClient() && packet.getBody()[0] == RequestServerList.OPCODE)
			{
				final PacketPayloadEnumerator ppe = SimplePpeProvider.getPacketPayloadEnumerator();
				if (ppe != null)
				{
					final ByteBuffer bb = ByteBuffer.wrap(packet.getBody()).order(ByteOrder.LITTLE_ENDIAN);
					final MMOBuffer buf = new MMOBuffer();
					buf.setByteBuffer(bb);
					RandomAccessMMOBuffer rab = null;
					try
					{
						rab = ppe.enumeratePacketPayload(_owner._version, buf, () -> EndpointType.CLIENT);
					}
					catch (PartialPayloadEnumerationException e)
					{
						rab = e.getBuffer();
					}
					catch (InvalidPacketOpcodeSchemeException e)
					{
						LOG.error("This cannot happen", e);
					}
					if (rab != null)
						_owner._display._serverListType = (int)rab.readFirstInteger(SERVER_LIST_TYPE);
				}
			}
			
			if (_owner._captureState == ListCaptureState.CAPTURE_DISABLED)
				return;
				
			final PacketListEntry e = new PacketListEntry(_owner.getProtocol(), packet);
			_cached.add(e);
			if (fire)
				_owner.updateCachedCount(_cached.size());
				
			addPacket(e, fire);
		}
		
		private void addPacket(PacketListEntry e, boolean fire)
		{
			final ReceivedPacket packet = e.getPacket();
			
			final Set<IPacketTemplate> config = packet.getEndpoint().isClient() ? _showFromClient : _showFromServer;
			
			final IPacketTemplate pt = VersionnedPacketTable.getInstance().getTemplate(_owner.getProtocol(), packet.getEndpoint(), packet.getBody());
			if (!config.contains(pt.isDefined() ? pt : IPacketTemplate.ANY_DYNAMIC_PACKET))
				return;
				
			final int idx = _displayed.size();
			_displayed.add(e);
			if (fire)
			{
				fireTableRowsInserted(idx, idx);
				_owner.updateDisplayedCount(_displayed.size());
			}
		}
		
		private static boolean isEmpty(Set<?>... sets)
		{
			for (final Set<?> set : sets)
				if (set != null && !set.isEmpty())
					return false;
					
			return true;
		}
		
		private static <E> boolean contains(Set<E> set, E element)
		{
			if (set == null)
				return false;
			return set.contains(element);
		}
		
		void configChanged(Set<IPacketTemplate> displayedClientPackets, Set<IPacketTemplate> addedClientPackets, final Set<IPacketTemplate> removedClientPackets,
				Set<IPacketTemplate> displayedServerPackets, Set<IPacketTemplate> addedServerPackets, final Set<IPacketTemplate> removedServerPackets)
		{
			boolean rebuild = (isEmpty(addedClientPackets, removedClientPackets, addedServerPackets, removedServerPackets) || // no info supplied
					!isEmpty(addedClientPackets, addedServerPackets)); // must reform anyway
					
			if (removedClientPackets != null && removedClientPackets.size() > displayedClientPackets.size())
				rebuild = true;
			else if (removedServerPackets != null && removedServerPackets.size() > displayedServerPackets.size())
				rebuild = true;
				
			setDisplayed(displayedClientPackets, true);
			setDisplayed(displayedServerPackets, false);
			
			// this seems to be much much faster row that filtering is installed
			rebuild = Boolean.parseBoolean("true");
			
			if (rebuild)
			{
				removeDisplayed();
				
				for (PacketListEntry e : _cached)
					addPacket(e, false);
					
				final int visible = _displayed.size();
				if (visible > 0)
					fireTableRowsInserted(0, visible - 1);
				_owner.updateDisplayedCount(visible);
				
				return;
			}
			
			// OK, something changed and nothing has been added so...
			// something has been removed
			final VersionnedPacketTable table = VersionnedPacketTable.getInstance();
			int index = 0;
			for (Iterator<PacketListEntry> it = _displayed.iterator(); it.hasNext();)
			{
				final PacketListEntry e = it.next();
				final ReceivedPacket rp = e.getPacket();
				final IPacketTemplate pt = table.getTemplate(_owner.getProtocol(), rp.getEndpoint(), rp.getBody());
				
				final Set<IPacketTemplate> target = rp.getEndpoint().isClient() ? removedClientPackets : removedServerPackets;
				if (contains(target, pt.isDefined() ? pt : IPacketTemplate.ANY_DYNAMIC_PACKET))
				{
					it.remove();
					fireTableRowsDeleted(index, index);
					_owner.updateDisplayedCount(_displayed.size());
					
					continue;
				}
				
				index++;
			}
		}
		
		@Override
		public int getRowCount()
		{
			return _displayed.size();
		}
		
		@Override
		public int getColumnCount()
		{
			return COLUMNS.length;
		}
		
		@Override
		public String getColumnName(int column)
		{
			return COLUMNS[column];
		}
		
		@Override
		public Class<?> getColumnClass(int columnIndex)
		{
			return String.class;
		}
		
		@Override
		public String getValueAt(int rowIndex, int columnIndex)
		{
			PacketListEntry ple = getValueAt(rowIndex);
			switch (columnIndex)
			{
				case 0:
					return ple.getSender();
				case 1:
					return ple.getOpcode();
				case 2:
					return ple.getName();
				default:
					return "N/A";
			}
		}
		
		public PacketListEntry getValueAt(int rowIndex)
		{
			return _displayed.get(rowIndex);
		}
		
		private void setDisplayed(Set<IPacketTemplate> displayed, boolean client)
		{
			if (displayed == null)
				displayed = Collections.emptySet();
				
			if (client)
				_showFromClient = displayed;
			else
				_showFromServer = displayed;
		}
	}
	
	/** Packet capture mode enumeration. */
	public enum ListCaptureState
	{
		/** New packets will be added to the packet table */
		CAPTURE_ENABLED,
		/** New packets will not be added to the packet table */
		CAPTURE_DISABLED,
		/** Attempts to add packets will not be made */
		OFFLINE;
	}
	
	private final class ListExcerpt implements PacketTableAccessor
	{
		ListExcerpt()
		{
			// nothing special
		}
		
		@Override
		public ReceivedPacket getSelectedPacket()
		{
			return PacketList.this.getSelectedPacket();
		}
		
		@Override
		public List<ReceivedPacket> getVisiblePackets()
		{
			final RowSorter<?> view = _list.getRowSorter();
			final int total = view.getViewRowCount();
			if (total == 0)
				return Collections.emptyList();
				
			final List<ReceivedPacket> result = new ArrayList<>(total);
			for (int i = 0; i < total; ++i)
				result.add(_model.getValueAt(view.convertRowIndexToModel(i)).getPacket());
			return result;
		}
		
		@Override
		public List<ReceivedPacket> getTablePackets()
		{
			final List<ReceivedPacket> result = new ArrayList<ReceivedPacket>(_model._displayed.size());
			for (final PacketListEntry e : _model._displayed)
				result.add(e.getPacket());
			return result;
		}
		
		@Override
		public List<ReceivedPacket> getMemoryPackets()
		{
			final List<ReceivedPacket> result = new ArrayList<ReceivedPacket>(_model._cached.size());
			for (final PacketListEntry e : _model._cached)
				result.add(e.getPacket());
			return result;
		}
		
		@Override
		public IProtocolVersion getProtocolVersion()
		{
			return _version;
		}
		
		@Override
		public ICacheServerID getCacheContext()
		{
			return _cacheContext;
		}
	}
}
