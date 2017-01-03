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

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import eu.revengineer.simplejse.JavaClassScriptCache;
import eu.revengineer.simplejse.SupportedCompilerType;
import eu.revengineer.simplejse.config.CompilerOptions;
import eu.revengineer.simplejse.config.CompilerOptionsImpl;
import eu.revengineer.simplejse.config.JCSCConfig;
import eu.revengineer.simplejse.config.JCSCConfigFlag;
import eu.revengineer.simplejse.config.ScriptEngineConfig;
import eu.revengineer.simplejse.init.ReloadableScriptInitializer;
import eu.revengineer.simplejse.init.SimpleAbstractScriptInitializer;
import eu.revengineer.simplejse.reporting.DiagnosticLogFile;

import net.l2emuproject.lang.L2System;
import net.l2emuproject.lang.management.StartupManager;
import net.l2emuproject.proxy.NetProInfo;
import net.l2emuproject.util.jar.ClasspathExtractor;
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
	NetProScriptCache(ScriptEngineConfig config)
	{
		super(config);
	}
	
	/** Allows a stale precompiled script cache to be loaded. */
	public void setStaleCacheOK()
	{
		SingletonHolder.FLAGS.remove(DO_NOT_LOAD_STALE_CACHE);
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
		static final Set<JCSCConfigFlag> FLAGS;
		
		static
		{
			SimpleAbstractScriptInitializer.LOG = new NetProScriptLogger(L2Logger.getLogger(ReloadableScriptInitializer.class));
			JavaClassScriptCache.LOG = new NetProScriptLogger(L2Logger.getLogger(NetProScriptCache.class));
			
			INITIALIZER = new ReloadableScriptInitializer();
			
			final String ver = NetProInfo.isUnreleased() ? "" : "_" + (NetProInfo.isSnapshot() ? NetProInfo.getRevisionNumber() : NetProInfo.getVersion());
			Map<SupportedCompilerType, CompilerOptions> compilerOptions = CompilerOptionsImpl.DEFAULTS;
			{
				final String separator = System.getProperty("path.separator", ";"), classpath = System.getProperty("java.class.path", "no" + separator + "classpath");
				if (!L2System.isIDEMode() && classpath.split(separator).length == 1 && classpath.endsWith(".jar"))
				{
					// java -jar somename.jar
					final String ecjCP;
					try
					{
						ecjCP = ClasspathExtractor.getClasspathOf(classpath);
					}
					catch (final IOException e)
					{
						throw new AssertionError("classpath", e);
					}
					compilerOptions = new EnumMap<>(SupportedCompilerType.class);
					// JDK compiler will correctly interpret jar files on classpath
					compilerOptions.put(SupportedCompilerType.JDK, CompilerOptionsImpl.DEFAULTS.get(SupportedCompilerType.JDK));
					// JDT batch compiler will take literal values from classpath; we know for NP that NP jar has ALL library jars
					// so we do not need to recurse further; we can just use it as the classpath for ECJ
					final CompilerOptions oldECJ = CompilerOptionsImpl.DEFAULTS.get(SupportedCompilerType.ECJ);
					final List<String> proc = new ArrayList<>();
					for (final String old : oldECJ.getProcessorOptions())
						proc.add(old);
					proc.add("-classpath");
					proc.add(ecjCP);
					final List<String> comp = new ArrayList<>();
					for (final String old : oldECJ.getCompilerOptions())
						comp.add(old);
					comp.add("-classpath");
					comp.add(ecjCP);
					compilerOptions.put(SupportedCompilerType.ECJ, new CompilerOptionsImpl(proc, comp));
				}
			}
			final ScriptEngineConfig config = JCSCConfig.create(Paths.get("scripts"), Paths.get("script" + ver + ".cache"), JCSCConfig.DEFAULT_ENCODING, compilerOptions,
					new DiagnosticLogFile(Paths.get("script_apt.log")), new DiagnosticLogFile(Paths.get("script.log")), INITIALIZER, JCSCConfig.DEFAULT_BUFFER_SIZE, JCSCConfig.DEFAULT_WRITER_SUPPLIER,
					DEFLATE_CACHE, DO_NOT_LOAD_STALE_CACHE);
			INSTANCE = new NetProScriptCache(config);
			FLAGS = config.getFlags();
			
			StartupManager.markInitialized(NetProScriptCache.class);
		}
	}
}
