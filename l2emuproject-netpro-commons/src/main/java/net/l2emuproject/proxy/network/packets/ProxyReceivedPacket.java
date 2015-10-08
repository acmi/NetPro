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
package net.l2emuproject.proxy.network.packets;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import net.l2emuproject.network.mmocore.InvalidPacketException;
import net.l2emuproject.network.mmocore.MMOBuffer;
import net.l2emuproject.network.mmocore.ReceivablePacket;
import net.l2emuproject.proxy.network.ByteBufferUtils;
import net.l2emuproject.proxy.network.FloatingPacketManager;
import net.l2emuproject.proxy.network.Packet;
import net.l2emuproject.proxy.network.Proxy;
import net.l2emuproject.util.HexUtil;

/**
 * This class represents a received packet that may be modified and transmitted to the intended
 * target.
 * 
 * @author savormix
 */
public abstract class ProxyReceivedPacket extends ReceivablePacket<Proxy, ProxyReceivedPacket, ProxyRepeatedPacket>
{
	private final int _opcode;
	
	/**
	 * Creates a default received proxy packet that is ready for forwarding.
	 * 
	 * @param opcode
	 *            main opcode
	 */
	protected ProxyReceivedPacket(int opcode)
	{
		_opcode = opcode;
	}
	
	/**
	 * @see #read(MMOBuffer)
	 * @param buf
	 *            packet's body without the main opcode
	 * @param packet
	 *            modification controller
	 * @throws BufferUnderflowException
	 *             if packet does not match the expected format
	 * @throws RuntimeException
	 *             if a generic failure occurs while reading
	 */
	protected void readAndModify(MMOBuffer buf, Packet packet) throws BufferUnderflowException, RuntimeException
	{
		// do nothing by default
	}
	
	public final void readAndChangeState(Proxy proxy, MMOBuffer buf)
	{
		setClient2(proxy);
		readAndModify(buf, null);
	}
	
	/**
	 * Returns the internal proxy object that received this packet.
	 * 
	 * @return receiver
	 */
	protected Proxy getReceiver()
	{
		return getClient();
	}
	
	/**
	 * Returns the internal proxy object that will forward this packet to the intended recipient.
	 * 
	 * @return recipient
	 */
	protected Proxy getRecipient()
	{
		return getClient().getTarget();
	}
	
	@Override
	protected final void read(MMOBuffer buf) throws BufferUnderflowException, RuntimeException
	{
		// read the whole packet body into a dedicated buffer
		// since there's no access to the real buffer anyway
		final ByteBuffer immutable;
		{
			int bodySize = buf.getAvailableBytes();
			byte[] bytes = new byte[1 + bodySize]; // opcode + body
			bytes[0] = (byte)_opcode;
			buf.readB(bytes, 1, bodySize);
			immutable = ByteBufferUtils.asReadOnly(ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN));
		}
		
		// create a wrapper for modification control
		final Packet pack = new Packet(immutable);
		
		// apply internal modifications
		{
			// provide with a MMOBuffer that was passed here
			final MMOBuffer buf2 = new MMOBuffer();
			buf2.setByteBuffer(immutable);
			immutable.clear(); // reset position
			buf2.readUC(); // skip opcode
			
			try
			{
				readAndModify(buf2, pack);
			}
			catch (RuntimeException e)
			{
				LOG.error("Faulting packet: " + HexUtil.printData(immutable, 0, immutable.capacity()));
				throw e;
			}
		}
		
		// apply 3rd party modifications (scripts)
		getClient().notifyPacketArrived(pack);
		
		final Proxy recipient = getRecipient();
		FloatingPacketManager.getInstance().addPending(getClient(), pack, recipient);
	}
	
	@Override
	protected final void runImpl() throws InvalidPacketException, RuntimeException
	{
		// do nothing
	}
}
