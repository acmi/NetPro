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
package net.l2emuproject.proxy.script;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import eu.revengineer.simplejse.type.UnloadableScript;

import net.l2emuproject.proxy.network.AbstractL2ClientProxy;
import net.l2emuproject.proxy.network.AbstractL2ServerProxy;
import net.l2emuproject.proxy.network.ForwardedNotificationManager;
import net.l2emuproject.proxy.network.Proxy;
import net.l2emuproject.proxy.network.SessionStateManagingExecutor;
import net.l2emuproject.proxy.network.meta.RandomAccessMMOBuffer;
import net.l2emuproject.proxy.state.entity.context.ICacheServerID;
import net.l2emuproject.proxy.state.entity.context.ServerSocketID;
import net.l2emuproject.util.ImmutableSortedArraySet;
import net.l2emuproject.util.logging.L2Logger;

/**
 * A packet enumeration based packet postprocessor.
 * 
 * @author _dev_
 * @param <C> client connection type
 * @param <S> server connection type
 */
public abstract class PpeEnabledScript<C extends AbstractL2ClientProxy, S extends AbstractL2ServerProxy> implements UnloadableScript
{
	// not intended to be used by subclasses!
	private static final L2Logger LOG = L2Logger.getLogger(PpeEnabledScript.class);
	
	private final Set<String> _handledAliases;
	
	/** Creates a PPE script. */
	protected PpeEnabledScript()
	{
		final Set<String> aliases = new TreeSet<>();
		for (Class<?> c = getClass(); c != null; c = c.getSuperclass())
			fillAliasesRecursively(aliases, c);
		_handledAliases = ImmutableSortedArraySet.of(aliases, String.class);
	}
	
	private static final void fillAliasesRecursively(Collection<String> aliases, Class<?> declaringClass)
	{
		// take an overriding field value over others
		fillAliases(aliases, declaringClass);
		for (final Class<?> superClass : declaringClass.getInterfaces())
			fillAliasesRecursively(aliases, superClass);
	}
	
	private static final void fillAliases(Collection<String> aliases, Class<?> declaringClass)
	{
		for (final Field f : declaringClass.getDeclaredFields())
		{
			if (!Modifier.isStatic(f.getModifiers()))
				continue;
			
			final ScriptFieldAlias sfa = f.getAnnotation(ScriptFieldAlias.class);
			if (sfa == null || sfa.disabled())
				continue;
			
			try
			{
				f.setAccessible(true);
				aliases.add(String.valueOf(f.get(null)));
				f.setAccessible(false);
			}
			catch (final Exception e)
			{
				LOG.error("Cannot handle field alias " + f.getName(), e);
			}
		}
	}
	
	/**
	 * Returns all field aliases to be handled by this script.
	 * 
	 * @return field {@code <scriptAlias>}es
	 */
	public final Set<String> getHandledScriptFieldAliases()
	{
		return _handledAliases;
	}
	
	/**
	 * Returns how early this PPE script should be executed, when compared to other PPE scripts. Lower values - earlier execution. Default is 0.
	 * 
	 * @return script priority
	 */
	public double getPriority()
	{
		return 0D;
	}
	
	/**
	 * Handles a client packet that has been forwarded to the server.
	 * 
	 * @param client sender
	 * @param server receiver
	 * @param buf packet content
	 * @throws RuntimeException if anything unexpected happens
	 */
	public abstract void handleClientPacket(C client, S server, RandomAccessMMOBuffer buf) throws RuntimeException;
	
	/**
	 * Handles a server packet that has been forwarded to a client.
	 * 
	 * @param client receiver
	 * @param server sender
	 * @param buf packet content
	 * @throws RuntimeException if anything unexpected happens
	 */
	public abstract void handleServerPacket(C client, S server, RandomAccessMMOBuffer buf) throws RuntimeException;
	
	/**
	 * Performs any clean up actions (if necessary) following a client disconnection.
	 * 
	 * @param client disconnected client endpoint
	 */
	public void handleDisconnection(C client)
	{
		// do nothing by default
	}
	
	/**
	 * Retrieves a value from a session-bound mapping.
	 * 
	 * @param client session key
	 * @param key mapping key
	 * @return value or {@code null}
	 */
	protected <T> T get(C client, String key)
	{
		final SessionStateManagingExecutor exec = ForwardedNotificationManager.getInstance().getPacketExecutor(client);
		return exec != null ? exec.getSessionStateFor(client, getSessionStateKey(key)) : null;
	}
	
	/**
	 * Retrieves a value from a session-bound mapping, setting a new value if none exists.
	 * 
	 * @param client session key
	 * @param key mapping key
	 * @param mappingFunction default value function
	 * @return value or {@code null}
	 */
	protected <T> T computeIfAbsent(C client, String key, Function<String, T> mappingFunction)
	{
		final SessionStateManagingExecutor exec = ForwardedNotificationManager.getInstance().getPacketExecutor(client);
		return exec != null ? exec.computeSessionStateIfAbsentFor(client, getSessionStateKey(key), mappingFunction) : null;
	}
	
	/**
	 * Sets a value in the session-bound mapping, returning the previously set value.
	 * 
	 * @param client session key
	 * @param key mapping key
	 * @param value value to set
	 * @return value or {@code null}
	 */
	protected Object set(C client, String key, Object value)
	{
		final SessionStateManagingExecutor exec = ForwardedNotificationManager.getInstance().getPacketExecutor(client);
		return exec != null ? exec.setSessionStateFor(client, getSessionStateKey(key), value) : null;
	}
	
	/**
	 * Removes a value from a session-bound mapping.
	 * 
	 * @param client session key
	 * @param key mapping key
	 * @return value or {@code null}
	 */
	protected <T> T remove(C client, String key)
	{
		final SessionStateManagingExecutor exec = ForwardedNotificationManager.getInstance().getPacketExecutor(client);
		return exec != null ? exec.removeSessionStateFor(client, getSessionStateKey(key)) : null;
	}
	
	/**
	 * Removes a value from a session-bound mapping. If this value was an executable task, it will be interrupted and/or cancelled.
	 * 
	 * @param client session key
	 * @param key mapping key
	 */
	protected void discard(C client, String key)
	{
		final SessionStateManagingExecutor exec = ForwardedNotificationManager.getInstance().getPacketExecutor(client);
		if (exec != null)
			exec.discardSessionStateByKey(k -> getSessionStateKey(key).equals(k));
	}
	
	/**
	 * Removes all values from a session-bound mapping. If any of those values were executable tasks, they will be interrupted and/or cancelled.
	 * 
	 * @param client session key
	 */
	protected void clear(C client)
	{
		final SessionStateManagingExecutor exec = ForwardedNotificationManager.getInstance().getPacketExecutor(client);
		exec.discardSessionStateByKey(k -> String.valueOf(k).startsWith(getSessionStateKey("")));
	}
	
	/**
	 * Schedules an action to be executed synchronously on the packet notification handling thread.<BR>
	 * <BR>
	 * This allows you to evade real concurrency as long as your scheduled actions take little time to complete, that is they do not involve I/O or
	 * excessive heap allocations, or heavy multi-dimensional analysis.<BR>
	 * Typical use of this method is to update script's internal state (data structures in the heap) without needing to use locking/synchronization.
	 * 
	 * @param client a connection endpoint
	 * @param taskName task identifier
	 * @param r a task
	 * @param delay amount of time to wait
	 * @param unit time unit used to specify the amount
	 * @return a scheduled task wrapper
	 */
	protected ScheduledFuture<?> schedule(C client, String taskName, Runnable r, long delay, TimeUnit unit)
	{
		final SessionStateManagingExecutor exec = ForwardedNotificationManager.getInstance().getPacketExecutor(client);
		return exec != null ? exec.scheduleSessionBound(client, getSessionStateKey(taskName), r, delay, unit) : null;
	}
	
	/**
	 * Schedules an action to be repeatedly executed synchronously on the packet notification handling thread.
	 * 
	 * @param client a connection endpoint
	 * @param taskName task identifier
	 * @param r a task
	 * @param initialDelay amount of time to wait initially
	 * @param delay amount of time to wait
	 * @param unit time unit used to specify the amount
	 * @return a scheduled task wrapper
	 */
	protected ScheduledFuture<?> scheduleWithFixedDelay(C client, String taskName, Runnable r, long initialDelay, long delay, TimeUnit unit)
	{
		final SessionStateManagingExecutor exec = ForwardedNotificationManager.getInstance().getPacketExecutor(client);
		return exec != null ? exec.scheduleSessionBoundWithFixedDelay(client, getSessionStateKey(taskName), r, initialDelay, delay, unit) : null;
	}
	
	@Override
	public void onUnload() throws RuntimeException
	{
		ForwardedNotificationManager.getInstance().discardSessionStateByKey(k -> String.valueOf(k).startsWith(getSessionStateKey("")));
	}
	
	private String getSessionStateKey(String key)
	{
		return getClass().getName() + "#" + key;
	}
	
	/**
	 * Retrieves a context that identifies which entity existence boundary context is used when caching entities for the given connection.
	 * 
	 * @param proxy a connection endpoint
	 * @return entity existence boundary context
	 */
	public static final ICacheServerID getEntityContext(Proxy proxy)
	{
		proxy = proxy.getServer();
		
		if (proxy == null)
		{
			LOG.warn("Premature entity context call!");
			return null;
		}
		
		return new ServerSocketID(proxy.getInetSocketAddress());
	}
}
