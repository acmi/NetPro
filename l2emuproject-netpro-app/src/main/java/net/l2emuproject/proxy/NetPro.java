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
package net.l2emuproject.proxy;

import static net.l2emuproject.proxy.ui.javafx.UtilityDialogs.wrapException;

import java.awt.GraphicsEnvironment;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Semaphore;

import javax.tools.Diagnostic;
import javax.tools.Diagnostic.Kind;
import javax.tools.JavaFileObject;

import org.apache.commons.lang3.mutable.MutableBoolean;

import eu.revengineer.simplejse.config.JCSCConfig;
import eu.revengineer.simplejse.exception.IncompatibleScriptCacheException;
import eu.revengineer.simplejse.exception.StaleScriptCacheException;
import eu.revengineer.simplejse.reporting.AptReportingHandler;
import eu.revengineer.simplejse.reporting.DiagnosticLogFile;
import eu.revengineer.simplejse.reporting.JavacReportingHandler;

import net.l2emuproject.lang.NetProThreadPriority;
import net.l2emuproject.lang.management.ShutdownManager;
import net.l2emuproject.lang.management.TerminationStatus;
import net.l2emuproject.proxy.config.ProxyConfig;
import net.l2emuproject.proxy.io.definitions.VersionnedPacketTable;
import net.l2emuproject.proxy.network.game.client.L2GameClientConnections;
import net.l2emuproject.proxy.network.game.server.L2GameServerConnections;
import net.l2emuproject.proxy.network.login.client.L2LoginClientConnections;
import net.l2emuproject.proxy.network.login.server.L2LoginServerConnections;
import net.l2emuproject.proxy.script.LogLoadScriptManager;
import net.l2emuproject.proxy.script.NetProScriptCache;
import net.l2emuproject.proxy.script.PpeEnabledLoaderScriptRegistry;
import net.l2emuproject.proxy.script.ScriptManager;
import net.l2emuproject.proxy.script.analytics.ObjectAnalytics;
import net.l2emuproject.proxy.script.analytics.ObjectLocationAnalytics;
import net.l2emuproject.proxy.script.analytics.PledgeAnalytics;
import net.l2emuproject.proxy.script.analytics.user.LiveUserAnalytics;
import net.l2emuproject.proxy.script.game.HighLevelEventGenerator;
import net.l2emuproject.proxy.script.game.PpeGameScriptRegistry;
import net.l2emuproject.proxy.setup.IPAliasManager;
import net.l2emuproject.proxy.ui.i18n.UIStrings;
import net.l2emuproject.proxy.ui.javafx.ExceptionAlert;
import net.l2emuproject.proxy.ui.javafx.FXUtils;
import net.l2emuproject.proxy.ui.javafx.WindowTracker;
import net.l2emuproject.proxy.ui.javafx.error.view.CompilationErrorExpandableController;
import net.l2emuproject.proxy.ui.javafx.error.view.ExceptionSummaryDialogController;
import net.l2emuproject.proxy.ui.javafx.main.view.MainWindowController;
import net.l2emuproject.proxy.ui.javafx.main.view.SplashScreenController;
import net.l2emuproject.proxy.ui.javafx.packet.ProtocolPacketHidingManager;
import net.l2emuproject.proxy.ui.savormix.loader.LoadOption;
import net.l2emuproject.util.StackTraceUtil;
import net.l2emuproject.util.logging.ListeningLog;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.geometry.VPos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.SplitPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

/**
 * Starts the application with a GUI.
 * 
 * @author _dev_
 */
public class NetPro extends Application implements NetProThreadPriority
{
	private static final Queue<String> PENDING_LOG_ENTRIES = new ArrayBlockingQueue<>(50_000, true);
	private static final StringProperty LOADING_STAGE_DESCRIPTION = new SimpleStringProperty(null);
	
	static Stage SPLASH_STAGE, PRIMARY_STAGE;
	
	@Override
	public void start(Stage primaryStage)
	{
		PRIMARY_STAGE = primaryStage;
		PRIMARY_STAGE.setOnHidden(e -> ShutdownManager.exit(TerminationStatus.MANUAL_SHUTDOWN));
		
		// automatically discard disposed windows from tracker
		final WindowTracker windowTracker = WindowTracker.getInstance();
		final Timeline tlWindowTrackerCleanup = new Timeline(new KeyFrame(Duration.ZERO), new KeyFrame(Duration.minutes(5), e -> windowTracker.cleanup()));
		tlWindowTrackerCleanup.setCycleCount(Animation.INDEFINITE);
		tlWindowTrackerCleanup.play();
		
		// 1. SHOW SPLASH SCREEN
		SPLASH_STAGE = new Stage(StageStyle.TRANSPARENT);
		try
		{
			// 1.1 DYNAMIC SPLASH SCREEN
			final FXMLLoader loader = new FXMLLoader(FXUtils.getFXML(SplashScreenController.class), UIStrings.getBundle());
			SPLASH_STAGE.setScene(new Scene(loader.load(), null));
			loader.<SplashScreenController> getController().bindDescription(LOADING_STAGE_DESCRIPTION);
		}
		catch (final IOException e)
		{
			// 1.2 STATIC SPLASH SCREEN
			final int altSize = 256;
			final Canvas canvas = new Canvas(altSize, altSize);
			final GraphicsContext gc = canvas.getGraphicsContext2D();
			gc.setFill(Color.BLACK);
			gc.fillRect(0, 0, altSize, altSize);
			
			gc.setFill(Color.WHITE);
			gc.setTextAlign(TextAlignment.CENTER);
			gc.setTextBaseline(VPos.BOTTOM);
			gc.fillText(e.getMessage(), altSize >> 1, altSize);
			gc.setTextBaseline(VPos.CENTER);
			gc.setFont(Font.font(altSize * 0.75));
			gc.fillText("NP", altSize >> 1, altSize >> 1);
			SPLASH_STAGE.setScene(new Scene(new Group(canvas), null));
		}
		SPLASH_STAGE.getIcons().addAll(FXUtils.getIconListFX());
		SPLASH_STAGE.setOnHidden(e -> {
			if (ShutdownManager.isRunningHooks())
				return;
			
			Platform.exit();
			System.exit(0);
		});
		windowTracker.add(SPLASH_STAGE);
		SPLASH_STAGE.show();
		
		final Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
		SPLASH_STAGE.setX((screenBounds.getWidth() - SPLASH_STAGE.getWidth()) / 2D);
		SPLASH_STAGE.setY((screenBounds.getHeight() - SPLASH_STAGE.getHeight()) / 2D);
		
		// 2. LANGUAGE SELECTION
		final String language;
		final Set<String> languages = UIStrings.SUPPORTED_LOCALES.keySet();
		if (languages.size() > 1)
		{
			// 2.1 SHOW LANGUAGE SELECTION
			String defaultChoice = null;
			// 2.1.1 SET DEFAULT SELECTION BASED ON DEFAULT LOCALE
			for (final Entry<String, Locale> e : UIStrings.SUPPORTED_LOCALES.entrySet())
			{
				if (e.getValue().getLanguage().equals(UIStrings.CURRENT_LOCALE.getLanguage()))
				{
					defaultChoice = e.getKey();
					break;
				}
			}
			if (defaultChoice == null)
			{
				// 2.1.2 SET DEFAULT SELECTION TO ENGLISH
				for (final Entry<String, Locale> e : UIStrings.SUPPORTED_LOCALES.entrySet())
				{
					if (e.getValue().getLanguage().equals(Locale.ENGLISH.getLanguage()))
					{
						defaultChoice = e.getKey();
						break;
					}
				}
			}
			
			// 2.1.3 QUERY USER FOR LANGUAGE
			final ChoiceDialog<String> dlgSelectLanguage = new ChoiceDialog<>(defaultChoice, languages);
			dlgSelectLanguage.setHeaderText("Select preferred language:");
			dlgSelectLanguage.setTitle("Language selection");
			dlgSelectLanguage.initModality(Modality.APPLICATION_MODAL);
			dlgSelectLanguage.initOwner(SPLASH_STAGE);
			dlgSelectLanguage.initStyle(StageStyle.UTILITY);
			
			windowTracker.add(dlgSelectLanguage);
			final Optional<String> result = dlgSelectLanguage.showAndWait();
			if (!result.isPresent())
			{
				Platform.exit();
				System.exit(0);
			}
			language = result.get();
		}
		else // 2.2 SELECT THE ONLY OPTION
			language = languages.isEmpty() ? null : languages.iterator().next();
		
		UIStrings.CURRENT_LOCALE = language != null ? UIStrings.SUPPORTED_LOCALES.getOrDefault(language, Locale.ENGLISH) : Locale.ENGLISH;
		
		// 3. START UNDERLYING APPLICATION
		final Thread startupThread = new Thread(NetPro::loadInOrder, "NetProStartupThread");
		startupThread.setPriority(STARTUP);
		startupThread.start();
	}
	
	/** Starts the underlying application. Additional actions are performed if the GUI elements were pre-initialized. */
	private static void loadInOrder()
	{
		// 1. DETERMINE IF GUI IS AVAILABLE
		final StartupStateReporter reporter;
		if (PRIMARY_STAGE != null)
		{
			// 1.1 GET ACCESS TO SPLASH SCREEN
			reporter = desc -> Platform.runLater(() -> LOADING_STAGE_DESCRIPTION.setValue(desc));
			// 1.2 SETUP UI LOGGING FORWARDER
			ListeningLog.addListener(message -> {
				final boolean added = PENDING_LOG_ENTRIES.offer(message);
				if (!added)
				{
					PENDING_LOG_ENTRIES.clear();
					PENDING_LOG_ENTRIES.offer("|--------------------|--------------------|");
				}
			});
		}
		else
			reporter = null;
		
		L2Proxy.addStartupHook(() -> {
			// 2.1 INSTALL ANALYTICS
			if (reporter != null)
				reporter.onState(UIStrings.get("startup.analytics"));
			LogLoadScriptManager.getInstance().addScript(PpeEnabledLoaderScriptRegistry.getInstance());
			if (StartupOption.DISABLE_PROXY.isNotSet())
			{
				// only useful LIVE
				ScriptManager.getInstance().addScript(PpeGameScriptRegistry.getInstance());
				LiveUserAnalytics.getInstance().onLoad();
				new ObjectLocationAnalytics().onLoad();
				HighLevelEventGenerator.getInstance().onLoad();
			}
			new ObjectAnalytics().onLoad();
			new PledgeAnalytics().onLoad();
			
			// 2.2 LOAD USER SCRIPTS
			if (reporter != null)
			{
				reporter.onState(UIStrings.get("startup.scripts"));
				
				final InteractiveScriptCompilationHandler interactiveErrorReporter = new InteractiveScriptCompilationHandler(reporter);
				NetProScriptCache.INITIALIZER_APT_HANDLER = interactiveErrorReporter;
				NetProScriptCache.INITIALIZER_JAVAC_HANDLER = interactiveErrorReporter;
			}
			
			final NetProScriptCache cache = NetProScriptCache.getInstance();
			final Map<Class<?>, RuntimeException> fqcn2Exception = new TreeMap<>((c1, c2) -> c1.getName().compareTo(c2.getName()));
			scripts: if (LoadOption.DISABLE_SCRIPTS.isNotSet())
			{
				loadCache: if (!ProxyConfig.DISABLE_SCRIPT_CACHE)
				{
					// 2.2.1 LOAD PRECOMPILED CACHE
					try
					{
						try
						{
							cache.restoreFromCache(fqcn2Exception);
							break scripts;
						}
						catch (final StaleScriptCacheException e)
						{
							if (reporter == null)
								break loadCache; // proceed to compilation
								
							// 2.2.2 ASK TO REBUILD STALE CACHE
							synchronized (reporter)
							{
								final MutableBoolean recompile = new MutableBoolean(false);
								Platform.runLater(() -> {
									final Alert confirmDlg = new Alert(AlertType.WARNING);
									confirmDlg.setTitle(UIStrings.get("startup.scripts.stalecache.dialog.title"));
									confirmDlg.setHeaderText(UIStrings.get("startup.scripts.stalecache.dialog.header"));
									confirmDlg.setContentText(UIStrings.get("startup.scripts.stalecache.dialog.content", UIStrings.get("generic.button.yes"), UIStrings.get("generic.button.no")));
									confirmDlg.getButtonTypes().setAll(new ButtonType(UIStrings.get("generic.button.yes"), ButtonData.YES),
											new ButtonType(UIStrings.get("generic.button.no"), ButtonData.NO));
									
									confirmDlg.initOwner(SPLASH_STAGE);
									confirmDlg.initModality(Modality.APPLICATION_MODAL);
									confirmDlg.initStyle(StageStyle.DECORATED);
									
									WindowTracker.getInstance().add(confirmDlg);
									final Optional<ButtonType> result = confirmDlg.showAndWait();
									if (result.isPresent() && result.get().getButtonData() == ButtonData.YES)
										recompile.setTrue();
									
									synchronized (reporter)
									{
										reporter.notifyAll();
									}
								});
								reporter.wait();
								
								if (recompile.booleanValue())
									break loadCache;
							}
							
							cache.setStaleCacheOK();
							cache.restoreFromCache(fqcn2Exception);
							break scripts;
						}
					}
					catch (IncompatibleScriptCacheException | IOException e)
					{
						fqcn2Exception.clear();
						// proceed to compilation
					}
				}
				
				if (!cache.isCompilerUnavailable())
				{
					// 2.2.3 COMPILE AND LOAD SCRIPTS
					cache.compileAllScripts(fqcn2Exception);
					cache.writeToCache();
					break scripts;
				}
				
				if (reporter == null)
					break scripts;
				
				// 2.2.4 ASK TO CONTINUE WITH NO SCRIPTS LOADED
				synchronized (reporter)
				{
					Platform.runLater(() -> {
						final Alert confirmDlg = new Alert(AlertType.WARNING);
						confirmDlg.setTitle(UIStrings.get("startup.scripts.jre.nocache.dialog.title"));
						confirmDlg.setHeaderText(UIStrings.get("startup.scripts.jre.nocache.dialog.header"));
						confirmDlg.setContentText(UIStrings.get("startup.scripts.jre.nocache.dialog.content", System.getProperty("java.home"), NetProScriptCache.getScriptCacheName()));
						confirmDlg.getButtonTypes().setAll(new ButtonType(UIStrings.get("startup.scripts.jre.nocache.dialog.button.continue"), ButtonData.YES),
								new ButtonType(UIStrings.get("startup.scripts.jre.nocache.dialog.button.exit"), ButtonData.NO));
						
						confirmDlg.initOwner(SPLASH_STAGE);
						confirmDlg.initModality(Modality.APPLICATION_MODAL);
						confirmDlg.initStyle(StageStyle.DECORATED);
						
						WindowTracker.getInstance().add(confirmDlg);
						final Optional<ButtonType> result = confirmDlg.showAndWait();
						if (!result.isPresent() || result.get().getButtonData() != ButtonData.YES)
							ShutdownManager.exit(TerminationStatus.MANUAL_SHUTDOWN);
						
						synchronized (reporter)
						{
							reporter.notifyAll();
						}
					});
					reporter.wait();
				}
			}
			
			if (reporter != null)
			{
				if (!fqcn2Exception.isEmpty())
				{
					// 2.2.5 REPORT SCRIPT INITIALIZATION ERRORS
					synchronized (reporter)
					{
						Platform.runLater(() -> {
							final Alert alert = new Alert(AlertType.WARNING);
							alert.initModality(Modality.APPLICATION_MODAL);
							alert.initOwner(SPLASH_STAGE);
							alert.initStyle(StageStyle.UTILITY);
							alert.setTitle(UIStrings.get("startup.scripts.err.dialog.title.init"));
							alert.setHeaderText(UIStrings.get("startup.scripts.err.dialog.header.init"));
							alert.setContentText(UIStrings.get("startup.scripts.err.dialog.content.init"));
							
							alert.getDialogPane().setExpandableContent(MainWindowController.makeScriptExceptionMapExpandableContent(fqcn2Exception));
							WindowTracker.getInstance().add(alert);
							alert.showAndWait();
							
							synchronized (reporter)
							{
								reporter.notifyAll();
							}
						});
						reporter.wait();
					}
				}
				
				reporter.onState(UIStrings.get("startup.definitions"));
			}
			
			// 2.3 LOAD NETWORK PROTOCOL RELATED DEFINITIONS
			VersionnedPacketTable.getInstance();
			if (reporter != null)
			{
				// 2.3.1 LOAD PROTOCOL BASED PACKET HIDING CONFIG
				final Semaphore semaphore = new Semaphore(0);
				ProtocolPacketHidingManager.AUTOMATIC_LOADING_EXCEPTION_HANDLER = exceptions -> {
					if (!exceptions.isEmpty())
					{
						Platform.runLater(() -> {
							final Alert alert = new Alert(AlertType.WARNING);
							alert.initModality(Modality.APPLICATION_MODAL);
							alert.initOwner(SPLASH_STAGE);
							alert.initStyle(StageStyle.UTILITY);
							alert.setTitle(UIStrings.get("startup.definitions.hiding.err.dialog.title"));
							alert.setHeaderText(UIStrings.get("startup.definitions.hiding.err.dialog.header"));
							alert.setContentText(UIStrings.get("startup.definitions.hiding.err.dialog.content"));
							
							alert.getDialogPane().setExpandableContent(makeThrowableMapExpandableContent(exceptions));
							WindowTracker.getInstance().add(alert);
							alert.showAndWait();
							
							// different thread
							semaphore.release();
						});
					}
					else // same thread
						semaphore.release();
				};
				ProtocolPacketHidingManager.getInstance();
				semaphore.acquire();
				
				reporter.onState(UIStrings.get("startup.ipalias"));
			}
			// 2.4 LOAD IP ALIASES
			IPAliasManager.getInstance();
		});
		
		// 2. START THE PROXY SERVER AND LOAD DEPENDENCIES
		try
		{
			L2Proxy.main(); // launches the backend
		}
		catch (final Throwable t)
		{
			// 2.F HANDLE FAILURE
			
			// 2.F.1 WRITE A LOG FILE
			try (final PrintWriter out = new PrintWriter("crash_" + System.currentTimeMillis() + ".txt", StandardCharsets.UTF_8.name()))
			{
				t.printStackTrace(out);
			}
			catch (final Throwable th)
			{
				// too bad, really
			}
			
			if (SPLASH_STAGE != null)
			{
				// 2.F.2.1 SHOW GUI DIALOG
				Platform.runLater(() -> {
					final Alert dlgFail = new ExceptionAlert(t, SPLASH_STAGE);
					WindowTracker.getInstance().add(dlgFail);
					dlgFail.showAndWait();
				});
			}
			else // 2.F.2.2 WRITE TO CONSOLE
				t.printStackTrace(System.err);
			ShutdownManager.exit(TerminationStatus.RUNTIME_UNCAUGHT_ERROR);
			return;
		}
		
		if (reporter == null)
			return;
		
		// 3. OPEN GUI
		reporter.onState(UIStrings.get("startup.ui"));
		Platform.runLater(() -> {
			// 3.1 OPEN MAIN WINDOW
			try
			{
				final FXMLLoader loader = new FXMLLoader(FXUtils.getFXML(MainWindowController.class), UIStrings.getBundle());
				final Scene scene = new Scene(loader.load());
				final MainWindowController controller = loader.getController();
				// 3.2 LINK LOGGING WITH UI CONSOLE
				final Timeline tlLogging = new Timeline(new KeyFrame(Duration.ZERO, evt -> {
					for (String msg; (msg = PENDING_LOG_ENTRIES.poll()) != null;)
						controller.appendToConsole(msg);
				}), new KeyFrame(Duration.seconds(0.2)));
				tlLogging.setCycleCount(Animation.INDEFINITE);
				tlLogging.play();
				PRIMARY_STAGE.setScene(scene);
				PRIMARY_STAGE.setTitle("NetPro" + (NetProInfo.isUnreleased() ? "" : " " + (NetProInfo.isSnapshot() ? "r" + NetProInfo.getRevisionNumber() : NetProInfo.getVersion())));
				PRIMARY_STAGE.getIcons().addAll(FXUtils.getIconListFX());
				
				WindowTracker.getInstance().add(PRIMARY_STAGE, false);
				PRIMARY_STAGE.show();
				
				L2LoginClientConnections.getInstance().addConnectionListener(controller);
				L2LoginClientConnections.getInstance().addPacketListener(controller);
				L2LoginServerConnections.getInstance().addConnectionListener(controller);
				L2LoginServerConnections.getInstance().addPacketListener(controller);
				L2GameClientConnections.getInstance().addConnectionListener(controller);
				L2GameServerConnections.getInstance().addConnectionListener(controller);
				L2GameClientConnections.getInstance().addPacketListener(controller);
				L2GameServerConnections.getInstance().addPacketListener(controller);
			}
			catch (final IOException e)
			{
				// 3.F MAIN WINDOW MISSING, WARN AND EXIT
				final Throwable t = StackTraceUtil.stripUntilClassContext(e, true, NetPro.class.getName());
				wrapException(t, "generic.err.internal.title", null, "generic.err.internal.header.fxml", null, SPLASH_STAGE, Modality.WINDOW_MODAL).showAndWait();
				ShutdownManager.exit(TerminationStatus.ENVIRONMENT_MISSING_COMPONENT_OR_SERVICE);
			}
			finally
			{
				if (SPLASH_STAGE != null)
				{
					// 3.3 CLOSE SPLASH SCREEN
					SPLASH_STAGE.setOnHidden(null);
					SPLASH_STAGE.hide();
					SPLASH_STAGE = null;
				}
			}
		});
	}
	
	private static final <T extends Throwable> Node makeThrowableMapExpandableContent(Map<Path, T> path2Exception)
	{
		if (path2Exception.isEmpty())
			return null;
		
		try
		{
			final FXMLLoader loader = new FXMLLoader(FXUtils.getFXML(ExceptionSummaryDialogController.class), UIStrings.getBundle());
			final SplitPane result = loader.load();
			final ExceptionSummaryDialogController controller = loader.getController();
			controller.setAllExceptions(path2Exception, p -> p.getFileName().toString(), t -> StackTraceUtil.traceToString(StackTraceUtil.stripUntilClassContext(t, true, NetPro.class.getName())));
			return result;
		}
		catch (final IOException e)
		{
			final Throwable t = StackTraceUtil.stripUntilClassContext(e, true, NetPro.class.getName());
			wrapException(t, "generic.err.internal.title", null, "generic.err.internal.header.fxml", null, SPLASH_STAGE, Modality.WINDOW_MODAL).showAndWait();
			return null;
		}
	}
	
	/**
	 * Starts the application.
	 * 
	 * @param args command line arguments, governed by {@link LoadOption}
	 */
	public static void main(String[] args)
	{
		for (final String arg : args)
		{
			final StartupOption lo = StartupOption.getByAlias(arg);
			if (lo == null)
			{
				if (StartupOption.IGNORE_UNKNOWN.isSet())
					continue;
				
				System.err.println("Unrecognized command line argument: " + arg);
				continue;
			}
			
			switch (lo)
			{
				case HELP:
					final StringBuilder sb = new StringBuilder();
					for (final StartupOption opt : StartupOption.values())
					{
						if (!opt.isInHelp())
							continue;
						
						final String[] alias = opt.getAlias();
						sb.append(alias[0]);
						for (int i = 1; i < alias.length; ++i)
							sb.append(", ").append(alias[i]);
						sb.append(" - ").append(opt.getDescription()).append("\r\n");
					}
					System.out.print(sb);
					System.exit(0);
					break;
				default:
					lo.setSystemProperty();
					break;
				// throw new InternalError("Unhandled startup option: " + lo);
			}
		}
		
		if (GraphicsEnvironment.isHeadless()) // a fool's hope
			StartupOption.DISABLE_UI.setSystemProperty();
		
		if (StartupOption.DISABLE_PROXY.isSet() && StartupOption.DISABLE_UI.isSet())
			System.exit(0);
		
		if (StartupOption.DISABLE_UI.isNotSet())
		{
			launch(args);
			return;
		}
		
		loadInOrder();
	}
	
	private static final class InteractiveScriptCompilationHandler implements AptReportingHandler, JavacReportingHandler
	{
		private final List<String> _messages;
		private final Object _lock;
		
		InteractiveScriptCompilationHandler(Object lock)
		{
			_messages = new LinkedList<>();
			_lock = lock;
		}
		
		@Override
		public void onCustomDiagnostic(String diagnosticText)
		{
			_messages.add(diagnosticText);
		}
		
		@Override
		public void onSanityDiagnostic(String diagnosticText)
		{
			onCustomDiagnostic("-=[!ASSERT!]=--> " + diagnosticText);
		}
		
		@Override
		public void report(Diagnostic<? extends JavaFileObject> diagnostic)
		{
			if (diagnostic.getKind() == Kind.ERROR)
				_messages.add(DiagnosticLogFile.formatDiagnostic(diagnostic, JCSCConfig.DEFAULT_ENCODING));
		}
		
		@Override
		public void onFirstStart()
		{
			onStart();
		}
		
		@Override
		public void onStart()
		{
			_messages.clear();
		}
		
		@Override
		public void onInitialEnd(Collection<Path> erroneousScripts)
		{
			onEnd(erroneousScripts, true);
		}
		
		@Override
		public void onEnd(Collection<Path> erroneousScripts)
		{
			onEnd(erroneousScripts, false);
		}
		
		private void onEnd(Collection<Path> erroneousScripts, boolean init)
		{
			// this is apt, so we do not need to inform about "success" less than halfway there
			if (erroneousScripts.isEmpty())
				return;
			
			try
			{
				synchronized (_lock)
				{
					Platform.runLater(() -> {
						final Alert confirmDlg = new Alert(init ? AlertType.WARNING : AlertType.ERROR);
						confirmDlg.setTitle(UIStrings.get("startup.scripts.err.dialog.title"));
						if (init)
						{
							confirmDlg.setHeaderText(UIStrings.get("startup.scripts.err.dialog.header"));
							confirmDlg.setContentText(UIStrings.get("startup.scripts.err.dialog.content", System.getProperty("java.home")));
							confirmDlg.getButtonTypes().setAll(new ButtonType(UIStrings.get("startup.scripts.err.dialog.button.continue"), ButtonData.YES),
									new ButtonType(UIStrings.get("startup.scripts.err.dialog.button.exit"), ButtonData.NO));
						}
						else
						{
							confirmDlg.setHeaderText(UIStrings.get("scripts.load.err.dialog.header.compile"));
							confirmDlg.setContentText(UIStrings.get("scripts.load.err.dialog.content.compile", System.getProperty("java.home")));
						}
						
						try
						{
							final FXMLLoader loader = new FXMLLoader(FXUtils.getFXML(CompilationErrorExpandableController.class), UIStrings.getBundle());
							confirmDlg.getDialogPane().setExpandableContent(loader.load());
							confirmDlg.getDialogPane().setExpanded(!init);
							
							final CompilationErrorExpandableController controller = loader.getController();
							final StringBuilder sbScripts = new StringBuilder(), sbErrors = new StringBuilder();
							final Iterator<Path> itScript = erroneousScripts.iterator();
							sbScripts.append(itScript.next());
							while (itScript.hasNext())
								sbScripts.append("\r\n").append(itScript.next());
							if (!_messages.isEmpty())
							{
								final Iterator<String> itErr = _messages.iterator();
								sbErrors.append(itErr.next());
								while (itErr.hasNext())
									sbErrors.append("\r\n").append(itErr.next());
							}
							controller.setErroneousScripts(sbScripts.toString(), sbErrors.toString());
						}
						catch (final IOException e)
						{
							throw new AssertionError("Compilation error view is missing", e);
						}
						
						confirmDlg.initOwner(SPLASH_STAGE);
						confirmDlg.initModality(Modality.APPLICATION_MODAL);
						confirmDlg.initStyle(StageStyle.DECORATED);
						
						WindowTracker.getInstance().add(confirmDlg);
						final Optional<ButtonType> result = confirmDlg.showAndWait();
						if (!result.isPresent() || result.get().getButtonData() == ButtonData.NO)
							ShutdownManager.exit(TerminationStatus.MANUAL_SHUTDOWN);
						
						synchronized (_lock)
						{
							_lock.notifyAll();
						}
					});
					_lock.wait();
				}
			}
			catch (final InterruptedException e)
			{
				throw new RuntimeException(e);
			}
		}
		
		@Override
		public void onFailure()
		{
			try
			{
				synchronized (_lock)
				{
					Platform.runLater(() -> {
						final Alert confirmDlg = new Alert(AlertType.ERROR);
						confirmDlg.setTitle(UIStrings.get("startup.scripts.err.apt.fail.dialog.title"));
						confirmDlg.setHeaderText(UIStrings.get("startup.scripts.err.apt.fail.dialog.header"));
						confirmDlg.setContentText(UIStrings.get("startup.scripts.err.apt.fail.dialog.content", System.getProperty("java.home")));
						confirmDlg.getButtonTypes().setAll(new ButtonType(UIStrings.get("startup.scripts.err.apt.fail.dialog.button.continue"), ButtonData.YES),
								new ButtonType(UIStrings.get("startup.scripts.err.apt.fail.dialog.button.exit"), ButtonData.NO));
						
						confirmDlg.initOwner(SPLASH_STAGE);
						confirmDlg.initModality(Modality.APPLICATION_MODAL);
						confirmDlg.initStyle(StageStyle.DECORATED);
						
						WindowTracker.getInstance().add(confirmDlg);
						final Optional<ButtonType> result = confirmDlg.showAndWait();
						if (!result.isPresent() || result.get().getButtonData() != ButtonData.YES)
							ShutdownManager.exit(TerminationStatus.MANUAL_SHUTDOWN);
						
						synchronized (_lock)
						{
							_lock.notifyAll();
						}
					});
					_lock.wait();
				}
			}
			catch (final InterruptedException e)
			{
				throw new RuntimeException(e);
			}
		}
		
		@Override
		public void onInitialEnd(Map<Collection<Path>, Collection<String>> compilationErrors)
		{
			onEnd(compilationErrors.keySet(), true);
		}
		
		@Override
		public void onEnd(Map<Collection<Path>, Collection<String>> compilationErrors)
		{
			onEnd(compilationErrors.keySet(), false);
		}
		
		private void onEnd(Set<Collection<Path>> erroneousScripts, boolean init)
		{
			final List<Path> scripts = new ArrayList<>();
			for (final Collection<Path> paths : erroneousScripts)
				scripts.addAll(paths);
			onEnd(scripts, init);
		}
	}
}
