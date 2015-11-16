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

import net.l2emuproject.network.protocol.IProtocolVersion;

/**
 * An exception to notify caller that there are no writers capable of forming the desired packet for the given protocol version.
 * 
 * @author _dev_
 */
public class UnknownPacketStructureException extends Exception
{
	private static final long serialVersionUID = -7927313991649990986L;
	
	/**
	 * Constructs this exception.
	 * 
	 * @param identifier packet identifier
	 * @param protocol faulting protocol version
	 */
	public UnknownPacketStructureException(String identifier, IProtocolVersion protocol)
	{
		super("No writer available for packet '" + identifier + "' in " + protocol);
	}
}
