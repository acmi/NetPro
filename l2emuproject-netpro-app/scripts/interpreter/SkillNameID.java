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

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import net.l2emuproject.proxy.script.interpreter.L2SkillTranslator;
import net.l2emuproject.proxy.script.interpreter.ScriptedIntegerIdInterpreter;
import net.l2emuproject.proxy.state.entity.context.ICacheServerID;
import net.l2emuproject.proxy.ui.savormix.io.base.IOConstants;
import net.l2emuproject.util.logging.L2Logger;

/**
 * Interprets the given byte/word/dword as a skill name ID.
 * 
 * @author savormix
 */
public class SkillNameID extends ScriptedIntegerIdInterpreter
{
	private static final L2Logger LOG = L2Logger.getLogger(SkillNameID.class);
	
	/** Constructs this interpreter. */
	public SkillNameID()
	{
		super(loadInterpretations());
	}
	
	private static final Map<Long, String> loadInterpretations()
	{
		final Map<Long, String> mapping = new HashMap<>();
		try (final BufferedReader br = IOConstants.openScriptResource("interpreter", "skill.txt"))
		{
			for (String line; (line = br.readLine()) != null;)
			{
				final int idx = line.indexOf('\t'), idx2 = line.indexOf('\t', idx + 1);
				if (idx == -1 || idx2 == -1)
					continue;
				
				final int id = Integer.parseInt(line.substring(0, idx));
				final int lvl = Integer.parseInt(line.substring(idx + 1, idx2));
				final String name = line.substring(idx2 + 1);
				
				mapping.put(Long.valueOf(L2SkillTranslator.getSkillNameID(id, lvl, null)), name.intern());
			}
		}
		catch (IOException e)
		{
			LOG.error("Could not load skill level interpretations", e);
		}
		mapping.put(Long.valueOf(L2SkillTranslator.getSkillNameID(0, 0, 0)), "None"); // SBSU
		mapping.put(Long.valueOf(L2SkillTranslator.getSkillNameID(0, 1, 0)), "None"); // default interpretation without level
		return mapping;
	}
	
	@Override
	public Object getInterpretation(long value, ICacheServerID entityCacheContext)
	{
		final Object result = super.getInterpretation(value, entityCacheContext);
		if (result instanceof String)
			return result;
		
		return L2SkillTranslator.getSkillID(value) + "_" + L2SkillTranslator.getSkillLevel(value) + "_" + L2SkillTranslator.getSkillSublevel(value);
	}
}
