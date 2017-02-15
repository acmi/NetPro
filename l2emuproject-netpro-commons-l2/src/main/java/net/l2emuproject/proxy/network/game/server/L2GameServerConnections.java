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
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SocketChannel;

import net.l2emuproject.lang.NetProThreadPriority;
import net.l2emuproject.network.mmocore.MMOConfig;
import net.l2emuproject.proxy.network.AbstractL2ServerConnections;
import net.l2emuproject.proxy.network.game.L2SessionManager;
import net.l2emuproject.proxy.network.game.NewGameServerConnection;
import net.l2emuproject.proxy.network.game.client.L2GameClient;
import net.l2emuproject.proxy.network.game.client.L2GameClientConnections;

/**
 * Manages outgoing connections to game servers initiated when a L2 client connects to this proxy on
 * the advertised port.
 * 
 * @author savormix
 */
public final class L2GameServerConnections extends AbstractL2ServerConnections implements NetProThreadPriority
{
	private static final class SingletonHolder
	{
		static
		{
			final MMOConfig cfg = new MMOConfig("Server[L2]");
			cfg.setConnectCompletionInterval(50);
			cfg.setConnectPriority(CONNECTOR_GAME);
			cfg.setIOInterval(Integer.getInteger(L2GameClientConnections.class.getName() + "#" + PROPERTY_RW_INTERVAL, 3));
			cfg.setIOPriority(NETWORK_IO_GAME);
			cfg.setIOThreadCount(1);
			
			try
			{
				INSTANCE = new L2GameServerConnections(cfg);
			}
			catch (final IOException e)
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
	 * Connects to a game server on behalf of {@code client}.
	 * 
	 * @param client connection initiator
	 */
	public void connect(L2GameClient client)
	{
		if (client == null)
			return;
		
		final NewGameServerConnection authorizedSession = L2SessionManager.getInstance().getAuthorizedSession(client.getInetAddress());
		if (authorizedSession == null)
		{
			LOG.info("Unsolicited connection from " + client.getHostAddress());
			client.notifyFailure();
			return;
		}
		
		LOG.info("Handed over session: " + authorizedSession);
		try
		{
			final L2GameClient originSession = authorizedSession.getUnderlyingConnection();
			if (originSession != null)
				client.setVersion(originSession.getProtocol());
			connectProxy(client, authorizedSession.getAddress());
		}
		catch (final Exception e) // RuntimeException
		{
			LOG.error("Cannot connect to " + authorizedSession.getAddress(), e);
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
