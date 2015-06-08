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
package net.l2emuproject.proxy.network.meta.structure.field.bytes;

import java.nio.BufferUnderflowException;
import java.util.Set;

import net.l2emuproject.network.mmocore.MMOBuffer;

/**
 * A class that represents a fixed size byte array field.
 * 
 * @author _dev_
 */
public final class FixedSizeByteArrayFieldElement extends AbstractByteArrayFieldElement
{
	private final int _arrayLength;
	
	/**
	 * Constructs this field element.
	 * 
	 * @param id field ID
	 * @param alias field description
	 * @param optional whether this field may be excluded from packet
	 * @param fieldAliases field IDs for scripts
	 * @param valueModifier associated value modifier
	 * @param valueInterpreter associated value interpreter
	 * @param arrayLength size of this field, in bytes
	 */
	public FixedSizeByteArrayFieldElement(String id, String alias, boolean optional, Set<String> fieldAliases,
			String valueModifier, String valueInterpreter, int arrayLength)
	{
		super(id, alias, optional, fieldAliases, valueModifier, valueInterpreter);
		
		_arrayLength = arrayLength;
	}
	
	@Override
	protected byte[] readValue(MMOBuffer buf, Object... args) throws BufferUnderflowException
	{
		return buf.readB(_arrayLength);
	}
	
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + _arrayLength;
		return result;
	}
	
	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		FixedSizeByteArrayFieldElement other = (FixedSizeByteArrayFieldElement)obj;
		if (_arrayLength != other._arrayLength)
			return false;
		return true;
	}
}
