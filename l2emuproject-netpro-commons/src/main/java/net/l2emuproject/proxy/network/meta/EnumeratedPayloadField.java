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
package net.l2emuproject.proxy.network.meta;

import net.l2emuproject.proxy.network.meta.structure.FieldElement;

/**
 * Defines a field.
 * 
 * @author _dev_
 */
public class EnumeratedPayloadField
{
	private final FieldElement<?> _element;
	private final int _offset;
	
	/**
	 * Constructs this enumerated field wrapper.
	 * 
	 * @param element field
	 * @param offset field offset
	 */
	public EnumeratedPayloadField(FieldElement<?> element, int offset)
	{
		_element = element;
		_offset = offset;
	}
	
	/**
	 * Returns the associated field element.
	 * 
	 * @return field
	 */
	public FieldElement<?> getElement()
	{
		return _element;
	}
	
	/**
	 * Returns the address of this field within the packet's body.
	 * 
	 * @return field's offset
	 */
	public int getOffset()
	{
		return _offset;
	}
	
	@Override
	public String toString()
	{
		return "[" + _offset + "]" + _element;
	}
}
