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
package net.l2emuproject.proxy.script;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.EnumMap;
import java.util.Map;

import javolution.util.FastMap;

import net.l2emuproject.network.mmocore.MMOBuffer;
import net.l2emuproject.network.protocol.IProtocolVersion;
import net.l2emuproject.proxy.network.ServiceType;
import net.l2emuproject.proxy.script.Script.WrapperPool;
import net.l2emuproject.proxy.ui.savormix.io.task.HistoricalPacketLog;
import net.l2emuproject.util.logging.L2Logger;

/**
 * Manages scripts that deal with packets originating from historical packet logs.
 * 
 * @author savormix
 */
public final class LogLoadScriptManager
{
	private static final L2Logger LOG = L2Logger.getLogger(LogLoadScript.class);
	
	private final Map<ServiceType, Map<Class<? extends LogLoadScript>, LogLoadScript>> _scripts;
	private final WrapperPool _wrapperPool;
	
	LogLoadScriptManager()
	{
		_scripts = new EnumMap<>(ServiceType.class);
		_scripts.put(ServiceType.LOGIN, FastMap.<Class<? extends LogLoadScript>, LogLoadScript> newInstance().setShared(true));
		_scripts.put(ServiceType.GAME, FastMap.<Class<? extends LogLoadScript>, LogLoadScript> newInstance().setShared(true));
		_wrapperPool = new WrapperPool();
	}
	
	/**
	 * Notifies all scripts about a packet, newly loaded from a historical packet log.
	 * 
	 * @param login login[t]/game[f]
	 * @param client client[t]/server[f]
	 * @param body packet body
	 * @param version network protocol version
	 * @param cacheContext entity existence boundary defining context
	 */
	public void onLoadedPacket(boolean login, boolean client, byte[] body, IProtocolVersion version, HistoricalPacketLog cacheContext)
	{
		final ByteBuffer bb = ByteBuffer.wrap(body).asReadOnlyBuffer().order(ByteOrder.LITTLE_ENDIAN);
		final MMOBuffer buf = _wrapperPool.create();
		try
		{
			for (LogLoadScript script : _scripts.get(login ? ServiceType.LOGIN : ServiceType.GAME).values())
			{
				bb.clear();
				buf.setByteBuffer(bb);
				
				if (!script.isHandled(client, bb.get(0) & 0xFF))
					continue;
				
				try
				{
					if (client)
						script.handleClientPacket(buf, version, cacheContext);
					else
						script.handleServerPacket(buf, version, cacheContext);
				}
				catch (RuntimeException e)
				{
					LOG.error(script, e);
				}
			}
		}
		finally
		{
			_wrapperPool.store(buf);
		}
	}
	
	/**
	 * Adds a script to receive notifications about packets loaded from historical logs.
	 * 
	 * @param script a script
	 */
	public void addScript(LogLoadScript script)
	{
		LogLoadScript old = _scripts.get(script.isLogin() ? ServiceType.LOGIN : ServiceType.GAME).put(script.getClass(), script);
		if (old != null)
			LOG.warn(old + " was replaced by a newer version.");
	}
	
	/**
	 * Returns a singleton instance of this type.
	 * 
	 * @return an instance of this class
	 */
	public static final LogLoadScriptManager getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static final class SingletonHolder
	{
		static final LogLoadScriptManager INSTANCE = new LogLoadScriptManager();
	}
}
