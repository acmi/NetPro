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
package net.l2emuproject.proxy.setup;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import net.l2emuproject.proxy.io.IOConstants;
import net.l2emuproject.util.L2XMLUtils;
import net.l2emuproject.util.logging.L2Logger;

/**
 * Manages IP aliases to be used for the GUI elements.
 * 
 * @author _dev_
 */
public final class IPAliasManager implements IOConstants
{
	private static final L2Logger LOG = L2Logger.getLogger(IPAliasManager.class);
	
	private final Map<String, String> _aliases;
	
	IPAliasManager()
	{
		_aliases = new HashMap<>();
		
		// predefined
		try
		{
			final Node root = L2XMLUtils.childNamed(L2XMLUtils.getXMLFile(CONFIG_DIRECTORY.resolve("ipalias.xml")), "addresses");
			for (final Node addr : L2XMLUtils.listNodesByNodeName(root, "address"))
				_aliases.put(L2XMLUtils.getString(addr, "ip"), L2XMLUtils.getString(addr, "alias"));
		}
		catch (IOException | ParserConfigurationException | SAXException e)
		{
			LOG.warn("IP alias definition file missing.");
		}
		
		// custom user-defined (automatic override)
		try
		{
			final Node root = L2XMLUtils.childNamed(L2XMLUtils.getXMLFile(APPLICATION_DIRECTORY.resolve("ipalias.xml")), "addresses");
			for (final Node addr : L2XMLUtils.listNodesByNodeName(root, "address"))
				_aliases.put(L2XMLUtils.getString(addr, "ip"), L2XMLUtils.getString(addr, "alias"));
		}
		catch (NoSuchFileException | FileNotFoundException e)
		{
			// ignore
		}
		catch (IOException | ParserConfigurationException | SAXException e)
		{
			_aliases.clear();
			LOG.error("User's IP aliases", e);
		}
	}
	
	/**
	 * Returns an alias for the given address. If there is no alias defined, returns {@code hostAddress}.
	 * 
	 * @param hostAddress an address
	 * @return alias or {@code hostAddress}
	 */
	public String getAlias(String hostAddress)
	{
		return _aliases.getOrDefault(hostAddress, hostAddress);
	}
	
	/**
	 * Calls {@link #getAlias(String)}.
	 * 
	 * @param hostAddress an address
	 * @return alias or {@code hostAddress}
	 */
	public static String toUserFriendlyString(String hostAddress)
	{
		return getInstance().getAlias(hostAddress);
	}
	
	/**
	 * Returns a singleton object.
	 * 
	 * @return an instance of this class
	 */
	public static IPAliasManager getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static final class SingletonHolder
	{
		static final IPAliasManager INSTANCE = new IPAliasManager();
	}
}
