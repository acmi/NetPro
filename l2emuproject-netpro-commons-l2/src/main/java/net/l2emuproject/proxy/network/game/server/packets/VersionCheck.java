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
import net.l2emuproject.network.IGameProtocolVersion;
import net.l2emuproject.network.mmocore.MMOBuffer;
import net.l2emuproject.proxy.network.Packet;
import net.l2emuproject.proxy.network.game.client.L2GameClient;
import net.l2emuproject.proxy.network.meta.EnumeratedPayloadField;
import net.l2emuproject.proxy.network.meta.PacketPayloadEnumerator;
import net.l2emuproject.proxy.network.meta.RandomAccessMMOBuffer;
import net.l2emuproject.proxy.network.meta.RequiredInvasiveOperations;
import net.l2emuproject.proxy.network.meta.SimplePpeProvider;
import net.l2emuproject.proxy.network.meta.exception.InvalidPacketOpcodeSchemeException;
import net.l2emuproject.proxy.network.meta.exception.PartialPayloadEnumerationException;

/**
 * This packet contains the cipher key part sent to a L2 client and the character management
 * obfuscation key.
 * 
 * @author savormix
 */
public final class VersionCheck extends L2GameServerPacket implements RequiredInvasiveOperations
{
	/** Packet's identifier */
	public static final int OPCODE = 0x2E;
	/** Packet's legacy identifier */
	public static final int OPCODE_LEGACY = 0x00;
	
	/**
	 * Constructs a packet to extract cipher and obfuscation keys.
	 * 
	 * @param opcode opcode to use when forwarding to client
	 */
	public VersionCheck(int opcode)
	{
		super(opcode);
	}
	
	@Override
	protected void readAndModify(MMOBuffer buf, Packet packet) throws BufferUnderflowException, RuntimeException
	{
		final L2GameClient client = getRecipient();
		final IGameProtocolVersion cpv = client.getProtocol();
		boolean cipherEnabled = false;
		long cipherKeyPart, shuffleSeed = 0;
		
		findInitialValues:
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
				
				cipherKeyPart = enumerator.readFirstInteger(CIPHER_KEY_PART);
				cipherEnabled = enumerator.readFirstInteger32(CIPHER_STATE) != 0;
				
				final EnumeratedPayloadField field = enumerator.getSingleFieldIndex(OBFUSCATION_KEY);
				shuffleSeed = field != null ? enumerator.readInteger(field) : 0;
				break findInitialValues;
			}
			
			LOG.warn("Using precompiled logic");
			
			buf.readC(); // is compatible
			
			cipherKeyPart = cpv.isNewerThanOrEqualTo(ClientProtocolVersion.INTERLUDE) ? buf.readQ() : buf.readD();
			cipherEnabled = buf.readD() != 0;
			
			if (cpv.isNewerThanOrEqualTo(ClientProtocolVersion.HELLBOUND))
			{
				buf.skip(4 + 1); // unk, game server ID, unk
				
				shuffleSeed = buf.readD();
			}
		}
		
		if (cipherEnabled)
		{
			getReceiver().initCipher(cipherKeyPart);
			client.initCipher(cipherKeyPart);
		}
		client.getDeobfuscator().init(shuffleSeed);
	}
	
	@Override
	protected int getMinimumLength()
	{
		//return READ_C + 8 + 9 + READ_D;
		return 0; // due to PPE
	}
}
