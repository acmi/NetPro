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
import java.net.InetAddress;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import net.l2emuproject.network.IPv4AddressPrefix;
import net.l2emuproject.network.IPv4AddressTrie;
import net.l2emuproject.proxy.network.BindableSocketSet;
import net.l2emuproject.proxy.network.ListenSocket;
import net.l2emuproject.proxy.network.ProxySocket;
import net.l2emuproject.proxy.ui.savormix.io.base.IOConstants;
import net.l2emuproject.util.L2XMLUtils;
import net.l2emuproject.util.logging.L2Logger;

/**
 * Manages local and remote service sockets.
 * 
 * @author _dev_
 */
public final class SocketManager implements IOConstants
{
	private static final L2Logger LOG = L2Logger.getLogger(SocketManager.class);
	
	private static final Pattern IPV4_ADDRESS_PREFIX = Pattern.compile(
			"((?:25[0-5])|(?:2[0-4][0-9])|(?:1[0-9][0-9])|(?:[1-9]?[0-9]))\\.((?:25[0-5])|(?:2[0-4][0-9])|(?:1[0-9][0-9])|(?:[1-9]?[0-9]))\\.((?:25[0-5])|(?:2[0-4][0-9])|(?:1[0-9][0-9])|(?:[1-9]?[0-9]))\\.((?:25[0-5])|(?:2[0-4][0-9])|(?:1[0-9][0-9])|(?:[1-9]?[0-9]))/((?:[1-2]?[0-9])|(?:3[0-2]))");
			
	private final IPv4AddressTrie<ListenSocket> _gameWorldHubSockets;
	private final BindableSocketSet<ProxySocket> _authSockets;
	
	SocketManager()
	{
		Pair<IPv4AddressTrie<ListenSocket>, BindableSocketSet<ProxySocket>> cfg = null;
		try
		{
			cfg = load(APPLICATION_DIRECTORY.resolve("serviceconfig.xml"));
		}
		catch (NoSuchFileException | FileNotFoundException e)
		{
			// ignore and load predefined
		}
		catch (Exception e)
		{
			LOG.error("User's service config", e);
		}
		
		if (cfg == null)
		{
			try
			{
				cfg = load(CONFIG_DIRECTORY.resolve("serviceconfig.xml"));
			}
			catch (Exception e)
			{
				throw new InternalError(e);
			}
		}
		
		_gameWorldHubSockets = cfg.getLeft();
		_authSockets = cfg.getRight();
	}
	
	private final Pair<IPv4AddressTrie<ListenSocket>, BindableSocketSet<ProxySocket>> load(Path file) throws IOException, ParserConfigurationException, SAXException, RuntimeException
	{
		final Node root = L2XMLUtils.childNamed(L2XMLUtils.getXMLFile(file), "sockets");
		final IPv4AddressTrie<ListenSocket> gwSockets = new IPv4AddressTrie<>();
		for (final Node sock : L2XMLUtils.listNodesByNodeName(L2XMLUtils.firstChildNamed(root, "gameWorldSockets"), "gameWorldSocket"))
		{
			final ListenSocket socket = new ListenSocket(InetAddress.getByName(L2XMLUtils.getString(sock, "ip")), L2XMLUtils.getInteger(sock, "port"));
			
			final String tmp = L2XMLUtils.getNodeAttributeStringValue(sock, "clientAddressPrefix", null);
			if (tmp == null)
			{
				gwSockets.setFallbackElement(socket);
				continue;
			}
			
			final Matcher cap = IPV4_ADDRESS_PREFIX.matcher(tmp);
			if (!cap.matches())
				throw new IllegalArgumentException(tmp);
				
			final byte[] addr = new byte[4];
			for (int i = 0; i < 4; ++i)
				addr[i] = (byte)Integer.parseInt(cap.group(i + 1));
			final IPv4AddressPrefix prefix = new IPv4AddressPrefix(addr, Integer.parseInt(cap.group(5)));
			gwSockets.put(prefix, socket);
		}
		final BindableSocketSet<ProxySocket> authSockets = new BindableSocketSet<>();
		for (final Node sock : L2XMLUtils.listNodesByNodeName(L2XMLUtils.firstChildNamed(root, "authorizationSockets"), "authorizationSocket"))
		{
			if (L2XMLUtils.getNodeAttributeBooleanValue(sock, "disabled", false))
				continue;
				
			final Node listen = L2XMLUtils.firstChildNamed(sock, "listen");
			final Node svc = L2XMLUtils.firstChildNamed(sock, "service");
			authSockets.add(new ProxySocket(InetAddress.getByName(L2XMLUtils.getString(listen, "ip")), L2XMLUtils.getInteger(listen, "port"), L2XMLUtils.getString(svc, "host"),
					L2XMLUtils.getInteger(svc, "port")));
		}
		return ImmutablePair.of(gwSockets, authSockets);
	}
	
	/**
	 * Sockets to listen on for game client connections.
	 * 
	 * @return game sockets
	 */
	public IPv4AddressTrie<ListenSocket> getGameWorldSockets()
	{
		return _gameWorldHubSockets;
	}
	
	/**
	 * Sockets to listen on for login client connections.
	 * 
	 * @return login sockets
	 */
	public BindableSocketSet<ProxySocket> getAuthSockets()
	{
		return _authSockets;
	}
	
	/**
	 * Returns a singleton object.
	 * 
	 * @return an instance of this class
	 */
	public static SocketManager getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static final class SingletonHolder
	{
		static final SocketManager INSTANCE = new SocketManager();
	}
}
