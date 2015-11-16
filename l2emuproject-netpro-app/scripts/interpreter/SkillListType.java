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

import java.util.HashMap;
import java.util.Map;

import net.l2emuproject.proxy.script.interpreter.ScriptedIntegerIdInterpreter;

/**
 * Interprets the given byte/word/dword as a type of a skill list.
 * 
 * @author savormix
 */
public final class SkillListType extends ScriptedIntegerIdInterpreter
{
	/** Constructs this interpreter. */
	public SkillListType()
	{
		super(makeMap());
	}
	
	private static final Map<Long, ?> makeMap()
	{
		final Map<Long, String> result = new HashMap<>();
		result.put(0L, "Class");
		result.put(1L, "Fishing");
		result.put(2L, "Pledge");
		result.put(3L, "Pledge unit");
		result.put(5L, "Certification");
		result.put(6L, "Collection");
		result.put(9L, "Linking");
		result.put(57L, "Revelation of Chaos");
		return result;
	}
}
