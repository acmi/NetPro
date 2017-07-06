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
package examples.game.stealth;

import net.l2emuproject.network.protocol.ClientProtocolVersion;
import net.l2emuproject.proxy.network.Packet;
import net.l2emuproject.proxy.network.game.client.L2GameClient;
import net.l2emuproject.proxy.network.game.server.L2GameServer;
import net.l2emuproject.proxy.script.game.GameScript;

/**
 * A script that prevents MSN chat from being logged at the game server.<BR>
 * <BR>
 * The notification about logging will still be shown to you and your contact.
 * 
 * @author savormix
 */
public final class AntiMsnLogging extends GameScript
{
	/** Constructs this script. */
	public AntiMsnLogging()
	{
		super(new int[] { 0x6D, 0xCE }, null);
	}
	
	@Override
	protected void clientPacketArrived(L2GameClient sender, L2GameServer recipient, Packet packet) throws IllegalStateException
	{
		if ((packet.getForwardedBody().get(0) == 0x6D) == sender.getProtocol().isNewerThanOrEqualTo(ClientProtocolVersion.THE_KAMAEL))
			packet.demandLoss(this);
	}
	
	@Override
	public String getScriptName()
	{
		return "Anti-MSN Logging";
	}
	
	@Override
	public String getAuthor()
	{
		return "savormix";
	}
	
	@Override
	public String getVersionString()
	{
		return "All";
	}
}
