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
package interpreter.shortcut;

import net.l2emuproject.lang.L2TextBuilder;
import net.l2emuproject.network.protocol.IProtocolVersion;
import net.l2emuproject.proxy.network.meta.interpreter.IntegerTranslator;
import net.l2emuproject.proxy.script.interpreter.ScriptedFieldValueInterpreter;
import net.l2emuproject.proxy.state.entity.context.ICacheServerID;

/**
 * Interprets the given byte/word/dword as a shortcut's slot on one of the shortcut bars.
 * 
 * @author savormix
 */
public abstract class ShortcutSlot extends ScriptedFieldValueInterpreter implements IntegerTranslator
{
	private final int _barSize;
	
	/**
	 * Constructs this interpreter.
	 * 
	 * @param barSize amount of slots in a shortcut bar
	 */
	protected ShortcutSlot(int barSize)
	{
		_barSize = barSize;
	}
	
	@Override
	public Object translate(long value, IProtocolVersion protocol, ICacheServerID entityCacheContext)
	{
		final L2TextBuilder sb = new L2TextBuilder("Slot: ");
		sb.append(value % _barSize + 1).append(", bar: ").append(value / _barSize + 1);
		return sb.toString();
	}
}
