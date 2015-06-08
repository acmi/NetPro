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
 * A reiterated packet node container.
 * 
 * @author savormix
 */
public class LoopElement extends StructureElementListElement
{
	/**
	 * Equals to maximal packet size, minus header size, minus single opcode, minus minimal loop size integer field size.<BR>
	 * A loop size that exceeds this number is invalid, as the loop elements can under no circumstances be contained in a single packet.
	 */
	public static final int MAXIMAL_SANE_LOOP_SIZE = ((1 << 16) - 1) - 2 - 1 - 2;
	
	private final String _id;
	
	/**
	 * Constructs this container.
	 * 
	 * @param id container ID
	 * @param nodes elements
	 */
	public LoopElement(String id, List<PacketStructureElement> nodes)
	{
		super(nodes);
		
		_id = id;
	}
	
	/**
	 * Returns the ID of this container.
	 * 
	 * @return loop ID
	 */
	public String getId()
	{
		return _id;
	}
	
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = super.hashCode();
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
		LoopElement other = (LoopElement)obj;
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
		return _id + super.toString();
	}
}
