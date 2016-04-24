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

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

import net.l2emuproject.proxy.network.EndpointType;
import net.l2emuproject.proxy.network.meta.IPacketTemplate;

/**
 * A reference implementation of {@link IPacketHidingConfig}.
 * 
 * @author _dev_
 */
public class PacketHidingConfig implements IPacketHidingConfig
{
	private final IPacketHidingConfig _parentConfig;
	private final Map<EndpointType, Set<byte[]>> _configuration;
	
	/**
	 * Creates a packet display configuration.
	 * 
	 * @param parentConfig inherited configuration
	 * @param clientPacketPrefixes client packet opcodes
	 * @param serverPacketPrefixes server packet opcodes
	 */
	public PacketHidingConfig(IPacketHidingConfig parentConfig, Set<byte[]> clientPacketPrefixes, Set<byte[]> serverPacketPrefixes)
	{
		_parentConfig = parentConfig;
		_configuration = new EnumMap<>(EndpointType.class);
		_configuration.put(EndpointType.CLIENT, clientPacketPrefixes);
		_configuration.put(EndpointType.SERVER, serverPacketPrefixes);
	}
	
	/**
	 * Creates a packet display configuration.
	 * 
	 * @param clientPacketPrefixes client packet opcodes
	 * @param serverPacketPrefixes server packet opcodes
	 */
	public PacketHidingConfig(Set<byte[]> clientPacketPrefixes, Set<byte[]> serverPacketPrefixes)
	{
		this(AllPackets.CONFIG, clientPacketPrefixes, serverPacketPrefixes);
	}
	
	@Override
	public boolean isHidden(EndpointType senderType, IPacketTemplate packetType)
	{
		return _configuration.getOrDefault(senderType, Collections.emptySet()).contains(packetType.getPrefix()) || _parentConfig.isHidden(senderType, packetType);
	}
	
	@Override
	public void setHidden(EndpointType senderType, IPacketTemplate packetType)
	{
		_configuration.get(senderType).add(packetType.getPrefix());
	}
	
	@Override
	public void setVisible(EndpointType senderType, IPacketTemplate packetType)
	{
		_configuration.get(senderType).remove(packetType.getPrefix());
	}
	
	@Override
	public Map<EndpointType, Set<byte[]>> getSaveableFormat()
	{
		return _configuration;
	}
	
	private static final class AllPackets implements IPacketHidingConfig
	{
		static final AllPackets CONFIG = new AllPackets();
		
		@Override
		public boolean isHidden(EndpointType senderType, IPacketTemplate packetType)
		{
			return false;
		}
		
		@Override
		public void setHidden(EndpointType senderType, IPacketTemplate packetType)
		{
			throw new UnsupportedOperationException();
		}
		
		@Override
		public void setVisible(EndpointType senderType, IPacketTemplate packetType)
		{
			// do nothing
		}
		
		@Override
		public Map<EndpointType, Set<byte[]>> getSaveableFormat()
		{
			return Collections.emptyMap();
		}
	}
}
