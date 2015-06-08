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
package net.l2emuproject.proxy.network.game.server.packets;

import net.l2emuproject.proxy.network.game.client.L2GameClient;
import net.l2emuproject.proxy.network.game.server.L2GameServer;
import net.l2emuproject.proxy.network.packets.ProxyReceivedPacket;

/**
 * A wrapper class representing a packet sent by a game server to a L2 client.
 * 
 * @author savormix
 */
public abstract class L2GameServerPacket extends ProxyReceivedPacket
{
	/**
	 * Constructs a game server packet that is ready for forwarding.
	 * 
	 * @param opcode main opcode
	 */
	protected L2GameServerPacket(int opcode)
	{
		super(opcode);
	}
	
	@Override
	protected L2GameServer getReceiver()
	{
		return (L2GameServer)super.getReceiver();
	}
	
	@Override
	protected L2GameClient getRecipient()
	{
		return (L2GameClient)super.getRecipient();
	}
}
