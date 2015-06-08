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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map.Entry;
import java.util.stream.Stream;

import net.l2emuproject.geometry.IPoint3D;
import net.l2emuproject.geometry.PointGeometry;
import net.l2emuproject.proxy.state.entity.ObjectInfo;
import net.l2emuproject.proxy.state.entity.context.ICacheServerID;
import net.l2emuproject.proxy.state.entity.type.EntityWithTemplateType;
import net.l2emuproject.proxy.state.entity.type.ObjectType;

/**
 * Stores object info for game world objects that use unique world object IDs.
 * 
 * @author savormix
 */
public class ObjectInfoCache extends EntityInfoCache<ObjectInfo>
{
	// just a failsafe
	private static final int KNOWNLIST_RANGE = 3_500;
	
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
	public Collection<ObjectInfo> getAllObjectsByType(ICacheServerID context, Class<? extends ObjectType> type)
	{
		final Collection<ObjectInfo> objects = new ArrayList<>();
		for (Entry<Integer, ObjectInfo> e : getEntities(context).entrySet())
		{
			final ObjectInfo oi = e.getValue();
			if (type.isInstance(oi.getType()))
				objects.add(oi);
		}
		return objects;
	}
	
	/**
	 * Returns known objects of the given type with the given class ID, within the knownlist distance from the given point.
	 * 
	 * @param context entity existence boundary defining context
	 * @param type returned object type
	 * @param templateID returned object template ID
	 * @param origin location or {@code null}
	 * @return known objects that match the given criteria
	 */
	public Stream<ObjectInfo> getKnownObjects(ICacheServerID context, Class<? extends EntityWithTemplateType> type, int templateID, IPoint3D origin)
	{
		final Stream<ObjectInfo> result = getEntities(context).values().stream().filter(oi -> type.isInstance(oi.getType())).filter(oi -> ((EntityWithTemplateType)oi.getType()).getTemplateID() == templateID);
		return origin != null ? result.filter(oi -> PointGeometry.isWithinRawSolidDistance(oi.getCurrentLocation(), origin, KNOWNLIST_RANGE)) : result;
	}
	
	@Override
	protected ObjectInfo create(int id)
	{
		return new ObjectInfo(id);
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
