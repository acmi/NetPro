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

import net.l2emuproject.lang.L2TextBuilder;
import net.l2emuproject.lang.management.ShutdownManager;
import net.l2emuproject.lang.management.TerminationStatus;
import net.l2emuproject.proxy.config.ProxyConfig;
import net.l2emuproject.proxy.network.ListenSocket;
import net.l2emuproject.proxy.network.ProxySocket;
import net.l2emuproject.proxy.network.game.client.L2GameClientConnections;
import net.l2emuproject.proxy.network.game.server.L2GameServerConnections;
import net.l2emuproject.proxy.network.login.client.L2LoginClientConnections;
import net.l2emuproject.proxy.network.login.server.L2LoginServerConnections;
import net.l2emuproject.proxy.setup.SocketManager;
import net.l2emuproject.proxy.ui.savormix.io.AutoLogger;
import net.l2emuproject.proxy.ui.savormix.loader.LoadOption;
import net.l2emuproject.util.L2Utils;
import net.l2emuproject.util.logging.L2Logger;

/**
 * This class contains the application entry point.
 * 
 * @author savormix
 */
public class L2Proxy extends Config
{
	private static final L2Logger LOG = L2Logger.getLogger(L2Proxy.class);
	
	private static Collection<Runnable> STARTUP_HOOKS = new ArrayList<>();
	
	/**
	 * Adds a task to be executed after fundamental initialization, but before starting the proxy server.
	 * 
	 * @param hook an executable task
	 * @throws UnsupportedOperationException if called after startup
	 */
	public static void addStartupHook(Runnable hook) throws UnsupportedOperationException
	{
		synchronized (L2Proxy.class)
		{
			STARTUP_HOOKS.add(hook);
		}
	}
	
	/**
	 * Launches the proxy core.
	 * 
	 * @param args
	 *            ignored, see {@link net.l2emuproject.proxy.Config}
	 */
	public static void main(String... args)
	{
		if (LoadOption.DISABLE_PROXY.isNotSet())
		{
			System.setProperty(L2LoginClientConnections.class.getName() + "#" + PROPERTY_ACC_INTERVAL, String.valueOf(ProxyConfig.ACC_SELECTOR_INTERVAL_LOGIN));
			System.setProperty(L2GameClientConnections.class.getName() + "#" + PROPERTY_ACC_INTERVAL, String.valueOf(ProxyConfig.ACC_SELECTOR_INTERVAL_GAME));
			
			System.setProperty(L2LoginClientConnections.class.getName() + "#" + PROPERTY_RW_INTERVAL, String.valueOf(ProxyConfig.RW_SELECTOR_INTERVAL_LC));
			System.setProperty(L2LoginServerConnections.class.getName() + "#" + PROPERTY_RW_INTERVAL, String.valueOf(ProxyConfig.RW_SELECTOR_INTERVAL_LS));
			System.setProperty(L2GameClientConnections.class.getName() + "#" + PROPERTY_RW_INTERVAL, String.valueOf(ProxyConfig.RW_SELECTOR_INTERVAL_GC));
			System.setProperty(L2GameServerConnections.class.getName() + "#" + PROPERTY_RW_INTERVAL, String.valueOf(ProxyConfig.RW_SELECTOR_INTERVAL_GS));
		}
		
		synchronized (L2Proxy.class)
		{
			for (final Runnable hook : STARTUP_HOOKS)
				hook.run();
			STARTUP_HOOKS = Collections.emptySet();
		}
		
		final L2LoginServerConnections lsc;
		if (LoadOption.DISABLE_PROXY.isNotSet())
		{
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
						lcc.openServerSocket(socket.getBindAddress(), socket.getListenPort());
					}
					catch (IOException e)
					{
						it.remove();
						LOG.error("Could not start listening on " + socket, e);
					}
				}
				lcc.start();
				for (final ListenSocket socket : sm.getGameWorldSockets().values())
				{
					try
					{
						gcc.openServerSocket(socket.getBindAddress(), socket.getListenPort());
					}
					catch (BindException e)
					{
						if (!e.getMessage().startsWith("Cannot assign requested address"))
							//	LOG.info("Not a local adapter address: " + socket.getBindAddress());
							//else
							LOG.error("Failed binding on " + socket, e);
					}
				}
				gcc.start();
			}
			catch (Throwable e)
			{
				LOG.fatal("Could not start proxy!", e);
				ShutdownManager.exit(TerminationStatus.RUNTIME_INITIALIZATION_FAILURE);
				return;
			}
			
			{
				final AutoLogger al = AutoLogger.getInstance();
				lcc.addConnectionListener(al);
				lcc.addPacketListener(al);
				lsc.addPacketListener(al);
				gcc.addConnectionListener(al);
				gcc.addPacketListener(al);
				L2GameServerConnections.getInstance().addPacketListener(al);
			}
		}
		else
			lsc = null;
		
		applicationLoaded("l2emuproject-netpro-app", ProxyInfo.getFullVersionInfo(), false);
		
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
			LOG.info(tb.moveToString());
			
			ShutdownManager.addShutdownHook(new ShutdownHelper());
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
