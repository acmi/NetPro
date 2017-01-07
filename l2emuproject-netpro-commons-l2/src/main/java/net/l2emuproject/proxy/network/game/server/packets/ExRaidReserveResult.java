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

import java.net.InetSocketAddress;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.concurrent.TimeUnit;

import net.l2emuproject.network.mmocore.MMOBuffer;
import net.l2emuproject.proxy.network.ListenSocket;
import net.l2emuproject.proxy.network.Packet;
import net.l2emuproject.proxy.network.game.L2GameServerInfo;
import net.l2emuproject.proxy.network.game.L2SessionManager;
import net.l2emuproject.proxy.network.game.L2SessionSetterAsync;
import net.l2emuproject.proxy.network.game.NewGameServerConnection;
import net.l2emuproject.proxy.network.game.client.L2GameClient;
import net.l2emuproject.proxy.network.game.server.L2GameServerPackets;
import net.l2emuproject.proxy.network.login.server.L2LoginServerConnections;
import net.l2emuproject.proxy.network.meta.EnumeratedPayloadField;
import net.l2emuproject.proxy.network.meta.PacketPayloadEnumerator;
import net.l2emuproject.proxy.network.meta.RandomAccessMMOBuffer;
import net.l2emuproject.proxy.network.meta.RequiredInvasiveOperations;
import net.l2emuproject.proxy.network.meta.SimplePpeProvider;
import net.l2emuproject.proxy.network.meta.exception.InvalidPacketOpcodeSchemeException;
import net.l2emuproject.proxy.network.meta.exception.PartialPayloadEnumerationException;
import net.l2emuproject.util.concurrent.L2ThreadPool;

/**
 * This packet contains information about a dimensional server session.
 * 
 * @author savormix
 */
public final class ExRaidReserveResult extends L2GameServerPacket implements RequiredInvasiveOperations
{
	/** Packet's extended identifier */
	public static final int OPCODE2 = 0x00_B7;
	
	/**
	 * Constructs a packet to extract cipher and obfuscation keys.
	 */
	public ExRaidReserveResult()
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
			enumerator = ppe.enumeratePacketPayload(client.getProtocol(), buf, getReceiver());
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
		
		final boolean result = enumerator.readFirstInteger32(DIMENSIONAL_SESSION_RESULT) != 0;
		if (!result)
			return; // do nothing
			
		final EnumeratedPayloadField ipIdx = enumerator.getSingleFieldIndex(DIMENSIONAL_SESSION_IP);
		final byte[] ip = enumerator.readBytes(ipIdx);
		final NewGameServerConnection authorizedSession = new L2GameServerInfo(ip, client);
		
		final ByteBuffer thisPacket = packet.getDefaultBufferForModifications();
		
		final ListenSocket gameWorldSocket = L2LoginServerConnections.getInstance().getAdvertisedSocket(client);
		if (gameWorldSocket != null)
		{
			final InetSocketAddress socketAddress = gameWorldSocket.getListenAddress();
			final byte[] fakeIp = socketAddress.getAddress().getAddress();
			thisPacket.position(ipIdx.getOffset());
			thisPacket.put(fakeIp);
			packet.setForwardedBody(thisPacket);
		}
		
		if (L2SessionManager.getInstance().setAuthorizedSession(authorizedSession))
		{
			LOG.info("Active session: " + authorizedSession);
			return;
		}
		
		LOG.info("Delayed session: " + authorizedSession);
		packet.demandLoss(null);
		L2ThreadPool.schedule(new L2SessionSetterAsync(client, authorizedSession, packet.getReceivedBody(), thisPacket), 10, TimeUnit.MILLISECONDS);
	}
	
	@Override
	protected int getMinimumLength()
	{
		return 0; // due to PPE
	}
}
