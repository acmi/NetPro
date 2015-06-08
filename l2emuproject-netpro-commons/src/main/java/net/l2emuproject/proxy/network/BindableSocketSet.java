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

import java.util.AbstractSet;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * @author _dev_
 * @param <E> listening socket type
 */
public class BindableSocketSet<E extends ListenSocket> extends AbstractSet<E>
{
	private final List<E> _container;
	
	/**
	 * Creates a set with the given elements.
	 * 
	 * @param elements contained elements
	 * @throws IllegalArgumentException if given sockets cannot all be bound
	 */
	public BindableSocketSet(Collection<? extends E> elements) throws IllegalArgumentException
	{
		_container = new LinkedList<>();
		
		addAll(elements);
	}
	
	/** Creates an empty set. */
	public BindableSocketSet()
	{
		this(Collections.emptySet());
	}
	
	@Override
	public boolean add(E socket) throws IllegalArgumentException
	{
		for (final ListenSocket ls : _container)
			if (socket.bindingEquals(ls))
				throw new IllegalArgumentException("Conflicting listening sockets: " + ls + " and " + socket);
		
		return _container.add(socket);
	}
	
	@Override
	public Iterator<E> iterator()
	{
		return _container.iterator();
	}
	
	@Override
	public int size()
	{
		return _container.size();
	}
}
