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

import net.l2emuproject.network.protocol.IProtocolVersion;
import net.l2emuproject.proxy.script.interpreter.L2SkillTranslator;
import net.l2emuproject.proxy.script.interpreter.ScriptedIntegerIdInterpreter;
import net.l2emuproject.proxy.state.entity.context.ICacheServerID;

/**
 * Interprets the given byte/word/dword as a skill name ID.
 * 
 * @author savormix
 */
public class SkillNameID extends ScriptedIntegerIdInterpreter
{
	/** Constructs this interpreter. */
	public SkillNameID()
	{
		super(loadFromResource2("skill.txt", (id, lvl) -> L2SkillTranslator.getSkillNameID((int)id, (int)lvl, null)));
	}
	
	@Override
	public Object translate(long value, IProtocolVersion protocol, ICacheServerID entityCacheContext)
	{
		final Object result = super.translate(value, protocol, entityCacheContext);
		if (result instanceof String)
			return result;
		
		return L2SkillTranslator.getSkillID(value) + "_" + L2SkillTranslator.getSkillLevel(value) + "_" + L2SkillTranslator.getSkillSublevel(value);
	}
	
	/**
	 * Returns the highest level for the given skill ID, ignoring entries with sublevels. Returns {@code 0} for unknown skills.
	 * 
	 * @param skillID skill ID
	 * @param protocol protocol version
	 * @return highest non-enchanted level or {@code 0}
	 */
	public int getHighestLevel(int skillID, IProtocolVersion protocol)
	{
		int result = 0;
		for (int level = 1; level < 100; ++level)
		{
			final Object i = super.translate(L2SkillTranslator.getSkillNameID(skillID, level, 0), protocol, null);
			if (!(i instanceof String))
				continue; // it is not mandatory for skills to have level 1, and probably levels in between
				
			result = level;
		}
		return result;
	}
}
