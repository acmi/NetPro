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
package net.l2emuproject.proxy.network;

import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Predicate;

import net.l2emuproject.lang.NetProThreadPriority;
import net.l2emuproject.proxy.network.Proxy.AsyncDisconnectionNotifier;
import net.l2emuproject.proxy.network.listener.PacketManipulator;
import net.l2emuproject.util.concurrent.RunnableStatsManager;
import net.l2emuproject.util.logging.L2Logger;

/**
 * This executor is designed to execute asynchronous packet arrival/departure notifications to packet listeners/manipulators, while retaining their order.<BR>
 * Built-in performance monitoring ensures that any anomalies can be quickly detected and taken care of.
 * 
 * @author savormix
 */
public class ForwardedNotificationExecutor extends ScheduledThreadPoolExecutor implements NetProThreadPriority, SessionStateManagingExecutor
{
	private static final L2Logger LOG = L2Logger.getLogger(ForwardedNotificationExecutor.class);
	
	private static final int SINGLE_SEQUENTIAL_LISTENER_WARNING_THRESHOLD = 5;
	
	private final Map<Proxy, Map<Object, Object>> _sessionStateMap;
	
	private Thread _activeThread;
	// perfectly possible due to 1 thread
	private long _start;
	

	/**
	 * Creates a single-threaded executor for packet notifications.
	 * 
	 * @param id an individual identifier
	 */
	ForwardedNotificationExecutor(int id)
	{
		super(0);
		
		setThreadFactory(r -> {
			_activeThread = new Thread(r, "AsyncPacketHandlerHub-" + id);
			_activeThread.setPriority(ASYNC_PACKET_NOTIFIER);
			return _activeThread;
		});
		setMaximumPoolSize(1);
		setKeepAliveTime(5, TimeUnit.MINUTES);
		
		_sessionStateMap = new IdentityHashMap<>();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T> T getSessionStateFor(Proxy client, Object key)
	{
		validateCall(client, false);
		return (T)_sessionStateMap.getOrDefault(client, Collections.emptyMap()).get(key);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T> T getSessionStateOrDefaultFor(Proxy client, Object key, T defaultValue)
	{
		validateCall(client, false);
		return (T)_sessionStateMap.getOrDefault(client, Collections.emptyMap()).getOrDefault(key, defaultValue);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <K, V> V computeSessionStateIfAbsentFor(Proxy client, K key, Function<K, V> mappingFunction)
	{
		validateCall(client, true);
		return (V)_sessionStateMap.computeIfAbsent(client, k -> new HashMap<>()).computeIfAbsent(key, (Function<? super Object, ? extends Object>)mappingFunction);
	}
	
	@Override
	public Object setSessionStateFor(Proxy client, Object key, Object value)
	{
		validateCall(client, true);
		return _sessionStateMap.computeIfAbsent(client, k -> new HashMap<>()).put(key, value);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T> T removeSessionStateFor(Proxy client, Object key)
	{
		validateCall(client, true);
		final Map<Object, Object> stateMap = _sessionStateMap.get(client);
		return stateMap != null ? (T)stateMap.remove(key) : null;
	}
	
	@Override
	public <T> T removeSessionStateFor(Proxy client, Object key, T expectedValue)
	{
		validateCall(client, true);
		final Map<Object, Object> stateMap = _sessionStateMap.get(client);
		return stateMap != null ? (stateMap.remove(key, expectedValue) ? expectedValue : null) : null;
	}
	
	@Override
	public void discardSessionStateByKey(Predicate<Object> keyMatcher)
	{
		if (Thread.currentThread() != _activeThread)
		{
			execute(() -> discardSessionStateByKey(keyMatcher));
			return;
		}
		
		final SortedSet<String> discarded = new TreeSet<>(), cancelled = new TreeSet<>();
		for (final Map<Object, Object> stateMap : _sessionStateMap.values())
		{
			final Iterator<Entry<Object, Object>> it = stateMap.entrySet().iterator();
			while (it.hasNext())
			{
				final Entry<Object, Object> e = it.next();
				if (!keyMatcher.test(e.getKey()))
					continue;
				
				final String reportedKey = String.valueOf(e.getKey());
				final Object value = e.getValue();
				
				if (!(value instanceof Future))
				{
					discarded.add(reportedKey);
					it.remove();
					continue;
				}
				
				final Future<?> task = (Future<?>)value;
				if (!task.isDone())
				{
					task.cancel(true);
					cancelled.add(reportedKey);
				}
				else
					discarded.add(reportedKey);
				it.remove();
			}
		}
		if (!discarded.isEmpty() || !cancelled.isEmpty())
			LOG.info("\r\nDiscarded: " + discarded + "\r\nCancelled: " + cancelled);
	}
	
	@Override
	public void discardSessionStateByKey(Proxy client, Predicate<Object> keyMatcher)
	{
		final SortedSet<String> discarded = new TreeSet<>(), cancelled = new TreeSet<>();
		final Map<Object, Object> stateMap = _sessionStateMap.getOrDefault(client, Collections.emptyMap());
		final Iterator<Entry<Object, Object>> it = stateMap.entrySet().iterator();
		while (it.hasNext())
		{
			final Entry<Object, Object> e = it.next();
			if (!keyMatcher.test(e.getKey()))
				continue;
			
			final String reportedKey = String.valueOf(e.getKey());
			final Object value = e.getValue();
			
			if (!(value instanceof Future))
			{
				discarded.add(reportedKey);
				it.remove();
				continue;
			}
			
			final Future<?> task = (Future<?>)value;
			if (!task.isDone())
			{
				task.cancel(true);
				cancelled.add(reportedKey);
			}
			else
				discarded.add(reportedKey);
			it.remove();
		}
		if (!discarded.isEmpty() || !cancelled.isEmpty())
			LOG.info("\r\nDiscarded: " + discarded + "\r\nCancelled: " + cancelled);
	}
	
	@Override
	public Future<?> executeSessionBound(Proxy client, Object key, Runnable r)
	{
		if (Thread.currentThread() != _activeThread)
		{
			// auto-assume call from a long-running task thread
			try
			{
				return submit(() -> executeSessionBound(client, key, r)).get();
			}
			catch (InterruptedException | ExecutionException e)
			{
				throw new RuntimeException(e);
			}
		}
		
		final Object oldValue = removeSessionStateFor(client, key);
		if (oldValue instanceof Future<?>)
			((Future<?>)oldValue).cancel(true);
		final BlockingQueue<Future<?>> expectedValue = new ArrayBlockingQueue<>(1);
		final Future<?> newValue = submit(() -> {
			try
			{
				r.run();
			}
			catch (final RuntimeException e)
			{
				LOG.error(key, e);
			}
			finally
			{
				try
				{
					removeSessionStateFor(client, key, expectedValue.take());
				}
				catch (final InterruptedException e)
				{
					// application is shutting down, so whatever, really
				}
			}
		});
		setSessionStateFor(client, key, newValue);
		expectedValue.add(newValue);
		return newValue;
	}
	
	@Override
	public ScheduledFuture<?> scheduleSessionBound(Proxy client, Object key, Runnable r, long delay, TimeUnit unit)
	{
		if (Thread.currentThread() != _activeThread)
		{
			// auto-assume call from a long-running task thread
			try
			{
				return submit(() -> scheduleSessionBound(client, key, r, delay, unit)).get();
			}
			catch (InterruptedException | ExecutionException e)
			{
				throw new RuntimeException(e);
			}
		}
		
		final Object oldValue = removeSessionStateFor(client, key);
		if (oldValue instanceof Future<?>)
			((Future<?>)oldValue).cancel(true);
		final BlockingQueue<ScheduledFuture<?>> expectedValue = new ArrayBlockingQueue<>(1);
		final ScheduledFuture<?> newValue = schedule(() -> {
			try
			{
				r.run();
			}
			catch (final RuntimeException e)
			{
				LOG.error(key, e);
			}
			finally
			{
				try
				{
					removeSessionStateFor(client, key, expectedValue.take());
				}
				catch (final InterruptedException e)
				{
					// application is shutting down, so whatever, really
				}
			}
		}, delay, unit);
		setSessionStateFor(client, key, newValue);
		expectedValue.add(newValue);
		return newValue;
	}
	
	@Override
	public ScheduledFuture<?> scheduleSessionBoundWithFixedDelay(Proxy client, Object key, Runnable r, long initialDelay, long delay, TimeUnit unit)
	{
		if (Thread.currentThread() != _activeThread)
		{
			// auto-assume call from a long-running task thread
			try
			{
				return submit(() -> scheduleSessionBoundWithFixedDelay(client, key, r, initialDelay, delay, unit)).get();
			}
			catch (InterruptedException | ExecutionException e)
			{
				throw new RuntimeException(e);
			}
		}
		
		final Object oldValue = removeSessionStateFor(client, key);
		if (oldValue instanceof Future<?>)
			((Future<?>)oldValue).cancel(true);
		final ScheduledFuture<?> newValue = scheduleWithFixedDelay(r, initialDelay, delay, unit);
		setSessionStateFor(client, key, newValue);
		return newValue;
	}
	
	private void validateCall(Proxy client, boolean denyOpIfDisconnected)
	{
		if (client != client.getClient())
			throw new IllegalArgumentException("Session state must be assigned to client");
		if (Thread.currentThread() != _activeThread)
			throw new IllegalMonitorStateException("Thread unsafe call");
		if (denyOpIfDisconnected && (client.isDced() || client.isFailed()))
			throw new IllegalStateException("State mutation while discard pending");
	}
	
	private void discardSessionState(Proxy client)
	{
		final Map<Object, Object> sessionState = _sessionStateMap.remove(client);
		if (sessionState == null)
			return;
		
		int total = 0;
		for (final Object value : sessionState.values())
		{
			if (value instanceof Future<?>)
			{
				((Future<?>)value).cancel(true);
				++total;
			}
		}
		if (total > 0)
			LOG.info("[DC] Terminated " + total + " pending script-related tasks.");
	}
	
	@Override
	protected void afterExecute(Runnable r, Throwable t)
	{
		// defy proper nesting
		final long end = System.nanoTime();
		
		super.afterExecute(r, t);
		
		// but still adhere to it
		if (r instanceof ManipForwardNotifier)
		{
			final ManipForwardNotifier fn = (ManipForwardNotifier)r;
			final PacketManipulator pm = fn.getManip();
			if (t != null)
			{
				LOG.error(pm, t);
				return;
			}
			
			RunnableStatsManager.handleStats(pm.getClass(), "packetForwarded(Proxy, Proxy, ByteBuffer, ByteBuffer)", end - _start, SINGLE_SEQUENTIAL_LISTENER_WARNING_THRESHOLD);
		}
		else if (r instanceof ListenerForwardNotifier)
		{
			final ListenerForwardNotifier fn = (ListenerForwardNotifier)r;
			RunnableStatsManager.handleStats(fn.getListener().getClass(), "onPacket(Proxy, Proxy, ByteBuffer, long)", end - _start, SINGLE_SEQUENTIAL_LISTENER_WARNING_THRESHOLD);
		}
		else if (r instanceof AsyncDisconnectionNotifier)
			discardSessionState(((AsyncDisconnectionNotifier)r).getClient());
	}
	
	@Override
	protected void beforeExecute(Thread t, Runnable r)
	{
		super.beforeExecute(t, r);
		
		// and we defy proper nesting
		_start = System.nanoTime();
	}
}
