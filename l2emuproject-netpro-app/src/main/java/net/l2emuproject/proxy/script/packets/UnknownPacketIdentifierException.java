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

/**
 * An exception to notify caller about an invalid packet identifier (or a missing packet writer).
 * 
 * @author _dev_
 */
public class UnknownPacketIdentifierException extends RuntimeException
{
	private static final long serialVersionUID = 8140287745445260254L;
	
	/**
	 * Creates this exception.
	 * 
	 * @param identifier faulting packet identifier
	 */
	public UnknownPacketIdentifierException(String identifier)
	{
		super("No writer registered for packet '" + identifier + "'");
	}
}
