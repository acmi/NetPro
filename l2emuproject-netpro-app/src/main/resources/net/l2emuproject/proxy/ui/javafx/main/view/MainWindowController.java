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

import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.Future;
import java.util.function.Consumer;

import eu.revengineer.simplejse.exception.DependencyResolutionException;
import eu.revengineer.simplejse.exception.MutableOperationInProgressException;
import eu.revengineer.simplejse.logging.BytesizeInterpreter;
import eu.revengineer.simplejse.logging.BytesizeInterpreter.BytesizeUnit;
import eu.revengineer.simplejse.type.UnloadableScript;

import net.l2emuproject.lang.L2TextBuilder;
import net.l2emuproject.lang.management.ShutdownManager;
import net.l2emuproject.lang.management.TerminationStatus;
import net.l2emuproject.proxy.script.NetProScriptCache;
import net.l2emuproject.proxy.ui.i18n.UIStrings;
import net.l2emuproject.proxy.ui.javafx.ExceptionAlert;
import net.l2emuproject.proxy.ui.javafx.FXLocator;
import net.l2emuproject.proxy.ui.savormix.loader.LoadOption;
import net.l2emuproject.util.StackTraceUtil;
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
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Accordion;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.Dialog;
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
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
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
			about.initOwner(getMainWindow());
			about.setTitle(UIStrings.get("about.title"));
			about.setScene(aboutDialog);
			about.show();
		});
	}
	
	private Window getMainWindow()
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
		final TextInputDialog nameDialog = initScriptDialog(new TextInputDialog(), inputDialogTitle, inputDialogHeader, null);
		final Optional<String> result = nameDialog.showAndWait();
		if (!result.isPresent())
			return;
		
		final String pattern = result.get();
		final WaitingIndicatorDialogController waitDialog = showWaitDialog("generic.waitdlg.title", "scripts.load.waitdlg.content.index", pattern);
		waitDialog.getWindow().setUserData(pattern);
		
		action.accept(waitDialog);
	}
	
	private WaitingIndicatorDialogController showWaitDialog(String title, String description, Object... descriptionTokens)
	{
		try
		{
			final FXMLLoader loader = new FXMLLoader(FXLocator.getFXML(WaitingIndicatorDialogController.class), UIStrings.getBundle());
			final Scene scene = new Scene(loader.load(), null);
			
			final Stage stage = new Stage(StageStyle.UTILITY);
			stage.initModality(Modality.NONE);
			stage.initOwner(getMainWindow());
			stage.setTitle(UIStrings.get(title));
			stage.setScene(scene);
			stage.getIcons().addAll(FXLocator.getIconListFX());
			stage.sizeToScene();
			stage.setResizable(false);
			
			final WaitingIndicatorDialogController controller = loader.getController();
			controller.setContentText(UIStrings.get(description, descriptionTokens));
			
			stage.show();
			return controller;
		}
		catch (IOException e)
		{
			throw new AssertionError("Waiting dialog is missing", e);
		}
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
						makeScriptAlert(INFORMATION, "scripts.load.err.dialog.title", "scripts.load.err.dialog.header.nonexistent", "scripts.load.err.dialog.content.nonexistent", pattern).show();
						return;
					}
					
					final String result;
					if (matchingScripts.size() > 1)
					{
						final Optional<String> resultWrapper = showChoiceDialog("scripts.load.dialog.title", "scripts.load.dialog.header.select", matchingScripts);
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
							
							makeScriptAlert(ERROR, "scripts.load.err.dialog.title", "scripts.load.err.dialog.header.singleop", "scripts.load.err.dialog.content.singleop", result).show();
							return;
						}
						catch (DependencyResolutionException e)
						{
							nextWaitDialogController.onWaitEnd();
							
							makeScriptAlert(ERROR, "scripts.load.err.dialog.title", "scripts.load.err.dialog.header.apt", "scripts.load.err.dialog.content.apt", System.getProperty("java.home"));
							return;
						}
						catch (IOException e)
						{
							nextWaitDialogController.onWaitEnd();
							
							initScriptDialog(new ExceptionAlert(e), "scripts.load.err.dialog.title", "scripts.load.err.dialog.header.runtime", null).show();
							return;
						}
						catch (IllegalArgumentException e)
						{
							nextWaitDialogController.onWaitEnd();
							
							makeScriptAlert(INFORMATION, "scripts.load.err.dialog.title", "scripts.load.err.dialog.header.nonexistent", "scripts.load.err.dialog.content.nonexistent", result).show();
							return;
						}
						
						Platform.runLater(() ->
						{
							nextWaitDialogController.onWaitEnd();
							
							if (fqcn2Exception.isEmpty())
							{
								makeScriptAlert(INFORMATION, "scripts.load.done.dialog.title", "scripts.load.done.dialog.header", "scripts.load.done.dialog.content", result).show();
								return;
							}
							
							final Alert alert = makeScriptAlert(WARNING, "scripts.load.done.dialog.title", "scripts.load.done.dialog.header", "scripts.load.done.dialog.content.runtime", result);
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
				
				makeScriptAlert(ERROR, "scripts.load.err.dialog.title", "scripts.load.err.dialog.header.singleop", "scripts.load.err.dialog.content.singleop", pattern).show();
			}
			catch (InterruptedException e)
			{
				// application shutting down
				waitDialogController.onWaitEnd();
			}
			catch (IOException ex)
			{
				initScriptDialog(new ExceptionAlert(ex), "scripts.load.err.dialog.title", "scripts.load.err.dialog.header.runtime", null).show();
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
				makeScriptAlert(INFORMATION, "scripts.unload.err.dialog.title", "scripts.unload.err.dialog.header.nonexistent", "scripts.unload.err.dialog.content.nonexistent", pattern).show();
				return;
			}
			
			final String result;
			if (matchingScripts.size() > 1)
			{
				final Optional<String> resultWrapper = showChoiceDialog("scripts.unload.dialog.title", "scripts.unload.dialog.header.select", matchingScripts);
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
					
					makeScriptAlert(INFORMATION, "scripts.unload.err.dialog.title", "scripts.unload.err.dialog.header.nonexistent", "scripts.unload.err.dialog.content.nonexistent", result);
					return;
				}
				
				Platform.runLater(() ->
				{
					nextWaitDialogController.onWaitEnd();
					
					if (fqcn2Exception.isEmpty())
					{
						makeScriptAlert(INFORMATION, "scripts.unload.done.dialog.title", "scripts.unload.done.dialog.header", "scripts.unload.done.dialog.content", result).show();
						return;
					}
					
					final Alert alert = makeScriptAlert(WARNING, "scripts.unload.done.dialog.title", "scripts.unload.done.dialog.header", "scripts.unload.done.dialog.content.runtime", result);
					alert.getDialogPane().setExpandableContent(makeScriptExceptionMapExpandabeContent(fqcn2Exception));
					alert.show();
				});
			});
			nextWaitDialogController.setCancelAction(() -> task.cancel(true));
		}
		catch (MutableOperationInProgressException e)
		{
			waitDialogController.onWaitEnd();
			
			makeScriptAlert(ERROR, "scripts.unload.err.dialog.title", "scripts.unload.err.dialog.header.singleop", "scripts.unload.err.dialog.content.singleop", pattern).show();
		}
		catch (InterruptedException e)
		{
			// application shutting down
			waitDialogController.onWaitEnd();
		}
	}
	
	private <E> Optional<E> showChoiceDialog(String title, String header, Set<E> choices)
	{
		return initScriptDialog(new ChoiceDialog<>(choices.iterator().next(), choices), title, header, null).showAndWait();
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
	
	private final Alert makeScriptAlert(AlertType type, String title, String header, String content, Object... contentTokens)
	{
		return initScriptDialog(new Alert(type), title, header, content, contentTokens);
	}
	
	private final <T, D extends Dialog<T>> D initScriptDialog(D dialog, String title, String header, String content, Object... contentTokens)
	{
		dialog.initModality(Modality.NONE);
		dialog.initOwner(getMainWindow());
		dialog.initStyle(StageStyle.UTILITY);
		
		dialog.setTitle(UIStrings.get(title));
		dialog.setHeaderText(UIStrings.get(header));
		if (content != null)
			dialog.setContentText(UIStrings.get(content, contentTokens));
		
		return dialog;
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
