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
import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Iterator;

import net.l2emuproject.network.mmocore.MMOBuffer;
import net.l2emuproject.proxy.io.NewIOHelper;
import net.l2emuproject.proxy.io.exception.LogFileIterationIOException;
import net.l2emuproject.proxy.network.EndpointType;
import net.l2emuproject.proxy.network.game.client.L2GameClient;
import net.l2emuproject.proxy.network.game.client.L2GameClientPackets;
import net.l2emuproject.proxy.network.game.server.L2GameServer;
import net.l2emuproject.proxy.network.game.server.L2GameServerPackets;

/**
 * Allows convenient PacketSamurai/YAL packet log reading.
 * 
 * @author _dev_
 */
public class PSPacketLogFileIterator implements Iterator<PSLogFilePacket>, AutoCloseable, Closeable
{
	private final PSLogFileHeader _logFileMetadata;
	private final NewIOHelper _input;
	
	private final L2GameClient _fakeClient;
	private final L2GameServer _fakeServer;
	private final MMOBuffer _buf;
	
	PSPacketLogFileIterator(PSLogFileHeader logFileMetadata) throws IOException
	{
		_logFileMetadata = logFileMetadata;
		_input = new NewIOHelper(Files.newByteChannel(logFileMetadata.getLogFile(), StandardOpenOption.READ));
		
		// move to first packet
		_input.setPositionInChannel(logFileMetadata.getHeaderSize());
		
		_fakeServer = new L2GameServer(null, null, _fakeClient = new L2GameClient(null, null));
		_buf = new MMOBuffer();
	}
	
	@Override
	public boolean hasNext() throws LogFileIterationIOException
	{
		try
		{
			_input.fetchUntilAvailable(1/* + 2 + 8*/);
			return true;
		}
		catch (final EOFException e)
		{
			return false;
		}
		catch (final IOException e)
		{
			throw new LogFileIterationIOException(e);
		}
	}
	
	@Override
	public PSLogFilePacket next() throws LogFileIterationIOException
	{
		try
		{
			while (true)
			{
				final EndpointType type = EndpointType.valueOf(!_input.readBoolean());
				final byte[] body = new byte[_input.readChar() - 2];
				final long time = _input.readLong();
				_input.read(body);
				
				if (_logFileMetadata.isEnciphered())
				{
					final ByteBuffer wrapper = ByteBuffer.wrap(body).order(ByteOrder.LITTLE_ENDIAN);
					_buf.setByteBuffer(wrapper);
					if (type.isClient())
					{
						_fakeClient.decipher(wrapper);
						_fakeClient.setFirstTime(false);
						L2GameClientPackets.getInstance().handlePacket(wrapper, _fakeClient, _buf.readUC()).readAndChangeState(_fakeClient, _buf);
					}
					else
					{
						_fakeServer.decipher(wrapper);
						L2GameServerPackets.getInstance().handlePacket(wrapper, _fakeServer, _buf.readUC()).readAndChangeState(_fakeServer, _buf);
					}
				}
				
				return new PSLogFilePacket(type, body, time);
			}
		}
		catch (final IOException e)
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
