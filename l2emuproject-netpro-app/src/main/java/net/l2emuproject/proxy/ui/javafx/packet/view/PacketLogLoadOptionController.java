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

import static net.l2emuproject.proxy.ui.javafx.UtilityDialogs.makeNonModalUtilityAlert;

import net.l2emuproject.network.protocol.IProtocolVersion;

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Accordion;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TitledPane;
import javafx.scene.control.Tooltip;
import javafx.stage.Modality;
import javafx.stage.Window;

/**
 * @author _dev_
 */
public final class PacketLogLoadOptionController
{
	@FXML
	private TitledPane _tpWrapper;
	
	@FXML
	private Label _labSize;
	
	@FXML
	private Tooltip _ttSize;
	
	@FXML
	private Label _labVersion;
	
	@FXML
	private Label _labPacketCount;
	
	@FXML
	private ComboBox<IProtocolVersion> _cbProtocol;
	
	@FXML
	private CheckBox _cbInvisible;
	
	@FXML
	private CheckBox _cbInjected;
	
	@FXML
	private CheckBox _cbNonCaptured;
	
	private Window getDialogWindow()
	{
		return _cbProtocol.getScene().getWindow();
	}
	
	@FXML
	private void loadPacketLog(ActionEvent event)
	{
		final IProtocolVersion protocolVersion = _cbProtocol.getSelectionModel().getSelectedItem();
		if (protocolVersion == null)
		{
			final Alert alert = makeNonModalUtilityAlert(AlertType.ERROR, getDialogWindow(), "open.netpro.err.dialog.title", "open.netpro.err.dialog.header.noprotocol",
					"open.netpro.err.dialog.content.noprotocol");
			alert.initModality(Modality.WINDOW_MODAL);
			alert.show();
			return;
		}
	}
	
	@FXML
	private void closeTab(ActionEvent event)
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
	
	/**
	 * Initializes the packet log summary view.
	 * 
	 * @param filename filename of the packet log
	 * @param approxSize approximate filesize representation
	 * @param exactSize exact filesize declaration
	 * @param logVersion packet log file version
	 * @param packetCount packet amount in log
	 * @param applicableProtocols all protocol versions applicable to this type of log
	 * @param detectedProtocol log protocol version
	 */
	public void setPacketLog(String filename, String approxSize, String exactSize, String logVersion, String packetCount, ObservableList<IProtocolVersion> applicableProtocols,
			IProtocolVersion detectedProtocol)
	{
		_tpWrapper.setText(filename);
		_labSize.setText(approxSize);
		_ttSize.setText(exactSize);
		_labVersion.setText(logVersion);
		_labPacketCount.setText(packetCount);
		_cbProtocol.setItems(applicableProtocols);
		_cbProtocol.getSelectionModel().select(detectedProtocol);
	}
}
