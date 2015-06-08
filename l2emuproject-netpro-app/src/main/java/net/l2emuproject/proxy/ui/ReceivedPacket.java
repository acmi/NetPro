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
package net.l2emuproject.proxy.ui;

import net.l2emuproject.proxy.network.EndpointType;
import net.l2emuproject.proxy.network.ServiceType;

/**
 * A received packet wrapper intended for display methods.
 * 
 * @author savormix
 */
public class ReceivedPacket
{
	/** A timestamp shared by wrappers of packets that have not been sent yet. */
	public static final long UNSENT_PACKET_TIMESTAMP = -1L;
	
	// millions of instances very likely, keep field count to bare minimum
	private final int _proxyType;
	private final byte[] _body;
	private final long _received;
	
	/**
	 * Constructs a packet wrapper for displaying purposes.
	 * 
	 * @param service service type
	 * @param endpoint endpoint type
	 * @param body packet's body
	 * @param received time of receival
	 */
	public ReceivedPacket(ServiceType service, EndpointType endpoint, byte[] body, long received)
	{
		_proxyType = getLegacyProxyTypeOrdinal(service, endpoint);
		_body = body;
		_received = received;
	}
	
	/**
	 * Constructs an injected packet wrapper for displaying purposes.
	 * 
	 * @param service service type
	 * @param endpoint endpoint type
	 * @param body packet's body
	 */
	public ReceivedPacket(ServiceType service, EndpointType endpoint, byte[] body)
	{
		this(service, endpoint, body, UNSENT_PACKET_TIMESTAMP);
	}
	
	/**
	 * Returns the service type of this packet.
	 * 
	 * @return login/game
	 */
	public ServiceType getService()
	{
		return ServiceType.valueOf(_proxyType < 2);
	}
	
	/**
	 * Returns the authoring endpoint type.
	 * 
	 * @return client/server
	 */
	public EndpointType getEndpoint()
	{
		return EndpointType.valueOf((_proxyType & 1) == 0);
	}
	
	/**
	 * Returns the packet's body.
	 * 
	 * @return packet's body
	 */
	public byte[] getBody()
	{
		return _body;
	}
	
	/**
	 * Returns an approximate time of packet arrival to proxy.
	 * 
	 * @return reception time
	 */
	public long getReceived()
	{
		return _received;
	}
	
	/**
	 * Emulates the {@code ordinal()} method of an obsolete enum.
	 * Might be removed eventually.
	 * 
	 * @param service login/game
	 * @param endpoint client/server
	 * @return legacy value
	 */
	public static int getLegacyProxyTypeOrdinal(ServiceType service, EndpointType endpoint)
	{
		return (service.ordinal() << 1) | endpoint.ordinal();
	}
}
