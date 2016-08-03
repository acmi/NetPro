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
package examples.game.helper;

import net.l2emuproject.proxy.network.game.client.L2GameClient;
import net.l2emuproject.proxy.network.game.server.L2GameServer;
import net.l2emuproject.proxy.network.meta.RandomAccessMMOBuffer;
import net.l2emuproject.proxy.script.game.PpeEnabledGameScript;
import net.l2emuproject.proxy.script.packets.util.CommonPacketSender;
import net.l2emuproject.proxy.script.packets.util.SystemMessageRecipient;

/**
 * Notifies the user about their spoil skill usage success ON SCREEN.
 * 
 * @author _dev_
 */
public class SpoilSuccessNotifier extends PpeEnabledGameScript implements SystemMessageRecipient
{
	private static final int SM_ALREADY_SPOILED = 357;
	private static final int SM_SPOILED = 612;
	
	private static final int POS_SUCCESS = 2, POS_REDUNDANT = 7;
	private static final boolean SMALL_FONT = true, FADE = false;
	private static final int DURATION = 1_500;
	
	@Override
	public String getName()
	{
		return "Spoil success notifier";
	}
	
	@Override
	public void handleClientPacket(L2GameClient client, L2GameServer server, RandomAccessMMOBuffer buf) throws RuntimeException
	{
		// nothing to do here
	}
	
	@Override
	public void handleServerPacket(L2GameClient client, L2GameServer server, RandomAccessMMOBuffer buf) throws RuntimeException
	{
		final int message = buf.readFirstInteger32(SYSMSG_ID);
		switch (message)
		{
			case SM_SPOILED:
				CommonPacketSender.sendImmutableScreenSystemMessage(client, message, POS_SUCCESS, 0, SMALL_FONT, 0, 1, false, DURATION, FADE);
				break;
			case SM_ALREADY_SPOILED:
				CommonPacketSender.sendImmutableScreenSystemMessage(client, message, POS_REDUNDANT, 0, SMALL_FONT, 0, 1, false, DURATION, FADE);
				break;
			default:
				return;
		}
	}
}
