/*
 * Copyright 2011-2017 L2EMU UNIQUE
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

import java.util.stream.Stream;

/**
 * @author _dev_
 */
public interface AbnormalEffectList extends Iterable<AbnormalStatusModifier>
{
	/**
	 * Returns a modifier associated with the given skill ID, if it hasn't expired yet.
	 * 
	 * @param skillID skill ID
	 * @return an active abnormal status modifier for the given skill ID
	 */
	default AbnormalStatusModifier getBySkillID(int skillID)
	{
		return getBySkillID(skillID, false);
	}
	
	/**
	 * Returns a modifier associated with the given skill ID.
	 * 
	 * @param skillID skill ID
	 * @param includeExpired {@code true} to check the list as it was received, ignoring expiry
	 * @return an active abnormal status modifier for the given skill ID
	 */
	AbnormalStatusModifier getBySkillID(int skillID, boolean includeExpired);
	
	/**
	 * Returns whether there is at least one currently active effect related to the given skill ID.
	 * 
	 * @param skillID skill ID
	 * @return {@code true} if the given skill's abnormal modifier is in effect, {@code false} otherwise
	 */
	boolean isInEffect(int skillID);
	
	/**
	 * Returns all currently active abnormal status modifiers.
	 * 
	 * @return active abnormals
	 */
	Stream<AbnormalStatusModifier> getActiveModifiers();
}
