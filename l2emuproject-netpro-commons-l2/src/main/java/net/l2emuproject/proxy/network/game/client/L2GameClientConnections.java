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
package net.l2emuproject.proxy.network.game.client;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SocketChannel;

import net.l2emuproject.lang.NetProThreadPriority;
import net.l2emuproject.network.mmocore.MMOConfig;
import net.l2emuproject.proxy.network.AbstractL2ClientConnections;
import net.l2emuproject.proxy.network.game.server.L2GameServerConnections;

/**
 * Manages incoming L2 client connections to the port advertised by this proxy.
 * 
 * @author savormix
 */
public final class L2GameClientConnections extends AbstractL2ClientConnections implements NetProThreadPriority
{
	private static final class SingletonHolder
	{
		static
		{
			final MMOConfig cfg = new MMOConfig("Client[L2]");
			cfg.setAcceptInterval(Integer.getInteger(L2GameClientConnections.class.getName() + "#" + PROPERTY_ACC_INTERVAL, 750));
			cfg.setAcceptPriority(ACCEPTOR_GAME);
			cfg.setIOInterval(Integer.getInteger(L2GameClientConnections.class.getName() + "#" + PROPERTY_RW_INTERVAL, 3));
			cfg.setIOPriority(NETWORK_IO_GAME);
			cfg.setIOThreadCount(1);
			
			try
			{
				INSTANCE = new L2GameClientConnections(cfg);
			}
			catch (IOException e)
			{
				throw new Error(e);
			}
		}
		
		static final L2GameClientConnections INSTANCE;
	}
	
	/**
	 * Returns a singleton object.
	 * 
	 * @return an instance of this class
	 */
	public static final L2GameClientConnections getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	L2GameClientConnections(MMOConfig config) throws IOException
	{
		super(config, L2GameClientPackets.getInstance());
	}
	
	@Override
	protected L2GameClient createClientImpl(SocketChannel socketChannel) throws ClosedChannelException
	{
		L2GameClient lgc = new L2GameClient(this, socketChannel);
		L2GameServerConnections.getInstance().connect(lgc);
		return lgc;
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
