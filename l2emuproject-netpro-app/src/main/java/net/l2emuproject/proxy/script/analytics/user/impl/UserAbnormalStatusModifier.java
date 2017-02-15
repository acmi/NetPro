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

import java.util.concurrent.TimeUnit;

import net.l2emuproject.proxy.network.meta.container.MetaclassRegistry;
import net.l2emuproject.proxy.network.meta.exception.InvalidFieldValueInterpreterException;
import net.l2emuproject.proxy.network.meta.interpreter.IntegerInterpreter;
import net.l2emuproject.proxy.script.analytics.user.AbnormalStatusModifier;
import net.l2emuproject.proxy.script.interpreter.L2SkillTranslator;
import net.l2emuproject.util.TimeAmountInterpreter;

/**
 * Represents an abnormal status modifier (effect).
 * 
 * @author _dev_
 */
public final class UserAbnormalStatusModifier implements AbnormalStatusModifier
{
	private final int _skillID, _skillLevel, _skillSublevel;
	private final long _nanosecondsRemaining, _nanoTimeOnArrival;
	
	/**
	 * Creates a temporary status modifier.
	 * 
	 * @param skillID associated skill ID
	 * @param skillLevelAndSublevel associated skill level and sublevel
	 * @param secondsRemaining seconds until this modifier expires
	 * @param nanoTimeOnArrival {@link System#nanoTime()} of when the previous parameter was received
	 */
	public UserAbnormalStatusModifier(int skillID, int skillLevelAndSublevel, int secondsRemaining, long nanoTimeOnArrival)
	{
		_skillID = skillID;
		_skillLevel = L2SkillTranslator.getSkillLevel(skillLevelAndSublevel);
		_skillSublevel = L2SkillTranslator.getSkillSublevel(skillLevelAndSublevel);
		_nanosecondsRemaining = secondsRemaining < 0 ? -1 : TimeUnit.SECONDS.toNanos(secondsRemaining);
		_nanoTimeOnArrival = nanoTimeOnArrival;
	}
	
	@Override
	public int getSkillID()
	{
		return _skillID;
	}
	
	@Override
	public int getSkillLevel()
	{
		return _skillLevel;
	}
	
	@Override
	public int getSkillSublevel()
	{
		return _skillSublevel;
	}
	
	@Override
	public long getTimeRemaining(TimeUnit timeUnit)
	{
		return _nanosecondsRemaining >= 0 ? timeUnit.convert(Math.max(0L, (_nanoTimeOnArrival + _nanosecondsRemaining) - System.nanoTime()), TimeUnit.NANOSECONDS) : -1;
	}
	
	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder();
		try
		{
			final IntegerInterpreter interpreter = MetaclassRegistry.getInstance().getInterpreter("SkillNameID", IntegerInterpreter.class);
			sb.append(interpreter.getInterpretation(L2SkillTranslator.getSkillNameID(_skillID, _skillLevel, _skillSublevel), null));
		}
		catch (final InvalidFieldValueInterpreterException e)
		{
			sb.append(_skillID).append('_').append(_skillLevel).append('_').append(_skillSublevel);
		}
		if (isExpired())
			sb.append(" [EXPIRED]");
		else if (!isPermanent())
			sb.append(" [").append(TimeAmountInterpreter.consolidate(getSecondsRemaining(), TimeUnit.SECONDS)).append(" remaining]");
		return sb.toString();
	}
}
