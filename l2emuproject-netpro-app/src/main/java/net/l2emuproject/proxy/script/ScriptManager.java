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

import java.util.EnumMap;
import java.util.Map;

import javolution.util.FastMap;

import net.l2emuproject.proxy.network.ServiceType;
import net.l2emuproject.proxy.script.Script.WrapperPool;
import net.l2emuproject.proxy.script.game.GameScript;
import net.l2emuproject.proxy.script.login.LoginScript;
import net.l2emuproject.util.logging.L2Logger;

/**
 * Manages all active scripts.
 * 
 * @author savormix
 */
@SuppressWarnings("rawtypes")
public final class ScriptManager
{
	private static final L2Logger LOG = L2Logger.getLogger(LogLoadScript.class);
	
	private final Map<ServiceType, Map<Class<? extends Script>, Script>> _scripts;
	private final WrapperPool _wrapperPool;
	
	ScriptManager()
	{
		_scripts = new EnumMap<>(ServiceType.class);
		_scripts.put(ServiceType.LOGIN, FastMap.<Class<? extends Script>, Script> newInstance().setShared(true));
		_scripts.put(ServiceType.GAME, FastMap.<Class<? extends Script>, Script> newInstance().setShared(true));
		_wrapperPool = new WrapperPool();
	}
	
	/**
	 * Returns a buffer wrapper allocation and recycling manager.
	 * 
	 * @return buffer wrapper pool.
	 */
	public WrapperPool getWrapperPool()
	{
		return _wrapperPool;
	}
	
	/**
	 * Adds a new script to be managed. This does not change whether the script is enabled or not.
	 * 
	 * @param script a script
	 */
	public void addScript(Script<?, ?> script)
	{
		final ServiceType key;
		if (script instanceof LoginScript)
			key = ServiceType.LOGIN;
		else if (script instanceof GameScript)
			key = ServiceType.GAME;
		else
		{
			LOG.error("Unsupported script: " + script.getClass());
			return;
		}
		
		final Script<?, ?> old = _scripts.get(key).put(script.getClass(), script);
		if (old != null && old != script)
		{
			old.tearDown();
			LOG.info(old + " was replaced by " + script);
		}
		else
			script.setUp();
	}
	
	/**
	 * Returns a singleton instance of this type.
	 * 
	 * @return an instance of this class
	 */
	public static final ScriptManager getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static final class SingletonHolder
	{
		static final ScriptManager INSTANCE = new ScriptManager();
	}
}
