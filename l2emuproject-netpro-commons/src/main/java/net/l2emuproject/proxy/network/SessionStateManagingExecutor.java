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
package net.l2emuproject.proxy.network;

import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * @author _dev_
 */
public interface SessionStateManagingExecutor
{
	/**
	 * Returns a session-bound state value.
	 * 
	 * @param client Session key
	 * @param key State identifier
	 * @return state value assigned to the given identifier within a session
	 */
	<T> T getSessionStateFor(Proxy client, Object key);
	
	/**
	 * Returns a session-bound state value.
	 * 
	 * @param client Session key
	 * @param key State identifier
	 * @param defaultValue value to be returned if no value is assigned
	 * @return state value assigned to the given identifier within a session
	 */
	<T> T getSessionStateOrDefaultFor(Proxy client, Object key, T defaultValue);
	
	/**
	 * Returns a session-bound state value, setting a new value if none is present..
	 * 
	 * @param client Session key
	 * @param key State identifier
	 * @param mappingFunction Default value function
	 * @return previous state value assigned to the given identifier (possibly {@code null})
	 */
	<K, V> V computeSessionStateIfAbsentFor(Proxy client, K key, Function<K, V> mappingFunction);
	
	/**
	 * Sets a session-bound state value.
	 * 
	 * @param client Session key
	 * @param key State identifier
	 * @param value State value
	 * @return previous state value assigned to the given identifier (possibly {@code null})
	 */
	Object setSessionStateFor(Proxy client, Object key, Object value);
	
	/**
	 * Removes a session-bound state value.
	 * 
	 * @param client Session key
	 * @param key State identifier
	 * @return state value that was assigned to the given identifier (possibly {@code null})
	 */
	<T> T removeSessionStateFor(Proxy client, Object key);
	
	/**
	 * Removes a session-bound state value only if it matches the given value.
	 * 
	 * @param client Session key
	 * @param key State identifier
	 * @param expectedValue Value to remove
	 * @return {@code expectedValue}, if it was removed, otherwise {@code null}
	 */
	<T> T removeSessionStateFor(Proxy client, Object key, T expectedValue);
	
	/**
	 * Removes a session-bound state value, interrupting/cancelling execution if applicable.
	 * 
	 * @param client Session key
	 * @param key State identifier
	 * @return {@code true} if something was removed, {@code false} if no action was taken
	 */
	default boolean discardSessionStateFor(Proxy client, Object key)
	{
		final Object removed = removeSessionStateFor(client, key);
		if (removed == null)
			return false;
		if (removed instanceof Future)
			((Future<?>)removed).cancel(true);
		return true;
	}
	
	/**
	 * Discards all keys that match the predicate {@code keyMatcher}. If any key maps to a {@link Future} object, the associated task will be interrupted.
	 * 
	 * @param keyMatcher State key matcher
	 */
	void discardSessionStateByKey(Predicate<Object> keyMatcher);
	
	/**
	 * Discards all keys for the given {@code client} that match the predicate {@code keyMatcher}. If any key maps to a {@link Future} object, the associated task will be interrupted.
	 * 
	 * @param client Session key
	 * @param keyMatcher State key matcher
	 */
	void discardSessionStateByKey(Proxy client, Predicate<Object> keyMatcher);
	
	/**
	 * Schedules a task to be executed after (at least) the given delay. If the session is terminated before or during execution, it will be interrupted and/or terminated.
	 * 
	 * @param client Session key
	 * @param key Task identifier
	 * @param r Executable object
	 * @param delay Delay
	 * @param unit Delay unit
	 * @return a scheduled task
	 */
	ScheduledFuture<?> scheduleSessionBound(Proxy client, Object key, Runnable r, long delay, TimeUnit unit);
	
	/**
	 * Schedules a task to be executed after (at least) the given delay. If the session is terminated before or during execution, it will be interrupted and/or terminated.
	 * 
	 * @param client Session key
	 * @param key Task identifier
	 * @param r Executable object
	 * @param initialDelay Delay until first execution
	 * @param delay Delay between subsequent executions
	 * @param unit Delay unit
	 * @return a scheduled task
	 */
	ScheduledFuture<?> scheduleSessionBoundWithFixedDelay(Proxy client, Object key, Runnable r, long initialDelay, long delay, TimeUnit unit);
}
