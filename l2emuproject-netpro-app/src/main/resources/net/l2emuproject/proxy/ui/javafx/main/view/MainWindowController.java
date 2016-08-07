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

import static javafx.scene.control.Alert.AlertType.ERROR;
import static javafx.scene.control.Alert.AlertType.INFORMATION;
import static javafx.scene.control.Alert.AlertType.WARNING;
import static net.l2emuproject.proxy.ui.javafx.UtilityDialogs.initNonModalUtilityDialog;
import static net.l2emuproject.proxy.ui.javafx.UtilityDialogs.makeNonModalUtilityAlert;
import static net.l2emuproject.proxy.ui.javafx.UtilityDialogs.showChoiceDialog;
import static net.l2emuproject.proxy.ui.javafx.UtilityDialogs.showWaitDialog;
import static net.l2emuproject.proxy.ui.javafx.UtilityDialogs.wrapException;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import eu.revengineer.simplejse.exception.DependencyResolutionException;
import eu.revengineer.simplejse.exception.MutableOperationInProgressException;
import eu.revengineer.simplejse.logging.BytesizeInterpreter;
import eu.revengineer.simplejse.logging.BytesizeInterpreter.BytesizeUnit;
import eu.revengineer.simplejse.type.UnloadableScript;

import net.l2emuproject.lang.L2System;
import net.l2emuproject.lang.L2TextBuilder;
import net.l2emuproject.lang.management.ShutdownManager;
import net.l2emuproject.lang.management.TerminationStatus;
import net.l2emuproject.network.protocol.IProtocolVersion;
import net.l2emuproject.network.protocol.ProtocolVersionManager;
import net.l2emuproject.proxy.NetPro;
import net.l2emuproject.proxy.io.IOConstants;
import net.l2emuproject.proxy.io.definitions.VersionnedPacketTable;
import net.l2emuproject.proxy.io.exception.DamagedFileException;
import net.l2emuproject.proxy.io.exception.EmptyPacketLogException;
import net.l2emuproject.proxy.io.exception.FilesizeMeasureException;
import net.l2emuproject.proxy.io.exception.IncompletePacketLogFileException;
import net.l2emuproject.proxy.io.exception.InsufficientlyLargeFileException;
import net.l2emuproject.proxy.io.exception.TruncatedPacketLogFileException;
import net.l2emuproject.proxy.io.exception.UnknownFileTypeException;
import net.l2emuproject.proxy.io.packetlog.LogFileHeader;
import net.l2emuproject.proxy.io.packetlog.PacketLogFileUtils;
import net.l2emuproject.proxy.io.packetlog.l2ph.L2PhLogFileHeader;
import net.l2emuproject.proxy.io.packetlog.l2ph.L2PhLogFileUtils;
import net.l2emuproject.proxy.network.EndpointType;
import net.l2emuproject.proxy.network.ServiceType;
import net.l2emuproject.proxy.network.meta.IPacketTemplate;
import net.l2emuproject.proxy.network.meta.UserDefinedProtocolVersion;
import net.l2emuproject.proxy.network.meta.container.OpcodeOwnerSet;
import net.l2emuproject.proxy.script.NetProScriptCache;
import net.l2emuproject.proxy.ui.i18n.BytesizeFormat;
import net.l2emuproject.proxy.ui.i18n.UIStrings;
import net.l2emuproject.proxy.ui.javafx.ExceptionAlert;
import net.l2emuproject.proxy.ui.javafx.FXUtils;
import net.l2emuproject.proxy.ui.javafx.WindowTracker;
import net.l2emuproject.proxy.ui.javafx.error.view.ExceptionSummaryDialogController;
import net.l2emuproject.proxy.ui.javafx.io.view.L2PhPacketLogLoadOptionController;
import net.l2emuproject.proxy.ui.javafx.io.view.PacketLogLoadOptionController;
import net.l2emuproject.proxy.ui.javafx.packet.IPacketHidingConfig;
import net.l2emuproject.proxy.ui.javafx.packet.view.PacketDisplayConfigDialogController;
import net.l2emuproject.proxy.ui.javafx.packet.view.PacketLogTabController;
import net.l2emuproject.proxy.ui.savormix.loader.LoadOption;
import net.l2emuproject.util.HexUtil;
import net.l2emuproject.util.StackTraceUtil;
import net.l2emuproject.util.concurrent.L2ThreadPool;
import net.l2emuproject.util.logging.L2Logger;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.value.ObservableValueBase;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Accordion;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.Slider;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.TitledPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import javafx.util.Duration;

/**
 * A controller that takes care of UI elements found in the primary application screen (the one that is always open).
 * 
 * @author _dev_
 */
public class MainWindowController implements Initializable, IOConstants
{
	static final L2Logger LOG = L2Logger.getLogger(MainWindowController.class);
	
	private File _lastOpenDirectory = IOConstants.LOG_DIRECTORY.toFile();
	private File _lastOpenDirectoryL2PH = null;
	
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
	private ToggleButton _tbPacketHidingConfig;
	
	@FXML
	private BorderPane _packetHidingWrapper;
	
	@FXML
	private PacketDisplayConfigDialogController _packetHidingConfigController;
	
	@FXML
	private Label _labProtocol;
	
	@FXML
	private Menu _mExport;
	
	@FXML
	private Menu _mSelectedPacketExport;
	
	@FXML
	private Menu _mVisiblePacketExport;
	
	@FXML
	private Menu _mMemoryPacketExport;
	
	@FXML
	private CheckMenuItem _showLogConsole;
	
	@FXML
	private CheckMenuItem _scrollLock;
	
	@FXML
	private Menu _mPacketDisplay;
	
	@FXML
	private Menu _mScripts;
	
	private final Map<IProtocolVersion, Stage> _openPacketHidingConfigWindows;
	
	/** Constructs this controller. */
	public MainWindowController()
	{
		_openPacketHidingConfigWindows = new HashMap<>();
	}
	
	@Override
	public void initialize(URL location, ResourceBundle resources)
	{
		final Timeline tlHeapUsage = new Timeline(new KeyFrame(Duration.ZERO, e -> {
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
		_labJvmType.setText(UIStrings.get("main.javaenv", L2System.isJREMode() ? "JRE" : "JDK", System.getProperty("java.specification.version", "1.?"),
				System.getProperty("java.vm.specification.version", "1.?")));
		final String javaPath = System.getProperty("java.home");
		if (javaPath != null)
			_labJvmType.setTooltip(new Tooltip(javaPath));
		
		_tpConnections.getSelectionModel().selectedItemProperty().addListener((obs, old, neu) -> {
			_packetHidingWrapper.setVisible(false);
			_tbPacketHidingConfig.setSelected(false);
			
			final Object ctrl = neu != null ? neu.getUserData() : null;
			if (ctrl == null || !(ctrl instanceof PacketLogTabController))
			{
				_mExport.setDisable(true);
				
				_cbCaptureSession.setVisible(false);
				_labProtocol.setVisible(false);
				_tbPacketHidingConfig.setVisible(false);
				return;
			}
			
			final PacketLogTabController controller = (PacketLogTabController)ctrl;
			
			_mSelectedPacketExport.disableProperty().bind(Bindings.not(controller.anyPacketSelected()));
			_mVisiblePacketExport.disableProperty().bind(Bindings.not(controller.hasVisiblePackets()));
			_mMemoryPacketExport.disableProperty().bind(Bindings.not(controller.hasMemoryPackets()));
			_mExport.setDisable(false);
			
			_labProtocol.textProperty().bind(Bindings.convert(controller.protocolProperty()));
			_labProtocol.setVisible(true);
			_tbPacketHidingConfig.setVisible(true);
		});
		_tpConnections.addEventHandler(KeyEvent.KEY_PRESSED, e -> {
			if (e.getCode() != KeyCode.F3 || e.isAltDown())
				return;
			
			e.consume(); // always consume it here while tabpane/any child is focused
			
			final Tab currentTab = _tpConnections.getSelectionModel().getSelectedItem();
			if (currentTab != null && currentTab.isClosable())
			{
				_tpConnections.getTabs().remove(currentTab);
				currentTab.getOnClosed().handle(null);
			}
		});
		
		_taConsole.wrapTextProperty().bind(_tbConsoleWrap.selectedProperty());
		_taConsole.styleProperty().bind(Bindings.concat("-fx-font-size:", _sConsoleFontSize.valueProperty(), "; -fx-font-family: Consolas, monospace"));
		clearConsole(null);
		
		_mScripts.disableProperty().bind(new ObservableValueBase<Boolean>(){
			@Override
			public Boolean getValue()
			{
				return LoadOption.DISABLE_SCRIPTS.isSet() || NetProScriptCache.getInstance().isCompilerUnavailable();
			}
		});
		
		rebuildProtocolMenu();
	}
	
	private void rebuildProtocolMenu()
	{
		for (final Window wnd : _openPacketHidingConfigWindows.values())
			wnd.hide();
		_openPacketHidingConfigWindows.clear();
		
		_mPacketDisplay.getItems().clear();
		_mPacketDisplay.setDisable(LoadOption.DISABLE_DEFS.isSet());
		if (_mPacketDisplay.isDisable())
			return;
		
		final ObservableList<MenuItem> allCategories = _mPacketDisplay.getItems();
		
		final Menu authSubmenu = new Menu("Auth");
		for (final IProtocolVersion protocol : VersionnedPacketTable.getInstance().getKnownProtocols(ServiceType.LOGIN))
		{
			if (!(protocol instanceof UserDefinedProtocolVersion))
				continue;
			
			final MenuItem mi = new MenuItem(String.valueOf(protocol));
			mi.setUserData(protocol);
			mi.setOnAction(this::openProtocolPacketHidingConfigWindow);
			authSubmenu.getItems().add(mi);
		}
		allCategories.add(authSubmenu);
		allCategories.add(new SeparatorMenuItem());
		
		final SortedMap<String, Menu> gameProtocolCategories = new TreeMap<>();
		for (final IProtocolVersion protocol : VersionnedPacketTable.getInstance().getKnownProtocols(ServiceType.GAME))
		{
			if (!(protocol instanceof UserDefinedProtocolVersion))
				continue;
			
			final MenuItem mi = new MenuItem(String.valueOf(protocol));
			mi.setUserData(protocol);
			mi.setOnAction(this::openProtocolPacketHidingConfigWindow);
			gameProtocolCategories.computeIfAbsent(((UserDefinedProtocolVersion)protocol).getCategory(), Menu::new).getItems().add(mi);
		}
		allCategories.addAll(gameProtocolCategories.values());
	}
	
	private void openProtocolPacketHidingConfigWindow(ActionEvent event)
	{
		final Object userData = ((MenuItem)event.getSource()).getUserData();
		if (!(userData instanceof IProtocolVersion))
		{
			final Alert alert = makeNonModalUtilityAlert(AlertType.ERROR, getMainWindow(), "generic.err.internal.title", "generic.err.internal.header", null, "MWC_OPPHC");
			alert.initModality(Modality.WINDOW_MODAL);
			alert.show();
			return;
		}
		
		final IProtocolVersion protocol = (IProtocolVersion)userData;
		final Stage openWnd = _openPacketHidingConfigWindows.get(protocol);
		if (openWnd != null)
		{
			openWnd.toFront();
			return;
		}
		
		final PacketLogTabController currentTabController = getCurrentPacketTabController();
		if (protocol.equals(currentTabController.protocolProperty().get()))
		{
			if (_tbPacketHidingConfig.isSelected())
				return; // an UI control is already open
				
			_tbPacketHidingConfig.setSelected(true);
			showPacketHidingConfig(event);
			return;
		}
		
		try
		{
			final Stage wnd = new Stage(StageStyle.DECORATED);
			wnd.setTitle(String.valueOf(protocol));
			wnd.getIcons().addAll(FXUtils.getIconListFX());
			
			final FXMLLoader loader = new FXMLLoader(FXUtils.getFXML(PacketDisplayConfigDialogController.class), UIStrings.getBundle());
			wnd.setScene(new Scene(loader.load(), null));
			final PacketDisplayConfigDialogController controller = loader.getController();
			setPacketTemplates(controller, protocol, null, null);
			
			wnd.setOnHidden(e -> _openPacketHidingConfigWindows.remove(protocol, wnd));
			_openPacketHidingConfigWindows.put(protocol, wnd);
			wnd.show();
		}
		catch (final IOException e)
		{
			final Throwable t = StackTraceUtil.stripUntilClassContext(e, true, MainWindowController.class.getName());
			wrapException(t, "generic.err.internal.title", null, "generic.err.internal.header.fxml", null, getMainWindow(), Modality.NONE).show();
			return;
		}
	}
	
	/**
	 * Returns the primary application window.
	 * 
	 * @return main application window
	 */
	public Window getMainWindow()
	{
		return _tpConnections.getScene().getWindow();
	}
	
	private PacketLogTabController getCurrentPacketTabController()
	{
		final Tab packetTab = _tpConnections.getSelectionModel().getSelectedItem();
		if (packetTab == null)
			return null;
		
		final Object controller = packetTab.getUserData();
		return controller instanceof PacketLogTabController ? (PacketLogTabController)controller : null;
	}
	
	@FXML
	private void clearConsole(ActionEvent evt)
	{
		final Date now = new Date();
		_taConsole.setPromptText(UIStrings.get("main.consoletab.prompt", now, now, now));
		_taConsole.clear();
	}
	
	@FXML
	private void showPacketHidingConfig(ActionEvent evt)
	{
		if (!_tbPacketHidingConfig.isSelected())
		{
			_packetHidingWrapper.setVisible(false);
			return;
		}
		
		final PacketLogTabController packetTabController = getCurrentPacketTabController();
		if (packetTabController == null)
		{
			_tbPacketHidingConfig.setSelected(false);
			return;
		}
		
		final IProtocolVersion protocol = packetTabController.protocolProperty().get();
		final Stage openWnd = _openPacketHidingConfigWindows.get(protocol);
		if (openWnd != null)
			openWnd.hide(); // do not have two UI controls open at the same time
			
		setPacketTemplates(_packetHidingConfigController, protocol, packetTabController.packetHidingConfigProperty(), packetTabController::applyFilters);
		_packetHidingWrapper.setVisible(true);
	}
	
	private final void setPacketTemplates(PacketDisplayConfigDialogController controller, IProtocolVersion protocol, ObjectProperty<IPacketHidingConfig> tabHidingConfigProperty,
			Runnable onTabConfigChage)
	{
		final VersionnedPacketTable table = VersionnedPacketTable.getInstance();
		final Set<IPacketTemplate> clientPackets = table.getCurrentTemplates(protocol, EndpointType.CLIENT).collect(Collectors.toCollection(() -> new TreeSet<>(OpcodeOwnerSet.COMPARATOR)));
		final Set<IPacketTemplate> serverPackets = table.getCurrentTemplates(protocol, EndpointType.SERVER).collect(Collectors.toCollection(() -> new TreeSet<>(OpcodeOwnerSet.COMPARATOR)));
		controller.setPacketTemplates(clientPackets, serverPackets, tabHidingConfigProperty, onTabConfigChage, protocol, () -> refreshFilters(protocol));
	}
	
	@FXML
	private void copySelectedPacket(ActionEvent event)
	{
		final PacketLogTabController controller = getCurrentPacketTabController();
		if (controller != null)
			controller.copyPacketAsPlaintext(event);
	}
	
	@FXML
	private void copySelectedPacketXML(ActionEvent event)
	{
		final PacketLogTabController controller = getCurrentPacketTabController();
		if (controller != null)
			controller.copyPacketAsXML(event);
	}
	
	@FXML
	private void copyVisiblePackets(ActionEvent event)
	{
		final PacketLogTabController controller = getCurrentPacketTabController();
		if (controller != null)
			controller.copyVisiblePacketsAsPlaintext(event);
	}
	
	@FXML
	private void copyVisiblePacketsXML(ActionEvent event)
	{
		final PacketLogTabController controller = getCurrentPacketTabController();
		if (controller != null)
			controller.copyVisiblePacketsAsXML(event);
	}
	
	// --------------------------
	// ========== FILE MENU BEGIN
	// --------------------------
	
	@FXML
	private void showOpenLogNP(ActionEvent event)
	{
		final FileChooser fc = new FileChooser();
		fc.setTitle(UIStrings.get("open.netpro.fileselect.title"));
		fc.getExtensionFilters().addAll(new ExtensionFilter(UIStrings.get("open.netpro.fileselect.description"), "*." + LOG_EXTENSION),
				new ExtensionFilter(UIStrings.get("generic.filedlg.allfiles"), "*.*"));
		fc.setInitialDirectory(_lastOpenDirectory);
		
		final List<File> selectedFiles = fc.showOpenMultipleDialog(getMainWindow());
		if (selectedFiles == null || selectedFiles.isEmpty())
			return;
		
		_lastOpenDirectory = selectedFiles.iterator().next().getParentFile();
		
		final WaitingIndicatorDialogController waitDialog = showWaitDialog(getMainWindow(), "generic.waitdlg.title",
				selectedFiles.size() > 1 ? "open.netpro.waitdlg.header.multiple" : "open.netpro.waitdlg.header",
				selectedFiles.size() > 1 ? String.valueOf(selectedFiles.size()) : selectedFiles.iterator().next().getName());
		final Future<?> preprocessTask = L2ThreadPool.submitLongRunning(() -> {
			final List<LogFileHeader> validLogFiles = new ArrayList<>(selectedFiles.size());
			for (final File selectedFile : selectedFiles)
			{
				final Path packetLogFile = selectedFile.toPath();
				final String filename = packetLogFile.getFileName().toString();
				try
				{
					validLogFiles.add(PacketLogFileUtils.getMetadata(packetLogFile));
				}
				catch (final InterruptedException e)
				{
					// cancelled by user
					validLogFiles.clear();
					break;
				}
				catch (FilesizeMeasureException | IOException e)
				{
					final Throwable t = StackTraceUtil.stripUntilClassContext(e, true, MainWindowController.class.getName());
					Platform.runLater(
							() -> wrapException(t, "open.netpro.err.dialog.title.named", new Object[]
					{ filename }, "open.netpro.err.dialog.header.io", null, getMainWindow(), Modality.NONE).show());
				}
				catch (final InsufficientlyLargeFileException e)
				{
					Platform.runLater(() -> makeNonModalUtilityAlert(WARNING, getMainWindow(), "open.netpro.err.dialog.title.named", new Object[] { filename },
							"open.netpro.err.dialog.header.toosmall", null, "open.netpro.err.dialog.content.toosmall", filename).show());
				}
				catch (final IncompletePacketLogFileException e)
				{
					// TODO: if not currently locked (being written), offer to repair automatically
					// otherwise inform that it represents a currently active connection
					
					// for now just inform and skip
					Platform.runLater(() -> makeNonModalUtilityAlert(ERROR, getMainWindow(), "open.netpro.err.dialog.title.named", new Object[] { filename },
							"open.netpro.err.dialog.header.incomplete", null, "open.netpro.err.dialog.content.incomplete", filename).show());
				}
				catch (final UnknownFileTypeException e)
				{
					Platform.runLater(() -> makeNonModalUtilityAlert(WARNING, getMainWindow(), "open.netpro.err.dialog.title.named", new Object[] { filename },
							"open.netpro.err.dialog.header.wrongfile", null, "open.netpro.err.dialog.content.wrongfile", filename, HexUtil.bytesToHexString(e.getMagic8Bytes(), " ")).show());
				}
				catch (final TruncatedPacketLogFileException e)
				{
					Platform.runLater(() -> makeNonModalUtilityAlert(ERROR, getMainWindow(), "open.netpro.err.dialog.title.named", new Object[] { filename }, "open.netpro.err.dialog.header.truncated",
							null, "open.netpro.err.dialog.content.truncated", filename).show());
				}
				catch (final EmptyPacketLogException e)
				{
					Platform.runLater(() -> makeNonModalUtilityAlert(WARNING, getMainWindow(), "open.netpro.err.dialog.title.named", new Object[] { filename }, "open.netpro.err.dialog.header.empty",
							null, "open.netpro.err.dialog.content.empty", filename).show());
				}
				catch (final DamagedFileException e)
				{
					Platform.runLater(() -> makeNonModalUtilityAlert(ERROR, getMainWindow(), "open.netpro.err.dialog.title.named", new Object[] { filename }, "open.netpro.err.dialog.header.damaged",
							null, "open.netpro.err.dialog.content.damaged", filename).show());
				}
				catch (final RuntimeException e)
				{
					final Throwable t = StackTraceUtil.stripUntilClassContext(e, true, MainWindowController.class.getName());
					Platform.runLater(
							() -> wrapException(t, "open.netpro.err.dialog.title.named", new Object[]
					{ filename }, "open.netpro.err.dialog.header.runtime", null, getMainWindow(), Modality.NONE).show());
				}
			}
			
			Platform.runLater(() -> {
				waitDialog.onWaitEnd();
				
				if (validLogFiles.isEmpty())
					return;
				
				// make the tabbed window here
				final Stage confirmStage = new Stage(StageStyle.DECORATED);
				
				final TitledPane[] tabs = new TitledPane[validLogFiles.size()];
				try
				{
					for (int i = 0; i < tabs.length; ++i)
					{
						final LogFileHeader validLogFile = validLogFiles.get(i);
						
						final String approxSize, exactSize;
						final NumberFormat integerFormat = NumberFormat.getIntegerInstance(UIStrings.CURRENT_LOCALE);
						final long filesize = validLogFile.getLogFileSize();
						if (filesize != -1)
						{
							approxSize = BytesizeFormat.formatAsDecimal(filesize);
							exactSize = UIStrings.get("load.infodlg.details.size.tooltip", integerFormat.format(filesize));
						}
						else
						{
							approxSize = UIStrings.get("generic.unavailable");
							exactSize = UIStrings.get("load.infodlg.details.size.unavailable.tooltip");
						}
						
						final FXMLLoader loader = new FXMLLoader(FXUtils.getFXML(PacketLogLoadOptionController.class), UIStrings.getBundle());
						final TitledPane tab = loader.load();
						final PacketLogLoadOptionController controller = loader.getController();
						controller.setMainWindow(this);
						
						final ServiceType service = validLogFile.getService();
						final String filename = validLogFile.getLogFile().getFileName().toString();
						controller.setPacketLog(filename, approxSize, exactSize, HexUtil.fillHex(validLogFile.getVersion(), 2), integerFormat.format(validLogFile.getPackets()),
								FXCollections.observableArrayList(VersionnedPacketTable.getInstance().getKnownProtocols(service)),
								ProtocolVersionManager.getInstance().getProtocol(validLogFile.getProtocol(), service.isLogin()));
						tab.setUserData(validLogFile);
						tabs[i] = tab;
					}
				}
				catch (final IOException e)
				{
					final Throwable t = StackTraceUtil.stripUntilClassContext(e, true, MainWindowController.class.getName());
					wrapException(t, "generic.err.internal.title", null, "generic.err.internal.header.fxml", null, getMainWindow(), Modality.NONE).show();
					return;
				}
				
				final Accordion tabPane = new Accordion(tabs);
				tabPane.setExpandedPane(tabs[0]);
				
				// no owner here - any invalid load options should only block the confirm window, but not the main window
				confirmStage.setTitle(UIStrings.get("load.infodlg.title"));
				confirmStage.setScene(new Scene(tabPane));
				confirmStage.getIcons().addAll(FXUtils.getIconListFX());
				confirmStage.setAlwaysOnTop(true);
				WindowTracker.getInstance().add(confirmStage);
				confirmStage.show();
			});
		});
		waitDialog.setCancelAction(() -> preprocessTask.cancel(true));
	}
	
	@FXML
	private void showRepairLogNP(ActionEvent event)
	{
		final DirectoryChooser dc = new DirectoryChooser();
		dc.setTitle(UIStrings.get("repair.netpro.dirselect.title"));
		
		final File selectedDir = dc.showDialog(getMainWindow());
		if (selectedDir == null)
			return;
	}
	
	@FXML
	private void showOpenLogPH(ActionEvent event)
	{
		final FileChooser fc = new FileChooser();
		fc.setTitle(UIStrings.get("open.l2ph.fileselect.title"));
		fc.getExtensionFilters().addAll(new ExtensionFilter(UIStrings.get("open.l2ph.fileselect.description"), "*.pLog", "*.rawLog"),
				new ExtensionFilter(UIStrings.get("open.l2ph.fileselect.description.std"), "*.pLog"),
				new ExtensionFilter(UIStrings.get("open.l2ph.fileselect.description.raw"), "*.rawLog"),
				new ExtensionFilter(UIStrings.get("generic.filedlg.allfiles"), "*.*"));
		fc.setInitialDirectory(_lastOpenDirectoryL2PH);
		
		final List<File> selectedFiles = fc.showOpenMultipleDialog(getMainWindow());
		if (selectedFiles == null || selectedFiles.isEmpty())
			return;
		
		_lastOpenDirectoryL2PH = selectedFiles.iterator().next().getParentFile();
		
		final WaitingIndicatorDialogController waitDialog = showWaitDialog(getMainWindow(), "generic.waitdlg.title",
				selectedFiles.size() > 1 ? "open.netpro.waitdlg.header.multiple" : "open.netpro.waitdlg.header",
				selectedFiles.size() > 1 ? String.valueOf(selectedFiles.size()) : selectedFiles.iterator().next().getName());
		final Future<?> preprocessTask = L2ThreadPool.submitLongRunning(() -> {
			final List<L2PhLogFileHeader> validLogFiles = new ArrayList<>(selectedFiles.size());
			for (final File selectedFile : selectedFiles)
			{
				final Path packetLogFile = selectedFile.toPath();
				final String filename = packetLogFile.getFileName().toString();
				try
				{
					try
					{
						validLogFiles.add(L2PhLogFileUtils.getRawMetadata(packetLogFile));
					}
					catch (final UnknownFileTypeException e)
					{
						validLogFiles.add(L2PhLogFileUtils.getMetadata(packetLogFile));
					}
				}
				catch (final IOException e)
				{
					final Throwable t = StackTraceUtil.stripUntilClassContext(e, true, MainWindowController.class.getName());
					Platform.runLater(
							() -> wrapException(t, "open.netpro.err.dialog.title.named", new Object[]
					{ filename }, "open.netpro.err.dialog.header.io", null, getMainWindow(), Modality.NONE).show());
				}
				catch (final InsufficientlyLargeFileException e)
				{
					Platform.runLater(() -> makeNonModalUtilityAlert(WARNING, getMainWindow(), "open.netpro.err.dialog.title.named", new Object[] { filename },
							"open.netpro.err.dialog.header.toosmall", null, "open.netpro.err.dialog.content.toosmall", filename).show());
				}
				catch (final UnknownFileTypeException e)
				{
					Platform.runLater(() -> makeNonModalUtilityAlert(WARNING, getMainWindow(), "open.netpro.err.dialog.title.named", new Object[] { filename },
							"open.netpro.err.dialog.header.wrongfile", null, "open.netpro.err.dialog.content.wrongfile", filename, HexUtil.bytesToHexString(e.getMagic8Bytes(), " ")).show());
				}
				catch (final DamagedFileException e)
				{
					Platform.runLater(() -> makeNonModalUtilityAlert(ERROR, getMainWindow(), "open.netpro.err.dialog.title.named", new Object[] { filename }, "open.netpro.err.dialog.header.damaged",
							null, "open.netpro.err.dialog.content.damaged", filename).show());
				}
				catch (final RuntimeException e)
				{
					final Throwable t = StackTraceUtil.stripUntilClassContext(e, true, MainWindowController.class.getName());
					Platform.runLater(
							() -> wrapException(t, "open.netpro.err.dialog.title.named", new Object[]
					{ filename }, "open.netpro.err.dialog.header.runtime", null, getMainWindow(), Modality.NONE).show());
				}
			}
			
			Platform.runLater(() -> {
				waitDialog.onWaitEnd();
				
				if (validLogFiles.isEmpty())
					return;
				
				// make the tabbed window here
				final Stage confirmStage = new Stage(StageStyle.DECORATED);
				
				final TitledPane[] tabs = new TitledPane[validLogFiles.size()];
				try
				{
					for (int i = 0; i < tabs.length; ++i)
					{
						final L2PhLogFileHeader validLogFile = validLogFiles.get(i);
						
						final String approxSize, exactSize;
						final NumberFormat integerFormat = NumberFormat.getIntegerInstance(UIStrings.CURRENT_LOCALE);
						final long filesize = validLogFile.getLogFileSize();
						if (filesize != -1)
						{
							approxSize = BytesizeFormat.formatAsDecimal(filesize);
							exactSize = UIStrings.get("load.infodlg.details.size.tooltip", integerFormat.format(filesize));
						}
						else
						{
							approxSize = UIStrings.get("generic.unavailable");
							exactSize = UIStrings.get("load.infodlg.details.size.unavailable.tooltip");
						}
						
						final FXMLLoader loader = new FXMLLoader(FXUtils.getFXML(L2PhPacketLogLoadOptionController.class), UIStrings.getBundle());
						final TitledPane tab = loader.load();
						final L2PhPacketLogLoadOptionController controller = loader.getController();
						controller.setMainWindow(this);
						
						final ServiceType service = validLogFile.getFirstPacketServiceType();
						final String filename = validLogFile.getLogFile().getFileName().toString();
						controller.setPacketLog(filename, approxSize, exactSize, UIStrings.get(validLogFile.isRaw() ? "load.infodlg.phx.details.type.raw" : "load.infodlg.phx.details.type.std"),
								FXCollections.observableArrayList(VersionnedPacketTable.getInstance().getKnownProtocols(service)),
								ProtocolVersionManager.getInstance().getProtocol(validLogFile.getProtocol(), service.isLogin()), validLogFile.getFirstPacketArrivalTime());
						tab.setUserData(validLogFile);
						tabs[i] = tab;
					}
				}
				catch (final IOException e)
				{
					final Throwable t = StackTraceUtil.stripUntilClassContext(e, true, MainWindowController.class.getName());
					wrapException(t, "generic.err.internal.title", null, "generic.err.internal.header.fxml", null, getMainWindow(), Modality.NONE).show();
					return;
				}
				
				final Accordion tabPane = new Accordion(tabs);
				tabPane.setExpandedPane(tabs[0]);
				
				// no owner here - any invalid load options should only block the confirm window, but not the main window
				confirmStage.setTitle(UIStrings.get("load.infodlg.title"));
				confirmStage.setScene(new Scene(tabPane));
				confirmStage.getIcons().addAll(FXUtils.getIconListFX());
				confirmStage.setAlwaysOnTop(true);
				WindowTracker.getInstance().add(confirmStage);
				confirmStage.show();
			});
		});
		waitDialog.setCancelAction(() -> preprocessTask.cancel(true));
	}
	
	@FXML
	private void showOpenLogPS(ActionEvent event)
	{
		
	}
	
	@FXML
	private void showSaveVisiblePackets(ActionEvent event)
	{
		
	}
	
	@FXML
	private void showSaveVisiblePacketsXML(ActionEvent event)
	{
		
	}
	
	@FXML
	private void showSaveMemoryPackets(ActionEvent event)
	{
		
	}
	
	@FXML
	private void showSaveMemoryPacketsXML(ActionEvent event)
	{
		
	}
	
	@FXML
	private void showConvertToPlaintext(ActionEvent event)
	{
		
	}
	
	@FXML
	private void showConvertToXML(ActionEvent event)
	{
		
	}
	
	@FXML
	private void showConvertToPhx(ActionEvent event)
	{
		
	}
	
	@FXML
	private void showConvertToPhxRaw(ActionEvent event)
	{
		
	}
	
	@FXML
	private void showConvertToPacketSamurai(ActionEvent event)
	{
		
	}
	
	@FXML
	private void showConvertToStream(ActionEvent event)
	{
		
	}
	
	@FXML
	private void showPacketExplainer(ActionEvent event)
	{
		
	}
	
	@FXML
	private void openNewPacketInjectDialog(ActionEvent event)
	{
		
	}
	
	@FXML
	private void reloadDefinitions(ActionEvent event)
	{
		rebuildProtocolMenu();
	}
	
	@FXML
	private void toggleConsole(ActionEvent evt)
	{
		final ObservableList<Tab> tabs = _tpConnections.getTabs();
		if (_showLogConsole.isSelected())
		{
			tabs.add(0, _tabConsole);
			_tpConnections.getSelectionModel().select(0);
		}
		else
			tabs.remove(_tabConsole);
	}
	
	@FXML
	private void doGC(ActionEvent event)
	{
		L2ThreadPool.executeLongRunning(System::gc);
	}
	
	@FXML
	private void exitApp(ActionEvent event)
	{
		// see WindowTracker
		ShutdownManager.exit(TerminationStatus.MANUAL_SHUTDOWN);
	}
	
	// ------------------------
	// ========== FILE MENU END
	// ------------------------
	
	// ----------------------------
	// ========== SCRIPT MENU BEGIN
	// ----------------------------
	
	@FXML
	private void loadScript(ActionEvent event)
	{
		askForScriptName("scripts.load.dialog.title", "scripts.fqcn.explanation", this::loadScript);
	}
	
	@FXML
	private void unloadScript(ActionEvent event)
	{
		askForScriptName("scripts.unload.dialog.title", "scripts.fqcn.explanation", this::unloadScript);
	}
	
	private void askForScriptName(String inputDialogTitle, String inputDialogHeader, Consumer<WaitingIndicatorDialogController> action)
	{
		final TextInputDialog nameDialog = initNonModalUtilityDialog(new TextInputDialog(), getMainWindow(), inputDialogTitle, inputDialogHeader, null);
		final Optional<String> result = nameDialog.showAndWait();
		if (!result.isPresent())
			return;
		
		final String pattern = result.get();
		final WaitingIndicatorDialogController waitDialog = showWaitDialog(getMainWindow(), "generic.waitdlg.title", "scripts.load.waitdlg.content.index", pattern);
		waitDialog.getWindow().setUserData(pattern);
		
		action.accept(waitDialog);
	}
	
	private void loadScript(WaitingIndicatorDialogController waitDialogController)
	{
		final Window waitDialogWindow = waitDialogController.getWindow();
		final String pattern = String.valueOf(waitDialogWindow.getUserData());
		
		final Future<?> task = L2ThreadPool.submitLongRunning(() -> {
			try
			{
				final Set<String> matchingScripts = new TreeSet<>(NetProScriptCache.getInstance().findIndexedScripts(pattern, Integer.MAX_VALUE, 0));
				
				Platform.runLater(() -> {
					waitDialogController.onWaitEnd();
					
					if (matchingScripts.isEmpty())
					{
						makeNonModalUtilityAlert(INFORMATION, getMainWindow(), "scripts.load.err.dialog.title", "scripts.load.err.dialog.header.nonexistent",
								"scripts.load.err.dialog.content.nonexistent", pattern).show();
						return;
					}
					
					final String result;
					if (matchingScripts.size() > 1)
					{
						final Optional<String> resultWrapper = showChoiceDialog(getMainWindow(), "scripts.load.dialog.title", "scripts.load.dialog.header.select", matchingScripts);
						if (!resultWrapper.isPresent())
							return;
						
						result = resultWrapper.get();
					}
					else
						result = matchingScripts.iterator().next();
					
					final WaitingIndicatorDialogController nextWaitDialogController = showWaitDialog(getMainWindow(), "generic.waitdlg.title", "scripts.load.waitdlg.content.compile", result);
					final Future<?> nextTask = L2ThreadPool.submitLongRunning(() -> {
						final Map<Class<?>, RuntimeException> fqcn2Exception = new TreeMap<>((c1, c2) -> c1.getName().compareTo(c2.getName()));
						try
						{
							NetProScriptCache.getInstance().compileSingleScript(result, fqcn2Exception);
						}
						catch (final MutableOperationInProgressException e)
						{
							nextWaitDialogController.onWaitEnd();
							
							makeNonModalUtilityAlert(ERROR, getMainWindow(), "scripts.load.err.dialog.title", "scripts.load.err.dialog.header.singleop", "scripts.load.err.dialog.content.singleop",
									result).show();
							return;
						}
						catch (final DependencyResolutionException e)
						{
							nextWaitDialogController.onWaitEnd();
							
							makeNonModalUtilityAlert(ERROR, getMainWindow(), "scripts.load.err.dialog.title", "scripts.load.err.dialog.header.apt", "scripts.load.err.dialog.content.apt",
									System.getProperty("java.home"));
							return;
						}
						catch (final IOException e)
						{
							nextWaitDialogController.onWaitEnd();
							
							initNonModalUtilityDialog(new ExceptionAlert(e), getMainWindow(), "scripts.load.err.dialog.title", "scripts.load.err.dialog.header.runtime", null).show();
							return;
						}
						catch (final IllegalArgumentException e)
						{
							nextWaitDialogController.onWaitEnd();
							
							makeNonModalUtilityAlert(INFORMATION, getMainWindow(), "scripts.load.err.dialog.title", "scripts.load.err.dialog.header.nonexistent",
									"scripts.load.err.dialog.content.nonexistent", result).show();
							return;
						}
						
						Platform.runLater(() -> {
							nextWaitDialogController.onWaitEnd();
							
							if (fqcn2Exception.isEmpty())
							{
								makeNonModalUtilityAlert(INFORMATION, getMainWindow(), "scripts.load.done.dialog.title", "scripts.load.done.dialog.header", "scripts.load.done.dialog.content", result)
										.show();
								return;
							}
							
							final Alert alert = makeNonModalUtilityAlert(WARNING, getMainWindow(), "scripts.load.done.dialog.title", "scripts.load.done.dialog.header",
									"scripts.load.done.dialog.content.runtime", result);
							alert.getDialogPane().setExpandableContent(makeScriptExceptionMapExpandableContent(fqcn2Exception));
							alert.show();
						});
					});
					nextWaitDialogController.setCancelAction(() -> nextTask.cancel(true));
				});
			}
			catch (final MutableOperationInProgressException e)
			{
				waitDialogController.onWaitEnd();
				
				makeNonModalUtilityAlert(ERROR, getMainWindow(), "scripts.load.err.dialog.title", "scripts.load.err.dialog.header.singleop", "scripts.load.err.dialog.content.singleop", pattern)
						.show();
			}
			catch (final InterruptedException e)
			{
				// application shutting down
				waitDialogController.onWaitEnd();
			}
			catch (final IOException ex)
			{
				wrapException(ex, "scripts.load.err.dialog.title", null, "scripts.load.err.dialog.header.runtime", null, getMainWindow(), Modality.NONE).show();
			}
		});
		waitDialogController.setCancelAction(() -> task.cancel(true));
	}
	
	private void unloadScript(WaitingIndicatorDialogController waitDialogController)
	{
		final Window waitDialogWindow = waitDialogController.getWindow();
		final String pattern = String.valueOf(waitDialogWindow.getUserData());
		// the script is in memory, so finding it does not need a background task
		try
		{
			final Set<String> matchingScripts = new TreeSet<>(NetProScriptCache.getInstance().findCompiledScripts(pattern, Integer.MAX_VALUE, 0));
			// Remove unmanaged or already unloaded scripts
			for (final Iterator<String> it = matchingScripts.iterator(); it.hasNext();)
				if (NetProScriptCache.getInitializer().getManagedScript(it.next()) == null)
					it.remove();
				
			waitDialogController.onWaitEnd();
			
			if (matchingScripts.isEmpty())
			{
				makeNonModalUtilityAlert(INFORMATION, getMainWindow(), "scripts.unload.err.dialog.title", "scripts.unload.err.dialog.header.nonexistent",
						"scripts.unload.err.dialog.content.nonexistent", pattern).show();
				return;
			}
			
			final String result;
			if (matchingScripts.size() > 1)
			{
				final Optional<String> resultWrapper = showChoiceDialog(getMainWindow(), "scripts.unload.dialog.title", "scripts.unload.dialog.header.select", matchingScripts);
				if (!resultWrapper.isPresent())
					return;
				
				result = resultWrapper.get();
			}
			else
				result = matchingScripts.iterator().next();
			
			final WaitingIndicatorDialogController nextWaitDialogController = showWaitDialog(getMainWindow(), "generic.waitdlg.title", "scripts.unload.waitdlg.content", result);
			final Future<?> task = L2ThreadPool.submitLongRunning(() -> {
				final Map<Class<?>, RuntimeException> fqcn2Exception = new TreeMap<>((c1, c2) -> c1.getName().compareTo(c2.getName()));
				try
				{
					NetProScriptCache.getInitializer().unloadScript(result, fqcn2Exception);
				}
				catch (final IllegalArgumentException e)
				{
					nextWaitDialogController.onWaitEnd();
					
					makeNonModalUtilityAlert(INFORMATION, getMainWindow(), "scripts.unload.err.dialog.title", "scripts.unload.err.dialog.header.nonexistent",
							"scripts.unload.err.dialog.content.nonexistent", result);
					return;
				}
				
				Platform.runLater(() -> {
					nextWaitDialogController.onWaitEnd();
					
					if (fqcn2Exception.isEmpty())
					{
						makeNonModalUtilityAlert(INFORMATION, getMainWindow(), "scripts.unload.done.dialog.title", "scripts.unload.done.dialog.header", "scripts.unload.done.dialog.content", result)
								.show();
						return;
					}
					
					final Alert alert = makeNonModalUtilityAlert(WARNING, getMainWindow(), "scripts.unload.done.dialog.title", "scripts.unload.done.dialog.header",
							"scripts.unload.done.dialog.content.runtime", result);
					alert.getDialogPane().setExpandableContent(makeScriptExceptionMapExpandableContent(fqcn2Exception));
					alert.show();
				});
			});
			nextWaitDialogController.setCancelAction(() -> task.cancel(true));
		}
		catch (final MutableOperationInProgressException e)
		{
			waitDialogController.onWaitEnd();
			
			makeNonModalUtilityAlert(ERROR, getMainWindow(), "scripts.unload.err.dialog.title", "scripts.unload.err.dialog.header.singleop", "scripts.unload.err.dialog.content.singleop", pattern)
					.show();
		}
		catch (final InterruptedException e)
		{
			// application shutting down
			waitDialogController.onWaitEnd();
		}
	}
	
	/**
	 * Creates a user-friendly view of the given script exception map.
	 * 
	 * @param fqcn2Exception script exception map
	 * @return exception map representation
	 */
	public static final Node makeScriptExceptionMapExpandableContent(Map<Class<?>, RuntimeException> fqcn2Exception)
	{
		if (fqcn2Exception.isEmpty())
			return null;
		
		try
		{
			final FXMLLoader loader = new FXMLLoader(FXUtils.getFXML(ExceptionSummaryDialogController.class), UIStrings.getBundle());
			final SplitPane result = loader.load();
			final ExceptionSummaryDialogController controller = loader.getController();
			controller.setAllExceptions(fqcn2Exception, Class::getName, (c, t) -> StackTraceUtil.traceToString(StackTraceUtil.stripUntilFirstMethodCall(t, true, c.getClassLoader(),
					UnloadableScript.class, Collections.emptySet(), "onFirstLoad", "onReload", "onLoad", "onStateSave", "onUnload")));
			return result;
		}
		catch (final IOException e)
		{
			final Throwable t = StackTraceUtil.stripUntilClassContext(e, true, NetPro.class.getName());
			wrapException(t, "generic.err.internal.title", null, "generic.err.internal.header.fxml", null, null, Modality.WINDOW_MODAL).showAndWait();
			return null;
		}
	}
	
	@FXML
	private void loadAllScripts(ActionEvent event)
	{
		
	}
	
	// --------------------------
	// ========== SCRIPT MENU END
	// --------------------------
	
	// --------------------------
	// ========== HELP MENU BEGIN
	// --------------------------
	
	@FXML
	private void showAbout(ActionEvent event)
	{
		final Scene aboutDialog;
		try
		{
			final FXMLLoader loader = new FXMLLoader(FXUtils.getFXML(AboutDialogController.class), UIStrings.getBundle());
			aboutDialog = new Scene(loader.load());
		}
		catch (final IOException e)
		{
			LOG.error("", e);
			return;
		}
		
		final Stage about = new Stage(StageStyle.TRANSPARENT);
		about.initModality(Modality.APPLICATION_MODAL);
		about.initOwner(getMainWindow());
		about.setTitle(UIStrings.get("about.title"));
		about.setScene(aboutDialog);
		about.getIcons().addAll(FXUtils.getIconListFX());
		WindowTracker.getInstance().add(about);
		about.show();
	}
	
	@FXML
	private void showConfigExplainDialog(ActionEvent event)
	{
		
	}
	
	// ------------------------
	// ========== HELP MENU END
	// ------------------------
	
	/**
	 * Returns the scroll lock property.
	 * 
	 * @return scroll lock
	 */
	public BooleanProperty scrollLockProperty()
	{
		return _scrollLock.selectedProperty();
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
	
	/**
	 * Updates packet hiding configuration and other filters on all tabs that are associated with the given protocol.
	 * If {@code protocolVersion} is {@code null}, all tabs will be refreshed.
	 * 
	 * @param protocolVersion protocol version or {@code null}
	 */
	public void refreshFilters(IProtocolVersion protocolVersion)
	{
		for (final Tab tab : _tpConnections.getTabs())
		{
			final Object userData = tab.getUserData();
			if (!(userData instanceof PacketLogTabController))
				continue;
			
			final PacketLogTabController controller = (PacketLogTabController)userData;
			if (protocolVersion == null || protocolVersion.equals(controller.protocolProperty().get()))
				controller.applyFilters();
		}
	}
	
	/**
	 * Adds a new tab to the connection pane.
	 * 
	 * @param tab a tab
	 */
	public void addConnectionTab(Tab tab)
	{
		final ObservableList<Tab> tabs = _tpConnections.getTabs();
		tab.setOnClosed(e -> {
			final int threshold = tabs.contains(_tabConsole) ? 1 : 0;
			if (tabs.size() <= threshold)
				tabs.add(_tabIdle);
		});
		tabs.remove(_tabIdle);
		tabs.add(tab);
		_tpConnections.getSelectionModel().select(tab);
	}
}
