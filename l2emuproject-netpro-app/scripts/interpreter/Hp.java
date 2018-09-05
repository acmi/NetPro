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

import net.l2emuproject.network.protocol.IProtocolVersion;
import net.l2emuproject.proxy.network.meta.interpreter.IntegerTranslator;
import net.l2emuproject.proxy.script.interpreter.ScriptedFieldValueInterpreter;
import net.l2emuproject.proxy.state.entity.context.ICacheServerID;

/**
 * Interprets the given byte/word/dword value as some object's HP.
 * 
 * @author savormix
 */
public class Hp extends ScriptedFieldValueInterpreter implements IntegerTranslator
{
	// High Five specific
	private static final int PREDICTED_ABSOLUTE_HP_LIMIT = 99_999;
	private static final int EQUAL_TO_MAXIMUM_HP = 10_000_000;
	
	@Override
	public Object translate(long value, IProtocolVersion protocol, ICacheServerID entityCacheContext)
	{
		if (value > PREDICTED_ABSOLUTE_HP_LIMIT)
			return String.valueOf(value * 100 / EQUAL_TO_MAXIMUM_HP) + "%";
		
		return value;
	}
}
