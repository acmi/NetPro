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
package net.l2emuproject.proxy.script.game;

import net.l2emuproject.proxy.network.ProxyConnections;
import net.l2emuproject.proxy.network.game.client.L2GameClient;
import net.l2emuproject.proxy.network.game.client.L2GameClientConnections;
import net.l2emuproject.proxy.network.game.server.L2GameServer;
import net.l2emuproject.proxy.network.game.server.L2GameServerConnections;
import net.l2emuproject.proxy.script.Script;

/**
 * A script for game server/client packet manipulations.
 * 
 * @author savormix
 */
public abstract class GameScript extends Script<L2GameClient, L2GameServer>
{
	/**
	 * Creates a script that handles game client/server packets.
	 * 
	 * @param handledClient handled main client opcodes
	 * @param handledServer handled main server opcodes
	 */
	protected GameScript(int[] handledClient, int[] handledServer)
	{
		super(handledClient, handledServer);
	}
	
	@Override
	public final Class<L2GameClient> getClientClass()
	{
		return L2GameClient.class;
	}
	
	@Override
	public final void setUp()
	{
		ProxyConnections pc;
		pc = L2GameClientConnections.getInstance();
		pc.addConnectionListener(this);
		pc.addPacketListener(this);
		pc = L2GameServerConnections.getInstance();
		pc.addConnectionListener(this);
		pc.addPacketListener(this);
	}
	
	@Override
	public final void tearDown()
	{
		ProxyConnections pc;
		pc = L2GameClientConnections.getInstance();
		pc.removeConnectionListener(this);
		pc.removePacketListener(this);
		pc = L2GameServerConnections.getInstance();
		pc.removeConnectionListener(this);
		pc.removePacketListener(this);
		tearDownImpl();
	}
	
	/** Release all used resources, as this script is no longer used. */
	protected void tearDownImpl()
	{
		// do nothing by default
	}
}
