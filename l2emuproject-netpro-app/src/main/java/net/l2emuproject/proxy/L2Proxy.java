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

import static net.l2emuproject.proxy.network.AbstractL2ClientConnections.PROPERTY_ACC_INTERVAL;
import static net.l2emuproject.proxy.network.ProxyConnections.PROPERTY_RW_INTERVAL;

import java.io.IOException;
import java.net.BindException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import net.l2emuproject.config.loader.CurrentConfigManager;
import net.l2emuproject.lang.L2TextBuilder;
import net.l2emuproject.lang.management.ShutdownManager;
import net.l2emuproject.lang.management.StartupManager.StartupHook;
import net.l2emuproject.lang.management.TerminationStatus;
import net.l2emuproject.proxy.config.ConfigMarker;
import net.l2emuproject.proxy.config.ProxyConfig;
import net.l2emuproject.proxy.network.ListenSocket;
import net.l2emuproject.proxy.network.ProxySocket;
import net.l2emuproject.proxy.network.game.client.L2GameClientConnections;
import net.l2emuproject.proxy.network.game.server.L2GameServerConnections;
import net.l2emuproject.proxy.network.login.client.L2LoginClientConnections;
import net.l2emuproject.proxy.network.login.server.L2LoginServerConnections;
import net.l2emuproject.proxy.setup.SocketManager;
import net.l2emuproject.proxy.ui.i18n.UIStrings;
import net.l2emuproject.proxy.ui.savormix.io.AutoLogger;
import net.l2emuproject.proxy.ui.savormix.loader.LoadOption;
import net.l2emuproject.util.AppInit;
import net.l2emuproject.util.L2Utils;
import net.l2emuproject.util.concurrent.L2ThreadPool;
import net.l2emuproject.util.concurrent.ThreadPoolInitializer;
import net.l2emuproject.util.logging.L2Logger;

/**
 * This class contains the application entry point.
 * 
 * @author savormix
 */
public class L2Proxy
{
	private static Collection<StartupHook> STARTUP_HOOKS = new ArrayList<>();
	private static StartupStateReporter STATE_REPORTER;
	
	/**
	 * Adds a task to be executed after fundamental initialization, but before starting the proxy server.
	 * 
	 * @param hook an executable task
	 * @throws UnsupportedOperationException if called after startup
	 */
	public static void addStartupHook(StartupHook hook) throws UnsupportedOperationException
	{
		synchronized (L2Proxy.class)
		{
			STARTUP_HOOKS.add(hook);
		}
	}
	
	/**
	 * Sets the current state reporter.
	 * 
	 * @param reporter state reporter
	 */
	public static void setStartupReporter(StartupStateReporter reporter)
	{
		STATE_REPORTER = reporter;
	}
	
	/**
	 * Launches the proxy core.
	 * 
	 * @param args ignored
	 */
	public static void main(String... args)
	{
		if (STATE_REPORTER != null)
			STATE_REPORTER.onState(UIStrings.get("startup.config"));
		AppInit.defaultInit();
		NetProInfo.showStartupInfo();
		
		final L2Logger logger = L2Logger.getLogger(L2Proxy.class);
		logger.debug("Loading configuration…");
		try
		{
			CurrentConfigManager.getInstance().registerAll(ConfigMarker.class.getPackage()).load();
		}
		catch (Exception e)
		{
			logger.fatal("Could not load configurable properties!", e);
			ShutdownManager.exit(TerminationStatus.RUNTIME_INVALID_CONFIGURATION);
			return;
		}
		logger.spam("…SUCCESS");
		
		logger.spam("Setting up thread pools…");
		try
		{
			L2ThreadPool.initThreadPools(new ThreadPoolInitializer()
			{
				// FIXME: this needs to be done eventually
			});
		}
		catch (Exception e)
		{
			logger.fatal("Could not setup thread pools!", e);
			ShutdownManager.exit(TerminationStatus.RUNTIME_INITIALIZATION_FAILURE);
			return;
		}
		logger.spam("…SUCCESS");
		
		if (LoadOption.DISABLE_PROXY.isNotSet())
		{
			logger.spam("Setting mmocore thread sleep interval values…");
			System.setProperty(L2LoginClientConnections.class.getName() + "#" + PROPERTY_ACC_INTERVAL, String.valueOf(ProxyConfig.ACC_SELECTOR_INTERVAL_LOGIN));
			System.setProperty(L2GameClientConnections.class.getName() + "#" + PROPERTY_ACC_INTERVAL, String.valueOf(ProxyConfig.ACC_SELECTOR_INTERVAL_GAME));
			
			System.setProperty(L2LoginClientConnections.class.getName() + "#" + PROPERTY_RW_INTERVAL, String.valueOf(ProxyConfig.RW_SELECTOR_INTERVAL_LC));
			System.setProperty(L2LoginServerConnections.class.getName() + "#" + PROPERTY_RW_INTERVAL, String.valueOf(ProxyConfig.RW_SELECTOR_INTERVAL_LS));
			System.setProperty(L2GameClientConnections.class.getName() + "#" + PROPERTY_RW_INTERVAL, String.valueOf(ProxyConfig.RW_SELECTOR_INTERVAL_GC));
			System.setProperty(L2GameServerConnections.class.getName() + "#" + PROPERTY_RW_INTERVAL, String.valueOf(ProxyConfig.RW_SELECTOR_INTERVAL_GS));
			logger.spam("Sleep intervals set.");
		}
		
		logger.debug("Executing hooks before network init…");
		synchronized (L2Proxy.class)
		{
			for (final StartupHook hook : STARTUP_HOOKS)
			{
				try
				{
					hook.onStartup();
				}
				catch (Exception e)
				{
					logger.fatal("Initialization failure", e);
					ShutdownManager.exit(TerminationStatus.RUNTIME_INITIALIZATION_FAILURE);
					return;
				}
			}
			STARTUP_HOOKS = Collections.emptySet();
		}
		logger.trace("All pre-proxy hooks executed.");
		
		final L2LoginServerConnections lsc;
		if (LoadOption.DISABLE_PROXY.isNotSet())
		{
			if (STATE_REPORTER != null)
				STATE_REPORTER.onState(UIStrings.get("startup.sockets"));
			logger.debug("Setting up sockets…");
			final SocketManager sm = SocketManager.getInstance();
			final L2LoginClientConnections lcc = L2LoginClientConnections.getInstance();
			final L2GameClientConnections gcc = L2GameClientConnections.getInstance();
			
			lsc = L2LoginServerConnections.getInstance();
			lsc.setAdvertisedSockets(sm.getGameWorldSockets());
			lsc.setAuthSockets(sm.getAuthSockets());
			lsc.registerBindingListeners(lcc).start();
			L2GameServerConnections.getInstance().registerBindingListeners(gcc).start();
			
			try
			{
				for (final Iterator<ProxySocket> it = sm.getAuthSockets().iterator(); it.hasNext();)
				{
					final ProxySocket socket = it.next();
					try
					{
						if (logger.isTraceEnabled())
							logger.trace("Opening listen socket " + socket + "…");
						lcc.openServerSocket(socket.getBindAddress(), socket.getListenPort());
						logger.trace("…SUCCESS");
					}
					catch (IOException e)
					{
						it.remove();
						logger.error("Could not start listening on " + socket, e);
					}
				}
				logger.spam("Starting auth multiplexer…");
				lcc.start();
				logger.spam("…SUCCESS");
				for (final ListenSocket socket : sm.getGameWorldSockets().values())
				{
					try
					{
						if (logger.isTraceEnabled())
							logger.trace("Opening listen socket " + socket + "…");
						gcc.openServerSocket(socket.getBindAddress(), socket.getListenPort());
						logger.trace("…SUCCESS");
					}
					catch (BindException e)
					{
						if (!e.getMessage().startsWith("Cannot assign requested address"))
							//	LOG.info("Not a local adapter address: " + socket.getBindAddress());
							//else
							logger.error("Failed binding on " + socket, e);
					}
				}
				logger.spam("Starting main multiplexer…");
				gcc.start();
				logger.spam("…SUCCESS");
			}
			catch (Throwable e)
			{
				logger.fatal("Could not start proxy!", e);
				ShutdownManager.exit(TerminationStatus.RUNTIME_INITIALIZATION_FAILURE);
				return;
			}
			
			if (STATE_REPORTER != null)
				STATE_REPORTER.onState(UIStrings.get("startup.autologger"));
			logger.trace("Setting up automatic packet logging…");
			{
				final AutoLogger al = AutoLogger.getInstance();
				lcc.addConnectionListener(al);
				lcc.addPacketListener(al);
				lsc.addPacketListener(al);
				gcc.addConnectionListener(al);
				gcc.addPacketListener(al);
				L2GameServerConnections.getInstance().addPacketListener(al);
			}
			logger.spam("…SUCCESS");
		}
		else
			lsc = null;
			
		AppInit.defaultPostInit(NetProInfo.getFullVersionInfo());
		
		if (lsc != null)
		{
			L2Utils.printSection("Proxy");
			L2TextBuilder tb = new L2TextBuilder();
			tb.appendNewline("Configuration:");
			
			for (final ProxySocket ps : SocketManager.getInstance().getAuthSockets())
			{
				tb.append("Listening on ").append(ps.getBindAddress()).append(':').append(ps.getListenPort());
				tb.append(", proxied to ").append(ps.getServiceAddress()).append(':').appendNewline(ps.getServicePort());
			}
			
			tb.append("Listed game servers will be changed to (either or):");
			for (final ListenSocket socket : SocketManager.getInstance().getGameWorldSockets().values())
				tb.appendNewline().append(socket.getBindAddress()).append(':').append(socket.getListenPort());
			logger.info(tb.moveToString());
			
			logger.spam("Installing multiplexer shutdown hook…");
			ShutdownManager.addShutdownHook(new ShutdownHelper());
			logger.spam("…SUCCESS");
		}
	}
	
	static final class ShutdownHelper implements Runnable
	{
		private static final L2Logger LOG = L2Logger.getLogger(ShutdownHelper.class);
		
		@Override
		public void run()
		{
			try
			{
				LOG.info("Shutting down the proxy server...");
				
				LOG.info("Proxy login listener...");
				L2LoginClientConnections.getInstance().shutdown();
				LOG.info("Proxy game listener...");
				L2GameClientConnections.getInstance().shutdown();
				LOG.info("Active login server connections...");
				L2LoginServerConnections.getInstance().shutdown();
				LOG.info("Active game server connections...");
				L2GameServerConnections.getInstance().shutdown();
				
				LOG.info("Proxy server has been shut down.");
			}
			catch (Throwable t)
			{
				LOG.warn("Orderly shutdown sequence interrupted", t);
			}
		}
	}
}
