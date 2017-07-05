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
package net.l2emuproject.proxy.io.packetlog.ps;

import java.io.Closeable;
import java.io.IOException;
import java.util.Iterator;

import net.l2emuproject.proxy.io.exception.LogFileIterationIOException;
import net.l2emuproject.proxy.network.game.client.L2GameClient;
import net.l2emuproject.proxy.network.game.server.L2GameServer;

/**
 * Allows convenient PacketSamurai/YAL packet log reading.
 * 
 * @author _dev_
 */
public class PSPacketLogFileIterator implements Iterator<PSLogFilePacket>, AutoCloseable, Closeable
{
	private final PSLogFileHeader _logFileMetadata;
	private final boolean _unshuffleOpcodes;
	private final L2GameServer _fakeServer;
	
	private int _delegateIndex;
	private PSPacketLogPartIterator _delegate;
	
	PSPacketLogFileIterator(PSLogFileHeader logFileMetadata, boolean unshuffleOpcodes) throws IOException
	{
		_logFileMetadata = logFileMetadata;
		_unshuffleOpcodes = unshuffleOpcodes;
		
		_delegate = new PSPacketLogPartIterator(logFileMetadata.getParts().get(_delegateIndex = 0), unshuffleOpcodes, _fakeServer = new L2GameServer(null, null, new L2GameClient(null, null)));
	}
	
	@Override
	public boolean hasNext() throws LogFileIterationIOException
	{
		while (_delegateIndex < _logFileMetadata.getParts().size())
		{
			final boolean result = _delegate.hasNext();
			if (result)
				return true;
			
			try
			{
				_delegate.close();
			}
			catch (final IOException e)
			{
				// whatever
			}
			
			if (++_delegateIndex >= _logFileMetadata.getParts().size())
				return false;
			
			try
			{
				_delegate = new PSPacketLogPartIterator(_logFileMetadata.getParts().get(_delegateIndex), _unshuffleOpcodes, _fakeServer);
			}
			catch (final IOException e)
			{
				throw new LogFileIterationIOException(_logFileMetadata.getParts().get(_delegateIndex).getLogFile().getFileName().toString(), e);
			}
		}
		return false;
	}
	
	@Override
	public PSLogFilePacket next() throws LogFileIterationIOException
	{
		hasNext();
		return _delegate.next();
	}
	
	@Override
	public void close() throws IOException
	{
		_delegate.close();
	}
	
	@Override
	public String toString()
	{
		return _logFileMetadata.toString();
	}
}
