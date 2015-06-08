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
package net.l2emuproject.proxy.network.meta.exception;

import net.l2emuproject.proxy.network.EndpointType;

/**
 * Signifies that the opcode mapping configuration requires more bytes for the given packet prefix to map to a single packet.
 * 
 * @author _dev_
 */
public class InvalidPacketOpcodeSchemeException extends Exception
{
	private static final long serialVersionUID = -3962493496765451746L;
	
	/**
	 * Constructs this exception.
	 * 
	 * @param packetType client/server
	 * @param packet packet content description (preferably hex octet string)
	 * @param cause underlying exception from opcode mapper
	 */
	public InvalidPacketOpcodeSchemeException(EndpointType packetType, String packet, Throwable cause)
	{
		super("Invalid " + packetType + " packet: " + packet, cause);
	}
}
