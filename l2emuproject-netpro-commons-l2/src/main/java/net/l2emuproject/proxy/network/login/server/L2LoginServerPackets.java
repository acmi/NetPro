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
package net.l2emuproject.proxy.network.login.server;

import java.nio.ByteBuffer;

import net.l2emuproject.proxy.network.Proxy;
import net.l2emuproject.proxy.network.ProxyPacketHandler;
import net.l2emuproject.proxy.network.login.server.packets.SetEncryption;
import net.l2emuproject.proxy.network.login.server.packets.PlayOk;
import net.l2emuproject.proxy.network.login.server.packets.ServerList;
import net.l2emuproject.proxy.network.packets.ProxyReceivedPacket;

/**
 * Handles incoming login server packets.
 * 
 * @author savormix
 */
public final class L2LoginServerPackets extends ProxyPacketHandler
{
	@Override
	protected ProxyReceivedPacket handlePacketImpl(ByteBuffer buf, Proxy server, int opcode)
	{
		switch (opcode)
		{
			case SetEncryption.OPCODE:
				return new SetEncryption();
			case ServerList.OPCODE:
				return new ServerList();
			case PlayOk.OPCODE:
				return new PlayOk();
			default:
				return null;
		}
	}
	
	L2LoginServerPackets()
	{
		// singleton
	}
	
	/**
	 * Returns a singleton instance of this type.
	 * 
	 * @return an instance of this class
	 */
	public static final L2LoginServerPackets getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static final class SingletonHolder
	{
		static final L2LoginServerPackets INSTANCE = new L2LoginServerPackets();
	}
}
