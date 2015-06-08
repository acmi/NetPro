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
package net.l2emuproject.proxy.state.entity.context;

import java.net.InetSocketAddress;

import net.l2emuproject.lang.NotARealProxyObject;

/**
 * An entity existence boundary defining cache context specified as a live server. This context is shared, so cacheable entities may come from more than a single source.
 * 
 * @author _dev_
 */
public final class ServerSocketID extends NotARealProxyObject<InetSocketAddress> implements ICacheServerID
{
	/**
	 * Creates a shared context identifier for a live server.
	 * 
	 * @param id complete address of a live server
	 */
	public ServerSocketID(InetSocketAddress id)
	{
		super(id);
	}
}
