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
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Consumer;

import net.l2emuproject.lang.management.ShutdownManager;
import net.l2emuproject.network.protocol.IProtocolVersion;
import net.l2emuproject.proxy.io.IOConstants;
import net.l2emuproject.proxy.io.definitions.VersionnedPacketTable;
import net.l2emuproject.proxy.io.packethiding.PacketHidingConfigFileUtils;
import net.l2emuproject.proxy.network.ServiceType;
import net.l2emuproject.util.logging.L2Logger;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleObjectProperty;

/**
 * Manages protocol-based packet hiding configurations and their persistence (both manual and automatic).
 * 
 * @author _dev_
 */
public final class ProtocolPacketHidingManager implements IOConstants
{
	/** Allows custom handling of exceptions that occur when loading default protocol packet hiding configurations. */
	public static Consumer<Map<Path, Exception>> AUTOMATIC_LOADING_EXCEPTION_HANDLER = null;
	
	private static final L2Logger LOG = L2Logger.getLogger(ProtocolPacketHidingManager.class);
	
	private static final String AUTH_PREFIX = "auth_", GAME_PREFIX = "l2_";
	
	// should only be interacted from the JavaFX thread
	private final Map<IProtocolVersion, ObjectProperty<IPacketHidingConfig>> _configurations;
	private final Set<IProtocolVersion> _pendingSave;
	
	ProtocolPacketHidingManager()
	{
		_configurations = new HashMap<>();
		_pendingSave = new HashSet<>();
		
		if (AUTOMATIC_LOADING_EXCEPTION_HANDLER != null)
			AUTOMATIC_LOADING_EXCEPTION_HANDLER.accept(autoLoad());
		
		ShutdownManager.addShutdownHook(() ->
		{
			for (final IProtocolVersion protocol : _pendingSave)
			{
				final ObjectProperty<IPacketHidingConfig> cfg = _configurations.get(protocol);
				if (cfg != null)
				{
					try
					{
						PacketHidingConfigFileUtils.saveHidingConfiguration(PROTOCOL_PACKET_HIDING_DIR
								.resolve((ServiceType.valueOf(protocol).isLogin() ? AUTH_PREFIX : GAME_PREFIX) + protocol.getVersion() + "." + PROTOCOL_PACKET_HIDING_EXTENSION), cfg.get());
					}
					catch (IOException e)
					{
						LOG.error("Config autosave for " + protocol, e);
					}
					catch (InterruptedException e)
					{
						break;
					}
				}
			}
		});
	}
	
	/**
	 * Returns an up-to-date user-managed packet hiding configuration for the given protocol version.
	 * 
	 * @param protocol protocol version
	 * @return packet hiding configuration
	 */
	public ObjectProperty<IPacketHidingConfig> getHidingConfiguration(IProtocolVersion protocol)
	{
		final ObjectProperty<IPacketHidingConfig> config = _configurations.get(protocol);
		return config != null ? config : _configurations.computeIfAbsent(protocol, p -> new SimpleObjectProperty<>(newHidingConfig()));
	}
	
	/**
	 * Marks the packet hiding configuration associated with the given protocol as modified.
	 * 
	 * @param protocol protocol version
	 */
	public void markModified(IProtocolVersion protocol)
	{
		if (_configurations.containsKey(protocol))
			_pendingSave.add(protocol);
	}
	
	private Map<Path, Exception> autoLoad()
	{
		final Map<Path, Exception> report = new TreeMap<Path, Exception>();
		
		final Map<ServiceType, String> type2Prefix = new EnumMap<>(ServiceType.class);
		type2Prefix.put(ServiceType.LOGIN, AUTH_PREFIX);
		type2Prefix.put(ServiceType.GAME, GAME_PREFIX);
		
		final Map<IProtocolVersion, ReadOnlyObjectWrapper<IPacketHidingConfig>> configurations = new HashMap<>();
		for (final Entry<ServiceType, String> e : type2Prefix.entrySet())
		{
			int totalConfigurations = 0;
			for (final IProtocolVersion protocol : VersionnedPacketTable.getInstance().getKnownProtocols(e.getKey()))
			{
				final Path file = PROTOCOL_PACKET_HIDING_DIR.resolve(e.getValue() + protocol.getVersion() + "." + PROTOCOL_PACKET_HIDING_EXTENSION);
				try
				{
					configurations.put(protocol, new ReadOnlyObjectWrapper<>(PacketHidingConfigFileUtils.readHidingConfiguration(file)));
					++totalConfigurations;
				}
				catch (FileNotFoundException | NoSuchFileException ex)
				{
					// nothing to be loaded automatically
					configurations.put(protocol, new ReadOnlyObjectWrapper<>(newHidingConfig()));
				}
				catch (Exception ex)
				{
					report.put(file, ex);
				}
			}
			LOG.info("Loaded packet hiding configurations for " + totalConfigurations + " " + e.getKey().toString().toLowerCase(Locale.ENGLISH) + " protocol versions.");
		}
		_configurations.putAll(configurations);
		
		return Collections.unmodifiableMap(report);
	}
	
	private IPacketHidingConfig newHidingConfig()
	{
		return new PacketHidingConfig(new HashSet<>(Collections.emptySet()), new HashSet<>(Collections.emptySet()));
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
