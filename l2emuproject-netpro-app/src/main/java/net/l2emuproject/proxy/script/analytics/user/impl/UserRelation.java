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

import net.l2emuproject.proxy.script.analytics.user.PlayerToPlayerRelation;

/**
 * @author _dev_
 */
public final class UserRelation implements PlayerToPlayerRelation
{
	private final int _otherPlayerOID, _relationMask;
	private final boolean _defaultInteractionAttack;
	private final int _reputation;
	private final Boolean _isInPvP;
	
	public UserRelation(int otherPlayerOID, int relationMask, boolean defaultInteractionAttack, int reputation, int isInPvP)
	{
		_otherPlayerOID = otherPlayerOID;
		_relationMask = relationMask;
		_defaultInteractionAttack = defaultInteractionAttack;
		_reputation = reputation;
		_isInPvP = isInPvP == 2 ? null : (isInPvP == 0 ? Boolean.FALSE : Boolean.TRUE);
	}
	
	@Override
	public int getOtherPlayerOID()
	{
		return _otherPlayerOID;
	}
	
	@Override
	public int getRelationMask()
	{
		return _relationMask;
	}
	
	@Override
	public boolean isDefaultInteractionAttack()
	{
		return _defaultInteractionAttack;
	}
	
	@Override
	public int getReputation()
	{
		return _reputation;
	}
	
	@Override
	public Boolean isInPvP()
	{
		return _isInPvP;
	}
}
