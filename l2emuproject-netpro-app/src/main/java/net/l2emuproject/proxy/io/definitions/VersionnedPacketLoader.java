/*
 * Copyright 2011-2018 L2EMU UNIQUE
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
package net.l2emuproject.proxy.io.definitions;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.w3c.dom.Node;

import net.l2emuproject.proxy.io.IOConstants;
import net.l2emuproject.proxy.network.EndpointType;
import net.l2emuproject.proxy.network.meta.IPacketTemplate;
import net.l2emuproject.proxy.network.meta.UserDefinedGameProtocolVersion;
import net.l2emuproject.proxy.network.meta.UserDefinedProtocolVersion;
import net.l2emuproject.proxy.network.meta.container.PacketPrefixResolver;
import net.l2emuproject.util.HexUtil;
import net.l2emuproject.util.L2XMLUtils;
import net.l2emuproject.util.logging.L2Logger;

/**
 * Facilitates incremental opcode mapping and packet definition loading.
 * 
 * @author savormix
 * @param <T> protocol type
 */
public class VersionnedPacketLoader<T extends UserDefinedProtocolVersion>
{
	private static final L2Logger LOG = L2Logger.getLogger(VersionnedPacketLoader.class);
	
	private static final Map<ByteArrayWrapper, byte[]> INTERNED_PACKET_PREFIXES = new ConcurrentHashMap<>();
	
	private final Map<String, byte[]> _id2PrefixClient, _id2PrefixServer;
	private final Map<String, IPacketTemplate> _clientPacketsByID, _serverPacketsByID;
	private final Map<byte[], String> _prefix2IDClient, _prefix2IDServer;
	// reference only (these maps should be given as unmodifiable)
	private final Map<String, String> _id2NameClient, _id2NameServer;
	
	public VersionnedPacketLoader(Map<String, String> id2NameClient, Map<String, String> id2NameServer)
	{
		// default constructor
		_id2PrefixClient = new IdentityHashMap<>();
		_id2PrefixServer = new IdentityHashMap<>();
		_clientPacketsByID = new IdentityHashMap<>();
		_serverPacketsByID = new IdentityHashMap<>();
		_prefix2IDClient = new HashMap<>();
		_prefix2IDServer = new HashMap<>();
		
		_id2NameClient = id2NameClient;
		_id2NameServer = id2NameServer;
	}
	
	public VersionnedPacketLoader(VersionnedPacketLoader<T> other)
	{
		// copy constructor
		_id2PrefixClient = new IdentityHashMap<>(other._id2PrefixClient);
		_id2PrefixServer = new IdentityHashMap<>(other._id2PrefixServer);
		_clientPacketsByID = new IdentityHashMap<>(other._clientPacketsByID);
		_serverPacketsByID = new IdentityHashMap<>(other._serverPacketsByID);
		_prefix2IDClient = new HashMap<>(other._prefix2IDClient);
		_prefix2IDServer = new HashMap<>(other._prefix2IDServer);
		
		_id2NameClient = other._id2NameClient;
		_id2NameServer = other._id2NameServer;
	}
	
	public Map<EndpointType, PacketPrefixResolver> load(T protocol, String definitionDirName) throws IOException
	{
		final String proName = protocol.getAlias();
		final Path root = IOConstants.CONFIG_DIRECTORY.resolve("packets").resolve(definitionDirName);
		final List<RedundantOpcodeMapping> redundantMappings = new ArrayList<>(); // abusing lazy allocation
		int removedClientPackets = 0, removedServerPackets = 0;
		try
		{
			final Node mappings = L2XMLUtils.getChildNodeByName(L2XMLUtils.getXMLFile(root.resolve("opcode_mapping.xml")), "mapping");
			removedClientPackets = loadOpcodeMapping(mappings, "client", proName, definitionDirName, _id2NameClient, _id2PrefixClient, _clientPacketsByID, _prefix2IDClient, redundantMappings);
			removedServerPackets = loadOpcodeMapping(mappings, "server", proName, definitionDirName, _id2NameServer, _id2PrefixServer, _serverPacketsByID, _prefix2IDServer, redundantMappings);
		}
		catch (final FileNotFoundException e)
		{
			// with incremental loading, this is OK now
			// LOG.warn(protocol + ": " + e.getMessage());
		}
		catch (final Exception e)
		{
			LOG.fatal(protocol, e);
			return null;
		}
		
		String shuffle = "";
		if (protocol instanceof UserDefinedGameProtocolVersion)
		{
			shuffle = "[shuffle: " + ((UserDefinedGameProtocolVersion)protocol).getOpcodeTableShuffleConfig().getShuffleMode() + "]";
		}
		LOG.info(proName + " declares " + _id2PrefixClient.size() + " client" + shuffle + " and " + _id2PrefixServer.size() + " server packets.");
		if (!redundantMappings.isEmpty())
		{
			LOG.info(proName + " redundantly declares " + redundantMappings);
			/*
			try
			{
				boolean clientEntries = true;
				final List<String> lines = Files.readAllLines(root.resolve("opcode_mapping.xml"));
				for (final Iterator<String> it = lines.iterator(); it.hasNext();)
				{
					final String line = it.next();
					if (line.contains("<server"))
					{
						clientEntries = false;
						continue;
					}
					for (final RedundantOpcodeMapping redundantEntry : redundantMappings)
					{
						if (clientEntries != redundantEntry._client)
							continue;
						
						if (line.contains("opcodePrefix=\"" + redundantEntry._prefix + "\"") && line.contains("id=\"" + redundantEntry._id + "\""))
							it.remove();
					}
				}
				Files.write(root.resolve("opcode_mapping_autofix.xml"), lines);
			}
			catch (IOException ex)
			{
				// ignore
			}
			*/
		}
		
		Set<IPacketTemplate> clientPackets = Collections.emptySet(), serverPackets = Collections.emptySet();
		{
			EndpointPacketLoader loader = new EndpointPacketLoader(protocol, _id2PrefixClient, _id2NameClient, _clientPacketsByID);
			try
			{
				Files.walkFileTree(root.resolve("client"), loader);
				LOG.info(proName + ": [" + loader.getAdded() + " new, " + loader.getUpdated() + " updated, " + removedClientPackets + " removed client packets].");
			}
			catch (final NoSuchFileException ex)
			{
				// looks too verbose to me
				// LOG.warn(e.getKey() + ": missing " + ex.getMessage());
			}
			finally
			{
				clientPackets = loader.getPackets();
			}
			loader = new EndpointPacketLoader(protocol, _id2PrefixServer, _id2NameServer, _serverPacketsByID);
			try
			{
				Files.walkFileTree(root.resolve("server"), loader);
				LOG.info(proName + ": [" + loader.getAdded() + " new, " + loader.getUpdated() + " updated, " + removedServerPackets + " removed server packets].");
			}
			catch (final NoSuchFileException ex)
			{
				// looks too verbose to me
				// LOG.warn(e.getKey() + ": missing " + ex.getMessage());
			}
			finally
			{
				serverPackets = loader.getPackets();
			}
		}
		
		final Map<EndpointType, PacketPrefixResolver> endpointMap = new EnumMap<>(EndpointType.class);
		endpointMap.put(EndpointType.CLIENT, new PacketPrefixResolver(clientPackets));
		endpointMap.put(EndpointType.SERVER, new PacketPrefixResolver(serverPackets));
		return endpointMap;
	}
	
	private int loadOpcodeMapping(Node node, String type, String proName, String definitionDirName, Map<String, String> id2Name, Map<String, byte[]> id2Prefix,
			Map<String, IPacketTemplate> id2Template, Map<byte[], String> prefix2ID, List<RedundantOpcodeMapping> redundantMappings)
	{
		int removedPackets = 0;
		final Set<ByteArrayWrapper> declaredOpcodes = new HashSet<>();
		final Set<String> assignedIDs = new HashSet<>();
		
		final Node clientMaps = L2XMLUtils.getChildNodeByName(node, type);
		for (final Node packet : L2XMLUtils.listNodesByNodeName(clientMaps, "removedPacket"))
		{
			final String packetID = L2XMLUtils.getAttribute(packet, "id").intern();
			if (!id2Name.containsKey(packetID))
				LOG.info(proName + " attempts to remove a nonexistent " + type + " packet: " + packetID + " [" + definitionDirName + "]");
			if (id2Prefix.remove(packetID) == null)
				LOG.info(proName + " declares a removed " + type + " packet, even though it was never mapped to any opcode: " + packetID + " [" + definitionDirName + "]");
			id2Template.remove(packetID);
			++removedPackets;
		}
		for (final Node packet : L2XMLUtils.listNodesByNodeName(clientMaps, "packet"))
		{
			final String packetID = L2XMLUtils.getAttribute(packet, "id").intern();
			if (!id2Name.containsKey(packetID))
				LOG.info(proName + " attempts to declare a nonexistent " + type + " packet: " + packetID + " [" + definitionDirName + "]");
			
			final String prefixHexString = L2XMLUtils.getAttribute(packet, "opcodePrefix");
			final byte[] prefix = hexStringToInternedBytes(prefixHexString);
			if (id2Prefix.put(packetID, prefix) == prefix)
				redundantMappings.add(new RedundantOpcodeMapping(prefixHexString, packetID, true));
			if (!declaredOpcodes.add(new ByteArrayWrapper(prefix)))
				LOG.info(proName + ": too many declarations for " + type + " packet '" + prefixHexString + "'!");
			if (!assignedIDs.add(packetID))
				LOG.info(proName + " maps multiple opcode sets to " + type + " packet: " + packetID + " [" + definitionDirName + "]");
			
			// automatically remove obsolete mappings
			final String previousID = prefix2ID.put(prefix, packetID);
			if (previousID != null && previousID != packetID)
			{
				final byte[] currentPrefixOfPreviousPacket = id2Prefix.get(previousID);
				if (currentPrefixOfPreviousPacket == prefix)
					id2Prefix.remove(previousID);
			}
		}
		return removedPackets;
	}
	
	/**
	 * Returns an interned version of the given byte array, iff such a byte array was already interned.
	 * 
	 * @param prefix a packet prefix
	 * @return an equivalent byte array
	 */
	public static final byte[] internedValueOf(byte[] prefix)
	{
		return INTERNED_PACKET_PREFIXES.getOrDefault(new ByteArrayWrapper(prefix), prefix);
	}
	
	private static final byte[] hexStringToInternedBytes(String bytes)
	{
		final byte[] equivalent = HexUtil.hexStringToBytes(bytes);
		final byte[] interned = INTERNED_PACKET_PREFIXES.putIfAbsent(new ByteArrayWrapper(equivalent), equivalent);
		return interned != null ? interned : equivalent;
	}
	
	private static final class RedundantOpcodeMapping
	{
		final String _prefix;
		final String _id;
		final boolean _client;
		
		RedundantOpcodeMapping(String prefix, String id, boolean client)
		{
			_prefix = prefix;
			_id = id;
			_client = client;
		}
		
		@Override
		public String toString()
		{
			return "[" + (_client ? "C" : "S") + "]" + _prefix + "->" + _id;
		}
	}
	
	private static final class ByteArrayWrapper
	{
		private final byte[] _array;
		
		ByteArrayWrapper(byte[] array)
		{
			_array = array;
		}
		
		@Override
		public boolean equals(Object o)
		{
			return o instanceof ByteArrayWrapper ? Arrays.equals(_array, ((ByteArrayWrapper)o)._array) : false;
		}
		
		@Override
		public int hashCode()
		{
			return Arrays.hashCode(_array);
		}
		
		@Override
		public String toString()
		{
			return HexUtil.bytesToHexString(_array, " ");
		}
	}
}
