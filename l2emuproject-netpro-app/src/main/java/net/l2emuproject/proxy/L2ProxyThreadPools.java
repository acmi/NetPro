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

import net.l2emuproject.util.concurrent.AbstractThreadPoolInitializer;

/**
 * Default thread pool initializer.
 * 
 * @author savormix
 */
public final class L2ProxyThreadPools extends AbstractThreadPoolInitializer
{
	/*
	private static final int SCALE_SCHEDULER = 2;
	private static final int SCALE_EXECUTOR = 2;
	private static final int SCALE_BACKGROUND = 3;
	*/
	
	@Override
	public void initThreadPool() throws Exception
	{
		/*
		final int ap = Runtime.getRuntime().availableProcessors();
		addScheduledPool(new ScheduledThreadPoolExecutor(ap * SCALE_SCHEDULER));
		addInstantPool(new ThreadPoolExecutor(ap * SCALE_EXECUTOR, ap * SCALE_EXECUTOR, 0, TimeUnit.NANOSECONDS,
				new ArrayBlockingQueue<Runnable>(100000)));
		addLongRunningPool(new ThreadPoolExecutor(0, ap * SCALE_BACKGROUND, 5L, TimeUnit.MINUTES,
				new LinkedBlockingQueue<Runnable>()));
		*/
	}
}
