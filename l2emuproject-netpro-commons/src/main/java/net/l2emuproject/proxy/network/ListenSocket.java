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
import java.net.InetSocketAddress;

/**
 * @author _dev_
 */
public class ListenSocket
{
	/** An internet address indicating addresses assigned to all local network adapters. */
	public static final InetAddress WILDCARD = new InetSocketAddress(0).getAddress();
	
	final InetAddress _bindAddress;
	final int _listenPort;
	
	/**
	 * Creates a socket.
	 * 
	 * @param bindAddress socket address
	 * @param listenPort socket port
	 */
	public ListenSocket(InetAddress bindAddress, int listenPort)
	{
		_bindAddress = bindAddress;
		_listenPort = listenPort;
	}
	
	/**
	 * Returns whether this socket and the given socket may be bound at the same time.
	 * 
	 * @param socket a socket
	 * @return {@code true} if sockets can be bound concurrently, {@code false} otherwise
	 */
	public boolean bindingEquals(ListenSocket socket)
	{
		if (_listenPort != socket._listenPort)
			return false;
		
		if (WILDCARD.equals(_bindAddress) || WILDCARD.equals(socket._bindAddress))
			return true;
		
		return _bindAddress.equals(socket._bindAddress);
	}
	
	/**
	 * Returns the address of this socket.
	 * 
	 * @return listening address
	 */
	public InetAddress getBindAddress()
	{
		return _bindAddress;
	}
	
	/**
	 * Returns the port of this socket.
	 * 
	 * @return listening port
	 */
	public int getListenPort()
	{
		return _listenPort;
	}
	
	@Override
	public String toString()
	{
		return _bindAddress + ":" + _listenPort;
	}
}
