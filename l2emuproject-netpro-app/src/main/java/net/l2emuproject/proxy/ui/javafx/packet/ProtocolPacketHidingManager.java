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
package net.l2emuproject.proxy.ui.javafx.packet;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Consumer;

import net.l2emuproject.network.protocol.IProtocolVersion;
import net.l2emuproject.proxy.network.EndpointType;
import net.l2emuproject.proxy.network.ServiceType;
import net.l2emuproject.proxy.ui.savormix.io.VersionnedPacketTable;
import net.l2emuproject.proxy.ui.savormix.io.base.IOConstants;
import net.l2emuproject.proxy.ui.savormix.io.base.NewIOHelper;
import net.l2emuproject.util.concurrent.MapUtils;
import net.l2emuproject.util.logging.L2Logger;

/**
 * Manages protocol-based packet hiding configurations and their persistence (both manual and automatic).
 * 
 * @author _dev_
 */
public final class ProtocolPacketHidingManager implements IOConstants
{
	private static final L2Logger LOG = L2Logger.getLogger(ProtocolPacketHidingManager.class);
	/** Allows custom handling of exceptions that occur when loading default protocol packet hiding configurations. */
	public static Consumer<Map<Path, Exception>> AUTOMATIC_LOADING_EXCEPTION_HANDLER = null;
	
	private volatile Map<IProtocolVersion, IPacketHidingConfig> _configurations;
	
	ProtocolPacketHidingManager()
	{
		_configurations = Collections.emptyMap();
		
		if (AUTOMATIC_LOADING_EXCEPTION_HANDLER != null)
			AUTOMATIC_LOADING_EXCEPTION_HANDLER.accept(autoLoad());
	}
	
	/**
	 * Returns an up-to-date user-managed packet hiding configuration for the given protocol version.
	 * 
	 * @param protocol protocol version
	 * @return packet hiding configuration
	 */
	public IPacketHidingConfig getHidingConfiguration(IProtocolVersion protocol)
	{
		final IPacketHidingConfig config = _configurations.get(protocol);
		return config != null ? config : MapUtils.putIfAbsent(_configurations, protocol, newHidingConfig(Collections.emptySet(), Collections.emptySet()));
	}
	
	/**
	 * Saves the given packet hiding configuration to file.
	 * 
	 * @param file output file
	 * @param configuration packet hiding configuration
	 * @throws IOException if the configuration could not be saved
	 */
	public void saveHidingConfiguration(Path file, IPacketHidingConfig configuration) throws IOException
	{
		final Map<EndpointType, Set<byte[]>> type2Prefixes = configuration.getSaveableFormat();
		saveToFile(file, type2Prefixes.getOrDefault(EndpointType.CLIENT, Collections.emptySet()), type2Prefixes.getOrDefault(EndpointType.SERVER, Collections.emptySet()));
	}
	
	private Map<Path, Exception> autoLoad()
	{
		final Map<Path, Exception> report = new TreeMap<Path, Exception>();
		
		final Map<ServiceType, String> type2Prefix = new EnumMap<>(ServiceType.class);
		type2Prefix.put(ServiceType.LOGIN, "auth_");
		type2Prefix.put(ServiceType.GAME, "l2_");
		
		final Map<IProtocolVersion, IPacketHidingConfig> configurations = new HashMap<>();
		for (final Entry<ServiceType, String> e : type2Prefix.entrySet())
		{
			int totalConfigurations = 0;
			for (final IProtocolVersion protocol : VersionnedPacketTable.getInstance().getKnownProtocols(e.getKey()))
			{
				final Path file = PROTOCOL_PACKET_HIDING_DIR.resolve(e.getValue() + protocol.getVersion() + "." + PROTOCOL_PACKET_HIDING_EXTENSION);
				try
				{
					configurations.put(protocol, readHidingConfig(file));
					++totalConfigurations;
				}
				catch (FileNotFoundException | NoSuchFileException ex)
				{
					// nothing to be loaded automatically
					configurations.put(protocol, newHidingConfig(Collections.emptySet(), Collections.emptySet()));
				}
				catch (Exception ex)
				{
					report.put(file, ex);
				}
			}
			LOG.info("Loaded packet hiding configurations for " + totalConfigurations + " " + e.getKey().toString().toLowerCase(Locale.ENGLISH) + " protocol versions.");
		}
		_configurations = configurations.isEmpty() ? Collections.emptyMap() : configurations;
		
		return Collections.unmodifiableMap(report);
	}
	
	private void saveToFile(Path file, Set<byte[]> clientPacketPrefixes, Set<byte[]> serverPacketPrefixes) throws IOException
	{
		Files.createDirectories(file.getParent());
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
				out.setPositionInChannel(blockSizePos);
				out.writeInt((int)blockSize);
				out.setPositionInChannel(nextBlockPos);
			}
		}
	}
	
	private IPacketHidingConfig readHidingConfig(Path file) throws IOException
	{
		final Map<EndpointType, Set<byte[]>> type2Prefixes = new EnumMap<EndpointType, Set<byte[]>>(EndpointType.class);
		for (final EndpointType type : EndpointType.VALUES)
			type2Prefixes.put(type, new HashSet<>());
		try (final SeekableByteChannel channel = Files.newByteChannel(file, StandardOpenOption.READ); final NewIOHelper in = new NewIOHelper(channel))
		{
			in.readLong(); // magic
			in.readByte(); // version
			
			final int blockCount = in.readByte() & 0xFF;
			for (int i = 0; i < blockCount; ++i)
			{
				final int blockSize = in.readInt();
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
				for (int j = 0; j < prefixCount; ++j)
				{
					final int totalSize = in.readByte();
					final int prefixSize = in.readByte();
					final byte[] prefix = new byte[prefixSize];
					in.read(prefix);
					prefixSet.add(VersionnedPacketTable.internedValueOf(prefix));
					in.skip(totalSize - prefixSize - 1, false);
				}
			}
		}
		return newHidingConfig(type2Prefixes.get(EndpointType.CLIENT), type2Prefixes.get(EndpointType.SERVER));
	}
	
	private IPacketHidingConfig newHidingConfig(Set<byte[]> clientPrefixes, Set<byte[]> serverPrefixes)
	{
		return new PacketHidingConfig(new CopyOnWriteArraySet<>(clientPrefixes), new CopyOnWriteArraySet<>(serverPrefixes));
	}
	
	/**
	 * Returns a singleton instance of this type.
	 * 
	 * @return an instance of this class
	 */
	public static final ProtocolPacketHidingManager getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static final class SingletonHolder
	{
		static final ProtocolPacketHidingManager INSTANCE = new ProtocolPacketHidingManager();
	}
}
