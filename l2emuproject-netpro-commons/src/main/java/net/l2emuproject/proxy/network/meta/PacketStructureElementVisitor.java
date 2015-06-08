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

import org.apache.commons.lang3.ArrayUtils;

import net.l2emuproject.proxy.network.meta.structure.BranchElement;
import net.l2emuproject.proxy.network.meta.structure.FieldElement;
import net.l2emuproject.proxy.network.meta.structure.LoopElement;
import net.l2emuproject.proxy.network.meta.structure.PacketStructureElement;
import net.l2emuproject.proxy.network.meta.structure.field.FieldValue;
import net.l2emuproject.proxy.network.meta.structure.field.bytes.AbstractByteArrayFieldElement;
import net.l2emuproject.proxy.network.meta.structure.field.bytes.ByteArrayFieldValue;
import net.l2emuproject.proxy.network.meta.structure.field.decimal.AbstractDecimalFieldElement;
import net.l2emuproject.proxy.network.meta.structure.field.decimal.DecimalFieldValue;
import net.l2emuproject.proxy.network.meta.structure.field.integer.AbstractIntegerFieldElement;
import net.l2emuproject.proxy.network.meta.structure.field.integer.IntegerFieldValue;
import net.l2emuproject.proxy.network.meta.structure.field.string.AbstractStringFieldElement;
import net.l2emuproject.proxy.network.meta.structure.field.string.StringFieldValue;

/**
 * Allows implementors to visit each packet structure element in a concrete packet body.
 * 
 * @author _dev_
 */
public interface PacketStructureElementVisitor
{
	/**
	 * Called after the packet prefix (known opcodes) have been read.
	 * 
	 * @param bytesWithoutOpcodes remaining bytes
	 * @throws RuntimeException if any unexpected exception happens
	 */
	void onStart(int bytesWithoutOpcodes) throws RuntimeException;
	
	/**
	 * Called when a branch is about to be entered or skipped.
	 * 
	 * @param element branch descriptor
	 * @param conditionMet whether the branch will be entered
	 * @throws RuntimeException if any unexpected exception happens
	 */
	void onBranch(BranchElement element, boolean conditionMet) throws RuntimeException;
	
	/**
	 * Called when a branch is about to be exited. This means that either all branch elements have been processed, or a branch was skipped.
	 * 
	 * @param element branch descriptor
	 * @throws RuntimeException if any unexpected exception happens
	 */
	void onBranchEnd(BranchElement element) throws RuntimeException;
	
	/**
	 * Called when a loop is about to be entered.
	 * 
	 * @param element loop descriptor
	 * @param expectedIterations loop size (as read from the associated field)
	 * @throws RuntimeException if any unexpected exception happens
	 */
	void onLoopStart(LoopElement element, int expectedIterations) throws RuntimeException;
	
	/**
	 * Called when a loop iteration is about to start.
	 * 
	 * @param element loop descriptor
	 * @throws RuntimeException if any unexpected exception happens
	 */
	void onLoopIterationStart(LoopElement element) throws RuntimeException;
	
	/**
	 * Called when a loop iteration is about to end.
	 * 
	 * @param element loop descriptor
	 * @throws RuntimeException if any unexpected exception happens
	 */
	void onLoopIterationEnd(LoopElement element) throws RuntimeException;
	
	/**
	 * Called when a loop is about to be exited.
	 * 
	 * @param element loop descriptor
	 * @throws RuntimeException if any unexpected exception happens
	 */
	void onLoopEnd(LoopElement element) throws RuntimeException;
	
	/**
	 * Called when a byte array field is encountered.
	 * 
	 * @param element field descriptor
	 * @param value field value (as read from packet)
	 * @throws RuntimeException if any unexpected exception happens
	 */
	void onByteArrayField(AbstractByteArrayFieldElement element, ByteArrayFieldValue value) throws RuntimeException;
	
	/**
	 * Called when a floating point field is encountered.
	 * 
	 * @param element field descriptor
	 * @param value field value (as read from packet)
	 * @throws RuntimeException if any unexpected exception happens
	 */
	void onDecimalField(AbstractDecimalFieldElement element, DecimalFieldValue value) throws RuntimeException;
	
	/**
	 * Called when an integer field is encountered.
	 * 
	 * @param element field descriptor
	 * @param value field value (as read from packet)
	 * @throws RuntimeException if any unexpected exception happens
	 */
	void onIntegerField(AbstractIntegerFieldElement element, IntegerFieldValue value) throws RuntimeException;
	
	/**
	 * Called when a string field is encountered.
	 * 
	 * @param element field descriptor
	 * @param value field value (as read from packet)
	 * @throws RuntimeException if any unexpected exception happens
	 */
	void onStringField(AbstractStringFieldElement element, StringFieldValue value) throws RuntimeException;
	
	/**
	 * Returns additional arguments to be passed to a custom field's value reading method.
	 * 
	 * @param element field descriptor
	 * @return additional arguments ({@code null} may or may not be supported by the custom implementation)
	 * @throws RuntimeException if any unexpected exception happens
	 */
	default Object[] onCustomFieldValueRead(FieldElement<?> element) throws RuntimeException
	{
		return ArrayUtils.EMPTY_OBJECT_ARRAY;
	}
	
	/**
	 * Called when a nonstandard field is encountered.
	 * 
	 * @param element field descriptor
	 * @param value field value
	 * @throws RuntimeException if any unexpected exception happens
	 * @param <V> value type of the passed field descriptor
	 */
	default <V extends FieldValue> void onCustomField(FieldElement<V> element, V value) throws RuntimeException
	{
		// for custom fields
	}
	
	/**
	 * Called when a nonstandard condition is encountered.
	 * 
	 * @param element branch descriptor
	 * @param condition condition instance
	 * @param value conditional field value
	 * @return whether the condition is met
	 * @throws RuntimeException if any unexpected exception happens
	 */
	default boolean onCustomCondition(BranchElement element, FieldValueCondition condition, FieldValue value) throws RuntimeException
	{
		// for custom conditions
		return false;
	}
	
	/**
	 * Called when a nonstandard element is encountered.
	 * 
	 * @param element field descriptor
	 * @throws RuntimeException if any unexpected exception happens
	 */
	default void onCustomElement(PacketStructureElement element) throws RuntimeException
	{
		// for custom elements
	}
	
	/**
	 * Called if one of the field elements cannot be visited because there are not enough bytes remaining in the buffer.
	 * 
	 * @param e underlying cause
	 * @param remainingBytes amount of unread bytes still remaining in the buffer
	 * @throws RuntimeException if any unexpected exception happens
	 */
	void onAbruptTermination(BufferUnderflowException e, int remainingBytes) throws RuntimeException;
	
	/**
	 * Called if an exception is thrown while visiting elements or if a {@link RuntimeException} is thrown from any of visitor methods.
	 * 
	 * @param e an exception
	 * @param remainingBytes amount of unread bytes still remaining in the buffer
	 */
	void onException(Exception e, int remainingBytes);
	
	/**
	 * Returns the type of action to be taken if an invalid condition is encountered on a branch.<BR>
	 * <BR>
	 * Returning {@code null} will terminate visitation.<BR>
	 * Returning {@code true} will cause the branch to be entered; {@code false} will skip the branch.
	 * 
	 * @param element associated branch descriptor
	 * @param value conditional field value (as read from the associated conditional field)
	 * @return whether to continue or terminate visitation
	 * @throws RuntimeException if any unexpected exception happens
	 */
	default Boolean onInvalidCondition(BranchElement element, FieldValue value) throws RuntimeException
	{
		return null;
	}
	
	/**
	 * Returns whether to continue element visitation if an invalid interpreter is encountered on a field.
	 * 
	 * @param element associated field descriptor
	 * @return whether to continue or terminate visitation
	 * @throws RuntimeException if any unexpected exception happens
	 */
	default boolean onInvalidInterpreter(FieldElement<?> element) throws RuntimeException
	{
		return true;
	}
	
	/**
	 * Returns whether to continue element visitation if an invalid modifier is encountered on a field.
	 * 
	 * @param element associated field descriptor
	 * @return whether to continue or terminate visitation
	 * @throws RuntimeException if any unexpected exception happens
	 */
	default boolean onInvalidModifier(FieldElement<?> element) throws RuntimeException
	{
		return false;
	}
	
	/*
	default Object onInvalidInterpreter(FieldElement<?> element, FieldValue value) throws RuntimeException
	{
		return null;
	}
	
	default <T> T onInvalidModifier(FieldElement<?> element, T value) throws RuntimeException
	{
		return null;
	}
	*/
	
	/**
	 * Called after all known elements have been visited.
	 * 
	 * @param remainingBytes amount of unread bytes (0 expected for unpadded packets with correct definitions)
	 * @throws RuntimeException if any unexpected exception happens
	 */
	void onCompletion(int remainingBytes) throws RuntimeException;
}
