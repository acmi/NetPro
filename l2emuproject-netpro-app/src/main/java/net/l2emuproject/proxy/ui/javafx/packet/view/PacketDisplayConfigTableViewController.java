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
import org.google.jhsheets.filtered.operators.StringOperator;
import org.google.jhsheets.filtered.tablecolumn.ColumnFilterEvent;
import org.google.jhsheets.filtered.tablecolumn.FilterableStringTableColumn;

import net.l2emuproject.network.protocol.IProtocolVersion;
import net.l2emuproject.proxy.network.EndpointType;
import net.l2emuproject.proxy.network.meta.IPacketTemplate;
import net.l2emuproject.proxy.ui.javafx.FXUtils;
import net.l2emuproject.proxy.ui.javafx.packet.IPacketHidingConfig;
import net.l2emuproject.proxy.ui.javafx.packet.PacketTemplateEntry;
import net.l2emuproject.proxy.ui.javafx.packet.ProtocolPacketHidingManager;

import javafx.beans.property.ObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;

/**
 * Handles the packet hiding configuration view for a single endpoint type.
 * 
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
	
	private final ObservableList<PacketTemplateEntry> _allTemplates, _visibleTemplates;
	
	private Runnable _onTabConfigChange, _onProtocolConfigChange;
	
	/** Creates this controller. */
	public PacketDisplayConfigTableViewController()
	{
		_allTemplates = FXCollections.observableArrayList();
		_visibleTemplates = FXCollections.observableArrayList();
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
		
		final SortedList<PacketTemplateEntry> sortableTablePackets = new SortedList<>(_visibleTemplates);
		sortableTablePackets.comparatorProperty().bind(_tvTemplates.comparatorProperty());
		_tvTemplates.setItems(sortableTablePackets);
		_tvTemplates.addEventHandler(ColumnFilterEvent.FILTER_CHANGED_EVENT, e -> applyFilters());
	}
	
	@FXML
	private void setAllHidden(ActionEvent event)
	{
		if (_tcShowInTab.isVisible())
		{
			final Runnable onCompletion = _onTabConfigChange;
			_onTabConfigChange = null;
			for (final PacketTemplateEntry templateEntry : _visibleTemplates)
				templateEntry.visibleInTabProperty().set(false);
			if (onCompletion != null)
				(_onTabConfigChange = onCompletion).run();
			
			return;
		}
		
		final Runnable onCompletion = _onProtocolConfigChange;
		_onProtocolConfigChange = null;
		for (final PacketTemplateEntry templateEntry : _visibleTemplates)
			templateEntry.visibleInProtocolProperty().set(false);
		if (onCompletion != null)
			(_onProtocolConfigChange = onCompletion).run();
	}
	
	@FXML
	private void setAllVisible(ActionEvent event)
	{
		if (_tcShowInTab.isVisible())
		{
			final Runnable onCompletion = _onTabConfigChange;
			_onTabConfigChange = null;
			for (final PacketTemplateEntry templateEntry : _visibleTemplates)
				templateEntry.visibleInTabProperty().set(true);
			if (onCompletion != null)
				(_onTabConfigChange = onCompletion).run();
			
			return;
		}
		
		final Runnable onCompletion = _onProtocolConfigChange;
		_onProtocolConfigChange = null;
		for (final PacketTemplateEntry templateEntry : _visibleTemplates)
			templateEntry.visibleInProtocolProperty().set(true);
		if (onCompletion != null)
			(_onProtocolConfigChange = onCompletion).run();
	}
	
	private boolean isHiddenByName(PacketTemplateEntry packetEntry)
	{
		for (final StringOperator filter : _tcTemplateName.getFilters())
			if (FXUtils.isHidden(packetEntry.nameProperty().get(), filter))
				return true;
		
		return false;
	}
	
	private void applyFilters()
	{
		final ObservableList<PacketTemplateEntry> filterMatchingPackets = FXCollections.observableArrayList();
		for (final PacketTemplateEntry packetEntry : _allTemplates)
			if (!isHiddenByName(packetEntry))
				filterMatchingPackets.add(packetEntry);
		_visibleTemplates.setAll(filterMatchingPackets);
	}
	
	/**
	 * Fills the table with given packet templates and binds the UI controls with underlying packet hiding configurations.
	 * 
	 * @param endpointType endpoint type for all the templates
	 * @param templates known packet templates
	 * @param tabHidingConfigProperty packet hiding config of the associated tab (or {@code null})
	 * @param onTabConfigChange action to be taken when tab config changes
	 * @param protocolVersion associated protocol version
	 * @param onProtocolConfigChange action to be taken when protocol config changes
	 */
	public void setTemplates(EndpointType endpointType, Set<IPacketTemplate> templates, ObjectProperty<IPacketHidingConfig> tabHidingConfigProperty, Runnable onTabConfigChange,
			IProtocolVersion protocolVersion, Runnable onProtocolConfigChange)
	{
		_onTabConfigChange = onTabConfigChange;
		_onProtocolConfigChange = onProtocolConfigChange;
		
		_tcShowInTab.setVisible(tabHidingConfigProperty != null);
		_allTemplates.clear();
		final ObservableValue<IPacketHidingConfig> protocolHidingConfig = ProtocolPacketHidingManager.getInstance().getHidingConfiguration(protocolVersion);
		for (final IPacketTemplate template : templates)
		{
			final PacketTemplateEntry entry = new PacketTemplateEntry(template, tabHidingConfigProperty != null ? !tabHidingConfigProperty.get().isHidden(endpointType, template) : false,
					!protocolHidingConfig.getValue().isHidden(endpointType, template), protocolVersion);
			entry.visibleInProtocolProperty().addListener((obs, old, neu) ->
			{
				protocolHidingConfig.getValue().setHidden(endpointType, template, !neu);
				ProtocolPacketHidingManager.getInstance().markModified(protocolVersion);
				if (_onProtocolConfigChange != null)
					_onProtocolConfigChange.run();
			});
			if (tabHidingConfigProperty != null)
			{
				entry.visibleInTabProperty().addListener((obs, old, neu) ->
				{
					tabHidingConfigProperty.get().setHidden(endpointType, template, !neu);
					if (_onTabConfigChange != null)
						_onTabConfigChange.run();
				});
			}
			_allTemplates.add(entry);
		}
		applyFilters();
	}
}
