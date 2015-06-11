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

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;

import net.l2emuproject.network.mmocore.MMOBuffer;
import net.l2emuproject.proxy.network.meta.condition.ByteArrayCondition;
import net.l2emuproject.proxy.network.meta.condition.DecimalCondition;
import net.l2emuproject.proxy.network.meta.condition.IntegerCondition;
import net.l2emuproject.proxy.network.meta.condition.StringCondition;
import net.l2emuproject.proxy.network.meta.container.MetaclassRegistry;
import net.l2emuproject.proxy.network.meta.container.OpcodeOwnerSet;
import net.l2emuproject.proxy.network.meta.container.OpcodeOwnerSet.OpcodeOwner;
import net.l2emuproject.proxy.network.meta.exception.CompositeReadBufferUnderflowException;
import net.l2emuproject.proxy.network.meta.exception.InvalidFieldValueConditionException;
import net.l2emuproject.proxy.network.meta.exception.InvalidFieldValueInterpreterException;
import net.l2emuproject.proxy.network.meta.exception.InvalidFieldValueModifierException;
import net.l2emuproject.proxy.network.meta.exception.RunawayLoopException;
import net.l2emuproject.proxy.network.meta.structure.BranchElement;
import net.l2emuproject.proxy.network.meta.structure.FieldElement;
import net.l2emuproject.proxy.network.meta.structure.LoopElement;
import net.l2emuproject.proxy.network.meta.structure.PacketStructureElement;
import net.l2emuproject.proxy.network.meta.structure.StructureElementListElement;
import net.l2emuproject.proxy.network.meta.structure.field.FieldValue;
import net.l2emuproject.proxy.network.meta.structure.field.FieldValueReadOption;
import net.l2emuproject.proxy.network.meta.structure.field.InterpreterContext;
import net.l2emuproject.proxy.network.meta.structure.field.bytes.AbstractByteArrayFieldElement;
import net.l2emuproject.proxy.network.meta.structure.field.bytes.ByteArrayFieldValue;
import net.l2emuproject.proxy.network.meta.structure.field.bytes.DynamicSizeByteArrayFieldElement;
import net.l2emuproject.proxy.network.meta.structure.field.decimal.AbstractDecimalFieldElement;
import net.l2emuproject.proxy.network.meta.structure.field.decimal.DecimalFieldValue;
import net.l2emuproject.proxy.network.meta.structure.field.integer.AbstractIntegerFieldElement;
import net.l2emuproject.proxy.network.meta.structure.field.integer.IntegerFieldValue;
import net.l2emuproject.proxy.network.meta.structure.field.string.AbstractStringFieldElement;
import net.l2emuproject.proxy.network.meta.structure.field.string.StringFieldValue;
import net.l2emuproject.util.HexUtil;

/**
 * Represents a predefined packet template.
 * 
 * @author _dev_
 */
public final class PacketTemplate implements IPacketTemplate, Comparable<OpcodeOwner>
{
	private final byte[] _prefix;
	private final String _name;
	private final List<PacketStructureElement> _structure;
	private final boolean _hasScriptAliases;
	
	/**
	 * Creates a packet template.
	 * 
	 * @param prefix raw opcode array
	 * @param name packet name
	 * @param structure compact structure definition
	 */
	public PacketTemplate(byte[] prefix, String name, List<PacketStructureElement> structure)
	{
		_prefix = prefix;
		_name = name;
		_structure = structure;
		
		boolean hasScriptAliases = false;
		for (final PacketStructureElement elem : structure)
		{
			if (hasScriptAliases(elem))
			{
				hasScriptAliases = true;
				break;
			}
		}
		_hasScriptAliases = hasScriptAliases;
	}
	
	/**
	 * Creates a packet template without name and structure.
	 * 
	 * @param prefix raw opcode array (allowed to be truncated)
	 */
	public PacketTemplate(byte[] prefix)
	{
		this(prefix, null, Collections.emptyList());
	}
	
	@Override
	public byte[] getPrefix()
	{
		return _prefix;
	}
	
	@Override
	public String getName()
	{
		return _name;
	}
	
	@Override
	public List<PacketStructureElement> getStructure()
	{
		return _structure;
	}
	
	@Override
	public boolean isDefined()
	{
		return _name != null;
	}
	
	@Override
	public boolean isWithScriptAliases()
	{
		return _hasScriptAliases;
	}
	
	@Override
	public void visitStructureElements(PacketStructureElementVisitor visitor, ByteBuffer body, Map<FieldValueReadOption, ?> options)
	{
		final MMOBuffer buffer = new MMOBuffer().setByteBuffer(body);
		try
		{
			visitor.onStart(buffer.getAvailableBytes());
			final Map<String, FieldValue> fieldID2Value = new HashMap<>();
			for (final PacketStructureElement elem : _structure)
				visitSingleElement(elem, visitor, body, buffer, options, fieldID2Value);
			visitor.onCompletion(buffer.getAvailableBytes());
		}
		catch (BufferUnderflowException e)
		{
			final int remaining = e instanceof CompositeReadBufferUnderflowException ? ((CompositeReadBufferUnderflowException)e).getRemainingBytesBeforeRead() : buffer.getAvailableBytes();
			try
			{
				visitor.onAbruptTermination(e, remaining);
			}
			catch (RuntimeException e2)
			{
				visitor.onException(e2, remaining);
			}
		}
		catch (RuntimeException e)
		{
			visitor.onException(e, buffer.getAvailableBytes());
		}
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
	public int compareTo(OpcodeOwner other)
	{
		return OpcodeOwnerSet.COMPARATOR.compare(this, other);
	}
	
	@Override
	public String toString()
	{
		return HexUtil.bytesToHexString(_prefix, ":") + (_name != null ? " " + _name : "");
	}
	
	@SuppressWarnings("unchecked")
	private static final <V extends FieldValue> void visitSingleElement(PacketStructureElement element, PacketStructureElementVisitor visitor, ByteBuffer body, MMOBuffer buffer,
			Map<FieldValueReadOption, ?> options, Map<String, FieldValue> fieldID2Value)
	{
		if (element instanceof BranchElement)
		{
			final BranchElement branch = (BranchElement)element;
			final String id = branch.getId();
			Boolean conditionMet = null;
			if (id != null)
			{
				final FieldValue valueToTest = fieldID2Value.get(branch.getId());
				if (valueToTest == null)
					throw new NullPointerException("Missing test value for " + branch);
				
				Throwable cause = null;
				try
				{
					if (valueToTest instanceof ByteArrayFieldValue)
					{
						final ByteArrayCondition condition = MetaclassRegistry.getInstance().getCondition(branch.getCondition(), ByteArrayCondition.class);
						final ByteArrayFieldValue value = (ByteArrayFieldValue)valueToTest;
						conditionMet = condition.test(value.value());
					}
					else if (valueToTest instanceof DecimalFieldValue)
					{
						final DecimalCondition condition = MetaclassRegistry.getInstance().getCondition(branch.getCondition(), DecimalCondition.class);
						final DecimalFieldValue value = (DecimalFieldValue)valueToTest;
						conditionMet = condition.test(value.value());
					}
					else if (valueToTest instanceof IntegerFieldValue)
					{
						final IntegerCondition condition = MetaclassRegistry.getInstance().getCondition(branch.getCondition(), IntegerCondition.class);
						final IntegerFieldValue value = (IntegerFieldValue)valueToTest;
						conditionMet = condition.test(value.value());
					}
					else if (valueToTest instanceof StringFieldValue)
					{
						final StringCondition condition = MetaclassRegistry.getInstance().getCondition(branch.getCondition(), StringCondition.class);
						final StringFieldValue value = (StringFieldValue)valueToTest;
						conditionMet = condition.test(value.value());
					}
					else
					{
						final FieldValueCondition condition = MetaclassRegistry.getInstance().getCondition(branch.getCondition(), FieldValueCondition.class);
						conditionMet = visitor.onCustomCondition(branch, condition, valueToTest);
					}
				}
				catch (InvalidFieldValueConditionException e)
				{
					conditionMet = visitor.onInvalidCondition(branch, valueToTest);
					cause = e;
				}
				
				if (conditionMet == null)
					throw new IllegalArgumentException("Invalid condition type for " + branch, cause);
			}
			else
				conditionMet = Boolean.TRUE;
			
			visitor.onBranch(branch, conditionMet);
			
			if (conditionMet == Boolean.TRUE)
				for (final PacketStructureElement subElement : branch.getNodes())
					visitSingleElement(subElement, visitor, body, buffer, options, fieldID2Value);
			
			visitor.onBranchEnd(branch);
		}
		else if (element instanceof LoopElement)
		{
			final LoopElement loop = (LoopElement)element;
			final FieldValue size = fieldID2Value.get(loop.getId());
			if (size == null)
				throw new NullPointerException("Missing size for " + loop);
			
			final int iterationCount = (int)((IntegerFieldValue)size).value();
			if (iterationCount < 0 || iterationCount > LoopElement.MAXIMAL_SANE_LOOP_SIZE)
				throw new RunawayLoopException("Runaway loop (" + iterationCount + "): " + loop);
			
			visitor.onLoopStart(loop, iterationCount);
			
			for (int i = 0; i < iterationCount; ++i)
			{
				visitor.onLoopIterationStart(loop);
				for (final PacketStructureElement subElement : loop.getNodes())
					visitSingleElement(subElement, visitor, body, buffer, options, fieldID2Value);
				visitor.onLoopIterationEnd(loop);
			}
			
			visitor.onLoopEnd(loop);
		}
		else if (element instanceof FieldElement)
		{
			final FieldElement<V> field = (FieldElement<V>)element;
			
			Object[] args = ArrayUtils.EMPTY_OBJECT_ARRAY;
			FieldValue value = null;
			try
			{
				prepareForContextualReview(body, options);
				
				if (field instanceof AbstractByteArrayFieldElement)
				{
					if (field instanceof DynamicSizeByteArrayFieldElement)
					{
						final FieldValue size = fieldID2Value.get("bytesize");
						if (size == null)
							throw new NullPointerException("Missing size for " + field);
						
						final int bytesize = (int)((IntegerFieldValue)size).value();
						// the max loop size is calculated as if one byte was a loop iteration element
						if (bytesize < 0 || bytesize > LoopElement.MAXIMAL_SANE_LOOP_SIZE)
							throw new RunawayLoopException("Runaway byte array (" + bytesize + "): " + field);
						
						args = new Object[] { bytesize };
					}
					
					value = field.readValue(buffer, options, args);
					visitor.onByteArrayField((AbstractByteArrayFieldElement)field, (ByteArrayFieldValue)value);
				}
				else if (field instanceof AbstractDecimalFieldElement)
				{
					value = field.readValue(buffer, options, args);
					visitor.onDecimalField((AbstractDecimalFieldElement)field, (DecimalFieldValue)value);
				}
				else if (field instanceof AbstractIntegerFieldElement)
				{
					value = field.readValue(buffer, options, args);
					visitor.onIntegerField((AbstractIntegerFieldElement)field, (IntegerFieldValue)value);
				}
				else if (field instanceof AbstractStringFieldElement)
				{
					value = field.readValue(buffer, options, args);
					visitor.onStringField((AbstractStringFieldElement)field, (StringFieldValue)value);
				}
				else
				{
					args = visitor.onCustomFieldValueRead(field);
					value = field.readValue(buffer, options, args);
					visitor.onCustomField(field, (V)value);
				}
			}
			catch (InvalidFieldValueInterpreterException e)
			{
				if (!visitor.onInvalidInterpreter(field))
					throw new RuntimeException(e);
				
				Map<FieldValueReadOption, ?> remainingOptions = new EnumMap<>(options);
				remainingOptions.remove(FieldValueReadOption.COMPUTE_INTERPRETATION);
				try
				{
					value = field.readValue(buffer, remainingOptions, args);
				}
				catch (InvalidFieldValueInterpreterException e1)
				{
					// cannot happen
					throw new RuntimeException(e1);
				}
				catch (InvalidFieldValueModifierException e1)
				{
					if (!visitor.onInvalidModifier(field))
						throw new RuntimeException(e);
					
					remainingOptions.remove(FieldValueReadOption.APPLY_MODIFICATIONS);
					try
					{
						value = field.readValue(buffer, remainingOptions, args);
					}
					catch (InvalidFieldValueInterpreterException | InvalidFieldValueModifierException e2)
					{
						// cannot happen
						throw new RuntimeException(e2);
					}
				}
			}
			catch (InvalidFieldValueModifierException e)
			{
				if (!visitor.onInvalidModifier(field))
					throw new RuntimeException(e);
				
				Map<FieldValueReadOption, ?> remainingOptions = new EnumMap<>(options);
				remainingOptions.remove(FieldValueReadOption.APPLY_MODIFICATIONS);
				try
				{
					value = field.readValue(buffer, remainingOptions, args);
				}
				catch (InvalidFieldValueModifierException e1)
				{
					// cannot happen
					throw new RuntimeException(e1);
				}
				catch (InvalidFieldValueInterpreterException e1)
				{
					if (!visitor.onInvalidInterpreter(field))
						throw new RuntimeException(e);
					
					remainingOptions.remove(FieldValueReadOption.COMPUTE_INTERPRETATION);
					try
					{
						value = field.readValue(buffer, remainingOptions, args);
					}
					catch (InvalidFieldValueInterpreterException | InvalidFieldValueModifierException e2)
					{
						// cannot happen
						throw new RuntimeException(e2);
					}
				}
			}
			{
				final String id = field.getID();
				if (id != null)
					fieldID2Value.put(id, value);
			}
		}
		else
		{
			visitor.onCustomElement(element);
		}
	}
	
	private static final void prepareForContextualReview(ByteBuffer body, Map<FieldValueReadOption, ?> options)
	{
		final Object ctx = options.get(FieldValueReadOption.COMPUTE_INTERPRETATION);
		if (ctx == null)
			return;
		
		final InterpreterContext context = (InterpreterContext)ctx;
		context.getWireframe()._buffer.position(body.position());
	}
	
	private static final boolean hasScriptAliases(PacketStructureElement element)
	{
		if (element instanceof StructureElementListElement)
		{
			for (final PacketStructureElement subelement : ((StructureElementListElement)element).getNodes())
				if (hasScriptAliases(subelement))
					return true;
			return false;
		}
		
		if (element instanceof FieldElement)
			return !((FieldElement<?>)element).getFieldAliases().isEmpty();
		
		return false;
	}
}
