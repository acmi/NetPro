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
package net.l2emuproject.proxy.script.analytics.user;

import java.util.stream.Stream;

import net.l2emuproject.proxy.state.entity.L2ObjectInfo;
import net.l2emuproject.proxy.state.entity.L2ObjectInfoCache;
import net.l2emuproject.proxy.state.entity.ObjectInfo;
import net.l2emuproject.proxy.state.entity.context.ICacheServerID;
import net.l2emuproject.proxy.state.entity.type.ItemType;
import net.l2emuproject.proxy.state.entity.type.NonPlayerControllable;
import net.l2emuproject.proxy.state.entity.type.PetType;
import net.l2emuproject.proxy.state.entity.type.PlayerControllable;
import net.l2emuproject.proxy.state.entity.type.SummonType;

/**
 * @author _dev_
 */
public interface VisibleWorldObjects
{
	/**
	 * Returns {@code true} iff the game object with the given runtime ID is currently in the immediate vicinity of the user character.
	 * 
	 * @param worldObjectID runtime ID of a world object
	 * @return {@code true} if visible and may be targeted/interacted with, {@code false} otherwise
	 */
	boolean isCurrentlyVisible(int worldObjectID);
	
	/**
	 * Makes a world object visible to the user.
	 * 
	 * @param worldObjectID runtime ID of a world object
	 */
	void add(int worldObjectID);
	
	/**
	 * Makes a world object no longer visible to the user.
	 * 
	 * @param worldObjectID runtime ID of a world object
	 */
	void delete(int worldObjectID);
	
	/**
	 * Returns an inter-player relation if the given world object is both currently visible and a player.
	 * 
	 * @param worldObjectID runtime ID of a world object
	 * @return relation or {@code null}
	 */
	PlayerToPlayerRelation getCurrentRelation(int worldObjectID);
	
	/**
	 * Assigns an inter-player relation if the given world object is currently visible.
	 * 
	 * @param worldObjectID runtime ID of a world object
	 * @param relation relation
	 */
	void setCurrentRelation(int worldObjectID, PlayerToPlayerRelation relation);
	
	ICacheServerID getEntityCacheContext();
	
	Stream<Integer> visibleObjectIDs();
	
	default Stream<ObjectInfo<L2ObjectInfo>> visibleObjects()
	{
		return visibleObjectIDs().map(id -> L2ObjectInfoCache.getOrAdd(id, getEntityCacheContext()));
	}
	
	default Stream<ObjectInfo<L2ObjectInfo>> visibleItems()
	{
		return visibleObjects().filter(o -> o.getType() instanceof ItemType);
	}
	
	default Stream<ObjectInfo<L2ObjectInfo>> visiblePlayers()
	{
		return visibleObjects().filter(o -> o.getType() instanceof PlayerControllable);
	}
	
	default Stream<ObjectInfo<L2ObjectInfo>> visibleNPCs()
	{
		return visibleObjects().filter(o -> o.getType() instanceof NonPlayerControllable);
	}
	
	default Stream<ObjectInfo<L2ObjectInfo>> visiblePets()
	{
		return visibleObjects().filter(o -> o.getType() instanceof PetType);
	}
	
	default Stream<ObjectInfo<L2ObjectInfo>> visibleSummons()
	{
		return visibleObjects().filter(o -> o.getType() instanceof SummonType);
	}
}
