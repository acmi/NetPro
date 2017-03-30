/*
 * Copyright 2011-2017 L2EMU UNIQUE
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

import net.l2emuproject.network.security.OpcodeTableShuffleConfig;
import net.l2emuproject.network.security.OpcodeTableShuffleType;

/**
 * Represents a user-defined client packet opcode shuffling configuration.
 * 
 * @author _dev_
 */
public class UserDefinedOpcodeTableShuffleConfig implements OpcodeTableShuffleConfig
{
	private final OpcodeTableShuffleType _shuffleMode;
	private final int[] _const1;
	private final int _total2;
	private final int[] _const2;
	
	/**
	 * Creates this configuration.
	 * 
	 * @param shuffleMode shuffle implementation
	 * @param const1 primary opcodes exempt from shuffling
	 * @param total2 secondary opcode amount (<u>largest existing opcode</u>)
	 * @param const2 secondary opcodes exempt from shuffling
	 */
	public UserDefinedOpcodeTableShuffleConfig(OpcodeTableShuffleType shuffleMode, int[] const1, int total2, int[] const2)
	{
		_shuffleMode = shuffleMode;
		_const1 = const1;
		_total2 = total2;
		_const2 = const2;
	}
	
	@Override
	public OpcodeTableShuffleType getShuffleMode()
	{
		return _shuffleMode;
	}
	
	@Override
	public int getOp1TableSize()
	{
		return 0xD0;
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
