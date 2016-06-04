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
import org.google.jhsheets.filtered.tablecolumn.FilterableStringTableColumn;

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
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;

/**
 * @author _dev_
 */
public class PacketDisplayConfigTableViewController implements Initializable
{
	@FXML
	private FilteredTableView<PacketTemplateEntry> _tvTemplates;
	
	@FXML
	private TableColumn<FilteredTableView<PacketTemplateEntry>, Boolean> _tcShowInTab;
	
	@FXML
	private TableColumn<FilteredTableView<PacketTemplateEntry>, Boolean> _tcShowInProtocol;
	
	@FXML
	private TableColumn<FilteredTableView<PacketTemplateEntry>, String> _tcTemplateOps;
	
	@FXML
	private FilterableStringTableColumn<FilteredTableView<PacketTemplateEntry>, String> _tcTemplateName;
	
	private final ObservableList<PacketTemplateEntry> _templates;
	
	private EndpointType _endpointType;
	private Runnable _onConfigChange;
	
	public PacketDisplayConfigTableViewController()
	{
		_templates = FXCollections.observableArrayList();
	}
	
	@Override
	public void initialize(URL location, ResourceBundle resources)
	{
		_tcShowInTab.setCellValueFactory(new PropertyValueFactory<>("visibleInTab"));
		_tcShowInTab.setCellFactory(CheckBoxTableCell.forTableColumn(_tcShowInTab));
		_tcShowInProtocol.setCellValueFactory(new PropertyValueFactory<>("visibleInProtocol"));
		_tcShowInProtocol.setCellFactory(CheckBoxTableCell.forTableColumn(_tcShowInProtocol));
		_tcTemplateOps.setCellValueFactory(new PropertyValueFactory<>("opcode"));
		_tcTemplateName.setCellValueFactory(new PropertyValueFactory<>("name"));
		
		_tvTemplates.setItems(_templates);
	}
	
	@FXML
	void setAllHidden(ActionEvent event)
	{
		final Runnable postAction = _onConfigChange;
		_onConfigChange = null;
		for (final PacketTemplateEntry templateEntry : _templates)
			(_tcShowInTab.isVisible() ? templateEntry.visibleInTabProperty() : templateEntry.visibleInProtocolProperty()).set(false);
		if (postAction != null)
			(_onConfigChange = postAction).run();
	}
	
	@FXML
	void setAllVisible(ActionEvent event)
	{
		final Runnable postAction = _onConfigChange;
		_onConfigChange = null;
		for (final PacketTemplateEntry templateEntry : _templates)
			(_tcShowInTab.isVisible() ? templateEntry.visibleInTabProperty() : templateEntry.visibleInProtocolProperty()).set(true);
		if (postAction != null)
			(_onConfigChange = postAction).run();
	}
	
	public void setTemplates(Set<IPacketTemplate> templates, EndpointType endpointType, IPacketHidingConfig tabHidingConfig, IProtocolVersion protocolVersion, Runnable onConfigChange)
	{
		_endpointType = endpointType;
		_onConfigChange = onConfigChange;
		
		_tcShowInTab.setVisible(tabHidingConfig != null);
		final IPacketHidingConfig protocolHidingConfig = ProtocolPacketHidingManager.getInstance().getHidingConfiguration(protocolVersion);
		for (final IPacketTemplate template : templates)
		{
			final PacketTemplateEntry entry = new PacketTemplateEntry(template, tabHidingConfig != null ? !tabHidingConfig.isHidden(endpointType, template) : false,
					!protocolHidingConfig.isHidden(endpointType, template), protocolVersion);
			entry.visibleInProtocolProperty().addListener((obs, old, neu) ->
			{
				if (neu)
					protocolHidingConfig.setVisible(endpointType, template);
				else
					protocolHidingConfig.setHidden(endpointType, template);
				ProtocolPacketHidingManager.getInstance().markModified(protocolVersion);
				if (_onConfigChange != null)
					_onConfigChange.run();
			});
			if (tabHidingConfig != null)
			{
				entry.visibleInTabProperty().addListener((obs, old, neu) ->
				{
					if (neu)
						tabHidingConfig.setVisible(endpointType, template);
					else
						tabHidingConfig.setHidden(endpointType, template);
					if (_onConfigChange != null)
						_onConfigChange.run();
				});
			}
			_templates.add(entry);
		}
	}
}
