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

import net.l2emuproject.network.IProtocolVersion;
import net.l2emuproject.proxy.network.Proxy;

/**
 * Defines a simple packet writer, capable of writing an arbitrary amount of different packets.
 * 
 * @author _dev_
 */
public interface PacketWriter
{
	/**
	 * Sends a packet to {@code recipient}. It is guaranteed that the protocol version of {@code recipient} is supported by this writer.
	 * 
	 * @param recipient connection endpoint
	 * @param packet packet identifier
	 * @param args packet data field values
	 * @throws InvalidPacketWriterArgumentsException if {@code args} are invalid/no longer valid
	 */
	void sendPacket(Proxy recipient, String packet, Object[] args) throws InvalidPacketWriterArgumentsException;
	
	/**
	 * Returns the oldest supported network protocol version.
	 * 
	 * @return earliest supported protocol
	 */
	IProtocolVersion oldestSupportedProtocolVersion();
	
	/**
	 * Returns the newest supported network protocol version.
	 * 
	 * @return latest supported protocol
	 */
	IProtocolVersion newestSupportedProtocolVersion();
}
