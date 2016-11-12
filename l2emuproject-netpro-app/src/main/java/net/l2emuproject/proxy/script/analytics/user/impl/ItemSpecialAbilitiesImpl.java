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

import java.util.Arrays;

import net.l2emuproject.proxy.script.analytics.user.ItemSpecialAbilities;

/**
 * @author _dev_
 */
public final class ItemSpecialAbilitiesImpl implements ItemSpecialAbilities
{
	private final int[] _primaries, _specials;
	
	/**
	 * Wraps item's soecial abilities.
	 * 
	 * @param primaries set of general SAs
	 * @param specials set of special SAs
	 */
	public ItemSpecialAbilitiesImpl(int[] primaries, int[] specials)
	{
		_primaries = primaries;
		_specials = specials;
	}
	
	@Override
	public int[] getPrimaries()
	{
		return _primaries;
	}
	
	@Override
	public int[] getSpecials()
	{
		return _specials;
	}
	
	@Override
	public String toString()
	{
		return String.valueOf(ItemSpecialAbilities.toString(this));
	}
	
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(_primaries);
		result = prime * result + Arrays.hashCode(_specials);
		return result;
	}
	
	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final ItemSpecialAbilitiesImpl other = (ItemSpecialAbilitiesImpl)obj;
		if (!Arrays.equals(_primaries, other._primaries))
			return false;
		if (!Arrays.equals(_specials, other._specials))
			return false;
		return true;
	}
}
