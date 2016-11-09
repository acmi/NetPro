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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.mutable.MutableInt;

import net.l2emuproject.proxy.network.game.client.L2GameClient;
import net.l2emuproject.proxy.network.game.server.L2GameServer;
import net.l2emuproject.proxy.network.meta.RandomAccessMMOBuffer;
import net.l2emuproject.proxy.script.analytics.user.LiveUserAnalytics;
import net.l2emuproject.proxy.script.analytics.user.LiveUserAnalytics.UserInfo;
import net.l2emuproject.proxy.script.game.InteractiveChatCommands;
import net.l2emuproject.proxy.script.game.PpeEnabledGameScript;
import net.l2emuproject.proxy.script.packets.InvalidPacketWriterArgumentsException;
import net.l2emuproject.proxy.script.packets.util.CommonPacketSender;
import net.l2emuproject.proxy.state.entity.L2ObjectInfoCache;

/**
 * Helps visualizing polygon points as they are visited.
 * 
 * @author _dev_
 */
public class LocationMarker extends PpeEnabledGameScript implements InteractiveChatCommands
{
	private final Map<L2GameClient, MutableInt> _visibleItems;
	
	/** Constructs this interactive script. */
	public LocationMarker()
	{
		_visibleItems = new ConcurrentHashMap<>();
	}
	
	@Override
	public String getName()
	{
		return "Location marker";
	}
	
	@Override
	public void handleClientPacket(L2GameClient client, L2GameServer server, RandomAccessMMOBuffer buf) throws RuntimeException
	{
		final String msg = buf.readFirstString(CHAT_COMMAND);
		try
		{
			if ("\\\\loc_reset".equals(msg))
			{
				final MutableInt visibleItems = _visibleItems.remove(client);
				if (visibleItems == null)
				{
					CommonPacketSender.sendChatMessage(client, 5, "SYS", "No markers are visible.");
					return;
				}
				
				if (visibleItems.intValue() < 1)
					return;
				
				synchronized (visibleItems)
				{
					for (int oid = visibleItems.intValue(); oid > 0; --oid)
						CommonPacketSender.sendDeleteObject(client, oid, 0);
					visibleItems.setValue(0);
				}
				CommonPacketSender.sendChatMessage(client, 5, "SYS", "Location markers removed.");
				return;
			}
			
			if (!"\\\\loc".equals(msg))
				return;
			
			MutableInt maxOID = _visibleItems.get(client);
			if (maxOID == null)
			{
				final MutableInt tracker = new MutableInt();
				maxOID = _visibleItems.putIfAbsent(client, tracker);
				if (maxOID == null)
					maxOID = tracker;
			}
			
			final UserInfo ui = LiveUserAnalytics.getInstance().getUserInfo(client);
			synchronized (maxOID)
			{
				maxOID.add(1);
				CommonPacketSender.sendSpawnItem(client, maxOID.intValue(), 3434,
						L2ObjectInfoCache.getOrAdd(ui.getUserOID(), getEntityContext(server)).getExtraInfo().getCurrentLocation(), false, 1L, 0);
			}
			CommonPacketSender.sendUserCmd(server, 0);
		}
		catch (final InvalidPacketWriterArgumentsException e)
		{
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public void handleServerPacket(L2GameClient client, L2GameServer server, RandomAccessMMOBuffer buf) throws RuntimeException
	{
		// do nothing here
	}
}
