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
package net.l2emuproject.proxy.network;

import java.nio.ByteBuffer;

import net.l2emuproject.network.mmocore.PacketHandler;
import net.l2emuproject.proxy.network.packets.GenericReceivedPacket;
import net.l2emuproject.proxy.network.packets.ProxyReceivedPacket;
import net.l2emuproject.proxy.network.packets.ProxyRepeatedPacket;

/**
 * Default handler for proxified packets.<BR>
 * <BR>
 * If a packet doesn't have a custom handler, a simple pass-through handler is returned.
 * 
 * @author savormix
 */
public abstract class ProxyPacketHandler extends PacketHandler<Proxy, ProxyReceivedPacket, ProxyRepeatedPacket>
{
	/**
	 * Chooses an overriding packet handler, if the packet is important/contains important data. <BR>
	 * <BR>
	 * If <TT>null</TT> is returned, a generic handler will be used instead.
	 * 
	 * @param buf a byte buffer containing any further opcodes, and packet data
	 * @param client a client that received the packet
	 * @param opcode the first byte (opcode) of the packet
	 * @return an overriding handler or null
	 */
	protected abstract ProxyReceivedPacket handlePacketImpl(ByteBuffer buf, Proxy client, int opcode);
	
	@Override
	public final ProxyReceivedPacket handlePacket(ByteBuffer buf, Proxy client, int opcode)
	{
		final ProxyReceivedPacket prp = handlePacketImpl(buf, client, opcode);
		return prp != null ? prp : new GenericReceivedPacket(opcode);
	}
}
