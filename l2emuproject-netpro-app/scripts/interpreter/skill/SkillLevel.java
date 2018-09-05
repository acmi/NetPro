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
package interpreter.skill;

import net.l2emuproject.network.protocol.IProtocolVersion;
import net.l2emuproject.proxy.network.meta.interpreter.IntegerTranslator;
import net.l2emuproject.proxy.script.interpreter.L2SkillTranslator;
import net.l2emuproject.proxy.script.interpreter.ScriptedFieldValueInterpreter;
import net.l2emuproject.proxy.state.entity.context.ICacheServerID;

/**
 * An interpreter compatible with both legacy skill levels and IO+ joint level+sublevel fields.
 * Take note, IO client does not expect 2 separate fields for skill level, but a single (joint) one.
 * 
 * @author _dev_
 */
public final class SkillLevel extends ScriptedFieldValueInterpreter implements IntegerTranslator
{
	@Override
	public Object translate(long value, IProtocolVersion protocol, ICacheServerID entityCacheContext)
	{
		final int level = L2SkillTranslator.getSkillLevel(value), sublevel = L2SkillTranslator.getSkillSublevel(value);
		if (sublevel == 0)
			return level;
		
		return level + ", sublevel: " + sublevel;
	}
}
