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
package net.l2emuproject.proxy.network.game.server;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SocketChannel;
import java.util.Arrays;

import net.l2emuproject.network.mmocore.MMOConfig;
import net.l2emuproject.proxy.network.AbstractL2ServerConnections;
import net.l2emuproject.proxy.network.game.L2GameServerInfo;
import net.l2emuproject.proxy.network.game.L2SessionManager;
import net.l2emuproject.proxy.network.game.client.L2GameClient;

/**
 * Manages outgoing connections to game servers initiated when a L2 client connects to this proxy on
 * the advertised port.
 * 
 * @author savormix
 */
public final class L2GameServerConnections extends AbstractL2ServerConnections
{
	private static final class SingletonHolder
	{
		static
		{
			final MMOConfig cfg = new MMOConfig("GS Proxy");
			cfg.setKeepAlive(true);
			// this app is not likely to serve thousands of connections
			// so we can try to minimize the latency caused by proxying the connection
			cfg.setReadWriteSelectorSleepTime(Integer.getInteger(L2GameServerConnections.class.getName() + "#" + PROPERTY_RW_INTERVAL, 3));
			cfg.setThreadCount(1);
			
			try
			{
				INSTANCE = new L2GameServerConnections(cfg);
			}
			catch (IOException e)
			{
				throw new Error(e);
			}
		}
		
		static final L2GameServerConnections INSTANCE;
	}
	
	/**
	 * Returns a singleton object.
	 * 
	 * @return an instance of this class
	 */
	public static final L2GameServerConnections getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	L2GameServerConnections(MMOConfig config) throws IOException
	{
		super(config, L2GameServerPackets.getInstance());
	}
	
	/**
	 * Connects to a game server on behalf of <TT>client</TT>.
	 * 
	 * @param client connection initiator
	 */
	public void connect(L2GameClient client)
	{
		if (client == null)
			return;
			
		final L2GameServerInfo gsi = L2SessionManager.getInstance().getRoute(client);
		if (gsi == null)
		{
			LOG.warn("Can't find target game server for " + client.getHostAddress());
			L2SessionManager.getInstance().describeExistingRoutes();
			client.notifyFailure();
			return;
		}
		
		//LOG.info("Found target GS on port " + gsi.getPort());
		
		final InetAddress address;
		try
		{
			address = InetAddress.getByAddress(gsi.getIPv4());
			//LOG.info("Found target GS, connecting to: " + address);
			
			connectProxy(client, address, gsi.getPort());
		}
		catch (Exception e) // UnknownHostException, RuntimeException
		{
			LOG.error("Cannot connect to " + Arrays.toString(gsi.getIPv4()), e);
			client.closeNow();
		}
	}
	
	@Override
	protected L2GameServer createClientImpl(SocketChannel socketChannel) throws ClosedChannelException
	{
		final L2GameClient lgc = takeClient();
		return new L2GameServer(this, socketChannel, lgc);
	}
	
	@Override
	public int getBlockingImmutablePacketProcessingWarnThreshold()
	{
		return 1;
	}
	
	@Override
	public int getBlockingMutablePacketProcessingWarnThreshold()
	{
		return 3;
	}
}
