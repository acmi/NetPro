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
package net.l2emuproject.proxy.network.meta;

import net.l2emuproject.network.protocol.ClientProtocolVersion;
import net.l2emuproject.network.protocol.IGameProtocolVersion;
import net.l2emuproject.network.protocol.IProtocolVersion;
import net.l2emuproject.network.protocol.ProtocolVersionManager;
import net.l2emuproject.network.protocol.UnknownProtocolVersion;
import net.l2emuproject.network.security.OpcodeTableShuffleConfig;
import net.l2emuproject.network.security.OpcodeTableShuffleType;

/**
 * A game protocol version constructed from user-supplied details.
 * 
 * @author savormix
 */
public class UserDefinedGameProtocolVersion extends UserDefinedProtocolVersion implements IGameProtocolVersion
{
	private final OpcodeTableShuffleConfig _shuffleConfig;
	
	/**
	 * Constructs a game protocol version definition.
	 * 
	 * @param alias protocol name
	 * @param category protocol group
	 * @param version protocol revision number
	 * @param date protocol version introduction to NA data
	 * @param shuffleMode opcode shuffle implementation to be used
	 * @param const1 primary opcodes exempt from CM opcode shuffling
	 * @param total2 total amount of secondary (extended) opcodes
	 * @param const2 secondary (extended) opcodes exempt from CM opcode shuffling
	 */
	public UserDefinedGameProtocolVersion(String alias, String category, int version, long date, OpcodeTableShuffleType shuffleMode, int[] const1, int total2, int[] const2)
	{
		super(alias, category, version, date);
		
		_shuffleConfig = new UserDefinedOpcodeTableShuffleConfig(shuffleMode, const1, total2, const2);
	}
	
	private static IProtocolVersion getActual(IProtocolVersion version)
	{
		if (version instanceof ClientProtocolVersion)
		{
			final IProtocolVersion protocol = ProtocolVersionManager.getInstance().getGameProtocol(version.getVersion());
			if (!(protocol instanceof UnknownProtocolVersion))
				return protocol;
		}
		
		return version;
	}
	
	@Override
	public OpcodeTableShuffleConfig getOpcodeTableShuffleConfig()
	{
		return _shuffleConfig;
	}
	
	@Override
	public boolean isOlderThan(IProtocolVersion version)
	{
		return super.isOlderThan(getActual(version));
	}
	
	@Override
	public boolean isOlderThanOrEqualTo(IProtocolVersion version)
	{
		return super.isOlderThanOrEqualTo(getActual(version));
	}
	
	@Override
	public boolean isNewerThan(IProtocolVersion version)
	{
		return super.isNewerThan(getActual(version));
	}
	
	@Override
	public boolean isNewerThanOrEqualTo(IProtocolVersion version)
	{
		return super.isNewerThanOrEqualTo(getActual(version));
	}
}
