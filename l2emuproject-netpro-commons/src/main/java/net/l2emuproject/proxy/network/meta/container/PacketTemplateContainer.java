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
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

import net.l2emuproject.proxy.network.meta.IPacketTemplate;
import net.l2emuproject.proxy.network.meta.PacketTemplate;

/**
 * A packet template container that maps packet opcodes to concrete templates.
 * 
 * @author _dev_
 */
public class PacketTemplateContainer implements IPacketResolver
{
	private final Set<IPacketTemplate> _templates;
	private final OpcodeResolution _lookupDelegate;
	
	/**
	 * Creates a packet template container.
	 * 
	 * @param templates known packet templates
	 */
	public PacketTemplateContainer(Collection<IPacketTemplate> templates)
	{
		_templates = new ConcurrentSkipListSet<>(templates);
		_lookupDelegate = new OpcodeResolution(0);
		for (final IPacketTemplate packet : templates)
			_lookupDelegate.registerPacket(packet);
	}
	
	/** Creates an empty packet template container. */
	public PacketTemplateContainer()
	{
		this(Collections.emptySet());
	}
	
	// two different methods because packet field enumeration is called extremely often
	// and no allocations should proceed; we cannot guarantee a non-direct buffer
	
	@Override
	public IPacketTemplate resolve(byte[] packet, int offset, int length)
	{
		IPacketTemplate result = _lookupDelegate.resolve(packet, offset, length);
		if (result != null)
			return result;
		
		try
		{
			result = new PacketTemplate(Arrays.copyOf(packet, _lookupDelegate.getTruncationPoint(packet, offset, length)));
		}
		catch (IllegalArgumentException e)
		{
			// already done concurrently
			return _lookupDelegate.resolve(packet, offset, length);
		}
		
		_templates.add(result);
		try
		{
			_lookupDelegate.registerPacket(result);
		}
		catch (IllegalArgumentException e)
		{
			// already done concurrently
			return _lookupDelegate.resolve(packet, offset, length);
		}
		
		return result;
	}
	
	@Override
	public IPacketTemplate resolve(ByteBuffer packet)
	{
		final int pos = packet.position();
		IPacketTemplate result = _lookupDelegate.resolve(packet);
		if (result != null)
			return result;
		
		packet.position(pos);
		final byte[] prefix;
		try
		{
			prefix = new byte[_lookupDelegate.getTruncationPoint(packet)];
		}
		catch (IllegalArgumentException e)
		{
			// already done concurrently
			packet.position(pos);
			return _lookupDelegate.resolve(packet);
		}
		
		packet.position(pos);
		packet.get(prefix);
		
		result = new PacketTemplate(prefix);
		_templates.add(result);
		try
		{
			_lookupDelegate.registerPacket(result);
		}
		catch (IllegalArgumentException e)
		{
			// already done concurrently
			packet.position(pos);
			return _lookupDelegate.resolve(packet);
		}
		
		return result;
	}
	
	/**
	 * Returns all currently known and dynamically created packet templates.
	 * 
	 * @return all possible resolution results
	 */
	public Set<IPacketTemplate> getTemplates()
	{
		return _templates;
	}
	
	@Override
	public String toString()
	{
		return _templates.toString();
	}
}
