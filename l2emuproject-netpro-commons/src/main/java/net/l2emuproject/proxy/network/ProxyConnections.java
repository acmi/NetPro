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
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SocketChannel;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import net.l2emuproject.network.mmocore.FloodManager;
import net.l2emuproject.network.mmocore.MMOConfig;
import net.l2emuproject.network.mmocore.MMOController;
import net.l2emuproject.proxy.network.listener.ConnectionListener;
import net.l2emuproject.proxy.network.listener.PacketListener;
import net.l2emuproject.proxy.network.listener.PacketManipulator;
import net.l2emuproject.proxy.network.packets.ProxyReceivedPacket;
import net.l2emuproject.proxy.network.packets.ProxyRepeatedPacket;

/**
 * This class manages connections between the underlying proxy server and a specific endpoint (client/server). It also provides a built-in
 * connection/packet event notification system.
 * 
 * @author savormix
 */
public abstract class ProxyConnections extends MMOController<Proxy, ProxyReceivedPacket, ProxyRepeatedPacket>
{
	/** System propery used to set the I/O interval */
	public static final String PROPERTY_RW_INTERVAL = "ReadWriteSelectorSleepTime";
	
	private final Set<ConnectionListener> _connectionListeners = new CopyOnWriteArraySet<>();
	private final Set<PacketListener> _packetListeners = new CopyOnWriteArraySet<>();
	private final Set<PacketManipulator> _packetManipulators = new CopyOnWriteArraySet<>();
	
	/**
	 * Creates a proxy connection manager.
	 * 
	 * @param config
	 *            MMO networking configuration
	 * @param packetHandler
	 *            received packet handler
	 * @throws IOException
	 *             if the manager could not be set up
	 */
	protected ProxyConnections(MMOConfig config, ProxyPacketHandler packetHandler) throws IOException
	{
		super(config, packetHandler);
	}
	
	/**
	 * Returns an object that represents the given connection.
	 * 
	 * @param socketChannel
	 *            connection
	 * @return an internal object that represents the connection
	 * @throws ClosedChannelException
	 *             if the given channel was closed during operations
	 */
	protected abstract Proxy createClientImpl(SocketChannel socketChannel) throws ClosedChannelException;
	
	/**
	 * Returns the maximum amount of time a single entity may take reading a packet, while blocking an I/O thread, without receiving an infraction.
	 * 
	 * @return maximal amount of time to evade warning
	 */
	public abstract int getBlockingImmutablePacketProcessingWarnThreshold();
	
	/**
	 * Returns the maximum amount of time a single entity may take rewriting a packet, while blocking an I/O thread, without receiving an infraction.
	 * 
	 * @return maximal amount of time to evade warning
	 */
	public abstract int getBlockingMutablePacketProcessingWarnThreshold();
	
	@Override
	protected final Proxy createClient(SocketChannel socketChannel) throws ClosedChannelException
	{
		final Proxy p = createClientImpl(socketChannel);
		if (p.isFailed())
			return p;
		
		ForwardedNotificationManager.getInstance().addConnectionNotification(new AsyncConnectionNotifier(p, _connectionListeners));
		final Proxy otherEndpoint = p.getTarget();
		if (otherEndpoint != null) // in this case, otherEndpoint is client
			FloatingPacketManager.getInstance().firePending(otherEndpoint, p);
		
		return p;
	}
	
	/**
	 * Adds a connection event listener.
	 * 
	 * @param listener
	 *            connection listener
	 */
	public final void addConnectionListener(ConnectionListener listener)
	{
		_connectionListeners.add(listener);
	}
	
	/**
	 * Removes a connection event listener.
	 * 
	 * @param listener
	 *            connection listener
	 */
	public final void removeConnectionListener(ConnectionListener listener)
	{
		_connectionListeners.remove(listener);
	}
	
	/**
	 * Adds a packet event listener.
	 * 
	 * @param listener
	 *            packet listener
	 */
	public final void addPacketListener(PacketListener listener)
	{
		_packetListeners.add(listener);
	}
	
	/**
	 * Removes a packet event listener.
	 * 
	 * @param listener
	 *            packet listener
	 */
	public final void removePacketListener(PacketListener listener)
	{
		_packetListeners.remove(listener);
	}
	
	/**
	 * Adds a packet event listener.
	 * 
	 * @param listener
	 *            packet listener
	 */
	public final void addPacketListener(PacketManipulator listener)
	{
		_packetManipulators.add(listener);
	}
	
	/**
	 * Adds a packet event listener.
	 * 
	 * @param listener
	 *            packet listener
	 */
	public final void removePacketListener(PacketManipulator listener)
	{
		_packetManipulators.remove(listener);
	}
	
	/**
	 * Returns registered connection event listeners.
	 * 
	 * @return connection listeners
	 */
	final Set<ConnectionListener> getConnectionListeners()
	{
		return _connectionListeners;
	}
	
	/**
	 * Returns registered packet event listeners.
	 * 
	 * @return packet listeners
	 */
	final Set<PacketListener> getPacketListeners()
	{
		return _packetListeners;
	}
	
	/**
	 * Returns registered packet manipulating listeners.
	 * 
	 * @return packet manipulators
	 */
	final Set<PacketManipulator> getPacketManipulators()
	{
		return _packetManipulators;
	}
	
	@Override
	protected FloodManager initAcceptsFloodManager()
	{
		return FloodManager.EMPTY_FLOOD_MANAGER;
	}
	
	@Override
	protected FloodManager initPacketsFloodManager()
	{
		return FloodManager.EMPTY_FLOOD_MANAGER;
	}
	
	@Override
	protected FloodManager initErrorsFloodManager()
	{
		return FloodManager.EMPTY_FLOOD_MANAGER;
	}
	
	/** Notifies connection listeners about a new connection, that may be made by a client or on behalf of a client. */
	public static final class AsyncConnectionNotifier implements Runnable
	{
		private final Proxy _endpoint;
		private final Set<ConnectionListener> _listeners;
		
		AsyncConnectionNotifier(Proxy endpoint, Set<ConnectionListener> listeners)
		{
			_endpoint = endpoint;
			_listeners = listeners;
		}
		
		@Override
		public void run()
		{
			if (_endpoint.getType().isClient())
			{
				for (final ConnectionListener listener : _listeners)
					listener.onClientConnection(_endpoint);
			}
			else
			{
				for (final ConnectionListener listener : _listeners)
					listener.onServerConnection(_endpoint);
			}
		}
		
		/**
		 * Returns the associated socket wrapper.
		 * 
		 * @return proxy connection endpoint
		 */
		public Proxy getEndpoint()
		{
			return _endpoint;
		}
	}
}
