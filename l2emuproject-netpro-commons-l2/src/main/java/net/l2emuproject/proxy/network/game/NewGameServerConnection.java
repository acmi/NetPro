/*
 * Copyright 2011-2016 L2EMU UNIQUE
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
package net.l2emuproject.proxy.network.game;

import java.net.InetAddress;
import java.net.InetSocketAddress;

import net.l2emuproject.proxy.network.game.client.L2GameClient;

/**
 * Game server connection info wrapper.
 * 
 * @author _dev_
 */
public interface NewGameServerConnection
{
	/**
	 * Returns the address of the server to connect to.
	 * 
	 * @return server IP and port
	 */
	InetSocketAddress getAddress();
	
	/**
	 * Returns the address of a client that authorized itself for this game server connection.
	 * 
	 * @return client IP
	 */
	InetAddress getAuthorizedClientAddress();
	
	/**
	 * Returns the connection that requested to be transferred over.
	 * 
	 * @return existing connection
	 */
	L2GameClient getUnderlyingConnection();
}
