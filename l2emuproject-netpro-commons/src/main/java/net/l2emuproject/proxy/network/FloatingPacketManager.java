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

import static net.l2emuproject.network.protocol.LoginProtocolVersion.MODERN;

import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import javolution.util.FastMap;

import net.l2emuproject.lang.L2TextBuilder;
import net.l2emuproject.network.mmocore.MMOLogger;
import net.l2emuproject.network.protocol.ILoginProtocolVersion;
import net.l2emuproject.network.protocol.IProtocolVersion;
import net.l2emuproject.network.security.LoginCipher;
import net.l2emuproject.proxy.network.packets.ProxyRepeatedPacket;
import net.l2emuproject.util.concurrent.L2ThreadPool;

/**
 * Handles packets sent by one side before a connection to the endpoint is established.<BR>
 * <BR>
 * This manager guarantees that all packets are sent in order.
 * 
 * @author savormix
 */
public class FloatingPacketManager
{
	static final MMOLogger LOG = new MMOLogger(FloatingPacketManager.class, 1000);
	
	final FastMap<Proxy, Queue<Packet>> _pending;
	
	FloatingPacketManager()
	{
		final FastMap<Proxy, Queue<Packet>> pending = FastMap.newInstance();
		pending.setShared(true);
		_pending = pending;
		
		L2ThreadPool.scheduleAtFixedRate(new Cleanup(), 30_000, 60_000);
		LOG.info("Initialized.");
	}
	
	/**
	 * Attempts to send {@code packet} from {@code proxy} to {@code recipient}. If {@code recipient} is {@code null}, the passed {@code packet}
	 * will be cached until the connection is actually established.
	 * 
	 * @param proxy packet sender
	 * @param packet a packet
	 * @param recipient packet recipient
	 */
	public void addPending(Proxy proxy, Packet packet, Proxy recipient)
	{
		if (recipient != null)
		{
			firePending(proxy, recipient);
			firePacket(packet, proxy, recipient);
			return;
		}
		
		Queue<Packet> pending = _pending.get(proxy);
		if (pending == null)
		{
			final Queue<Packet> newQ = new ConcurrentLinkedQueue<>();
			pending = _pending.putIfAbsent(proxy, newQ);
			if (pending == null)
				pending = newQ;
		}
		pending.add(packet);
	}
	
	/**
	 * Fires all packets that have not yet been sent from {@code key} to {@code recipient} (if there are any).
	 * 
	 * @param key connection initiator
	 * @param recipient connection target
	 */
	public void firePending(Proxy key, Proxy recipient)
	{
		final Queue<Packet> pending = _pending.remove(key);
		if (pending == null)
			return;
		
		while (!pending.isEmpty())
		{
			final Packet pack = pending.poll();
			firePacket(pack, key, recipient);
		}
	}
	
	@SuppressWarnings("static-method")
	private void firePacket(Packet pack, Proxy key, Proxy recipient)
	{
		final IProtocolVersion protocol = recipient.getProtocol();
		if (!pack.isLossForced()) // check whether to forward or not
		{
			ByteBuffer body = pack.getForwardedBody(); // retrieve possibly changed body
			recipient.sendPacket(new ProxyRepeatedPacket(body));
			// make checksum visible to notifications
			if (key.___supportsAheadOfTimeIntervention() && protocol instanceof ILoginProtocolVersion && (body.capacity() & 3) == 0) // except legacy unenciphered
			{
				body = ByteBufferUtils.asMutable(body);
				body.clear();
				LoginCipher.injectChecksum(body, recipient.getType().isClient() || protocol.isOlderThan(MODERN) ? 8 : 16);
				body = ByteBufferUtils.asReadOnly(body);
			}
			key.notifyPacketForwarded(pack.getReceivedBody(), body, pack.getReceptionTime()); // immutable buffers
		}
		else
			// not forwarded
			key.notifyPacketForwarded(pack.getReceivedBody(), null, pack.getReceptionTime());
		
		for (ByteBuffer body : pack.getAdditionalPackets()) // send additional
		{
			recipient.sendPacket(new ProxyRepeatedPacket(body));
			if (key.___supportsAheadOfTimeIntervention() && protocol instanceof ILoginProtocolVersion && (body.capacity() & 3) == 0) // except legacy unenciphered
			{
				// make checksum visible to notifications
				body = ByteBufferUtils.asMutable(body);
				body.clear();
				LoginCipher.injectChecksum(body, recipient.getType().isClient() || protocol.isOlderThan(MODERN) ? 8 : 16);
				body = ByteBufferUtils.asReadOnly(body);
			}
			key.notifyPacketForwarded(null, body, pack.getReceptionTime()); // immutable
		}
	}
	
	/**
	 * Returns a singleton instance of this type.
	 * 
	 * @return an instance of this class
	 */
	public static final FloatingPacketManager getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static final class SingletonHolder
	{
		static final FloatingPacketManager INSTANCE = new FloatingPacketManager();
	}
	
	final class Cleanup implements Runnable
	{
		@Override
		public void run()
		{
			// TODO: is this safe?
			for (Iterator<Entry<Proxy, Queue<Packet>>> it = _pending.entrySet().iterator(); it.hasNext();)
			{
				final Entry<Proxy, Queue<Packet>> e = it.next();
				final Proxy p = e.getKey();
				if (p.isDced() || p.isFailed())
				{
					final Queue<Packet> q = e.getValue();
					it.remove();
					
					final L2TextBuilder sb = new L2TextBuilder("Removed ");
					sb.append(q.size()).append(" packets on unfinished connection from ").append(p.getHostAddress());
					LOG.info(sb);
				}
			}
		}
	}
}
