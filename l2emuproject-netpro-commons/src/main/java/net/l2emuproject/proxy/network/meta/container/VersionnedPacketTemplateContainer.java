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
package net.l2emuproject.proxy.network.meta.container;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Stream;

import net.l2emuproject.network.protocol.IProtocolVersion;
import net.l2emuproject.proxy.network.EndpointType;
import net.l2emuproject.proxy.network.meta.IPacketTemplate;

/**
 * A packet template container that maps packet opcodes to packet templates based on protocol version in use.
 * 
 * @author _dev_
 * @param <T> protocol type
 */
public class VersionnedPacketTemplateContainer<T extends IProtocolVersion>
{
	private final Map<T, Map<EndpointType, PacketPrefixResolver>> _containers;
	private final Map<Set<String>, T> _fallbacks;
	
	/**
	 * Creates a versionned packet template container.
	 * 
	 * @param containers packet templates
	 */
	public VersionnedPacketTemplateContainer(Map<T, Map<EndpointType, PacketPrefixResolver>> containers)
	{
		_containers = containers;
		
		_fallbacks = new HashMap<>();
		for (final Entry<T, Map<EndpointType, PacketPrefixResolver>> e : containers.entrySet()) {
			final T protocol = e.getKey();
			final Map<EndpointType, PacketPrefixResolver> map = e.getValue();
			if (map == null || map.size() != 2)
				continue;
			_fallbacks.compute(protocol.getAltModes(), (k, old) -> old == null || protocol.isNewerThan(old) ? protocol : old);
		}
	}
	
	/**
	 * Returns a matching packet template for the given client/server packet in the specified protocol version.
	 * 
	 * @param version protocol version
	 * @param endpoint endpoint type
	 * @param packet packet body [complete array]
	 * @return matching packet template
	 */
	public IPacketTemplate getTemplate(T version, EndpointType endpoint, byte[] packet)
	{
		return getTemplate(version, endpoint, packet, 0, packet.length);
	}
	
	/**
	 * Returns a matching packet template for the given client/server packet in the specified protocol version.
	 * 
	 * @param version protocol version
	 * @param endpoint endpoint type
	 * @param packet packet body buffer
	 * @param offset packet offset in buffer
	 * @param length packet body size
	 * @return matching packet template
	 */
	public IPacketTemplate getTemplate(T version, EndpointType endpoint, byte[] packet, int offset, int length)
	{
		final Map<EndpointType, PacketPrefixResolver> map = _containers.get(version);
		if (map == null)
			return getTemplate(getFallback(version.getAltModes()), endpoint, packet, offset, length);
		
		final PacketPrefixResolver container = map.get(endpoint);
		if (container == null)
			return getTemplate(getFallback(version.getAltModes()), endpoint, packet, offset, length);
		
		return container.resolve(packet);
	}
	
	/**
	 * Returns a matching packet template for the given client/server packet in the specified protocol version.<BR>
	 * <BR>
	 * It is assumed that the packet body is between current buffer's position and limit.
	 * 
	 * @param version protocol version
	 * @param endpoint endpoint type
	 * @param packet packet body buffer
	 * @return matching packet template
	 */
	public IPacketTemplate getTemplate(T version, EndpointType endpoint, ByteBuffer packet)
	{
		final Map<EndpointType, PacketPrefixResolver> map = _containers.get(version);
		if (map == null)
			return getTemplate(getFallback(version.getAltModes()), endpoint, packet);
		
		final PacketPrefixResolver container = map.get(endpoint);
		if (container == null)
			return getTemplate(getFallback(version.getAltModes()), endpoint, packet);
		
		return container.resolve(packet);
	}
	
	/**
	 * Returns all known and dynamically added client/server packet templates for a specific protocol version.
	 * 
	 * @param version protocol version
	 * @param endpoint endpoint type
	 * @return all possible resolution results
	 */
	public Stream<IPacketTemplate> getTemplates(T version, EndpointType endpoint)
	{
		final Map<EndpointType, PacketPrefixResolver> map = _containers.get(version);
		if (map == null)
			return getTemplates(getFallback(version.getAltModes()), endpoint);
		
		final PacketPrefixResolver container = map.get(endpoint);
		if (container == null)
			return getTemplates(getFallback(version.getAltModes()), endpoint);
		
		return container.getAllTemplates().stream();
	}
	
	/**
	 * Returns a protocol version to be used if no packet definitions are provided for the one originally requested.
	 * @param altModes alternative modes
	 * @return fallback protocol version
	 */
	public T getFallback(Set<String> altModes)
	{
		// quick logic explanation without fancy formatting in javadoc
		// alternative modes are expected to be in order of significance, e.g.
		// 1. classic 2. taiwan 3. add_differentiator
		// and the fallback versions will be queried in this order:
		// 1. c t a_d
		// 2. c t
		// 3. c
		// 4. t a_d
		// 5. t
		// 6. a_d
		// 7. [no alt modes]
		
		if (!altModes.isEmpty()) {
			List<String> orderedModes = new ArrayList<>(altModes);
			for (int i = 0; i < altModes.size(); ++i) {
				for (int sz = altModes.size() - i; sz > 0; --sz) {
					T fallback = _fallbacks.get(new HashSet<>(orderedModes.subList(i, i + sz)));
					if (fallback != null)
						return fallback;
				}
			}
		}
		return _fallbacks.get(Collections.emptySet());
	}
}
