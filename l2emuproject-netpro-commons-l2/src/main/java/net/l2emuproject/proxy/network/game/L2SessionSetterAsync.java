/*
 * Copyright 2011-2016 L2EMU UNIQUE
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
package net.l2emuproject.proxy.network.game;

import java.nio.ByteBuffer;
import java.util.concurrent.TimeUnit;

import net.l2emuproject.proxy.network.AbstractL2ClientProxy;
import net.l2emuproject.proxy.network.packets.ProxyRepeatedPacket;
import net.l2emuproject.util.Rnd;
import net.l2emuproject.util.concurrent.L2ThreadPool;
import net.l2emuproject.util.logging.L2Logger;

/**
 * If multiple clients are about to be handed over to game server(s), this ensures they are only allowed to initiate connection to NP once NP is ready to hand each of them over.
 * 
 * @author _dev_
 */
public final class L2SessionSetterAsync implements Runnable
{
	private static final long RETRY_INTERVAL_MILLIS_MIN = 10, RETRY_INTERVAL_MILLIS_MAX = 50;
	
	private static final L2Logger LOG = L2Logger.getLogger(L2SessionSetterAsync.class);
	
	private final AbstractL2ClientProxy _client;
	private final NewGameServerConnection _authorizedSession;
	private final ByteBuffer _originalPacket, _sentPacket;
	
	/**
	 * Constructs an executable task.
	 * 
	 * @param client client requesting handover
	 * @param authorizedSession session details
	 * @param originalPacket packet to send to client when clear (before NP changes)
	 * @param sentPacket packet to send to client when clear (after NP changes)
	 */
	public L2SessionSetterAsync(AbstractL2ClientProxy client, NewGameServerConnection authorizedSession, ByteBuffer originalPacket, ByteBuffer sentPacket)
	{
		_client = client;
		_authorizedSession = authorizedSession;
		_originalPacket = originalPacket;
		_sentPacket = sentPacket;
	}
	
	@Override
	public void run()
	{
		if (_client.isDced())
		{
			LOG.info("Discarded session: " + _authorizedSession + " (async)");
			return;
		}
		if (L2SessionManager.getInstance().setAuthorizedSession(_authorizedSession))
		{
			_client.sendPacket(new ProxyRepeatedPacket(_sentPacket));
			_client.notifyPacketForwarded(_originalPacket, _sentPacket, System.currentTimeMillis());
			LOG.info("Active session: " + _authorizedSession + " (async)");
			return;
		}
		L2ThreadPool.schedule(this, Rnd.get(RETRY_INTERVAL_MILLIS_MIN, RETRY_INTERVAL_MILLIS_MAX), TimeUnit.MILLISECONDS);
	}
}
