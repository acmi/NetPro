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
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Path;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.commons.lang3.tuple.Pair;

import eu.revengineer.simplejse.exception.DependencyResolutionException;
import eu.revengineer.simplejse.exception.MutableOperationInProgressException;
import eu.revengineer.simplejse.logging.BytesizeInterpreter;
import eu.revengineer.simplejse.logging.BytesizeInterpreter.BytesizeUnit;
import eu.revengineer.simplejse.type.UnloadableScript;

import net.l2emuproject.lang.L2System;
import net.l2emuproject.lang.L2TextBuilder;
import net.l2emuproject.lang.management.ShutdownManager;
import net.l2emuproject.lang.management.TerminationStatus;
import net.l2emuproject.network.mmocore.MMOBuffer;
import net.l2emuproject.network.protocol.IProtocolVersion;
import net.l2emuproject.network.protocol.ProtocolVersionManager;
import net.l2emuproject.proxy.NetPro;
import net.l2emuproject.proxy.StartupOption;
import net.l2emuproject.proxy.io.IOConstants;
import net.l2emuproject.proxy.io.definitions.VersionnedPacketTable;
import net.l2emuproject.proxy.io.exception.DamagedFileException;
import net.l2emuproject.proxy.io.exception.EmptyPacketLogException;
import net.l2emuproject.proxy.io.exception.FilesizeMeasureException;
import net.l2emuproject.proxy.io.exception.IncompletePacketLogFileException;
import net.l2emuproject.proxy.io.exception.InsufficientlyLargeFileException;
import net.l2emuproject.proxy.io.exception.TruncatedPacketLogFileException;
import net.l2emuproject.proxy.io.exception.UnknownFileTypeException;
import net.l2emuproject.proxy.io.packetlog.HistoricalLogIOThread.CaptureController;
import net.l2emuproject.proxy.io.packetlog.LogFileHeader;
import net.l2emuproject.proxy.io.packetlog.PacketLogFileUtils;
import net.l2emuproject.proxy.io.packetlog.l2ph.L2PhLogFileHeader;
import net.l2emuproject.proxy.io.packetlog.l2ph.L2PhLogFileUtils;
import net.l2emuproject.proxy.io.packetlog.ps.PSLogFileHeader;
import net.l2emuproject.proxy.io.packetlog.ps.PSLogPartHeader;
import net.l2emuproject.proxy.io.packetlog.ps.PSPacketLogUtils;
import net.l2emuproject.proxy.network.EndpointType;
import net.l2emuproject.proxy.network.Proxy;
import net.l2emuproject.proxy.network.ServiceType;
import net.l2emuproject.proxy.network.listener.ConnectionListener;
import net.l2emuproject.proxy.network.listener.PacketListener;
import net.l2emuproject.proxy.network.meta.IPacketTemplate;
import net.l2emuproject.proxy.network.meta.PacketPayloadEnumerator;
import net.l2emuproject.proxy.network.meta.RandomAccessMMOBuffer;
import net.l2emuproject.proxy.network.meta.SimplePpeProvider;
import net.l2emuproject.proxy.network.meta.UserDefinedProtocolVersion;
import net.l2emuproject.proxy.network.meta.container.OpcodeOwnerSet;
import net.l2emuproject.proxy.network.meta.exception.InvalidPacketOpcodeSchemeException;
import net.l2emuproject.proxy.network.meta.exception.PartialPayloadEnumerationException;
import net.l2emuproject.proxy.script.NetProScriptCache;
import net.l2emuproject.proxy.setup.IPAliasManager;
import net.l2emuproject.proxy.state.entity.cache.EntityInfoCache;
import net.l2emuproject.proxy.state.entity.context.ServerSocketID;
import net.l2emuproject.proxy.ui.ReceivedPacket;
import net.l2emuproject.proxy.ui.i18n.BytesizeFormat;
import net.l2emuproject.proxy.ui.i18n.UIStrings;
import net.l2emuproject.proxy.ui.javafx.ExceptionAlert;
import net.l2emuproject.proxy.ui.javafx.FXUtils;
import net.l2emuproject.proxy.ui.javafx.WindowTracker;
import net.l2emuproject.proxy.ui.javafx.error.view.ExceptionSummaryDialogController;
import net.l2emuproject.proxy.ui.javafx.io.view.L2PhPacketLogLoadOptionController;
import net.l2emuproject.proxy.ui.javafx.io.view.PacketLogLoadOptionController;
import net.l2emuproject.proxy.ui.javafx.io.view.SamuraiPacketLogLoadOptionController;
import net.l2emuproject.proxy.ui.javafx.packet.IPacketHidingConfig;
import net.l2emuproject.proxy.ui.javafx.packet.PacketLogEntry;
import net.l2emuproject.proxy.ui.javafx.packet.view.PacketBuilderDialogController;
import net.l2emuproject.proxy.ui.javafx.packet.view.PacketDisplayConfigDialogController;
import net.l2emuproject.proxy.ui.javafx.packet.view.PacketLogTabController;
import net.l2emuproject.proxy.ui.javafx.packet.view.PacketLogTabUserData;
import net.l2emuproject.util.HexUtil;
import net.l2emuproject.util.StackTraceUtil;
import net.l2emuproject.util.concurrent.L2ThreadPool;
import net.l2emuproject.util.logging.L2Logger;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.IntegerBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValueBase;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
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
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
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
public class MainWindowController implements IOConstants, ConnectionListener, PacketListener, CaptureController
{
	static final L2Logger LOG = L2Logger.getLogger(MainWindowController.class);
	
	private File _lastOpenDirectory = IOConstants.LOG_DIRECTORY.toFile();
	
	@FXML
	private MenuItem _miSave;
	
	@FXML
	private TabPane _tpConnections;
	
	@FXML
	private Tab _tabConsole;
	
	@FXML
	private TextArea _taConsole;
	
	@FXML
	private CheckMenuItem _cmiConsoleWrap;
	
	@FXML
	private CheckMenuItem _cmiConsoleScroll;
	
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
	private CheckMenuItem _showLogConsole;
	
	@FXML
	private Menu _mPacketDisplay;
	
	@FXML
	private Menu _mScripts;
	
	private final Map<IProtocolVersion, Stage> _openPacketHidingConfigWindows;
	
	private final MutableInt _connectionIdentifier;
	
	private final Map<Proxy, Boolean> _sessionCapture;
	private volatile boolean _globalCaptureDisable;
	
	private final ObservableList<Pair<Integer, Proxy>> _injectableConnections;
	
	private Stage _packetInjectDialog;
	
	/** Constructs this controller. */
	public MainWindowController()
	{
		_openPacketHidingConfigWindows = new HashMap<>();
		_connectionIdentifier = new MutableInt(0);
		
		_sessionCapture = new ConcurrentHashMap<>();
		
		_injectableConnections = FXCollections.observableArrayList(new LinkedList<>());
	}
	
	@FXML
	private void initialize()
	{
		_cbCaptureSession.setVisible(false);
		_labProtocol.setVisible(false);
		_tbPacketHidingConfig.setVisible(false);
		
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
			if (ctrl == null || !(ctrl instanceof PacketLogTabUserData))
			{
				_miSave.disableProperty().unbind();
				_miSave.setDisable(true);
				
				_cbCaptureSession.setVisible(false);
				_labProtocol.setVisible(false);
				_tbPacketHidingConfig.setVisible(false);
				return;
			}
			
			final PacketLogTabUserData ud = (PacketLogTabUserData)ctrl;
			final PacketLogTabController controller = ud.getController();
			
			_miSave.disableProperty().bind(Bindings.not(controller.hasMemoryPackets()));

			_cbCaptureSession.setSelected(ud.isCaptureDisabled());
			_cbCaptureSession.setVisible(ud.isOnline());
			_labProtocol.textProperty().bind(Bindings.convert(controller.protocolProperty()));
			_labProtocol.setVisible(true);
			final IntegerBinding protocolVersionProperty = Bindings.createIntegerBinding(() -> controller.protocolProperty().get().getVersion(), controller.protocolProperty());
			_tbPacketHidingConfig.textProperty().bind(UIStrings.getEx("main.pdc", protocolVersionProperty));
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
		
		_taConsole.wrapTextProperty().bind(_cmiConsoleWrap.selectedProperty());
		_taConsole.styleProperty().bind(Bindings.concat("-fx-font-size:", _sConsoleFontSize.valueProperty(), "; -fx-font-family: Consolas, monospace"));
		clearConsole(null);
		
		_mScripts.disableProperty().bind(new ObservableValueBase<Boolean>(){
			@Override
			public Boolean getValue()
			{
				return StartupOption.DISABLE_SCRIPTS.isSet() || NetProScriptCache.getInstance().isCompilerUnavailable();
			}
		});
		
		rebuildProtocolMenu();
		
		_globalCaptureDisable = _cbCaptureGlobal.isSelected();
		_cbCaptureGlobal.selectedProperty().addListener((obs, old, neu) -> _globalCaptureDisable = neu);
		_cbCaptureSession.selectedProperty().addListener((obs, old, neu) -> {
			final Tab tab = _tpConnections.getSelectionModel().getSelectedItem();
			if (tab == null)
				return;
			
			final Object ud = tab.getUserData();
			if (!(ud instanceof PacketLogTabUserData))
				return;
			
			final PacketLogTabUserData userData = (PacketLogTabUserData)ud;
			if (userData.getClient() == null)
				return;
			
			userData.setCaptureDisabled(neu);
			_sessionCapture.put(userData.getClient(), neu);
		});
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
		return controller instanceof PacketLogTabUserData ? ((PacketLogTabUserData)controller).getController() : null;
	}
	
	private void rebuildProtocolMenu()
	{
		for (final Window wnd : _openPacketHidingConfigWindows.values())
			wnd.hide();
		_openPacketHidingConfigWindows.clear();
		
		_mPacketDisplay.getItems().clear();
		_mPacketDisplay.setDisable(StartupOption.DISABLE_DEFS.isSet());
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
		if (currentTabController != null && protocol.equals(currentTabController.protocolProperty().get()))
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
	private void showOpenHistoricalPacketLog(ActionEvent event)
	{
		final FileChooser fc = new FileChooser();
		fc.setTitle(UIStrings.get("menu.file.open.filedlg.title"));
		// @formatter:off
		fc.getExtensionFilters().addAll(
			new ExtensionFilter(UIStrings.get("menu.file.open.filedlg.filter.all"), "*." + HISTORICAL_LOG_EXTENSION, "*." + LOG_EXTENSION, "*.pLog", "*.rawLog", "*.psl"),
			new ExtensionFilter(UIStrings.get("menu.file.open.filedlg.filter.all.np"), "*." + HISTORICAL_LOG_EXTENSION, "*." + LOG_EXTENSION),
			new ExtensionFilter(UIStrings.get("menu.file.open.filedlg.filter.np.npl"), "*." + HISTORICAL_LOG_EXTENSION),
			new ExtensionFilter(UIStrings.get("menu.file.open.filedlg.filter.np.plog"), "*." + LOG_EXTENSION),
			new ExtensionFilter(UIStrings.get("menu.file.open.filedlg.filter.all.l2ph"), "*.pLog", "*.rawLog"),
			new ExtensionFilter(UIStrings.get("menu.file.open.filedlg.filter.l2ph.pLog"), "*.pLog"),
			new ExtensionFilter(UIStrings.get("menu.file.open.filedlg.filter.l2ph.rawLog"), "*.rawLog"),
			new ExtensionFilter(UIStrings.get("menu.file.open.filedlg.filter.ps.psl"), "*.psl"),
			new ExtensionFilter(UIStrings.get("generic.filedlg.allfiles"), "*.*")
		);
		// @formatter:on
		fc.setInitialDirectory(_lastOpenDirectory);
		
		final List<File> selectedFiles = fc.showOpenMultipleDialog(getMainWindow());
		if (selectedFiles == null || selectedFiles.isEmpty())
			return;
		
		_lastOpenDirectory = selectedFiles.iterator().next().getParentFile();
		
		final WaitingIndicatorDialogController waitDialog = showWaitDialog(getMainWindow(), "generic.waitdlg.title",
				selectedFiles.size() > 1 ? "open.waitdlg.header.multiple" : "open.waitdlg.header",
				selectedFiles.size() > 1 ? String.valueOf(selectedFiles.size()) : selectedFiles.iterator().next().getName());
		final Future<?> preprocessTask = L2ThreadPool.submitLongRunning(() -> {
			final List<Object> validLogFiles = new ArrayList<>(selectedFiles.size());
			for (final File selectedFile : selectedFiles)
			{
				final Path packetLogFile = selectedFile.toPath();
				final String filename = packetLogFile.getFileName().toString();
				
				// to simplify error reporting, the rules are simple:
				// 3rd party logs will be attempted to open BY EXTENSION ONLY
				// Everything else will be attempted as NP logs, and if that fails, legacy NP logs
				try
				{
					if (filename.endsWith(".pLog"))
					{
						validLogFiles.add(L2PhLogFileUtils.getMetadata(packetLogFile));
						continue;
					}
					else if (filename.endsWith(".rawLog"))
					{
						validLogFiles.add(L2PhLogFileUtils.getRawMetadata(packetLogFile));
						continue;
					}
				}
				catch (final IOException e)
				{
					final Throwable t = StackTraceUtil.stripUntilClassContext(e, true, MainWindowController.class.getName());
					Platform.runLater(
							() -> wrapException(t, "open.netpro.err.dialog.title.named", new Object[]
							{ filename }, "open.netpro.err.dialog.header.io", null, getMainWindow(), Modality.NONE).show());
					continue;
				}
				catch (final InsufficientlyLargeFileException e)
				{
					Platform.runLater(() -> makeNonModalUtilityAlert(WARNING, getMainWindow(), "open.netpro.err.dialog.title.named", new Object[] { filename },
							"open.netpro.err.dialog.header.toosmall", null, "open.netpro.err.dialog.content.toosmall", filename).show());
					continue;
				}
				catch (final UnknownFileTypeException e)
				{
					Platform.runLater(() -> makeNonModalUtilityAlert(WARNING, getMainWindow(), "open.netpro.err.dialog.title.named", new Object[] { filename },
							"open.netpro.err.dialog.header.wrongfile", null, "open.netpro.err.dialog.content.wrongfile", filename, HexUtil.bytesToHexString(e.getMagic8Bytes(), " ")).show());
					continue;
				}
				catch (final DamagedFileException e)
				{
					Platform.runLater(() -> makeNonModalUtilityAlert(ERROR, getMainWindow(), "open.netpro.err.dialog.title.named", new Object[] { filename }, "open.netpro.err.dialog.header.damaged",
							null, "open.netpro.err.dialog.content.damaged", filename).show());
					continue;
				}
				catch (final RuntimeException e)
				{
					final Throwable t = StackTraceUtil.stripUntilClassContext(e, true, MainWindowController.class.getName());
					Platform.runLater(
							() -> wrapException(t, "open.netpro.err.dialog.title.named", new Object[]
							{ filename }, "open.netpro.err.dialog.header.runtime", null, getMainWindow(), Modality.NONE).show());
					continue;
				}
				
				try
				{
					if (filename.endsWith(".psl"))
					{
						final PSLogFileHeader fullHeader = PSPacketLogUtils.getMetadata(packetLogFile);
						if (fullHeader != null)
							validLogFiles.add(fullHeader);
						continue;
					}
				}
				catch (final InterruptedException e)
				{
					// cancelled by user
					validLogFiles.clear();
					break;
				}
				catch (final IOException e)
				{
					final Throwable t = StackTraceUtil.stripUntilClassContext(e, true, MainWindowController.class.getName());
					Platform.runLater(
							() -> wrapException(t, "open.netpro.err.dialog.title.named", new Object[]
							{ filename }, "open.netpro.err.dialog.header.io", null, getMainWindow(), Modality.NONE).show());
					continue;
				}
				catch (final InsufficientlyLargeFileException e)
				{
					Platform.runLater(() -> makeNonModalUtilityAlert(WARNING, getMainWindow(), "open.netpro.err.dialog.title.named", new Object[] { filename },
							"open.netpro.err.dialog.header.toosmall", null, "open.netpro.err.dialog.content.toosmall", filename).show());
					continue;
				}
				catch (final TruncatedPacketLogFileException e)
				{
					Platform.runLater(() -> makeNonModalUtilityAlert(ERROR, getMainWindow(), "open.netpro.err.dialog.title.named", new Object[] { filename }, "open.netpro.err.dialog.header.truncated",
							null, "open.netpro.err.dialog.content.truncated", filename).show());
					continue;
				}
				catch (final DamagedFileException e)
				{
					Platform.runLater(() -> makeNonModalUtilityAlert(ERROR, getMainWindow(), "open.netpro.err.dialog.title.named", new Object[] { filename }, "open.netpro.err.dialog.header.damaged",
							null, "open.netpro.err.dialog.content.damaged", filename).show());
					continue;
				}
				catch (final RuntimeException e)
				{
					final Throwable t = StackTraceUtil.stripUntilClassContext(e, true, MainWindowController.class.getName());
					Platform.runLater(
							() -> wrapException(t, "open.netpro.err.dialog.title.named", new Object[]
							{ filename }, "open.netpro.err.dialog.header.runtime", null, getMainWindow(), Modality.NONE).show());
					continue;
				}
				
				try
				{
					// TODO: NetPro2 packet log
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
						final Object header = validLogFiles.get(i);
						if (header instanceof L2PhLogFileHeader)
						{
							final L2PhLogFileHeader validLogFile = (L2PhLogFileHeader)header;
							
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
									ProtocolVersionManager.getInstance().getProtocol(validLogFile.getProtocol(), service.isLogin(), validLogFile.getAltModes()), validLogFile.getFirstPacketArrivalTime());
							tab.setUserData(validLogFile);
							tabs[i] = tab;
							continue;
						}
						if (header instanceof PSLogFileHeader)
						{
							final PSLogFileHeader validLogFile = (PSLogFileHeader)header;
							final String approxSize, exactSize;
							final NumberFormat integerFormat = NumberFormat.getIntegerInstance(UIStrings.CURRENT_LOCALE);
							long filesize = 0;
							for (final PSLogPartHeader logPart : validLogFile.getParts())
							{
								if (logPart.getLogFileSize() == -1)
								{
									filesize = -1;
									break;
								}
								filesize += logPart.getLogFileSize();
							}
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
							
							final FXMLLoader loader = new FXMLLoader(FXUtils.getFXML(SamuraiPacketLogLoadOptionController.class), UIStrings.getBundle());
							final TitledPane tab = loader.load();
							final SamuraiPacketLogLoadOptionController controller = loader.getController();
							controller.setMainWindow(this);
							
							final PSLogPartHeader logPart = validLogFile.getParts().get(0);
							final ServiceType service = logPart.getServicePort() == 2106 ? ServiceType.LOGIN : ServiceType.GAME;
							final String filename = logPart.getLogFile().getFileName().toString();
							final Long cp = logPart.getSessionID() % logPart.getServicePort() == 0 ? logPart.getSessionID() / logPart.getServicePort() : null;
							controller.setPacketLog(filename, approxSize, exactSize, HexUtil.fillHex(logPart.getVersion(), 2),
									integerFormat.format(validLogFile.getParts().stream().mapToLong(PSLogPartHeader::getPackets).sum()),
									UIStrings.get(logPart.isMultipart() ? "load.infodlg.ps.details.structure.multipart" : "load.infodlg.ps.details.structure.solid"),
									logPart.getServerIP().getHostAddress() + ":" + logPart.getServicePort(),
									logPart.getClientIP().getHostAddress() + (cp != null ? (":" + cp) : ""), logPart.getProtocolName(), logPart.getComments(),
									UIStrings.get(logPart.isEnciphered() ? "load.infodlg.ps.details.stream.unprocessed" : "load.infodlg.ps.details.stream.preprocessed"),
									FXCollections.observableArrayList(VersionnedPacketTable.getInstance().getKnownProtocols(service)),
									ProtocolVersionManager.getInstance().getProtocol(logPart.getProtocol(), service.isLogin(), logPart.getAltModes()), logPart.isEnciphered(), logPart.isEnciphered());
							tab.setUserData(validLogFile);
							tabs[i] = tab;
							continue;
						}
						
						final LogFileHeader validLogFile = (LogFileHeader)header;
						
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
						controller.setPacketLog(filename, approxSize, exactSize, String.format(Locale.ROOT, "%02X", validLogFile.getVersion()), integerFormat.format(validLogFile.getPackets()),
								FXCollections.observableArrayList(VersionnedPacketTable.getInstance().getKnownProtocols(service)),
								ProtocolVersionManager.getInstance().getProtocol(validLogFile.getProtocol(), service.isLogin(), validLogFile.getAltModes()));
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
	private void reloadDefinitions(ActionEvent event)
	{
		L2ThreadPool.executeLongRunning(() -> {
			VersionnedPacketTable.getInstance().reloadConfig();
			Platform.runLater(() -> {
				rebuildProtocolMenu();
				
				final ObservableList<Tab> tabs = _tpConnections.getTabs();
				for (final Tab tab : tabs)
				{
					final Object ud = tab.getUserData();
					if (!(ud instanceof PacketLogTabUserData))
						continue;
					
					final PacketLogTabUserData userData = (PacketLogTabUserData)ud;
					final ObjectProperty<IProtocolVersion> pp = userData.getController().protocolProperty();
					final IProtocolVersion old = pp.getValue();
					final IProtocolVersion neu = ProtocolVersionManager.getInstance().getProtocol(old.getVersion(), ServiceType.valueOf(old).isLogin(), old.getAltModes());
					pp.set(neu);
					userData.getController().refreshSelectedPacketView();
				}
			});
		});
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
	// ========== VIEW MENU BEGIN
	// ----------------------------
	
	@FXML
	private void showPacketInjectDialog(ActionEvent event)
	{
		if (_packetInjectDialog != null)
		{
			_packetInjectDialog.show();
			_packetInjectDialog.toFront();
			return;
		}
		
		try
		{
			final FXMLLoader loader = new FXMLLoader(FXUtils.getFXML(PacketBuilderDialogController.class), UIStrings.getBundle());
			final Scene scene = new Scene(loader.load());
			loader.<PacketBuilderDialogController> getController().setClientConnections(_injectableConnections);
			
			_packetInjectDialog = new Stage(StageStyle.DECORATED);
			_packetInjectDialog.setTitle(UIStrings.get("packetbuilder.injectdlg.title"));
			_packetInjectDialog.setScene(scene);
			_packetInjectDialog.getIcons().addAll(FXUtils.getIconListFX());
			// _packetInjectDialog.setAlwaysOnTop(true);
			WindowTracker.getInstance().add(_packetInjectDialog);
			_packetInjectDialog.show();
		}
		catch (final IOException e)
		{
			final Throwable t = StackTraceUtil.stripUntilClassContext(e, true, PacketBuilderDialogController.class.getName());
			wrapException(t, "generic.err.internal.title", null, "generic.err.internal.header.fxml", null, getMainWindow(), Modality.WINDOW_MODAL).show();
			return;
		}
	}
	
	// ------------------------
	// ========== VIEW MENU END
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
							Platform.runLater(() -> {
								nextWaitDialogController.onWaitEnd();
								
								makeNonModalUtilityAlert(ERROR, getMainWindow(), "scripts.load.err.dialog.title", "scripts.load.err.dialog.header.singleop", "scripts.load.err.dialog.content.singleop",
										result).show();
							});
							return;
						}
						catch (final DependencyResolutionException e)
						{
							Platform.runLater(() -> {
								nextWaitDialogController.onWaitEnd();
								
								makeNonModalUtilityAlert(ERROR, getMainWindow(), "scripts.load.err.dialog.title", "scripts.load.err.dialog.header.apt", "scripts.load.err.dialog.content.apt",
										System.getProperty("java.home"));
							});
							return;
						}
						catch (final IOException e)
						{
							Platform.runLater(() -> {
								nextWaitDialogController.onWaitEnd();
								
								initNonModalUtilityDialog(new ExceptionAlert(e), getMainWindow(), "scripts.load.err.dialog.title", "scripts.load.err.dialog.header.runtime", null).show();
							});
							return;
						}
						catch (final IllegalArgumentException e)
						{
							Platform.runLater(() -> {
								nextWaitDialogController.onWaitEnd();
								
								makeNonModalUtilityAlert(INFORMATION, getMainWindow(), "scripts.load.err.dialog.title", "scripts.load.err.dialog.header.nonexistent",
										"scripts.load.err.dialog.content.nonexistent", result).show();
							});
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
				Platform.runLater(() -> {
					waitDialogController.onWaitEnd();
					
					makeNonModalUtilityAlert(ERROR, getMainWindow(), "scripts.load.err.dialog.title", "scripts.load.err.dialog.header.singleop", "scripts.load.err.dialog.content.singleop", pattern)
							.show();
				});
			}
			catch (final InterruptedException e)
			{
				// application shutting down
				Platform.runLater(waitDialogController::onWaitEnd);
			}
			catch (final IOException ex)
			{
				Platform.runLater(() -> wrapException(ex, "scripts.load.err.dialog.title", null, "scripts.load.err.dialog.header.runtime", null, getMainWindow(), Modality.NONE).show());
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
	 * Adds a new message to the console tab.
	 * 
	 * @param message log entry
	 */
	public void appendToConsole(String message)
	{
		final double left = _taConsole.getScrollLeft(), top = _taConsole.getScrollTop();
		_taConsole.appendText(message);
		_taConsole.appendText("\r\n");
		if (_cmiConsoleScroll.isSelected())
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
			if (!(userData instanceof PacketLogTabUserData))
				continue;
			
			final PacketLogTabController controller = ((PacketLogTabUserData)userData).getController();
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
		final PacketLogTabUserData userData = (PacketLogTabUserData)tab.getUserData();
		tab.setOnClosed(e -> {
			final int threshold = tabs.contains(_tabConsole) ? 1 : 0;
			if (tabs.size() <= threshold) {
				tabs.add(_tabIdle);
				_tpConnections.getSelectionModel().select(_tabIdle);
			}
			if (userData.getClient() != null)
				_sessionCapture.remove(userData.getClient());
			if (userData.getServer() != null)
				EntityInfoCache.removeSharedContext(new ServerSocketID(userData.getServer().getInetSocketAddress()));
		});
		tabs.remove(_tabIdle);
		tabs.add(tab);
		_tpConnections.getSelectionModel().select(tab);
		if (userData.getClient() != null)
			_sessionCapture.put(userData.getClient(), userData.isCaptureDisabled());
		if (userData.getServer() != null)
			EntityInfoCache.addSharedContext(new ServerSocketID(userData.getServer().getInetSocketAddress()));
	}
	
	@Override
	public void onClientPacket(Proxy sender, Proxy recipient, ByteBuffer packet, long time) throws RuntimeException
	{
		findTabAndAddPacket(sender, recipient, packet, time);
	}
	
	@Override
	public void onServerPacket(Proxy sender, Proxy recipient, ByteBuffer packet, long time) throws RuntimeException
	{
		findTabAndAddPacket(sender, recipient, packet, time);
	}
	
	private void findTabAndAddPacket(Proxy sender, Proxy recipient, ByteBuffer packet, long time)
	{
		final byte[] body = new byte[packet.clear().remaining()];
		packet.get(body);
		
		final ReceivedPacket result = new ReceivedPacket(ServiceType.valueOf(sender.getProtocol()), sender.getType(), body, time);
		final PacketLogEntry ple = new PacketLogEntry(result);
		
		Platform.runLater(() -> {
			final ObservableList<Tab> tabs = _tpConnections.getTabs();
			for (final Tab tab : tabs)
			{
				final Object ud = tab.getUserData();
				if (!(ud instanceof PacketLogTabUserData))
					continue;
				
				final PacketLogTabUserData userData = (PacketLogTabUserData)ud;
				if (userData.getClient() != sender && userData.getServer() != sender && userData.getClient() != recipient && userData.getServer() != recipient)
					continue;
				
				if (userData.charNameProperty() != null)
				{
					// set character name
					final String charName = getNewCharName(result, sender.getProtocol());
					if (charName != null)
						userData.charNameProperty().set(charName);
				}
				
				if (_cbCaptureGlobal.isSelected() || userData.isCaptureDisabled())
					return;
				
				ple.updateView(sender.getProtocol());
				userData.getController().addPackets(Collections.singleton(ple));
				break;
			}
		});
	}
	
	private String getNewCharName(ReceivedPacket packet, IProtocolVersion protocol)
	{
		if (!packet.getEndpoint().isServer())
			return null;
		
		final IPacketTemplate template = VersionnedPacketTable.getInstance().getTemplate(protocol, packet.getEndpoint(), packet.getBody());
		if (template == null || template.getName() == null)
			return null;
		switch (template.getName())
		{
		case "CharacterSelectionInfo":
			return "[Lobby]";
		case "CharacterSelected":
			final PacketPayloadEnumerator ppe = SimplePpeProvider.getPacketPayloadEnumerator();
			if (ppe == null)
				return "[Unknown]";
			
			try
			{
				RandomAccessMMOBuffer enumerator = null;
				try
				{
					enumerator = ppe.enumeratePacketPayload(protocol, new MMOBuffer().setByteBuffer(ByteBuffer.wrap(packet.getBody()).order(ByteOrder.LITTLE_ENDIAN)), () -> packet.getEndpoint());
				}
				catch (final InvalidPacketOpcodeSchemeException e)
				{
					LOG.error("This cannot happen", e);
					return "[Unknown]";
				}
				catch (final PartialPayloadEnumerationException e)
				{
					// ignore this due to reasons
					enumerator = e.getBuffer();
				}
				return enumerator.readFirstString("__AUTO_EXTRACT_CHAR_NAME");
			}
			catch (RuntimeException e)
			{
				return "[Unknown]";
			}
		default:
			return null;
		}
	}
	
	@Override
	public void onProtocolVersion(Proxy affected, IProtocolVersion version) throws RuntimeException
	{
		Platform.runLater(() -> {
			final ObservableList<Tab> tabs = _tpConnections.getTabs();
			for (final Tab tab : tabs)
			{
				final Object ud = tab.getUserData();
				if (!(ud instanceof PacketLogTabUserData))
					continue;
				
				final PacketLogTabUserData userData = (PacketLogTabUserData)ud;
				if (affected != userData.getClient() && affected != userData.getServer())
					continue;
				
				userData.getController().protocolProperty().set(version);
				break;
			}
		});
	}
	
	@Override
	public void onClientConnection(Proxy client)
	{
		// ignore
	}
	
	@Override
	public void onServerConnection(Proxy server)
	{
		Platform.runLater(() -> {
			final int cid = _connectionIdentifier.incrementAndGet();
			// for clarity, auth injections are no longer supported in this version
			ServiceType serviceType = ServiceType.valueOf(server.getProtocol());
			if (serviceType == ServiceType.GAME)
				_injectableConnections.add(Pair.of(cid, server.getClient()));
			
			StringProperty charNameProperty = null;
			
			final Tab tab;
			final PacketLogTabController controller;
			try
			{
				final FXMLLoader loader = new FXMLLoader(FXUtils.getFXML(PacketLogTabController.class), UIStrings.getBundle());
				tab = new Tab(IPAliasManager.toUserFriendlyString(
						UIStrings.get("packettab.title", cid, IPAliasManager.toUserFriendlyString(server.getClient().getHostAddress()),
								IPAliasManager.toUserFriendlyString(server.getHostAddress()))),
						loader.load());
				if (serviceType == ServiceType.GAME)
				{
					charNameProperty = new SimpleStringProperty("[Lobby]");
					tab.textProperty().bind(charNameProperty.concat("@").concat(IPAliasManager.toUserFriendlyString(server.getHostAddress())));
				}
				controller = loader.getController();
			}
			catch (final IOException e)
			{
				final Throwable t = StackTraceUtil.stripUntilClassContext(e, true, PacketLogLoadOptionController.class.getName());
				wrapException(t, "generic.err.internal.title", null, "generic.err.internal.header.fxml", null, getMainWindow(), Modality.WINDOW_MODAL).show();
				return;
			}
			
			controller.protocolProperty().set(server.getProtocol());
			controller.setEntityCacheContext(new ServerSocketID(server.getInetSocketAddress()));
			controller.setOnProtocolPacketHidingConfigurationChange(this::refreshFilters);
			
			final Canvas icon = new Canvas(10, 10);
			final GraphicsContext gc = icon.getGraphicsContext2D();
			gc.setFill(new RadialGradient(0, 0, 0.5, 0.5, 0.7, true, CycleMethod.NO_CYCLE, new Stop(0, Color.GREEN), new Stop(1, Color.TRANSPARENT)));
			gc.fillOval(0, 0, 10, 10);
			tab.setGraphic(icon);
			
			final PacketLogTabUserData userData = new PacketLogTabUserData(controller, charNameProperty);
			userData.setServer(server);
			userData.setClient(server.getClient());
			userData.setOnline(true);
			tab.setUserData(userData);
			addConnectionTab(tab);
		});
	}
	
	@Override
	public void onDisconnection(Proxy client, Proxy server)
	{
		Platform.runLater(() -> {
			for (final Iterator<Pair<Integer, Proxy>> it = _injectableConnections.iterator(); it.hasNext();)
			{
				if (it.next().getRight() == client)
				{
					it.remove();
					break;
				}
			}
			final ObservableList<Tab> tabs = _tpConnections.getTabs();
			for (final Tab tab : tabs)
			{
				final Object ud = tab.getUserData();
				if (!(ud instanceof PacketLogTabUserData))
					continue;
				
				final PacketLogTabUserData userData = (PacketLogTabUserData)ud;
				if (client != userData.getClient() && server != userData.getServer())
					continue;
				
				userData.setOnline(false);
				
				final Canvas icon = new Canvas(10, 10);
				final GraphicsContext gc = icon.getGraphicsContext2D();
				gc.setFill(new RadialGradient(0, 0, 0.5, 0.5, 0.7, true, CycleMethod.NO_CYCLE, new Stop(0, Color.BLACK), new Stop(1, Color.TRANSPARENT)));
				gc.fillOval(0, 0, 10, 10);
				tab.setGraphic(icon);
				
				if (_tpConnections.getSelectionModel().getSelectedItem() == tab)
					_cbCaptureSession.setVisible(false);
				break;
			}
		});
	}
	
	@Override
	public boolean isCaptureDisabledFor(Proxy client)
	{
		return _globalCaptureDisable || _sessionCapture.getOrDefault(client, Boolean.FALSE).booleanValue();
	}
}
