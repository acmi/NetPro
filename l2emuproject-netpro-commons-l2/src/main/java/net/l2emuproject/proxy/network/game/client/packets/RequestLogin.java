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
package net.l2emuproject.proxy.network.game.client.packets;

import java.nio.BufferUnderflowException;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import net.l2emuproject.network.mmocore.MMOBuffer;
import net.l2emuproject.network.protocol.ClientProtocolVersion;
import net.l2emuproject.network.protocol.IGameProtocolVersion;
import net.l2emuproject.network.protocol.ProtocolVersionManager;
import net.l2emuproject.proxy.network.Packet;
import net.l2emuproject.proxy.network.game.L2ServerLocale;
import net.l2emuproject.proxy.network.meta.PacketPayloadEnumerator;
import net.l2emuproject.proxy.network.meta.RandomAccessMMOBuffer;
import net.l2emuproject.proxy.network.meta.RequiredInvasiveOperations;
import net.l2emuproject.proxy.network.meta.SimplePpeProvider;
import net.l2emuproject.proxy.network.meta.exception.InvalidPacketOpcodeSchemeException;
import net.l2emuproject.proxy.network.meta.exception.PartialPayloadEnumerationException;

/**
 * This packet contains the protocol version that L2 client will use for communications.<BR>
 * 
 * @author savormix
 */
public final class RequestLogin extends L2GameClientPacket implements RequiredInvasiveOperations
{
	/** Packet's identifier */
	public static final int OPCODE = 0x2B;
	/** Packet's legacy identifier */
	public static final int OPCODE_LEGACY = 0x08;
	
	/**
	 * Constructs a packet to extract the protocol version to be used
	 * 
	 * @param opcode opcode to use when forwarding to server
	 */
	public RequestLogin(int opcode)
	{
		super(opcode);
	}
	
	@Override
	protected void readAndModify(MMOBuffer buf, Packet packet) throws BufferUnderflowException, RuntimeException
	{
		final IGameProtocolVersion protocol = getReceiver().getProtocol();
		if (protocol.isOlderThan(ClientProtocolVersion.C3_RISE_OF_DARKNESS))
			return;
		
		final int localization;
		findLocale:
		{
			usePPE:
			{
				final PacketPayloadEnumerator ppe = SimplePpeProvider.getPacketPayloadEnumerator();
				if (ppe == null)
					break usePPE;
				
				RandomAccessMMOBuffer enumerator = null;
				try
				{
					enumerator = ppe.enumeratePacketPayload(protocol, buf, getClient());
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
				
				localization = enumerator.readFirstInteger32(LOCALIZATION_TYPE);
				break findLocale;
			}
			
			LOG.warn("Using precompiled logic");
			
			buf.readS(); // account name
			buf.readD(); // account ID
			buf.readD(); // session ID
			buf.readD(); // account ID
			buf.readD(); // auth key
			
			localization = buf.readD();
		}
		
		final Set<String> altModes = new LinkedHashSet<>(getRecipient().getAltModes());
		altModes.addAll(L2ServerLocale.valueOf(localization).getAltModeSet());
		getRecipient().setAltModes(Collections.unmodifiableSet(altModes));
		
		// adjust protocol version accordingly
		IGameProtocolVersion newProtocol = ProtocolVersionManager.getInstance().getGameProtocol(protocol.getVersion(), altModes);
		if (newProtocol != null)
			getReceiver().adjustVersion(newProtocol);
	}
	
	@Override
	protected int getMinimumLength()
	{
		return 0; // due to PPE
	}
}
