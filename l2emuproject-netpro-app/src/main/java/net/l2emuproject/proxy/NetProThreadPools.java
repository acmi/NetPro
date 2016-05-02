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
package net.l2emuproject.proxy;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import net.l2emuproject.util.concurrent.L2ThreadPool;
import net.l2emuproject.util.concurrent.ThreadPoolInitializer;

/**
 * Initializes thread pools.
 * 
 * @author _dev_
 */
final class NetProThreadPools implements ThreadPoolInitializer
{
	@Override
	public Set<ScheduledThreadPoolExecutor> getScheduledPools()
	{
		// Scheduled pools will primarily handle MMOLoggers and various other recurring tasks
		final ScheduledThreadPoolExecutor pool = new ScheduledThreadPoolExecutor(1, L2ThreadPool.getDefaultSPFactory());
		pool.setKeepAliveTime(2, TimeUnit.MINUTES);
		return Collections.singleton(pool);
	}
	
	@Override
	public Set<ThreadPoolExecutor> getInstantPools()
	{
		// Asynchronous packet notifications have dedicated executors, so this shall be primarily for scripts that require fast execution outside the notification thread
		return Collections.singleton(new ThreadPoolExecutor(0, Integer.MAX_VALUE, 15L, TimeUnit.SECONDS, new SynchronousQueue<>(), L2ThreadPool.getDefaultIPFactory()));
	}
	
	@Override
	public Set<ThreadPoolExecutor> getLongRunningPools()
	{
		// Typically for user-invoked I/O related tasks, either via UI or scripts
		return Collections.singleton(new ThreadPoolExecutor(0, Integer.MAX_VALUE, 1L, TimeUnit.MINUTES, new SynchronousQueue<>(), L2ThreadPool.getDefaultLPFactory()));
	}
}
