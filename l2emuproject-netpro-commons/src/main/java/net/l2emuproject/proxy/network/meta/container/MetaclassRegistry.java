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
package net.l2emuproject.proxy.network.meta.container;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.l2emuproject.proxy.network.meta.FieldValueCondition;
import net.l2emuproject.proxy.network.meta.FieldValueInterpreter;
import net.l2emuproject.proxy.network.meta.FieldValueModifier;
import net.l2emuproject.proxy.network.meta.condition.impl.Negative;
import net.l2emuproject.proxy.network.meta.condition.impl.NonNegative;
import net.l2emuproject.proxy.network.meta.condition.impl.NonPositive;
import net.l2emuproject.proxy.network.meta.condition.impl.Positive;
import net.l2emuproject.proxy.network.meta.condition.impl.Zero;
import net.l2emuproject.proxy.network.meta.condition.impl.bitmask.Bit0;
import net.l2emuproject.proxy.network.meta.condition.impl.bitmask.Bit1;
import net.l2emuproject.proxy.network.meta.condition.impl.bitmask.Bit2;
import net.l2emuproject.proxy.network.meta.condition.impl.bitmask.Bit3;
import net.l2emuproject.proxy.network.meta.condition.impl.bitmask.Bit4;
import net.l2emuproject.proxy.network.meta.condition.impl.bitmask.Bit5;
import net.l2emuproject.proxy.network.meta.condition.impl.bitmask.Bit6;
import net.l2emuproject.proxy.network.meta.condition.impl.bitmask.Bit7;
import net.l2emuproject.proxy.network.meta.condition.impl.bitmask.NotAllBits;
import net.l2emuproject.proxy.network.meta.exception.InvalidFieldValueConditionException;
import net.l2emuproject.proxy.network.meta.exception.InvalidFieldValueInterpreterException;
import net.l2emuproject.proxy.network.meta.exception.InvalidFieldValueModifierException;
import net.l2emuproject.proxy.network.meta.interpreter.impl.IPv4;
import net.l2emuproject.proxy.network.meta.interpreter.impl.MillisRemaining;
import net.l2emuproject.proxy.network.meta.interpreter.impl.SecondsLeftToTime;
import net.l2emuproject.proxy.network.meta.interpreter.impl.SecondsSinceEpoch;
import net.l2emuproject.proxy.network.meta.interpreter.impl.TimeRemaining;
import net.l2emuproject.proxy.network.meta.interpreter.impl.YesOrNo;

/**
 * @author _dev_
 */
public class MetaclassRegistry
{
	private final Map<String, FieldValueCondition> _conditions;
	private final Map<String, FieldValueInterpreter> _interpreters;
	private final Map<String, FieldValueModifier> _modifiers;
	
	MetaclassRegistry()
	{
		_conditions = new ConcurrentHashMap<>();
		_interpreters = new ConcurrentHashMap<>();
		_modifiers = new ConcurrentHashMap<>();
		
		{
			// generic conditions
			registerInternal(_conditions, new Negative());
			registerInternal(_conditions, new NonNegative());
			registerInternal(_conditions, new NonPositive());
			registerInternal(_conditions, new Positive());
			registerInternal(_conditions, new Zero());
			registerInternal(_conditions, new Bit0(), 1);
			registerInternal(_conditions, new Bit1(), 1);
			registerInternal(_conditions, new Bit2(), 1);
			registerInternal(_conditions, new Bit3(), 1);
			registerInternal(_conditions, new Bit4(), 1);
			registerInternal(_conditions, new Bit5(), 1);
			registerInternal(_conditions, new Bit6(), 1);
			registerInternal(_conditions, new Bit7(), 1);
			registerInternal(_conditions, new NotAllBits(), 1);
		}
		{
			// generic interpreters
			registerInternal(_interpreters, new IPv4());
			registerInternal(_interpreters, new MillisRemaining());
			registerInternal(_interpreters, new SecondsLeftToTime());
			registerInternal(_interpreters, new SecondsSinceEpoch());
			registerInternal(_interpreters, new TimeRemaining());
			registerInternal(_interpreters, new YesOrNo());
		}
		{
			// generic modifiers
		}
	}
	
	private static <T> void registerInternal(Map<String, ? super T> map, T instance, int packagesToInclude)
	{
		final String fqcn = instance.getClass().getName();
		int start = fqcn.lastIndexOf('.') + 1;
		while (start > 0 && --packagesToInclude >= 0)
			start = fqcn.lastIndexOf('.', start - 2) + 1;
		
		map.put(fqcn.substring(start), instance);
	}
	
	private static <T> void registerInternal(Map<String, ? super T> map, T instance)
	{
		registerInternal(map, instance, 0);
	}
	
	/**
	 * Registers a field value condition.
	 * 
	 * @param alias condition alias used in packet definitions
	 * @param condition condition to register
	 */
	public void register(String alias, FieldValueCondition condition)
	{
		_conditions.put(alias, condition);
	}
	
	/**
	 * Removes a field value condition.
	 * 
	 * @param alias condition alias used in packet definitions
	 * @param condition condition to remove
	 */
	public void remove(String alias, FieldValueCondition condition)
	{
		_conditions.remove(alias, condition);
	}
	
	/**
	 * Returns a typed interpreter, if applicable.
	 * 
	 * @param alias interpreter alias
	 * @param expectedClass type to be returned
	 * @return interpreter of requested type
	 * @throws InvalidFieldValueConditionException if expectations are not met
	 * @param <T> expected return type
	 */
	public <T extends FieldValueCondition> T getCondition(String alias, Class<? extends T> expectedClass) throws InvalidFieldValueConditionException
	{
		final FieldValueCondition result = _conditions.get(alias);
		if (expectedClass.isInstance(result))
			return expectedClass.cast(result);
		
		throw new InvalidFieldValueConditionException(result, alias, expectedClass.getName());
	}
	
	/**
	 * Registers a field value interpreter.
	 * 
	 * @param alias interpreter alias used in packet definitions
	 * @param interpreter interpreter to register
	 */
	public void register(String alias, FieldValueInterpreter interpreter)
	{
		_interpreters.put(alias, interpreter);
	}
	
	/**
	 * Removes a field value interpreter.
	 * 
	 * @param alias interpreter alias used in packet definitions
	 * @param interpreter interpreter to remove
	 */
	public void remove(String alias, FieldValueInterpreter interpreter)
	{
		_interpreters.remove(alias, interpreter);
	}
	
	/**
	 * Returns a typed interpreter, if applicable.
	 * 
	 * @param alias interpreter alias
	 * @param expectedClass type to be returned
	 * @return interpreter of requested type
	 * @throws InvalidFieldValueInterpreterException if expectations are not met
	 * @param <T> expected return type
	 */
	public <T extends FieldValueInterpreter> T getInterpreter(String alias, Class<? extends T> expectedClass) throws InvalidFieldValueInterpreterException
	{
		final FieldValueInterpreter result = _interpreters.get(alias);
		if (expectedClass.isInstance(result))
			return expectedClass.cast(result);
		
		throw new InvalidFieldValueInterpreterException(result, alias, expectedClass.getName());
	}
	
	/**
	 * Registers a field value modifier.
	 * 
	 * @param alias modifier alias used in packet definitions
	 * @param modifier modifier to register
	 */
	public void register(String alias, FieldValueModifier modifier)
	{
		_modifiers.put(alias, modifier);
	}
	
	/**
	 * Removes a field value modifier.
	 * 
	 * @param alias modifier alias used in packet definitions
	 * @param modifier modifier to remove
	 */
	public void remove(String alias, FieldValueModifier modifier)
	{
		_modifiers.remove(alias, modifier);
	}
	
	/**
	 * Returns a typed interpreter, if applicable.
	 * 
	 * @param alias interpreter alias
	 * @param expectedClass type to be returned
	 * @return interpreter of requested type
	 * @throws InvalidFieldValueModifierException if expectations are not met
	 * @param <T> expected return type
	 */
	public <T extends FieldValueModifier> T getModifier(String alias, Class<? extends T> expectedClass) throws InvalidFieldValueModifierException
	{
		final FieldValueModifier result = _modifiers.get(alias);
		if (expectedClass.isInstance(result))
			return expectedClass.cast(result);
		
		throw new InvalidFieldValueModifierException(result, alias, expectedClass.getName());
	}
	
	/**
	 * Returns a singleton instance of this type.
	 * 
	 * @return an instance of this class
	 */
	public static final MetaclassRegistry getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static final class SingletonHolder
	{
		static final MetaclassRegistry INSTANCE = new MetaclassRegistry();
	}
}
