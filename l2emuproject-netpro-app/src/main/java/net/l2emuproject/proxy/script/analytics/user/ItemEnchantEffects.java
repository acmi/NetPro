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

import net.l2emuproject.proxy.network.meta.container.MetaclassRegistry;
import net.l2emuproject.proxy.network.meta.exception.InvalidFieldValueInterpreterException;
import net.l2emuproject.proxy.network.meta.interpreter.IntegerInterpreter;

/**
 * @author _dev_
 */
public interface ItemEnchantEffects
{
	int getEffect1();
	
	int getEffect2();
	
	int getEffect3();
	
	static Triple<String, String, String> toString(ItemEnchantEffects enchantEffect)
	{
		try
		{
			final IntegerInterpreter mapper = MetaclassRegistry.getInstance().getInterpreter("Augmentation", IntegerInterpreter.class);
			return Triple.of(String.valueOf(mapper.getInterpretation(enchantEffect.getEffect1())), String.valueOf(mapper.getInterpretation(enchantEffect.getEffect2())),
					String.valueOf(mapper.getInterpretation(enchantEffect.getEffect3())));
		}
		catch (final InvalidFieldValueInterpreterException e)
		{
			return Triple.of("{" + enchantEffect.getEffect1() + "}", "{" + enchantEffect.getEffect2() + "}", "{" + enchantEffect.getEffect3() + "}");
		}
	}
	
	static boolean isWithEnchantEffect(ItemEnchantEffects enchantEffect)
	{
		return enchantEffect.getEffect1() != NO_EFFECTS.getEffect1() || enchantEffect.getEffect2() != NO_EFFECTS.getEffect2() || enchantEffect.getEffect3() != NO_EFFECTS.getEffect3();
	}
	
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
