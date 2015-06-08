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

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javolution.util.FastMap;

import org.apache.commons.lang3.mutable.MutableInt;

import net.l2emuproject.proxy.state.entity.EntityInfo;
import net.l2emuproject.proxy.state.entity.context.ICacheServerID;
import net.l2emuproject.util.L2Collections;
import net.l2emuproject.util.L2FastSet;
import net.l2emuproject.util.concurrent.L2ThreadPool;
import net.l2emuproject.util.logging.L2Logger;

/**
 * Contains information about entities reconstructed from intercepted network traffic.
 * 
 * @param <E> type of entity
 * @author savormix
 */
public abstract class EntityInfoCache<E extends EntityInfo>
{
	private static final L2Logger LOG = L2Logger.getLogger(EntityInfoCache.class);
	private static final Set<EntityInfoCache<?>> CACHES = new L2FastSet<EntityInfoCache<?>>().setShared(true);
	
	private final FastMap<ICacheServerID, FastMap<Integer, E>> _entities;
	
	/** Creates this cache. */
	protected EntityInfoCache()
	{
		_entities = FastMap.newInstance();
		_entities.setShared(true);
		
		CACHES.add(this);
	}
	
	/**
	 * Retrieves an entity with the given ID from this cache, creating it as necessary.
	 * 
	 * @param id entity ID
	 * @param context entity existence boundary defining context
	 * @return an entity with the given ID
	 */
	public final E getOrAdd(int id, ICacheServerID context)
	{
		// LOG.info("getOrAdd() with " + context);
		
		FastMap<Integer, E> cached = _entities.get(context);
		if (cached == null)
			cached = L2Collections.putIfAbsent(_entities, context, new FastMap<Integer, E>().setShared(true));
		return getOrAdd(cached, id);
	}
	
	/**
	 * Retrieves all entities from the given existence context.
	 * 
	 * @param context entity existence boundary defining context
	 * @return entities in context
	 */
	protected Map<Integer, E> getEntities(ICacheServerID context)
	{
		final Map<Integer, E> map = _entities.get(context);
		return map != null ? map : Collections.<Integer, E> emptyMap();
	}
	
	private final E getOrAdd(FastMap<Integer, E> cache, Integer id)
	{
		E cached = cache.get(id);
		if (cached == null)
		{
			final E newE = create(id);
			cached = cache.putIfAbsent(id, newE);
			if (cached == null)
				cached = newE;
		}
		return cached;
	}
	
	private final void remove(ICacheServerID context)
	{
		// LOG.info("remove() with " + context);
		
		FastMap<Integer, E> unused = _entities.remove(context);
		if (unused == null)
			return;
		
		L2ThreadPool.executeLongRunning(new ContextElementRemover(unused.values()));
	}
	
	/**
	 * Creates an entity with the given ID, initializing other fields with default values.
	 * 
	 * @param id entity ID
	 * @return new entity with the given ID
	 */
	protected abstract E create(int id);
	
	private static void removeAll(ICacheServerID context)
	{
		// LOG.info("removeAll() with " + context);
		for (EntityInfoCache<?> cache : CACHES)
			cache.remove(context);
	}
	
	private static final Map<ICacheServerID, MutableInt> SHARED_CONTEXTS = new HashMap<>();
	
	/**
	 * Adds a new entity existence boundary defining context to this cache.<BR>
	 * <BR>
	 * Typically, this is either a game server (shared context) or a historical packet log (unique context per file).
	 * 
	 * @param cacheContext entity context
	 */
	public static final void addSharedContext(ICacheServerID cacheContext)
	{
		final int count;
		synchronized (EntityInfoCache.class)
		{
			MutableInt cnt = SHARED_CONTEXTS.get(cacheContext);
			if (cnt == null)
			{
				cnt = new MutableInt();
				SHARED_CONTEXTS.put(cacheContext, cnt);
			}
			cnt.increment();
			count = cnt.intValue();
		}
		LOG.info("SECC count: " + count + " for " + cacheContext);
	}
	
	/**
	 * Removes an entity existence boundary defining context from this cache. This will remove all cached entities associated with that context.
	 * 
	 * @param cacheContext entity context
	 */
	public static final void removeSharedContext(ICacheServerID cacheContext)
	{
		final int count;
		synchronized (EntityInfoCache.class)
		{
			MutableInt cnt = SHARED_CONTEXTS.get(cacheContext);
			if (cnt != null)
			{
				cnt.decrement();
				count = cnt.intValue();
			}
			else
				count = 0;
		}
		LOG.info("SECC count: " + count + " for " + cacheContext);
		if (count == 0)
			removeAll(cacheContext);
	}
	
	private static final class ContextElementRemover implements Runnable
	{
		private static final L2Logger LOG = L2Logger.getLogger(EntityInfoCache.ContextElementRemover.class);
		
		private final Collection<? extends EntityInfo> _elements;
		
		ContextElementRemover(Collection<? extends EntityInfo> elements)
		{
			_elements = elements;
		}
		
		@Override
		public void run()
		{
			for (final EntityInfo e : _elements)
			{
				try
				{
					e.release();
				}
				catch (RuntimeException ex)
				{
					LOG.error(e.describe(), ex);
				}
			}
		}
	}
}
