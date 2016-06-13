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

import static eu.revengineer.simplejse.config.JCSCConfigFlag.DEFLATE_CACHE;
import static eu.revengineer.simplejse.config.JCSCConfigFlag.DO_NOT_LOAD_STALE_CACHE;

import java.nio.file.Paths;

import eu.revengineer.simplejse.JavaClassScriptCache;
import eu.revengineer.simplejse.config.JCSCConfig;
import eu.revengineer.simplejse.config.ScriptEngineConfig;
import eu.revengineer.simplejse.init.ReloadableScriptInitializer;
import eu.revengineer.simplejse.reporting.AptReportingHandler;
import eu.revengineer.simplejse.reporting.DiagnosticLogFile;
import eu.revengineer.simplejse.reporting.JavacReportingHandler;

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
	/** Allows to override the default {@code apt} handler */
	public static AptReportingHandler INITIALIZER_APT_HANDLER = new DiagnosticLogFile(Paths.get("script_apt.log"));
	/** Allows to override the default {@code javac} handler */
	public static JavacReportingHandler INITIALIZER_JAVAC_HANDLER = new DiagnosticLogFile(Paths.get("script.log"));
	
	NetProScriptCache(ScriptEngineConfig config)
	{
		super(config);
	}
	
	/**
	 * Returns the expected precompiled script cache filename.
	 * 
	 * @return script cache name
	 */
	public static final String getScriptCacheName()
	{
		final String ver = NetProInfo.isUnreleased() ? "" : "_" + (NetProInfo.isSnapshot() ? NetProInfo.getRevisionNumber() : NetProInfo.getVersion());
		return "script" + ver + ".cache";
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
			JavaClassScriptCache.installLoggers(c -> new NetProScriptLogger(L2Logger.getLogger(c)));
			
			INITIALIZER = new ReloadableScriptInitializer();
			INSTANCE = new NetProScriptCache(
					JCSCConfig.create(Paths.get("scripts"), Paths.get(getScriptCacheName()), INITIALIZER_APT_HANDLER, INITIALIZER_JAVAC_HANDLER, INITIALIZER, DEFLATE_CACHE, DO_NOT_LOAD_STALE_CACHE));
			
			StartupManager.markInitialized(NetProScriptCache.class);
		}
	}
}
