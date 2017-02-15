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

import java.util.concurrent.TimeUnit;

/**
 * Represents an effect container listed in {@code AbnormalStatusUpdate}.
 * 
 * @author _dev_
 */
public interface AbnormalStatusModifier
{
	/**
	 * Returns the associated skill ID.
	 * 
	 * @return skill ID
	 */
	int getSkillID();
	
	/**
	 * Returns the associated skill level.
	 * 
	 * @return skill level
	 */
	int getSkillLevel();
	
	/**
	 * Returns the associated skill sublevel (enchant route + level).
	 * 
	 * @return skill sublevel (enchant route + level)
	 */
	int getSkillSublevel();
	
	/**
	 * Returns the time remaining until this modifier expires. This method will always return a non-negative value for temporary effects; a negative value indicates a permanent effect.
	 * Due to Lineage II implementation details, the finest granularity available is {@link TimeUnit#SECONDS}.
	 * 
	 * @param timeUnit time unit of the returned value
	 * @return amount of time remaining
	 */
	long getTimeRemaining(TimeUnit timeUnit);
	
	/**
	 * Returns the amount of time, in seconds, until this modifier expires. This method will always return a non-negative value for temporary effects;
	 * a negative value indicates a permanent effect. This is, in fact, the finest granularity available.
	 * 
	 * @return amount of seconds until this modifier expires
	 */
	default int getSecondsRemaining()
	{
		return (int)getTimeRemaining(TimeUnit.SECONDS);
	}
	
	/**
	 * Returns {@code true} if this modifier has already expired and is no longer in effect, {@code false} otherwise.
	 * 
	 * @return {@code true} if this modifier has expired, {@code false} otherwise
	 */
	default boolean isExpired()
	{
		return getTimeRemaining(TimeUnit.NANOSECONDS) == 0;
	}
	
	/**
	 * Returns {@code true} if this modifier has already expired and is no longer in effect, {@code false} otherwise.
	 * 
	 * @return {@code true} if this modifier has expired, {@code false} otherwise
	 */
	default boolean isPermanent()
	{
		return getTimeRemaining(TimeUnit.NANOSECONDS) < 0;
	}
}
