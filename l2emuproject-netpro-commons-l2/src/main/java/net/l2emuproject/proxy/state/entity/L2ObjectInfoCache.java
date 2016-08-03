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
package net.l2emuproject.proxy.state.entity;

import java.util.function.Supplier;
import java.util.stream.Stream;

import net.l2emuproject.geometry.point.IPoint3D;
import net.l2emuproject.geometry.point.PointGeometry;
import net.l2emuproject.proxy.state.entity.cache.ObjectInfoCache;
import net.l2emuproject.proxy.state.entity.context.ICacheServerID;
import net.l2emuproject.proxy.state.entity.type.EntityWithTemplateType;

/**
 * @author _dev_
 */
public final class L2ObjectInfoCache
{
	private L2ObjectInfoCache()
	{
		// utility class
	}
	
	private static final Supplier<Object> WRAPPER_SUPPLIER = new L2ObjectInfoSupplier();
	// just a failsafe
	private static final int KNOWNLIST_RANGE = 3_500;
	
	/**
	 * Returns known objects of the given type with the given class ID, within the knownlist distance from the given point.
	 * 
	 * @param context entity existence boundary defining context
	 * @param type returned object type
	 * @param templateID returned object template ID
	 * @param origin location or {@code null}
	 * @return known objects that match the given criteria
	 */
	@SuppressWarnings("unchecked")
	public static final Stream<ObjectInfo<L2ObjectInfo>> getKnownObjects(ICacheServerID context, Class<? extends EntityWithTemplateType> type, int templateID, IPoint3D origin)
	{
		final Stream<ObjectInfo<L2ObjectInfo>> result = ObjectInfoCache.getInstance().getAllObjectsByType(context, type)
				.filter(oi -> ((EntityWithTemplateType)oi.getType()).getTemplateID() == templateID).map(oi -> (ObjectInfo<L2ObjectInfo>)oi);
		return origin != null ? result.filter(oi -> PointGeometry.isWithinRawSolidDistance(oi.getExtraInfo().getCurrentLocation(), origin, KNOWNLIST_RANGE)) : result;
	}
	
	/**
	 * Retrieves an entity with the given ID from this cache, creating it as necessary.
	 * 
	 * @param id entity ID
	 * @param context entity existence boundary defining context
	 * @return an entity with the given ID
	 */
	@SuppressWarnings("unchecked")
	public static final ObjectInfo<L2ObjectInfo> getOrAdd(int id, ICacheServerID context)
	{
		return (ObjectInfo<L2ObjectInfo>)ObjectInfoCache.getInstance().getOrAdd(id, WRAPPER_SUPPLIER, context);
	}
	
	private static final class L2ObjectInfoSupplier implements Supplier<Object>
	{
		L2ObjectInfoSupplier()
		{
			// nothing to initialize
		}
		
		@Override
		public Object get()
		{
			return new L2ObjectInfo();
		}
	}
}
