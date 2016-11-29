/*
 * Copyright 2011-2016 L2EMU UNIQUE
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
package net.l2emuproject.proxy.script.analytics.user.impl;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

import net.l2emuproject.proxy.network.meta.container.MetaclassRegistry;
import net.l2emuproject.proxy.network.meta.exception.InvalidFieldValueInterpreterException;
import net.l2emuproject.proxy.network.meta.interpreter.IntegerInterpreter;
import net.l2emuproject.proxy.script.analytics.user.SkillListSkill;
import net.l2emuproject.proxy.script.interpreter.L2SkillTranslator;

/**
 * Represents a skill from a user's skill list.
 * 
 * @author _dev_
 */
public final class UserSkill implements SkillListSkill
{
	private final Set<SkillFlags> _flags;
	private final int _level, _sublevel, _id;
	
	public UserSkill(boolean passive, int levelAndSublevel, int id, boolean disabled, boolean enchantable)
	{
		final Set<SkillFlags> flags = EnumSet.noneOf(SkillFlags.class);
		if (passive)
			flags.add(SkillFlags.PASSIVE);
		if (disabled)
			flags.add(SkillFlags.DISABLED);
		if (enchantable)
			flags.add(SkillFlags.ENCHANTABLE);
		_flags = Collections.unmodifiableSet(flags);
		_level = L2SkillTranslator.getSkillLevel(levelAndSublevel);
		_sublevel = L2SkillTranslator.getSkillSublevel(levelAndSublevel);
		_id = id;
	}
	
	@Override
	public Set<SkillFlags> getFlags()
	{
		return _flags;
	}
	
	@Override
	public int getLevel()
	{
		return _level;
	}
	
	@Override
	public int getSublevel()
	{
		return _sublevel;
	}
	
	@Override
	public int getID()
	{
		return _id;
	}
	
	@Override
	public String toString()
	{
		try
		{
			final IntegerInterpreter interpreter = MetaclassRegistry.getInstance().getInterpreter("SkillNameID", IntegerInterpreter.class);
			return String.valueOf(interpreter.getInterpretation(L2SkillTranslator.getSkillNameID(_id, _level, _sublevel), null));
		}
		catch (final InvalidFieldValueInterpreterException e)
		{
			return _id + "_" + _level + "_" + _sublevel;
		}
	}
}
