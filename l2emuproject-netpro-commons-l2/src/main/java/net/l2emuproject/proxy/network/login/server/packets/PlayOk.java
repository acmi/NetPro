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

import net.l2emuproject.network.mmocore.MMOBuffer;
import net.l2emuproject.proxy.network.Packet;
import net.l2emuproject.proxy.network.game.L2SessionManager;

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
		// must be done before this packet is forwarded
		L2SessionManager.getInstance().addRoute(getRecipient());
	}
	
	@Override
	protected int getMinimumLength()
	{
		// return READ_D + READ_D;
		return 0; // content is not used
	}
}
