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
package net.l2emuproject.proxy.script.packets;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import net.l2emuproject.network.IProtocolVersion;
import net.l2emuproject.proxy.network.EndpointType;
import net.l2emuproject.proxy.network.Proxy;
import net.l2emuproject.util.L2Collections;

/**
 * Manages classes capable of writing packets and sending them to connection endpoints.
 * 
 * @author _dev_
 */
public final class PacketWriterRegistry
{
	private final Map<EndpointType, Map<String, List<PacketWriter>>> _packetWriters;
	
	PacketWriterRegistry()
	{
		_packetWriters = new EnumMap<>(EndpointType.class);
		for (final EndpointType et : EndpointType.values())
			_packetWriters.put(et, new ConcurrentHashMap<>());
	}
	
	/**
	 * Attempts to send a packet to the given endpoint, depending on what packet writers are currently registered.
	 * 
	 * @param recipient connection endpoint
	 * @param packetIdentifier packet ID known to a registered writer
	 * @param writerArgs arguments to pass to the packet writer
	 * @throws UnknownPacketIdentifierException if there are no writers for the given {@code packetIdentifier}
	 * @throws UnknownPacketStructureException if there are no writers for the {@code recipient}'s protocol version
	 * @throws InvalidPacketWriterArgumentsException if incorrect arguments were passed to the packet writer
	 */
	public void sendPacket(Proxy recipient, String packetIdentifier, Object... writerArgs) throws UnknownPacketIdentifierException, UnknownPacketStructureException,
			InvalidPacketWriterArgumentsException
	{
		final List<PacketWriter> protocol2Writer = _packetWriters.get(recipient.getTarget().getType()).get(packetIdentifier);
		if (protocol2Writer == null || protocol2Writer.isEmpty())
			throw new UnknownPacketIdentifierException(packetIdentifier);
		final IProtocolVersion protocol = recipient.getProtocol();
		PacketWriter writer = null;
		for (final PacketWriter pw : protocol2Writer)
		{
			if (protocol.isNewerThanOrEqualTo(pw.oldestSupportedProtocolVersion()) && protocol.isOlderThanOrEqualTo(pw.newestSupportedProtocolVersion()))
			{
				writer = pw;
				break;
			}
		}
		if (writer == null)
			throw new UnknownPacketStructureException(packetIdentifier, protocol);
		writer.sendPacket(recipient, packetIdentifier, writerArgs);
	}
	
	/**
	 * Registers a new packet writer as capable of writing a packet identified by {@code packetIdentifier}.
	 * 
	 * @param packetType type of packet [sender]
	 * @param packetIdentifier packet identifier
	 * @param packetWriter packet writer
	 */
	public void registerWriter(EndpointType packetType, String packetIdentifier, PacketWriter packetWriter)
	{
		L2Collections.putIfAbsent(_packetWriters.get(packetType), packetIdentifier, new CopyOnWriteArrayList<>()).add(packetWriter);
	}
	
	/**
	 * Removes a packet writer from being used to write packets identified by {@code packetIdentifier}.
	 * 
	 * @param packetType type of packet [sender]
	 * @param packetIdentifier packet identifier
	 * @param packetWriter packet writer
	 */
	public void removeWriter(EndpointType packetType, String packetIdentifier, PacketWriter packetWriter)
	{
		final List<PacketWriter> writers = _packetWriters.get(packetType).get(packetIdentifier);
		if (writers != null)
			writers.remove(packetWriter);
	}
	
	/**
	 * Returns a singleton instance of this type.
	 * 
	 * @return an instance of this class
	 */
	public static final PacketWriterRegistry getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static final class SingletonHolder
	{
		static final PacketWriterRegistry INSTANCE = new PacketWriterRegistry();
	}
}
