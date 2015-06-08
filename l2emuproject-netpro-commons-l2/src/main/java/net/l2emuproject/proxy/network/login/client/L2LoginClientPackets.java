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

import java.nio.ByteBuffer;

import net.l2emuproject.proxy.network.Proxy;
import net.l2emuproject.proxy.network.ProxyPacketHandler;
import net.l2emuproject.proxy.network.login.client.packets.RequestServerList;
import net.l2emuproject.proxy.network.login.client.packets.RequestServerLogin;
import net.l2emuproject.proxy.network.packets.ProxyReceivedPacket;

/**
 * Handles incoming login client packets.
 * 
 * @author savormix
 */
public final class L2LoginClientPackets extends ProxyPacketHandler
{
	@Override
	protected ProxyReceivedPacket handlePacketImpl(ByteBuffer buf, Proxy client, int opcode)
	{
		switch (opcode)
		{
			case RequestServerList.OPCODE:
				return new RequestServerList();
			case RequestServerLogin.OPCODE:
				return new RequestServerLogin();
			default:
				return null;
		}
	}
	
	L2LoginClientPackets()
	{
		// singleton
	}
	
	/**
	 * Returns a singleton instance of this type.
	 * 
	 * @return an instance of this class
	 */
	public static final L2LoginClientPackets getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static final class SingletonHolder
	{
		static final L2LoginClientPackets INSTANCE = new L2LoginClientPackets();
	}
}
