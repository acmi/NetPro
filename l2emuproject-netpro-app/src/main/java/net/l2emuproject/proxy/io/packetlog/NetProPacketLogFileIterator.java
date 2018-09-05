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

import java.io.Closeable;
import java.io.IOException;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

import net.l2emuproject.proxy.io.NewIOHelper;
import net.l2emuproject.proxy.io.exception.LogFileIterationIOException;
import net.l2emuproject.proxy.network.EndpointType;
import net.l2emuproject.util.BitMaskUtils;

/**
 * Allows convenient NetPro packet log reading.
 * 
 * @author _dev_
 */
public class NetProPacketLogFileIterator implements Iterator<LogFilePacket>, AutoCloseable, Closeable
{
	// private static final L2Logger LOG = L2Logger.getLogger(NetProPacketLogFileIterator.class);
	
	private static final int MINIMAL_PACKET_LENGTH_IN_LOG_FILE = 1 + 2 + 1 + 8;
	private static final int MINIMAL_PACKET_LENGTH_IN_LOG_FILE_V7 = 1 + 2 + 1 + 8 + 1;
	
	private static final int DEFLATED_BUFFER_SIZE = 8192;
	private static final byte[] OMITTED = { 0, 0, (byte)0xFF, (byte)0xFF };
	
	private final LogFileHeader _logFileMetadata;
	private final NewIOHelper _input;
	
	private Inflater _inflater;
	private ByteBuffer _inflated;
	private byte[] _deflated;
	private int _remainingInDeflatedBlock;
	
	NetProPacketLogFileIterator(LogFileHeader logFileMetadata) throws IOException
	{
		_logFileMetadata = logFileMetadata;
		
		_input = new NewIOHelper(Files.newByteChannel(logFileMetadata.getLogFile(), StandardOpenOption.READ));
		// move to first packet
		_input.setPositionInChannel(logFileMetadata.getHeaderSize());
	}
	
	NetProPacketLogFileIterator(NewIOHelper input, int version, long footerStart, int compressionType, Path logFile) throws IOException
	{
		_logFileMetadata = new LogFileHeader(logFile, -1L, version, -1, -1, footerStart, -1L, null, -1, Collections.emptySet(), compressionType, -1, Collections.emptyMap(), Collections.emptyMap(),
				-1L);
		_input = input;
	}
	
	@Override
	public boolean hasNext() throws LogFileIterationIOException
	{
		try
		{
			switch (_logFileMetadata.getCompressionType())
			{
				case 0:
					return _input.getPositionInChannel(false) + (_logFileMetadata.getVersion() >= 7 ? MINIMAL_PACKET_LENGTH_IN_LOG_FILE_V7 : MINIMAL_PACKET_LENGTH_IN_LOG_FILE) <= _logFileMetadata
							.getFooterStart();
				case 1:
					initC1();
					// LOG.info("hasNext: compressed[" + _remainingInDeflatedBlock + "] uncompressed[" + (_inflated != null ? _inflated.remaining() : 0) + "]");
					return _remainingInDeflatedBlock != -1 || _inflated.remaining() >= MINIMAL_PACKET_LENGTH_IN_LOG_FILE;
				default:
					return false;
			}
		}
		catch (final IOException e)
		{
			throw new LogFileIterationIOException(_logFileMetadata.getLogFile().getFileName().toString(), e);
		}
	}
	
	@Override
	public LogFilePacket next() throws LogFileIterationIOException
	{
		switch (_logFileMetadata.getCompressionType())
		{
			case 0:
				try
				{
					final EndpointType type = EndpointType.valueOf(_input.readBoolean());
					final byte[] body = new byte[_input.readChar()];
					_input.read(body);
					final long time = _input.readLong();
					final Set<LoggedPacketFlag> flags = _logFileMetadata.getVersion() >= 7 ? BitMaskUtils.setOf(_input.readByte(), LoggedPacketFlag.class) : Collections.emptySet();
					return new LogFilePacket(type, body, time, flags);
				}
				catch (final IOException e)
				{
					throw new LogFileIterationIOException(_logFileMetadata.getLogFile().getFileName().toString(), e);
				}
			case 1:
				try
				{
					initC1();
					final EndpointType type = EndpointType.valueOf(fetch(1).get() != 0);
					final byte[] body = new byte[fetch(2).getChar()];
					fetch(body.length).get(body);
					final long time = fetch(8).getLong();
					final Set<LoggedPacketFlag> flags = _logFileMetadata.getVersion() >= 7 ? BitMaskUtils.setOf(fetch(1).get(), LoggedPacketFlag.class) : Collections.emptySet();
					return new LogFilePacket(type, body, time, flags);
				}
				catch (final IOException e)
				{
					throw new LogFileIterationIOException(_logFileMetadata.getLogFile().getFileName().toString(), e);
				}
				catch (final DataFormatException e)
				{
					throw new LogFileIterationIOException(_logFileMetadata.getLogFile().getFileName().toString(), e);
				}
			default:
				throw new UnsupportedOperationException("Compression type " + _logFileMetadata.getCompressionType() + " is not supported");
		}
	}
	
	private void initC1() throws IOException
	{
		if (_inflater != null)
			return;
		
		_inflater = new Inflater(true);
		_inflated = ByteBuffer.allocate(1 << 16).order(ByteOrder.LITTLE_ENDIAN);
		_inflated.limit(0);
		_deflated = new byte[DEFLATED_BUFFER_SIZE];
		_remainingInDeflatedBlock = _input.readInt();
		// LOG.debug("[INIT] First block size: " + _remainingInDeflatedBlock);
	}
	
	private ByteBuffer fetch(int bytes) throws IOException, DataFormatException
	{
		if (_inflated.capacity() < bytes)
			throw new BufferOverflowException();
		if (_inflated.remaining() < bytes)
		{
			if (_remainingInDeflatedBlock == -1)
				return null;
			
			_inflated.compact();
			while (_inflated.position() < bytes)
			{
				if (!_inflater.needsInput())
				{
					final int inflatedSize = _inflater.inflate(_inflated.array(), _inflated.position(), _inflated.remaining());
					// LOG.debug("[LOOP] Inflated " + inflatedSize + " bytes");
					_inflated.position(_inflated.position() + inflatedSize);
					continue;
				}
				
				int length = Math.min(_remainingInDeflatedBlock, _deflated.length - OMITTED.length);
				_input.read(_deflated, 0, length);
				_remainingInDeflatedBlock -= length;
				// LOG.debug("Read " + length + " compressed bytes");
				if (_remainingInDeflatedBlock == 0)
				{
					// LOG.debug("[LOOP] Block end: omitted buffer added");
					System.arraycopy(OMITTED, 0, _deflated, length, OMITTED.length);
					length += OMITTED.length;
					_remainingInDeflatedBlock = _input.readInt();
					// LOG.debug("[LOOP] Next block size: " + _remainingInDeflatedBlock);
				}
				_inflater.setInput(_deflated, 0, length);
				// LOG.debug("[LOOP] Fed " + length + " bytes to Inflater");
			}
			_inflated.flip();
		}
		return _inflated;
	}
	
	@Override
	public void close() throws IOException
	{
		_input.close();
	}
	
	@Override
	public String toString()
	{
		return _logFileMetadata.toString();
	}
}
