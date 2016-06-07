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

import net.l2emuproject.lang.L2TextBuilder;
import net.l2emuproject.proxy.network.EndpointType;
import net.l2emuproject.proxy.network.meta.IPacketTemplate;
import net.l2emuproject.util.HexUtil;

/**
 * A reference implementation of {@link IPacketHidingConfig}.
 * 
 * @author _dev_
 */
public class PacketHidingConfig implements IPacketHidingConfig
{
	private final Map<EndpointType, Set<byte[]>> _configuration;
	
	/**
	 * Creates a packet display configuration.
	 * 
	 * @param clientPacketPrefixes client packet opcodes
	 * @param serverPacketPrefixes server packet opcodes
	 */
	public PacketHidingConfig(Set<byte[]> clientPacketPrefixes, Set<byte[]> serverPacketPrefixes)
	{
		_configuration = new EnumMap<>(EndpointType.class);
		_configuration.put(EndpointType.CLIENT, clientPacketPrefixes);
		_configuration.put(EndpointType.SERVER, serverPacketPrefixes);
	}
	
	@Override
	public boolean isHidden(EndpointType senderType, IPacketTemplate packetType)
	{
		return _configuration.getOrDefault(senderType, Collections.emptySet()).contains(packetType.getPrefix());
	}
	
	@Override
	public void setHidden(EndpointType senderType, IPacketTemplate packetType, boolean hidden)
	{
		final Set<byte[]> config = _configuration.get(senderType);
		if (hidden)
			config.add(packetType.getPrefix());
		else
			config.remove(packetType.getPrefix());
	}
	
	@Override
	public Map<EndpointType, Set<byte[]>> getSaveableFormat()
	{
		return _configuration;
	}
	
	@Override
	public String toString()
	{
		final L2TextBuilder tb = new L2TextBuilder("{CLIENT=[");
		for (final byte[] prefix : _configuration.getOrDefault(EndpointType.CLIENT, Collections.emptySet()))
			tb.append(HexUtil.bytesToHexString(prefix, ":")).append(", ");
		tb.setLength(tb.length() - 2).append("], SERVER=[");
		for (final byte[] prefix : _configuration.getOrDefault(EndpointType.SERVER, Collections.emptySet()))
			tb.append(HexUtil.bytesToHexString(prefix, ":")).append(", ");
		tb.setLength(tb.length() - 2).append("]}");
		return tb.moveToString();
	}
}
