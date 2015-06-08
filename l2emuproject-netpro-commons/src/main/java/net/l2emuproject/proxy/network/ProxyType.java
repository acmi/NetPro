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

import net.l2emuproject.util.EnumValues;

/**
 * Enumeration of available proxy types.
 * 
 * @author NB4L1
 */
public enum ProxyType
{
	/** Receives from client, forwards to login server */
	LOGIN_CLIENT(true, true),
	/** Receives from login server, forwards to client */
	LOGIN_SERVER(true, false),
	/** Receives from client, forwards to game server */
	GAME_CLIENT(false, true),
	/** Receives from game server, forwards to client */
	GAME_SERVER(false, false);
	
	private final boolean _login;
	private final boolean _client;
	
	private ProxyType(boolean login, boolean client)
	{
		_login = login;
		_client = client;
	}
	
	/**
	 * Returns {@code true} if this represents the authorization service. 
	 *
	 * @return is this an endpoint for login service
	 */
	public boolean isLogin()
	{
		return _login;
	}
	
	/**
	 * Returns {@code true} if this represents a client endpoint. 
	 *
	 * @return is this a client endpoint
	 */
	public boolean isClient()
	{
		return _client;
	}
	
	/**
	 * Returns a proxy type.
	 * 
	 * @param login whether it communicates during login connection
	 * @param client whether it communicates with client
	 * @return type of proxy
	 */
	public static ProxyType valueOf(boolean login, boolean client)
	{
		if (login)
		{
			if (client)
				return ProxyType.LOGIN_CLIENT;
			else
				return ProxyType.LOGIN_SERVER;
		}
		else
		{
			if (client)
				return ProxyType.GAME_CLIENT;
			else
				return ProxyType.GAME_SERVER;
		}
	}
	
	/** Login client &amp; server, Game client &amp; server */
	public static final EnumValues<ProxyType> VALUES = new EnumValues<>(ProxyType.class);
}
