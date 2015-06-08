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
package net.l2emuproject.proxy.network.meta.container;

import java.nio.ByteBuffer;

import net.l2emuproject.proxy.network.meta.IPacketTemplate;

/**
 * An interface that can select a packet definition for a received packet's body.
 * 
 * @author _dev_
 */
public interface IPacketResolver
{
	/**
	 * Selects a definition for a packet that is contained in {@code packet}, starting at {@code offset} and taking up {@code length} bytes.
	 * 
	 * @param packet a buffer that contains the packet body
	 * @param offset offset in buffer to packet
	 * @param length packet's size in bytes
	 * @return a suitable packet definition or {@link IPacketTemplate#ANY_DYNAMIC_PACKET}
	 */
	IPacketTemplate resolve(byte[] packet, int offset, int length);
	
	/**
	 * Selects a definition for a packet that is completely contained in {@code packet}.
	 * 
	 * @param packet packet's body
	 * @return a suitable packet definition or {@link IPacketTemplate#ANY_DYNAMIC_PACKET}
	 */
	default IPacketTemplate resolve(byte[] packet)
	{
		return resolve(packet, 0, packet.length);
	}
	
	/**
	 * Selects a definition for a packet that is contained in {@code packet}, assuming it starts at current position and extends to current limit.
	 * 
	 * @param packet a buffer that contains the packet body
	 * @return a suitable packet definition or {@link IPacketTemplate#ANY_DYNAMIC_PACKET}
	 */
	default IPacketTemplate resolve(ByteBuffer packet)
	{
		return resolve(packet.array(), packet.position(), packet.remaining());
	}
}
