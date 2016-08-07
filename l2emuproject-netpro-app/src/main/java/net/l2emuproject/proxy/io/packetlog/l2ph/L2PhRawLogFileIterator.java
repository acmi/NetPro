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

import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

import net.l2emuproject.io.EmptyChecksum;
import net.l2emuproject.network.mmocore.MMOBuffer;
import net.l2emuproject.proxy.io.IOConstants;
import net.l2emuproject.proxy.io.NewIOHelper;
import net.l2emuproject.proxy.io.exception.LogFileIterationIOException;
import net.l2emuproject.proxy.network.EndpointType;
import net.l2emuproject.proxy.network.ServiceType;
import net.l2emuproject.proxy.network.game.client.L2GameClient;
import net.l2emuproject.proxy.network.game.client.L2GameClientPackets;
import net.l2emuproject.proxy.network.game.server.L2GameServer;
import net.l2emuproject.proxy.network.game.server.L2GameServerPackets;

/**
 * Allows convenient L2PacketHack raw packet log reading.
 * 
 * @author _dev_
 */
public class L2PhRawLogFileIterator implements IL2PhLogFileIterator, IOConstants
{
	//private final L2PhLogFileHeader _logFileMetadata;
	private final NewIOHelper _input;
	
	private final L2GameClient _fakeClient;
	private final L2GameServer _fakeServer;
	private final MMOBuffer _buf;
	
	L2PhRawLogFileIterator(L2PhLogFileHeader logFileMetadata) throws IOException
	{
		//_logFileMetadata = logFileMetadata;
		_input = new NewIOHelper(Files.newByteChannel(logFileMetadata.getLogFile(), StandardOpenOption.READ), ByteBuffer.allocate(DEFAULT_BUFFER_SIZE).order(ByteOrder.LITTLE_ENDIAN),
				EmptyChecksum.getInstance());
		
		_fakeServer = new L2GameServer(null, null, _fakeClient = new L2GameClient(null, null));
		_buf = new MMOBuffer();
	}
	
	@Override
	public boolean hasNext() throws LogFileIterationIOException
	{
		try
		{
			_input.fetchUntilAvailable(1/* + 2 + 8 + 2*/);
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
	public L2PhLogFilePacket next() throws LogFileIterationIOException
	{
		try
		{
			final int packetType = _input.readByte();
			final int size = _input.readChar() - 2;
			
			final ServiceType service = L2PhLogFileUtils.toServiceType((byte)packetType);
			final EndpointType endpoint = EndpointType.valueOf((packetType & 1) == 0);
			final long time = L2PhLogFileUtils.toUNIX(Double.longBitsToDouble(_input.readLong()));
			_input.readChar(); // packet size as part of packet
			final byte[] content = new byte[size];
			_input.read(content);
			
			final ByteBuffer wrapper = ByteBuffer.wrap(content).order(ByteOrder.LITTLE_ENDIAN);
			_buf.setByteBuffer(wrapper);
			if (endpoint.isClient())
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
			
			return new L2PhLogFilePacket(service, endpoint, time, content);
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
