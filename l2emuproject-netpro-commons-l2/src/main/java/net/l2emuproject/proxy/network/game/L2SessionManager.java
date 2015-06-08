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
package net.l2emuproject.proxy.network.game;

import javolution.util.FastMap;

import net.l2emuproject.proxy.network.game.client.L2GameClient;
import net.l2emuproject.proxy.network.login.client.L2LoginClient;
import net.l2emuproject.util.logging.L2Logger;

/**
 * Tracks what game servers have been selected by which addresses to properly redirect newly connecting game clients.
 * 
 * @author savormix
 */
public class L2SessionManager
{
	// private static final MMOLogger LOG = new MMOLogger(L2Redirector.class, 5000);
	
	private final FastMap<String, L2GameServerInfo> _routes;
	
	L2SessionManager()
	{
		_routes = FastMap.<String, L2GameServerInfo> newInstance().setShared(true);
	}
	
	/**
	 * Assigns a client requested game server ID to client's IP.
	 * 
	 * @param client a client
	 * @return whether a route has been created
	 */
	public boolean addRoute(L2LoginClient client)
	{
		if (client == null)
			return false;
		
		Integer id = client.getTargetServer();
		if (id == null)
			return false;
		
		L2GameServerInfo gsi = client.getServers().get(id);
		if (gsi == null)
			return false;
		
		getRoutes().put(client.getHostAddress(), gsi);
		return true;
	}
	
	/**
	 * Returns the assigned game server info.
	 * 
	 * @param client a client
	 * @return game server info
	 */
	public L2GameServerInfo getRoute(L2GameClient client)
	{
		if (client == null)
			return null;
		
		final L2GameServerInfo gsi = getRoutes().remove(client.getHostAddress());
		/*
		if (gsi == null)
		{
			final byte[] b = client.getInetAddress().getAddress();
			if (b[0] == 10 || (b[0] == (byte)192 && b[1] == (byte)168) || (b[0] == (byte)172 && b[1] >= 16 && b[1] <= 31))
				return getRoutes().remove("127.0.0.1");
			
			// FIXME: do something when imposing the target login server
			// but do not allow anyone to intercept sessions
		}
		*/
		return gsi;
	}
	
	/** Writes debug information to console */
	public void describeExistingRoutes()
	{
		L2Logger.getLogger(getClass()).info(getClass(), getRoutes().keySet().toString());
	}
	
	private FastMap<String, L2GameServerInfo> getRoutes()
	{
		return _routes;
	}
	
	/**
	 * Returns a singleton instance of this type.
	 * 
	 * @return an instance of this class
	 */
	public static final L2SessionManager getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static final class SingletonHolder
	{
		static final L2SessionManager INSTANCE = new L2SessionManager();
	}
}
