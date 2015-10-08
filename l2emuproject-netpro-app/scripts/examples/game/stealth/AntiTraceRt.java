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

import java.nio.ByteBuffer;

import org.apache.commons.lang3.ArrayUtils;

import net.l2emuproject.network.ClientProtocolVersion;
import net.l2emuproject.proxy.network.Packet;
import net.l2emuproject.proxy.network.game.client.L2GameClient;
import net.l2emuproject.proxy.network.game.server.L2GameServer;
import net.l2emuproject.proxy.script.game.GameScript;

/**
 * A script that automatically removes client's IP & hops sent in the <TT>RequestEnterWorld</TT> packet.<BR>
 * <BR>
 * By default, client sends a valid IP, but some hops may be 0.0.0.0. With this script, both the IP
 * and all hops are changed to 0.0.0.0.
 * 
 * @author savormix
 */
public final class AntiTraceRt extends GameScript
{
	private static final int REQUEST_ENTER_WORLD = 17;
	private static final byte[] NULL_IPS = new byte[20]; // prevent allocations
	
	/** Constructs this script. */
	public AntiTraceRt()
	{
		super(new int[] { REQUEST_ENTER_WORLD }, ArrayUtils.EMPTY_INT_ARRAY);
	}
	
	@Override
	protected void clientPacketArrived(L2GameClient sender, L2GameServer recipient, Packet packet)
	{
		if (sender.getProtocol().isOlderThan(ClientProtocolVersion.GRACIA_FINAL))
			return;
			
		final ByteBuffer newBody = packet.getDefaultBufferForModifications();
		newBody.position(sender.getProtocol().isNewerThanOrEqualTo(ClientProtocolVersion.VALIANCE) ? 1 : 85); // ignore opcode & whatever
		newBody.put(NULL_IPS);
		// ignore everything else
		packet.setForwardedBody(newBody);
	}
	
	@Override
	public String getScriptName()
	{
		return "Anti-tracert";
	}
	
	@Override
	public String getAuthor()
	{
		return "savormix";
	}
	
	@Override
	public String getVersionString()
	{
		return "GF and above";
	}
}
