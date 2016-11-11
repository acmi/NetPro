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

import net.l2emuproject.proxy.script.analytics.user.ItemEnchantEffects;

/**
 * Wraps three variation effects as item's enchant effects.
 * 
 * @author _dev_
 */
public final class ItemEnchantEffectsImpl implements ItemEnchantEffects
{
	private final int _effect1, _effect2, _effect3;
	
	public ItemEnchantEffectsImpl(int effect1, int effect2, int effect3)
	{
		_effect1 = effect1;
		_effect2 = effect2;
		_effect3 = effect3;
	}
	
	@Override
	public int getEffect1()
	{
		return _effect1;
	}
	
	@Override
	public int getEffect2()
	{
		return _effect2;
	}
	
	@Override
	public int getEffect3()
	{
		return _effect3;
	}
	
	@Override
	public String toString()
	{
		return String.valueOf(ItemEnchantEffects.toString(this));
	}
	
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + _effect1;
		result = prime * result + _effect2;
		result = prime * result + _effect3;
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
		final ItemEnchantEffectsImpl other = (ItemEnchantEffectsImpl)obj;
		if (_effect1 != other._effect1)
			return false;
		if (_effect2 != other._effect2)
			return false;
		if (_effect3 != other._effect3)
			return false;
		return true;
	}
}
