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
package net.l2emuproject.proxy.network.game;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

import net.l2emuproject.proxy.network.game.client.L2GameClient;

/**
 * Game server connection info wrapper.
 * 
 * @author savormix
 */
public final class L2GameServerInfo implements NewGameServerConnection
{
	private final byte[] _ipv4;
	private final int _port;
	private final InetAddress _authorizedClientAddress;
	private final L2GameClient _underlyingConnection;
	
	/**
	 * Constructs a game server info object.
	 * 
	 * @param ipv4 IP address
	 * @param port port
	 * @param authorizedClientAddress IP of a client that is cleared to be merged with this connection
	 */
	public L2GameServerInfo(byte[] ipv4, int port, InetAddress authorizedClientAddress)
	{
		_ipv4 = ipv4;
		_port = port;
		_authorizedClientAddress = authorizedClientAddress;
		_underlyingConnection = null;
	}
	
	/**
	 * Constructs a game server info object.
	 * 
	 * @param ipv4 IP address
	 * @param underlyingConnection origin session
	 */
	public L2GameServerInfo(byte[] ipv4, L2GameClient underlyingConnection)
	{
		_ipv4 = ipv4;
		_port = underlyingConnection.getServer().getInetSocketAddress().getPort();
		_authorizedClientAddress = underlyingConnection.getInetAddress();
		_underlyingConnection = underlyingConnection;
	}
	
	@Override
	public InetSocketAddress getAddress()
	{
		try
		{
			return new InetSocketAddress(InetAddress.getByAddress(_ipv4), _port);
		}
		catch (final UnknownHostException e)
		{
			throw new InternalError("Invalid IPv4", e); // incorrect array length
		}
	}
	
	@Override
	public InetAddress getAuthorizedClientAddress()
	{
		return _authorizedClientAddress;
	}
	
	@Override
	public L2GameClient getUnderlyingConnection()
	{
		return _underlyingConnection;
	}
	
	@Override
	public String toString()
	{
		return getAuthorizedClientAddress() + " -> " + getAddress();
	}
}
