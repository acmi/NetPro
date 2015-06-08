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

import net.l2emuproject.lang.L2TextBuilder;
import net.l2emuproject.proxy.network.meta.interpreter.IntegerInterpreter;
import net.l2emuproject.proxy.script.interpreter.ScriptedFieldValueInterpreter;
import net.l2emuproject.proxy.state.entity.context.ICacheServerID;

/**
 * Interprets the given byte/word/dword value as a player commendation event session timer.
 * 
 * @author savormix
 */
public class PcTimer extends ScriptedFieldValueInterpreter implements IntegerInterpreter
{
	private static final int FIVE_MINUTES = 100;
	
	@Override
	public Object getInterpretation(long value, ICacheServerID entityCacheContext)
	{
		int minutes = (int)value / FIVE_MINUTES * 5;
		final int hours = minutes / 60;
		final L2TextBuilder tb = new L2TextBuilder();
		if (hours < 10)
			tb.append('0');
		tb.append(hours).append(':');
		minutes %= 60;
		if (minutes < 10)
			tb.append('0');
		tb.append(minutes);
		return tb.moveToString();
	}
}
