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
package net.l2emuproject.proxy.network.meta;

import net.l2emuproject.network.IProtocolVersion;
import net.l2emuproject.network.mmocore.MMOBuffer;
import net.l2emuproject.proxy.network.meta.exception.InvalidPacketOpcodeSchemeException;
import net.l2emuproject.proxy.network.meta.exception.PartialPayloadEnumerationException;
import net.l2emuproject.proxy.network.packets.IPacketSource;

/**
 * Allows to enumerate named data fields inside a packet's payload.
 * 
 * @author _dev_
 */
public interface PacketPayloadEnumerator
{
	/**
	 * Enumerates any fields this payload enumerator knows about. Upon termination, {@code buf}'s position will be unchanged.
	 * All further data retrieval should be done via the returned buffer.<BR>
	 * <BR>
	 * An enumerator only enumerates key fields. By default, an enumerator implementation will not build every single
	 * node of a tree based on a packet's structure.
	 * 
	 * @param protocol protocol version
	 * @param buf packet content wrapper
	 * @param author packet provider
	 * @return a buffer to access enumerated fields
	 * @throws InvalidPacketOpcodeSchemeException if a packet opcode is incompatible with the packet template provider
	 * @throws PartialPayloadEnumerationException if a packet cannot be enumerated completely
	 */
	RandomAccessMMOBuffer enumeratePacketPayload(IProtocolVersion protocol, MMOBuffer buf, IPacketSource author) throws InvalidPacketOpcodeSchemeException, PartialPayloadEnumerationException;
}
