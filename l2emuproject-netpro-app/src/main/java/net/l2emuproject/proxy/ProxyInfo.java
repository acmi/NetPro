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
package net.l2emuproject.proxy;

import eu.revengineer.simplejse.init.IScriptInitializer;

import net.l2emuproject.lang.L2TextBuilder;
import net.l2emuproject.network.IProtocolVersion;
import net.l2emuproject.proxy.network.AbstractL2Proxy;
import net.l2emuproject.proxy.network.Proxy;
import net.l2emuproject.ui.AsyncTask;
import net.l2emuproject.util.jar.FormattedVersion;

/**
 * Generic class to provide version info.
 * 
 * @author savormix
 */
public final class ProxyInfo
{
	private ProxyInfo()
	{
		// utility class
	}
	
	private static final FormattedVersion PROXY_VERSION = new FormattedVersion(L2Proxy.class);
	private static final FormattedVersion GENERIC_COMMONS_VERSION = new FormattedVersion(IProtocolVersion.class);
	private static final FormattedVersion GENERIC_COMMONS_UI_VERSION = new FormattedVersion(AsyncTask.class);
	private static final FormattedVersion PROXY_COMMONS_VERSION = new FormattedVersion(Proxy.class);
	private static final FormattedVersion PROXY_COMMONS_L2_VERSION = new FormattedVersion(AbstractL2Proxy.class);
	private static final FormattedVersion SCRIPT_ENGINE_VERSION = new FormattedVersion(IScriptInitializer.class);
	
	/** Shows startup and version information. */
	public static void showStartupInfo()
	{
		System.out.println("___      ___                ________      .__________.");
		System.out.println("`MM\\     `M'                `MMMMMMMb.    | " + whitespace(PROXY_VERSION) + " |");
		System.out.println(" MMM\\     M           /      MM    `Mb    |__________|");
		System.out.println(" M\\MM\\    M   ____   /M      MM     MM ___  __   _____");
		System.out.println(" M \\MM\\   M  6MMMMb /MMMMM   MM     MM `MM 6MM  6MMMMMb");
		System.out.println(" M  \\MM\\  M 6M'  `Mb MM      MM    .M9  MM69 \" 6M'   `Mb");
		System.out.println(" M   \\MM\\ M MM    MM MM      MMMMMMM9'  MM'    MM     MM");
		System.out.println(" M    \\MM\\M MMMMMMMM MM      MM         MM     MM     MM");
		System.out.println(" M     \\MMM MM       MM      MM         MM     MM     MM");
		System.out.println(" M      \\MM YM    d9 YM.  ,  MM         MM     YM.   ,M9");
		System.out.println("_M_      \\M  YMMMM9   YMMM9 _MM_       _MM_     YMMMMM9");
	}
	
	private static String whitespace(FormattedVersion version)
	{
		final L2TextBuilder tb = new L2TextBuilder();
		for (int i = version.getRevisionNumber().length(); i < 8; ++i)
			tb.append(' ');
		return tb.insert(tb.length() >> 1, version.getRevisionNumber()).moveToString();
	}
	
	/**
	 * Returns version information string.
	 * 
	 * @return version info
	 */
	public static String getVersionInfo()
	{
		return PROXY_VERSION.getVersionInfo();
	}
	
	/**
	 * Returns the number of the latest commit.
	 * 
	 * @return revision number
	 */
	public static String getRevisionNumber()
	{
		return PROXY_VERSION.getRevisionNumber();
	}
	
	/**
	 * Returns application version string.
	 * 
	 * @return application version
	 */
	public static String getVersion()
	{
		return PROXY_VERSION.getVersionNumber();
	}
	
	/**
	 * Returns whether the application is executed directly off java classes instead of a properly built release JAR.
	 * 
	 * @return whether the application does not specify a version
	 */
	public static boolean isUnreleased()
	{
		return "exported".equals(ProxyInfo.getRevisionNumber());
	}
	
	/**
	 * Returns whether this version represents a snapshot.
	 * 
	 * @return whether this is a snapshot release
	 */
	public static boolean isSnapshot()
	{
		return getVersion().contains("SNAPSHOT");
	}
	
	/**
	 * Returns version info of all related components.
	 * 
	 * @return complete version info
	 */
	public static String[] getFullVersionInfo()
	{
		// @formatter:off
		final String[] full = {
				"l2emuproject-netpro-app:        " + PROXY_VERSION.getFullVersionInfo(),
				"l2emuproject-netpro-commons:    " + PROXY_COMMONS_VERSION.getFullVersionInfo(),
				"l2emuproject-netpro-commons-l2: " + PROXY_COMMONS_L2_VERSION.getFullVersionInfo(),
				"l2emuproject-commons:           " + GENERIC_COMMONS_VERSION.getFullVersionInfo(),
				"l2emuproject-commons-ui:        " + GENERIC_COMMONS_UI_VERSION.getFullVersionInfo(),
				"simplejse:                      " + SCRIPT_ENGINE_VERSION.getFullVersionInfo(),
		};
		// @formatter:on
		return full;
	}
}
