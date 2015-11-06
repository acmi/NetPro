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

import java.nio.ByteBuffer;
import java.util.Arrays;

import org.apache.commons.lang3.ArrayUtils;

import eu.revengineer.simplejse.type.UnloadableScript;

import net.l2emuproject.network.mmocore.MMOBuffer;
import net.l2emuproject.network.protocol.IProtocolVersion;
import net.l2emuproject.proxy.network.Packet;
import net.l2emuproject.proxy.network.Proxy;
import net.l2emuproject.proxy.network.listener.ConnectionListener;
import net.l2emuproject.proxy.network.listener.PacketManipulator;
import net.l2emuproject.proxy.ui.savormix.loader.LoadOption;
import net.l2emuproject.util.ObjectPool;
import net.l2emuproject.util.logging.L2Logger;

/**
 * A customizable script.<BR>
 * <BR>
 * A script is shared between all active connections, therefore methods may be invoked in different
 * contexts. Depending on each <TT>MMOController</TT>'s configuration, some or all script methods
 * may be invoked concurrently.
 * 
 * @param <C> internal type representing a client
 * @param <S> internal type representing a server
 * @author savormix
 */
public abstract class Script<C extends Proxy, S extends Proxy> implements ConnectionListener, PacketManipulator, UnloadableScript
{
	/** Default logger for scripts. */
	protected static final L2Logger LOG = L2Logger.getLogger(Script.class);
	/** Opcode of all packets without a body. */
	protected static final int EMPTY_PACKET_OPCODE = -1;
	/** Indicates that all packets should be processed by this script, regardless of opcodes. */
	protected static final int[] WILDCARD = {};
	
	private final int[] _handledClient;
	private final int[] _handledServer;
	
	private volatile boolean _enabled;
	
	/**
	 * Constructs this script.
	 * 
	 * @param handledClient handled main client opcodes
	 * @param handledServer handled main server opcodes
	 */
	protected Script(int[] handledClient, int[] handledServer)
	{
		_handledClient = handledClient != null ? handledClient : ArrayUtils.EMPTY_INT_ARRAY;
		_handledServer = handledServer != null ? handledServer : ArrayUtils.EMPTY_INT_ARRAY;
		
		Arrays.sort(_handledClient);
		Arrays.sort(_handledServer);
		
		_enabled = false;
	}
	
	@Override
	public final String getName()
	{
		final StringBuilder sb = new StringBuilder(getScriptName());
		sb.append(' ').append(getVersionString()).append(" by ").append(getAuthor());
		return sb.toString();
	}
	
	@Override
	public void onLoad() throws RuntimeException
	{
		ScriptManager.getInstance().addScript(this);
		setEnabled(LoadOption.DISABLE_SCRIPTS.isNotSet());
	}
	
	@Override
	public void onUnload() throws RuntimeException
	{
		// TODO: real unload?
		setEnabled(false);
	}
	
	/**
	 * Returns an user-friendly name of this script.
	 * 
	 * @return script name
	 */
	public abstract String getScriptName();
	
	/**
	 * Returns an user-friendly author name.
	 * 
	 * @return author name
	 */
	public abstract String getAuthor();
	
	/**
	 * Returns an user-friendly string that identifies which protocol versions are supported.
	 * 
	 * @return supported protocols
	 */
	public abstract String getVersionString();
	
	/**
	 * Returns a class to identify sender's type.
	 * 
	 * @return client's class
	 */
	protected abstract Class<C> getClientClass();
	
	/**
	 * Perform general setup for this script (register as a listener). <BR>
	 * <BR>
	 * This method is called a single time after an instance is created.
	 */
	public abstract void setUp();
	
	/**
	 * Undo general setup for this script (unregister as a listener). <BR>
	 * <BR>
	 * This method is called a single time after an instance is created, by default when a new
	 * version of the script is loaded.
	 */
	public abstract void tearDown();
	
	/**
	 * General client packet manipulation.
	 * 
	 * @param sender internal object that represents a client
	 * @param recipient internal object that represents a server
	 * @param packet received packet
	 * @throws RuntimeException if something went wrong or a manipulator conflict
	 * @see PacketManipulator#packetArrived(Proxy, Proxy, Packet)
	 */
	protected void clientPacketArrived(C sender, S recipient, Packet packet) throws RuntimeException
	{
		// do nothing by default
	}
	
	/**
	 * General server packet manipulation.
	 * 
	 * @param sender internal object that represents a server
	 * @param recipient internal object that represents a client
	 * @param packet received packet
	 * @throws RuntimeException if something went wrong or a manipulator conflict
	 * @see PacketManipulator#packetArrived(Proxy, Proxy, Packet)
	 */
	protected void serverPacketArrived(S sender, C recipient, Packet packet) throws RuntimeException
	{
		// do nothing by default
	}
	
	/**
	 * Sent client packet review.
	 * 
	 * @param sender internal object that represents a client
	 * @param recipient internal object that represents a server
	 * @param received original packet's body
	 * @param sent body after all modifications
	 * @see PacketManipulator#packetForwarded(Proxy, Proxy, ByteBuffer, ByteBuffer)
	 * @throws RuntimeException if something went wrong
	 */
	protected void clientPacketForwarded(C sender, S recipient, ByteBuffer received, ByteBuffer sent)
	{
		// by default, call the simplified version
		if (received == null)
			return;
		
		final WrapperPool wp = ScriptManager.getInstance().getWrapperPool();
		final MMOBuffer buf = wp.get();
		try
		{
			buf.setByteBuffer(received);
			clientPacketForwarded(sender, buf);
		}
		finally
		{
			wp.store(buf);
		}
	}
	
	/**
	 * Sent server packet review.
	 * 
	 * @param sender internal object that represents a server
	 * @param recipient internal object that represents a client
	 * @param received original packet's body
	 * @param sent body after all modifications
	 * @see PacketManipulator#packetForwarded(Proxy, Proxy, ByteBuffer, ByteBuffer)
	 * @throws RuntimeException if something went wrong
	 */
	protected void serverPacketForwarded(S sender, C recipient, ByteBuffer received, ByteBuffer sent)
	{
		// by default, call the simplified version
		if (received == null)
			return;
		
		final WrapperPool wp = ScriptManager.getInstance().getWrapperPool();
		final MMOBuffer buf = wp.get();
		try
		{
			buf.setByteBuffer(received);
			serverPacketForwarded(sender, buf);
		}
		finally
		{
			wp.store(buf);
		}
	}
	
	/**
	 * A simplified version of {@link #clientPacketForwarded(Proxy, Proxy, ByteBuffer, ByteBuffer)}.
	 * 
	 * @param client client that sent a packet
	 * @param buf unmodified client's packet
	 */
	protected void clientPacketForwarded(C client, MMOBuffer buf)
	{
		// do nothing by default
	}
	
	/**
	 * A simplified version of {@link #serverPacketForwarded(Proxy, Proxy, ByteBuffer, ByteBuffer)}.
	 * 
	 * @param server server that sent a packet
	 * @param buf unmodified server's packet
	 */
	protected void serverPacketForwarded(S server, MMOBuffer buf)
	{
		// do nothing by default
	}
	
	/**
	 * Cleanup after a connection is no longer used.
	 * 
	 * @param client internal object that represents a client
	 * @param server internal object that represents a server
	 */
	protected void connectionTerminated(C client, S server)
	{
		// do nothing by default
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public final void packetArrived(Proxy sender, Proxy recipient, Packet packet)
	{
		if (!isHandled(sender, packet.getReceivedBody()))
			return;
		
		if (getClientClass().isInstance(sender))
			clientPacketArrived((C)sender, (S)recipient, packet);
		else
			serverPacketArrived((S)sender, (C)recipient, packet);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public final void packetForwarded(Proxy sender, Proxy recipient, ByteBuffer received, ByteBuffer sent)
	{
		{
			final ByteBuffer body;
			if (sent == null)
				body = received;
			else
				body = sent;
			
			if (!isHandled(sender, body))
				return;
		}
		
		if (getClientClass().isInstance(sender))
			clientPacketForwarded((C)sender, (S)recipient, received, sent);
		else
			serverPacketForwarded((S)sender, (C)recipient, received, sent);
	}
	
	@Override
	public final void onClientConnection(Proxy client)
	{
		// ignore
	}
	
	@Override
	public final void onServerConnection(Proxy server)
	{
		// ignore
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public final void onDisconnection(Proxy client, Proxy server)
	{
		connectionTerminated((C)client, (S)server);
	}
	
	@Override
	public final void onProtocolVersion(Proxy affected, IProtocolVersion version)
	{
		// do nothing
	}
	
	private boolean isHandled(Proxy sender, ByteBuffer body)
	{
		if (!_enabled)
			return false;
		
		final int[] handledOpcodes = getClientClass().isInstance(sender) ? _handledClient : _handledServer;
		if (handledOpcodes == WILDCARD)
			return true;
		
		final int opcode = body.capacity() > 0 ? body.get(0) & 0xFF : EMPTY_PACKET_OPCODE;
		return Arrays.binarySearch(handledOpcodes, opcode) >= 0;
	}
	
	/**
	 * Enables or disables this script.
	 * 
	 * @param enabled whether the script should be enabled
	 */
	public void setEnabled(boolean enabled)
	{
		_enabled = enabled;
	}
	
	@Override
	public final String toString()
	{
		return getName();
	}
	
	/**
	 * A pool that allocates and recycles buffer wrappers.
	 * 
	 * @author savormix
	 */
	public static class WrapperPool extends ObjectPool<MMOBuffer>
	{
		WrapperPool()
		{
			super(true);
		}
		
		@Override
		protected MMOBuffer create()
		{
			return new MMOBuffer();
		}
		
		@Override
		protected void reset(MMOBuffer buf)
		{
			buf.setByteBuffer(null);
		}
	}
}
