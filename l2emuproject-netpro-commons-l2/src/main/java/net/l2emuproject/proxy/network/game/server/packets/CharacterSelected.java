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

import java.nio.BufferUnderflowException;

import net.l2emuproject.network.ClientProtocolVersion;
import net.l2emuproject.network.mmocore.MMOBuffer;
import net.l2emuproject.proxy.network.Packet;
import net.l2emuproject.proxy.network.game.client.L2GameClient;
import net.l2emuproject.proxy.network.meta.PacketPayloadEnumerator;
import net.l2emuproject.proxy.network.meta.RandomAccessMMOBuffer;
import net.l2emuproject.proxy.network.meta.RequiredInvasiveOperations;
import net.l2emuproject.proxy.network.meta.SimplePpeProvider;
import net.l2emuproject.proxy.network.meta.exception.InvalidPacketOpcodeSchemeException;
import net.l2emuproject.proxy.network.meta.exception.PartialPayloadEnumerationException;

/**
 * This packet contains the in-world client packet obfuscation key.
 * 
 * @author savormix
 */
public class CharacterSelected extends L2GameServerPacket implements RequiredInvasiveOperations
{
	/** Packet's identifier */
	public static final int OPCODE = 0x0B;
	
	/** Constructs a packet to extract the in-world obfuscation key. */
	public CharacterSelected()
	{
		super(OPCODE);
	}
	
	@Override
	protected void readAndModify(MMOBuffer buf, Packet packet) throws BufferUnderflowException, RuntimeException
	{
		final L2GameClient client = getRecipient();
		// no obfuscation before HB
		if (!client.getProtocol().isNewerThanOrEqualTo(ClientProtocolVersion.HELLBOUND))
		{
			// client.getDeobfuscator().init(0);
			return;
		}
		
		long seed = 0L;
		findSS: try
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
				
				seed = enumerator.readFirstInteger(OBFUSCATION_KEY);
				break findSS;
			}
			
			LOG.warn("Using precompiled logic");
			
			// FIXME: review this
			
			buf.readS(); // char name
			buf.readD(); // char ID
			buf.readS(); // title
			
			buf.skip(132); // char data
			if (client.getProtocol().isNewerThanOrEqualTo(ClientProtocolVersion.VALIANCE))
				buf.skip(4); // we don't need them, so we don't need to know the exact position
				
			buf.skip(64); // unk 0s
			
			seed = buf.readD();
		}
		catch (RuntimeException e)
		{
			LOG.error("CM opcode shuffling initialization failed", e);
		}
		client.getDeobfuscator().init(seed);
	}
	
	@Override
	protected int getMinimumLength()
	{
		// return READ_S/* * 2 */+ READ_D + READ_S + 132 + 64 + READ_D;
		return 0; // due to PPE
	}
}
