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
package net.l2emuproject.proxy.io.packetlog;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.Deflater;

import org.apache.commons.lang3.mutable.MutableInt;

import net.l2emuproject.proxy.io.NewIOHelper;
import net.l2emuproject.proxy.ui.ReceivedPacket;

/**
 * Allows tracking which packets have been logged in a log file.
 * 
 * @author savormix
 */
public class PacketLog
{
	private static final byte EXT_CLIENT = (byte)0xD0;
	private static final byte EXT_SERVER = (byte)0xFE;
	
	private final NewIOHelper _writer;
	private final Deflater _deflater;
	private final ByteBuffer _deflateBuffer;
	private final byte[] _outputBuffer;
	
	private final Map<Integer, MutableInt> _cp;
	private final Map<Integer, MutableInt> _sp;
	
	private final ByteBuffer _buffer;
	
	private int _total;
	private long _totalPacketBytes;
	
	/**
	 * Creates a packet log metadata tracker.
	 * 
	 * @param writer an associated log file writer
	 * @param deflater deflate implementation
	 */
	public PacketLog(NewIOHelper writer, Deflater deflater)
	{
		_writer = writer;
		_deflater = deflater;
		_deflateBuffer = _deflater != null ? ByteBuffer.allocate((1 + 2 + (1 << 16) - 1 + 8 + 1) << 1).order(ByteOrder.LITTLE_ENDIAN) : null;
		_outputBuffer = _deflater != null ? new byte[8192] : null;
		
		_cp = new HashMap<>();
		_sp = new HashMap<>();
		
		_buffer = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN);
		
		_total = 0;
		_totalPacketBytes = 0L;
	}
	
	void onPacket(ReceivedPacket packet)
	{
		++_total;
		_totalPacketBytes += packet.getBody().length;
		
		_buffer.putInt(0, 0);
		
		final Map<Integer, MutableInt> counter;
		final int ext;
		
		final byte[] body = packet.getBody();
		if (packet.getEndpoint().isClient())
		{
			counter = _cp;
			ext = EXT_CLIENT;
		}
		else
		{
			counter = _sp;
			ext = EXT_SERVER;
		}
		
		_buffer.put(body, 0, body[0] == ext ? 3 : 1).clear();
		
		final Integer key = _buffer.getInt(0);
		MutableInt count = counter.get(key);
		if (count == null)
		{
			count = new MutableInt();
			counter.put(key, count);
		}
		count.increment();
	}
	
	NewIOHelper getWriter()
	{
		return _writer;
	}
	
	Deflater getDeflater()
	{
		return _deflater;
	}
	
	ByteBuffer getDeflateBuffer()
	{
		return _deflateBuffer;
	}
	
	byte[] getOutputBuffer()
	{
		return _outputBuffer;
	}
	
	Map<Integer, MutableInt> getCp()
	{
		return _cp;
	}
	
	Map<Integer, MutableInt> getSp()
	{
		return _sp;
	}
	
	int getTotal()
	{
		return _total;
	}
	
	long getTotalPacketBytes()
	{
		return _totalPacketBytes;
	}
}
