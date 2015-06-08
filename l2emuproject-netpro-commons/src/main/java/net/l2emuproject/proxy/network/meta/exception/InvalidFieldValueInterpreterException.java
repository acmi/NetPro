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

import net.l2emuproject.proxy.network.meta.FieldValueInterpreter;

/**
 * An exception notifying about an unknown value interpreter type.
 * 
 * @author _dev_
 */
public class InvalidFieldValueInterpreterException extends Exception
{
	private static final long serialVersionUID = -1270092148489433306L;
	
	/**
	 * Constructs this exception.
	 * 
	 * @param interpreter interpreter
	 * @param alias interpreter type alias
	 * @param expectedSuperclass expected supertype
	 */
	public InvalidFieldValueInterpreterException(FieldValueInterpreter interpreter, String alias,
			String expectedSuperclass)
	{
		super(interpreter + "(" + alias + ")" + " cannot be cast to " + expectedSuperclass);
	}
}
