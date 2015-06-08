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
package net.l2emuproject.proxy.network;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * This class does something, but the name is misleading and the implementation is inefficient.
 * JavaDoc postponed until it is reworked.
 * 
 * @author _dev_
 * @param <E> container element type
 */
@SuppressWarnings("javadoc")
public class IPv4AddressTrie<E>
{
	private final Map<IPv4AddressPrefix, E> _container;
	private E _fallbackElement;
	
	public IPv4AddressTrie(Map<IPv4AddressPrefix, ? extends E> map) throws IllegalArgumentException
	{
		_container = new HashMap<>();
		for (final Entry<IPv4AddressPrefix, ? extends E> e : map.entrySet())
			put(e.getKey(), e.getValue());
		_fallbackElement = null;
	}
	
	public IPv4AddressTrie()
	{
		this(Collections.emptyMap());
	}
	
	public void put(IPv4AddressPrefix prefix, E element) throws IllegalArgumentException
	{
		for (final IPv4AddressPrefix k : _container.keySet())
			if (k.isOverlapping(prefix))
				throw new IllegalArgumentException("Overlapping prefixes: " + k + " and " + prefix);
		
		_container.put(prefix, element);
	}
	
	public E get(InetAddress ipv4Address)
	{
		for (final Entry<IPv4AddressPrefix, E> e : _container.entrySet())
			if (e.getKey().isIncluded(ipv4Address))
				return e.getValue();
		return _fallbackElement;
	}
	
	public void setFallbackElement(E element)
	{
		_fallbackElement = element;
	}
	
	public Collection<E> values()
	{
		if (_fallbackElement == null)
			return _container.values();
		
		final Collection<E> result = new ArrayList<>(_container.size() + 1);
		result.add(_fallbackElement);
		result.addAll(_container.values());
		return result;
	}
}
