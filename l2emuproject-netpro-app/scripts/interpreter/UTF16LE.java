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

import static java.nio.charset.StandardCharsets.UTF_16LE;

import net.l2emuproject.proxy.network.meta.interpreter.ByteArrayInterpreter;
import net.l2emuproject.proxy.script.interpreter.ScriptedFieldValueInterpreter;
import net.l2emuproject.proxy.state.entity.context.ICacheServerID;

/**
 * Interprets given bytes as an UTF16LE string.
 * 
 * @author _dev_
 */
public class UTF16LE extends ScriptedFieldValueInterpreter implements ByteArrayInterpreter
{
	@Override
	public Object getInterpretation(byte[] value, ICacheServerID entityCacheContext)
	{
		if (value.length < 2)
			return "<pre></pre>";
		
		int len = value.length;
		for (int i = 1; i < value.length; i += 2)
		{
			if (value[i - 1] == 0 && value[i] == 0)
			{
				len = i - 1;
				break;
			}
		}
		
		return "<pre>" + new String(value, 0, len, UTF_16LE) + "</pre>";
	}
}
