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

import org.apache.commons.lang3.tuple.Pair;

import net.l2emuproject.proxy.network.meta.container.MetaclassRegistry;
import net.l2emuproject.proxy.network.meta.exception.InvalidFieldValueInterpreterException;
import net.l2emuproject.proxy.network.meta.interpreter.IntegerInterpreter;

/**
 * An interface representing an item augmentation.
 * 
 * @author _dev_
 */
public interface ItemAugmentation
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
	 * Returns the complete descriptions of both variation effects assigned to the given augmentation.
	 * 
	 * @param augmentation an item augmentation
	 * @return variation effect descriptions
	 */
	static Pair<String, String> toString(ItemAugmentation augmentation)
	{
		try
		{
			final IntegerInterpreter mapper = MetaclassRegistry.getInstance().getInterpreter("Augmentation", IntegerInterpreter.class);
			return Pair.of(String.valueOf(mapper.getInterpretation(augmentation.getEffect1())), String.valueOf(mapper.getInterpretation(augmentation.getEffect2())));
		}
		catch (final InvalidFieldValueInterpreterException e)
		{
			return Pair.of("{" + augmentation.getEffect1() + "}", "{" + augmentation.getEffect2() + "}");
		}
	}
	
	/**
	 * Tests whether the given augmentation actually contains any variation effects.
	 * 
	 * @param augmentation an item augmentation
	 * @return {@code true} if there is at least one variation effect, {@code false} otherwise
	 */
	static boolean isAugmented(ItemAugmentation augmentation)
	{
		return augmentation.getEffect1() != NO_AUGMENTATION.getEffect1() || augmentation.getEffect2() != NO_AUGMENTATION.getEffect2();
	}
	
	/** A pre-allocated wrapper for an effectless augmentation. */
	ItemAugmentation NO_AUGMENTATION = new ItemAugmentation(){
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
		public String toString()
		{
			return "Not augmented";
		}
	};
}
