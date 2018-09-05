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
import net.l2emuproject.proxy.script.ScriptFieldAlias;
import net.l2emuproject.proxy.script.game.PpeEnabledGameScript;
import net.l2emuproject.proxy.script.packets.util.CommonPacketSender;
import net.l2emuproject.proxy.state.entity.L2ObjectInfo;
import net.l2emuproject.proxy.state.entity.L2ObjectInfoCache;
import net.l2emuproject.proxy.state.entity.ObjectInfo;

/**
 * Notifies the user about their spoil skill usage success ON SCREEN.
 * 
 * @author _dev_
 */
public class TargetLevelDifferenceReporter extends PpeEnabledGameScript
{
	@ScriptFieldAlias
	private static final String TARGET_OID = "mts_oid";
	@ScriptFieldAlias
	private static final String LEVEL_DIFFERENCE = "mts_level_diff";
	
	@Override
	public String getName()
	{
		return "Level difference reporter";
	}
	
	@Override
	public void handleClientPacket(L2GameClient client, L2GameServer server, RandomAccessMMOBuffer buf) throws RuntimeException
	{
		// nothing to do here
	}
	
	@Override
	public void handleServerPacket(L2GameClient client, L2GameServer server, RandomAccessMMOBuffer buf) throws RuntimeException
	{
		final int oid = buf.readFirstInteger32(TARGET_OID);
		final int diff = buf.readFirstInteger32(LEVEL_DIFFERENCE);
		if (diff == 0)
			return;
		
		final ObjectInfo<L2ObjectInfo> npc = L2ObjectInfoCache.getOrAdd(oid, getEntityContext(server));
		final StringBuilder sb = new StringBuilder();
		sb.append(npc.getName()).append(" is ");
		if (diff > 0)
			sb.append(diff).append(" level(s) below you");
		else
			sb.append(-diff).append(" level(s) above you");
		CommonPacketSender.sendChatMessage(client, 5, "SYS", sb.toString());
	}
}
