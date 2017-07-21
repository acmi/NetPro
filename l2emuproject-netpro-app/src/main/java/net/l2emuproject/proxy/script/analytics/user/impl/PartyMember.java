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

/**
 * @author _dev_
 */
public abstract class PartyMember
{
	private final int _smartID;
	private final int _currentHP, _maximumHP, _currentMP, _maximumMP, _currentCP, _maximumCP;
	
	public PartyMember(int smartID, int currentHP, int maximumHP, int currentMP, int maximumMP, int currentCP, int maximumCP)
	{
		super();
		_smartID = smartID;
		_currentHP = currentHP;
		_maximumHP = maximumHP;
		_currentMP = currentMP;
		_maximumMP = maximumMP;
		_currentCP = currentCP;
		_maximumCP = maximumCP;
	}
	
	public int getSmartID()
	{
		return _smartID;
	}
	
	public int getCurrentHP()
	{
		return _currentHP;
	}
	
	public int getMaximumHP()
	{
		return _maximumHP;
	}
	
	public int getCurrentMP()
	{
		return _currentMP;
	}
	
	public int getMaximumMP()
	{
		return _maximumMP;
	}
	
	public int getCurrentCP()
	{
		return _currentCP;
	}
	
	public int getMaximumCP()
	{
		return _maximumCP;
	}
}
