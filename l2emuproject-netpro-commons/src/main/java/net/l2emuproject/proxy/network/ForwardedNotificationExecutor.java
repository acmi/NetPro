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
import java.util.concurrent.Delayed;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.RunnableScheduledFuture;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
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
	private long _start, _end;
	

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
		setKeepAliveTime(1, TimeUnit.MINUTES);
		
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
		validateCall(client, false);
		if (isDisconnected(client))
		{
			final Map<K, V> present = (Map<K, V>)_sessionStateMap.get(client);
			if (present == null)
				throw new IllegalStateException("State mutation while discard pending");
			return present.computeIfAbsent(key, mappingFunction);
		}
		return (V)_sessionStateMap.computeIfAbsent(client, k -> new HashMap<>()).computeIfAbsent(key, (Function<? super Object, ? extends Object>)mappingFunction);
	}
	
	@Override
	public Object setSessionStateFor(Proxy client, Object key, Object value)
	{
		validateCall(client, false);
		if (isDisconnected(client))
		{
			final Map<Object, Object> present = _sessionStateMap.get(client);
			if (present == null)
				throw new IllegalStateException("State mutation while discard pending");
			return present.put(key, value);
		}
		return _sessionStateMap.computeIfAbsent(client, k -> new HashMap<>()).put(key, value);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T> T removeSessionStateFor(Proxy client, Object key)
	{
		validateCall(client, false);
		final Map<Object, Object> stateMap = _sessionStateMap.get(client);
		return stateMap != null ? (T)stateMap.remove(key) : null;
	}
	
	@Override
	public <T> T removeSessionStateFor(Proxy client, Object key, T expectedValue)
	{
		validateCall(client, false);
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
				final Future<Future<?>> task = submit(() -> executeSessionBound(client, key, r));
				while (true)
				{
					try
					{
						return task.get(1, TimeUnit.SECONDS);
					}
					catch (final TimeoutException e)
					{
						LOG.error("Still scheduling (instant) " + key + ", executor blocked!");
					}
				}
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
		final Future<?> newValue = submit(new SessionBoundTask(r, client, key, () -> {
			try
			{
				return expectedValue.take();
			}
			catch (final InterruptedException e)
			{
				// application is shutting down, so whatever, really
				return null;
			}
		}));
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
				final Future<ScheduledFuture<?>> task = submit(() -> scheduleSessionBound(client, key, r, delay, unit));
				while (true)
				{
					try
					{
						return task.get(1, TimeUnit.SECONDS);
					}
					catch (final TimeoutException e)
					{
						LOG.error("Still scheduling " + key + ", executor blocked!");
					}
				}
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
		final ScheduledFuture<?> newValue = schedule(new SessionBoundTask(r, client, key, () -> {
			try
			{
				return expectedValue.take();
			}
			catch (final InterruptedException e)
			{
				// application is shutting down, so whatever, really
				return null;
			}
		}), delay, unit);
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
				final Future<ScheduledFuture<?>> task = submit(() -> scheduleSessionBoundWithFixedDelay(client, key, r, initialDelay, delay, unit));
				while (true)
				{
					try
					{
						return task.get(1, TimeUnit.SECONDS);
					}
					catch (final TimeoutException e)
					{
						LOG.error("Still scheduling " + key + ", executor blocked!");
					}
				}
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
		if (denyOpIfDisconnected && isDisconnected(client))
			throw new IllegalStateException("State mutation while discard pending");
	}
	
	private boolean isDisconnected(Proxy client)
	{
		return client.isDced() || client.isFailed();
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
		_end = System.nanoTime();
		
		super.afterExecute(r, t);
		
		// but still adhere to it
		if (r instanceof LegacyPostExecSupport)
		{
			final Consumer<Throwable> postExec = ((LegacyPostExecSupport<?>)r).getLegacyPostExec();
			if (postExec != null)
			{
				postExec.accept(t);
				return;
			}
		}
		
		if (t != null)
			LOG.error("Uncaught", t);
	}
	
	@Override
	protected void beforeExecute(Thread t, Runnable r)
	{
		super.beforeExecute(t, r);
		
		// and we defy proper nesting
		_start = System.nanoTime();
	}
	
	@Override
	protected <V> RunnableScheduledFuture<V> decorateTask(Runnable r, RunnableScheduledFuture<V> task)
	{
		if (r instanceof ManipForwardNotifier)
		{
			final ManipForwardNotifier fn = (ManipForwardNotifier)r;
			final PacketManipulator pm = fn.getManip();
			
			return new LegacyPostExecSupport<>(task, t -> {
				if (t != null)
				{
					LOG.error(pm, t);
					return;
				}
				
				RunnableStatsManager.handleStats(pm.getClass(), "packetForwarded(Proxy, Proxy, ByteBuffer, ByteBuffer)", _end - _start, SINGLE_SEQUENTIAL_LISTENER_WARNING_THRESHOLD);
			});
		}
		else if (r instanceof ListenerForwardNotifier)
		{
			final ListenerForwardNotifier fn = (ListenerForwardNotifier)r;
			return new LegacyPostExecSupport<>(task, t -> {
				if (t != null)
				{
					LOG.error("", t);
					return;
				}
				
				RunnableStatsManager.handleStats(fn.getListener().getClass(), "onPacket(Proxy, Proxy, ByteBuffer, long)", _end - _start, SINGLE_SEQUENTIAL_LISTENER_WARNING_THRESHOLD);
			});
		}
		else if (r instanceof AsyncDisconnectionNotifier)
			return new LegacyPostExecSupport<>(task, t -> discardSessionState(((AsyncDisconnectionNotifier)r).getClient()));
		else if (r instanceof SessionBoundTask)
		{
			final SessionBoundTask scriptTask = (SessionBoundTask)r;
			return new LegacyPostExecSupport<>(task, t -> {
				removeSessionStateFor(scriptTask.getClient(), scriptTask.getKey(), scriptTask.asFuture());
				if (t != null)
					LOG.error(scriptTask, t);
			});
		}
		return task;
	}
	/*
	@Override
	protected <V> RunnableScheduledFuture<V> decorateTask(Callable<V> callable, RunnableScheduledFuture<V> task)
	{
		return task;
	}
	*/
	
	private static final class LegacyPostExecSupport<V> implements RunnableScheduledFuture<V>
	{
		private final RunnableScheduledFuture<V> _task;
		private final Consumer<Throwable> _legacyPostExec;
		
		LegacyPostExecSupport(RunnableScheduledFuture<V> task, Consumer<Throwable> legacyPostExec)
		{
			_task = task;
			_legacyPostExec = legacyPostExec;
		}
		
		public Consumer<Throwable> getLegacyPostExec()
		{
			return _legacyPostExec;
		}
		
		@Override
		public void run()
		{
			_task.run();
		}
		
		@Override
		public boolean cancel(boolean mayInterruptIfRunning)
		{
			return _task.cancel(mayInterruptIfRunning);
		}
		
		@Override
		public boolean isCancelled()
		{
			return _task.isCancelled();
		}
		
		@Override
		public boolean isDone()
		{
			return _task.isDone();
		}
		
		@Override
		public V get() throws InterruptedException, ExecutionException
		{
			return _task.get();
		}
		
		@Override
		public V get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException
		{
			return _task.get(timeout, unit);
		}
		
		@Override
		public long getDelay(TimeUnit unit)
		{
			return _task.getDelay(unit);
		}
		
		@Override
		public int compareTo(Delayed o)
		{
			return _task.compareTo(o);
		}
		
		@Override
		public boolean isPeriodic()
		{
			return _task.isPeriodic();
		}
	}
}
