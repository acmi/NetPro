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
import net.l2emuproject.proxy.state.entity.L2ObjectInfoCache;
import net.l2emuproject.proxy.state.entity.context.ICacheServerID;

/**
 * Interprets the given byte/word/dword as a world object ID.
 * 
 * @author savormix
 */
public class ObjectID extends ScriptedFieldValueInterpreter implements IntegerInterpreter
{
	@Override
	public Object getInterpretation(long value, ICacheServerID entityCacheContext)
	{
		final StringBuilder sb = new StringBuilder(L2ObjectInfoCache.getOrAdd((int)value, entityCacheContext).getName());
		if (value == 0)
			return sb.toString();
		final int cls = ((int)value >>> SmartIdClass.CLS_SHIFT);
		if (cls < 2) // this translation is used for PKs in a some special cases
			return sb.toString();
		if (cls < SmartIdClass.CLS_INDEX_OFFSET) // fail2j
			return sb.append("*L2j").toString();
		
		// hierarchical, reusable ID details
		// CLASS[index]r[revision]
		return sb.append(" -> ").append(SmartIdClass.toString((int)value)).append('[').append(value & 0x0F_FF_FF).append("]r[").append((value >>> 20) & 0x7F).append(']').toString();
	}
	
	private enum SmartIdClass
	{
		FAKE_ITEM("FITM"), AIRSHIP("ASHP"), ITEM("ITEM"), CREATURE("CRTR"), VEHICLE("VHCL"), STATIC_OBJECT("SOBJ"), PLEDGE("PLDG"), PARTY("PRTY"), GENERAL_OBJECT("GOBJ"), ALLIANCE("ALLY");
		
		static final int CLS_SHIFT = 27, CLS_INDEX_OFFSET = 6;
		
		private final String _abbrev;
		
		SmartIdClass(String abbrev)
		{
			_abbrev = abbrev;
		}
		
		@Override
		public String toString()
		{
			return _abbrev;
		}
		
		public static final String toString(int sid)
		{
			final int cls = (sid >>> CLS_SHIFT);
			final int index = cls - CLS_INDEX_OFFSET;
			if (index < 0 || index > values().length)
				return "What " + cls + " -_- ";
			return values()[index].toString();
		}
	}
}
