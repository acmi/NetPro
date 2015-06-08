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
package net.l2emuproject.proxy.ui.savormix.io;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.mutable.MutableInt;

import net.l2emuproject.proxy.ui.ReceivedPacket;
import net.l2emuproject.proxy.ui.savormix.io.base.NewIOHelper;

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
	
	private final Map<Integer, MutableInt> _cp;
	private final Map<Integer, MutableInt> _sp;
	
	private final ByteBuffer _buffer;
	
	private int _total;
	
	/**
	 * Creates a packet counter.
	 * 
	 * @param writer an associated log file writer
	 */
	public PacketLog(NewIOHelper writer)
	{
		_writer = writer;
		
		_cp = new HashMap<>();
		_sp = new HashMap<>();
		
		_buffer = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN);
		
		_total = 0;
	}
	
	void onPacket(ReceivedPacket packet)
	{
		++_total;
		
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
}
