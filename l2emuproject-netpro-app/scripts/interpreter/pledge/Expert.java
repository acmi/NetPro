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
package interpreter.pledge;

import java.util.Map;
import java.util.Map.Entry;

import eu.revengineer.simplejse.HasScriptDependencies;
import eu.revengineer.simplejse.init.InitializationPriority;

import net.l2emuproject.proxy.network.meta.container.MetaclassRegistry;
import net.l2emuproject.proxy.network.meta.exception.InvalidFieldValueInterpreterException;
import net.l2emuproject.proxy.script.ScriptedMetaclass;
import net.l2emuproject.proxy.script.interpreter.ScriptedIntegerIdInterpreter;

import interpreter.SysString;

/**
 * Interprets the given byte/word/dword as a decorative NPC ID.
 * 
 * @author savormix
 */
@HasScriptDependencies("interpreter.SysString")
@InitializationPriority(1)
public class Expert extends ScriptedIntegerIdInterpreter
{
	/** Constructs this interpreter. */
	public Expert()
	{
		super(convertToSysStrings(loadFromResource("expert.txt")));
	}
	
	private static final Map<Long, String> convertToSysStrings(Map<Long, String> strings)
	{
		try
		{
			final SysString interpreter = MetaclassRegistry.getInstance().getInterpreter(ScriptedMetaclass.getAlias(SysString.class), SysString.class);
			for (final Entry<Long, String> e : strings.entrySet())
				e.setValue(String.valueOf(interpreter.getInterpretation(Long.parseLong(e.getValue()))));
		}
		catch (final InvalidFieldValueInterpreterException e)
		{
			// the sysstring interpreter might be disabled
		}
		return strings;
	}
}
