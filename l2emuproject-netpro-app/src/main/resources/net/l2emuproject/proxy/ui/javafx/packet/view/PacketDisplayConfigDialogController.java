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

import java.util.Set;

import net.l2emuproject.network.protocol.IProtocolVersion;
import net.l2emuproject.proxy.network.EndpointType;
import net.l2emuproject.proxy.network.meta.IPacketTemplate;
import net.l2emuproject.proxy.ui.javafx.packet.IPacketHidingConfig;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;

/**
 * @author _dev_
 */
public class PacketDisplayConfigDialogController
{
	@FXML
	private PacketDisplayConfigTableViewController _clientPacketTableController;
	
	@FXML
	private PacketDisplayConfigTableViewController _serverPacketTableController;
	
	@FXML
	private void exportHidingConfig(ActionEvent event)
	{
		
	}
	
	@FXML
	private void importHidingConfig(ActionEvent event)
	{
		
	}
	
	public void setPacketTemplates(Set<IPacketTemplate> clientPackets, Set<IPacketTemplate> serverPackets, IPacketHidingConfig tabHidingConfig, IProtocolVersion protocolVersion,
			Runnable onConfigChange)
	{
		_clientPacketTableController.setTemplates(clientPackets, EndpointType.CLIENT, tabHidingConfig, protocolVersion, onConfigChange);
		_serverPacketTableController.setTemplates(serverPackets, EndpointType.SERVER, tabHidingConfig, protocolVersion, onConfigChange);
	}
}
