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
package net.l2emuproject.proxy.script.packets.util;

import net.l2emuproject.proxy.network.meta.EnumeratedPayloadField;
import net.l2emuproject.proxy.network.meta.RandomAccessMMOBuffer;
import net.l2emuproject.proxy.script.ScriptFieldAlias;

/**
 * Provides access to system message packets and related convenience methods.
 * 
 * @author _dev_
 */
public interface RestartResponseRecipient
{
	/** A boolean to indicate if returning to lobby is possible */
	@ScriptFieldAlias
	String RESTART_RESPONSE = "restart_response";
	
	/**
	 * Returns {@code true} if the packet indicates that the server will now return the client to the character selection screen, {@code false} for any other packet.
	 * 
	 * @param buf a server packet
	 * @return {@code true} if the users character is about to be detached, {@code false} otherwise
	 */
	default boolean isReturningToLobby(RandomAccessMMOBuffer buf)
	{
		final EnumeratedPayloadField rr = buf.getSingleFieldIndex(RESTART_RESPONSE);
		return rr != null ? buf.readInteger32(rr) != 0 : false;
	}
}
