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
import java.util.ResourceBundle;

import org.google.jhsheets.filtered.FilteredTableView;

import net.l2emuproject.network.protocol.IProtocolVersion;
import net.l2emuproject.proxy.ui.i18n.UIStrings;
import net.l2emuproject.proxy.ui.javafx.packet.PacketLogEntry;

import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;

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
	
	private final ObservableList<PacketLogEntry> _memoryPackets, _tablePackets;
	
	/** Creates this controller. */
	public PacketLogTabController()
	{
		_memoryPackets = FXCollections.observableArrayList();
		_tablePackets = FXCollections.observableArrayList();
	}
	
	@Override
	public void initialize(URL location, ResourceBundle resources)
	{
		_labTablePacketCount.textProperty().bind(Bindings.format(UIStrings.get("packettab.footer.count.table", "%d"), Bindings.size(_tablePackets)));
		_labMemoryPacketCount.textProperty().bind(Bindings.format(UIStrings.get("packettab.footer.count.memory", "%d"), Bindings.size(_memoryPackets)));
		
		_tvPackets.setItems(_tablePackets);
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
	
	/**
	 * Adds a new packet to the underlying table view.
	 * 
	 * @param packet a packet
	 * @param toTable whether the packet should be visible immediately
	 */
	public void addPacket(PacketLogEntry packet, boolean toTable)
	{
		_memoryPackets.add(packet);
		if (toTable)
			_tablePackets.add(packet);
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
