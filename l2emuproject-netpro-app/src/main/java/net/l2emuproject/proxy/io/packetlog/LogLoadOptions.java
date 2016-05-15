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
package net.l2emuproject.proxy.io.packetlog;

import java.util.Set;

import net.l2emuproject.network.protocol.IProtocolVersion;

/**
 * Bundles all user-configurable log load options.
 * 
 * @author _dev_
 */
public class LogLoadOptions
{
	private final IProtocolVersion _protocol;
	private final Set<LogLoadFlag> _flags;
	
	/**
	 * Constructs this wrapper.
	 * 
	 * @param protocol protocol version
	 * @param flags toggleable options
	 */
	public LogLoadOptions(IProtocolVersion protocol, Set<LogLoadFlag> flags)
	{
		_protocol = protocol;
		_flags = flags;
	}
	
	/**
	 * Returns the associated network protocol version.
	 * 
	 * @return protocol version
	 */
	public IProtocolVersion getProtocol()
	{
		return _protocol;
	}
	
	/**
	 * Returns all toggleable log load options.
	 * 
	 * @return toggleable options
	 */
	public Set<LogLoadFlag> getFlags()
	{
		return _flags;
	}
	
	/** Toggleable log load options. */
	public enum LogLoadFlag
	{
		/** Loads packets that will not be visible due to current display configuration of the associated protocol. */
		INCLUDE_NON_VISIBLE,
		/** Loads packets that were manually injected into the connection stream. */
		INCLUDE_SYNTHETIC,
		/** Loads packets that arrived while packet capture was disabled. */
		INCLUDE_NON_CAPTURED,
	}
}
