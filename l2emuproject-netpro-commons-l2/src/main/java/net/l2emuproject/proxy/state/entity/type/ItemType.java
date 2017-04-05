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
package net.l2emuproject.proxy.state.entity.type;

/**
 * Defines a world object type – item.
 * 
 * @author _dev_
 */
public class ItemType extends EntityWithTemplateType
{
	private final long _amount;
	
	/**
	 * Creates an item descriptor.
	 * 
	 * @param templateID item template ID
	 * @param amount item amount
	 */
	public ItemType(int templateID, long amount)
	{
		super(templateID);
		
		_amount = amount;
	}
	
	/**
	 * Returns the amount for stackable items.
	 * 
	 * @return item amount
	 */
	public long getAmount()
	{
		return _amount;
	}
	
	@Override
	public String describe()
	{
		return "Item";
	}
}
