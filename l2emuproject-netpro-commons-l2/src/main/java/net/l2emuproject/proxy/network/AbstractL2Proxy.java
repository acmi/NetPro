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

import java.nio.channels.ClosedChannelException;
import java.nio.channels.SocketChannel;

/**
 * Base class for internal L2 connection endpoint (socket) objects.
 * 
 * @author NB4L1
 */
public abstract class AbstractL2Proxy extends Proxy
{
	/**
	 * Creates an internal object representing an incoming connection.
	 * 
	 * @param mmoController connection manager
	 * @param socketChannel connection
	 * @param target internal object that represents an outgoing connection
	 * @throws ClosedChannelException if the given channel was closed during operations
	 */
	protected AbstractL2Proxy(ProxyConnections mmoController, SocketChannel socketChannel, Proxy target)
			throws ClosedChannelException
	{
		super(mmoController, socketChannel, target);
	}
}
