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

import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.apache.commons.lang3.tuple.Pair;

import net.l2emuproject.proxy.network.EndpointType;
import net.l2emuproject.proxy.network.Proxy;
import net.l2emuproject.proxy.network.ServiceType;
import net.l2emuproject.proxy.network.packets.ProxyRepeatedPacket;
import net.l2emuproject.proxy.setup.IPAliasManager;
import net.l2emuproject.proxy.state.entity.context.ServerSocketID;
import net.l2emuproject.proxy.ui.ReceivedPacket;
import net.l2emuproject.proxy.ui.i18n.UIStrings;
import net.l2emuproject.proxy.ui.javafx.packet.Packet2Html;
import net.l2emuproject.util.HexUtil;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.util.StringConverter;

/**
 * Handles a single packet builder tab.
 * 
 * @author _dev_
 */
public class PacketBuilderTabController
{
	@FXML
	private Button _btnConv;
	
	@FXML
	private Button _btnCopy;
	
	@FXML
	private TextField _tfConvInput;
	
	@FXML
	private RadioButton _rbInt8;
	
	@FXML
	private ToggleGroup _dataType;
	
	@FXML
	private RadioButton _rbInt16;
	
	@FXML
	private RadioButton _rbInt32;
	
	@FXML
	private RadioButton _rbInt64;
	
	@FXML
	private RadioButton _rbSingle;
	
	@FXML
	private RadioButton _rbDouble;
	
	@FXML
	private RadioButton _rbStringNT;
	
	@FXML
	private RadioButton _rbStringSz;
	
	@FXML
	private TextArea _taConvOutput;
	
	@FXML
	private TextArea _taHexInput;
	
	@FXML
	private Button _btnSend;
	
	@FXML
	ChoiceBox<Pair<Integer, Proxy>> _cbConnections;
	
	@FXML
	private RadioButton _rbClient;
	
	@FXML
	private ToggleGroup _packetType;
	
	@FXML
	private RadioButton _rbServer;
	
	@FXML
	private PacketInterpViewController _packetDisplayController;
	
	// used only on the JavaFX application thread
	private static final ByteBuffer CONVERSION_BUFFER = ByteBuffer.allocate(4096).order(ByteOrder.LITTLE_ENDIAN);
	
	@FXML
	private void initialize()
	{
		final ReadOnlyObjectProperty<Toggle> dtProp = _dataType.selectedToggleProperty();
		_btnConv.disableProperty().bind(dtProp.isNull().or(_tfConvInput.textProperty().isEmpty().and(dtProp.isNotEqualTo(_rbStringNT).and(dtProp.isNotEqualTo(_rbStringSz)))));
		_btnCopy.disableProperty().bind(_taConvOutput.textProperty().isEmpty());
		_btnSend.disableProperty()
				.bind(_taHexInput.textProperty().length().lessThan(2).or(_packetType.selectedToggleProperty().isNull()).or(_cbConnections.getSelectionModel().selectedItemProperty().isNull()));
		_cbConnections.setConverter(new StringConverter<Pair<Integer, Proxy>>(){
			@Override
			public String toString(Pair<Integer, Proxy> object)
			{
				//return object.getLeft() + " " + IPAliasManager.toUserFriendlyString(object.getRight().getServer().getHostAddress());
				final Proxy client = object.getRight();
				return UIStrings.get("packettab.title", object.getLeft(), IPAliasManager.toUserFriendlyString(client.getHostAddress()),
						IPAliasManager.toUserFriendlyString(client.getServer().getHostAddress()));
			}
			
			@Override
			public Pair<Integer, Proxy> fromString(String string)
			{
				final int cid = Integer.parseInt(string.substring(0, string.indexOf(' ')));
				for (final Pair<Integer, Proxy> e : _cbConnections.getItems())
					if (e.getLeft() == cid)
						return e;
				return null;
			}
		});
		_cbConnections.getSelectionModel().selectedItemProperty().addListener((obs, old, neu) -> refreshPacketDisplay());
		_packetType.selectedToggleProperty().addListener((obs, old, neu) -> refreshPacketDisplay());
		_taHexInput.textProperty().addListener((obs, old, neu) -> refreshPacketDisplay());
	}
	
	private void refreshPacketDisplay()
	{
		final Pair<Integer, Proxy> con = _cbConnections.getSelectionModel().getSelectedItem();
		if (con == null)
			return;
		
		final Toggle packetType = _packetType.getSelectedToggle();
		if (packetType == null)
			return;
		
		final byte[] packetContent = HexUtil.hexStringToBytes(_taHexInput.getText());
		if (packetContent.length < 1)
		{
			_packetDisplayController.setContent("", "");
			return;
		}
		
		final ReceivedPacket rp = new ReceivedPacket(ServiceType.GAME, EndpointType.valueOf(packetType == _rbClient), packetContent);
		final Pair<String, String> html = Packet2Html.getHTML(rp, con.getRight().getProtocol(), new ServerSocketID(con.getRight().getServer().getInetSocketAddress()));
		_packetDisplayController.setContent(html.getLeft(), html.getRight());
	}
	
	@FXML
	private void append(ActionEvent event)
	{
		_taHexInput.appendText("\r\n");
		_taHexInput.appendText(_taConvOutput.getText());
		_taConvOutput.setText("");
	}
	
	@FXML
	private void convert(ActionEvent event)
	{
		final Toggle dataType = _dataType.getSelectedToggle();
		if (dataType == null)
			return;
		
		CONVERSION_BUFFER.clear();
		if (dataType == _rbStringNT || dataType == _rbStringSz)
		{
			final String input = _tfConvInput.getText();
			if (dataType == _rbStringSz)
				CONVERSION_BUFFER.putShort((short)input.length());
			int written = 0;
			try
			{
				for (final char c : input.toCharArray())
				{
					CONVERSION_BUFFER.putChar(c);
					++written;
				}
			}
			catch (final BufferOverflowException e)
			{
				if (dataType == _rbStringSz)
					CONVERSION_BUFFER.putShort(0, (short)written);
				if (dataType == _rbStringNT)
					CONVERSION_BUFFER.position(CONVERSION_BUFFER.position() - 2);
			}
			if (dataType == _rbStringNT)
				CONVERSION_BUFFER.putChar('\0');
			_taConvOutput.setText(HexUtil.bytesToHexString(CONVERSION_BUFFER.array(), 0, CONVERSION_BUFFER.position(), " "));
			return;
		}
		
		if (dataType == _rbSingle || dataType == _rbDouble)
		{
			try
			{
				final double value = Double.parseDouble(_tfConvInput.getText());
				if (dataType == _rbSingle)
					CONVERSION_BUFFER.putFloat((float)value);
				else
					CONVERSION_BUFFER.putDouble(value);
				_taConvOutput.setText(HexUtil.bytesToHexString(CONVERSION_BUFFER.array(), 0, CONVERSION_BUFFER.position(), " "));
			}
			catch (final NumberFormatException e)
			{
				_taConvOutput.setText("");
			}
			return;
		}
		
		try
		{
			final long value = Long.parseLong(_tfConvInput.getText());
			if (dataType == _rbInt8)
			{
				if (value == (value & 0xFF))
					CONVERSION_BUFFER.put((byte)value);
			}
			else if (dataType == _rbInt16)
			{
				if (value == (value & 0xFF_FF))
					CONVERSION_BUFFER.putShort((short)value);
			}
			else if (dataType == _rbInt32)
			{
				if (value == (value & 0xFF_FF_FF_FFL))
					CONVERSION_BUFFER.putInt((int)value);
			}
			else
				CONVERSION_BUFFER.putLong(value);
			_taConvOutput.setText(HexUtil.bytesToHexString(CONVERSION_BUFFER.array(), 0, CONVERSION_BUFFER.position(), " "));
		}
		catch (final NumberFormatException e)
		{
			_taConvOutput.setText("");
		}
	}
	
	@FXML
	private void sendPacket(ActionEvent event)
	{
		final Toggle packetType = _packetType.getSelectedToggle();
		if (packetType == null)
			return;
		
		final Pair<Integer, Proxy> connection = _cbConnections.getValue();
		if (connection == null)
			return;
		
		Proxy target = connection.getRight();
		if (packetType == _rbClient) // client packet goes to server
			target = target.getServer();
		
		// for clarity, auth injections are no longer supported in this version
		final byte[] packet = HexUtil.hexStringToBytes(_taHexInput.getText());
		if (packet.length < 1)
			return;
		
		target.sendPacket(new ProxyRepeatedPacket(packet));
	}
	
	/**
	 * Binds the list of injectable connections to this tab.
	 * 
	 * @param clientConnections injectable connections
	 */
	public void setClientConnections(ObservableList<Pair<Integer, Proxy>> clientConnections)
	{
		_cbConnections.setItems(clientConnections);
	}
}
