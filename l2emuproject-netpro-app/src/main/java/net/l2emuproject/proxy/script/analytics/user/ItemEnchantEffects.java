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

import org.apache.commons.lang3.tuple.Triple;

import net.l2emuproject.network.protocol.IProtocolVersion;
import net.l2emuproject.proxy.network.meta.container.MetaclassRegistry;
import net.l2emuproject.proxy.network.meta.exception.InvalidFieldValueInterpreterException;
import net.l2emuproject.proxy.network.meta.interpreter.IntegerTranslator;

/**
 * An interface representing an item's enchant effects.
 * 
 * @author _dev_
 */
public interface ItemEnchantEffects
{
	/**
	 * Returns the ID of the first variation effect.
	 * 
	 * @return first effect
	 */
	int getEffect1();
	
	/**
	 * Returns the ID of the second variation effect.
	 * 
	 * @return second effect
	 */
	int getEffect2();
	
	/**
	 * Returns the ID of the third variation effect.
	 * 
	 * @return third effect
	 */
	int getEffect3();
	
	/**
	 * Returns the complete descriptions of all three variation effects assigned to the given augmentation.
	 * 
	 * @param enchantEffect item enchant effects
	 * @param protocol protocol version
	 * @return variation effect descriptions
	 */
	static Triple<String, String, String> toString(ItemEnchantEffects enchantEffect, IProtocolVersion protocol)
	{
		try
		{
			final IntegerTranslator mapper = MetaclassRegistry.getInstance().getTranslator("Augmentation", IntegerTranslator.class);
			return Triple.of(String.valueOf(mapper.translate(enchantEffect.getEffect1(), protocol, null)), String.valueOf(mapper.translate(enchantEffect.getEffect2(), protocol, null)),
					String.valueOf(mapper.translate(enchantEffect.getEffect3(), protocol, null)));
		}
		catch (final InvalidFieldValueInterpreterException e)
		{
			return Triple.of("{" + enchantEffect.getEffect1() + "}", "{" + enchantEffect.getEffect2() + "}", "{" + enchantEffect.getEffect3() + "}");
		}
	}
	
	/**
	 * Tests whether the given enchant effects actually contains any variation effects.
	 * 
	 * @param enchantEffect item enchant effects
	 * @return {@code true} if there is at least one variation effect, {@code false} otherwise
	 */
	static boolean isWithEnchantEffect(ItemEnchantEffects enchantEffect)
	{
		return enchantEffect.getEffect1() != NO_EFFECTS.getEffect1() || enchantEffect.getEffect2() != NO_EFFECTS.getEffect2() || enchantEffect.getEffect3() != NO_EFFECTS.getEffect3();
	}
	
	/** A pre-allocated wrapper for an effectless enchant level. */
	ItemEnchantEffects NO_EFFECTS = new ItemEnchantEffects(){
		@Override
		public int getEffect1()
		{
			return 0;
		}
		
		@Override
		public int getEffect2()
		{
			return 0;
		}
		
		@Override
		public int getEffect3()
		{
			return 0;
		}
		
		@Override
		public String toString()
		{
			return "No enchant effect";
		}
	};
}
