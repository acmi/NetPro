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

import java.net.InetAddress;

/**
 * A descriptor of a listening socket that forwards all data to/from a specific remote socket.
 * 
 * @author _dev_
 */
public class ProxySocket extends ListenSocket
{
	final String _serviceAddress;
	final int _servicePort;
	
	/**
	 * Constructs a proxy socket.
	 * 
	 * @param bindAddress listening address
	 * @param listenPort listening port
	 * @param serviceAddress remote hostname
	 * @param servicePort remote port
	 */
	public ProxySocket(InetAddress bindAddress, int listenPort, String serviceAddress, int servicePort)
	{
		super(bindAddress, listenPort);
		
		_serviceAddress = serviceAddress;
		_servicePort = servicePort;
	}
	
	/**
	 * Returns the hostname of a remote socket.
	 * 
	 * @return service address
	 */
	public String getServiceAddress()
	{
		return _serviceAddress;
	}
	
	/**
	 * Returns the port of a remote socket.
	 * 
	 * @return service port
	 */
	public int getServicePort()
	{
		return _servicePort;
	}
	
	@Override
	public String toString()
	{
		return super.toString() + "->" + _serviceAddress + ":" + _servicePort;
	}
}
