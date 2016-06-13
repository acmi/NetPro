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
package net.l2emuproject.proxy.network.login.client;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SocketChannel;

import net.l2emuproject.network.mmocore.MMOConfig;
import net.l2emuproject.proxy.network.AbstractL2ClientConnections;
import net.l2emuproject.proxy.network.login.server.L2LoginServerConnections;

/**
 * Manages incoming L2 client connections.
 * 
 * @author savormix
 */
public final class L2LoginClientConnections extends AbstractL2ClientConnections
{
	private static final class SingletonHolder
	{
		static
		{
			final MMOConfig cfg = new MMOConfig("LC Proxy");
			// this application might be run on user-class machines, so go easy on the CPU
			cfg.setAcceptInterval(Integer.getInteger(L2LoginClientConnections.class.getName() + "#" + PROPERTY_ACC_INTERVAL, 2_500));
			cfg.setIOInterval(Integer.getInteger(L2LoginClientConnections.class.getName() + "#" + PROPERTY_RW_INTERVAL, 100));
			cfg.setIOThreadCount(1);
			
			try
			{
				INSTANCE = new L2LoginClientConnections(cfg);
			}
			catch (IOException e)
			{
				throw new Error(e);
			}
		}
		
		static final L2LoginClientConnections INSTANCE;
	}
	
	/**
	 * Returns a singleton object.
	 * 
	 * @return an instance of this class
	 */
	public static final L2LoginClientConnections getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	L2LoginClientConnections(MMOConfig config) throws IOException
	{
		super(config, L2LoginClientPackets.getInstance());
	}
	
	@Override
	protected L2LoginClient createClientImpl(SocketChannel socketChannel) throws ClosedChannelException
	{
		L2LoginClient lc = new L2LoginClient(this, socketChannel);
		L2LoginServerConnections.getInstance().connect(lc, socketChannel);
		return lc;
	}
	
	@Override
	public int getBlockingImmutablePacketProcessingWarnThreshold()
	{
		return 20;
	}
	
	@Override
	public int getBlockingMutablePacketProcessingWarnThreshold()
	{
		return 50;
	}
}
