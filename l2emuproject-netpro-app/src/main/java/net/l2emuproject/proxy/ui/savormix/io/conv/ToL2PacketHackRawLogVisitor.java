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
package net.l2emuproject.proxy.ui.savormix.io.conv;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Set;

import net.l2emuproject.io.EmptyChecksum;
import net.l2emuproject.network.mmocore.MMOBuffer;
import net.l2emuproject.proxy.network.game.client.L2GameClient;
import net.l2emuproject.proxy.network.game.client.L2GameClientPackets;
import net.l2emuproject.proxy.network.game.server.L2GameServer;
import net.l2emuproject.proxy.network.game.server.L2GameServerPackets;
import net.l2emuproject.proxy.ui.ReceivedPacket;
import net.l2emuproject.proxy.ui.savormix.io.LogFileHeader;
import net.l2emuproject.proxy.ui.savormix.io.LoggedPacketFlag;
import net.l2emuproject.proxy.ui.savormix.io.base.IOConstants;
import net.l2emuproject.proxy.ui.savormix.io.base.NewIOHelper;
import net.l2emuproject.proxy.ui.savormix.io.task.HistoricalLogPacketVisitor;

/**
 * Writes a raw data stream based on visited packets.
 * 
 * @author _dev_
 */
public class ToL2PacketHackRawLogVisitor implements HistoricalLogPacketVisitor, IOConstants
{
	private final MMOBuffer _buf;
	
	private L2GameClient _fakeClient;
	private L2GameServer _fakeServer;
	private NewIOHelper _writer;
	
	/** Constructs this visitor. */
	public ToL2PacketHackRawLogVisitor()
	{
		_buf = new MMOBuffer();
	}
	
	@Override
	public void onStart(LogFileHeader logHeader) throws Exception
	{
		if (logHeader.isLogin())
			throw new UnsupportedOperationException();
		
		_fakeClient = new L2GameClient(null, null);
		_fakeServer = new L2GameServer(null, null, _fakeClient);
		
		final Path logFile = logHeader.getLogFile();
		final SeekableByteChannel channel = Files.newByteChannel(logFile.resolveSibling(logFile.getFileName() + ".rawLog"), StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING,
				StandardOpenOption.CREATE);
		_writer = new NewIOHelper(channel, ByteBuffer.allocate(DEFAULT_BUFFER_SIZE).order(ByteOrder.LITTLE_ENDIAN), EmptyChecksum.getInstance());
	}
	
	@Override
	public void onPacket(ReceivedPacket packet, Set<LoggedPacketFlag> flags) throws Exception
	{
		final boolean client = packet.getEndpoint().isClient();
		
		_writer.writeByte(client ? 4 : 3);
		_writer.writeChar(packet.getBody().length + 2);
		_writer.writeLong(Double.doubleToLongBits(toDateTime(packet.getReceived())));
		
		final ByteBuffer wrapper = ByteBuffer.wrap(packet.getBody()).order(ByteOrder.LITTLE_ENDIAN);
		_buf.setByteBuffer(wrapper);
		if (client)
		{
			L2GameClientPackets.getInstance().handlePacket(wrapper, _fakeClient, _buf.readUC()).readAndChangeState(_fakeClient, _buf);
			wrapper.clear();
			_fakeServer.encipher(wrapper);
			_fakeServer.setFirstTime(false);
		}
		else
		{
			L2GameServerPackets.getInstance().handlePacket(wrapper, _fakeServer, _buf.readUC()).readAndChangeState(_fakeServer, _buf);
			wrapper.clear();
			_fakeClient.encipher(wrapper);
		}
		
		_writer.writeChar(wrapper.clear().limit() + 2);
		_writer.write(wrapper);
	}
	
	@Override
	public void onEnd() throws Exception
	{
		if (_writer != null)
		{
			_writer.flush();
			_writer.close();
		}
	}
	
	/**
	 * Converts an UNIX millis timestamp to a Delphi {@code TDateTime}.
	 * 
	 * @param timestamp Java timestamp
	 * @return Delphi datetime
	 */
	public static final double toDateTime(long timestamp)
	{
		double result = timestamp;
		result /= (24 * 60 * 60 * 1000);
		return result + 25569D;
	}
}
