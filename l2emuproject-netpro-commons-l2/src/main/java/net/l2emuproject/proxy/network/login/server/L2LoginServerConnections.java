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
package net.l2emuproject.proxy.network.login.server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SocketChannel;
import java.util.Collections;
import java.util.Set;

import net.l2emuproject.network.mmocore.MMOConfig;
import net.l2emuproject.proxy.network.AbstractL2ServerConnections;
import net.l2emuproject.proxy.network.BindableSocketSet;
import net.l2emuproject.proxy.network.IPv4AddressPrefix;
import net.l2emuproject.proxy.network.IPv4AddressTrie;
import net.l2emuproject.proxy.network.ListenSocket;
import net.l2emuproject.proxy.network.ProxySocket;
import net.l2emuproject.proxy.network.login.client.L2LoginClient;

/**
 * Manages outgoing connections to game servers initiated when a new L2 client connects to this proxy.
 * 
 * @author savormix
 */
public final class L2LoginServerConnections extends AbstractL2ServerConnections
{
	private static final class SingletonHolder
	{
		static
		{
			final MMOConfig cfg = new MMOConfig("LS Proxy");
			// this application might be run on user-class machines, so go easy on the CPU
			cfg.setReadWriteSelectorSleepTime(Integer.getInteger(L2LoginServerConnections.class.getName() + "#" + PROPERTY_RW_INTERVAL, 100));
			cfg.setThreadCount(1);
			
			try
			{
				INSTANCE = new L2LoginServerConnections(cfg);
			}
			catch (IOException e)
			{
				throw new Error(e);
			}
		}
		
		static final L2LoginServerConnections INSTANCE;
	}
	
	/**
	 * Returns a singleton object.
	 * 
	 * @return an instance of this class
	 */
	public static final L2LoginServerConnections getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private IPv4AddressTrie<ListenSocket> _advertisedSockets;
	private Set<ProxySocket> _sockets;
	
	L2LoginServerConnections(MMOConfig config) throws IOException
	{
		super(config, L2LoginServerPackets.getInstance());
		
		_advertisedSockets = new IPv4AddressTrie<>(Collections.singletonMap(new IPv4AddressPrefix(0, 0), new ListenSocket(InetAddress.getLocalHost(), 7776)));
		_sockets = Collections.singleton(new ProxySocket(ListenSocket.WILDCARD, 2106, "127.0.0.1", 2107));
	}
	
	/**
	 * Attempts to complete the given connection by creating a new connection to the login server and then binding it with the given endpoint.
	 * 
	 * @param client actual connection initiator
	 * @param socketChannel client's socket
	 */
	public void connect(L2LoginClient client, SocketChannel socketChannel)
	{
		if (client == null)
			return;
		
		InetSocketAddress receiverAddress = null;
		try
		{
			receiverAddress = (InetSocketAddress)socketChannel.getLocalAddress();
		}
		catch (IOException e)
		{
			client.closeNow();
			return;
		}
		
		final ListenSocket receiverWrapper = new ListenSocket(receiverAddress.getAddress(), receiverAddress.getPort());
		ProxySocket socket = null;
		for (final ProxySocket ps : _sockets)
		{
			if (ps.bindingEquals(receiverWrapper))
			{
				socket = ps;
				break;
			}
		}
		
		if (socket == null)
		{
			LOG.error("Unexpected connection to " + receiverAddress);
			client.closeNow();
			return;
		}
		
		try
		{
			connectProxy(client, InetAddress.getByName(socket.getServiceAddress()), socket.getServicePort());
		}
		catch (Exception e) // UnknownHostException, RuntimeException, TooManyPendingConnectionsException
		{
			LOG.error("Cannot connect to " + socket.getServiceAddress() + ":" + socket.getServicePort(), e);
			client.closeNow();
		}
	}
	
	@Override
	protected L2LoginServer createClientImpl(SocketChannel socketChannel) throws ClosedChannelException
	{
		final L2LoginClient llc = takeClient();
		
		return new L2LoginServer(this, socketChannel, llc);
	}
	
	/**
	 * Changes the proxy's IP &amp; port advertised in the (game) <TT>ServerList</TT> packet.
	 * 
	 * @param advertisedSockets sockets on which proxy is listening
	 */
	public void setAdvertisedSockets(IPv4AddressTrie<ListenSocket> advertisedSockets)
	{
		_advertisedSockets = advertisedSockets;
	}
	
	/**
	 * Returns the IP address and port to be injected for the given client.
	 * 
	 * @param client client endpoint
	 * @return advertised socket
	 */
	public ListenSocket getAdvertisedSocket(L2LoginClient client)
	{
		return _advertisedSockets.get(client.getInetAddress());
	}
	
	/**
	 * Sets sockets with a fixed service endpoint.
	 * 
	 * @param sockets proxied sockets
	 */
	public void setAuthSockets(BindableSocketSet<ProxySocket> sockets)
	{
		_sockets = sockets;
	}
	
	@Override
	public int getBlockingImmutablePacketProcessingWarnThreshold()
	{
		return 20;
	}
	
	@Override
	public int getBlockingMutablePacketProcessingWarnThreshold()
	{
		return 50;
	}
}
