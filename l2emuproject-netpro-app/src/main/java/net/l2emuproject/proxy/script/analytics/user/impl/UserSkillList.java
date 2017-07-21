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
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Stream;

import net.l2emuproject.proxy.script.analytics.user.SkillListSkill;

/**
 * Represents an user's skill list.
 * 
 * @author _dev_
 */
public final class UserSkillList implements Iterable<SkillListSkill>
{
	private Map<Integer, SkillListSkill> _skills;
	
	/** Creates an empty skill list. */
	public UserSkillList()
	{
		setSkills(Collections.emptySet());
	}
	
	/**
	 * Sets the skill list.
	 * 
	 * @param skills skill list
	 */
	public void setSkills(Iterable<SkillListSkill> skills)
	{
		final Map<Integer, SkillListSkill> skillMap = new LinkedHashMap<>();
		for (final SkillListSkill skill : skills)
			skillMap.put(skill.getID(), skill);
		_skills = Collections.unmodifiableMap(skillMap);
	}
	
	/**
	 * Returns a skill with the given ID.
	 * 
	 * @param skillID skill ID
	 * @return a skill or {@code null}
	 */
	public SkillListSkill getByID(int skillID)
	{
		return _skills.get(skillID);
	}
	
	/**
	 * Returns {@code true} if a skill is contained in this list and not currently disabled, {@code false} otherwise.
	 * 
	 * @param skillID skill ID
	 * @return whether the skill is available
	 */
	public boolean isAvailable(int skillID)
	{
		final SkillListSkill skill = getByID(skillID);
		return skill != null && !skill.isDisabled();
	}
	
	/**
	 * Returns {@code true} if a skill is contained in this list, is not a passive skill and is not currently disabled, {@code false} otherwise.
	 * 
	 * @param skillID skill ID
	 * @return whether the skill is requestable
	 */
	public boolean isRequestable(int skillID)
	{
		final SkillListSkill skill = getByID(skillID);
		return skill != null && !skill.isPassive() && !skill.isDisabled();
	}
	
	/**
	 * Returns all skills that are not currently disabled.
	 * 
	 * @return enabled skills
	 */
	public Stream<SkillListSkill> getAvailable()
	{
		return _skills.values().stream().filter(s -> !s.isDisabled());
	}
	
	/**
	 * Returns all non-passive skills that are not currently disabled.
	 * 
	 * @return enabled non-passive skills
	 */
	public Stream<SkillListSkill> getRequestable()
	{
		return getAvailable().filter(s -> !s.isPassive());
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
