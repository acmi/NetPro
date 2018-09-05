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
package net.l2emuproject.proxy.io.definitions;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Stream;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang3.ArrayUtils;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import net.l2emuproject.network.protocol.IGameProtocolVersion;
import net.l2emuproject.network.protocol.ILoginProtocolVersion;
import net.l2emuproject.network.protocol.IProtocolVersion;
import net.l2emuproject.network.protocol.ProtocolVersionFactory;
import net.l2emuproject.network.protocol.ProtocolVersionManager;
import net.l2emuproject.network.security.OpcodeTableShuffleType;
import net.l2emuproject.proxy.StartupOption;
import net.l2emuproject.proxy.io.IOConstants;
import net.l2emuproject.proxy.network.EndpointType;
import net.l2emuproject.proxy.network.ServiceType;
import net.l2emuproject.proxy.network.meta.FieldValueTranslator;
import net.l2emuproject.proxy.network.meta.IPacketTemplate;
import net.l2emuproject.proxy.network.meta.L2PacketTablePayloadEnumerator;
import net.l2emuproject.proxy.network.meta.L2PpeProvider;
import net.l2emuproject.proxy.network.meta.ProtocolTreeNode;
import net.l2emuproject.proxy.network.meta.UserDefinedGameProtocolVersion;
import net.l2emuproject.proxy.network.meta.UserDefinedLoginProtocolVersion;
import net.l2emuproject.proxy.network.meta.UserDefinedProtocolVersion;
import net.l2emuproject.proxy.network.meta.container.PacketPrefixResolver;
import net.l2emuproject.proxy.network.meta.container.VersionnedPacketTemplateContainer;
import net.l2emuproject.util.L2XMLUtils;
import net.l2emuproject.util.logging.L2Logger;

/**
 * Packet definition configuration and load order management.
 * 
 * @author savormix
 */
public class VersionnedPacketTable implements IOConstants
{
	private static final L2Logger LOG = L2Logger.getLogger(VersionnedPacketTable.class);
	
	volatile ProtocolDefinitions _definitions;
	
	VersionnedPacketTable()
	{
		loadConfig();
		
		ProtocolVersionManager.getInstance().addLoginFactory(new ProtocolVersionFactory<ILoginProtocolVersion>(){
			@Override
			public ILoginProtocolVersion getByVersion(int version, Set<String> altModes)
			{
				return _definitions.getLoginProtocol(version, altModes);
			}
		});
		ProtocolVersionManager.getInstance().addGameFactory(new ProtocolVersionFactory<IGameProtocolVersion>(){
			@Override
			public IGameProtocolVersion getByVersion(int version, Set<String> altModes)
			{
				return _definitions.getGameProtocol(version, altModes);
			}
		});
		System.setProperty(FieldValueTranslator.PROPERTY_PROTOCOLS_LOADED, Boolean.TRUE.toString());
	}
	
	/**
	 * Returns all protocols that have been defined in the associated XML file for a specific service.
	 * 
	 * @param type login/game
	 * @return known protocol versions
	 */
	public Collection<? extends IProtocolVersion> getKnownProtocols(ServiceType type)
	{
		return _definitions.getKnownProtocols(type);
	}
	
	/**
	 * Returns a packet definition template for the given packet in the given context.
	 * It is assumed that a packet fills the given byte array.
	 * 
	 * @param protocol network protocol version
	 * @param endpoint packet author: client/server
	 * @param packet packet body
	 * @return packet template
	 */
	public IPacketTemplate getTemplate(IProtocolVersion protocol, EndpointType endpoint, byte[] packet)
	{
		return _definitions.getTemplate(protocol, endpoint, packet);
	}
	
	/**
	 * Returns a packet definition template for the given packet in the given context.
	 * 
	 * @param protocol network protocol version
	 * @param endpoint packet author: client/server
	 * @param packet packet body buffer
	 * @param packetOffset offset to packet body in buffer
	 * @param packetLength packet body length
	 * @return packet template
	 */
	public IPacketTemplate getTemplate(IProtocolVersion protocol, EndpointType endpoint, byte[] packet, int packetOffset, int packetLength)
	{
		return _definitions.getTemplate(protocol, endpoint, packet, packetOffset, packetLength);
	}
	
	/**
	 * Returns a packet definition template for the given packet in the given context.
	 * It is assumed that the packet body is between current buffer's position and limit.
	 * 
	 * @param protocol network protocol version
	 * @param endpoint packet author: client/server
	 * @param packet packet body buffer
	 * @return packet template
	 */
	public IPacketTemplate getTemplate(IProtocolVersion protocol, EndpointType endpoint, ByteBuffer packet)
	{
		return _definitions.getTemplate(protocol, endpoint, packet);
	}
	
	private Stream<IPacketTemplate> getTemplates(IProtocolVersion protocol, EndpointType endpoint)
	{
		return _definitions.getTemplates(protocol, endpoint);
	}
	
	/**
	 * Returns all packet definitions contained in an associated XML file, based on given context.
	 * 
	 * @param protocol network protocol version
	 * @param endpoint client/server
	 * @return explicitly defined packet templates
	 */
	public Stream<IPacketTemplate> getKnownTemplates(IProtocolVersion protocol, EndpointType endpoint)
	{
		return getTemplates(protocol, endpoint).filter(IPacketTemplate::isDefined);
	}
	
	/**
	 * Returns all currently known packet definitions for the given protocol and endpoint. Includes generated packet templates to represent encountered unknown packets.
	 * 
	 * @param protocol network protocol version
	 * @param endpoint client/server
	 * @return all packet templates at current point of time
	 */
	public Stream<IPacketTemplate> getCurrentTemplates(IProtocolVersion protocol, EndpointType endpoint)
	{
		return getTemplates(protocol, endpoint);
	}
	
	/** Reloads all protocol version and packet definitions. This is an atomic operation, as a partially loaded table will never be exposed. */
	public void reloadConfig()
	{
		loadConfig();
	}
	
	// TODO: revise
	private synchronized void loadConfig()
	{
		final Path definitionRoot = CONFIG_DIRECTORY.resolve("packets");
		
		try
		{
			LOG.info("Loading packet declarations…");
			final Map<String, String> id2NameClient = new IdentityHashMap<>(), id2NameServer = new IdentityHashMap<>();
			{
				final Node packets = L2XMLUtils.getChildNodeByName(L2XMLUtils.getXMLFile(definitionRoot.resolve("all_known_packets.xml")), "packets");
				for (final Node packet : L2XMLUtils.listNodesByNodeName(L2XMLUtils.getChildNodeByName(packets, "client"), "packet"))
					id2NameClient.put(L2XMLUtils.getAttribute(packet, "id").intern(), L2XMLUtils.getAttribute(packet, "name").intern());
				for (final Node packet : L2XMLUtils.listNodesByNodeName(L2XMLUtils.getChildNodeByName(packets, "server"), "packet"))
					id2NameServer.put(L2XMLUtils.getAttribute(packet, "id").intern(), L2XMLUtils.getAttribute(packet, "name").intern());
			}
			LOG.info("Total: " + id2NameClient.size() + " client packets and " + id2NameServer.size() + " server packets.");
			
			LOG.info("Loading protocol declarations…");
			int totalDeclaredLP = 0, disabledLP = 0, orphanedLP = 0;
			int totalDeclaredGP = 0, disabledGP = 0, orphanedGP = 0;
			final SortedMap<UserDefinedLoginProtocolVersion, String> lp = new TreeMap<>();
			final SortedMap<UserDefinedGameProtocolVersion, String> gp = new TreeMap<>();
			final Map<String, UserDefinedLoginProtocolVersion> id2lp = new IdentityHashMap<>();
			final Map<String, UserDefinedGameProtocolVersion> id2gp = new IdentityHashMap<>();
			final Map<String, String> id2lpPID = new IdentityHashMap<>(), id2gpPID = new IdentityHashMap<>();
			for (final Node pro : L2XMLUtils.listNodes(L2XMLUtils.getChildNodeByName(L2XMLUtils.getXMLFile(definitionRoot.resolve("all_known_protocols.xml")), "protocols")))
			{
				if (!pro.getNodeName().endsWith("rotocol"))
					continue;
				
				final boolean auth = pro.getNodeName().startsWith("authP");
				
				final String id, alias, category, parentID;
				final String ownID = L2XMLUtils.getNodeAttributeStringValue(pro, "id", null);
				if (ownID == null)
				{
					LOG.warn("Invalid definition config encountered. ID is missing.");
					continue;
				}
				id = ownID.intern();
				alias = L2XMLUtils.getNodeAttributeStringValue(pro, "alias", null);
				if (alias == null)
				{
					LOG.warn("Invalid definition config encountered. Alias is missing.");
					continue;
				}
				category = L2XMLUtils.getNodeAttributeStringValue(pro, "category", null);
				if (category == null)
				{
					LOG.warn("Invalid definition config encountered, category is missing: " + alias);
					continue;
				}
				final String pid = L2XMLUtils.getNodeAttributeStringValue(pro, "parentID", null);
				parentID = pid != null ? pid.intern() : null;
				
				if (auth)
					totalDeclaredLP += 1;
				else
					totalDeclaredGP += 1;
				
				if (L2XMLUtils.getBoolean(pro, "disabled", false))
				{
					LOG.info("Skipping " + alias + " protocol...");
					if (auth)
						disabledLP += 1;
					else
						disabledGP += 1;
					continue;
				}
				
				OpcodeTableShuffleType shuffleMode = OpcodeTableShuffleType.NONE;
				int[] primary = ArrayUtils.EMPTY_INT_ARRAY, secondary = ArrayUtils.EMPTY_INT_ARRAY;
				int secondaryCount = 0x02_FF;
				VersionInfo version = null;
				Set<String> altModes = Collections.emptySet();
				String definitionDir = null;
				
				for (final Node opt : L2XMLUtils.listNodes(pro))
				{
					switch (opt.getNodeName())
					{
						case "version":
							version = new VersionInfo(Integer.parseInt(opt.getTextContent()), L2XMLUtils.getString(opt, "date"));
							break;
						case "alternativeModes":
							for (final Node mode : L2XMLUtils.listNodesByNodeName(opt, "mode"))
							{
								if (altModes.isEmpty())
									altModes = new LinkedHashSet<>();
								altModes.add(mode.getTextContent().trim().intern());
							}
							break;
						case "shuffle":
							shuffleMode = Enum.valueOf(OpcodeTableShuffleType.class, opt.getTextContent());
							break;
						case "primary":
							for (final Node op : L2XMLUtils.listNodesByNodeName(opt, "constant"))
								primary = ArrayUtils.add(primary, Integer.decode(op.getTextContent()));
							break;
						case "secondary":
							secondaryCount = L2XMLUtils.getNodeAttributeIntValue(opt, "count", -1);
							for (final Node op : L2XMLUtils.listNodesByNodeName(opt, "constant"))
								secondary = ArrayUtils.add(secondary, Integer.decode(op.getTextContent()));
							break;
						case "definitions":
							definitionDir = L2XMLUtils.getString(opt, "dir");
							break;
					}
				}
				
				if (version == null)
				{
					LOG.warn("Invalid definition config encountered. Revision (version) number not specified.");
					continue;
				}
				
				try
				{
					final SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
					if (auth)
					{
						final UserDefinedLoginProtocolVersion ver = new UserDefinedLoginProtocolVersion(alias, category, version._version, df.parse(version._date).getTime());
						lp.put(ver, definitionDir);
						id2lp.put(id, ver);
						id2lpPID.put(id, parentID);
					}
					else
					{
						final UserDefinedGameProtocolVersion ver = new UserDefinedGameProtocolVersion(alias, category, version._version, altModes, df.parse(version._date).getTime(), shuffleMode,
								primary,
								secondaryCount, secondary);
						gp.put(ver, definitionDir);
						id2gp.put(id, ver);
						id2gpPID.put(id, parentID);
					}
				}
				catch (final ParseException e)
				{
					LOG.warn("Failed parsing protocol " + alias, e);
					continue;
				}
			}
			
			orphanedLP = removeOrphans(id2lp, id2lpPID, lp);
			orphanedGP = removeOrphans(id2gp, id2gpPID, gp);
			
			LOG.info("Loaded " + lp.size() + "/" + totalDeclaredLP + " login protocols (" + disabledLP + " disabled, " + orphanedLP + " orphaned) and " + gp.size() + "/" + totalDeclaredGP
					+ " game protocols(" + disabledGP + " disabled, " + orphanedGP + " orphaned).");
			if (lp.isEmpty())
				throw new RuntimeException("No login protocols defined");
			if (gp.isEmpty())
				throw new RuntimeException("No game protocols defined");
			
			final Map<UserDefinedLoginProtocolVersion, Map<EndpointType, PacketPrefixResolver>> loginMap;
			final Map<UserDefinedGameProtocolVersion, Map<EndpointType, PacketPrefixResolver>> gameMap;
			if (StartupOption.DISABLE_DEFS.isNotSet())
			{
				LOG.info("Loading packet definitions…");
				
				loginMap = new HashMap<>();
				gameMap = new HashMap<>();
				
				{
					LOG.info("Loading login packets…");
					final ProtocolTreeNode<UserDefinedLoginProtocolVersion> lpt = ProtocolTreeNode.fromMap(id2lpPID, id2lp);
					for (final ProtocolTreeNode<UserDefinedLoginProtocolVersion> root : lpt.getChildren())
						loadToMap(new VersionnedPacketLoader<>(id2NameClient, id2NameServer), root, lp, loginMap);
				}
				
				{
					LOG.info("Loading game packets…");
					final ProtocolTreeNode<UserDefinedGameProtocolVersion> gpt = ProtocolTreeNode.fromMap(id2gpPID, id2gp);
					for (final ProtocolTreeNode<UserDefinedGameProtocolVersion> root : gpt.getChildren())
						loadToMap(new VersionnedPacketLoader<>(id2NameClient, id2NameServer), root, gp, gameMap);
				}
			}
			else
			{
				{
					final Map<EndpointType, PacketPrefixResolver> endpointMap = new EnumMap<>(EndpointType.class);
					endpointMap.put(EndpointType.CLIENT, new PacketPrefixResolver(Collections.emptySet()));
					endpointMap.put(EndpointType.SERVER, new PacketPrefixResolver(Collections.emptySet()));
					loginMap = Collections.singletonMap(lp.lastKey(), endpointMap);
				}
				{
					final Map<EndpointType, PacketPrefixResolver> endpointMap = new EnumMap<>(EndpointType.class);
					endpointMap.put(EndpointType.CLIENT, new PacketPrefixResolver(Collections.emptySet()));
					endpointMap.put(EndpointType.SERVER, new PacketPrefixResolver(Collections.emptySet()));
					gameMap = Collections.singletonMap(gp.lastKey(), endpointMap);
				}
			}
			
			_definitions = new ProtocolDefinitions(lp.keySet(), gp.keySet(), new VersionnedPacketTemplateContainer<>(loginMap), new VersionnedPacketTemplateContainer<>(gameMap));
			
			if (L2PpeProvider.getPacketPayloadEnumerator() == null)
				L2PpeProvider.initialize(new L2PacketTablePayloadEnumerator());
		}
		catch (IOException | ParserConfigurationException | SAXException e)
		{
			LOG.fatal("Cannot load protocol/packet definitions", e);
		}
	}
	
	private static <T extends UserDefinedProtocolVersion> int removeOrphans(Map<String, T> id2Protocol, Map<String, String> id2pid, Map<T, String> loaded)
	{
		int result = 0;
		boolean removed;
		do
		{
			removed = false;
			for (final Entry<String, String> e : id2pid.entrySet())
			{
				final String pid = e.getValue();
				if (pid == null)
					continue;
				if (!id2Protocol.containsKey(pid))
				{
					final String id = e.getKey();
					LOG.info("Missing parent: " + pid + ", removing protocol " + id);
					final T loadedProtocol = id2Protocol.remove(id);
					loaded.remove(loadedProtocol);
					id2pid.remove(id);
					result += 1;
					removed = true;
					break;
				}
			}
		}
		while (removed);
		return result;
	}
	
	private static <T extends UserDefinedProtocolVersion> void loadToMap(VersionnedPacketLoader<T> loader, ProtocolTreeNode<T> node, Map<T, String> protocol2DefDir,
			Map<T, Map<EndpointType, PacketPrefixResolver>> map) throws IOException
	{
		final T protocol = Objects.requireNonNull(node.getProtocol());
		final Map<EndpointType, PacketPrefixResolver> endpointMap = loader.load(protocol, Objects.requireNonNull(protocol2DefDir.get(protocol)));
		if (endpointMap == null)
			return;
		map.put(protocol, endpointMap);
		for (final ProtocolTreeNode<T> child : node.getChildren())
			loadToMap(new VersionnedPacketLoader<>(loader), child, protocol2DefDir, map);
	}
	
	private static final class VersionInfo
	{
		final int _version;
		final String _date;
		
		VersionInfo(int version, String date)
		{
			_version = version;
			_date = date;
		}
		
		@Override
		public int hashCode()
		{
			return _version;
		}
		
		@Override
		public boolean equals(Object o)
		{
			if (o instanceof VersionInfo)
				return _version == ((VersionInfo)o)._version;
			
			return false;
		}
		
		@Override
		public String toString()
		{
			return _version + " " + _date;
		}
	}
	
	/**
	 * Returns a singleton instance of this type.
	 * 
	 * @return an instance of this class
	 */
	public static final VersionnedPacketTable getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static final class SingletonHolder
	{
		static final VersionnedPacketTable INSTANCE = new VersionnedPacketTable();
	}
}
