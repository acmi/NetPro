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

import net.l2emuproject.proxy.script.analytics.user.ItemElementalAttributes;

/**
 * @author _dev_
 */
public final class ItemElementalAttributesImpl implements ItemElementalAttributes
{
	private final int _atkElement, _atkPower;
	private final int _defFire, _defWater, _defWind, _defEarth, _defHoly, _defDark;
	
	public ItemElementalAttributesImpl(int atkElement, int atkPower, int defFire, int defWater, int defWind, int defEarth, int defHoly, int defDark)
	{
		_atkElement = atkElement;
		_atkPower = atkPower;
		_defFire = defFire;
		_defWater = defWater;
		_defWind = defWind;
		_defEarth = defEarth;
		_defHoly = defHoly;
		_defDark = defDark;
	}
	
	@Override
	public int getAtkElement()
	{
		return _atkElement;
	}
	
	@Override
	public int getAtkPower()
	{
		return _atkPower;
	}
	
	@Override
	public int getDefFire()
	{
		return _defFire;
	}
	
	@Override
	public int getDefWater()
	{
		return _defWater;
	}
	
	@Override
	public int getDefWind()
	{
		return _defWind;
	}
	
	@Override
	public int getDefEarth()
	{
		return _defEarth;
	}
	
	@Override
	public int getDefHoly()
	{
		return _defHoly;
	}
	
	@Override
	public int getDefDark()
	{
		return _defDark;
	}
	
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + _atkElement;
		result = prime * result + _atkPower;
		result = prime * result + _defDark;
		result = prime * result + _defEarth;
		result = prime * result + _defFire;
		result = prime * result + _defHoly;
		result = prime * result + _defWater;
		result = prime * result + _defWind;
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
		final ItemElementalAttributesImpl other = (ItemElementalAttributesImpl)obj;
		if (_atkElement != other._atkElement)
			return false;
		if (_atkPower != other._atkPower)
			return false;
		if (_defDark != other._defDark)
			return false;
		if (_defEarth != other._defEarth)
			return false;
		if (_defFire != other._defFire)
			return false;
		if (_defHoly != other._defHoly)
			return false;
		if (_defWater != other._defWater)
			return false;
		if (_defWind != other._defWind)
			return false;
		return true;
	}
}
