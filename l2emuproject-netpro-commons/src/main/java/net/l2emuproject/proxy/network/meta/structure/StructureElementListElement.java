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

import java.util.Collections;
import java.util.List;

/**
 * A node container.
 * 
 * @author savormix
 */
public abstract class StructureElementListElement implements PacketStructureElement
{
	private final List<PacketStructureElement> _nodes;
	
	/**
	 * Constructs this container node.
	 * 
	 * @param nodes elements
	 */
	public StructureElementListElement(List<PacketStructureElement> nodes)
	{
		_nodes = Collections.unmodifiableList(nodes);
	}
	
	/**
	 * Returns subelements of this node.
	 * 
	 * @return subnodes
	 */
	public List<PacketStructureElement> getNodes()
	{
		return _nodes;
	}
	
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((_nodes == null) ? 0 : _nodes.hashCode());
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
		StructureElementListElement other = (StructureElementListElement)obj;
		if (_nodes == null)
		{
			if (other._nodes != null)
				return false;
		}
		else if (!_nodes.equals(other._nodes))
			return false;
		return true;
	}
	
	@Override
	public String toString()
	{
		return _nodes.toString();
	}
}
