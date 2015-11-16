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

import net.l2emuproject.network.protocol.IGameProtocolVersion;
import net.l2emuproject.network.protocol.ILoginProtocolVersion;
import net.l2emuproject.network.protocol.IProtocolVersion;
import net.l2emuproject.util.EnumValues;

/**
 * Represents a server type.
 * 
 * @author savormix
 */
public enum ServiceType
{
	/** Represents the authorization and server lobby service. */
	LOGIN,
	/** Represents the in-game world and character lobby service. */
	GAME;
	
	/**
	 * Returns whether this represents a login service.
	 * 
	 * @return is login service
	 */
	public boolean isLogin()
	{
		return this == LOGIN;
	}
	
	/**
	 * Returns a service type.
	 * 
	 * @param login whether to return login type
	 * @return login/game service
	 */
	public static ServiceType valueOf(boolean login)
	{
		return login ? LOGIN : GAME;
	}
	
	/**
	 * Retrieves the service type from a network protocol definition.
	 * 
	 * @param protocol a protocol definition
	 * @return service type of protocol
	 */
	public static ServiceType valueOf(IProtocolVersion protocol)
	{
		if (protocol instanceof ILoginProtocolVersion)
			return LOGIN;
		else if (protocol instanceof IGameProtocolVersion)
			return GAME;
		
		return null;
	}
	
	/** Returns {@link #values()}. */
	public static final EnumValues<ServiceType> VALUES = new EnumValues<>(ServiceType.class);
}
