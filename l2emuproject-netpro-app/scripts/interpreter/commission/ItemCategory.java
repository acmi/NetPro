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
package interpreter.commission;

import java.util.List;

import net.l2emuproject.proxy.network.meta.EnumeratedPayloadField;
import net.l2emuproject.proxy.network.meta.RandomAccessMMOBuffer;
import net.l2emuproject.proxy.network.meta.interpreter.ContextualFieldValueInterpreter;
import net.l2emuproject.proxy.network.meta.interpreter.IntegerInterpreter;
import net.l2emuproject.proxy.script.interpreter.ScriptedFieldValueInterpreter;
import net.l2emuproject.proxy.state.entity.context.ICacheServerID;

/**
 * @author _dev_
 */
public class ItemCategory extends ScriptedFieldValueInterpreter implements ContextualFieldValueInterpreter, IntegerInterpreter
{
	private static final String[] DEPTH_1 = { "Weapon", "Armor", "Accessory", "Supplies", "Pet goods", "Etc" };
	// @formatter:off
	private static final String[] DEPTH_2 = {
		"N/A", "1-h sword", "1-h magic sword", "Dagger", "Rapier", "2-h sword", "Ancient sword", "Dual sword", "Dual dagger", "Blunt weapon", "1-h magic blunt weapon", "2-h blunt weapon",
		"2-h magic blunt weapon", "Dual blunt weapon", "Bow", "Crossbow", "Hand-to-hand", "Pole", "Other weapon",
		"Helmet", "Upper piece", "Lower piece", "Full body", "Gloves", "Feet", "Shield", "Sigil", "Underwear", "Cloak",
		"Ring", "Earring", "Necklace", "Belt", "Bracelet", "Hair accessory",
		"Potion", "Scroll: enchant weapon", "Scroll: enchant armor", "Scroll: other", "Soulshot", "Spiritshot",
		"N/A", "Pet equipment", "Pet supplies",
		"Crystal", "Recipe", "Crafting main ingredients", "Life stone", "Soul crystal", "Attribute stone", "Weapon enchant stone", "Armor enchant stone", "Spellbook","Gemstone", "Pouch",
		"Pin", "Magic rune clip", "Magic ornament", "Dyes", "Other item",
	};
	// @formatter:on
	
	private final ThreadLocal<Integer> _depth = new ThreadLocal<Integer>()
	{
		@Override
		protected Integer initialValue()
		{
			return 2;
		}
	};
	
	@Override
	public void reviewContext(RandomAccessMMOBuffer buf)
	{
		final List<EnumeratedPayloadField> cat = buf.getFieldIndices("__INTERP_ENABLING_DEPTH_TYPE"), depth = buf.getFieldIndices("__INTERP_ENABLING_DEPTH");
		for (int i = 0; i < cat.size(); ++i)
		{
			if (cat.get(i).getOffset() != buf.getCurrentOffset())
				continue;
			
			_depth.set(buf.readInteger32(depth.get(i)));
			return;
		}
		
		_depth.set(2);
	}
	
	@Override
	public Object getInterpretation(long value, ICacheServerID entityCacheContext)
	{
		final int idx = (int)value;
		try
		{
			switch (_depth.get())
					{
				case 1:
					return DEPTH_1[idx];
				case 2:
					return DEPTH_2[idx];
			}
		}
		catch (ArrayIndexOutOfBoundsException e)
		{
			// proceed
		}
		return value;
	}
}
