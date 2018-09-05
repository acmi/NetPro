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
package net.l2emuproject.proxy.network.login.server.packets;

import static net.l2emuproject.network.protocol.LoginProtocolVersion.TRANSFER_C3;
import static net.l2emuproject.network.security.LoginCipher.READ_ONLY_C3_C4_TRANSFER_KEY;
import static net.l2emuproject.network.security.LoginCipher.READ_ONLY_PRELUDE_KEY;

import java.nio.BufferUnderflowException;
import java.util.Collections;

import net.l2emuproject.network.mmocore.MMOBuffer;
import net.l2emuproject.network.protocol.ILoginProtocolVersion;
import net.l2emuproject.network.protocol.LoginProtocolVersion;
import net.l2emuproject.network.protocol.ProtocolVersionManager;
import net.l2emuproject.proxy.network.Packet;
import net.l2emuproject.proxy.network.meta.EnumeratedPayloadField;
import net.l2emuproject.proxy.network.meta.PacketPayloadEnumerator;
import net.l2emuproject.proxy.network.meta.RandomAccessMMOBuffer;
import net.l2emuproject.proxy.network.meta.RequiredInvasiveOperations;
import net.l2emuproject.proxy.network.meta.SimplePpeProvider;
import net.l2emuproject.proxy.network.meta.exception.InvalidPacketOpcodeSchemeException;
import net.l2emuproject.proxy.network.meta.exception.PartialPayloadEnumerationException;

/**
 * This packet contains the blowfish key used in further communications.<BR>
 * <BR>
 * It also contains the public RSA key used to encipher authorization details. However, time needed
 * to find the private key makes it worthless.<BR>
 * <BR>
 * Technically you could substitute it with your own key when sending to client, decipher client's info
 * and re-encipher with server's key. However, the proxy does not need that data to operate.<BR>
 * See the FastLogin example script for further details on credential manipulation.
 * 
 * @author savormix
 */
public class SetEncryption extends L2LoginServerPacket implements RequiredInvasiveOperations
{
	/** Packet's identifier */
	public static final int OPCODE = 0x00;
	
	/** Constructs a packet to extract the new Blowfish key. */
	public SetEncryption()
	{
		super(OPCODE);
	}
	
	@Override
	protected void readAndModify(MMOBuffer buf, Packet packet) throws BufferUnderflowException, RuntimeException
	{
		// the biggest FAIL is here: we cannot know what the version is to pick a relevant packet definition
		// unless we read those two fields. We shall hope for the best :)
		buf.readD(); // session ID
		final ILoginProtocolVersion protocol;
		findProtocol:
		{
			int version = buf.readD(); // protocol version
			// TODO: check with later AuthDs
			if (version == 0)
				version = LoginProtocolVersion.PRELUDE_BETA.getVersion();
			final ILoginProtocolVersion ver = ProtocolVersionManager.getInstance().getLoginProtocol(version, Collections.emptySet());
			if (ver != null)
			{
				getRecipient().setVersion(protocol = ver);
				break findProtocol;
			}
			
			LOG.warn("Server will use unsupported " + version + " protocol, defaulting to " + (protocol = getRecipient().getProtocol()));
		}
		
		// initially, set the key as if it was preshared (legacy mode)
		byte[] blowfishKey = protocol.isNewerThanOrEqualTo(TRANSFER_C3) ? READ_ONLY_C3_C4_TRANSFER_KEY : READ_ONLY_PRELUDE_KEY;
		findKey:
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
				
				final EnumeratedPayloadField ppf = enumerator.getSingleFieldIndex(BLOWFISH_KEY);
				if (ppf != null)
					blowfishKey = enumerator.readBytes(ppf);
				
				break findKey;
			}
			
			LOG.warn("Using precompiled logic");
			
			if (buf.getAvailableBytes() < 128 + 16 + 16)
				break findKey;
			
			buf.skip(128 + 16);
			blowfishKey = buf.readB(16);
		}
		
		getReceiver().initCipher(blowfishKey);
		getRecipient().initCipher(blowfishKey);
	}
	
	@Override
	protected int getMinimumLength()
	{
		//return READ_D + READ_D + 128 + 16 + 16 + READ_C;
		return 0; // due to PPE
	}
}
