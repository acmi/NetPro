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
package net.l2emuproject.proxy.ui.savormix.io.exception;

import java.io.IOException;

/**
 * This exception notifies the user that an invalid file has been selected.
 * 
 * @author savormix
 */
public class UnsupportedFileException extends IOException
{
	private static final long serialVersionUID = 3157554728983785543L;
	
	/**
	 * Constructs an exception with the given message.
	 * 
	 * @param message a message
	 */
	public UnsupportedFileException(String message)
	{
		super(message);
	}
	
	/**
	 * Constructs an exception with the given cause.
	 * 
	 * @param cause underlying cause
	 */
	public UnsupportedFileException(Throwable cause)
	{
		super(cause);
	}
	
	/**
	 * Constructs an exception with the given message and cause.
	 * 
	 * @param message a message
	 * @param cause underlying cause
	 */
	public UnsupportedFileException(String message, Throwable cause)
	{
		super(message, cause);
	}
}
