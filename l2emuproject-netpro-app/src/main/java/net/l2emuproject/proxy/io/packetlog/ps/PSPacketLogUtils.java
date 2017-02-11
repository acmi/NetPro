/*
 * Copyright 2011-2016 L2EMU UNIQUE
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

import java.io.EOFException;
import java.io.IOException;
import java.net.InetAddress;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import net.l2emuproject.network.protocol.IProtocolVersion;
import net.l2emuproject.proxy.io.NewIOHelper;
import net.l2emuproject.proxy.io.definitions.VersionnedPacketTable;
import net.l2emuproject.proxy.io.exception.DamagedFileException;
import net.l2emuproject.proxy.io.exception.EmptyPacketLogException;
import net.l2emuproject.proxy.io.exception.IncompletePacketLogFileException;
import net.l2emuproject.proxy.io.exception.InsufficientlyLargeFileException;
import net.l2emuproject.proxy.io.exception.TruncatedPacketLogFileException;
import net.l2emuproject.proxy.io.exception.UnknownFileTypeException;
import net.l2emuproject.proxy.network.EndpointType;
import net.l2emuproject.proxy.ui.javafx.packet.ProtocolPacketHidingManager;

/**
 * Various methods related to PacketSamurai/YAL historical packet log file handling.
 * 
 * @author _dev_
 */
public class PSPacketLogUtils
{
	private PSPacketLogUtils()
	{
		// utility class
	}
	
	/**
	 * Returns an iterator that can iterate over packets contained in a PacketSamurai/YAL packet log file.
	 * 
	 * @param logFileMetadata log file metadata
	 * @return logfile packet iterator
	 * @throws IOException if a generic I/O error occurs
	 */
	public static final PSPacketLogFileIterator getPacketIterator(PSLogFileHeader logFileMetadata) throws IOException
	{
		return new PSPacketLogFileIterator(logFileMetadata);
	}
	
	/**
	 * Tests whether the packet should be loaded to memory based on user's selection of packets to be loaded.
	 * 
	 * @param packet packet from a log file
	 * @param options user's selected loading options
	 * @return whether the given {@code packet} should be loaded
	 */
	public static final boolean isLoadable(PSLogFilePacket packet, PSLogLoadOptions options)
	{
		final EndpointType endpoint = packet.getEndpoint();
		final IProtocolVersion protocol = options.getProtocol();
		return !ProtocolPacketHidingManager.getInstance().getHidingConfiguration(protocol).get().isHidden(endpoint,
				VersionnedPacketTable.getInstance().getTemplate(protocol, endpoint, packet.getContent()));
	}
	
	/**
	 * Reads a limited amount of data from the specified log file to verify that it is a valid PackeSamurai/YAL packet log and stores it in the returned object.
	 * 
	 * @param packetLogFile path to log file
	 * @return log file metadata
	 * @throws InsufficientlyLargeFileException if the given file is too small to be a NetPro packet log
	 * @throws IncompletePacketLogFileException if the given file is either a work in progress or it's generation was abruptly terminated
	 * @throws UnknownFileTypeException if the given file is not a NetPro packet log (different/unknown format)
	 * @throws TruncatedPacketLogFileException if the given file does not include all mandatory metadata fields
	 * @throws EmptyPacketLogException if the given packet log is empty
	 * @throws DamagedFileException if the given file contains incoherent metadata
	 * @throws InterruptedException if the operation was cancelled by the user
	 * @throws IOException in case of a general I/O error
	 */
	public static final PSLogFileHeader getMetadata(Path packetLogFile) throws InsufficientlyLargeFileException, IncompletePacketLogFileException, UnknownFileTypeException,
			TruncatedPacketLogFileException, EmptyPacketLogException, DamagedFileException, InterruptedException, IOException
	{
		final int minSize = 1 + 4 + 1 + 2 + 2 + 4 + 4 + 2 + 2 + 2 + 8 + 8 + 1;
		
		long size = -1;
		try
		{
			size = Files.size(packetLogFile);
			if (size < minSize)
				throw new InsufficientlyLargeFileException();
		}
		catch (final IOException e)
		{
			// whatever
		}
		
		try (final SeekableByteChannel channel = Files.newByteChannel(packetLogFile, StandardOpenOption.READ); final NewIOHelper in = new NewIOHelper(channel))
		{
			try
			{
				in.fetchUntilAvailable(minSize);
			}
			catch (final EOFException e)
			{
				throw new InsufficientlyLargeFileException();
			}
			
			final int logFileVersion = in.readByte();
			final int totalPackets = in.readInt();
			if (totalPackets < 0)
			{
				throw new DamagedFileException("Packet amount");
			}
			final boolean multipart = in.readBoolean();
			final int partNumber = in.readChar();
			final int servicePort = in.readChar();
			final byte[] ipv4 = new byte[4];
			in.read(ipv4);
			final InetAddress sourceIP = InetAddress.getByAddress(ipv4);
			in.read(ipv4);
			final InetAddress destinationIP = InetAddress.getByAddress(ipv4);
			final StringBuilder sb = new StringBuilder();
			for (char c; (c = (char)in.readChar()) != 0;)
				sb.append(c);
			final String protocolName = sb.toString();
			sb.setLength(0);
			for (char c; (c = (char)in.readChar()) != 0;)
				sb.append(c);
			final String comments = sb.toString();
			sb.setLength(0);
			for (char c; (c = (char)in.readChar()) != 0;)
				sb.append(c);
			final String serverType = sb.toString();
			final long analyzerBitSet = in.readLong();
			final long sessionID = in.readLong();
			final boolean enciphered = in.readBoolean();
			
			if (Thread.interrupted())
				throw new InterruptedException();
			
			int protocolVersionNumber = -1;
			extractProtocolVersion:
			{
				// first packet should be [C] SendProtocolVersion
				if (in.readBoolean()) // not a client packet
					break extractProtocolVersion;
				
				final int packetSize = in.readChar() - 2;
				if (packetSize < 1 + 4)
					break extractProtocolVersion;
				
				in.readLong();
				
				final int opcode = in.readByte();
				if (opcode != 0x00 && opcode != 0x0E) // Chronicle opcode/Throne opcode
					break extractProtocolVersion;
				
				final ByteBuffer leBuffer = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN);
				in.read(leBuffer);
				leBuffer.clear();
				
				protocolVersionNumber = leBuffer.getInt();
			}
			return new PSLogFileHeader(packetLogFile, size, logFileVersion, totalPackets, multipart, partNumber, servicePort, sourceIP, destinationIP, protocolName, comments, serverType,
					analyzerBitSet, sessionID, enciphered, protocolVersionNumber);
		}
		catch (final BufferUnderflowException e)
		{
			throw new TruncatedPacketLogFileException();
		}
	}
}
