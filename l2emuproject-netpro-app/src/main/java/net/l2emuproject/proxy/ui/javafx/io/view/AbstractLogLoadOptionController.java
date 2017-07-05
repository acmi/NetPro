/*
 * Copyright 2011-2017 L2EMU UNIQUE
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
package net.l2emuproject.proxy.ui.javafx.io.view;

import net.l2emuproject.network.protocol.IProtocolVersion;
import net.l2emuproject.proxy.ui.javafx.main.view.MainWindowController;

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Accordion;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TitledPane;
import javafx.scene.control.Tooltip;
import javafx.stage.Window;

/**
 * Contains basic features for allowing injection into an accordion as well as interaction with the main application window.
 * 
 * @author _dev_
 * @param <T> type of user data object
 */
public abstract class AbstractLogLoadOptionController<T>
{
	protected MainWindowController _mainWindow;
	
	@FXML
	private TitledPane _tpWrapper;
	
	@FXML
	private Label _labSize;
	
	@FXML
	private Tooltip _ttSize;
	
	@FXML
	protected ComboBox<IProtocolVersion> _cbProtocol;
	
	@FXML
	protected CheckBox _cbInvisible;
	
	/**
	 * Links this controller with the main window.
	 * 
	 * @param mainWindow primary application window
	 */
	public void setMainWindow(MainWindowController mainWindow)
	{
		_mainWindow = mainWindow;
	}
	
	/**
	 * Initializes the packet log summary view.
	 * 
	 * @param filename filename of the packet log
	 * @param approxSize approximate filesize representation
	 * @param exactSize exact filesize declaration
	 * @param applicableProtocols all protocol versions applicable to this type of log
	 * @param detectedProtocol log protocol version
	 */
	protected void setPacketLog(String filename, String approxSize, String exactSize, ObservableList<IProtocolVersion> applicableProtocols, IProtocolVersion detectedProtocol)
	{
		_tpWrapper.setText(filename);
		_labSize.setText(approxSize);
		_ttSize.setText(exactSize);
		_cbProtocol.setItems(applicableProtocols);
		_cbProtocol.getSelectionModel().select(detectedProtocol);
	}
	
	protected Window getDialogWindow()
	{
		return _cbProtocol.getScene().getWindow();
	}
	
	@SuppressWarnings("unchecked")
	protected T getUserData()
	{
		return (T)_tpWrapper.getUserData();
	}
	
	@FXML
	protected void closeTab(ActionEvent event)
	{
		final Window wnd = getDialogWindow();
		
		final Accordion tabPane = (Accordion)_tpWrapper.getParent();
		final ObservableList<TitledPane> panes = tabPane.getPanes();
		if (panes.size() == 1)
		{
			wnd.hide();
			return;
		}
		
		int index = panes.indexOf(_tpWrapper);
		panes.remove(index);
		if (index >= panes.size())
			index = panes.size() - 1;
		tabPane.setExpandedPane(panes.get(index));
		
		// if animation is enabled, this must run after animation completes
		wnd.sizeToScene();
	}
}
