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
package net.l2emuproject.proxy.io;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;

import net.l2emuproject.proxy.io.exception.LogFileIterationIOException;
import net.l2emuproject.proxy.network.EndpointType;
import net.l2emuproject.proxy.ui.savormix.io.LoggedPacketFlag;
import net.l2emuproject.proxy.ui.savormix.io.base.NewIOHelper;
import net.l2emuproject.util.BitMaskUtils;

/**
 * Allows convenient NetPro packet log reading.
 * 
 * @author _dev_
 */
public class NetProPacketLogFileIterator implements Iterator<LogFilePacket>, AutoCloseable, Closeable
{
	private static final int MINIMAL_PACKET_LENGTH_IN_LOG_FILE = 1 + 2 + 1 + 8;
	private static final int MINIMAL_PACKET_LENGTH_IN_LOG_FILE_V7 = 1 + 2 + 1 + 8 + 1;
	
	private final LogFileHeader _logFileMetadata;
	private final NewIOHelper _input;
	
	NetProPacketLogFileIterator(LogFileHeader logFileMetadata) throws IOException
	{
		_logFileMetadata = logFileMetadata;
		
		_input = new NewIOHelper(Files.newByteChannel(logFileMetadata.getLogFile(), StandardOpenOption.READ));
		
		// move to first packet
		_input.setPositionInChannel(logFileMetadata.getHeaderSize());
	}
	
	@Override
	public boolean hasNext() throws LogFileIterationIOException
	{
		try
		{
			return _input.getPositionInChannel(false) + (_logFileMetadata.getVersion() >= 7 ? MINIMAL_PACKET_LENGTH_IN_LOG_FILE_V7 : MINIMAL_PACKET_LENGTH_IN_LOG_FILE) <= _logFileMetadata
					.getFooterStart();
		}
		catch (IOException e)
		{
			throw new LogFileIterationIOException(e);
		}
	}
	
	@Override
	public LogFilePacket next() throws LogFileIterationIOException
	{
		try
		{
			while (true)
			{
				final EndpointType type = EndpointType.valueOf(_input.readBoolean());
				final byte[] body = new byte[_input.readChar()];
				_input.read(body);
				final long time = _input.readLong();
				final Set<LoggedPacketFlag> flags = _logFileMetadata.getVersion() >= 7 ? BitMaskUtils.setOf(_input.readByte(), LoggedPacketFlag.class) : Collections.emptySet();
				
				return new LogFilePacket(type, body, time, flags);
			}
		}
		catch (IOException e)
		{
			throw new LogFileIterationIOException(e);
		}
	}
	
	@Override
	public void close() throws IOException
	{
		_input.close();
	}
}
