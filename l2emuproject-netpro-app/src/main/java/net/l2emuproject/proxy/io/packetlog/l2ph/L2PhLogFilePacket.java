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

import net.l2emuproject.proxy.network.EndpointType;
import net.l2emuproject.proxy.network.ServiceType;

/**
 * Represents a packet loaded from a L2PacketHack historical packet log file.
 * 
 * @author _dev_
 */
public class L2PhLogFilePacket
{
	private final ServiceType _service;
	private final EndpointType _endpoint;
	private final long _receivalTime;
	private final byte[] _content;
	
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
		_service = service;
		_endpoint = endpoint;
		_content = content;
		_receivalTime = receivalTime;
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
	
	/**
	 * Returns the type of this packet.
	 * 
	 * @return packet type
	 */
	public EndpointType getEndpoint()
	{
		return _endpoint;
	}
	
	/**
	 * Returns the time of arrival of this packet.
	 * 
	 * @return receival time
	 */
	public long getReceivalTime()
	{
		return _receivalTime;
	}
	
	/**
	 * Returns the content of this packet.
	 * 
	 * @return packet body
	 */
	public byte[] getContent()
	{
		return _content;
	}
}
