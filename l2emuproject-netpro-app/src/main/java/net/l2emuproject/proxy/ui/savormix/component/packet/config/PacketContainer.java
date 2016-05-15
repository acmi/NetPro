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
package net.l2emuproject.proxy.ui.savormix.component.packet.config;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import net.l2emuproject.network.protocol.IProtocolVersion;
import net.l2emuproject.proxy.io.definitions.VersionnedPacketTable;
import net.l2emuproject.proxy.network.EndpointType;
import net.l2emuproject.proxy.network.meta.IPacketTemplate;

/**
 * A class that tracks packet display configuration for a single endpoint.
 * 
 * @author savormix
 */
class PacketContainer
{
	private final EndpointType _endpoint;
	// only used on the UI thread OR when UI is blocked
	private final Set<IPacketTemplate> _committed;
	private final Set<IPacketTemplate> _selected;
	private final Set<IPacketTemplate> _deselected;
	
	public PacketContainer(IProtocolVersion version, EndpointType endpoint)
	{
		_endpoint = endpoint;
		
		// load known
		_committed = VersionnedPacketTable.getInstance().getKnownTemplates(version, endpoint).collect(Collectors.toCollection(HashSet::new));
		_committed.add(IPacketTemplate.ANY_DYNAMIC_PACKET);
		
		_selected = new HashSet<>();
		_deselected = new HashSet<>();
	}
	
	public EndpointType getProxyType()
	{
		return _endpoint;
	}
	
	public Set<IPacketTemplate> getCommitted()
	{
		return _committed;
	}
	
	public Set<IPacketTemplate> getSelected()
	{
		return _selected;
	}
	
	public Set<IPacketTemplate> getDeselected()
	{
		return _deselected;
	}
}
