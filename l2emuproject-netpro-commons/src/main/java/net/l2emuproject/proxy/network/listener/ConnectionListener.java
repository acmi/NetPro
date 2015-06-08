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
package net.l2emuproject.proxy.network.listener;

import net.l2emuproject.proxy.network.Proxy;

/**
 * Interface for entities willing to be notified about connectivity events.
 * 
 * @author savormix
 */
public interface ConnectionListener
{
	/**
	 * Notifies about a client connection to the proxy.
	 * 
	 * @param client internal object that represents the connection initiator
	 */
	void onClientConnection(Proxy client);
	
	/**
	 * Notifies about the proxy's connection to a server.
	 * 
	 * @param server internal object that represents the server
	 */
	void onServerConnection(Proxy server);
	
	/**
	 * Notifies about a closed connection to the proxy.
	 * 
	 * @param client internal object that represents the connection initiator
	 * @param server internal object that represents the server
	 */
	void onDisconnection(Proxy client, Proxy server);
}
