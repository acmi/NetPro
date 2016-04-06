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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

import net.l2emuproject.proxy.network.meta.IPacketTemplate;
import net.l2emuproject.proxy.network.meta.PacketTemplate;
import net.l2emuproject.util.concurrent.MapUtils;
import net.l2emuproject.util.logging.L2Logger;

/**
 * A generic resolver that determines the packet type based on predefined prefixes.
 * 
 * @author _dev_
 */
public final class PacketPrefixResolver implements IPacketResolver
{
	private static final L2Logger LOG = L2Logger.getLogger(PacketPrefixResolver.class);
	
	private final Map<Byte, Object> _trie;
	private final Set<IPacketTemplate> _allTemplates;
	
	/**
	 * Constructs a resolver for the given templates.
	 * 
	 * @param templates packet templates
	 * @throws IllegalArgumentException in case of a prefixless template
	 */
	public PacketPrefixResolver(Collection<IPacketTemplate> templates) throws IllegalArgumentException
	{
		_trie = new ConcurrentHashMap<>();
		_allTemplates = new CopyOnWriteArraySet<>();
		for (final IPacketTemplate template : templates)
		{
			final Object conflictingObject = registerPrefix(_trie, template, 0);
			if (conflictingObject == null)
			{
				_allTemplates.add(template);
				continue;
			}
			
			if (conflictingObject instanceof IPacketTemplate)
				LOG.warn("Cannot add resolution for " + template + " as it conflicts with " + conflictingObject);
			else
				LOG.warn("Cannot add resolution for " + template + " as it conflicts with " + ((Map<?, ?>)conflictingObject).values());
		}
	}
	
	@SuppressWarnings("unchecked")
	private Object registerPrefix(Map<Byte, Object> trie, IPacketTemplate template, int prefixOffset) throws IllegalArgumentException
	{
		final byte[] prefix = template.getPrefix();
		final int remaining = prefix.length - prefixOffset;
		if (remaining < 1)
			throw new IllegalArgumentException();
		
		final Byte key = Byte.valueOf(prefix[prefixOffset]);
		if (remaining == 1)
			return trie.putIfAbsent(key, template);
		
		Object nextLevelObject = trie.get(key);
		if (nextLevelObject == null)
			nextLevelObject = MapUtils.putIfAbsent(trie, key, new ConcurrentHashMap<>());
		return nextLevelObject instanceof IPacketTemplate ? nextLevelObject : registerPrefix((Map<Byte, Object>)nextLevelObject, template, prefixOffset + 1);
	}
	
	@Override
	public IPacketTemplate resolve(byte[] packet, int offset, int length)
	{
		return resolve(_trie, packet, offset, offset, length);
	}
	
	@SuppressWarnings("unchecked")
	private IPacketTemplate resolve(Map<Byte, Object> trie, byte[] packet, int originalOffset, int offset, int length)
	{
		if (length < 1)
			return null;
		
		final Byte key = Byte.valueOf(packet[offset]);
		final Object nextLevelObject = trie.get(key);
		
		if (nextLevelObject == null)
		{
			final IPacketTemplate fakeTemplate = new PacketTemplate(Arrays.copyOfRange(packet, originalOffset, offset + 1));
			final IPacketTemplate result = (IPacketTemplate)MapUtils.putIfAbsent(trie, key, fakeTemplate);
			if (result == fakeTemplate)
				_allTemplates.add(result);
			return result;
		}
		
		if (nextLevelObject instanceof IPacketTemplate)
			return (IPacketTemplate)nextLevelObject;
		
		return resolve((Map<Byte, Object>)nextLevelObject, packet, originalOffset, offset + 1, length - 1);
	}
	
	@Override
	public IPacketTemplate resolve(ByteBuffer packet)
	{
		return resolve(_trie, packet, packet.position());
	}
	
	@SuppressWarnings("unchecked")
	private IPacketTemplate resolve(Map<Byte, Object> trie, ByteBuffer packet, int originalPosition)
	{
		if (packet.remaining() < 1)
			return null;
		
		final Byte key = Byte.valueOf(packet.get());
		final Object nextLevelObject = trie.get(key);
		
		if (nextLevelObject == null)
		{
			final byte[] prefix = new byte[packet.position() - originalPosition];
			packet.position(originalPosition);
			packet.get(prefix);
			final IPacketTemplate fakeTemplate = new PacketTemplate(prefix);
			final IPacketTemplate result = (IPacketTemplate)MapUtils.putIfAbsent(trie, key, fakeTemplate);
			if (result == fakeTemplate)
				_allTemplates.add(result);
			return result;
		}
		
		if (nextLevelObject instanceof IPacketTemplate)
			return (IPacketTemplate)nextLevelObject;
		
		return resolve((Map<Byte, Object>)nextLevelObject, packet, originalPosition);
	}
	
	Set<IPacketTemplate> getAllTemplates()
	{
		return Collections.unmodifiableSet(_allTemplates);
	}
	
	@Override
	public String toString()
	{
		return _allTemplates.toString();
	}
}
