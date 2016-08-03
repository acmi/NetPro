/*
 * Copyright 2011-2015 L2EMU UNIQUE
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
package net.l2emuproject.proxy.state.entity.cache;

import java.util.stream.Stream;

import net.l2emuproject.proxy.state.entity.ObjectInfo;
import net.l2emuproject.proxy.state.entity.context.ICacheServerID;
import net.l2emuproject.proxy.state.entity.type.ObjectType;

/**
 * Stores object info for game world objects that use unique world object IDs.
 * 
 * @author savormix
 */
public class ObjectInfoCache extends EntityInfoCache<ObjectInfo<?>>
{
	ObjectInfoCache()
	{
		// singleton
	}
	
	/**
	 * Retrieve all known world objects of a specific subtype.
	 * 
	 * @param context entity context
	 * @param type object subtype
	 * @return all objects of the given type
	 */
	public Stream<ObjectInfo<?>> getAllObjectsByType(ICacheServerID context, Class<? extends ObjectType> type)
	{
		return getEntities(context).values().stream().filter(oi -> type.isInstance(oi.getType()));
	}
	
	@Override
	protected ObjectInfo<?> create(int id, Object extraInfo)
	{
		return new ObjectInfo<>(id, extraInfo);
	}
	
	/**
	 * Returns a singleton instance of this type.
	 * 
	 * @return an instance of this class
	 */
	public static final ObjectInfoCache getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static final class SingletonHolder
	{
		static final ObjectInfoCache INSTANCE = new ObjectInfoCache();
	}
}
