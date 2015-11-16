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

import net.l2emuproject.proxy.network.meta.interpreter.IntegerInterpreter;
import net.l2emuproject.proxy.script.interpreter.ScriptedFieldValueInterpreter;
import net.l2emuproject.proxy.state.entity.context.ICacheServerID;

/**
 * Interprets the given byte/word/dword value as an inventory slot number.
 * 
 * @author savormix
 * @deprecated This was done for HF, so after ExQuestItemList separation in Freya. Needs to be completely redesigned.
 */
@Deprecated
public class InventorySlot extends ScriptedFieldValueInterpreter implements IntegerInterpreter
{
	/**
	 * Interprets -1.
	 * 
	 * @return -1's interpretation
	 */
	protected String getSpecialInterpretation()
	{
		return "Auto";
	}
	
	@Override
	public Object getInterpretation(long value, ICacheServerID entityCacheContext)
	{
		if (value == -1 || value == 255)
			return getSpecialInterpretation();
			
		return value;
	}
}
