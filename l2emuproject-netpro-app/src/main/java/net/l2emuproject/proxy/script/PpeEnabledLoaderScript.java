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
package net.l2emuproject.proxy.script;

import java.util.Set;

import net.l2emuproject.proxy.network.meta.RandomAccessMMOBuffer;
import net.l2emuproject.proxy.state.entity.context.ICacheServerID;

/**
 * A packet enumeration based packet postprocessor.
 * 
 * @author _dev_
 */
public interface PpeEnabledLoaderScript
{
	/**
	 * Returns a user-friendly name of this script.
	 * 
	 * @return script's name
	 */
	String getName();
	
	/**
	 * Returns all field aliases to be handled by this script.
	 * 
	 * @return field {@code <scriptAlias>}es
	 */
	Set<String> getHandledScriptFieldAliases();
	
	/**
	 * Handle a client packet that has been forwarded to the server.
	 * 
	 * @param buf packet content
	 * @param cacheContext cache identifier
	 * @throws RuntimeException if something unexpected happens
	 */
	void handleClientPacket(RandomAccessMMOBuffer buf, ICacheServerID cacheContext) throws RuntimeException;
	
	/**
	 * Handle a server packet that has been forwarded to a client.
	 * 
	 * @param buf packet content
	 * @param cacheContext cache identifier
	 * @throws RuntimeException if something unexpected happens
	 */
	void handleServerPacket(RandomAccessMMOBuffer buf, ICacheServerID cacheContext) throws RuntimeException;
}
