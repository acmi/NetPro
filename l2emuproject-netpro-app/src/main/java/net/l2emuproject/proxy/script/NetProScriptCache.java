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

import java.nio.file.Paths;
import java.util.EnumSet;
import java.util.Set;

import eu.revengineer.simplejse.JavaClassScriptCache;
import eu.revengineer.simplejse.config.JCSCConfig;
import eu.revengineer.simplejse.config.JCSCConfigFlag;
import eu.revengineer.simplejse.init.ReloadableScriptInitializer;
import eu.revengineer.simplejse.init.SimpleAbstractScriptInitializer;

import net.l2emuproject.lang.management.StartupManager;
import net.l2emuproject.proxy.NetProInfo;
import net.l2emuproject.util.logging.L2Logger;

/**
 * Provides a dedicated cross-compatible interface to manage all types of java scripts during runtime.<BR>
 * <BR>
 * In reality, this is just a copy-pasted L2ScriptCache with removed legacy script support and less configurable options.
 * 
 * @author _dev_
 */
public class NetProScriptCache extends JavaClassScriptCache
{
	NetProScriptCache(JCSCConfig config)
	{
		super(config);
	}
	
	/**
	 * Returns the compiled script initializer/unloadable script manager.
	 * 
	 * @return script initializer
	 */
	public static final ReloadableScriptInitializer getInitializer()
	{
		return SingletonHolder.INITIALIZER;
	}
	
	/**
	 * Returns a singleton instance of this type.
	 * 
	 * @return an instance of this class
	 */
	public static final NetProScriptCache getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static final class SingletonHolder
	{
		static final ReloadableScriptInitializer INITIALIZER;
		static final NetProScriptCache INSTANCE;
		
		static
		{
			SimpleAbstractScriptInitializer.LOG = new NetProScriptLogger(L2Logger.getLogger(ReloadableScriptInitializer.class));
			JavaClassScriptCache.LOG = new NetProScriptLogger(L2Logger.getLogger(NetProScriptCache.class));
			
			INITIALIZER = new ReloadableScriptInitializer();
			
			final Set<JCSCConfigFlag> flags = EnumSet.noneOf(JCSCConfigFlag.class);
			flags.add(JCSCConfigFlag.DEFLATE_CACHE);
			
			final String ver = NetProInfo.isUnreleased() ? "" : "_" + (NetProInfo.isSnapshot() ? NetProInfo.getRevisionNumber() : NetProInfo.getVersion());
			INSTANCE = new NetProScriptCache(new JCSCConfig(Paths.get("scripts"), Paths.get("script" + ver + ".cache"), Paths.get("script_apt.log"), Paths.get("script.log"), flags, INITIALIZER));
			
			StartupManager.markInitialized(NetProScriptCache.class);
		}
	}
}
