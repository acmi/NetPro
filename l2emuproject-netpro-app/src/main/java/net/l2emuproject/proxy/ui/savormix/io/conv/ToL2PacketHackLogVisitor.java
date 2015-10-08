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

import java.io.BufferedWriter;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import net.l2emuproject.proxy.ui.ReceivedPacket;
import net.l2emuproject.proxy.ui.savormix.io.LogFileHeader;
import net.l2emuproject.proxy.ui.savormix.io.base.IOConstants;
import net.l2emuproject.proxy.ui.savormix.io.task.HistoricalLogPacketVisitor;
import net.l2emuproject.util.HexUtil;

/**
 * Writes a raw data stream based on visited packets.
 * 
 * @author _dev_
 */
public class ToL2PacketHackLogVisitor implements HistoricalLogPacketVisitor, IOConstants
{
	private final ByteBuffer _buf;
	
	private BufferedWriter _writer;
	
	/** Constructs this visitor. */
	public ToL2PacketHackLogVisitor()
	{
		_buf = ByteBuffer.allocate(1 + 8 + Character.MAX_VALUE).order(ByteOrder.LITTLE_ENDIAN);
	}
	
	@Override
	public void onStart(LogFileHeader logHeader) throws Exception
	{
		if (logHeader.isLogin())
			throw new UnsupportedOperationException();
			
		final Path logFile = logHeader.getLogFile();
		_writer = Files.newBufferedWriter(logFile.resolveSibling(logFile.getFileName() + ".pLog"), StandardCharsets.US_ASCII);
	}
	
	@Override
	public void onPacket(ReceivedPacket packet) throws Exception
	{
		final boolean client = packet.getEndpoint().isClient();
		
		_buf.clear();
		_buf.put((byte)(client ? 4 : 3)).putDouble(ToL2PacketHackRawLogVisitor.toDateTime(packet.getReceived()));
		_buf.putChar((char)(packet.getBody().length + 2)).put(packet.getBody()).flip();
		
		_writer.append(HexUtil.bytesToHexString(_buf.array(), 0, _buf.remaining(), "")).append("\r\n");
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
}
