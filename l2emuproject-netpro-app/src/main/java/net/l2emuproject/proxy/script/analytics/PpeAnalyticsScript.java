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
package net.l2emuproject.proxy.script.analytics;

import net.l2emuproject.proxy.network.game.client.L2GameClient;
import net.l2emuproject.proxy.network.game.server.L2GameServer;
import net.l2emuproject.proxy.network.meta.RandomAccessMMOBuffer;
import net.l2emuproject.proxy.script.PpeEnabledLoaderScript;
import net.l2emuproject.proxy.script.PpeEnabledLoaderScriptRegistry;
import net.l2emuproject.proxy.script.game.PpeEnabledGameScript;
import net.l2emuproject.proxy.state.entity.context.ICacheServerID;
import net.l2emuproject.proxy.ui.savormix.loader.LoadOption;

/**
 * A script that operates on a or enables certain state about each game world to be saved.
 * 
 * @author _dev_
 */
public abstract class PpeAnalyticsScript extends PpeEnabledGameScript implements PpeEnabledLoaderScript
{
	@Override
	public abstract void handleClientPacket(RandomAccessMMOBuffer buf, ICacheServerID cacheContext) throws RuntimeException;
	
	@Override
	public abstract void handleServerPacket(RandomAccessMMOBuffer buf, ICacheServerID cacheContext) throws RuntimeException;
	
	@Override
	public void handleClientPacket(L2GameClient client, L2GameServer server, RandomAccessMMOBuffer buf) throws RuntimeException
	{
		handleClientPacket(buf, getEntityContext(server));
	}
	
	@Override
	public void handleServerPacket(L2GameClient client, L2GameServer server, RandomAccessMMOBuffer buf) throws RuntimeException
	{
		handleServerPacket(buf, getEntityContext(server));
	}
	
	@Override
	public void onLoad() throws RuntimeException
	{
		if (LoadOption.DISABLE_PROXY.isNotSet())
			super.onLoad();
		if (LoadOption.DISABLE_UI.isNotSet())
			PpeEnabledLoaderScriptRegistry.getInstance().register(this);
	}
	
	@Override
	public void onUnload() throws RuntimeException
	{
		if (LoadOption.DISABLE_PROXY.isNotSet())
			super.onUnload();
		if (LoadOption.DISABLE_UI.isNotSet())
			PpeEnabledLoaderScriptRegistry.getInstance().remove(this);
	}
}
