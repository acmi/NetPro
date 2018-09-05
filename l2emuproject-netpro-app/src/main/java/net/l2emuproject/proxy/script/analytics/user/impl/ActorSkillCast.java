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
package net.l2emuproject.proxy.script.analytics.user.impl;

import java.util.concurrent.TimeUnit;

import net.l2emuproject.proxy.network.meta.container.MetaclassRegistry;
import net.l2emuproject.proxy.network.meta.exception.InvalidFieldValueInterpreterException;
import net.l2emuproject.proxy.network.meta.interpreter.IntegerTranslator;
import net.l2emuproject.proxy.script.analytics.user.SkillCast;
import net.l2emuproject.proxy.script.interpreter.L2SkillTranslator;
import net.l2emuproject.util.TimeAmountInterpreter;

/**
 * @author _dev_
 */
public final class ActorSkillCast implements SkillCast
{
	private final int _id, _level, _sublevel;
	private final long _endTime;
	private boolean _cancelled;
	
	/**
	 * Creates a skill cast.
	 * 
	 * @param skillID skill ID
	 * @param skillLevel skill level
	 * @param skillSublevel skill sublevel
	 * @param startTime nanosecond timestamp at cast start
	 * @param duration cast time
	 * @param durationUnit cast time unit
	 */
	public ActorSkillCast(int skillID, int skillLevel, int skillSublevel, long startTime, long duration, TimeUnit durationUnit)
	{
		_id = skillID;
		_level = skillLevel;
		_sublevel = skillSublevel;
		_endTime = startTime + durationUnit.toNanos(duration);
		_cancelled = false;
	}
	
	@Override
	public int getID()
	{
		return _id;
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
	public boolean isOver()
	{
		// overflow conscious code
		return _endTime - System.nanoTime() <= 0;
	}
	
	@Override
	public long getRemainingTime(TimeUnit unit)
	{
		return _cancelled ? -1 : unit.convert(_endTime - System.nanoTime(), TimeUnit.NANOSECONDS);
	}
	
	@Override
	public boolean isCancelled()
	{
		return _cancelled;
	}
	
	@Override
	public void setCancelled()
	{
		_cancelled = true;
	}
	
	@Override
	public String toString()
	{
		final long remainingTime = getRemainingTime(TimeUnit.MILLISECONDS);
		final String remainTime = "[" + (_cancelled ? "CNCL" : (remainingTime <= 0 ? "DONE" : TimeAmountInterpreter.consolidateMillis(remainingTime))) + "]";
		try
		{
			final IntegerTranslator interpreter = MetaclassRegistry.getInstance().getTranslator("SkillNameID", IntegerTranslator.class);
			return remainTime + String.valueOf(interpreter.translate(L2SkillTranslator.getSkillNameID(_id, _level, _sublevel), null));
		}
		catch (final InvalidFieldValueInterpreterException e)
		{
			return remainTime + _id + "_" + _level + "_" + _sublevel;
		}
	}
}
