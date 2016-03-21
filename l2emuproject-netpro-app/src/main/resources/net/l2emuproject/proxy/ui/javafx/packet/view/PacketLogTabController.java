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
package net.l2emuproject.proxy.ui.javafx.packet.view;

import java.net.URL;
import java.text.NumberFormat;
import java.util.ResourceBundle;

import org.google.jhsheets.filtered.FilteredTableView;
import org.google.jhsheets.filtered.operators.StringOperator;
import org.google.jhsheets.filtered.tablecolumn.ColumnFilterEvent;
import org.google.jhsheets.filtered.tablecolumn.FilterableStringTableColumn;

import net.l2emuproject.network.protocol.IProtocolVersion;
import net.l2emuproject.proxy.ui.i18n.UIStrings;
import net.l2emuproject.proxy.ui.javafx.packet.PacketLogEntry;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.css.PseudoClass;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Duration;

/**
 * A controller that manages a single packet log tab.
 * 
 * @author _dev_
 */
public class PacketLogTabController implements Initializable
{
	@FXML
	private Label _labTablePacketCount;
	
	@FXML
	private Label _labMemoryPacketCount;
	
	@FXML
	private FilteredTableView<PacketLogEntry> _tvPackets;
	
	@FXML
	private TableColumn<PacketLogEntry, String> _colSender;
	
	@FXML
	private FilterableStringTableColumn<PacketLogEntry, String> _colOpcode;
	
	@FXML
	private FilterableStringTableColumn<PacketLogEntry, String> _colName;
	
	private static final int AUTO_SCROLL_THRESHOLD = 250;
	
	private final ObservableList<PacketLogEntry> _memoryPackets, _tablePackets;
	private BooleanProperty _scrollLockProperty;
	private boolean _autoScrollPending;
	
	/** Creates this controller. */
	public PacketLogTabController()
	{
		_memoryPackets = FXCollections.observableArrayList();
		_tablePackets = FXCollections.observableArrayList();
		
		_scrollLockProperty = new SimpleBooleanProperty(false);
	}
	
	@Override
	public void initialize(URL location, ResourceBundle resources)
	{
		final NumberFormat format = NumberFormat.getIntegerInstance(UIStrings.CURRENT_LOCALE);
		_labTablePacketCount.textProperty().bind(UIStrings.getEx("packettab.footer.count.table", Bindings.createStringBinding(() -> format.format(_tablePackets.size()), _tablePackets)));
		_labMemoryPacketCount.textProperty().bind(UIStrings.getEx("packettab.footer.count.memory", Bindings.createStringBinding(() -> format.format(_memoryPackets.size()), _memoryPackets)));
		
		final PseudoClass clientPacketRowClass = PseudoClass.getPseudoClass("client");
		_tvPackets.setRowFactory(tv ->
		{
			final TableRow<PacketLogEntry> row = new TableRow<>();
			row.itemProperty().addListener((obs, old, neu) -> row.pseudoClassStateChanged(clientPacketRowClass, neu != null ? neu.getEndpoint().isClient() : false));
			return row;
		});
		
		final SortedList<PacketLogEntry> sortableTablePackets = new SortedList<>(_tablePackets);
		sortableTablePackets.comparatorProperty().bind(_tvPackets.comparatorProperty());
		_tvPackets.setItems(sortableTablePackets);
		_tvPackets.addEventHandler(ColumnFilterEvent.FILTER_CHANGED_EVENT, e ->
		{
			final ObservableList<PacketLogEntry> filterMatchingPackets = FXCollections.observableArrayList();
			for (final PacketLogEntry packetEntry : _memoryPackets)
			{
				final boolean hidden;
				if (e.sourceColumn() == _colOpcode)
					hidden = isHiddenByOpcode(packetEntry);
				else
					hidden = isHiddenByName(packetEntry);
				
				if (!hidden)
					filterMatchingPackets.add(packetEntry);
			}
			_tablePackets.setAll(filterMatchingPackets);
		});
		
		_colSender.setCellValueFactory(new PropertyValueFactory<>("sender"));
		_colOpcode.setCellValueFactory(new PropertyValueFactory<>("opcode"));
		_colName.setCellValueFactory(new PropertyValueFactory<>("name"));
		
		final Timeline tlAutoScroll = new Timeline(new KeyFrame(Duration.ZERO), new KeyFrame(Duration.millis(AUTO_SCROLL_THRESHOLD), e ->
		{
			if (!_autoScrollPending)
				return;
			
			_autoScrollPending = false;
			final int idx = _tvPackets.getItems().size() - 1;
			if (idx >= 0)
				_tvPackets.scrollTo(idx);
		}));
		tlAutoScroll.setCycleCount(Timeline.INDEFINITE);
		tlAutoScroll.play();
	}
	
	@FXML
	private void clearMemory(ActionEvent event)
	{
		clearTable(event);
		_memoryPackets.clear();
	}
	
	@FXML
	private void clearTable(ActionEvent event)
	{
		_tablePackets.clear();
	}
	
	private boolean isHiddenByOpcode(PacketLogEntry packetEntry)
	{
		for (final StringOperator filter : _colOpcode.getFilters())
			if (isHidden(packetEntry.getOpcode(), filter))
				return true;
		
		return false;
	}
	
	private boolean isHiddenByName(PacketLogEntry packetEntry)
	{
		for (final StringOperator filter : _colName.getFilters())
			if (isHidden(packetEntry.getOpcode(), filter))
				return true;
		
		return false;
	}
	
	private boolean isHidden(String actualValue, StringOperator filter)
	{
		final String filterValue = filter.getValue();
		switch (filter.getType())
		{
			case EQUALS:
				if (!actualValue.equals(filterValue))
					return true;
				break;
			case NOTEQUALS:
				if (actualValue.equals(filterValue))
					return true;
				break;
			case CONTAINS:
				if (!actualValue.contains(filterValue))
					return true;
				break;
			case STARTSWITH:
				if (!actualValue.startsWith(filterValue))
					return true;
				break;
			case ENDSWITH:
				if (!actualValue.endsWith(filterValue))
					return true;
				break;
		}
		return false;
	}
	
	/**
	 * Allows the automatic scrolldown behavior to be controlled for the packet table.
	 * 
	 * @param scrollLockProperty scroll lock
	 */
	public void installScrollLock(BooleanProperty scrollLockProperty)
	{
		if (scrollLockProperty == null)
			scrollLockProperty = new SimpleBooleanProperty(false);
		
		_scrollLockProperty = scrollLockProperty;
	}
	
	/**
	 * Adds a new packet to the underlying table view.
	 * 
	 * @param packet a packet
	 */
	public void addPacket(PacketLogEntry packet)
	{
		_memoryPackets.add(packet);
		
		if (isHiddenByOpcode(packet) || isHiddenByName(packet))
			return;
		
		_tablePackets.add(packet);
		
		if (_scrollLockProperty.get())
			return;
		
		_autoScrollPending = true;
	}
	
	/**
	 * Refreshes packet rows based on packet definitions associated with {@code protocol}.
	 * 
	 * @param protocol a network protocol version
	 */
	public void updateView(IProtocolVersion protocol)
	{
		for (final PacketLogEntry e : _memoryPackets)
			e.updateView(protocol);
		_tvPackets.refresh();
	}
}
