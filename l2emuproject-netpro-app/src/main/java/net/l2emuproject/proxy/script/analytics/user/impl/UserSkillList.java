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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.Stream;

import net.l2emuproject.proxy.script.analytics.user.SkillListSkill;

/**
 * @author _dev_
 */
public final class UserSkillList implements Iterable<SkillListSkill>
{
	private volatile Map<Integer, SkillListSkill> _skills;
	
	/** Creates an empty skill list. */
	public UserSkillList()
	{
		setSkills(Collections.emptySet());
	}
	
	public void setSkills(Iterable<SkillListSkill> skills)
	{
		final Map<Integer, SkillListSkill> skillMap = new HashMap<>();
		for (final SkillListSkill skill : skills)
			skillMap.put(skill.getID(), skill);
		_skills = Collections.unmodifiableMap(skillMap);
	}
	
	public SkillListSkill getByID(int skillID)
	{
		return _skills.get(skillID);
	}
	
	public boolean isAvailable(int skillID)
	{
		final SkillListSkill skill = getByID(skillID);
		return skill != null && !skill.isDisabled();
	}
	
	public boolean isRequestable(int skillID)
	{
		final SkillListSkill skill = getByID(skillID);
		return skill != null && !skill.isPassive() && !skill.isDisabled();
	}
	
	public Stream<SkillListSkill> getAvailable()
	{
		return _skills.values().stream().filter(s -> !s.isDisabled());
	}
	
	public Stream<SkillListSkill> getRequestable()
	{
		return _skills.values().stream().filter(s -> !s.isPassive() && !s.isDisabled());
	}
	
	@Override
	public Iterator<SkillListSkill> iterator()
	{
		return _skills.values().iterator();
	}
	
	@Override
	public String toString()
	{
		return String.valueOf(_skills.values());
	}
}
