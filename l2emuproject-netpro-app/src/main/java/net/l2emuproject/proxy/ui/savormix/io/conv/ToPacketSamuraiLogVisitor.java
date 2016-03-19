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

import static net.l2emuproject.proxy.ui.savormix.io.LoggedPacketFlag.HIDDEN;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Set;

import net.l2emuproject.io.EmptyChecksum;
import net.l2emuproject.proxy.io.LogFileHeader;
import net.l2emuproject.proxy.ui.ReceivedPacket;
import net.l2emuproject.proxy.ui.savormix.io.LoggedPacketFlag;
import net.l2emuproject.proxy.ui.savormix.io.base.IOConstants;
import net.l2emuproject.proxy.ui.savormix.io.base.NewIOHelper;
import net.l2emuproject.proxy.ui.savormix.io.task.HistoricalLogPacketVisitor;

/**
 * Writes a raw data stream based on visited packets.
 * 
 * @author _dev_
 */
public class ToPacketSamuraiLogVisitor implements HistoricalLogPacketVisitor, IOConstants
{
	private NewIOHelper _writer;
	private int _totalPackets;
	
	/** Constructs this visitor. */
	public ToPacketSamuraiLogVisitor()
	{
	}
	
	@Override
	public void onStart(LogFileHeader logHeader) throws Exception
	{
		if (logHeader.getService().isLogin())
			throw new UnsupportedOperationException();
		
		final Path logFile = logHeader.getLogFile();
		final SeekableByteChannel channel = Files.newByteChannel(logFile.resolveSibling(logFile.getFileName() + ".psl"), StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING,
				StandardOpenOption.CREATE);
		_writer = new NewIOHelper(channel, ByteBuffer.allocate(DEFAULT_BUFFER_SIZE).order(ByteOrder.LITTLE_ENDIAN), EmptyChecksum.getInstance());
		
		_writer.writeByte(7);
		_writer.writeInt(0); // reserve packet count
		_writer.writeBoolean(false); // always single part
		_writer.writeChar(0); // part number
		_writer.writeChar(7777); // non-JP
		_writer.writeLong(0); // no IPs
		for (final char c : "lineage2".toCharArray())
			_writer.writeChar(c);
		_writer.writeChar(0); // protocol name
		_writer.writeChar(0); // comments
		_writer.writeChar(0); // server type
		_writer.writeLong(0); // undocumented
		_writer.writeLong(logHeader.getCreated()); // session ID
		_writer.writeByte(0); // not enciphered
		
		_totalPackets = 0;
	}
	
	@Override
	public void onPacket(ReceivedPacket packet, Set<LoggedPacketFlag> flags) throws Exception
	{
		if (flags.contains(HIDDEN))
			return;
		
		final boolean client = packet.getEndpoint().isClient();
		
		_writer.writeByte(client ? 0 : 1);
		_writer.writeChar(packet.getBody().length + 2);
		_writer.writeLong(packet.getReceived());
		_writer.write(packet.getBody());
		
		++_totalPackets;
	}
	
	@Override
	public void onEnd() throws Exception
	{
		if (_writer == null)
			return;
		
		_writer.flush();
		
		_writer.setPositionInChannel(1);
		_writer.writeInt(_totalPackets);
		_writer.flush();
		_writer.close();
	}
}
