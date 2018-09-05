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
package net.l2emuproject.proxy.script.analytics.user;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;

import net.l2emuproject.network.protocol.IProtocolVersion;
import net.l2emuproject.proxy.network.meta.container.MetaclassRegistry;
import net.l2emuproject.proxy.network.meta.exception.InvalidFieldValueInterpreterException;
import net.l2emuproject.proxy.network.meta.interpreter.IntegerTranslator;

/**
 * Contains all special abilities applied on an item.
 * 
 * @author _dev_
 */
public interface ItemSpecialAbilities
{
	/**
	 * Get the general, multi-stage special ability array (order is important).
	 * 
	 * @return multi-stage (or legacy) SAs
	 */
	int[] getPrimaries();
	
	/**
	 * Get the "special" special ability array (order is important).
	 * 
	 * @return special SAs
	 */
	int[] getSpecials();
	
	/**
	 * Returns complete descriptions of all special abilties assigned to the given item.
	 * 
	 * @param specialAbilities item special abilities
	 * @param protocol protocol version
	 * @return descriptions of all SAs, in order
	 */
	static List<String> toString(ItemSpecialAbilities specialAbilities, IProtocolVersion protocol)
	{
		final List<String> result = new ArrayList<>(specialAbilities.getPrimaries().length + specialAbilities.getSpecials().length);
		try
		{
			final IntegerTranslator mapper = MetaclassRegistry.getInstance().getTranslator("item.SoulCrystal", IntegerTranslator.class);
			for (final int specialAbility : specialAbilities.getPrimaries())
				result.add(String.valueOf(mapper.translate(specialAbility, protocol, null)));
			for (final int specialAbility : specialAbilities.getSpecials())
				result.add(String.valueOf(mapper.translate(specialAbility, protocol, null)));
		}
		catch (final InvalidFieldValueInterpreterException e)
		{
			for (final int specialAbility : specialAbilities.getPrimaries())
				result.add("{" + specialAbility + "}");
			for (final int specialAbility : specialAbilities.getSpecials())
				result.add("{" + specialAbility + "}");
		}
		return result;
	}
	
	/**
	 * Returns the strings used to compose SA names shown in item name, e.g. Mystic/HP Mystic/Sigel.
	 * 
	 * @param specialAbilities special ability wrapper
	 * @param protocol protocol version
	 * @return individual SA names to be used on item name, in order
	 */
	static List<String> toTitleString(ItemSpecialAbilities specialAbilities, IProtocolVersion protocol)
	{
		final List<String> result = new ArrayList<>(specialAbilities.getPrimaries().length + specialAbilities.getSpecials().length);
		for (final int specialAbility : specialAbilities.getPrimaries())
			result.add(toTitleString(specialAbility, protocol));
		for (final int specialAbility : specialAbilities.getSpecials())
			result.add(toTitleString(specialAbility, protocol));
		return result;
	}
	
	/**
	 * Returns a string used in item name for the given SA.
	 * 
	 * @param specialAbility SA
	 * @param protocol protocol version
	 * @return SA name, without stage nor description
	 */
	static String toTitleString(int specialAbility, IProtocolVersion protocol)
	{
		try
		{
			final IntegerTranslator mapper = MetaclassRegistry.getInstance().getTranslator("item.SoulCrystal", IntegerTranslator.class);
			final String fullString = String.valueOf(mapper.translate(specialAbility, protocol, null));
			int endIndex = fullString.indexOf(" Stage ");
			if (endIndex == -1)
				endIndex = fullString.indexOf(" (");
			return endIndex != -1 ? fullString.substring(0, endIndex) : fullString;
		}
		catch (final InvalidFieldValueInterpreterException e)
		{
			return "{" + specialAbility + "}";
		}
	}
	
	/**
	 * Returns {@code true}, if there is at least one special ability applied.
	 * 
	 * @param specialAbilities special ability wrapper
	 * @return whether any SAs are applied
	 */
	static boolean isWithSpecialAbility(ItemSpecialAbilities specialAbilities)
	{
		return !Arrays.equals(NO_SPECIAL_ABILITY.getPrimaries(), specialAbilities.getPrimaries()) ||
				!Arrays.equals(NO_SPECIAL_ABILITY.getSpecials(), specialAbilities.getSpecials());
	}
	
	/** A pre-allocated wrapper equivalent to not having any SAs */
	ItemSpecialAbilities NO_SPECIAL_ABILITY = new ItemSpecialAbilities(){
		@Override
		public int[] getPrimaries()
		{
			return ArrayUtils.EMPTY_INT_ARRAY;
		}
		
		@Override
		public int[] getSpecials()
		{
			return ArrayUtils.EMPTY_INT_ARRAY;
		}
		
		@Override
		public String toString()
		{
			return "No special abilities";
		}
	};
}
