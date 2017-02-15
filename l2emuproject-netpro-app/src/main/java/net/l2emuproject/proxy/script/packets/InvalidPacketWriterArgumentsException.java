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
package net.l2emuproject.proxy.script.packets;

import java.util.Arrays;

/**
 * An exception to notify the caller about invalid (or no longer valid) packet send method arguments.
 *
 * @author _dev_
 */
public class InvalidPacketWriterArgumentsException extends RuntimeException
{
	private static final long serialVersionUID = 7512960027104743176L;
	
	/**
	 * Creates an exception with a custom description.
	 * 
	 * @param message description
	 */
	public InvalidPacketWriterArgumentsException(String message)
	{
		super(message);
	}
	
	/**
	 * Creates an exception caused by another concrete exception.
	 * 
	 * @param args arguments that were passed
	 * @param cause exception during send
	 */
	public InvalidPacketWriterArgumentsException(Object[] args, Exception cause)
	{
		this(Arrays.deepToString(args), cause);
	}
	
	/**
	 * Creates an exception caused by another concrete exception with a custom description.
	 * 
	 * @param message description
	 * @param cause exception during send
	 */
	public InvalidPacketWriterArgumentsException(String message, Exception cause)
	{
		super(message, cause);
	}
}
