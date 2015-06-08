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
package net.l2emuproject.proxy.network;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;

import javolution.util.FastMap;

import net.l2emuproject.network.mmocore.MMOConfig;
import net.l2emuproject.network.mmocore.ThreadWrapper;
import net.l2emuproject.proxy.network.exception.TooManyPendingConnectionsException;
import net.l2emuproject.proxy.network.listener.ConnectionListener;
import net.l2emuproject.proxy.network.packets.ProxyRepeatedPacket;

/**
 * This class manages connections between the underlying proxy server and an existing Lineage II service.
 * 
 * @author NB4L1 (original)
 * @author _dev_ (alternate implementation)
 */
public abstract class AbstractL2ServerConnections extends ProxyConnections
{
	static final byte[] SILENT_FAIL = { 0x01, 23 };
	// linked to retain priority
	private final FastMap<AbstractL2ClientProxy, ThreadWrapper> _clients;
	
	/**
	 * Creates a L2 connection manager.
	 * 
	 * @param config MMO networking configuration
	 * @param packetHandler received packet handler
	 * @throws IOException if the manager could not be set up
	 */
	protected AbstractL2ServerConnections(MMOConfig config, ProxyPacketHandler packetHandler) throws IOException
	{
		super(config, packetHandler);
		
		_clients = new FastMap<AbstractL2ClientProxy, ThreadWrapper>().setShared(true);
	}
	
	/**
	 * Binds this connection manager to the connection manager for the other connection endpoint.
	 * 
	 * @param clientConnections client connection manager
	 * @return {@code this}
	 */
	public AbstractL2ServerConnections registerBindingListeners(AbstractL2ClientConnections clientConnections)
	{
		clientConnections.addConnectionListener(new SwiftEliminator());
		return this;
	}
	
	FastMap<AbstractL2ClientProxy, ThreadWrapper> getClients()
	{
		return _clients;
	}
	
	/**
	 * Connects to a server on behalf of <TT>client</TT>.
	 * 
	 * @param <T> type of client
	 * @param client connection initiator
	 * @param address server's address
	 * @param port server's port
	 * @throws TooManyPendingConnectionsException if a new connection must be dropped as there are plenty of unfinished ones
	 */
	protected final <T extends AbstractL2ClientProxy> void connectProxy(final T client, InetAddress address, int port) throws TooManyPendingConnectionsException
	{
		final InetSocketAddress destination = new InetSocketAddress(address, port);
		if (getClients().size() > 9)
			throw new TooManyPendingConnectionsException(client);
		
		// five seconds, otherwise the server will most likely be unplayable anyway
		final ThreadWrapper connector = connect(destination, 5_000, () ->
		{
			// force to throw exception to detect defects early
			getClients().remove(client);
			client.close(new ProxyRepeatedPacket(SILENT_FAIL));
		});
		getClients().put(client, connector);
	}
	
	/**
	 * Retrieves a client that is waiting until this proxy will connect to a server.
	 * 
	 * @param <T> type of client
	 * @return a connected client
	 */
	@SuppressWarnings("unchecked")
	protected final <T extends AbstractL2ClientProxy> T takeClient()
	{
		T result = null;
		while (result == null && !getClients().isEmpty())
		{
			result = (T)getClients().head().getNext().getKey();
			if (getClients().remove(result) == null)
				result = null;
		}
		return result;
	}
	
	private final class SwiftEliminator implements ConnectionListener
	{
		SwiftEliminator()
		{
		}
		
		@Override
		public void onClientConnection(Proxy client)
		{
			// do nothing
		}
		
		@Override
		public void onServerConnection(Proxy server)
		{
			// do nothing
		}
		
		@Override
		public void onDisconnection(Proxy client, Proxy server)
		{
			final ThreadWrapper tw = getClients().remove(client);
			if (tw != null)
				tw.interrupt();
		}
	}
}
