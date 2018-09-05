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
 * Interprets the given byte/word/dword as an elemental power value and translates it to elemental
 * level.
 * 
 * @author savormix
 */
public class ElementPower extends ScriptedFieldValueInterpreter implements IntegerTranslator
{
	@Override
	public Object translate(long value, IProtocolVersion protocol, ICacheServerID entityCacheContext)
	{
		// FIXME: differentiate by item type (weapon/armor)
		return value;
		/*
		if (value < 1)
			return "None";
		else if (value < 25)
			return "Level 1";
		else if (value < 75)
			return "Level 2";
		else if (value < 150)
			return "Level 3";
		else if (value < 175)
			return "Level 4";
		else if (value < 225)
			return "Level 5";
		else if (value < 300)
			return "Level 6";
		else if (value < 325)
			return "Level 7";
		else if (value < 375)
			return "Level 8";
		else if (value < 450)
			return "Level 9";
		else if (value < 475)
			return "Level 10";
		else if (value < 525)
			return "Level 11";
		else if (value < 600)
			return "Level 12";
		else
			return "Level 13";
		*/
	}
}
