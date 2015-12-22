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

import static net.l2emuproject.proxy.network.login.client.L2LoginClient.FLAG_SERVER_LIST_C0;
import static net.l2emuproject.proxy.network.login.client.L2LoginClient.FLAG_SERVER_LIST_C1;
import static net.l2emuproject.proxy.network.login.client.L2LoginClient.FLAG_SERVER_LIST_C2;
import static net.l2emuproject.proxy.network.login.client.L2LoginClient.FLAG_SERVER_LIST_NAMED;
import static net.l2emuproject.proxy.network.login.client.packets.RequestServerList.TYPE_BARE;
import static net.l2emuproject.proxy.network.login.client.packets.RequestServerList.TYPE_C0;
import static net.l2emuproject.proxy.network.login.client.packets.RequestServerList.TYPE_C1;
import static net.l2emuproject.proxy.network.login.client.packets.RequestServerList.TYPE_C2;
import static net.l2emuproject.proxy.network.login.client.packets.RequestServerList.TYPE_NAMED;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.List;

import net.l2emuproject.network.mmocore.MMOBuffer;
import net.l2emuproject.proxy.network.ListenSocket;
import net.l2emuproject.proxy.network.Packet;
import net.l2emuproject.proxy.network.game.L2GameServerInfo;
import net.l2emuproject.proxy.network.login.client.L2LoginClient;
import net.l2emuproject.proxy.network.login.server.L2LoginServerConnections;
import net.l2emuproject.proxy.network.meta.EnumeratedPayloadField;
import net.l2emuproject.proxy.network.meta.PacketPayloadEnumerator;
import net.l2emuproject.proxy.network.meta.RandomAccessMMOBuffer;
import net.l2emuproject.proxy.network.meta.RequiredInvasiveOperations;
import net.l2emuproject.proxy.network.meta.ServerListTypePublisher;
import net.l2emuproject.proxy.network.meta.SimplePpeProvider;
import net.l2emuproject.proxy.network.meta.exception.InvalidPacketOpcodeSchemeException;
import net.l2emuproject.proxy.network.meta.exception.PartialPayloadEnumerationException;

/**
 * This packet contains game server information.
 * 
 * @author savormix
 */
public final class ServerList extends L2LoginServerPacket implements RequiredInvasiveOperations, ServerListTypePublisher
{
	/** Packet's identifier */
	public static final int OPCODE = 0x04;
	
	/** Constructs a packet to replace real server IPs &amp; ports. */
	public ServerList()
	{
		super(OPCODE);
	}
	
	@Override
	protected void readAndModify(MMOBuffer buf, Packet packet) throws BufferUnderflowException, RuntimeException
	{
		final L2LoginClient client = getRecipient();
		final ListenSocket gameWorldSocket = L2LoginServerConnections.getInstance().getAdvertisedSocket(client);
		// not trying to intercept
		if (gameWorldSocket == null)
			return;
			
		final byte[] fakeIp = gameWorldSocket.getBindAddress().getAddress();
		final int fakePort = gameWorldSocket.getListenPort();
		
		ByteBuffer newBody = packet.getDefaultBufferForModifications();
		buf.setByteBuffer(newBody);
		buf.readUC(); // opcode
		
		final boolean c0 = client.isProtocolFlagSet(FLAG_SERVER_LIST_C0), named = client.isProtocolFlagSet(FLAG_SERVER_LIST_NAMED);
		final boolean c1 = client.isProtocolFlagSet(FLAG_SERVER_LIST_C1), c2 = client.isProtocolFlagSet(FLAG_SERVER_LIST_C2);
		usePPE:
		{
			final PacketPayloadEnumerator ppe = SimplePpeProvider.getPacketPayloadEnumerator();
			if (ppe == null)
				break usePPE;
				
			RandomAccessMMOBuffer enumerator = null;
			try
			{
				LIST_TYPE.set(c2 ? TYPE_C2 : c1 ? TYPE_C1 : named ? TYPE_NAMED : c0 ? TYPE_C0 : TYPE_BARE);
				enumerator = ppe.enumeratePacketPayload(getClient().getProtocol(), buf, getClient());
			}
			catch (InvalidPacketOpcodeSchemeException e)
			{
				LOG.error("This cannot happen", e);
				break usePPE;
			}
			catch (PartialPayloadEnumerationException e)
			{
				// ignore this until a 'consume all' element is introduced
				enumerator = e.getBuffer();
			}
			
			final List<EnumeratedPayloadField> ids = enumerator.getFieldIndices(GAME_SERVER_ID);
			final List<EnumeratedPayloadField> ips = enumerator.getFieldIndices(GAME_SERVER_IP);
			final List<EnumeratedPayloadField> ports = enumerator.getFieldIndices(GAME_SERVER_PORT);
			
			for (int i = 0; i < ids.size(); ++i)
			{
				final EnumeratedPayloadField ip = ips.get(i);
				final byte[] realIPv4;
				{
					realIPv4 = enumerator.readBytes(ip);
					enumerator.seekField(ip);
					buf.writeB(fakeIp);
				}
				
				final EnumeratedPayloadField port = ports.get(i);
				final int realPort;
				{
					realPort = enumerator.readInteger32(port);
					enumerator.seekField(port);
					buf.writeD(fakePort);
				}
				
				final L2GameServerInfo gsi = new L2GameServerInfo(realIPv4, realPort);
				getRecipient().getServers().put(enumerator.readInteger32(ids.get(i)), gsi);
			}
			
			packet.setForwardedBody(newBody);
			return;
		}
		
		LOG.warn("Using precompiled logic (only versions 0 to 5 supported)");
		
		final int size = buf.readC();
		buf.readC(); // last server ID
		for (int i = 0; i < size; i++)
		{
			int id = buf.readC(); // server ID
			
			if (named)
				buf.skip(40);
				
			final int pos = newBody.position();
			byte[] realIPv4 = buf.readB(4);
			int port = buf.readD();
			L2GameServerInfo gsi = new L2GameServerInfo(realIPv4, port);
			getRecipient().getServers().put(id, gsi);
			
			newBody.position(pos); // back to IP
			for (int j = 0; j < 4; j++)
				buf.writeC(fakeIp[j]);
			buf.writeD(fakePort);
			
			if (c0)
			{
				buf.skip(7); // other info
				if (c1)
				{
					buf.skip(4);
					if (c2)
						buf.skip(1);
				}
			}
		}
		
		packet.setForwardedBody(newBody);
	}
	
	@Override
	protected int getMinimumLength()
	{
		// return READ_C + READ_C;
		return 0; // due to PPE
	}
}
