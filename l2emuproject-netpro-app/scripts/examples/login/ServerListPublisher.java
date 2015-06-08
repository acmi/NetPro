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
package examples.login;

import org.apache.commons.lang3.ArrayUtils;

import net.l2emuproject.proxy.network.Packet;
import net.l2emuproject.proxy.network.login.client.L2LoginClient;
import net.l2emuproject.proxy.network.login.server.L2LoginServer;
import net.l2emuproject.proxy.network.login.server.packets.ServerList;
import net.l2emuproject.proxy.script.login.LoginScript;

/**
 * A script that automatically publishes the real server list in addition to the fake one.<BR>
 * <BR>
 * This script sends a fake notification about a forwarded packet. Use with care.<BR>
 * Personally, I think the real server list in UI/log is worth it, since 3rd party scripts shouldn't
 * be messing with the server list in the first place.
 * 
 * @author savormix
 */
public final class ServerListPublisher extends LoginScript
{
	/** Constructs this script. */
	public ServerListPublisher()
	{
		super(ArrayUtils.EMPTY_INT_ARRAY, new int[] { ServerList.OPCODE });
	}
	
	@Override
	protected void serverPacketArrived(L2LoginServer sender, L2LoginClient recipient, Packet packet)
	{
		// report the received packet
		sender.notifyPacketForwarded(null, packet.getReceivedBody(), packet.getReceptionTime());
	}
	
	@Override
	public String getScriptName()
	{
		return "Original ServerList in UI";
	}
	
	@Override
	public String getAuthor()
	{
		return "savormix";
	}
	
	@Override
	public String getVersionString()
	{
		return "All known versions";
	}
}
