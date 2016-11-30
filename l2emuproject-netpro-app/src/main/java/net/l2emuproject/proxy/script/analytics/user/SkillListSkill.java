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
package net.l2emuproject.proxy.script.analytics.user;

import java.util.Set;

/**
 * Describes a skill listed in the user's skill list.
 * 
 * @author _dev_
 */
public interface SkillListSkill
{
	/**
	 * Extra information about this skill
	 * 
	 * @return a set of extras
	 */
	Set<SkillFlags> getFlags();
	
	/**
	 * Returns the level of this skill.
	 * 
	 * @return skill level
	 */
	int getLevel();
	
	/**
	 * Returns the sublevel (enchant route + level) of this skill.
	 * 
	 * @return sublevel (enchant route + level)
	 */
	int getSublevel();
	
	/**
	 * Returns the ID of this skill.
	 * 
	 * @return skill ID
	 */
	int getID();
	
	/**
	 * Returns whether this skill is passive.
	 * 
	 * @return {@code true} for a passive skill, {@code false} otherwise
	 */
	default boolean isPassive()
	{
		return getFlags().contains(SkillFlags.PASSIVE);
	}
	
	/**
	 * Returns whether this skill is disabled (unavailable).
	 * 
	 * @return {@code true} for a currently disabled skill, {@code false} otherwise
	 */
	default boolean isDisabled()
	{
		return getFlags().contains(SkillFlags.DISABLED);
	}
	
	/**
	 * Returns whether this skill is enchantable.
	 * 
	 * @return {@code true} for an enchantable skill, {@code false} otherwise
	 */
	default boolean isEnchantable()
	{
		return getFlags().contains(SkillFlags.ENCHANTABLE);
	}
	
	/**
	 * Extra information about current skill state.
	 * 
	 * @author _dev_
	 */
	enum SkillFlags
	{
		PASSIVE, DISABLED, ENCHANTABLE;
	}
}
