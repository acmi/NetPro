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

import net.l2emuproject.network.ClientProtocolVersion;
import net.l2emuproject.network.IGameProtocolVersion;
import net.l2emuproject.network.IProtocolVersion;
import net.l2emuproject.network.ProtocolVersionManager;
import net.l2emuproject.network.UnknownProtocolVersion;

/**
 * A game protocol version constructed from user-supplied details.
 * 
 * @author savormix
 */
public class UserDefinedGameProtocolVersion extends UserDefinedProtocolVersion implements IGameProtocolVersion
{
	private final int[] _const1;
	private final int _total2;
	private final int[] _const2;
	
	/**
	 * Constructs a game protocol version definition.
	 * 
	 * @param alias protocol name
	 * @param category protocol group
	 * @param version protocol revision number
	 * @param date protocol version introduction to NA data
	 * @param const1 primary opcodes exempt from CM opcode shuffling
	 * @param total2 total amount of secondary (extended) opcodes
	 * @param const2 secondary (extended) opcodes exempt from CM opcode shuffling
	 */
	public UserDefinedGameProtocolVersion(String alias, String category, int version, long date, int[] const1, int total2, int[] const2)
	{
		super(alias, category, version, date);
		
		_const1 = const1;
		_total2 = total2;
		_const2 = const2;
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
	
	@Override
	public int getOp2TableSize()
	{
		return _total2;
	}
	
	@Override
	public int[] getIgnoredOp1s()
	{
		return _const1;
	}
	
	@Override
	public int[] getIgnoredOp2s()
	{
		return _const2;
	}
}
