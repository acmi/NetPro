/*
 * Copyright 2011-2017 L2EMU UNIQUE
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
import java.util.function.Supplier;

/**
 * @author _dev_
 */
final class SessionBoundTask implements Runnable
{
	private final Runnable _task;
	private final Proxy _client;
	private final Object _key;
	private final Supplier<Future<?>> _thisAsFuture;
	
	public SessionBoundTask(Runnable task, Proxy client, Object key, Supplier<Future<?>> thisAsFuture)
	{
		_task = task;
		_client = client;
		_key = key;
		_thisAsFuture = thisAsFuture;
	}
	
	@Override
	public void run()
	{
		_task.run();
	}
	
	public Proxy getClient()
	{
		return _client;
	}
	
	public Object getKey()
	{
		return _key;
	}
	
	public Future<?> asFuture()
	{
		return _thisAsFuture.get();
	}
	
	@Override
	public String toString()
	{
		return String.valueOf(_key);
	}
}
