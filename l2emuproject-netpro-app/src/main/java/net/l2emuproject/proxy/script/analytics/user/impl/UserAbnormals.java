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

import net.l2emuproject.proxy.script.analytics.user.AbnormalEffectList;
import net.l2emuproject.proxy.script.analytics.user.AbnormalStatusModifier;

/**
 * Wraps the currently active abnormal status modifiers.
 * 
 * @author _dev_
 */
public final class UserAbnormals implements AbnormalEffectList
{
	private Map<Integer, AbnormalStatusModifier> _abnormalModifiers;
	
	/** Creates an empty abnormal status modifier list. */
	public UserAbnormals()
	{
		setAbnormalModifiers(Collections.emptySet());
	}
	
	/**
	 * Sets the current abnormal status modifiers (effects).
	 * 
	 * @param abnormals abnormal status modifiers
	 */
	public void setAbnormalModifiers(Iterable<AbnormalStatusModifier> abnormals)
	{
		final Map<Integer, AbnormalStatusModifier> skillID2Abnormal = new LinkedHashMap<>();
		for (final AbnormalStatusModifier mod : abnormals)
			skillID2Abnormal.put(mod.getSkillID(), mod);
		_abnormalModifiers = skillID2Abnormal;
	}
	
	/**
	 * Adds a new abnormal status modifier (effect).
	 * 
	 * @param abnormal new effect
	 */
	public void addAbnormalModifier(AbnormalStatusModifier abnormal)
	{
		_abnormalModifiers.put(abnormal.getSkillID(), abnormal);
	}
	
	/**
	 * Removes abnormal status modifier (effect).
	 * 
	 * @param skillID effect skill ID
	 */
	public void removeAbnormalModifier(int skillID)
	{
		_abnormalModifiers.remove(skillID);
	}
	
	@Override
	public AbnormalStatusModifier getBySkillID(int skillID, boolean includeExpired)
	{
		final AbnormalStatusModifier modifier = _abnormalModifiers.get(skillID);
		return modifier != null && !modifier.isExpired() ? modifier : null;
	}
	
	@Override
	public boolean isInEffect(int skillID)
	{
		final AbnormalStatusModifier mod = getBySkillID(skillID);
		return mod != null && !mod.isExpired();
	}
	
	@Override
	public Stream<AbnormalStatusModifier> getActiveModifiers()
	{
		return _abnormalModifiers.values().stream().filter(m -> !m.isExpired());
	}
	
	@Override
	public Iterator<AbnormalStatusModifier> iterator()
	{
		return Collections.unmodifiableMap(_abnormalModifiers).values().iterator();
	}
	
	@Override
	public String toString()
	{
		return String.valueOf(_abnormalModifiers.values());
	}
}
