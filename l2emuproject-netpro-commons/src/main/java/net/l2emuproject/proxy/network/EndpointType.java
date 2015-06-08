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
package net.l2emuproject.proxy.network;

/**
 * Represents a proxified communication connection's endpoint type.
 * 
 * @author savormix
 */
public enum EndpointType
{
	/** Represents a connection between a client and the underlying proxy server. */
	CLIENT,
	/** Represents a connection between the underlying proxy server and an existing Lineage II service. */
	SERVER;
	
	/**
	 * Returns {@code true}, if this is a client endpoint.
	 * 
	 * @return is this a client
	 */
	public boolean isClient()
	{
		return this == CLIENT;
	}
	
	/**
	 * Returns {@code true}, if this is a server endpoint.
	 * 
	 * @return is this a server
	 */
	public boolean isServer()
	{
		return this == SERVER;
	}
	
	/**
	 * Returns an endpoint type.
	 * 
	 * @param client whether the client endpoint is requested
	 * @return endpoint type as requested
	 */
	public static EndpointType valueOf(boolean client)
	{
		return client ? CLIENT : SERVER;
	}
}
