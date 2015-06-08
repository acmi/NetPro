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
package net.l2emuproject.proxy.ui.savormix.loader;

import java.util.Locale;

/**
 * Specifies an argument supported by the {@link Loader} class.
 * 
 * @author savormix
 */
public enum LoadOption
{
	/** Displays supported command line options */
	HELP("displays this text", "/?", "-h", "--help"),
	/** Starts NetPro without the graphical user interface */
	DISABLE_UI("disables the graphical user interface (implies --disable-definitions)", "-du", "--disable-ui"),
	/** Starts NetPro without opening any listening sockets */
	DISABLE_PROXY("disables the proxy server", "-dp", "--disable-proxy"),
	/** Disables packet defintions from being loaded */
	DISABLE_DEFS("disables packet definitions (all packets will be treated as unknown)", "-dd", "--disable-definitions"),
	/** Disables scripts from being loaded and initialized */
	DISABLE_SCRIPTS(true, "disables scripts (both live and log file)", "-ds", "--disable-scripts"),
	/** Writes all session data to the filename of a packet log. */
	FORCE_FULL_LOG_FILENAME("force packet log filename to contain fields that can be inferred from the directory tree", "-fff", "--force-full-filename"),
	// packet enumerators: notable performance degradation if broadly used on the I/O thread
	/** Not implemented */
	DISABLE_ENUMERATORS(true, "disables packet enumerators", "-de", "--disable-enumerators"),
	
	// developer options: remove annoying clutter
	@SuppressWarnings("javadoc")
	HIDE_SPLASH(true, "does not show a splash screen", "-hs", "--hide-splash"), @SuppressWarnings("javadoc")
	HIDE_OVERLAY(true, "does not show a translucent watermark overlay on certain application windows", "-ho", "--hide-overlay"), @SuppressWarnings("javadoc")
	HIDE_CONTRIBUTORS(true, "does not show contributor details on the main application window", "-hc", "--hide-contributors"), @SuppressWarnings("javadoc")
	HIDE_LOG_CONSOLE(true, "does not show console in GUI", "-hl", "--hide-log-console"),
	// very handy when you want to keep incomplete options
	// in order to switch between UI only/mixed modes etc.
	@SuppressWarnings("javadoc")
	IGNORE_UNKNOWN(true, "does not warn about unrecognized command line arguments", "-iu", "--ignore-unrecognized-args");
	
	private final boolean _hidden;
	private final String _description;
	private final String[] _alias;
	
	private LoadOption(String description, String... aliases)
	{
		this(false, description, aliases);
	}
	
	private LoadOption(boolean hidden, String description, String... aliases)
	{
		_hidden = hidden;
		_description = description;
		_alias = new String[aliases.length];
		for (int i = 0; i < _alias.length; ++i)
			_alias[i] = aliases[i].toLowerCase(Locale.ENGLISH);
	}
	
	String getDescription()
	{
		return _description;
	}
	
	String[] getAlias()
	{
		return _alias;
	}
	
	void setSystemProperty()
	{
		System.setProperty(getPropertyName(), "");
	}
	
	/**
	 * Tests whether this option has been specified when running the application.
	 * 
	 * @return whether the option was explicitly specified
	 */
	public boolean isSet()
	{
		return !isNotSet();
	}
	
	/**
	 * Tests whether this option has not been specified when running the application.
	 * 
	 * @return whether the option was not explicitly specified
	 */
	public boolean isNotSet()
	{
		return System.getProperty(getPropertyName()) == null;
	}
	
	boolean isInHelp()
	{
		return !isHidden();
	}
	
	private boolean isHidden()
	{
		return _hidden;
	}
	
	private String getPropertyName()
	{
		final StringBuilder sb = new StringBuilder(getClass().getSimpleName());
		sb.append('#').append(name());
		return sb.toString();
	}
	
	/**
	 * Returns an associated option for the given command line parameter. Returns {@code null} if a parameter does not specify
	 * one of the supported command line options.
	 * 
	 * @param alias command line param
	 * @return associated load option or {@code null}
	 */
	public static LoadOption getByAlias(String alias)
	{
		return CACHE.getLookup().get(alias.toLowerCase(Locale.ENGLISH));
	}
	
	private static final LoadOptionCache CACHE = new LoadOptionCache();
}
