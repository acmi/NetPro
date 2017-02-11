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
package net.l2emuproject.proxy.io.packetlog.l2ph;

import net.l2emuproject.proxy.io.packetlog.AbstractLogFilePacket;
import net.l2emuproject.proxy.network.EndpointType;
import net.l2emuproject.proxy.network.ServiceType;

/**
 * Represents a packet loaded from a L2PacketHack historical packet log file.
 * 
 * @author _dev_
 */
public class L2PhLogFilePacket extends AbstractLogFilePacket
{
	private final ServiceType _service;
	
	/**
	 * Constructs a historical packet wrapper.
	 * 
	 * @param service service type
	 * @param endpoint packet type
	 * @param receivalTime time of reception
	 * @param content packet body
	 */
	public L2PhLogFilePacket(ServiceType service, EndpointType endpoint, long receivalTime, byte[] content)
	{
		super(endpoint, content, receivalTime);
		
		_service = service;
	}
	
	/**
	 * Returns the service type of this packet.
	 * 
	 * @return service type
	 */
	public ServiceType getService()
	{
		return _service;
	}
}
