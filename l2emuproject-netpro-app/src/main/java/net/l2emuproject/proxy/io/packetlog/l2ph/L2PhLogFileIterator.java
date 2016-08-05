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
package net.l2emuproject.proxy.io.packetlog.l2ph;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Iterator;

import net.l2emuproject.proxy.io.exception.LogFileIterationIOException;
import net.l2emuproject.proxy.network.EndpointType;
import net.l2emuproject.proxy.network.ServiceType;
import net.l2emuproject.util.HexUtil;

/**
 * Allows convenient NetPro packet log reading.
 * 
 * @author _dev_
 */
public class L2PhLogFileIterator implements Iterator<L2PhLogFilePacket>, AutoCloseable, Closeable
{
	//private final L2PhLogFileHeader _logFileMetadata;
	private final BufferedReader _input;
	private final ByteBuffer _timeBuffer;
	private byte[] _nextPacket;
	
	L2PhLogFileIterator(L2PhLogFileHeader logFileMetadata) throws IOException
	{
		//_logFileMetadata = logFileMetadata;
		_input = Files.newBufferedReader(logFileMetadata.getLogFile(), StandardCharsets.US_ASCII);
		_timeBuffer = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN);
		_nextPacket = null;
	}
	
	@Override
	public boolean hasNext() throws LogFileIterationIOException
	{
		try
		{
			if (_nextPacket != null)
				return true;
			
			final String line = _input.readLine();
			if (line == null)
				return false;
			
			_nextPacket = HexUtil.hexStringToBytes(line);
			return true;
		}
		catch (final IOException e)
		{
			throw new LogFileIterationIOException(e);
		}
	}
	
	@Override
	public L2PhLogFilePacket next() throws LogFileIterationIOException
	{
		hasNext(); // prepare _nextPacket
		
		final ServiceType service = L2PhLogFileUtils.toServiceType(_nextPacket[0]);
		final EndpointType endpoint = EndpointType.valueOf((_nextPacket[0] & 1) == 0);
		_timeBuffer.put(_nextPacket, 1, _timeBuffer.remaining()).position(0);
		final long time = L2PhLogFileUtils.toUNIX(_timeBuffer.getLong(0));
		final byte[] content = Arrays.copyOfRange(_nextPacket, 11, _nextPacket.length);
		_nextPacket = null;
		return new L2PhLogFilePacket(service, endpoint, time, content);
	}
	
	@Override
	public void close() throws IOException
	{
		_input.close();
	}
}
