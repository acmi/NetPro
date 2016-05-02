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

import java.nio.ByteBuffer;
import java.util.Map;
import java.util.Set;

import net.l2emuproject.proxy.network.Proxy.AsyncDisconnectionNotifier;
import net.l2emuproject.proxy.network.ProxyConnections.AsyncConnectionNotifier;
import net.l2emuproject.proxy.network.listener.PacketListener;
import net.l2emuproject.proxy.network.listener.PacketManipulator;
import net.l2emuproject.util.Rnd;
import net.l2emuproject.util.logging.L2Logger;

import javolution.util.FastMap;

/**
 * Ensures that forwarded packet notifications are executed in order for each client/server pair.
 * 
 * @author savormix
 */
public class ForwardedNotificationManager
{
	private static final L2Logger LOG = L2Logger.getLogger(ForwardedNotificationManager.class);
	
	final ForwardedNotificationExecutor[] _executors;
	final Map<Proxy, ForwardedNotificationExecutor> _client2Executor;
	
	ForwardedNotificationManager()
	{
		// being deep down in a library, nothing better to do
		_executors = new ForwardedNotificationExecutor[Runtime.getRuntime().availableProcessors()];
		for (int i = 0; i < _executors.length; ++i)
			_executors[i] = new ForwardedNotificationExecutor();
		
		_client2Executor = new FastMap<Proxy, ForwardedNotificationExecutor>().setShared(true);
		
		net.l2emuproject.lang.management.ShutdownManager.addShutdownHook(() ->
		{
			LOG.info("Shutting down...");
			
			int unfinished = 0;
			for (final ForwardedNotificationExecutor exec : _executors)
				unfinished += exec.shutdownNow().size();
			_client2Executor.clear();
			
			LOG.info("Done. " + unfinished + " tasks were not executed.");
		});
	}
	
	/**
	 * Called on a successful connection. This manager will assign a concrete executor to deal with all packets sent by {@code client} to {@code server} and vice-versa.
	 * 
	 * @param notification notification task
	 */
	public void addConnectionNotification(AsyncConnectionNotifier notification)
	{
		// since this proxy connects on behalf of its clients, a client connection will always exist for any proxy connection
		final ForwardedNotificationExecutor exec = Rnd.get(_executors);
		ForwardedNotificationExecutor designatedExec = _client2Executor.putIfAbsent(notification.getEndpoint().getClient(), exec);
		if (designatedExec == null)
			designatedExec = exec;
		
		designatedExec.execute(notification);
	}
	
	/**
	 * Called when both endpoints have terminated the connection (regardless of which disconnected first).
	 * 
	 * @param notification notification task
	 */
	public void addDisconnectionNotification(AsyncDisconnectionNotifier notification)
	{
		final ForwardedNotificationExecutor exec = _client2Executor.remove(notification.getClient());
		if (exec != null)
			exec.execute(notification);
		else
			LOG.warn("Disconnection notification on an inactive connection?!");
	}
	
	/**
	 * Notifies packet listeners and manipulators about a received/sent packet asynchronously.
	 * 
	 * @param sender packet sender
	 * @param recipient packet recipient
	 * @param received received packet body [complete buffer] or {@code null}
	 * @param sent sent packet body [complete buffer] or {@code null}
	 * @param time packet arrival timestamp
	 * @param listeners packet listeners to be notified
	 * @param manipulators packet manipulators to be notified
	 */
	public void addPacketNotification(Proxy sender, Proxy recipient, ByteBuffer received, ByteBuffer sent, long time, Set<PacketListener> listeners, Set<PacketManipulator> manipulators)
	{
		final ForwardedNotificationExecutor exec = _client2Executor.get(sender.getClient());
		if (exec == null) // early quit
			return;
		
		for (final PacketManipulator pm : manipulators)
			exec.execute(new ManipForwardNotifier(sender, recipient, received, sent, pm));
		
		if (sent == null)
			return;
		
		for (final PacketListener pl : listeners)
			exec.execute(new ListenerForwardNotifier(sender, sent, pl, time));
	}
	
	/**
	 * Returns a packet notification executor that is assigned to {@code proxy}.
	 * 
	 * @param proxy a socket
	 * @return notification executor
	 */
	public ForwardedNotificationExecutor getPacketExecutor(Proxy proxy)
	{
		return _client2Executor.get(proxy.getClient());
	}
	
	/**
	 * Returns a singleton instance of this type.
	 * 
	 * @return an instance of this class
	 */
	public static final ForwardedNotificationManager getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static final class SingletonHolder
	{
		static final ForwardedNotificationManager INSTANCE = new ForwardedNotificationManager();
	}
}
