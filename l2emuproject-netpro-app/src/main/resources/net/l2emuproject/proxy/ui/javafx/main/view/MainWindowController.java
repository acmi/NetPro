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

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.Future;
import java.util.function.Consumer;

import org.apache.commons.lang3.ArrayUtils;

import eu.revengineer.simplejse.exception.DependencyResolutionException;
import eu.revengineer.simplejse.exception.MutableOperationInProgressException;
import eu.revengineer.simplejse.logging.BytesizeInterpreter;
import eu.revengineer.simplejse.logging.BytesizeInterpreter.BytesizeUnit;
import eu.revengineer.simplejse.type.UnloadableScript;

import net.l2emuproject.lang.L2TextBuilder;
import net.l2emuproject.lang.management.ShutdownManager;
import net.l2emuproject.lang.management.TerminationStatus;
import net.l2emuproject.network.protocol.ProtocolVersionManager;
import net.l2emuproject.proxy.io.LogFileHeader;
import net.l2emuproject.proxy.io.PacketLogFileUtils;
import net.l2emuproject.proxy.io.exception.DamagedPacketLogFileException;
import net.l2emuproject.proxy.io.exception.EmptyPacketLogException;
import net.l2emuproject.proxy.io.exception.FilesizeMeasureException;
import net.l2emuproject.proxy.io.exception.IncompletePacketLogFileException;
import net.l2emuproject.proxy.io.exception.InsufficientlyLargeLogFileException;
import net.l2emuproject.proxy.io.exception.TruncatedPacketLogFileException;
import net.l2emuproject.proxy.io.exception.UnknownFileTypeException;
import net.l2emuproject.proxy.network.ServiceType;
import net.l2emuproject.proxy.script.NetProScriptCache;
import net.l2emuproject.proxy.ui.i18n.BytesizeFormat;
import net.l2emuproject.proxy.ui.i18n.UIStrings;
import net.l2emuproject.proxy.ui.javafx.ExceptionAlert;
import net.l2emuproject.proxy.ui.javafx.FXUtils;
import net.l2emuproject.proxy.ui.javafx.WindowTracker;
import net.l2emuproject.proxy.ui.javafx.packet.view.PacketLogLoadOptionController;
import net.l2emuproject.proxy.ui.savormix.io.VersionnedPacketTable;
import net.l2emuproject.proxy.ui.savormix.io.base.IOConstants;
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
import javafx.scene.control.TitledPane;
import javafx.scene.control.ToggleButton;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import javafx.util.Duration;

/**
 * @author _dev_
 */
public class MainWindowController implements Initializable, IOConstants
{
	static final L2Logger LOG = L2Logger.getLogger(MainWindowController.class);
	
	private File lastOpenDirectory = IOConstants.LOG_DIRECTORY.toFile();
	
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
	private CheckMenuItem _scrollLock;
	
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
	private MenuItem _configExplainer;
	
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
	
	// --------------------------
	// ========== FILE MENU BEGIN
	// --------------------------
	
	@FXML
	void showOpenLogNP(ActionEvent event)
	{
		final FileChooser fc = new FileChooser();
		fc.setTitle(UIStrings.get("open.netpro.fileselect.title"));
		fc.getExtensionFilters().addAll(new ExtensionFilter(UIStrings.get("open.netpro.fileselect.description"), "*.plog"), new ExtensionFilter(UIStrings.get("generic.filedlg.allfiles"), "*.*"));
		fc.setInitialDirectory(lastOpenDirectory);
		
		final List<File> selectedFiles = fc.showOpenMultipleDialog(getMainWindow());
		if (selectedFiles == null || selectedFiles.isEmpty())
			return;
		
		lastOpenDirectory = selectedFiles.iterator().next().getParentFile();
		
		final WaitingIndicatorDialogController waitDialog = showWaitDialog("generic.waitdlg.title", selectedFiles.size() > 1 ? "open.netpro.waitdlg.header.multiple" : "open.netpro.waitdlg.header",
				selectedFiles.size() > 1 ? String.valueOf(selectedFiles.size()) : selectedFiles.iterator().next().getName());
		final Future<?> preprocessTask = L2ThreadPool.submitLongRunning(() ->
		{
			final List<LogFileHeader> validLogFiles = new ArrayList<>(selectedFiles.size());
			for (final File selectedFile : selectedFiles)
			{
				final Path packetLogFile = selectedFile.toPath();
				final String filename = packetLogFile.getFileName().toString();
				try
				{
					validLogFiles.add(PacketLogFileUtils.getMetadata(packetLogFile));
				}
				catch (InterruptedException e)
				{
					// cancelled by user
					waitDialog.onWaitEnd();
					return;
				}
				catch (FilesizeMeasureException | IOException e)
				{
					final Throwable t = StackTraceUtil.stripRunnable(e);
					Platform.runLater(() -> initNonModalUtilityDialog(new ExceptionAlert(t), getMainWindow(), "open.netpro.err.dialog.title.named", new Object[] { filename },
							"open.netpro.err.dialog.header.io", ArrayUtils.EMPTY_OBJECT_ARRAY, null).show());
				}
				catch (InsufficientlyLargeLogFileException e)
				{
					Platform.runLater(() -> makeNonModalUtilityAlert(WARNING, getMainWindow(), "open.netpro.err.dialog.title.named", new Object[] { filename },
							"open.netpro.err.dialog.header.toosmall", null, "open.netpro.err.dialog.content.toosmall", filename).show());
				}
				catch (IncompletePacketLogFileException e)
				{
					// TODO: if not currently locked (being written), offer to repair automatically
					// otherwise inform that it represents a currently active connection
					
					// for now just inform and skip
					Platform.runLater(() -> makeNonModalUtilityAlert(ERROR, getMainWindow(), "open.netpro.err.dialog.title.named", new Object[] { filename },
							"open.netpro.err.dialog.header.incomplete", null, "open.netpro.err.dialog.content.incomplete", filename).show());
				}
				catch (UnknownFileTypeException e)
				{
					Platform.runLater(() -> makeNonModalUtilityAlert(WARNING, getMainWindow(), "open.netpro.err.dialog.title.named", new Object[] { filename },
							"open.netpro.err.dialog.header.wrongfile", null, "open.netpro.err.dialog.content.wrongfile", filename, HexUtil.bytesToHexString(e.getMagic8Bytes(), " ")).show());
				}
				catch (TruncatedPacketLogFileException e)
				{
					Platform.runLater(() -> makeNonModalUtilityAlert(ERROR, getMainWindow(), "open.netpro.err.dialog.title.named", new Object[] { filename }, "open.netpro.err.dialog.header.truncated",
							null, "open.netpro.err.dialog.content.truncated", filename).show());
				}
				catch (EmptyPacketLogException e)
				{
					Platform.runLater(() -> makeNonModalUtilityAlert(WARNING, getMainWindow(), "open.netpro.err.dialog.title.named", new Object[] { filename }, "open.netpro.err.dialog.header.empty",
							null, "open.netpro.err.dialog.content.empty", filename).show());
				}
				catch (DamagedPacketLogFileException e)
				{
					Platform.runLater(() -> makeNonModalUtilityAlert(ERROR, getMainWindow(), "open.netpro.err.dialog.title.named", new Object[] { filename }, "open.netpro.err.dialog.header.damaged",
							null, "open.netpro.err.dialog.content.damaged", filename).show());
				}
				catch (RuntimeException e)
				{
					final Throwable t = StackTraceUtil.stripRunnable(e);
					Platform.runLater(() -> initNonModalUtilityDialog(new ExceptionAlert(t), getMainWindow(), "open.netpro.err.dialog.title.named", new Object[] { filename },
							"open.netpro.err.dialog.header.runtime", null, null).show());
				}
			}
			
			Platform.runLater(() ->
			{
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
				catch (IOException e)
				{
					initNonModalUtilityDialog(new ExceptionAlert(StackTraceUtil.stripUntilClassContext(e, true, MainWindowController.class.getName())), getMainWindow(),
							"ui.fxml.err.dialog.missing.title", "ui.fxml.err.dialog.missing.header", null).show();
					return;
				}
				
				final Accordion tabPane = new Accordion(tabs);
				tabPane.setExpandedPane(tabs[0]);
				
				// no owner here - any invalid load options should only block the confirm window, but not the main window
				confirmStage.setTitle(UIStrings.get("load.infodlg.title"));
				confirmStage.setScene(new Scene(tabPane));
				confirmStage.getIcons().addAll(FXUtils.getIconListFX());
				WindowTracker.getInstance().add(confirmStage);
				confirmStage.show();
			});
		});
		waitDialog.setCancelAction(() -> preprocessTask.cancel(true));
	}
	
	@FXML
	void showRepairLogNP(ActionEvent event)
	{
		final DirectoryChooser dc = new DirectoryChooser();
		dc.setTitle(UIStrings.get("repair.netpro.dirselect.title"));
		
		final File selectedDir = dc.showDialog(getMainWindow());
		if (selectedDir == null)
			return;
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
		final WaitingIndicatorDialogController waitDialog = showWaitDialog("generic.waitdlg.title", "scripts.load.waitdlg.content.index", pattern);
		waitDialog.getWindow().setUserData(pattern);
		
		action.accept(waitDialog);
	}
	
	private void loadScript(WaitingIndicatorDialogController waitDialogController)
	{
		final Window waitDialogWindow = waitDialogController.getWindow();
		final String pattern = String.valueOf(waitDialogWindow.getUserData());
		
		final Future<?> task = L2ThreadPool.submitLongRunning(() ->
		{
			try
			{
				final Set<String> matchingScripts = new TreeSet<>(NetProScriptCache.getInstance().findIndexedScripts(pattern, Integer.MAX_VALUE, 0));
				
				Platform.runLater(() ->
				{
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
					
					final WaitingIndicatorDialogController nextWaitDialogController = showWaitDialog("generic.waitdlg.title", "scripts.load.waitdlg.content.compile", result);
					final Future<?> nextTask = L2ThreadPool.submitLongRunning(() ->
					{
						final Map<Class<?>, RuntimeException> fqcn2Exception = new TreeMap<>((c1, c2) -> c1.getName().compareTo(c2.getName()));
						try
						{
							NetProScriptCache.getInstance().compileSingleScript(result, fqcn2Exception);
						}
						catch (MutableOperationInProgressException e)
						{
							nextWaitDialogController.onWaitEnd();
							
							makeNonModalUtilityAlert(ERROR, getMainWindow(), "scripts.load.err.dialog.title", "scripts.load.err.dialog.header.singleop", "scripts.load.err.dialog.content.singleop",
									result).show();
							return;
						}
						catch (DependencyResolutionException e)
						{
							nextWaitDialogController.onWaitEnd();
							
							makeNonModalUtilityAlert(ERROR, getMainWindow(), "scripts.load.err.dialog.title", "scripts.load.err.dialog.header.apt", "scripts.load.err.dialog.content.apt",
									System.getProperty("java.home"));
							return;
						}
						catch (IOException e)
						{
							nextWaitDialogController.onWaitEnd();
							
							initNonModalUtilityDialog(new ExceptionAlert(e), getMainWindow(), "scripts.load.err.dialog.title", "scripts.load.err.dialog.header.runtime", null).show();
							return;
						}
						catch (IllegalArgumentException e)
						{
							nextWaitDialogController.onWaitEnd();
							
							makeNonModalUtilityAlert(INFORMATION, getMainWindow(), "scripts.load.err.dialog.title", "scripts.load.err.dialog.header.nonexistent",
									"scripts.load.err.dialog.content.nonexistent", result).show();
							return;
						}
						
						Platform.runLater(() ->
						{
							nextWaitDialogController.onWaitEnd();
							
							if (fqcn2Exception.isEmpty())
							{
								makeNonModalUtilityAlert(INFORMATION, getMainWindow(), "scripts.load.done.dialog.title", "scripts.load.done.dialog.header", "scripts.load.done.dialog.content", result)
										.show();
								return;
							}
							
							final Alert alert = makeNonModalUtilityAlert(WARNING, getMainWindow(), "scripts.load.done.dialog.title", "scripts.load.done.dialog.header",
									"scripts.load.done.dialog.content.runtime", result);
							alert.getDialogPane().setExpandableContent(makeScriptExceptionMapExpandabeContent(fqcn2Exception));
							alert.show();
						});
					});
					nextWaitDialogController.setCancelAction(() -> nextTask.cancel(true));
				});
			}
			catch (MutableOperationInProgressException e)
			{
				waitDialogController.onWaitEnd();
				
				makeNonModalUtilityAlert(ERROR, getMainWindow(), "scripts.load.err.dialog.title", "scripts.load.err.dialog.header.singleop", "scripts.load.err.dialog.content.singleop", pattern)
						.show();
			}
			catch (InterruptedException e)
			{
				// application shutting down
				waitDialogController.onWaitEnd();
			}
			catch (IOException ex)
			{
				initNonModalUtilityDialog(new ExceptionAlert(ex), getMainWindow(), "scripts.load.err.dialog.title", "scripts.load.err.dialog.header.runtime", null).show();
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
			
			final WaitingIndicatorDialogController nextWaitDialogController = showWaitDialog("generic.waitdlg.title", "scripts.unload.waitdlg.content", result);
			final Future<?> task = L2ThreadPool.submitLongRunning(() ->
			{
				final Map<Class<?>, RuntimeException> fqcn2Exception = new TreeMap<>((c1, c2) -> c1.getName().compareTo(c2.getName()));
				try
				{
					NetProScriptCache.getInitializer().unloadScript(result, fqcn2Exception);
				}
				catch (IllegalArgumentException e)
				{
					nextWaitDialogController.onWaitEnd();
					
					makeNonModalUtilityAlert(INFORMATION, getMainWindow(), "scripts.unload.err.dialog.title", "scripts.unload.err.dialog.header.nonexistent",
							"scripts.unload.err.dialog.content.nonexistent", result);
					return;
				}
				
				Platform.runLater(() ->
				{
					nextWaitDialogController.onWaitEnd();
					
					if (fqcn2Exception.isEmpty())
					{
						makeNonModalUtilityAlert(INFORMATION, getMainWindow(), "scripts.unload.done.dialog.title", "scripts.unload.done.dialog.header", "scripts.unload.done.dialog.content", result)
								.show();
						return;
					}
					
					final Alert alert = makeNonModalUtilityAlert(WARNING, getMainWindow(), "scripts.unload.done.dialog.title", "scripts.unload.done.dialog.header",
							"scripts.unload.done.dialog.content.runtime", result);
					alert.getDialogPane().setExpandableContent(makeScriptExceptionMapExpandabeContent(fqcn2Exception));
					alert.show();
				});
			});
			nextWaitDialogController.setCancelAction(() -> task.cancel(true));
		}
		catch (MutableOperationInProgressException e)
		{
			waitDialogController.onWaitEnd();
			
			makeNonModalUtilityAlert(ERROR, getMainWindow(), "scripts.unload.err.dialog.title", "scripts.unload.err.dialog.header.singleop", "scripts.unload.err.dialog.content.singleop", pattern)
					.show();
		}
		catch (InterruptedException e)
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
	public static final Node makeScriptExceptionMapExpandabeContent(Map<Class<?>, RuntimeException> fqcn2Exception)
	{
		if (fqcn2Exception.isEmpty())
			return null;
		
		final Accordion accordion = new Accordion();
		accordion.setMinWidth(600);
		accordion.setMinHeight(400);
		for (final Entry<Class<?>, RuntimeException> e : fqcn2Exception.entrySet())
		{
			final Class<?> scriptClass = e.getKey();
			final TextArea taStackTrace = new TextArea(StackTraceUtil.traceToString(StackTraceUtil.stripUntilFirstMethodCall(e.getValue(), true, scriptClass.getClassLoader(), UnloadableScript.class,
					"onFirstLoad", "onReload", "onLoad", "onStateSave", "onUnload")));
			taStackTrace.setMaxHeight(Double.MAX_VALUE);
			taStackTrace.setMaxWidth(Double.MAX_VALUE);
			
			final TitledPane pane = new TitledPane(scriptClass.getName(), taStackTrace);
			accordion.getPanes().add(pane);
		}
		if (fqcn2Exception.size() == 1)
			accordion.setExpandedPane(accordion.getPanes().iterator().next());
		return accordion;
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
		catch (IOException e)
		{
			LOG.error("", e);
			return;
		}
		
		final Stage about = new Stage(StageStyle.TRANSPARENT);
		about.initModality(Modality.APPLICATION_MODAL);
		about.initOwner(getMainWindow());
		about.setTitle(UIStrings.get("about.title"));
		about.setScene(aboutDialog);
		WindowTracker.getInstance().add(about);
		about.show();
	}
	
	// ------------------------
	// ========== HELP MENU END
	// ------------------------
	
	private WaitingIndicatorDialogController showWaitDialog(String title, String description, Object... descriptionTokens)
	{
		try
		{
			final FXMLLoader loader = new FXMLLoader(FXUtils.getFXML(WaitingIndicatorDialogController.class), UIStrings.getBundle());
			final Scene scene = new Scene(loader.load(), null);
			
			final Stage stage = new Stage(StageStyle.UTILITY);
			stage.initModality(Modality.NONE);
			stage.initOwner(getMainWindow());
			stage.setTitle(UIStrings.get(title));
			stage.setScene(scene);
			stage.getIcons().addAll(FXUtils.getIconListFX());
			stage.sizeToScene();
			stage.setResizable(false);
			
			final WaitingIndicatorDialogController controller = loader.getController();
			controller.setContentText(UIStrings.get(description, descriptionTokens));
			
			WindowTracker.getInstance().add(stage);
			stage.show();
			return controller;
		}
		catch (IOException e)
		{
			throw new AssertionError("Waiting dialog is missing", e);
		}
	}
	
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
	 * Adds a new tab to the connection pane.
	 * 
	 * @param tab a tab
	 */
	public void addConnectionTab(Tab tab)
	{
		final ObservableList<Tab> tabs = _tpConnections.getTabs();
		tab.setOnClosed(e ->
		{
			final int threshold = tabs.contains(_tabConsole) ? 1 : 0;
			if (tabs.size() <= threshold)
				tabs.add(_tabIdle);
		});
		tabs.remove(_tabIdle);
		tabs.add(tab);
		_tpConnections.getSelectionModel().select(tab);
	}
}
