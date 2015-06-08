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
package net.l2emuproject.proxy.network.meta.structure.field.string;

import net.l2emuproject.proxy.network.meta.structure.field.AbstractFieldValue;

/**
 * A wrapper class for a 8-64 bit integer value.
 * 
 * @author _dev_
 */
public final class StringFieldValue extends AbstractFieldValue
{
	private final String _value;
	
	/**
	 * Stores given values.
	 * 
	 * @param raw raw value
	 * @param interpreted value interpretation
	 * @param value actual value
	 */
	public StringFieldValue(byte[] raw, Object interpreted, String value)
	{
		super(raw, interpreted);
		
		_value = value;
	}
	
	/**
	 * Returns the underlying value.
	 * 
	 * @return value
	 */
	public String value()
	{
		return _value;
	}
	
	@Override
	public String toString()
	{
		return _value;
	}
}
