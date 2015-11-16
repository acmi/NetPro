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
package net.l2emuproject.proxy.network.listener;

import java.nio.ByteBuffer;

import net.l2emuproject.network.protocol.IProtocolVersion;
import net.l2emuproject.proxy.network.Proxy;

/**
 * Interface for entities willing to be notified about transferred packets. <BR>
 * <BR>
 * This interface is a simplified version of {@link PacketManipulator}. For more information, see
 * method JavaDoc.
 * 
 * @author savormix
 */
public interface PacketListener
{
	/**
	 * Notifies about a <B>client</B> packet sent to a <B>server</B>.<BR>
	 * <BR>
	 * The packet itself may be not yet sent, on the way or already received at the moment this
	 * method is called.<BR>
	 * <BR>
	 * The packet body fills the entire read-only buffer with a backing array. It's
	 * position/mark/limit is undefined.
	 * 
	 * @param sender
	 *            internal object that represents the client
	 * @param recipient
	 *            internal object that represents the server
	 * @param packet
	 *            read only packet data
	 * @param time
	 *            time of arrival at proxy server
	 * @throws RuntimeException
	 *             if something went wrong
	 */
	void onClientPacket(Proxy sender, Proxy recipient, ByteBuffer packet, long time) throws RuntimeException;
	
	/**
	 * Notifies about a <B>server</B> packet sent to a <B>client</B>.<BR>
	 * <BR>
	 * The packet itself may be not yet sent, on the way or already received at the moment this
	 * method is called.<BR>
	 * <BR>
	 * The packet body fills the entire read-only buffer with a backing array. It's
	 * position/mark/limit is undefined.
	 * 
	 * @param sender
	 *            internal object that represents the server
	 * @param recipient
	 *            internal object that represents the client
	 * @param packet
	 *            read only packet data
	 * @param time
	 *            time of arrival at proxy server
	 * @throws RuntimeException
	 *             if something went wrong
	 */
	void onServerPacket(Proxy sender, Proxy recipient, ByteBuffer packet, long time) throws RuntimeException;
	
	/**
	 * Notifies about a protocol version change.<BR>
	 * <BR>
	 * This method runs on the selector RW thread. Implementors should only do a single assignment
	 * operation.
	 * 
	 * @param affected
	 *            internal object that represents a client or a server
	 * @param version
	 *            protocol version
	 * @throws RuntimeException
	 *             if something went wrong
	 */
	void onProtocolVersion(Proxy affected, IProtocolVersion version) throws RuntimeException;
}
