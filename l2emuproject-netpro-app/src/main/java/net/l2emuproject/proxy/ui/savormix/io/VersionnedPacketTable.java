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
package net.l2emuproject.proxy.ui.savormix.io;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang3.ArrayUtils;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import net.l2emuproject.network.IGameProtocolVersion;
import net.l2emuproject.network.ILoginProtocolVersion;
import net.l2emuproject.network.IProtocolVersion;
import net.l2emuproject.network.ProtocolVersionFactory;
import net.l2emuproject.network.ProtocolVersionManager;
import net.l2emuproject.proxy.network.EndpointType;
import net.l2emuproject.proxy.network.ServiceType;
import net.l2emuproject.proxy.network.meta.IPacketTemplate;
import net.l2emuproject.proxy.network.meta.L2PacketTablePayloadEnumerator;
import net.l2emuproject.proxy.network.meta.L2PpeProvider;
import net.l2emuproject.proxy.network.meta.UserDefinedGameProtocolVersion;
import net.l2emuproject.proxy.network.meta.UserDefinedLoginProtocolVersion;
import net.l2emuproject.proxy.network.meta.container.PacketTemplateContainer;
import net.l2emuproject.proxy.network.meta.container.VersionnedPacketTemplateContainer;
import net.l2emuproject.proxy.ui.savormix.io.base.IOConstants;
import net.l2emuproject.proxy.ui.savormix.loader.LoadOption;
import net.l2emuproject.util.HexUtil;
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
	private static final Map<ByteArrayWrapper, byte[]> INTERNED_PACKET_PREFIXES = new ConcurrentHashMap<>(2_000);
	
	volatile ProtocolDefinitions _definitions;
	
	VersionnedPacketTable()
	{
		loadConfig();
		
		ProtocolVersionManager.getInstance().addLoginFactory(new ProtocolVersionFactory<ILoginProtocolVersion>()
		{
			@Override
			public ILoginProtocolVersion getByVersion(int version)
			{
				return _definitions.getLoginProtocol(version);
			}
		});
		ProtocolVersionManager.getInstance().addGameFactory(new ProtocolVersionFactory<IGameProtocolVersion>()
		{
			@Override
			public IGameProtocolVersion getByVersion(int version)
			{
				return _definitions.getGameProtocol(version);
			}
		});
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
	
	/** Reloads all protocol version and packet definitions. This is an atomic operation, as a partially loaded table will never be exposed. */
	public void reloadConfig()
	{
		loadConfig();
	}
	
	// TODO: revise
	private void loadConfig()
	{
		final Path definitionRoot = CONFIG_DIRECTORY.resolve("packets");
		
		try
		{
			LOG.info("Loading packet declarations…");
			final Map<String, String> id2NameClient = new HashMap<>(), id2NameServer = new HashMap<>();
			{
				final Node packets = L2XMLUtils.getChildNodeByName(L2XMLUtils.getXMLFile(definitionRoot.resolve("all_known_packets.xml")), "packets");
				for (final Node packet : L2XMLUtils.listNodesByNodeName(L2XMLUtils.getChildNodeByName(packets, "client"), "packet"))
					id2NameClient.put(L2XMLUtils.getAttribute(packet, "id"), L2XMLUtils.getAttribute(packet, "name"));
				for (final Node packet : L2XMLUtils.listNodesByNodeName(L2XMLUtils.getChildNodeByName(packets, "server"), "packet"))
					id2NameServer.put(L2XMLUtils.getAttribute(packet, "id"), L2XMLUtils.getAttribute(packet, "name"));
			}
			LOG.info("Total: " + id2NameClient.size() + " client packets and " + id2NameServer.size() + " server packets.");
			
			LOG.info("Loading protocol declarations…");
			final SortedMap<UserDefinedLoginProtocolVersion, String> lp = new TreeMap<>();
			final SortedMap<UserDefinedGameProtocolVersion, String> gp = new TreeMap<>();
			for (final Node pro : L2XMLUtils.listNodes(L2XMLUtils.getChildNodeByName(L2XMLUtils.getXMLFile(definitionRoot.resolve("all_known_protocols.xml")), "protocols")))
			{
				if (!pro.getNodeName().endsWith("rotocol"))
					continue;
				
				final String alias, category;
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
				
				if (L2XMLUtils.getBoolean(pro, "disabled", false))
				{
					LOG.info("Skipping " + alias + " protocol...");
					continue;
				}
				
				int[] primary = ArrayUtils.EMPTY_INT_ARRAY, secondary = ArrayUtils.EMPTY_INT_ARRAY;
				int secondaryCount = 0x1FF;
				VersionInfo version = null;
				String definitionDir = null;
				
				for (final Node opt : L2XMLUtils.listNodes(pro))
				{
					switch (opt.getNodeName())
					{
						case "version":
							version = new VersionInfo(Integer.parseInt(opt.getTextContent()), L2XMLUtils.getString(opt, "date"));
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
					if (pro.getNodeName().startsWith("authP"))
					{
						final UserDefinedLoginProtocolVersion ver = new UserDefinedLoginProtocolVersion(alias, category, version._version, df.parse(version._date).getTime());
						lp.put(ver, definitionDir);
					}
					else
					{
						final UserDefinedGameProtocolVersion ver = new UserDefinedGameProtocolVersion(alias, category, version._version, df.parse(version._date).getTime(), primary, secondaryCount,
								secondary);
						gp.put(ver, definitionDir);
					}
				}
				catch (ParseException e)
				{
					LOG.warn("Failed parsing protocol " + alias, e);
					continue;
				}
			}
			
			LOG.info("Total: " + lp.size() + " login protocols and " + gp.size() + " game protocols.");
			if (lp.isEmpty())
				throw new RuntimeException("No login protocols defined");
			if (gp.isEmpty())
				throw new RuntimeException("No game protocols defined");
			
			final Map<UserDefinedLoginProtocolVersion, Map<EndpointType, PacketTemplateContainer>> loginMap;
			final Map<UserDefinedGameProtocolVersion, Map<EndpointType, PacketTemplateContainer>> gameMap;
			if (LoadOption.DISABLE_DEFS.isNotSet())
			{
				LOG.info("Loading packet definitions…");
				
				final ByteBuffer fullStructBuf = ByteBuffer.allocate(64 << 10).order(ByteOrder.LITTLE_ENDIAN);
				loginMap = new HashMap<>();
				gameMap = new HashMap<>();
				
				{
					LOG.info("Loading login packets…");
					final Map<String, byte[]> id2PrefixClient = new HashMap<>(), id2PrefixServer = new HashMap<>();
					final Map<String, IPacketTemplate> clientPacketsByID = new HashMap<>(), serverPacketsByID = new HashMap<>();
					for (final Entry<UserDefinedLoginProtocolVersion, String> e : lp.entrySet())
					{
						int removedClientPackets = 0, removedServerPackets = 0;
						final List<RedundantOpcodeMapping> redundantMappings = new ArrayList<>(); // abuse lazy allocation
						final Set<ByteArrayWrapper> declaredOpcodes = new HashSet<>();
						final Set<String> assignedIDs = new HashSet<>();
						final String proName = e.getKey().getAlias();
						final Path root = definitionRoot.resolve(e.getValue());
						try
						{
							final Node mappings = L2XMLUtils.getChildNodeByName(L2XMLUtils.getXMLFile(root.resolve("opcode_mapping.xml")), "mapping");
							final Node clientMaps = L2XMLUtils.getChildNodeByName(mappings, "client");
							for (final Node packet : L2XMLUtils.listNodesByNodeName(clientMaps, "removedPacket"))
							{
								final String packetID = L2XMLUtils.getAttribute(packet, "id");
								if (!id2NameClient.containsKey(packetID))
									LOG.info(proName + " attempts to remove a nonexistent client packet: " + packetID + " [" + e.getValue() + "]");
								if (id2PrefixClient.remove(packetID) == null)
									LOG.info(proName + " declares a removed client packet, even though it was never mapped to any opcode: " + packetID + " [" + e.getValue() + "]");
								clientPacketsByID.remove(packetID);
								++removedClientPackets;
							}
							for (final Node packet : L2XMLUtils.listNodesByNodeName(clientMaps, "packet"))
							{
								final String packetID = L2XMLUtils.getAttribute(packet, "id");
								if (!id2NameClient.containsKey(packetID))
									LOG.info(proName + " attempts to declare a nonexistent client packet: " + packetID + " [" + e.getValue() + "]");
								
								final String prefixHexString = L2XMLUtils.getAttribute(packet, "opcodePrefix");
								final byte[] prefix = hexStringToInternedBytes(prefixHexString);
								if (id2PrefixClient.put(packetID, prefix) == prefix)
									redundantMappings.add(new RedundantOpcodeMapping(prefixHexString, packetID, true));
								if (!declaredOpcodes.add(new ByteArrayWrapper(prefix)))
									LOG.info(proName + ": too many declarations for CM " + prefixHexString + "!");
								if (!assignedIDs.add(packetID))
									LOG.info(proName + " maps multiple opcode sets to client packet: " + packetID + " [" + e.getValue() + "]");
							}
							declaredOpcodes.clear();
							assignedIDs.clear();
							final Node serverMaps = L2XMLUtils.getChildNodeByName(mappings, "server");
							for (final Node packet : L2XMLUtils.listNodesByNodeName(serverMaps, "removedPacket"))
							{
								final String packetID = L2XMLUtils.getAttribute(packet, "id");
								if (!id2NameServer.containsKey(packetID))
									LOG.info(proName + " attempts to remove a nonexistent server packet: " + packetID + " [" + e.getValue() + "]");
								if (id2PrefixServer.remove(packetID) == null)
									LOG.info(proName + " declares a removed server packet, even though it was never mapped to any opcode: " + packetID + " [" + e.getValue() + "]");
								serverPacketsByID.remove(packetID);
								++removedServerPackets;
							}
							for (final Node packet : L2XMLUtils.listNodesByNodeName(serverMaps, "packet"))
							{
								final String packetID = L2XMLUtils.getAttribute(packet, "id");
								if (!id2NameServer.containsKey(packetID))
									LOG.info(proName + " attempts to declare a nonexistent server packet: " + packetID + " [" + e.getValue() + "]");
								
								final String prefixHexString = L2XMLUtils.getAttribute(packet, "opcodePrefix");
								final byte[] prefix = hexStringToInternedBytes(prefixHexString);
								if (id2PrefixServer.put(packetID, prefix) == prefix)
									redundantMappings.add(new RedundantOpcodeMapping(prefixHexString, packetID, false));
								if (!declaredOpcodes.add(new ByteArrayWrapper(prefix)))
									LOG.info(proName + ": too many declarations for SM " + prefixHexString + "!");
								if (!assignedIDs.add(packetID))
									LOG.info(proName + " maps multiple opcode sets to server packet: " + packetID + " [" + e.getValue() + "]");
							}
							declaredOpcodes.clear();
							assignedIDs.clear();
						}
						catch (FileNotFoundException ex)
						{
							// with incremental loading, this is OK now
							// LOG.warn(e.getKey() + ": " + ex.getMessage());
						}
						catch (Exception ex)
						{
							LOG.fatal(e.getKey(), ex);
							continue;
						}
						
						LOG.info(proName + " declares " + id2PrefixClient.size() + " client and " + id2PrefixServer.size() + " server packets.");
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
							EndpointPacketLoader loader = new EndpointPacketLoader(id2PrefixClient, id2NameClient, clientPacketsByID, fullStructBuf);
							try
							{
								Files.walkFileTree(root.resolve("client"), loader = new EndpointPacketLoader(id2PrefixClient, id2NameClient, clientPacketsByID, fullStructBuf));
								LOG.info(proName + ": [" + loader.getAdded() + " new, " + loader.getUpdated() + " updated, " + removedClientPackets + " removed client packets].");
							}
							catch (NoSuchFileException ex)
							{
								// looks too verbose to me
								// LOG.warn(e.getKey() + ": missing " + ex.getMessage());
							}
							finally
							{
								clientPackets = loader.getPackets();
							}
							loader = new EndpointPacketLoader(id2PrefixServer, id2NameServer, serverPacketsByID, fullStructBuf);
							try
							{
								Files.walkFileTree(root.resolve("server"), loader);
								LOG.info(proName + ": [" + loader.getAdded() + " new, " + loader.getUpdated() + " updated, " + removedServerPackets + " removed server packets].");
							}
							catch (NoSuchFileException ex)
							{
								// looks too verbose to me
								// LOG.warn(e.getKey() + ": missing " + ex.getMessage());
							}
							finally
							{
								serverPackets = loader.getPackets();
							}
						}
						
						final Map<EndpointType, PacketTemplateContainer> endpointMap = new EnumMap<>(EndpointType.class);
						endpointMap.put(EndpointType.CLIENT, new PacketTemplateContainer(clientPackets));
						endpointMap.put(EndpointType.SERVER, new PacketTemplateContainer(serverPackets));
						loginMap.put(e.getKey(), endpointMap);
					}
				}
				
				{
					LOG.info("Loading game packets…");
					final Map<String, byte[]> id2PrefixClient = new HashMap<>(), id2PrefixServer = new HashMap<>();
					final Map<String, IPacketTemplate> clientPacketsByID = new HashMap<>(), serverPacketsByID = new HashMap<>();
					for (final Iterator<Entry<UserDefinedGameProtocolVersion, String>> it = gp.entrySet().iterator(); it.hasNext();)
					{
						int removedClientPackets = 0, removedServerPackets = 0;
						final List<RedundantOpcodeMapping> redundantMappings = new ArrayList<>(); // abuse lazy allocation
						final Set<ByteArrayWrapper> declaredOpcodes = new HashSet<>();
						final Set<String> assignedIDs = new HashSet<>();
						final Entry<UserDefinedGameProtocolVersion, String> e = it.next();
						final String proName = e.getKey().getAlias();
						final Path root = definitionRoot.resolve(e.getValue());
						
						// an opcode mapping is mandatory
						try
						{
							final Node mappings = L2XMLUtils.getChildNodeByName(L2XMLUtils.getXMLFile(root.resolve("opcode_mapping.xml")), "mapping");
							final Node clientMaps = L2XMLUtils.getChildNodeByName(mappings, "client");
							for (final Node packet : L2XMLUtils.listNodesByNodeName(clientMaps, "removedPacket"))
							{
								final String packetID = L2XMLUtils.getAttribute(packet, "id");
								if (!id2NameClient.containsKey(packetID))
									LOG.info(proName + " attempts to remove a nonexistent client packet: " + packetID + " [" + e.getValue() + "]");
								if (id2PrefixClient.remove(packetID) == null)
									LOG.info(proName + " declares a removed client packet, even though it was never mapped to any opcode: " + packetID + " [" + e.getValue() + "]");
								clientPacketsByID.remove(packetID);
								assignedIDs.add(packetID);
								++removedClientPackets;
							}
							for (final Node packet : L2XMLUtils.listNodesByNodeName(L2XMLUtils.getChildNodeByName(mappings, "client"), "packet"))
							{
								final String packetID = L2XMLUtils.getAttribute(packet, "id");
								if (!id2NameClient.containsKey(packetID))
									LOG.info(proName + " attempts to declare a nonexistent client packet: " + packetID + " [" + e.getValue() + "]");
								
								final String prefixHexString = L2XMLUtils.getAttribute(packet, "opcodePrefix");
								final byte[] prefix = hexStringToInternedBytes(prefixHexString);
								if (id2PrefixClient.put(packetID, prefix) == prefix)
									redundantMappings.add(new RedundantOpcodeMapping(prefixHexString, packetID, true));
								if (!declaredOpcodes.add(new ByteArrayWrapper(prefix)))
									LOG.info(proName + ": too many declarations for CM " + prefixHexString + "!");
								if (!assignedIDs.add(packetID))
									LOG.info(proName + " maps multiple opcode sets to client packet: " + packetID + " [" + e.getValue() + "]");
							}
							declaredOpcodes.clear();
							assignedIDs.clear();
							final Node serverMaps = L2XMLUtils.getChildNodeByName(mappings, "server");
							for (final Node packet : L2XMLUtils.listNodesByNodeName(serverMaps, "removedPacket"))
							{
								final String packetID = L2XMLUtils.getAttribute(packet, "id");
								if (!id2NameServer.containsKey(packetID))
									LOG.info(proName + " attempts to remove a nonexistent server packet: " + packetID + " [" + e.getValue() + "]");
								if (id2PrefixServer.remove(packetID) == null)
									LOG.info(proName + " declares a removed server packet, even though it was never mapped to any opcode: " + packetID + " [" + e.getValue() + "]");
								serverPacketsByID.remove(packetID);
								++removedServerPackets;
							}
							for (final Node packet : L2XMLUtils.listNodesByNodeName(L2XMLUtils.getChildNodeByName(mappings, "server"), "packet"))
							{
								final String packetID = L2XMLUtils.getAttribute(packet, "id");
								if (!id2NameServer.containsKey(packetID))
									LOG.info(proName + " attempts to declare a nonexistent server packet: " + packetID + " [" + e.getValue() + "]");
								
								final String prefixHexString = L2XMLUtils.getAttribute(packet, "opcodePrefix");
								final byte[] prefix = hexStringToInternedBytes(prefixHexString);
								if (id2PrefixServer.put(packetID, prefix) == prefix)
									redundantMappings.add(new RedundantOpcodeMapping(prefixHexString, packetID, false));
								if (!declaredOpcodes.add(new ByteArrayWrapper(prefix)))
									LOG.info(proName + ": too many declarations for SM " + prefixHexString + "!");
								if (!assignedIDs.add(packetID))
									LOG.info(proName + " maps multiple opcode sets to server packet: " + packetID + " [" + e.getValue() + "]");
							}
							declaredOpcodes.clear();
							assignedIDs.clear();
						}
						catch (FileNotFoundException ex)
						{
							// with incremental loading, this is OK now
							// it.remove();
							// LOG.warn(e.getKey() + ": " + ex.getMessage());
						}
						catch (Exception ex)
						{
							it.remove();
							LOG.fatal(e.getKey(), ex);
							continue;
						}
						
						// but packets are not, as there might not be any changes between protocol revisions
						LOG.info(proName + " declares " + id2PrefixClient.size() + " client and " + id2PrefixServer.size() + " server packets.");
						if (!redundantMappings.isEmpty())
						{
							LOG.info(proName + " redundantly declares " + redundantMappings);
							/*
							try
							{
								boolean clientEntries = true;
								final List<String> lines = Files.readAllLines(root.resolve("opcode_mapping.xml"));
								for (final Iterator<String> it2 = lines.iterator(); it2.hasNext();)
								{
									final String line = it2.next();
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
											it2.remove();
									}
								}
								Files.write(root.resolve("opcode_mapping.xml"), lines);
								Files.delete(root.resolve("opcode_mapping_autofix.xml"));
							}
							catch (IOException ex)
							{
								// ignore
							}
							*/
						}
						
						Set<IPacketTemplate> clientPackets = Collections.emptySet(), serverPackets = Collections.emptySet();
						{
							EndpointPacketLoader loader = new EndpointPacketLoader(id2PrefixClient, id2NameClient, clientPacketsByID, fullStructBuf);
							try
							{
								Files.walkFileTree(root.resolve("client"), loader);
								LOG.info(proName + ": [" + loader.getAdded() + " new, " + loader.getUpdated() + " updated, " + removedClientPackets + " removed client packets].");
							}
							catch (NoSuchFileException ex)
							{
								// looks too verbose to me
								// LOG.warn(e.getKey() + ": missing " + ex.getMessage());
							}
							finally
							{
								clientPackets = loader.getPackets();
							}
							loader = new EndpointPacketLoader(id2PrefixServer, id2NameServer, serverPacketsByID, fullStructBuf);
							try
							{
								Files.walkFileTree(root.resolve("server"), loader);
								LOG.info(proName + ": [" + loader.getAdded() + " new, " + loader.getUpdated() + " updated, " + removedServerPackets + " removed server packets].");
							}
							catch (NoSuchFileException ex)
							{
								// looks too verbose to me
								// LOG.warn(e.getKey() + ": missing " + ex.getMessage());
							}
							finally
							{
								serverPackets = loader.getPackets();
							}
						}
						
						final Map<EndpointType, PacketTemplateContainer> endpointMap = new EnumMap<>(EndpointType.class);
						endpointMap.put(EndpointType.CLIENT, new PacketTemplateContainer(clientPackets));
						endpointMap.put(EndpointType.SERVER, new PacketTemplateContainer(serverPackets));
						gameMap.put(e.getKey(), endpointMap);
					}
				}
			}
			else
			{
				{
					final Map<EndpointType, PacketTemplateContainer> endpointMap = new EnumMap<>(EndpointType.class);
					endpointMap.put(EndpointType.CLIENT, new PacketTemplateContainer());
					endpointMap.put(EndpointType.SERVER, new PacketTemplateContainer());
					loginMap = Collections.singletonMap(lp.lastKey(), endpointMap);
				}
				{
					final Map<EndpointType, PacketTemplateContainer> endpointMap = new EnumMap<>(EndpointType.class);
					endpointMap.put(EndpointType.CLIENT, new PacketTemplateContainer());
					endpointMap.put(EndpointType.SERVER, new PacketTemplateContainer());
					gameMap = Collections.singletonMap(gp.lastKey(), endpointMap);
				}
			}
			
			_definitions = new ProtocolDefinitions(lp.keySet(), gp.keySet(), new VersionnedPacketTemplateContainer<UserDefinedLoginProtocolVersion>(loginMap, lp.lastKey()),
					new VersionnedPacketTemplateContainer<UserDefinedGameProtocolVersion>(gameMap, gp.lastKey()));
			
			if (L2PpeProvider.getPacketPayloadEnumerator() == null)
				L2PpeProvider.initialize(new L2PacketTablePayloadEnumerator());
		}
		catch (IOException | ParserConfigurationException | SAXException e)
		{
			LOG.fatal("Cannot load protocol/packet definitions", e);
		}
	}
	
	private static final byte[] hexStringToInternedBytes(String bytes)
	{
		final byte[] equivalent = HexUtil.hexStringToBytes(bytes);
		final byte[] interned = INTERNED_PACKET_PREFIXES.putIfAbsent(new ByteArrayWrapper(equivalent), equivalent);
		return interned != null ? interned : equivalent;
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
