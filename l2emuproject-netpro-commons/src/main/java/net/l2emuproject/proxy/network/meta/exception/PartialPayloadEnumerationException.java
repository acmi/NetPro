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
package net.l2emuproject.proxy.network.meta.exception;

import java.nio.BufferUnderflowException;

import net.l2emuproject.proxy.network.meta.IPacketTemplate;
import net.l2emuproject.proxy.network.meta.RandomAccessMMOBuffer;

/**
 * An exception that indicates that a packet's body did not meet the predefined packet structure template.
 * 
 * @author _dev_
 */
public class PartialPayloadEnumerationException extends Exception
{
	private static final long serialVersionUID = 4766588317420311987L;
	
	private final RandomAccessMMOBuffer _buffer;
	private final IPacketTemplate _template;
	private final int _unusedBytes;
	
	/**
	 * Constructs an exception to notify about extra bytes, unknown to the packet template.
	 * 
	 * @param buffer packet body buffer
	 * @param template selected packet template
	 * @param unusedBytes amount of extra bytes
	 */
	public PartialPayloadEnumerationException(RandomAccessMMOBuffer buffer, IPacketTemplate template, int unusedBytes)
	{
		super("remaining (unknown) bytes: " + unusedBytes);
		
		_buffer = buffer;
		_template = template;
		_unusedBytes = unusedBytes;
	}
	
	/**
	 * Constructs an exception to notify about missing bytes, that are expected by the packet template.
	 * 
	 * @param buffer packet body buffer
	 * @param template selected packet template
	 * @param cause actual runtime exception
	 */
	public PartialPayloadEnumerationException(RandomAccessMMOBuffer buffer, IPacketTemplate template,
			BufferUnderflowException cause)
	{
		super(cause);
		
		_buffer = buffer;
		_template = template;
		_unusedBytes = -1;
	}
	
	/**
	 * Constructs an exception to notify about a misaligned or outright incorrect packet template.
	 * 
	 * @param buffer packet body buffer
	 * @param template selected packet template
	 * @param cause actual runtime exception
	 */
	public PartialPayloadEnumerationException(RandomAccessMMOBuffer buffer, IPacketTemplate template,
			RunawayLoopException cause)
	{
		super(cause);
		
		_buffer = buffer;
		_template = template;
		_unusedBytes = -1;
	}
	
	/**
	 * Returns the associated packet body buffer.
	 * 
	 * @return packet wrapper
	 */
	public RandomAccessMMOBuffer getBuffer()
	{
		return _buffer;
	}
	
	/**
	 * Returns the associated packet structure definition template.
	 * 
	 * @return packet template
	 */
	public IPacketTemplate getTemplate()
	{
		return _template;
	}
	
	/**
	 * Returns the amount of extra bytes that are unknown to the packet definition or -1.
	 * 
	 * @return extra bytes or -1
	 */
	public int getUnusedBytes()
	{
		return _unusedBytes;
	}
}
