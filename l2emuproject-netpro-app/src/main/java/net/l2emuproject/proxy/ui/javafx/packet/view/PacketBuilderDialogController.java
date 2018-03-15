/*
 * Copyright 2011-2018 L2EMU UNIQUE
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

import static net.l2emuproject.proxy.ui.javafx.UtilityDialogs.wrapException;

import java.io.IOException;

import org.apache.commons.lang3.tuple.Pair;

import net.l2emuproject.proxy.network.Proxy;
import net.l2emuproject.proxy.ui.i18n.UIStrings;
import net.l2emuproject.proxy.ui.javafx.FXUtils;
import net.l2emuproject.util.StackTraceUtil;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.stage.Modality;
import javafx.stage.Window;

/**
 * Handles the packet builder dialog.
 * 
 * @author _dev_
 */
public final class PacketBuilderDialogController
{
	@FXML
	private Tab _tabAddNew;
	
	private ObservableList<Pair<Integer, Proxy>> _clientConnections = FXCollections.emptyObservableList();
	private int _tabNumber = 0;
	
	@FXML
	private void addNewBuilder(Event event)
	{
		if (!_tabAddNew.isSelected())
			return;
		
		final TabPane tp = _tabAddNew.getTabPane();
		if (tp == null)
			return;
		
		final Tab tab;
		final PacketBuilderTabController controller;
		try
		{
			final FXMLLoader loader = new FXMLLoader(FXUtils.getFXML(PacketBuilderTabController.class), UIStrings.getBundle());
			tab = new Tab(String.valueOf(++_tabNumber), loader.load());
			tab.setOnClosed(this::onBuilderRemoved);
			controller = loader.getController();
		}
		catch (final IOException e)
		{
			final Throwable t = StackTraceUtil.stripUntilClassContext(e, true, PacketBuilderTabController.class.getName());
			wrapException(t, "generic.err.internal.title", null, "generic.err.internal.header.fxml", null, getWindow(), Modality.WINDOW_MODAL).show();
			return;
		}
		controller.setClientConnections(_clientConnections);
		
		final ObservableList<Tab> tabs = tp.getTabs();
		final int index = tabs.size() - 1;
		tabs.add(index, tab);
		_tabAddNew.getTabPane().getSelectionModel().clearAndSelect(index);
		if (index == 0)
		{
			tab.setClosable(false);
			return;
		}
		for (final Tab t : tabs)
			if (t != _tabAddNew)
				t.setClosable(true);
	}
	
	@FXML
	private void onBuilderRemoved(Event event)
	{
		final ObservableList<Tab> tabs = _tabAddNew.getTabPane().getTabs();
		if (tabs.size() > 2)
			return;
		for (final Tab t : tabs)
			t.setClosable(false);
	}
	
	/**
	 * Returns the primary application window.
	 * 
	 * @return main application window
	 */
	private Window getWindow()
	{
		return _tabAddNew.getContent().getScene().getWindow();
	}
	
	/**
	 * Binds the list of injectable connections to this dialog.
	 * 
	 * @param clientConnections injectable connections
	 */
	public void setClientConnections(ObservableList<Pair<Integer, Proxy>> clientConnections)
	{
		_clientConnections = clientConnections;
		addNewBuilder(null);
	}
}
