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
 * Handles communications between proxy and the game client.
 * 
 * @author NB4L1
 */
public abstract class AbstractL2ClientProxy extends AbstractL2Proxy
{
	/**
	 * Creates an internal object representing a client connection.
	 * 
	 * @param mmoController connection manager
	 * @param socketChannel connection
	 * @throws ClosedChannelException if the given channel was closed during operations
	 */
	protected AbstractL2ClientProxy(ProxyConnections mmoController, SocketChannel socketChannel)
			throws ClosedChannelException
	{
		super(mmoController, socketChannel, null);
	}
	
	@Override
	public final EndpointType getType()
	{
		return EndpointType.CLIENT;
	}
}
