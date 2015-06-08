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
package net.l2emuproject.proxy.network.meta.structure;

import java.nio.BufferUnderflowException;
import java.util.Map;
import java.util.Set;

import net.l2emuproject.network.mmocore.MMOBuffer;
import net.l2emuproject.proxy.network.meta.exception.InvalidFieldValueInterpreterException;
import net.l2emuproject.proxy.network.meta.exception.InvalidFieldValueModifierException;
import net.l2emuproject.proxy.network.meta.structure.field.FieldValue;
import net.l2emuproject.proxy.network.meta.structure.field.FieldValueReadOption;

/**
 * A less chaotic way to deal with packet content display.<BR>
 * <BR>
 * It is still chaotic, though. Arbitrary objects are now supported with HTML format.<BR>
 * <BR>
 * (This concludes the original JavaDoc)<BR>
 * This class implements methods required to read a field value and properly setup the interpreter/modifier/condition related stuff.
 * This class retains a lot of legacy code and will require a data-type related rework in the future, therefore complete JavaDoc is postponed.
 * 
 * @author savormix
 * @param <V> type of value returned
 */
public abstract class FieldElement<V extends FieldValue> implements PacketStructureElement
{
	private final String _id;
	private final String _alias;
	private final boolean _optional;
	private final Set<String> _fieldAliases;
	private final String _valueModifier;
	private final String _valueInterpreter;
	
	/**
	 * Constructs this field element.
	 * 
	 * @param id field ID
	 * @param alias field description
	 * @param optional whether this field may be excluded from packet
	 * @param fieldAliases field IDs for scripts
	 * @param valueModifier associated value modifier
	 * @param valueInterpreter associated value interpreter
	 */
	protected FieldElement(String id, String alias, boolean optional, Set<String> fieldAliases, String valueModifier,
			String valueInterpreter)
	{
		_id = id;
		_alias = alias;
		_optional = optional;
		_fieldAliases = fieldAliases;
		_valueModifier = valueModifier;
		_valueInterpreter = valueInterpreter;
	}
	
	/**
	 * Reads the value of this field from the given buffer and interprets it within the given entity context.
	 * 
	 * @param buf packet body buffer
	 * @param options optional actions to perform
	 * @param args optional additional arguments
	 * @return the value of this field
	 * @throws BufferUnderflowException if this field falls outside the packet body
	 * @throws InvalidFieldValueInterpreterException if the associated value interpreter is invalid
	 * @throws InvalidFieldValueModifierException if the associated value modifier is invalid
	 */
	public abstract V readValue(MMOBuffer buf, Map<FieldValueReadOption, ?> options, Object... args) throws BufferUnderflowException, InvalidFieldValueInterpreterException, InvalidFieldValueModifierException;
	
	/**
	 * Returns the unique ID of this field. Used by loop and branch elements.
	 * 
	 * @return field ID
	 */
	public String getID()
	{
		return _id;
	}
	
	/**
	 * Returns a user-friendly description of this field.
	 * 
	 * @return field description
	 */
	public String getAlias()
	{
		return _alias;
	}
	
	/**
	 * Returns a set of names used by various scripts to refer to this field within a packet.
	 * 
	 * @return field aliases for scripts
	 */
	public Set<String> getFieldAliases()
	{
		return _fieldAliases;
	}
	
	/**
	 * Whether it is possible that this field may not be included within the packet.
	 * 
	 * @return experimental/may be excluded from structure
	 */
	protected boolean isOptional()
	{
		return _optional;
	}
	
	/**
	 * Returns the associated field value modifier.
	 * 
	 * @return value modifier
	 */
	protected String getValueModifier()
	{
		return _valueModifier;
	}
	
	/**
	 * Returns the associated field value interpreter.
	 * 
	 * @return value interpreter
	 */
	protected String getValueInterpreter()
	{
		return _valueInterpreter;
	}
	
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((_alias == null) ? 0 : _alias.hashCode());
		result = prime * result + ((_fieldAliases == null) ? 0 : _fieldAliases.hashCode());
		result = prime * result + ((_id == null) ? 0 : _id.hashCode());
		result = prime * result + (_optional ? 1231 : 1237);
		result = prime * result + ((_valueInterpreter == null) ? 0 : _valueInterpreter.hashCode());
		result = prime * result + ((_valueModifier == null) ? 0 : _valueModifier.hashCode());
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
		FieldElement<?> other = (FieldElement<?>)obj;
		if (_alias == null)
		{
			if (other._alias != null)
				return false;
		}
		else if (!_alias.equals(other._alias))
			return false;
		if (_fieldAliases == null)
		{
			if (other._fieldAliases != null)
				return false;
		}
		else if (!_fieldAliases.equals(other._fieldAliases))
			return false;
		if (_id == null)
		{
			if (other._id != null)
				return false;
		}
		else if (!_id.equals(other._id))
			return false;
		if (_optional != other._optional)
			return false;
		if (_valueInterpreter == null)
		{
			if (other._valueInterpreter != null)
				return false;
		}
		else if (!_valueInterpreter.equals(other._valueInterpreter))
			return false;
		if (_valueModifier == null)
		{
			if (other._valueModifier != null)
				return false;
		}
		else if (!_valueModifier.equals(other._valueModifier))
			return false;
		return true;
	}
	
	@Override
	public String toString()
	{
		return _alias;
	}
}
