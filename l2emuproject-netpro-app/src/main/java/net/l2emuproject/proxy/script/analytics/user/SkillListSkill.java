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
	Set<SkillFlags> getFlags();
	
	int getLevel();
	
	int getSublevel();
	
	int getID();
	
	default boolean isPassive()
	{
		return getFlags().contains(SkillFlags.PASSIVE);
	}
	
	default boolean isDisabled()
	{
		return getFlags().contains(SkillFlags.DISABLED);
	}
	
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
