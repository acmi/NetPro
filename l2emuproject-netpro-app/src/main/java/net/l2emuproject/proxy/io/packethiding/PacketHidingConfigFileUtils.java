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
package net.l2emuproject.proxy.io.packethiding;

import java.io.EOFException;
import java.io.IOException;
import java.nio.BufferUnderflowException;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import net.l2emuproject.proxy.io.IOConstants;
import net.l2emuproject.proxy.io.NewIOHelper;
import net.l2emuproject.proxy.io.definitions.VersionnedPacketLoader;
import net.l2emuproject.proxy.io.exception.DamagedFileException;
import net.l2emuproject.proxy.io.exception.InsufficientlyLargeFileException;
import net.l2emuproject.proxy.io.exception.UnknownFileTypeException;
import net.l2emuproject.proxy.network.EndpointType;
import net.l2emuproject.proxy.ui.javafx.packet.IPacketHidingConfig;
import net.l2emuproject.proxy.ui.javafx.packet.PacketHidingConfig;
import net.l2emuproject.util.logging.L2Logger;

/**
 * Various methods related to NetPro packet hiding configuration file handling.
 * 
 * @author _dev_
 */
public final class PacketHidingConfigFileUtils implements IOConstants
{
	private static final L2Logger LOG = L2Logger.getLogger(PacketHidingConfigFileUtils.class);
	
	private PacketHidingConfigFileUtils()
	{
		// utility class
	}
	
	/**
	 * Restores a packet hiding configuration from file.
	 * 
	 * @param file a file
	 * @return packet hiding configuration
	 * @throws InsufficientlyLargeFileException if the given file is too small to be a NetPro packet hiding configuration
	 * @throws UnknownFileTypeException if the given file is not a NetPro packet hiding configuration (different/unknown format)
	 * @throws DamagedFileException if the given file contains incoherent metadata
	 * @throws IOException in case of a general I/O error
	 * @throws InterruptedException if the operation was cancelled by the user
	 */
	public static final IPacketHidingConfig readHidingConfiguration(Path file)
			throws InsufficientlyLargeFileException, UnknownFileTypeException, DamagedFileException, IOException, InterruptedException
	{
		final Map<EndpointType, Set<byte[]>> type2Prefixes = new EnumMap<EndpointType, Set<byte[]>>(EndpointType.class);
		for (final EndpointType type : EndpointType.VALUES)
			type2Prefixes.put(type, new HashSet<>());
		try (final SeekableByteChannel channel = Files.newByteChannel(file, StandardOpenOption.READ); final NewIOHelper in = new NewIOHelper(channel))
		{
			try
			{
				final long magicValue = in.readLong(); // magic
				if (magicValue != PROTOCOL_PACKET_HIDING_MAGIC)
					throw new UnknownFileTypeException(magicValue);
				if (in.readByte() < 1) // version
					throw new DamagedFileException("version");
			}
			catch (BufferUnderflowException | EOFException e)
			{
				throw new InsufficientlyLargeFileException();
			}
			
			final int blockCount = in.readByte() & 0xFF;
			for (int i = 0; i < blockCount; ++i)
			{
				if (Thread.interrupted())
					throw new InterruptedException();
				
				final int blockSize = in.readInt();
				if (blockSize < 5)
					throw new DamagedFileException("block size");
				final int typeValue = in.readByte() & 0xFF;
				final Set<byte[]> prefixSet;
				try
				{
					prefixSet = type2Prefixes.get(EndpointType.VALUES[typeValue]);
				}
				catch (ArrayIndexOutOfBoundsException e)
				{
					LOG.warn("Skipping a block with invalid type: " + typeValue + " in " + file.getFileName());
					in.skip(blockSize - 1, false);
					continue;
				}
				final int prefixCount = in.readInt();
				if (prefixCount < 0)
					throw new DamagedFileException("prefix count");
				for (int j = 0; j < prefixCount; ++j)
				{
					final int totalSize = in.readByte();
					final int prefixSize = in.readByte();
					final byte[] prefix = new byte[prefixSize];
					in.read(prefix);
					prefixSet.add(VersionnedPacketLoader.internedValueOf(prefix));
					in.skip(totalSize - prefixSize - 1, false);
				}
			}
		}
		return new PacketHidingConfig(type2Prefixes.get(EndpointType.CLIENT), type2Prefixes.get(EndpointType.SERVER));
	}
	
	/**
	 * Saves the given packet hiding configuration to file.
	 * 
	 * @param file output file
	 * @param configuration packet hiding configuration
	 * @throws IOException if the configuration could not be saved
	 * @throws InterruptedException if user cancels I/O
	 */
	public static final void saveHidingConfiguration(Path file, IPacketHidingConfig configuration) throws IOException, InterruptedException
	{
		final Map<EndpointType, Set<byte[]>> type2Prefixes = configuration.getSaveableFormat();
		saveToFile(file, type2Prefixes.getOrDefault(EndpointType.CLIENT, Collections.emptySet()), type2Prefixes.getOrDefault(EndpointType.SERVER, Collections.emptySet()));
	}
	
	private static final void saveToFile(Path file, Set<byte[]> clientPacketPrefixes, Set<byte[]> serverPacketPrefixes) throws IOException, InterruptedException
	{
		Files.createDirectories(file.resolve(".."));
		try (final SeekableByteChannel channel = Files.newByteChannel(file, StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
				final NewIOHelper out = new NewIOHelper(channel))
		{
			out.writeLong(PROTOCOL_PACKET_HIDING_MAGIC);
			out.writeByte(PROTOCOL_PACKET_HIDING_VERSION);
			
			final Map<EndpointType, Set<byte[]>> type2Prefixes = new EnumMap<>(EndpointType.class);
			type2Prefixes.put(EndpointType.CLIENT, clientPacketPrefixes);
			type2Prefixes.put(EndpointType.SERVER, serverPacketPrefixes);
			
			out.writeByte(type2Prefixes.size());
			for (final Entry<EndpointType, Set<byte[]>> e : type2Prefixes.entrySet())
			{
				if (Thread.interrupted())
					throw new InterruptedException();
				
				final long blockSizePos = out.getPositionInChannel(true);
				out.writeInt(0); // block size
				out.writeByte(e.getKey().ordinal());
				
				final Set<byte[]> prefixes = e.getValue();
				out.writeInt(prefixes.size());
				for (final byte[] prefix : prefixes)
				{
					out.writeByte(1 + prefix.length); // Total entry size
					out.writeByte(prefix.length); // Prefix length
					out.write(prefix);
				}
				final long nextBlockPos = out.getPositionInChannel(true);
				final long blockSize = nextBlockPos - blockSizePos - 4;
				out.flush();
				out.setPositionInChannel(blockSizePos);
				out.writeInt((int)blockSize);
				out.flush();
				out.setPositionInChannel(nextBlockPos);
			}
		}
		catch (InterruptedException e)
		{
			try
			{
				Files.delete(file);
			}
			catch (IOException ex)
			{
				// does not matter
				LOG.info("cancel", ex);
			}
			throw e;
		}
	}
}
