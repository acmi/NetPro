package net.l2emuproject.proxy;

import java.awt.GraphicsEnvironment;
import java.io.IOException;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;

import net.l2emuproject.proxy.config.ProxyConfig;
import net.l2emuproject.proxy.script.LogLoadScriptManager;
import net.l2emuproject.proxy.script.NetProScriptCache;
import net.l2emuproject.proxy.script.PpeEnabledLoaderScriptRegistry;
import net.l2emuproject.proxy.script.ScriptManager;
import net.l2emuproject.proxy.script.analytics.LiveUserAnalytics;
import net.l2emuproject.proxy.script.analytics.ObjectAnalytics;
import net.l2emuproject.proxy.script.analytics.ObjectLocationAnalytics;
import net.l2emuproject.proxy.script.game.HighLevelEventGenerator;
import net.l2emuproject.proxy.script.game.PpeGameScriptRegistry;
import net.l2emuproject.proxy.setup.IPAliasManager;
import net.l2emuproject.proxy.ui.i18n.UIStrings;
import net.l2emuproject.proxy.ui.javafx.ExceptionAlert;
import net.l2emuproject.proxy.ui.javafx.FXLocator;
import net.l2emuproject.proxy.ui.javafx.main.view.MainWindowController;
import net.l2emuproject.proxy.ui.javafx.main.view.SplashScreenController;
import net.l2emuproject.proxy.ui.savormix.io.VersionnedPacketTable;
import net.l2emuproject.proxy.ui.savormix.loader.LoadOption;
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
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ChoiceDialog;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

/**
 * @author _dev_
 */
public class NetPro extends Application
{
	private static final Queue<String> PENDING_LOG_ENTRIES = new ArrayBlockingQueue<>(50_000, true);
	private static final StringProperty LOADING_STAGE_DESCRIPTION = new SimpleStringProperty(null);
	
	private static Stage SPLASH_STAGE, PRIMARY_STAGE;
	
	@Override
	public void start(Stage primaryStage)
	{
		PRIMARY_STAGE = primaryStage;
		
		try
		{
			final FXMLLoader loader = new FXMLLoader(FXLocator.getFXML(SplashScreenController.class), UIStrings.getBundle());
			final Scene scene = new Scene(loader.load(), null);
			final SplashScreenController controller = loader.getController();
			controller.bindDescription(LOADING_STAGE_DESCRIPTION);
			
			SPLASH_STAGE = new Stage(StageStyle.TRANSPARENT);
			SPLASH_STAGE.setScene(scene);
			SPLASH_STAGE.getIcons().addAll(FXLocator.getIconListFX());
			SPLASH_STAGE.show();
			
			final Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
			SPLASH_STAGE.setX((screenBounds.getWidth() - SPLASH_STAGE.getWidth()) / 2D);
			SPLASH_STAGE.setY((screenBounds.getHeight() - SPLASH_STAGE.getHeight()) / 2D);
		}
		catch (IOException e)
		{
			throw new AssertionError("Splash screen is missing", e);
		}
		
		final String language;
		final Set<String> languages = UIStrings.SUPPORTED_LOCALES.keySet();
		if (languages.size() > 1)
		{
			String defaultChoice = null;
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
				for (final Entry<String, Locale> e : UIStrings.SUPPORTED_LOCALES.entrySet())
				{
					if (e.getValue().getLanguage().equals(Locale.ENGLISH.getLanguage()))
					{
						defaultChoice = e.getKey();
						break;
					}
				}
			}
			
			final ChoiceDialog<String> dlgSelectLanguage = new ChoiceDialog<String>(defaultChoice, languages);
			dlgSelectLanguage.setHeaderText("Select preferred language:");
			dlgSelectLanguage.setTitle("Language selection");
			dlgSelectLanguage.initModality(Modality.APPLICATION_MODAL);
			dlgSelectLanguage.initOwner(SPLASH_STAGE);
			dlgSelectLanguage.initStyle(StageStyle.UTILITY);
			
			final Optional<String> result = dlgSelectLanguage.showAndWait();
			if (!result.isPresent())
			{
				Platform.exit();
				System.exit(0);
			}
			language = result.get();
		}
		else
			language = languages.isEmpty() ? null : languages.iterator().next();
			
		UIStrings.CURRENT_LOCALE = language != null ? UIStrings.SUPPORTED_LOCALES.getOrDefault(language, Locale.ENGLISH) : Locale.ENGLISH;
		
		new Thread(NetPro::loadInOrder, "ApplicationStartThread").start();
	}
	
	private static void loadInOrder()
	{
		final StartupStateReporter reporter = PRIMARY_STAGE != null ? desc -> Platform.runLater(() -> LOADING_STAGE_DESCRIPTION.setValue(desc)) : null;
		
		ListeningLog.addListener(message ->
		{
			final boolean added = PENDING_LOG_ENTRIES.offer(message);
			if (!added)
			{
				PENDING_LOG_ENTRIES.clear();
				PENDING_LOG_ENTRIES.offer("|--------------------|--------------------|");
			}
		});
		
		L2Proxy.addStartupHook(() ->
		{
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
			
			if (reporter != null)
				reporter.onState(UIStrings.get("startup.scripts"));
			final NetProScriptCache cache = NetProScriptCache.getInstance();
			if (LoadOption.DISABLE_SCRIPTS.isNotSet() && (ProxyConfig.DISABLE_SCRIPT_CACHE || !cache.restoreFromCache()) && !cache.isCompilerUnavailable())
			{
				cache.compileAllScripts();
				cache.writeToCache();
			}
			
			if (reporter != null)
				reporter.onState(UIStrings.get("startup.definitions"));
			VersionnedPacketTable.getInstance();
			if (reporter != null)
				reporter.onState(UIStrings.get("startup.ipalias"));
			IPAliasManager.getInstance();
		});
		
		try
		{
			L2Proxy.main(); // launches the backend
		}
		catch (Throwable t)
		{
			if (SPLASH_STAGE == null)
			{
				System.exit(-1);
				return;
			}
			Platform.runLater(() ->
			{
				final Alert dlgFail = new ExceptionAlert(t);
				dlgFail.initOwner(SPLASH_STAGE);
				dlgFail.showAndWait();
				Platform.exit();
				System.exit(1);
			});
			return;
		}
		
		if (reporter == null)
			return;
			
		reporter.onState(UIStrings.get("startup.ui"));
		Platform.runLater(() ->
		{
			try
			{
				final FXMLLoader loader = new FXMLLoader(FXLocator.getFXML(MainWindowController.class), UIStrings.getBundle());
				final Scene scene = new Scene(loader.load());
				final MainWindowController controller = loader.getController();
				final Timeline tlLogging = new Timeline(new KeyFrame(Duration.ZERO, evt ->
				{
					for (String msg; (msg = PENDING_LOG_ENTRIES.poll()) != null;)
						controller.appendLogEntry(msg);
				}), new KeyFrame(Duration.seconds(0.2)));
				tlLogging.setCycleCount(Animation.INDEFINITE);
				tlLogging.play();
				PRIMARY_STAGE.setScene(scene);
				PRIMARY_STAGE.setTitle("NetPro" + (NetProInfo.isUnreleased() ? "" : " " + (NetProInfo.isSnapshot() ? "r" + NetProInfo.getRevisionNumber() : NetProInfo.getVersion())));
				PRIMARY_STAGE.getIcons().addAll(FXLocator.getIconListFX());
				PRIMARY_STAGE.show();
			}
			catch (IOException e)
			{
				throw new AssertionError("Main window is missing", e);
			}
			finally
			{
				if (SPLASH_STAGE != null)
				{
					SPLASH_STAGE.hide();
					SPLASH_STAGE = null;
				}
			}
		});
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
}
