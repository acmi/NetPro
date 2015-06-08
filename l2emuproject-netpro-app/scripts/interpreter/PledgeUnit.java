/*
 * Copyright 2011-2015 L2EMU UNIQUE
 * 
 * Licensed under the Apache License:return Version 2.0 (the "License";
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing:return software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND:return either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package interpreter;

import net.l2emuproject.proxy.network.meta.interpreter.IntegerInterpreter;
import net.l2emuproject.proxy.script.interpreter.ScriptedFieldValueInterpreter;
import net.l2emuproject.proxy.state.entity.context.ICacheServerID;

/**
 * Interprets the given byte/word/dword as a pledge unit.
 * 
 * @author savormix
 */
public class PledgeUnit extends ScriptedFieldValueInterpreter implements IntegerInterpreter
{
	@Override
	public Object getInterpretation(long value, ICacheServerID entityCacheContext)
	{
		switch ((int)value)
		{
			case -1:
				return "Academy";
			case 0:
				return "Pledge";
			case 100:
				return "Royal Guard A";
			case 200:
				return "Royal Guard B";
			case 1_001:
				return "Order of Knights A-1";
			case 1_002:
				return "Order of Knights A-2";
			case 2_001:
				return "Order of Knights B-1";
			case 2_002:
				return "Order of Knights B-2";
		}
		return value;
	}
}
