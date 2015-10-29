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
import net.l2emuproject.proxy.script.interpreter.ScriptedBitmaskInterpreter;
import net.l2emuproject.proxy.state.entity.context.ICacheServerID;

/**
 * Interprets the given bit mask as a result of an attack attempt.
 * 
 * @author savormix
 */
public class HitTypes extends ScriptedBitmaskInterpreter
{
	private static final int SS_MASK = 1 << 4;
	private static final String SS_INTERP = "Using soulshot";
	private static final String[] SS_GRADES = { "No", "D", "C", "B", "A", "S", "R", "?" }; // 3 bits = 8 possible values
	
	/** Constructs this interpreter. */
	public HitTypes()
	{
		super(null, (Object)null, null, null, SS_INTERP, "Critical hit", "Deflected by shield", "Missed");
	}
	
	@Override
	public Object getInterpretation(long value, ICacheServerID entityCacheContext)
	{
		if ((value & SS_MASK) != SS_MASK)
			return super.getInterpretation(value, entityCacheContext);
			
		final L2TextBuilder sb = new L2TextBuilder(String.valueOf(super.getInterpretation(value, entityCacheContext)));
		final int insertionIdx = sb.indexOf(SS_INTERP) + SS_INTERP.length();
		sb.insert(insertionIdx, ": " + SS_GRADES[(int)(value & 7)] + " grade");
		return sb.moveToString();
	}
}
