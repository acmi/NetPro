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
package net.l2emuproject.proxy.state.entity.context;

import net.l2emuproject.network.protocol.IProtocolVersion;

/**
 * This is a marker superinterface for entity existence boundary defining cache contexts.
 * 
 * @author _dev_
 */
public interface ICacheServerID
{
	/*
	/**
	 * Returns whether it is safe to remove all cached entities.
	 * Only meaningful when an instance represents a shared context.
	 * 
	 * @return is last instance of a context
	 *
	boolean isLastOfAKind();
	*/
	/**
	 * Returns the protocol version of the associated context.
	 * @return protocol version
	 */
	//IProtocolVersion getProtocol();
}
