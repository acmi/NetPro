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
package net.l2emuproject.proxy.network.exception;

import net.l2emuproject.proxy.network.AbstractL2ClientProxy;

/**
 * A very proxy-specific exception, meaning that a limit of existing client connections
 * that have not yet been assigned a server connection, has been reached.
 * 
 * @author _dev_
 */
public class TooManyPendingConnectionsException extends Exception
{
	private static final long serialVersionUID = 3754108652178174824L;
	
	/**
	 * Creates an exception.
	 * 
	 * @param client client connection that was dropped
	 */
	public TooManyPendingConnectionsException(AbstractL2ClientProxy client)
	{
		super("Refused connection from " + client.getInetSocketAddress());
	}
}
