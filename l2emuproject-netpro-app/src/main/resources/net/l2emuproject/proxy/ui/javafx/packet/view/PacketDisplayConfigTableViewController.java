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
import java.util.Set;

import org.google.jhsheets.filtered.FilteredTableView;

import net.l2emuproject.network.protocol.IProtocolVersion;
import net.l2emuproject.proxy.network.EndpointType;
import net.l2emuproject.proxy.network.meta.IPacketTemplate;
import net.l2emuproject.proxy.ui.javafx.packet.IPacketHidingConfig;
import net.l2emuproject.proxy.ui.javafx.packet.PacketTemplateEntry;
import net.l2emuproject.proxy.ui.javafx.packet.ProtocolPacketHidingManager;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;

/**
 * @author _dev_
 */
public class PacketDisplayConfigTableViewController implements Initializable
{
	@FXML
	private FilteredTableView<PacketTemplateEntry> _tvTemplates;
	
	@FXML
	private TableColumn<FilteredTableView<PacketTemplateEntry>, Boolean> _tcShowInTab;
	
	private final ObservableList<PacketTemplateEntry> _templates;
	
	public PacketDisplayConfigTableViewController()
	{
		_templates = FXCollections.observableArrayList();
	}
	
	@Override
	public void initialize(URL location, ResourceBundle resources)
	{
		_tvTemplates.setItems(_templates);
	}
	
	@FXML
	void setAllHidden(ActionEvent event)
	{
		for (final PacketTemplateEntry templateEntry : _templates)
			(_tcShowInTab.isVisible() ? templateEntry.visibleInTabProperty() : templateEntry.visibleInProtocolProperty()).set(false);
	}
	
	@FXML
	void setAllVisible(ActionEvent event)
	{
		for (final PacketTemplateEntry templateEntry : _templates)
			(_tcShowInTab.isVisible() ? templateEntry.visibleInTabProperty() : templateEntry.visibleInProtocolProperty()).set(true);
	}
	
	public void setTemplates(Set<IPacketTemplate> templates, EndpointType endpointType, IPacketHidingConfig tabHidingConfig, IProtocolVersion protocolVersion)
	{
		_tcShowInTab.setVisible(tabHidingConfig != null);
		final IPacketHidingConfig protocolHidingConfig = ProtocolPacketHidingManager.getInstance().getHidingConfiguration(protocolVersion);
		for (final IPacketTemplate template : templates)
			_templates.add(new PacketTemplateEntry(template, tabHidingConfig != null ? !tabHidingConfig.isHidden(endpointType, template) : false,
					!protocolHidingConfig.isHidden(endpointType, template), protocolVersion));
	}
}
