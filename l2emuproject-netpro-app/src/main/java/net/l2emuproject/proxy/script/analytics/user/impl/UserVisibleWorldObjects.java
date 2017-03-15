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

import java.util.HashMap;
import java.util.Map;

import net.l2emuproject.proxy.script.analytics.user.PlayerToPlayerRelation;
import net.l2emuproject.proxy.script.analytics.user.VisibleWorldObjects;

/**
 * @author _dev_
 */
public final class UserVisibleWorldObjects implements VisibleWorldObjects
{
	private final Map<Integer, PlayerToPlayerRelation> _visibleObjects;
	
	/** Constructs this world object visibility tracker. */
	public UserVisibleWorldObjects()
	{
		_visibleObjects = new HashMap<>();
	}
	
	@Override
	public boolean isCurrentlyVisible(int worldObjectID)
	{
		return _visibleObjects.containsKey(worldObjectID);
	}
	
	@Override
	public void add(int worldObjectID)
	{
		_visibleObjects.put(worldObjectID, null);
	}
	
	@Override
	public void delete(int worldObjectID)
	{
		_visibleObjects.remove(worldObjectID);
	}
	
	@Override
	public PlayerToPlayerRelation getCurrentRelation(int worldObjectID)
	{
		return _visibleObjects.get(worldObjectID);
	}
	
	@Override
	public void setCurrentRelation(int worldObjectID, PlayerToPlayerRelation relation)
	{
		_visibleObjects.replace(worldObjectID, relation);
	}
}
