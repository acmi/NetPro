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
package net.l2emuproject.proxy.ui.savormix.loader;

import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.SplashScreen;
import java.awt.Window;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayDeque;
import java.util.Locale;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import net.l2emuproject.proxy.L2Proxy;
import net.l2emuproject.proxy.io.definitions.VersionnedPacketTable;
import net.l2emuproject.proxy.script.LogLoadScriptManager;
import net.l2emuproject.proxy.script.PpeEnabledLoaderScriptRegistry;
import net.l2emuproject.proxy.script.ScriptManager;
import net.l2emuproject.proxy.script.analytics.LiveUserAnalytics;
import net.l2emuproject.proxy.script.analytics.ObjectAnalytics;
import net.l2emuproject.proxy.script.analytics.ObjectLocationAnalytics;
import net.l2emuproject.proxy.script.analytics.PledgeAnalytics;
import net.l2emuproject.proxy.script.game.HighLevelEventGenerator;
import net.l2emuproject.proxy.script.game.PpeGameScriptRegistry;
import net.l2emuproject.proxy.setup.IPAliasManager;
import net.l2emuproject.proxy.setup.SettingsManager;
import net.l2emuproject.proxy.ui.savormix.component.ConnectionPane;
import net.l2emuproject.proxy.ui.savormix.component.SplashFrame;
import net.l2emuproject.util.L2Utils;
import net.l2emuproject.util.logging.L2Logger;
import net.l2emuproject.util.logging.ListeningLog;

import javafx.application.Platform;

/**
 * Loads the GUI without pre-initializing LogManager (see {@link java.awt.Component} static
 * initializers).<BR>
 * <BR>
 * This class is a part of a reference GUI provided for education purposes only. These classes
 * should help to understand how to interact with the underlying proxy core. <BR>
 * <BR>
 * Creating an own GUI is the preferred way to start using this application.
 * 
 * @author savormix
 */
@SuppressWarnings("restriction")
public final class Loader
{
	static Frontend ACTIVE_FRONTEND;
	static ConnectionPane ACTIVE_UI_PANE;
	static Queue<String> LOG_MESSAGES;
	private static final int UNPROCESSED_LOG_MESSAGE_LIMIT = 1_000;
	
	private Loader()
	{
		// application entry point class
	}
	
	/**
	 * Returns the primary window of the active GUI.
	 * 
	 * @return main GUI window
	 */
	public static Frontend getActiveFrontend()
	{
		return ACTIVE_FRONTEND;
	}
	
	/**
	 * Returns a tabbed pane which keeps track of connections to this proxy.
	 * 
	 * @return connection pane
	 */
	public static ConnectionPane getActiveUIPane()
	{
		return ACTIVE_UI_PANE;
	}
	
	/**
	 * Returns cached log entries to be shown in the GUI.
	 * 
	 * @return log entries
	 */
	public static Queue<String> getLogMessages()
	{
		return LOG_MESSAGES;
	}
	
	// TODO: support i18n
	/**
	 * Returns the locale to be used when displaying labeled control elements.
	 * 
	 * @return locale for GUI i18n
	 */
	public static Locale getLocale()
	{
		return Locale.getDefault();
	}
	
	/**
	 * Launches the proxy with a GUI managed by savormix.
	 * 
	 * @param args
	 *            command line arguments, governed by {@link LoadOption}
	 */
	public static void main(String[] args)
	{
		for (final String arg : args)
		{
			final LoadOption lo = LoadOption.getByAlias(arg);
			if (lo == null)
			{
				if (LoadOption.IGNORE_UNKNOWN.isSet())
					continue;
				
				System.err.println("Unrecognized command line argument: " + arg);
				System.exit(1);
				return;
			}
			
			switch (lo)
			{
				case HELP:
					final StringBuilder sb = new StringBuilder();
					for (final LoadOption opt : LoadOption.values())
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
				// throw new InternalError("Unhandled load option: " + lo);
			}
		}
		
		if (GraphicsEnvironment.isHeadless()) // a fool's hope
			LoadOption.DISABLE_UI.setSystemProperty();
		
		if (LoadOption.DISABLE_PROXY.isSet() && LoadOption.DISABLE_UI.isSet())
			System.exit(0);
		
		if (LoadOption.DISABLE_UI.isNotSet())
			Platform.setImplicitExit(false);
		
		final AtomicReference<Window> surrogateCallable = new AtomicReference<>();
		if (LoadOption.DISABLE_UI.isNotSet() && LoadOption.HIDE_SPLASH.isNotSet())
		{
			try
			{
				SwingUtilities.invokeAndWait(() ->
				{
					final SplashScreen bareImage = SplashScreen.getSplashScreen();
					final Window splash;
					surrogateCallable.set(splash = new SplashFrame());
					if (bareImage != null)
					{
						final Rectangle old = bareImage.getBounds();
						final Rectangle current = splash.getBounds();
						current.x = old.x;
						current.y = old.y;
						splash.setBounds(current);
					}
					splash.setVisible(true);
				});
			}
			catch (InvocationTargetException | InterruptedException e)
			{
				System.exit(1);
			}
		}
		
		if (LoadOption.DISABLE_UI.isNotSet() && LoadOption.HIDE_LOG_CONSOLE.isNotSet())
		{
			LOG_MESSAGES = new ArrayDeque<>(UNPROCESSED_LOG_MESSAGE_LIMIT);
			// TODO: this does not work with MMOLogger
			ListeningLog.addListener(message ->
			{
				final boolean added = LOG_MESSAGES.offer(message);
				if (!added || LOG_MESSAGES.size() > UNPROCESSED_LOG_MESSAGE_LIMIT)
				{
					LOG_MESSAGES.clear();
					// LOG_MESSAGES.offer(message);
					LOG_MESSAGES.offer("LOG TRUNCATED");
				}
			});
		}
		else
			LOG_MESSAGES = new ArrayDeque<>(0);
		
		{
			L2Proxy.addStartupHook(() ->
			{
				if (LoadOption.DISABLE_UI.isNotSet())
					LogLoadScriptManager.getInstance().addScript(PpeEnabledLoaderScriptRegistry.getInstance());
				if (LoadOption.DISABLE_PROXY.isNotSet())
				{
					// only useful LIVE
					ScriptManager.getInstance().addScript(PpeGameScriptRegistry.getInstance());
					LiveUserAnalytics.getInstance().onLoad();
					new ObjectLocationAnalytics().onLoad();
					HighLevelEventGenerator.getInstance().onLoad();
				}
				new ObjectAnalytics().onLoad();
				new PledgeAnalytics().onLoad();
				/*
				final NetProScriptCache cache = NetProScriptCache.getInstance();
				if (LoadOption.DISABLE_SCRIPTS.isNotSet() && (ProxyConfig.DISABLE_SCRIPT_CACHE || !cache.restoreFromCache()) && !cache.isCompilerUnavailable())
				{
					cache.compileAllScripts();
					cache.writeToCache();
				}
				*/
			});
			L2Proxy.addStartupHook(VersionnedPacketTable::getInstance);
			L2Proxy.addStartupHook(IPAliasManager::getInstance);
			try
			{
				L2Proxy.main(); // launches the backend
			}
			catch (Throwable t)
			{
				JOptionPane.showMessageDialog(null, t.getMessage(), t.getClass().getSimpleName(), JOptionPane.ERROR_MESSAGE);
				System.exit(1);
			}
		}
		
		if (LoadOption.DISABLE_UI.isNotSet())
		{
			final L2Logger log = L2Logger.getLogger(UILoader.class);
			L2Utils.printSection("Admin UI");
			log.info("Initializing...");
			try
			{
				SwingUtilities.invokeAndWait(new UILoader(surrogateCallable.get()));
			}
			catch (Exception e)
			{
				log.error("Unable to start UI.", e);
				return;
			}
			log.info("Done.");
		}
	}
	
	private static final class UILoader implements Runnable
	{
		private final Window _splash;
		
		public UILoader(Window splash)
		{
			_splash = splash;
		}
		
		@Override
		public void run()
		{
			if (System.getProperty("swing.defaultlaf") == null)
			{
				try
				{
					// Launch with user preferred
					UIManager.setLookAndFeel(SettingsManager.getInstance().getLookAndFeelName());
				}
				catch (Exception e)
				{
					try
					{
						// Default to native
						UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
					}
					catch (Exception e2)
					{
						// whatever
					}
				}
			}
			
			JFrame.setDefaultLookAndFeelDecorated(true);
			
			ACTIVE_FRONTEND = new Frontend();
			ACTIVE_FRONTEND.setVisible(true);
			if (_splash != null)
			{
				_splash.setVisible(false);
				_splash.dispose();
			}
		}
	}
}
