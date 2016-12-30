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

import net.l2emuproject.network.mmocore.MMOBuffer;
import net.l2emuproject.proxy.network.Packet;
import net.l2emuproject.proxy.network.game.client.L2GameClient;
import net.l2emuproject.proxy.network.game.server.L2GameServerPackets;
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
public final class ExShuffleSeedAndPublicKey extends L2GameServerPacket implements RequiredInvasiveOperations
{
	/** Packet's extended identifier */
	public static final int OPCODE2 = 0x01_38;
	
	/**
	 * Constructs a packet to extract cipher and obfuscation keys.
	 */
	public ExShuffleSeedAndPublicKey()
	{
		super(L2GameServerPackets.OPCODE_FOR_OP2);
	}
	
	@Override
	protected void readAndModify(MMOBuffer buf, Packet packet) throws BufferUnderflowException, RuntimeException
	{
		final L2GameClient client = getRecipient();
		
		final PacketPayloadEnumerator ppe = SimplePpeProvider.getPacketPayloadEnumerator();
		if (ppe == null)
			return;
		
		RandomAccessMMOBuffer enumerator = null;
		try
		{
			enumerator = ppe.enumeratePacketPayload(client.getProtocol(), buf, client);
		}
		catch (final InvalidPacketOpcodeSchemeException e)
		{
			LOG.error("This cannot happen", e);
			return;
		}
		catch (final PartialPayloadEnumerationException e)
		{
			// ignore this due to reasons
			enumerator = e.getBuffer();
		}
		
		final long shuffleSeed = enumerator.readFirstInteger(OBFUSCATION_KEY), cipherKeyPart = enumerator.readFirstInteger(CIPHER_KEY_PART);
		getReceiver().initCipher(cipherKeyPart);
		client.initCipher(cipherKeyPart);
		if (shuffleSeed != 0)
			client.getDeobfuscator().init(shuffleSeed);
	}
	
	@Override
	protected int getMinimumLength()
	{
		return 0; // due to PPE
	}
}
