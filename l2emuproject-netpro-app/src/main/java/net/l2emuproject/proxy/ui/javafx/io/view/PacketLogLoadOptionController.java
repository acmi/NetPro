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
package net.l2emuproject.proxy.ui.javafx.io.view;

import static net.l2emuproject.proxy.ui.javafx.UtilityDialogs.makeNonModalUtilityAlert;
import static net.l2emuproject.proxy.ui.javafx.UtilityDialogs.wrapException;

import java.io.IOException;
import java.nio.channels.ClosedByInterruptException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import net.l2emuproject.network.protocol.IProtocolVersion;
import net.l2emuproject.proxy.io.exception.LogFileIterationIOException;
import net.l2emuproject.proxy.io.packetlog.LogFileHeader;
import net.l2emuproject.proxy.io.packetlog.LogFilePacket;
import net.l2emuproject.proxy.io.packetlog.LogLoadOptions;
import net.l2emuproject.proxy.io.packetlog.LogLoadOptions.LogLoadFlag;
import net.l2emuproject.proxy.io.packetlog.NetProPacketLogFileIterator;
import net.l2emuproject.proxy.io.packetlog.PacketLogFileUtils;
import net.l2emuproject.proxy.script.LogLoadScriptManager;
import net.l2emuproject.proxy.ui.ReceivedPacket;
import net.l2emuproject.proxy.ui.i18n.UIStrings;
import net.l2emuproject.proxy.ui.javafx.FXUtils;
import net.l2emuproject.proxy.ui.javafx.WindowTracker;
import net.l2emuproject.proxy.ui.javafx.main.view.MainWindowController;
import net.l2emuproject.proxy.ui.javafx.packet.PacketLogEntry;
import net.l2emuproject.proxy.ui.javafx.packet.view.PacketLogTabController;
import net.l2emuproject.proxy.ui.javafx.packet.view.PacketLogTabUserData;
import net.l2emuproject.proxy.ui.savormix.io.task.HistoricalPacketLog;
import net.l2emuproject.util.StackTraceUtil;
import net.l2emuproject.util.concurrent.L2ThreadPool;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Accordion;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TitledPane;
import javafx.scene.control.Tooltip;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;

/**
 * Handles the packet log loading option selection dialog.
 * 
 * @author _dev_
 */
public final class PacketLogLoadOptionController
{
	private MainWindowController _mainWindow;
	
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
		if (_mainWindow == null)
		{
			final Alert alert = makeNonModalUtilityAlert(AlertType.ERROR, getDialogWindow(), "open.netpro.err.dialog.title", "generic.err.internal.header", null, "PLLO_1");
			alert.initModality(Modality.WINDOW_MODAL);
			alert.show();
			return;
		}
		
		final LogFileHeader logFileHeader;
		try
		{
			logFileHeader = (LogFileHeader)_tpWrapper.getUserData();
		}
		catch (NullPointerException | ClassCastException e)
		{
			final Alert alert = makeNonModalUtilityAlert(AlertType.ERROR, getDialogWindow(), "open.netpro.err.dialog.title", "generic.err.internal.header", null, "PLLO_2");
			alert.initModality(Modality.WINDOW_MODAL);
			alert.show();
			return;
		}
		
		final IProtocolVersion protocolVersion = _cbProtocol.getSelectionModel().getSelectedItem();
		if (protocolVersion == null)
		{
			final Alert alert = makeNonModalUtilityAlert(AlertType.ERROR, getDialogWindow(), "open.netpro.err.dialog.title", "open.netpro.err.dialog.header.noprotocol",
					"open.netpro.err.dialog.content.noprotocol");
			alert.initModality(Modality.WINDOW_MODAL);
			alert.show();
			return;
		}
		
		final String filename = logFileHeader.getLogFile().getFileName().toString();
		
		final Tab tab;
		final PacketLogTabController controller;
		
		final Scene progressDialogScene;
		final LogFileLoadProgressDialogController progressDialog;
		try
		{
			FXMLLoader loader = new FXMLLoader(FXUtils.getFXML(PacketLogTabController.class), UIStrings.getBundle());
			tab = new Tab(filename, loader.load());
			controller = loader.getController();
			
			loader = new FXMLLoader(FXUtils.getFXML(LogFileLoadProgressDialogController.class), UIStrings.getBundle());
			progressDialogScene = new Scene(loader.load(), null);
			progressDialog = loader.getController();
		}
		catch (final IOException e)
		{
			final Throwable t = StackTraceUtil.stripUntilClassContext(e, true, PacketLogLoadOptionController.class.getName());
			wrapException(t, "generic.err.internal.title", null, "generic.err.internal.header.fxml", null, getDialogWindow(), Modality.WINDOW_MODAL).show();
			return;
		}
		
		final HistoricalPacketLog cacheContext = new HistoricalPacketLog(logFileHeader.getLogFile());
		controller.protocolProperty().set(protocolVersion);
		controller.setEntityCacheContext(cacheContext);
		controller.setOnProtocolPacketHidingConfigurationChange(_mainWindow::refreshFilters);
		
		closeTab(event);
		tab.setUserData(new PacketLogTabUserData(controller));
		_mainWindow.addConnectionTab(tab);
		final int totalPackets = logFileHeader.getPackets();
		progressDialog.setFilename(filename);
		progressDialog.setLoadedAmount(0, totalPackets);
		
		final Stage progressDialogWindow = new Stage(StageStyle.UTILITY);
		progressDialogWindow.initModality(Modality.NONE);
		progressDialogWindow.initOwner(_mainWindow.getMainWindow());
		progressDialogWindow.setTitle(UIStrings.get("open.netpro.loaddlg.title"));
		progressDialogWindow.setScene(progressDialogScene);
		progressDialogWindow.getIcons().addAll(FXUtils.getIconListFX());
		progressDialogWindow.sizeToScene();
		progressDialogWindow.setResizable(false);
		WindowTracker.getInstance().add(progressDialogWindow);
		progressDialogWindow.show();
		
		final Set<LogLoadFlag> flags = EnumSet.noneOf(LogLoadFlag.class);
		if (_cbInvisible.isSelected())
			flags.add(LogLoadFlag.INCLUDE_NON_VISIBLE);
		if (_cbInjected.isSelected())
			flags.add(LogLoadFlag.INCLUDE_SYNTHETIC);
		if (_cbNonCaptured.isSelected())
			flags.add(LogLoadFlag.INCLUDE_NON_CAPTURED);
		final LogLoadOptions options = new LogLoadOptions(protocolVersion, flags);
		final LogLoadScriptManager scriptManager = LogLoadScriptManager.getInstance();
		
		final AtomicBoolean canUpdateUI = new AtomicBoolean(true);
		final AtomicInteger packetsRead = new AtomicInteger(0);
		final List<PacketLogEntry> packets = new ArrayList<>();
		final Future<?> loadTask = L2ThreadPool.submitLongRunning(() -> {
			try (final NetProPacketLogFileIterator it = PacketLogFileUtils.getPacketIterator(logFileHeader))
			{
				while (it.hasNext())
				{
					if (Thread.interrupted())
						return;
					
					final LogFilePacket packet = it.next();
					packetsRead.incrementAndGet();
					// scripts enable analytics on packets that will be visible in the table
					scriptManager.onLoadedPacket(logFileHeader.getService().isLogin(), packet.getEndpoint().isClient(), packet.getContent(), protocolVersion, cacheContext, packet.getReceivalTime());
					if (PacketLogFileUtils.isLoadable(packet, options))
					{
						final PacketLogEntry packetEntry = new PacketLogEntry(new ReceivedPacket(logFileHeader.getService(), packet.getEndpoint(), packet.getContent(), packet.getReceivalTime()));
						packetEntry.updateView(protocolVersion);
						
						synchronized (packets)
						{
							packets.add(packetEntry);
							if (canUpdateUI.getAndSet(false))
							{
								Platform.runLater(() -> {
									synchronized (packets)
									{
										controller.addPackets(packets);
										packets.clear();
										progressDialog.setLoadedAmount(packetsRead.get(), totalPackets);
										canUpdateUI.set(true);
									}
								});
							}
						}
					}
				}
			}
			catch (final IOException e) // trying to open
			{
				final Throwable t = StackTraceUtil.stripUntilClassContext(e, true, PacketLogLoadOptionController.class.getName());
				Platform.runLater(
						() -> wrapException(t, "open.netpro.err.dialog.title.named", new Object[]
				{ filename }, "open.netpro.err.dialog.header.io", null, getDialogWindow(), Modality.NONE).show());
			}
			catch (final LogFileIterationIOException ew) // during read
			{
				final Throwable e = ew.getCause();
				if (e instanceof ClosedByInterruptException) // user cancelled operation
					return;
				
				final Throwable t = StackTraceUtil.stripUntilClassContext(e, true, PacketLogLoadOptionController.class.getName());
				Platform.runLater(
						() -> wrapException(t, "open.netpro.err.dialog.title.named", new Object[]
				{ filename }, "open.netpro.err.dialog.header.io", null, getDialogWindow(), Modality.NONE).show());
			}
			finally
			{
				Platform.runLater(progressDialogWindow::hide);
			}
		});
		progressDialog.setTask(loadTask);
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
