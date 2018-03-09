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

import static java.nio.charset.StandardCharsets.US_ASCII;

import net.l2emuproject.proxy.network.meta.interpreter.ByteArrayInterpreter;
import net.l2emuproject.proxy.script.interpreter.ScriptedFieldValueInterpreter;
import net.l2emuproject.proxy.state.entity.context.ICacheServerID;

/**
 * Interprets given bytes as an ASCII string.
 * 
 * @author _dev_
 */
public class ASCII extends ScriptedFieldValueInterpreter implements ByteArrayInterpreter
{
	@Override
	public Object getInterpretation(byte[] value, ICacheServerID entityCacheContext)
	{
		int len;
		for (len = value.length; len > 0; --len)
			if (value[len - 1] != 0)
				break;
			
		return "<pre>" + new String(value, 0, len, US_ASCII) + "</pre>";
	}
}
