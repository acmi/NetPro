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

import java.util.Map;
import java.util.Map.Entry;

import eu.revengineer.simplejse.HasScriptDependencies;
import eu.revengineer.simplejse.init.InitializationPriority;

import net.l2emuproject.proxy.network.meta.container.MetaclassRegistry;
import net.l2emuproject.proxy.network.meta.exception.InvalidFieldValueInterpreterException;
import net.l2emuproject.proxy.script.ScriptedMetaclass;
import net.l2emuproject.proxy.script.interpreter.ScriptedIntegerIdInterpreter;

/**
 * Interprets the given byte/word/dword as a transformation ID.
 * 
 * @author savormix
 */
@HasScriptDependencies("interpreter.Npc")
@InitializationPriority(1)
public class Transformation extends ScriptedIntegerIdInterpreter
{
	/** Constructs this interpreter. */
	public Transformation()
	{
		super(convertToNPCs(loadFromResource("trans.txt")));
	}
	
	private static final Map<Long, String> convertToNPCs(Map<Long, String> transformations)
	{
		try
		{
			final Npc interpreter = MetaclassRegistry.getInstance().getInterpreter(ScriptedMetaclass.getAlias(Npc.class), Npc.class);
			for (final Entry<Long, String> e : transformations.entrySet())
				e.setValue(String.valueOf(interpreter.getInterpretation(Long.parseLong(e.getValue()))));
		}
		catch (InvalidFieldValueInterpreterException e)
		{
			// the NPC interpreter might be disabled
		}
		transformations.put(0L, "None");
		return transformations;
	}
}
