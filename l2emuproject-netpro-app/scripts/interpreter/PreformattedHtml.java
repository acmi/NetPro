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

import net.l2emuproject.proxy.network.meta.interpreter.StringInterpreter;
import net.l2emuproject.proxy.script.interpreter.ScriptedFieldValueInterpreter;
import net.l2emuproject.proxy.state.entity.context.ICacheServerID;

/**
 * Used to preserve tags in L2 HTMLs (including truncated HTMLs, like Community Board ones).
 * 
 * @author savormix
 */
public class PreformattedHtml extends ScriptedFieldValueInterpreter implements StringInterpreter
{
	@Override
	public Object getInterpretation(String value, ICacheServerID entityCacheContext)
	{
		/*
		if (value.startsWith(".."))
			return value;
			
		return value.replace("<", "&lt;").replace(">", "&gt;").replace("\r\n", "<br>").replace("\n", "<br>");
		*/
		return value;
	}
}
