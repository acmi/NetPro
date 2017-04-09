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

import java.util.concurrent.TimeUnit;

/**
 * @author _dev_
 */
public interface SkillCast
{
	/**
	 * Returns the ID of this skill.
	 * 
	 * @return skill ID
	 */
	int getID();
	
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
	 * Returns whether the skill cast has already ended (completed/cancelled).
	 * 
	 * @return is no longer in progress
	 */
	boolean isOver();
	
	/**
	 * Returns time remaining to complete this cast. A negative value is returned if {@link #isOver()} were to return {@code true}.
	 * 
	 * @param unit time unit to return the result in
	 * @return remaining cast time
	 */
	long getRemainingTime(TimeUnit unit);
	
	/**
	 * Returns {@code true} if this cast was interrupted before it could complete.
	 * 
	 * @return whether casting was not allowed to complete
	 */
	boolean isCancelled();
	
	/** Marks this cast as interrupted. */
	void setCancelled();
}
