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
package net.l2emuproject.proxy.network.meta.container;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.ArrayUtils;

import net.l2emuproject.proxy.network.meta.IPacketTemplate;

/**
 * Resolves packets that use the standard 3-op scheme (1, 2, 4). Provides zero-allocation* resolution schemes for packets presented both as byte arrays and byte buffers
 * for maximal throughput and convenience.<BR>
 * <BR>
 * * – zero-allocation is not guaranteed for non-predefined (unknown) packets
 * 
 * @author _dev_
 */
final class OpcodeResolution implements IPacketResolver
{
	private final int _opcodeIndex;
	private final Map<Integer, IPacketResolver> _delegates;
	
	OpcodeResolution(int opcodeIndex)
	{
		_opcodeIndex = opcodeIndex;
		_delegates = new ConcurrentHashMap<>();
	}
	
	/**
	 * Registers a packet template as a candidate for opcode resolutions.
	 * 
	 * @param packet a packet template
	 * @throws IllegalArgumentException if the given template cannot be registered
	 */
	public void registerPacket(IPacketTemplate packet) throws IllegalArgumentException
	{
		Integer primaryOpcode = readOpcode(packet.getPrefix());
		/*
		Integer primaryOpcode = null;
		try
		{
			primaryOpcode = readOpcode(packet.getPrefix());
		}
		catch (BufferUnderflowException e)
		{
			// this is a dirty trick to deal with legacy double-op packets
			try
			{
				final Field f = OpcodeResolution.class.getDeclaredField("_opcodeIndex");
				if (!f.isAccessible())
					f.setAccessible(true); // permanently
				f.set(this, _opcodeIndex - 1);
				
				primaryOpcode = readOpcode(packet.getPrefix());
			}
			catch (Exception e2)
			{
				throw new IllegalArgumentException("Cannot register: " + packet, e);
			}
		}
		*/
		if (isResolvedAtThisLevel(packet))
		{
			final IPacketResolver old = _delegates.get(primaryOpcode);
			if (old == null)
			{
				_delegates.putIfAbsent(primaryOpcode, new PacketResolution(packet));
				return;
			}
			
			if (!(old instanceof PacketResolution))
				throw new IllegalArgumentException("Already registered as extended: " + packet);
			
			throw new IllegalArgumentException("Already registered " + packet + " as " + ((PacketResolution)old).resolve(ArrayUtils.EMPTY_BYTE_ARRAY));
		}
		
		IPacketResolver delegate = _delegates.get(primaryOpcode);
		if (delegate == null)
		{
			delegate = new OpcodeResolution(_opcodeIndex + 1);
			_delegates.putIfAbsent(primaryOpcode, delegate);
		}
		
		if (!(delegate instanceof OpcodeResolution))
			throw new IllegalArgumentException("Already registered as extensionless: " + packet);
		
		final OpcodeResolution pr = (OpcodeResolution)delegate;
		pr.registerPacket(packet);
	}
	
	@Override
	public IPacketTemplate resolve(byte[] packet, int offset, int length) throws BufferUnderflowException
	{
		final int opcode = readOpcode(packet, offset, length);
		final IPacketResolver resolution = _delegates.get(opcode);
		if (resolution == null)
			return null;
		
		return resolution.resolve(packet, offset, length);
	}
	
	@Override
	public IPacketTemplate resolve(ByteBuffer packet) throws BufferUnderflowException
	{
		final int opcode = readOpcode(packet);
		final IPacketResolver resolution = _delegates.get(opcode);
		if (resolution == null)
			return null;
		
		return resolution.resolve(packet);
	}
	
	private int getMinPacketLength()
	{
		switch (_opcodeIndex)
		{
			case 0:
				return 1;
			case 1:
				return 3;
			default:
				return 3 + ((_opcodeIndex - 1) << 2);
		}
	}
	
	boolean isResolvedAtThisLevel(IPacketTemplate packet)
	{
		return packet.getPrefix().length <= getMinPacketLength();
	}
	
	private int readOpcode(byte[] buffer) throws BufferUnderflowException
	{
		return readOpcode(buffer, 0, buffer.length);
	}
	
	private int readOpcode(byte[] buffer, int packetOffset, int packetLength) throws BufferUnderflowException
	{
		if (packetLength < getMinPacketLength())
			throw new BufferUnderflowException();
		
		switch (_opcodeIndex)
		{
			case 0:
				return buffer[packetOffset] & 0xFF;
			case 1:
				return buffer[packetOffset + 1] & 0xFF | ((buffer[packetOffset + 2] & 0xFF) << 8);
			default:
				final int off = packetOffset + getMinPacketLength() - 4;
				return buffer[off] & 0xFF | ((buffer[off + 1] & 0xFF) << 8) | ((buffer[off + 2] & 0xFF) << 16) | ((buffer[off + 3] & 0xFF) << 24);
		}
	}
	
	private int readOpcode(ByteBuffer buffer) throws BufferUnderflowException
	{
		switch (_opcodeIndex)
		{
			case 0:
				return buffer.get() & 0xFF;
			case 1:
				return buffer.getChar();
			default:
				return buffer.getInt();
		}
	}
	
	int getTruncationPoint(byte[] buffer, int packetOffset, int packetLength) throws BufferUnderflowException, IllegalArgumentException
	{
		final Integer op = readOpcode(buffer, packetOffset, packetLength);
		final IPacketResolver pr = _delegates.get(op);
		if (pr == null)
			return getMinPacketLength();
		
		if (pr instanceof OpcodeResolution)
			return ((OpcodeResolution)pr).getTruncationPoint(buffer, packetOffset, packetLength);
		
		throw new IllegalArgumentException("'Unknown' packet is in fact " + ((PacketResolution)pr).resolve(ArrayUtils.EMPTY_BYTE_ARRAY));
	}
	
	int getTruncationPoint(ByteBuffer buffer) throws BufferUnderflowException, IllegalArgumentException
	{
		final Integer op = readOpcode(buffer);
		final IPacketResolver pr = _delegates.get(op);
		if (pr == null)
			return getMinPacketLength();
		
		if (pr instanceof OpcodeResolution)
			return ((OpcodeResolution)pr).getTruncationPoint(buffer);
		
		throw new IllegalArgumentException("'Unknown' packet is in fact " + ((PacketResolution)pr).resolve(ArrayUtils.EMPTY_BYTE_ARRAY));
	}
	
	int getOpcodeIndex()
	{
		return _opcodeIndex;
	}
	
	Map<Integer, IPacketResolver> getDelegates()
	{
		return _delegates;
	}
	
	@Override
	public String toString()
	{
		return "[" + _opcodeIndex + "] -> " + _delegates;
	}
}
