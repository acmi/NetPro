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

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;

import net.l2emuproject.network.protocol.IProtocolVersion;
import net.l2emuproject.proxy.network.meta.container.OpcodeOwnerSet;
import net.l2emuproject.proxy.network.meta.container.OpcodeOwnerSet.OpcodeOwner;
import net.l2emuproject.proxy.network.meta.structure.PacketStructureElement;
import net.l2emuproject.proxy.network.meta.structure.field.FieldValueReadOption;

/**
 * Represents a packet type: specifies the opcode prefix, name and the expected structure (format).
 * 
 * @author _dev_
 */
public interface IPacketTemplate extends OpcodeOwner
{
	/** Represents any possible packet, regardless of how many opcodes it may have. Used to indicate that a packet is not known or expected. */
	final IPacketTemplate ANY_DYNAMIC_PACKET = new IPacketTemplate()
	{
		@Override
		public byte[] getPrefix()
		{
			return ArrayUtils.EMPTY_BYTE_ARRAY;
		}
		
		@Override
		public String getName()
		{
			return "Unknown";
		}
		
		@Override
		public List<PacketStructureElement> getStructure()
		{
			return Collections.emptyList();
		}
		
		@Override
		public boolean isWithScriptAliases()
		{
			return false;
		}
		
		@Override
		public IProtocolVersion getDefinitionVersion()
		{
			return null;
		}
		
		@Override
		public void visitStructureElements(PacketStructureElementVisitor visitor, ByteBuffer body, Map<FieldValueReadOption, ?> options)
		{
			visitor.onStart(body.remaining());
			visitor.onCompletion(body.remaining());
		}
		
		@Override
		public int hashCode()
		{
			return OpcodeOwnerSet.COMPARATOR.hashCodeOf(this);
		}
		
		@Override
		public boolean equals(Object obj)
		{
			if (!(obj instanceof OpcodeOwner))
				return false;
			
			return OpcodeOwnerSet.COMPARATOR.areEqual(this, (OpcodeOwner)obj);
		}
		
		@Override
		public int compareTo(OpcodeOwner o)
		{
			return OpcodeOwnerSet.COMPARATOR.compare(this, o);
		}
	};
	
	/**
	 * Specifies the packet prefix. It is composed of up to three opcodes:
	 * <OL>
	 * <LI>Primary: {@code byte} (char)
	 * <LI>Secondary: {@code char} (word, optional)
	 * <LI>Tertiary: {@code int} (dword, optional)
	 * </OL>
	 * 
	 * @return mandatory packet prefix (in little-endian byte order)
	 */
	@Override
	byte[] getPrefix();
	
	/**
	 * Specifies an user-friendly name of this packet template.
	 * 
	 * @return packet name
	 */
	String getName();
	
	/**
	 * Returns the expected packet structure, in a proprietary format.
	 * 
	 * @return expected structure (packet format)
	 */
	List<PacketStructureElement> getStructure();
	
	/**
	 * Returns the version of the protocol that originally declared this definition, if applicable.
	 * 
	 * @return earliest protocol to use this definition (or {@code null})
	 */
	IProtocolVersion getDefinitionVersion();
	
	/**
	 * Specifies whether this is a predefined or an auto-generated packet template.
	 * 
	 * @return {@code true} if this template is predefined, {@code false} otherwise
	 */
	default boolean isDefined()
	{
		return getDefinitionVersion() != null;
	}
	
	/**
	 * Specifies whether this template contains any fields with script aliases that may be used by PPE enabled scripts.
	 * 
	 * @return should a packet based on this template be handled by the PPE script registry
	 */
	boolean isWithScriptAliases();
	
	/**
	 * Walks through the given packet {@code body} assuming data is laid out the way it is described by structural nodes returned by {@link #getStructure()}.
	 * It is assumed that the packet body starts at buffer's position and extends to the buffer's limit.
	 * 
	 * @param visitor actions to be executed while walking through packet's elements
	 * @param body packet's body buffer
	 * @param options additional actions to be performed with their arguments
	 */
	void visitStructureElements(PacketStructureElementVisitor visitor, ByteBuffer body, Map<FieldValueReadOption, ?> options);
}
