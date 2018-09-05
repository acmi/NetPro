/*
 * Copyright 2011-2018 L2EMU UNIQUE
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import net.l2emuproject.network.protocol.IProtocolVersion;

/**
 * @author savormix
 * @param <T> protocol type
 */
public class ProtocolTreeNode<T extends IProtocolVersion>
{
	private final T _protocol;
	private final List<ProtocolTreeNode<T>> _children;
	
	public ProtocolTreeNode(T protocol)
	{
		_protocol = protocol;
		_children = new ArrayList<>(1); // typical case is 0 to 1 child
	}
	
	public T getProtocol()
	{
		return _protocol;
	}
	
	public void addChild(T protocol)
	{
		_children.add(new ProtocolTreeNode<>(protocol));
	}
	
	public List<ProtocolTreeNode<T>> getChildren()
	{
		return _children;
	}
	
	public static <T extends IProtocolVersion> ProtocolTreeNode<T> fromMap(Map<String, String> id2PID, Map<String, T> id2Protocol)
	{
		final ProtocolTreeNode<T> root = new ProtocolTreeNode<>(null);
		childrenFromMap(root, id2PID, id2Protocol);
		return root;
	}
	
	private static <T extends IProtocolVersion> void childrenFromMap(ProtocolTreeNode<T> node, Map<String, String> id2PID, Map<String, T> id2Protocol)
	{
		String nodeID = null;
		if (node.getProtocol() != null)
		{
			for (final Entry<String, T> e : id2Protocol.entrySet())
			{
				if (e.getValue() == node.getProtocol())
				{
					nodeID = e.getKey();
					break;
				}
			}
			if (nodeID == null)
			{
				throw new IllegalArgumentException(node.getProtocol().toString());
			}
		}
		for (final Entry<String, String> e : id2PID.entrySet())
		{
			if (e.getValue() == nodeID)
				node.addChild(Objects.requireNonNull(id2Protocol.get(e.getKey())));
		}
		for (final ProtocolTreeNode<T> child : node.getChildren())
		{
			childrenFromMap(child, id2PID, id2Protocol);
		}
	}
	
	@Override
	public String toString()
	{
		return "ProtocolTreeNode[" + _protocol + "]";
	}
}
