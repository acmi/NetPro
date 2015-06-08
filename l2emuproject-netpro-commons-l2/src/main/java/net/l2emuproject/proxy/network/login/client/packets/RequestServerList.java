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
package net.l2emuproject.proxy.network.login.client.packets;

import static net.l2emuproject.proxy.network.login.client.L2LoginClient.FLAG_SERVER_LIST_C1;
import static net.l2emuproject.proxy.network.login.client.L2LoginClient.FLAG_SERVER_LIST_C2;

import java.nio.BufferUnderflowException;

import net.l2emuproject.network.mmocore.MMOBuffer;
import net.l2emuproject.proxy.network.Packet;
import net.l2emuproject.proxy.network.meta.PacketPayloadEnumerator;
import net.l2emuproject.proxy.network.meta.RandomAccessMMOBuffer;
import net.l2emuproject.proxy.network.meta.RequiredInvasiveOperations;
import net.l2emuproject.proxy.network.meta.SimplePpeProvider;
import net.l2emuproject.proxy.network.meta.exception.InvalidPacketOpcodeSchemeException;
import net.l2emuproject.proxy.network.meta.exception.PartialPayloadEnumerationException;

/**
 * This packet contains the requested game server list type.
 * 
 * @author savormix
 */
public final class RequestServerList extends L2LoginClientPacket implements RequiredInvasiveOperations
{
	/** Packet's identifier */
	public static final int OPCODE = 0x05;
	
	/** Indicates that each game server will have its basic information specified. */
	public static final int TYPE_C0 = 1;
	/** Indicates that each game server will have its type mask specified. */
	public static final int TYPE_C1 = 3;
	/** Indicates that each game server will have its bracket flag specified. */
	public static final int TYPE_C2 = 4;
	
	/** Constructs a packet to extract the ID of the game server to be connected to. */
	public RequestServerList()
	{
		super(OPCODE);
	}
	
	@Override
	protected void readAndModify(MMOBuffer buf, Packet packet) throws BufferUnderflowException, RuntimeException
	{
		int type;
		findType:
		{
			usePPE:
			{
				final PacketPayloadEnumerator ppe = SimplePpeProvider.getPacketPayloadEnumerator();
				if (ppe == null)
					break usePPE;
				
				RandomAccessMMOBuffer enumerator = null;
				try
				{
					enumerator = ppe.enumeratePacketPayload(getClient().getProtocol(), buf, getClient());
				}
				catch (InvalidPacketOpcodeSchemeException e)
				{
					LOG.error("This cannot happen", e);
					break usePPE;
				}
				catch (PartialPayloadEnumerationException e)
				{
					// ignore this due to reasons
					enumerator = e.getBuffer();
				}
				
				type = enumerator.readFirstInteger32(SERVER_LIST_TYPE);
				break findType;
			}
			
			LOG.warn("Using precompiled logic");
			
			buf.readQ(); // session key
			type = buf.readC();
		}
		
		if (type >= TYPE_C1)
		{
			getReceiver().enableProtocolFlags(FLAG_SERVER_LIST_C1);
			if (type >= TYPE_C2)
				getReceiver().enableProtocolFlags(FLAG_SERVER_LIST_C2);
		}
	}
	
	@Override
	protected int getMinimumLength()
	{
		// return READ_Q + READ_C;
		return 0; // due to PPE
	}
}
