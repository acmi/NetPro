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
package interpreter;

import net.l2emuproject.proxy.script.interpreter.ScriptedZeroBasedIntegerIdInterpreter;
import net.l2emuproject.proxy.state.entity.context.ICacheServerID;

/**
 * Interprets the given byte/word/dword as a name of a territory mercenary that is hiding their true name.
 * 
 * @author savormix
 */
public class MercenaryName extends ScriptedZeroBasedIntegerIdInterpreter
{
	/** Constructs this interpreter. */
	public MercenaryName()
	{
		super(new InterpreterMetadata(81), "Gludio Guardian", "Dion Guardian", "Giran Guardian", "Oren Guardian", "Aden Guardian", "Innadril Guardian", "Goddard Guardian", "Rune Guardian",
				"Schuttgart Guardian");
	}
	
	@Override
	public Object getInterpretation(long value, ICacheServerID entityCacheContext)
	{
		if (value == 0L)
			return "N/A";
		
		return super.getInterpretation(value, entityCacheContext);
	}
}
