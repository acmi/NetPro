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

import java.text.NumberFormat;
import java.util.Iterator;
import java.util.Locale;

import net.l2emuproject.proxy.network.meta.container.MetaclassRegistry;
import net.l2emuproject.proxy.network.meta.exception.InvalidFieldValueInterpreterException;
import net.l2emuproject.proxy.network.meta.interpreter.IntegerInterpreter;
import net.l2emuproject.proxy.script.analytics.user.ItemAugmentation;
import net.l2emuproject.proxy.script.analytics.user.ItemElementalAttributes;
import net.l2emuproject.proxy.script.analytics.user.ItemEnchantEffects;
import net.l2emuproject.proxy.script.analytics.user.ItemSpecialAbilities;
import net.l2emuproject.util.HexUtil;

/**
 * Wraps an item contained within a user's inventory.
 * 
 * @author _dev_
 */
public final class InventoryItem
{
	private final int _objectID;
	private final int _templateID;
	private final long _amount;
	private final int _enchantLevel;
	private final ItemAugmentation _augmentation;
	private final ItemElementalAttributes _elementalAttributes;
	private final ItemEnchantEffects _enchantEffect;
	private final int _appearance;
	private final ItemSpecialAbilities _specialAbilities;
	
	/**
	 * Creates an inventory item.
	 * 
	 * @param objectID runtime ID
	 * @param templateID template ID
	 * @param amount amount
	 * @param enchantLevel enchant level
	 * @param augmentation augmentation effects
	 * @param elementalAttributes elemental attribute values
	 * @param enchantEffect enchant effects
	 * @param appearance appearance
	 * @param specialAbilities special abilities
	 */
	public InventoryItem(int objectID, int templateID, long amount, int enchantLevel, ItemAugmentation augmentation, ItemElementalAttributes elementalAttributes, ItemEnchantEffects enchantEffect,
			int appearance,
			ItemSpecialAbilities specialAbilities)
	{
		_objectID = objectID;
		_templateID = templateID;
		_amount = amount;
		_enchantLevel = enchantLevel;
		_augmentation = augmentation;
		_elementalAttributes = elementalAttributes;
		_enchantEffect = enchantEffect;
		_appearance = appearance;
		_specialAbilities = specialAbilities;
	}
	
	/**
	 * Returns the runtime object ID assigned to this item.
	 * 
	 * @return object ID
	 */
	public int getObjectID()
	{
		return _objectID;
	}
	
	/**
	 * Returns the template ID for this item.
	 * 
	 * @return template ID
	 */
	public int getTemplateID()
	{
		return _templateID;
	}
	
	/**
	 * Returns the amount of items in this stack.
	 * 
	 * @return item amount
	 */
	public long getAmount()
	{
		return _amount;
	}
	
	/**
	 * Returns the enchant level / pet level for this item.
	 * 
	 * @return enchant level
	 */
	public int getEnchantLevel()
	{
		return _enchantLevel;
	}
	
	/**
	 * Returns the variation effects for this item's augmentation.
	 * 
	 * @return augmentation
	 */
	public ItemAugmentation getAugmentation()
	{
		return _augmentation;
	}
	
	/**
	 * Returns the elemental attribute values assigned to this item.
	 * 
	 * @return elemental attributes
	 */
	public ItemElementalAttributes getElementalAttributes()
	{
		return _elementalAttributes;
	}
	
	/**
	 * Returns the variation effects for this item's enchant level.
	 * 
	 * @return enchant effects
	 */
	public ItemEnchantEffects getEnchantEffect()
	{
		return _enchantEffect;
	}
	
	/**
	 * Returns the appearance of this item.
	 * 
	 * @return appearance
	 */
	public int getAppearance()
	{
		return _appearance;
	}
	
	/**
	 * Returns the special abilities on this item.
	 * 
	 * @return special abilities
	 */
	public ItemSpecialAbilities getSpecialAbilities()
	{
		return _specialAbilities;
	}
	
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + _objectID;
		return result;
	}
	
	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final InventoryItem other = (InventoryItem)obj;
		if (_objectID != other._objectID)
			return false;
		return true;
	}
	
	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder();
		sb.append('[').append(HexUtil.fillHex(_objectID, 8)).append("] ");
		if (_enchantLevel > 0)
			sb.append('+').append(_enchantLevel).append(' ');
		if (ItemAugmentation.isAugmented(_augmentation))
			sb.append("Augmented ");
		try
		{
			sb.append(MetaclassRegistry.getInstance().getInterpreter("Item", IntegerInterpreter.class).getInterpretation(_templateID));
		}
		catch (final InvalidFieldValueInterpreterException e)
		{
			sb.append('{').append(_templateID).append('}');
		}
		if (ItemSpecialAbilities.isWithSpecialAbility(_specialAbilities))
		{
			final Iterator<String> it = ItemSpecialAbilities.toTitleString(_specialAbilities).iterator();
			sb.append(' ').append(it.next());
			while (it.hasNext())
				sb.append('/').append(it.next());
		}
		for (final String elementalAttribute : ItemElementalAttributes.toString(_elementalAttributes))
			sb.append(' ').append(elementalAttribute);
		if (_amount > 1)
			sb.append('(').append(NumberFormat.getIntegerInstance(Locale.ENGLISH).format(_amount)).append(')');
		return sb.toString();
	}
}
