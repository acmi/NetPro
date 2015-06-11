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

/**
 * Additionally provides information about the buffer's state before sub-reads have started.
 * 
 * @author _dev_
 */
public class CompositeReadBufferUnderflowException extends BufferUnderflowException
{
	private static final long serialVersionUID = 1373953706045933980L;
	
	private final int _remainingBytesBeforeRead;
	
	/**
	 * Constructs this exception.
	 * 
	 * @param cause underlying exception
	 * @param remainingBytesBeforeRead total remaining bytes before the first sub-read
	 */
	public CompositeReadBufferUnderflowException(BufferUnderflowException cause, int remainingBytesBeforeRead)
	{
		initCause(cause);
		
		_remainingBytesBeforeRead = remainingBytesBeforeRead;
	}
	
	/**
	 * Returns the number of bytes remaining in the buffer before the composite read started.
	 * 
	 * @return total remaining bytes before the first sub-read
	 */
	public int getRemainingBytesBeforeRead()
	{
		return _remainingBytesBeforeRead;
	}
}
