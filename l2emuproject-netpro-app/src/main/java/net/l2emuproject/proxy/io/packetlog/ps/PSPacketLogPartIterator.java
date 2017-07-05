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

import net.l2emuproject.io.EmptyChecksum;
import net.l2emuproject.network.mmocore.MMOBuffer;
import net.l2emuproject.proxy.io.IOConstants;
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
public class PSPacketLogPartIterator implements Iterator<PSLogFilePacket>, AutoCloseable, Closeable, IOConstants
{
	private final PSLogPartHeader _logFileMetadata;
	private final NewIOHelper _input;
	private final boolean _unshuffleOpcodes;
	
	private final L2GameClient _fakeClient;
	private final L2GameServer _fakeServer;
	private final ByteBuffer _copyVsNew;
	private final MMOBuffer _buf;
	
	PSPacketLogPartIterator(PSLogPartHeader logFileMetadata, boolean unshuffleOpcodes, L2GameServer fakeServer) throws IOException
	{
		_logFileMetadata = logFileMetadata;
		_input = new NewIOHelper(Files.newByteChannel(logFileMetadata.getLogFile(), StandardOpenOption.READ), ByteBuffer.allocate(DEFAULT_BUFFER_SIZE).order(ByteOrder.LITTLE_ENDIAN),
				EmptyChecksum.getInstance());
		_unshuffleOpcodes = unshuffleOpcodes;
		
		// move to first packet
		_input.setPositionInChannel(logFileMetadata.getHeaderSize());
		
		_fakeServer = fakeServer;
		_fakeClient = fakeServer.getTargetClient();
		_copyVsNew = ByteBuffer.allocate(1 << 16 - 3).order(ByteOrder.LITTLE_ENDIAN);
		_buf = new MMOBuffer().setByteBuffer(_copyVsNew);
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
			throw new LogFileIterationIOException(_logFileMetadata.getLogFile().getFileName().toString(), e);
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
					_copyVsNew.clear();
					_copyVsNew.put(body).flip();
					if (type.isClient())
					{
						_fakeClient.decipher(_copyVsNew);
						_fakeClient.setFirstTime(false);
						L2GameClientPackets.getInstance().handlePacket(_copyVsNew, _fakeClient, _buf.readUC()).readAndChangeState(_fakeClient, _buf);
					}
					else
					{
						_fakeServer.decipher(_copyVsNew);
						L2GameServerPackets.getInstance().handlePacket(_copyVsNew, _fakeServer, _buf.readUC()).readAndChangeState(_fakeServer, _buf);
					}
					_copyVsNew.position(0);
					_copyVsNew.get(body);
				}
				else if (_unshuffleOpcodes)
				{
					_copyVsNew.clear();
					_copyVsNew.put(body).flip();
					if (type.isClient())
					{
						_fakeClient.getDeobfuscator().decodeOpcodes(_copyVsNew);
						final int len = Math.min(3, body.length);
						for (int i = 0; i < len; ++i)
							body[i] = _copyVsNew.get(i);
						L2GameClientPackets.getInstance().handlePacket(_copyVsNew, _fakeClient, _buf.readUC()).readAndChangeState(_fakeClient, _buf);
					}
					else
						L2GameServerPackets.getInstance().handlePacket(_copyVsNew, _fakeServer, _buf.readUC()).readAndChangeState(_fakeServer, _buf);
				}
				
				return new PSLogFilePacket(type, body, time);
			}
		}
		catch (final IOException e)
		{
			throw new LogFileIterationIOException(_logFileMetadata.getLogFile().getFileName().toString(), e);
		}
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
