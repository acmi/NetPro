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

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import net.l2emuproject.lang.NetProThreadPriority;
import net.l2emuproject.proxy.network.listener.PacketManipulator;
import net.l2emuproject.util.concurrent.RunnableStatsManager;
import net.l2emuproject.util.logging.L2Logger;

/**
 * This executor is designed to execute asynchronous packet arrival/departure notifications to packet listeners/manipulators, while retaining their order.<BR>
 * Built-in performance monitoring ensures that any anomalies can be quickly detected and taken care of.
 * 
 * @author savormix
 */
public class ForwardedNotificationExecutor extends ScheduledThreadPoolExecutor implements NetProThreadPriority
{
	private static final L2Logger LOG = L2Logger.getLogger(ForwardedNotificationExecutor.class);
	
	private static final int SINGLE_SEQUENTIAL_LISTENER_WARNING_THRESHOLD = 5;
	
	// perfectly possible due to 1 thread
	private long _start;
	
	/**
	 * Creates a single-threaded executor for packet notifications.
	 * 
	 * @param no executor number
	 */
	ForwardedNotificationExecutor(int no)
	{
		super(0, r ->
		{
			final Thread t = new Thread(r, "PacketNotifier-" + no);
			t.setPriority(ASYNC_PACKET_NOTIFIER);
			return t;
		});
		
		setMaximumPoolSize(1);
		setKeepAliveTime(1, TimeUnit.MINUTES);
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
	}
	
	@Override
	protected void beforeExecute(Thread t, Runnable r)
	{
		super.beforeExecute(t, r);
		
		// and we defy proper nesting
		_start = System.nanoTime();
	}
}
