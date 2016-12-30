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

import java.nio.ByteBuffer;

import net.l2emuproject.network.mmocore.MMOLogger;
import net.l2emuproject.network.protocol.ProtocolVersionManager;
import net.l2emuproject.proxy.network.Proxy;
import net.l2emuproject.proxy.network.ProxyPacketHandler;
import net.l2emuproject.proxy.network.game.client.packets.SendProtocolVersion;
import net.l2emuproject.proxy.network.packets.ProxyReceivedPacket;
import net.l2emuproject.util.HexUtil;

/**
 * Handles incoming game client packets.
 * 
 * @author savormix
 */
public final class L2GameClientPackets extends ProxyPacketHandler
{
	private static final MMOLogger LOG = new MMOLogger(L2GameClientPackets.class, 1_000);
	
	@Override
	protected ProxyReceivedPacket handlePacketImpl(ByteBuffer buf, Proxy client, int opcode)
	{
		// proxy only does what it must do
		// all other invasive ops can be done via scripts
		if (((L2GameClient)client).isHandshakeDone())
			return null;
		
		// FIXME: primary option should be enumerate and deal with it?
		// alternatively, since SPV has undergone various changes and opcodes seem to be static for now
		// we handle those two ops, and enumerate by default, expecting event server
		
		switch (opcode)
		{
			case SendProtocolVersion.OPCODE:
			case SendProtocolVersion.OPCODE_LEGACY:
				return new SendProtocolVersion(opcode);
			default:
				// dimensional server inherits protocol version from origin session
				if (client.getProtocol() == ProtocolVersionManager.getInstance().getFallbackProtocolGame())
					LOG.info("Possible ProtocolVersion opcode: 0x" + HexUtil.fillHex(opcode, 2));
				return null;
		}
	}
	
	L2GameClientPackets()
	{
		// singleton
	}
	
	/**
	 * Returns a singleton instance of this type.
	 * 
	 * @return an instance of this class
	 */
	public static final L2GameClientPackets getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static final class SingletonHolder
	{
		static final L2GameClientPackets INSTANCE = new L2GameClientPackets();
	}
}
