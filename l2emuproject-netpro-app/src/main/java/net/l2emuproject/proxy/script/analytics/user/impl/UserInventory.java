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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

/**
 * Provides a view over the current user's inventory.
 * 
 * @author _dev_
 */
public final class UserInventory
{
	private volatile Map<Integer, InventoryItem> _items;
	
	public UserInventory()
	{
		_items = Collections.emptyMap();
	}
	
	public void add(InventoryItem item)
	{
		_items.put(item.getObjectID(), item);
	}
	
	public void remove(InventoryItem item)
	{
		_items.remove(item.getObjectID());
	}
	
	public void update(InventoryItem item)
	{
		add(item);
	}
	
	public InventoryItem get(int objectID)
	{
		return _items.get(objectID);
	}
	
	public Stream<InventoryItem> getByTemplate(int templateID)
	{
		return _items.values().stream().filter(item -> item.getTemplateID() == templateID);
	}
	
	public void setInventory(Iterable<InventoryItem> inventoryItems)
	{
		final Map<Integer, InventoryItem> items = new ConcurrentHashMap<>();
		for (final InventoryItem item : inventoryItems)
			items.put(item.getObjectID(), item);
		_items = items;
	}
	
	@Override
	public String toString()
	{
		return _items.values().toString();
	}
}
