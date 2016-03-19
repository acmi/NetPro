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
package net.l2emuproject.proxy.io.exception;

import java.nio.ByteBuffer;

/**
 * Thrown when an unexpected file is encountered.
 * 
 * @author _dev_
 */
public final class UnknownFileTypeException extends Exception
{
	private static final long serialVersionUID = -6139319419583416390L;
	
	private final long _magicValue;
	
	/**
	 * Constructs this exception.
	 * 
	 * @param magicValue unexpected magic value
	 */
	public UnknownFileTypeException(long magicValue)
	{
		this._magicValue = magicValue;
	}
	
	/**
	 * Returns the magic value that did not match the expected one.
	 * 
	 * @return unexpected magic value
	 */
	public long getMagicValue()
	{
		return _magicValue;
	}
	
	/**
	 * Returns the magic value that did not match the expected one.
	 * 
	 * @return unexpected magic value
	 */
	public byte[] getMagic8Bytes()
	{
		return ByteBuffer.allocate(8).putLong(_magicValue).array();
	}
}
