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
package net.l2emuproject.proxy.ui.javafx.main.view;

import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.Optional;
import java.util.ResourceBundle;

import eu.revengineer.simplejse.logging.BytesizeInterpreter;
import eu.revengineer.simplejse.logging.BytesizeInterpreter.BytesizeUnit;

import net.l2emuproject.lang.L2TextBuilder;
import net.l2emuproject.lang.management.ShutdownManager;
import net.l2emuproject.lang.management.TerminationStatus;
import net.l2emuproject.proxy.script.NetProScriptCache;
import net.l2emuproject.proxy.ui.i18n.UIStrings;
import net.l2emuproject.proxy.ui.javafx.FXLocator;
import net.l2emuproject.proxy.ui.savormix.loader.LoadOption;
import net.l2emuproject.util.concurrent.L2ThreadPool;
import net.l2emuproject.util.logging.L2Logger;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ObservableValueBase;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Slider;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.ToggleButton;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

/**
 * @author _dev_
 */
public class MainWindowController implements Initializable
{
	static final L2Logger LOG = L2Logger.getLogger(MainWindowController.class);
	
	@FXML
	private TabPane _tpConnections;
	
	@FXML
	private Tab _tabConsole;
	
	@FXML
	private TextArea _taConsole;
	
	@FXML
	private ToggleButton _tbConsoleWrap;
	
	@FXML
	private ToggleButton _tbConsoleScroll;
	
	@FXML
	private Slider _sConsoleFontSize;
	
	@FXML
	private Tab _tabIdle;
	
	@FXML
	private ProgressBar _pbJvmHeap;
	
	@FXML
	private Label _labJvmHeap;
	
	@FXML
	private Label _labJvmType;
	
	@FXML
	private CheckBox _cbCaptureSession;
	
	@FXML
	private CheckBox _cbCaptureGlobal;
	
	@FXML
	private Label _labProtocol;
	
	@FXML
	private Label _labPacketDisplayConfig;
	
	@FXML
	private MenuItem _openNetProLog;
	
	@FXML
	private MenuItem _openPhxLog;
	
	@FXML
	private MenuItem _openPhxRawLog;
	
	@FXML
	private MenuItem _openPacketSamuraiLog;
	
	@FXML
	private Menu _mExport;
	
	@FXML
	private Menu _mSelectedPacketExport;
	
	@FXML
	private MenuItem _selectedPacket2Plaintext;
	
	@FXML
	private MenuItem _selectedPacket2XML;
	
	@FXML
	private MenuItem _visiblePackets2Plaintext;
	
	@FXML
	private MenuItem _visiblePackets2PlaintextFile;
	
	@FXML
	private MenuItem _visiblePackets2XML;
	
	@FXML
	private MenuItem _visiblePackets2XMLFile;
	
	@FXML
	private MenuItem _tablePackets2Plaintext;
	
	@FXML
	private MenuItem _tablePackets2PlaintextFile;
	
	@FXML
	private MenuItem _tablePackets2XML;
	
	@FXML
	private MenuItem _tablePackets2XMLFile;
	
	@FXML
	private MenuItem _memoryPackets2Plaintext;
	
	@FXML
	private MenuItem _memoryPackets2PlaintextFile;
	
	@FXML
	private MenuItem _memoryPackets2XML;
	
	@FXML
	private MenuItem _memoryPackets2XMLFile;
	
	@FXML
	private MenuItem _npLog2PhxLog;
	
	@FXML
	private MenuItem _npLog2PhxRawLog;
	
	@FXML
	private MenuItem _npLog2PacketSamuraiLog;
	
	@FXML
	private MenuItem _npLog2Plaintext;
	
	@FXML
	private MenuItem _npLog2XML;
	
	@FXML
	private MenuItem _npLog2Stream;
	
	@FXML
	private CheckMenuItem _showLogConsole;
	
	@FXML
	private MenuItem _jvmGC;
	
	@FXML
	private MenuItem _jvmExit;
	
	@FXML
	private MenuItem _scrollLock;
	
	@FXML
	private Menu _mPacketDisplay;
	
	@FXML
	private MenuItem _packetExplainer;
	
	@FXML
	private MenuItem _packetBuilder;
	
	@FXML
	private MenuItem _packetReload;
	
	@FXML
	private Menu _mScripts;
	
	@FXML
	private MenuItem _scriptLoad;
	
	@FXML
	private MenuItem _scriptUnload;
	
	@FXML
	private MenuItem _scriptLoadAll;
	
	@FXML
	private MenuItem _configExplainer;
	
	@FXML
	private MenuItem _about;
	
	@Override
	public void initialize(URL location, ResourceBundle resources)
	{
		final Timeline tlHeapUsage = new Timeline(new KeyFrame(Duration.ZERO, e ->
		{
			final long free = Runtime.getRuntime().freeMemory(), total = Runtime.getRuntime().totalMemory(), used = total - free;
			final L2TextBuilder tb = new L2TextBuilder(BytesizeInterpreter.consolidate(used, BytesizeUnit.BYTES, BytesizeUnit.BYTES, BytesizeUnit.MEBIBYTES, "0M"));
			int end = tb.indexOf("m");
			tb.setCharAt(end - 1, Character.toUpperCase(tb.charAt(end)));
			tb.setLength(end);
			tb.append(" of ").append(BytesizeInterpreter.consolidate(total, BytesizeUnit.BYTES, BytesizeUnit.BYTES, BytesizeUnit.MEBIBYTES, "0M"));
			end = tb.indexOf("m", end + 1);
			tb.setCharAt(end - 1, Character.toUpperCase(tb.charAt(end)));
			tb.setLength(end);
			tb.append(" (").append(used * 100 / total).append("%)");
			_pbJvmHeap.setProgress((double)used / total);
			_labJvmHeap.setText(tb.moveToString());
		}), new KeyFrame(Duration.seconds(1)));
		tlHeapUsage.setCycleCount(Animation.INDEFINITE);
		tlHeapUsage.play();
		_labJvmType.setText(UIStrings.get("main.javaenv", NetProScriptCache.getInstance().isCompilerUnavailable() ? "JRE" : "JDK"));
		
		_taConsole.wrapTextProperty().bind(_tbConsoleWrap.selectedProperty());
		_taConsole.styleProperty().bind(Bindings.concat("-fx-font-size:", _sConsoleFontSize.valueProperty(), "; -fx-font-family: Consolas, monospace"));
		clearConsole(null);
		
		_mScripts.disableProperty().bind(new ObservableValueBase<Boolean>()
		{
			@Override
			public Boolean getValue()
			{
				return LoadOption.DISABLE_SCRIPTS.isSet() || NetProScriptCache.getInstance().isCompilerUnavailable();
			}
		});
		
		_about.setOnAction(evt ->
		{
			final Scene aboutDialog;
			try
			{
				final FXMLLoader loader = new FXMLLoader(FXLocator.getFXML(AboutDialogController.class), UIStrings.getBundle());
				aboutDialog = new Scene(loader.load());
			}
			catch (IOException e)
			{
				LOG.error("", e);
				return;
			}
			
			final Stage about = new Stage(StageStyle.TRANSPARENT);
			about.initModality(Modality.APPLICATION_MODAL);
			about.setTitle(UIStrings.get("about.title"));
			about.setScene(aboutDialog);
			about.showAndWait();
		});
	}
	
	@FXML
	private void clearConsole(ActionEvent evt)
	{
		final Date now = new Date();
		_taConsole.setPromptText(UIStrings.get("main.consoletab.prompt", now, now, now));
		_taConsole.clear();
	}
	
	@FXML
	void copySelectedPacket(ActionEvent event)
	{
	
	}
	
	@FXML
	void copySelectedPacketXML(ActionEvent event)
	{
	
	}
	
	@FXML
	void copyVisiblePackets(ActionEvent event)
	{
	
	}
	
	@FXML
	void copyVisiblePacketsXML(ActionEvent event)
	{
	
	}
	
	@FXML
	void doGC(ActionEvent event)
	{
		L2ThreadPool.submitLongRunning(System::gc);
	}
	
	@FXML
	void exitApp(ActionEvent event)
	{
		Platform.exit();
		ShutdownManager.exit(TerminationStatus.MANUAL_SHUTDOWN);
	}
	
	@FXML
	void showOpenLogNP(ActionEvent event)
	{
		//final FileChooser fc = new FileChooser();
		//fc.set
	}
	
	@FXML
	void showOpenLogPH(ActionEvent event)
	{
	
	}
	
	@FXML
	void showOpenLogPS(ActionEvent event)
	{
	
	}
	
	@FXML
	void showOpenLogRawPH(ActionEvent event)
	{
	
	}
	
	@FXML
	void showSaveVisiblePackets(ActionEvent event)
	{
	
	}
	
	@FXML
	void showSaveVisiblePacketsXML(ActionEvent event)
	{
	
	}
	
	@FXML
	void loadScript(ActionEvent event)
	{
		final TextInputDialog nameDialog = new TextInputDialog();
		nameDialog.setTitle(UIStrings.get("scripts.load.dialog.title"));
		nameDialog.setHeaderText(UIStrings.get("scripts.fqcn.explanation"));
		final Optional<String> result = nameDialog.showAndWait();
	}
	
	@FXML
	void unloadScript(ActionEvent event)
	{
		final TextInputDialog nameDialog = new TextInputDialog();
		nameDialog.setTitle(UIStrings.get("scripts.unload.dialog.title"));
		nameDialog.setHeaderText(UIStrings.get("scripts.fqcn.explanation"));
		final Optional<String> result = nameDialog.showAndWait();
	}
	
	@FXML
	void loadAllScripts(ActionEvent event)
	{
	
	}
	
	@FXML
	private void toggleConsole(ActionEvent evt)
	{
		if (_showLogConsole.isSelected())
		{
			_tpConnections.getTabs().add(0, _tabConsole);
			_tpConnections.getSelectionModel().select(0);
		}
		else
			_tpConnections.getTabs().remove(_tabConsole);
	}
	
	/**
	 * Adds a new message to the console tab.
	 * 
	 * @param message log entry
	 */
	public void appendToConsole(String message)
	{
		final double left = _taConsole.getScrollLeft(), top = _taConsole.getScrollTop();
		_taConsole.appendText(message);
		_taConsole.appendText("\r\n");
		if (_tbConsoleScroll.isSelected())
		{
			_taConsole.setScrollLeft(left);
			_taConsole.setScrollTop(top);
		}
	}
}
