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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.RandomAccess;
import java.util.Set;

import javax.swing.table.AbstractTableModel;

import net.l2emuproject.proxy.network.meta.IPacketTemplate;
import net.l2emuproject.util.logging.L2Logger;

/**
 * @author savormix
 */
class PacketDisplayConfigListModel extends AbstractTableModel
{
	private static final long serialVersionUID = -1748070205873627025L;
	private static final L2Logger _log = L2Logger.getLogger(PacketDisplayConfigListModel.class);
	
	private static final String[] COLUMNS = { "Show", "Opcodes", "Name" };
	private static final Class<?>[] TYPES = { Boolean.class, String.class, String.class };
	
	private final PacketContainer _container;
	
	private final List<PacketConfig> _rows;
	private final Set<Integer> _modified;
	
	public PacketDisplayConfigListModel(final PacketContainer container)
	{
		_container = container;
		
		List<PacketConfig> rows = new ArrayList<>();
		if (!(rows instanceof RandomAccess))
			_log.warn(getClass().getSimpleName() + ": degraded performance");
		
		// _rows.add(new PacketConfig(PacketInfo.ANY_UNKNOWN_PACKET));
		for (final IPacketTemplate pi : _container.getSelected())
			rows.add(new PacketConfig(pi, true));
		for (final IPacketTemplate pi : _container.getDeselected())
			rows.add(new PacketConfig(pi, false));
		Collections.sort(rows);
		
		_rows = Collections.unmodifiableList(rows);
		_modified = new HashSet<>();
		
		_container.getCommitted().retainAll(_container.getSelected());
		commit();
	}
	
	public void commit()
	{
		_modified.clear();
		
		_container.getSelected().clear();
		_container.getDeselected().clear();
	}
	
	public void rollback()
	{
		for (final Integer row : _modified)
		{
			_rows.get(row).toggle();
			fireTableCellUpdated(row, 0);
		}
		
		commit();
	}
	
	public boolean isInTransaction()
	{
		return !_modified.isEmpty();
	}
	
	public void setAll(boolean selected)
	{
		for (int i = getRowCount() - 1; i >= 0; --i)
		{
			final PacketConfig pc = _rows.get(i);
			if (pc.isEnabled() != selected)
				toggleAtRow(i);
		}
	}
	
	public boolean toggleAtRow(int rowIndex)
	{
		final PacketConfig pc = _rows.get(rowIndex);
		if (pc == null)
			return false;
		
		// perhaps user decided to undo a modification
		final boolean reverse = _modified.remove(rowIndex);
		if (!reverse) // nah, another modification
			_modified.add(rowIndex);
		
		// track changes
		final boolean selected = pc.toggle();
		final Set<IPacketTemplate> addTo, removeFrom;
		if (selected)
		{
			addTo = _container.getSelected();
			removeFrom = _container.getDeselected();
		}
		else
		{
			addTo = _container.getDeselected();
			removeFrom = _container.getSelected();
		}
		{
			final IPacketTemplate pi = pc.getPacket();
			if (reverse)
				removeFrom.remove(pi);
			else
				addTo.add(pi);
		}
		
		fireTableCellUpdated(rowIndex, 0);
		return selected;
	}
	
	@Override
	public int getRowCount()
	{
		return _rows.size();
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
		return TYPES[columnIndex];
	}
	
	@Override
	public Object getValueAt(int rowIndex, int columnIndex)
	{
		final PacketConfig pc = _rows.get(rowIndex);
		switch (columnIndex)
		{
			case 0:
				return pc.isEnabled();
			case 1:
				return pc.getOpcodes();
			case 2:
				return pc.getPacket().getName();
			default:
				return null;
		}
	}
}
