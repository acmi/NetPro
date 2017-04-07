/*
 * Copyright 2011-2017 L2EMU UNIQUE
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
package net.l2emuproject.proxy.script.game.cnc;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.l2emuproject.lang.L2TextBuilder;
import net.l2emuproject.proxy.network.game.client.L2GameClient;
import net.l2emuproject.proxy.network.game.server.L2GameServer;
import net.l2emuproject.proxy.network.meta.EnumeratedPayloadField;
import net.l2emuproject.proxy.network.meta.RandomAccessMMOBuffer;
import net.l2emuproject.proxy.script.game.InteractiveBypasses;
import net.l2emuproject.proxy.script.game.InteractiveChatCommands;
import net.l2emuproject.proxy.script.game.PpeEnabledGameScript;
import net.l2emuproject.proxy.script.packets.util.CommonPacketSender;
import net.l2emuproject.util.logging.L2Logger;

/**
 * @author _dev_
 */
public final class CommandAndControlCenter extends PpeEnabledGameScript implements InteractiveBypasses, InteractiveChatCommands
{
	private static final L2Logger LOG = L2Logger.getLogger(CommandAndControlCenter.class);
	
	private final Map<Object, CnCMenu> _menus;
	
	CommandAndControlCenter()
	{
		_menus = new ConcurrentHashMap<>();
	}
	
	@Override
	public String getName()
	{
		return "C&C Ops";
	}
	
	public void add(Object key, CnCMenu menu)
	{
		_menus.put(key, menu);
	}
	
	public final void add(Object key, CnCAction action)
	{
		final CnCMenu menu = get(key);
		if (menu != null)
			menu.add(action);
	}
	
	public final void addChecked(Object key, CnCAction action) throws MissingCnCMenuException
	{
		final CnCMenu menu = get(key);
		if (menu == null)
			throw new MissingCnCMenuException(key);
		menu.add(action);
	}
	
	public final CnCMenu get(Object key)
	{
		return _menus.get(key);
	}
	
	public final void remove(Object key, CnCMenu menu)
	{
		_menus.remove(key, menu);
	}
	
	public final void remove(Object key, CnCAction action)
	{
		final CnCMenu menu = get(key);
		if (menu != null)
			menu.remove(action);
	}
	
	public final void removeChecked(Object key, CnCAction action) throws MissingCnCMenuException
	{
		final CnCMenu menu = get(key);
		if (menu == null)
			throw new MissingCnCMenuException(key);
		menu.remove(action);
	}
	
	@Override
	public void handleClientPacket(L2GameClient client, L2GameServer server, RandomAccessMMOBuffer buf) throws RuntimeException
	{
		final EnumeratedPayloadField chatCmd = buf.getSingleFieldIndex(CHAT_COMMAND);
		if (chatCmd != null)
		{
			for (final CnCMenu menu : _menus.values())
			{
				if (!menu.getOpenCommand().matcher(buf.readString(chatCmd)).matches())
					continue;
				
				final L2TextBuilder tb = new L2TextBuilder("<html><head><title>L2EMU Unique</title></head><body><center>");
				tb.append("<combobox width=270 var=sub list=\"");
				for (final CnCAction action : menu)
					tb.append(action.getLabel()).append(';');
				tb.setLength(tb.length() - 1);
				tb.append("\"><br><a action=\"bypass -h ").append(menu.getActionBypassPrefix()).append("$sub\">Go</a>");
				CommonPacketSender.sendTutorialHTML(client, tb.append("</center></body></html>").moveToString());
				return;
			}
			return;
		}
		
		final EnumeratedPayloadField bypass = buf.getSingleFieldIndex(TUTORIAL_BYPASS);
		if (bypass == null)
			return;
		
		final String cmd = buf.readString(bypass);
		for (final CnCMenu menu : _menus.values())
		{
			if (!cmd.startsWith(menu.getActionBypassPrefix()))
				continue;
			
			final String label = cmd.substring(menu.getActionBypassPrefix().length());
			final CnCAction action = menu.get(label);
			if (action == null)
			{
				LOG.warn("Unhandled action '" + label + "' by " + menu);
				return;
			}
			action.execute(client);
		}
	}
	
	@Override
	public void handleServerPacket(L2GameClient client, L2GameServer server, RandomAccessMMOBuffer buf) throws RuntimeException
	{
		// nothing to handle
	}
	
	public static CommandAndControlCenter getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static final class SingletonHolder
	{
		static final CommandAndControlCenter INSTANCE = new CommandAndControlCenter();
	}
}
