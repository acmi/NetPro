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
package net.l2emuproject.proxy.script.login;

import net.l2emuproject.proxy.network.ProxyConnections;
import net.l2emuproject.proxy.network.login.client.L2LoginClient;
import net.l2emuproject.proxy.network.login.client.L2LoginClientConnections;
import net.l2emuproject.proxy.network.login.server.L2LoginServer;
import net.l2emuproject.proxy.network.login.server.L2LoginServerConnections;
import net.l2emuproject.proxy.script.Script;

/**
 * A script for login server/client packet manipulations.
 * 
 * @author savormix
 */
public abstract class LoginScript extends Script<L2LoginClient, L2LoginServer>
{
	/**
	 * Constructs this script's object.
	 * 
	 * @param handledClient handled main client opcodes
	 * @param handledServer handled main server opcodes
	 */
	protected LoginScript(int[] handledClient, int[] handledServer)
	{
		super(handledClient, handledServer);
	}
	
	@Override
	public final Class<L2LoginClient> getClientClass()
	{
		return L2LoginClient.class;
	}
	
	@Override
	public final void setUp()
	{
		ProxyConnections pc;
		pc = L2LoginClientConnections.getInstance();
		pc.addConnectionListener(this);
		pc.addPacketListener(this);
		pc = L2LoginServerConnections.getInstance();
		pc.addConnectionListener(this);
		pc.addPacketListener(this);
	}
	
	@Override
	public final void tearDown()
	{
		ProxyConnections pc;
		pc = L2LoginClientConnections.getInstance();
		pc.removeConnectionListener(this);
		pc.removePacketListener(this);
		pc = L2LoginServerConnections.getInstance();
		pc.removeConnectionListener(this);
		pc.removePacketListener(this);
	}
}
