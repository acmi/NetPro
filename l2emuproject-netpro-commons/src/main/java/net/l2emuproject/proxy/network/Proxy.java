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

import static net.l2emuproject.proxy.network.EndpointType.CLIENT;
import static net.l2emuproject.proxy.network.EndpointType.SERVER;

import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SocketChannel;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import net.l2emuproject.network.IProtocolVersion;
import net.l2emuproject.network.mmocore.MMOConnection;
import net.l2emuproject.network.mmocore.MMOLogger;
import net.l2emuproject.proxy.network.listener.ConnectionListener;
import net.l2emuproject.proxy.network.listener.PacketListener;
import net.l2emuproject.proxy.network.listener.PacketManipulator;
import net.l2emuproject.proxy.network.packets.IPacketSource;
import net.l2emuproject.proxy.network.packets.ProxyReceivedPacket;
import net.l2emuproject.proxy.network.packets.ProxyRepeatedPacket;
import net.l2emuproject.proxy.state.entity.cache.EntityInfoCache;
import net.l2emuproject.proxy.state.entity.context.ServerSocketID;
import net.l2emuproject.util.concurrent.RunnableStatsManager;
import net.l2emuproject.util.logging.L2Logger;

/**
 * This class represents a connection between this proxy and another entity. It also provides an
 * in-built connection/packet event notification system. <BR>
 * <BR>
 * Each <TT>Proxy</TT> object acquires a target <TT>Proxy</TT> as soon as the connection to the
 * desired entity is established. <I>Generally, a client connects to the proxy, then the proxy
 * connects to a server. That means we can pass the client's <TT>Proxy</TT> object when creating the
 * server's and set the client's target after it's created.</I> <BR>
 * <BR>
 * A proxy may operate in two modes:
 * <OL>
 * <LI>Blind forwarding</LI>
 * <LI>Resending filtered packets</LI>
 * </OL>
 * The first mode can be easily achieved by leaving <TT>enchipher</TT> and <TT>decipher</TT> methods
 * empty as well as using the generic packet handler.<BR>
 * However, the proxy becomes a powerful tool if it deciphers incoming packets, possibly handles (or
 * modifies) them and resends them (or not) to the intended target. <BR>
 * <BR>
 * In order to save the proxy core or it's implementation uncluttered, interested parties should use
 * listeners
 * 
 * @author savormix
 */
public abstract class Proxy extends MMOConnection<Proxy, ProxyReceivedPacket, ProxyRepeatedPacket>implements IPacketSource
{
	/** Bundles log entries and logs them asynchronously each second. */
	protected static final MMOLogger LOG = new MMOLogger(Proxy.class, 1_000);
	
	private final ProxyConnections _mmoController;
	private final AtomicBoolean _failed, _disconnected;
	
	private Proxy _target;
	
	private IProtocolVersion _version;
	
	/**
	 * Creates an internal object that represents a connection to this proxy.
	 * 
	 * @param mmoController connection manager
	 * @param socketChannel connection
	 * @param target internal object that represents an outgoing connection
	 * @throws ClosedChannelException if the given channel was closed during operations
	 */
	protected Proxy(ProxyConnections mmoController, SocketChannel socketChannel, Proxy target) throws ClosedChannelException
	{
		super(mmoController, socketChannel);
		
		_mmoController = mmoController;
		_failed = new AtomicBoolean();
		_disconnected = new AtomicBoolean();
		
		if (target != null)
		{
			setTarget(target);
			target.setTarget(this);
		}
		
		if (_mmoController != null && getType().isServer())
			EntityInfoCache.addSharedContext(new ServerSocketID(getInetSocketAddress()));
	}
	
	/** Called when a connection to the other endpoint fails. */
	public void notifyFailure()
	{
		if (_failed.compareAndSet(false, true))
			closeNow();
	}
	
	/**
	 * Returns whether this connection could not be completed.
	 * 
	 * @return whether this connection has failed
	 */
	public boolean isFailed()
	{
		return _failed.get();
	}
	
	/**
	 * Returns whether this connection has been terminated.
	 * 
	 * @return whether this connection is inactive
	 */
	public boolean isDced()
	{
		return _disconnected.get();
	}
	
	@Override
	protected ProxyRepeatedPacket getDefaultClosePacket()
	{
		return null;
	}
	
	@Override
	protected final void onDisconnection()
	{
		if (!_disconnected.compareAndSet(false, true))
			return;
			
		onDisconnectionImpl();
		
		if (getType() == EndpointType.SERVER)
			EntityInfoCache.removeSharedContext(new ServerSocketID(getInetSocketAddress()));
			
		if (_target != null && !_target.isDced())
		{
			_target.weakClose();
			return;
		}
		
		final Proxy client, server;
		if (getType() == EndpointType.SERVER)
		{
			client = _target;
			server = this;
		}
		else
		{
			client = this;
			server = _target;
		}
		
		ForwardedNotificationManager.getInstance().addDisconnectionNotification(new AsyncDisconnectionNotifier(client, server));
	}
	
	/**
	 * Called on every disconnection.
	 * 
	 * @see #onDisconnection()
	 */
	protected abstract void onDisconnectionImpl();
	
	/**
	 * Returns the type of this connection.
	 * 
	 * @return endpoint type
	 */
	@Override
	public abstract EndpointType getType();
	
	/**
	 * Specifies where this proxy should send all packets received from this endpoint.
	 * 
	 * @param target forwarding target
	 */
	protected final void setTarget(Proxy target)
	{
		if (target != null && target.getType() == getType())
			throw new IllegalArgumentException("Invalid binding");
			
		_target = target;
	}
	
	/**
	 * Returns where all packets received from this endpoint will be forwarded.
	 * 
	 * @return forwarding target
	 */
	public final Proxy getTarget()
	{
		return _target;
	}
	
	/**
	 * Returns the client endpoint associated with this connection.
	 * 
	 * @return client endpoint
	 */
	public final Proxy getClient()
	{
		return getType() == CLIENT ? this : getTarget();
	}
	
	/**
	 * Returns the server endpoint associated with this connection.
	 * 
	 * @return server endpoint
	 */
	public final Proxy getServer()
	{
		return getType() == SERVER ? this : getTarget();
	}
	
	/**
	 * Returns the network protocol version associated with this endpoint.
	 * 
	 * @return protocol version
	 */
	public IProtocolVersion getProtocol()
	{
		return _version;
	}
	
	/**
	 * Changes the network protocol version associated with this endpoint.
	 * 
	 * @param version protocol version
	 */
	protected void setVersion(IProtocolVersion version)
	{
		_version = version;
		
		notifyProtocolVersion(getProtocol());
	}
	
	/**
	 * Notifies packet listeners about a received packet and requests modifications.
	 * 
	 * @param packet packet's body
	 */
	public final void notifyPacketArrived(final Packet packet)
	{
		for (PacketManipulator pm : getPacketManipulators())
		{
			try
			{
				final ByteBuffer ref = packet.getForwardedBody();
				ref.clear();
				packet.getReceivedBody().clear();
				{
					final long start = System.nanoTime();
					pm.packetArrived(this, getTarget(), packet);
					final long end = System.nanoTime();
					RunnableStatsManager.handleStats(pm.getClass(), "packetArrived(Proxy, Proxy, Packet)", end - start, packet.getForwardedBody() == ref
							? getMmoController().getBlockingImmutablePacketProcessingWarnThreshold() : getMmoController().getBlockingMutablePacketProcessingWarnThreshold());
				}
			}
			catch (RuntimeException e)
			{
				LOG.error(pm, e);
			}
		}
	}
	
	/**
	 * Notifies packet listeners about a forwarded packet in a separate thread.
	 * 
	 * @param received received body
	 * @param sent sent body
	 * @param time reception time
	 */
	public final void notifyPacketForwarded(final ByteBuffer received, final ByteBuffer sent, long time)
	{
		ForwardedNotificationManager.getInstance().addPacketNotification(this, getTarget(), received, sent, time, getPacketListeners(), getPacketManipulators());
	}
	
	// Allows NP to deal with servers that don't know how to send packets. Trust me, YOU DON'T WANT TO KNOW.
	@SuppressWarnings("javadoc")
	public abstract boolean ___supportsAheadOfTimeIntervention();
	
	private final void notifyProtocolVersion(IProtocolVersion version)
	{
		if (_mmoController == null)
			return;
			
		for (PacketListener pl : getPacketListeners())
		{
			final long start = System.nanoTime();
			pl.onProtocolVersion(this, version);
			final long end = System.nanoTime();
			RunnableStatsManager.handleStats(pl.getClass(), "onProtocolVersion(Proxy, IProtocolVersion)", end - start, 1);
		}
		for (PacketManipulator pm : getPacketManipulators())
		{
			final long start = System.nanoTime();
			pm.onProtocolVersion(this, version);
			final long end = System.nanoTime();
			RunnableStatsManager.handleStats(pm.getClass(), "onProtocolVersion(Proxy, IProtocolVersion)", end - start, 1);
		}
	}
	
	Set<ConnectionListener> getConnectionListeners()
	{
		return getMmoController().getConnectionListeners();
	}
	
	private Set<PacketListener> getPacketListeners()
	{
		return getMmoController().getPacketListeners();
	}
	
	private Set<PacketManipulator> getPacketManipulators()
	{
		return getMmoController().getPacketManipulators();
	}
	
	private ProxyConnections getMmoController()
	{
		return _mmoController;
	}
	
	/** Notifies listeners about a complete disconnection (after both client and server have terminated the connection). */
	public static final class AsyncDisconnectionNotifier implements Runnable
	{
		private static final L2Logger LOG = L2Logger.getLogger(Proxy.AsyncDisconnectionNotifier.class);
		private final Proxy _client, _server;
		
		AsyncDisconnectionNotifier(Proxy client, Proxy server)
		{
			// not a lambda due to RSM
			_client = client;
			_server = server;
		}
		
		@Override
		public void run()
		{
			Stream<ConnectionListener> allListeners = Stream.empty();
			if (_client != null)
				allListeners = Stream.concat(allListeners, _client.getConnectionListeners().stream());
			if (_server != null)
				allListeners = Stream.concat(allListeners, _server.getConnectionListeners().stream());
			final Set<ConnectionListener> uniqueListeners = allListeners.collect(Collectors.toSet());
			for (final ConnectionListener listener : uniqueListeners)
			{
				try
				{
					listener.onDisconnection(_client, _server);
				}
				catch (RuntimeException e)
				{
					LOG.error(listener, e);
				}
			}
		}
		
		/**
		 * Returns the client endpoint.
		 * 
		 * @return client socket wrapper
		 */
		public Proxy getClient()
		{
			return _client;
		}
	}
}
