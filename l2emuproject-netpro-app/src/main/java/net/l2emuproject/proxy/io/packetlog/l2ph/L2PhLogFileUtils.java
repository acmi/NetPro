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

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import net.l2emuproject.io.EmptyChecksum;
import net.l2emuproject.network.protocol.IProtocolVersion;
import net.l2emuproject.proxy.io.IOConstants;
import net.l2emuproject.proxy.io.NewIOHelper;
import net.l2emuproject.proxy.io.definitions.VersionnedPacketTable;
import net.l2emuproject.proxy.io.exception.DamagedFileException;
import net.l2emuproject.proxy.io.exception.InsufficientlyLargeFileException;
import net.l2emuproject.proxy.io.exception.UnknownFileTypeException;
import net.l2emuproject.proxy.network.EndpointType;
import net.l2emuproject.proxy.network.ServiceType;
import net.l2emuproject.proxy.ui.javafx.packet.ProtocolPacketHidingManager;
import net.l2emuproject.util.HexUtil;

/**
 * Various methods related to L2PacketHack historical packet log file handling.
 * 
 * @author _dev_
 */
public final class L2PhLogFileUtils implements IOConstants
{
	private L2PhLogFileUtils()
	{
		// utility class
	}
	
	/**
	 * Returns an iterator that can iterate over packets contained in a L2PacketHack packet log file.
	 * 
	 * @param logFileMetadata log file metadata
	 * @return logfile packet iterator
	 * @throws IOException if a generic I/O error occurs
	 */
	public static final IL2PhLogFileIterator getPacketIterator(L2PhLogFileHeader logFileMetadata) throws IOException
	{
		return logFileMetadata.isRaw() ? new L2PhRawLogFileIterator(logFileMetadata) : new L2PhLogFileIterator(logFileMetadata);
	}
	
	/**
	 * Tests whether the packet should be loaded to memory based on user's selection of packets to be loaded.
	 * 
	 * @param packet packet from a log file
	 * @param options user's selected loading options
	 * @return whether the given {@code packet} should be loaded
	 */
	public static final boolean isLoadable(L2PhLogFilePacket packet, L2PhLogLoadOptions options)
	{
		final EndpointType endpoint = packet.getEndpoint();
		final IProtocolVersion protocol = options.getProtocol();
		return !ProtocolPacketHidingManager.getInstance().getHidingConfiguration(protocol).get().isHidden(endpoint,
				VersionnedPacketTable.getInstance().getTemplate(protocol, endpoint, packet.getContent()));
	}
	
	/**
	 * Reads a limited amount of data from the specified log file to verify that it is a valid L2PacketHack standard packet log and stores it in the returned object.
	 * 
	 * @param packetLogFile path to log file
	 * @return log file metadata
	 * @throws InsufficientlyLargeFileException if the given file is too small to be a L2PacketHack packet log
	 * @throws UnknownFileTypeException if the given file is not a L2PacketHack packet log (different/unknown format)
	 * @throws DamagedFileException if the given file contains incoherent metadata
	 * @throws IOException in case of a general I/O error
	 */
	public static final L2PhLogFileHeader getMetadata(Path packetLogFile) throws InsufficientlyLargeFileException, UnknownFileTypeException,
			DamagedFileException, IOException
	{
		long filesize = -1L;
		try
		{
			filesize = Files.size(packetLogFile);
		}
		catch (final IOException e)
		{
			// whatever
		}
		
		final byte[] firstPacket;
		try (final BufferedReader in = Files.newBufferedReader(packetLogFile, StandardCharsets.US_ASCII))
		{
			firstPacket = HexUtil.hexStringToBytes(in.readLine());
		}
		final int minSize = 1 + 8 + 2;
		if (firstPacket.length < minSize)
			throw new InsufficientlyLargeFileException();
		if (firstPacket[0] < 1 || firstPacket[0] > 4)
			throw new UnknownFileTypeException(ByteBuffer.wrap(firstPacket, 0, 8).getLong());
		
		final long firstPacketArrivalTime = toUNIX(ByteBuffer.wrap(firstPacket, 1, 8).order(ByteOrder.LITTLE_ENDIAN).getDouble());
		final int opcode = firstPacket.length > minSize ? firstPacket[minSize] : -1;
		final int protocol = opcode == 0x00 || opcode == 0x0E ? ByteBuffer.wrap(firstPacket, 1 + 8 + 2 + 1, 4).order(ByteOrder.LITTLE_ENDIAN).getInt() : -1;
		return new L2PhLogFileHeader(packetLogFile, filesize, false, toServiceType(firstPacket[0]), firstPacketArrivalTime, protocol);
	}
	
	/**
	 * Reads a limited amount of data from the specified log file to verify that it is a valid L2PacketHack standard packet log and stores it in the returned object.
	 * 
	 * @param packetLogFile path to log file
	 * @return log file metadata
	 * @throws InsufficientlyLargeFileException if the given file is too small to be a L2PacketHack packet log
	 * @throws UnknownFileTypeException if the given file is not a L2PacketHack packet log (different/unknown format)
	 * @throws DamagedFileException if the given file contains incoherent metadata
	 * @throws IOException in case of a general I/O error
	 */
	public static final L2PhLogFileHeader getRawMetadata(Path packetLogFile) throws InsufficientlyLargeFileException, UnknownFileTypeException,
			DamagedFileException, IOException
	{
		long filesize = -1L;
		try
		{
			filesize = Files.size(packetLogFile);
		}
		catch (final IOException e)
		{
			// whatever
		}
		
		try (final SeekableByteChannel channel = Files.newByteChannel(packetLogFile, StandardOpenOption.READ);
				final NewIOHelper ioh = new NewIOHelper(channel, ByteBuffer.allocate(1_024).order(ByteOrder.LITTLE_ENDIAN), EmptyChecksum.getInstance()))
		{
			try
			{
				ioh.fetchUntilAvailable(1 + 2 + 8 + 2);
			}
			catch (final EOFException e)
			{
				throw new InsufficientlyLargeFileException();
			}
			
			final int firstPacketType = ioh.readByte();
			if (firstPacketType < 1 || firstPacketType > 4)
			{
				long fakeMagic = ioh.readLong();
				fakeMagic = (fakeMagic << 8) | firstPacketType;
				throw new UnknownFileTypeException(Long.reverseBytes(fakeMagic));
			}
			
			final int contentSize = ioh.readChar() - 2;
			final long firstPacketArrivalTime = toUNIX(Double.longBitsToDouble(ioh.readLong()));
			final int protocol;
			if (contentSize > 0)
			{
				ioh.readChar(); // packet size as part of packet
				final int opcode = ioh.readByte();
				protocol = opcode == 0x00 || opcode == 0x0E ? ioh.readInt() : -1;
			}
			else
			{
				protocol = -1;
			}
			return new L2PhLogFileHeader(packetLogFile, filesize, true, toServiceType((byte)firstPacketType), firstPacketArrivalTime, protocol);
		}
	}
	
	/**
	 * Returns service type given the L2PacketHack packet type.
	 * 
	 * @param packetType packet's type identifier
	 * @return service type
	 */
	public static final ServiceType toServiceType(byte packetType)
	{
		return ServiceType.valueOf(packetType < 3);
	}
	
	/**
	 * Converts a Delphi {@code TDateTime} to a UNIX millis timestamp.
	 * 
	 * @param datetime Delphi datetime
	 * @return Java timestamp
	 */
	public static final long toUNIX(double datetime)
	{
		return (long)((datetime - 25569D) * (24 * 60 * 60 * 1_000));
	}
}
