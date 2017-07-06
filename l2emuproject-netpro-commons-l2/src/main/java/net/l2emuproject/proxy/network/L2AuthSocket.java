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

import java.net.InetAddress;

/**
 * @author _dev_
 */
public class L2AuthSocket extends ProxySocket
{
	private final int _serverListVersion;
	
	/**
	 * Constructs a proxy socket.
	 * 
	 * @param bindAddress listening address
	 * @param listenPort listening port
	 * @param serviceAddress remote hostname
	 * @param servicePort remote port
	 * @param serverListVersion expected ServerList version
	 */
	public L2AuthSocket(InetAddress bindAddress, int listenPort, String serviceAddress, int servicePort, int serverListVersion)
	{
		super(bindAddress, listenPort, serviceAddress, servicePort);
		
		_serverListVersion = serverListVersion;
	}
	
	/**
	 * Returns the expected ServerList version.
	 * 
	 * @return expected ServerList version or {@code -1}
	 */
	public int getServerListVersion()
	{
		return _serverListVersion;
	}
}
