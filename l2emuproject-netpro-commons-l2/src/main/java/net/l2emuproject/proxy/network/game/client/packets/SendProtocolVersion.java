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
package net.l2emuproject.proxy.network.game.client.packets;

import java.nio.BufferUnderflowException;

import net.l2emuproject.network.mmocore.MMOBuffer;
import net.l2emuproject.network.protocol.IGameProtocolVersion;
import net.l2emuproject.network.protocol.ProtocolVersionManager;
import net.l2emuproject.proxy.network.Packet;

/**
 * This packet contains the protocol version that L2 client will use for communications.<BR>
 * 
 * @author savormix
 */
public final class SendProtocolVersion extends L2GameClientPacket
{
	/** Packet's identifier */
	public static final int OPCODE = 0x0E;
	/** Packet's legacy identifier */
	public static final int OPCODE_LEGACY = 0x00;
	
	/**
	 * Constructs a packet to extract the protocol version to be used
	 * 
	 * @param opcode opcode to use when forwarding to server
	 */
	public SendProtocolVersion(int opcode)
	{
		super(opcode);
	}
	
	@Override
	protected void readAndModify(MMOBuffer buf, Packet packet) throws BufferUnderflowException, RuntimeException
	{
		// unfortunately, we cannot use PPE here; we don't know which protocol version is used :P
		final int version = buf.readD();
		final IGameProtocolVersion cpv = ProtocolVersionManager.getInstance().getGameProtocol(version);
		if (cpv != null)
			getReceiver().setVersion(cpv);
		else
			LOG.warn("Client will use unsupported " + version + " protocol, defaulting to " + getReceiver().getProtocol());
	}
	
	@Override
	protected int getMinimumLength()
	{
		return READ_D;
	}
}
