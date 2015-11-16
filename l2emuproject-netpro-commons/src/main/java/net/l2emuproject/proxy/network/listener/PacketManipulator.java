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
import net.l2emuproject.proxy.network.Packet;
import net.l2emuproject.proxy.network.Proxy;

/**
 * Interface for entities willing to be notified about packet events.<BR>
 * <BR>
 * This interface is intended for packet-manipulating entities.
 * 
 * @author savormix
 */
public interface PacketManipulator
{
	/**
	 * Returns the name of this listener.
	 * 
	 * @return listener's name
	 */
	String getName();
	
	/**
	 * Notifies about a packet that is ready to be sent to target.<BR>
	 * <BR>
	 * Proxy has received this packet, deciphered and read it's body. The packet was not forwarded
	 * to the intended target yet. Proxy will not read any additional packets until this method
	 * completes.<BR>
	 * <BR>
	 * <TT>packet</TT> contains the packet data along with any information necessary to detect/avoid
	 * listener conflicts. Sending packets to <TT>recipient</TT> must only be done by
	 * {@link Packet#sendAdditionalPacket(ByteBuffer)}.<BR>
	 * When sending packets to <TT>sender</TT>, you have to choose one option:
	 * <OL>
	 * <LI>Send directly using <TT>sender.sendPacket(new ProxyRepeatedPacket(body))</TT>. This will skip all
	 * notifications and only this manipulator and <TT>sender</TT> will know about it.</LI>
	 * <LI>Send with 1., then call <TT>sender.notifyPacketForwarded(null, body)</TT>. This is the recommended way.</LI>
	 * <LI>Same as 2., just call <TT>sender.notifyPacketArrived(new Packet(body))</TT> before the forwarding
	 * notification. This allows scripts to cooperate, but may cause infinite loops.</LI>
	 * </OL>
	 * 
	 * @param sender
	 *            internal object that represents the sender
	 * @param recipient
	 *            internal object that represents the target
	 * @param packet
	 *            received packet
	 * @throws RuntimeException
	 *             if something went wrong
	 */
	void packetArrived(Proxy sender, Proxy recipient, Packet packet) throws RuntimeException;
	
	/**
	 * Notifies about a sent packet. Packet's body fills the entire read-only buffer with a backing
	 * array. The position/mark/limit is undefined.<BR>
	 * <BR>
	 * Proxy has received this packet, deciphered, read it's body and either forwarded to the
	 * intended target with possible modifications or did not forward at all. The packet itself may
	 * not have reached the target yet.<BR>
	 * <BR>
	 * If <TT>received</TT> is <TT>null</TT>, this packet was sent by a 3rd party (script) If <TT>sent</TT> is
	 * <TT>null</TT>, then this packet was not forwarded.
	 * 
	 * @param sender
	 *            internal object that represents the sender
	 * @param recipient
	 *            internal object that represents the target
	 * @param received
	 *            original packet's body
	 * @param sent
	 *            body after all modifications
	 * @throws RuntimeException
	 *             if something went wrong
	 */
	void packetForwarded(Proxy sender, Proxy recipient, ByteBuffer received, ByteBuffer sent) throws RuntimeException;
	
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
