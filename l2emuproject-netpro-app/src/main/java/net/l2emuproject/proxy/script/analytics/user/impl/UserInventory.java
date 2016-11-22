/*
 * Copyright 2011-2016 L2EMU UNIQUE
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
package net.l2emuproject.proxy.script.analytics.user.impl;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

/**
 * Provides a view over the current user's inventory.
 * 
 * @author _dev_
 */
public final class UserInventory implements Iterable<InventoryItem>
{
	private volatile Map<Integer, InventoryItem> _items;
	
	/** Creates an empty inventory. */
	public UserInventory()
	{
		setInventory(Collections.emptyList());
	}
	
	/**
	 * Adds an item to this inventory.
	 * 
	 * @param item an item
	 */
	public void add(InventoryItem item)
	{
		_items.put(item.getObjectID(), item);
	}
	
	/**
	 * Removes an item from this inventory.
	 * 
	 * @param item an item
	 */
	public void remove(InventoryItem item)
	{
		_items.remove(item.getObjectID());
	}
	
	/**
	 * Replaces an item in this inventory.
	 * 
	 * @param item an item
	 */
	public void update(InventoryItem item)
	{
		add(item);
	}
	
	/**
	 * Retrieves an item from this inventory based on it's runtime (object) ID.
	 * 
	 * @param objectID runtime ID
	 * @return a single item [stack]
	 */
	public InventoryItem get(int objectID)
	{
		return _items.get(objectID);
	}
	
	/**
	 * Retrieves all items/item stacks that share the given template ID.
	 * 
	 * @param templateID template ID
	 * @return items based on the specified template
	 */
	public Stream<InventoryItem> getByTemplate(int templateID)
	{
		return _items.values().stream().filter(item -> item.getTemplateID() == templateID);
	}
	
	/**
	 * Overrides inventory contents with the given items.
	 * 
	 * @param inventoryItems items to be newly contained
	 */
	public void setInventory(Iterable<InventoryItem> inventoryItems)
	{
		final Map<Integer, InventoryItem> items = new ConcurrentHashMap<>();
		for (final InventoryItem item : inventoryItems)
			items.put(item.getObjectID(), item);
		_items = items;
	}
	
	@Override
	public Iterator<InventoryItem> iterator()
	{
		return _items.values().iterator();
	}
	
	@Override
	public String toString()
	{
		return _items.values().toString();
	}
}
