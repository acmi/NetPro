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
package net.l2emuproject.proxy.network.login.server.packets;

import java.nio.BufferUnderflowException;
import java.util.concurrent.TimeUnit;

import net.l2emuproject.network.mmocore.MMOBuffer;
import net.l2emuproject.proxy.network.Packet;
import net.l2emuproject.proxy.network.game.L2SessionManager;
import net.l2emuproject.proxy.network.game.NewGameServerConnection;
import net.l2emuproject.proxy.network.login.client.L2LoginClient;
import net.l2emuproject.proxy.network.packets.ProxyRepeatedPacket;
import net.l2emuproject.util.concurrent.L2ThreadPool;

/**
 * This packet indicates that a L2 client will now connect on the previously advertised port.
 * 
 * @author savormix
 */
public final class PlayOk extends L2LoginServerPacket
{
	/** Packet's identifier */
	public static final int OPCODE = 0x07;
	
	/**
	 * Constructs a packet to know that the client will now connect on a different port
	 */
	public PlayOk()
	{
		super(OPCODE);
	}
	
	@Override
	protected void readAndModify(MMOBuffer buf, Packet packet) throws BufferUnderflowException, RuntimeException
	{
		final L2LoginClient client = getRecipient();
		final NewGameServerConnection authorizedSession = client.getSelectedServer();
		if (L2SessionManager.getInstance().setAuthorizedSession(authorizedSession))
		{
			LOG.info("Active AuthD session: " + authorizedSession);
			return;
		}
		
		final byte[] thisPacket = packet.getDefaultBufferForModifications().array();
		packet.demandLoss(null);
		L2ThreadPool.schedule(() -> {
			if (client.isDced())
				return;
			if (L2SessionManager.getInstance().setAuthorizedSession(authorizedSession))
			{
				client.sendPacket(new ProxyRepeatedPacket(thisPacket));
				LOG.info("Active AuthD session: " + authorizedSession + " (async)");
				return;
			}
			L2ThreadPool.schedule(this, 10, TimeUnit.MILLISECONDS);
		}, 10, TimeUnit.MILLISECONDS);
	}
	
	@Override
	protected int getMinimumLength()
	{
		// return READ_D + READ_D;
		return 0; // content is not used
	}
}
