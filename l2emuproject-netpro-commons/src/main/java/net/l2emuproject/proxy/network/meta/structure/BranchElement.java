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

import java.util.List;

/**
 * Node container protected with a conditional statement.
 * 
 * @author savormix
 */
public class BranchElement extends StructureElementListElement
{
	private final String _id, _condition;
	
	/**
	 * Constructs a branch node.
	 * 
	 * @param id branch ID
	 * @param condition branching condition
	 * @param nodes branch elements
	 */
	public BranchElement(String id, String condition, List<PacketStructureElement> nodes)
	{
		super(nodes);
		
		_id = id;
		_condition = condition;
	}
	
	/**
	 * Constructs a conditionless branch node.
	 * 
	 * @param nodes branch elements
	 */
	public BranchElement(List<PacketStructureElement> nodes)
	{
		this(null, null, nodes);
	}
	
	/**
	 * Returns the ID of a field associated with the branching condition.
	 * 
	 * @return branch ID
	 */
	public String getId()
	{
		return _id;
	}
	
	/**
	 * Returns the condition to be tested for before entering this branch.
	 * 
	 * @return branching condition
	 */
	public String getCondition()
	{
		return _condition;
	}
	
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((_condition == null) ? 0 : _condition.hashCode());
		result = prime * result + ((_id == null) ? 0 : _id.hashCode());
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
		BranchElement other = (BranchElement)obj;
		if (_condition == null)
		{
			if (other._condition != null)
				return false;
		}
		else if (!_condition.equals(other._condition))
			return false;
		if (_id == null)
		{
			if (other._id != null)
				return false;
		}
		else if (!_id.equals(other._id))
			return false;
		return true;
	}
	
	@Override
	public String toString()
	{
		return _id + (_condition != null ? (" (" + _condition + ")") : "") + " " + super.toString();
	}
}
