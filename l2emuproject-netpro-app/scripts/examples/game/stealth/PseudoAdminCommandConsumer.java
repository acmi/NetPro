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

import static net.l2emuproject.network.protocol.ClientProtocolVersion.THE_KAMAEL;

import java.nio.ByteBuffer;

import net.l2emuproject.network.mmocore.MMOBuffer;
import net.l2emuproject.proxy.network.Packet;
import net.l2emuproject.proxy.network.game.client.L2GameClient;
import net.l2emuproject.proxy.network.game.server.L2GameServer;
import net.l2emuproject.proxy.script.game.GameScript;

/**
 * Consumes bypasses and chat messages of a specific format (as NetPro handles them asynchronously).
 * 
 * @author _dev_
 */
public class PseudoAdminCommandConsumer extends GameScript
{
	private static final int REQ_BYPASS = 0x23, REQ_BYPASS_LEGACY = 0x21;
	private static final int REQ_SAY2 = 0x49, REQ_SAY2_LEGACY = 0x38;
	private static final int REQ_TUTORIAL_CMD = 0x86, REQ_TUTORIAL_CMD_LEGACY = 0x7C;
	
	/** Constructs this script. */
	public PseudoAdminCommandConsumer()
	{
		super(new int[] { REQ_BYPASS_LEGACY, REQ_BYPASS, REQ_SAY2_LEGACY, REQ_SAY2, REQ_TUTORIAL_CMD_LEGACY, REQ_TUTORIAL_CMD }, null);
	}
	
	@Override
	protected void clientPacketArrived(L2GameClient sender, L2GameServer recipient, Packet packet) throws RuntimeException
	{
		final boolean legacy = sender.getProtocol().isOlderThan(THE_KAMAEL);
		switch (packet.getReceivedBody().get(0))
		{
			case REQ_BYPASS_LEGACY:
			case REQ_SAY2_LEGACY:
			case REQ_TUTORIAL_CMD_LEGACY:
				if (!legacy)
					return;
				break;
			default:
				if (legacy)
					return;
				break;
		}
		
		final ByteBuffer original = packet.getReceivedBody();
		if (original == null) // injected
			return;
		
		original.position(1); // skip single opcode
		final MMOBuffer buf = new MMOBuffer();
		buf.setByteBuffer(original);
		
		final String cmd = buf.readS();
		// double-backslash prefix for chat messages, __l2emu for normal/tutorial bypasses
		if (cmd.startsWith("\\\\") || cmd.startsWith("__l2emu"))
			packet.demandLoss(this);
	}
	
	@Override
	public String getScriptName()
	{
		return "Pseudo-Admin Async Enabler";
	}
	
	@Override
	public String getAuthor()
	{
		return "_dev_";
	}
	
	@Override
	public String getVersionString()
	{
		return "All";
	}
}
