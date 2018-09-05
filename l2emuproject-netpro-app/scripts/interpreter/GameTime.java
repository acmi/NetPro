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
import net.l2emuproject.network.protocol.IProtocolVersion;
import net.l2emuproject.proxy.network.meta.interpreter.IntegerTranslator;
import net.l2emuproject.proxy.script.interpreter.ScriptedFieldValueInterpreter;
import net.l2emuproject.proxy.state.entity.context.ICacheServerID;

/**
 * Interprets the given byte/word/dword as in-game time (in-game minute count since server restart).
 * 
 * @author savormix
 */
public class GameTime extends ScriptedFieldValueInterpreter implements IntegerTranslator
{
	@Override
	public Object translate(long value, IProtocolVersion protocol, ICacheServerID entityCacheContext)
	{
		final int igHoursSinceStart = (int)value / 60;
		final int igHours = igHoursSinceStart % 24;
		final int igMinutes = (int)value % 60;
		
		final L2TextBuilder sb = new L2TextBuilder();
		if (igHours < 10)
			sb.append('0');
		sb.append(igHours).append(':');
		if (igMinutes < 10)
			sb.append('0');
		sb.append(igMinutes).append(" [Day ").append((igHoursSinceStart / 24) + 1).append(']');
		return sb.moveToString();
	}
}
