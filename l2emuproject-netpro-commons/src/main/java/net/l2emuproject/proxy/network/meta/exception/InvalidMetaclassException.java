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

/**
 * An exception notifying about an unknown metatype.
 * 
 * @author _dev_
 */
public class InvalidMetaclassException extends Exception
{
	private static final long serialVersionUID = 4947158472906915867L;
	
	private final String _alias;
	private final boolean _missing;
	
	/**
	 * Constructs this exception.
	 * 
	 * @param subtype metaclass subtype
	 * @param meta metaclass instance
	 * @param alias meta type alias
	 * @param expectedSuperclass expected supertype
	 */
	public InvalidMetaclassException(String subtype, Object meta, String alias, String expectedSuperclass)
	{
		super(meta != null ? (subtype + " " + meta + "(" + alias + ")" + " cannot be cast to " + expectedSuperclass) : (subtype + " '" + alias + "' is missing"));
		
		_alias = alias;
		_missing = meta == null;
	}
	
	/**
	 * Returns the associated interpreter.
	 * 
	 * @return interpreter alias
	 */
	public String getAlias()
	{
		return _alias;
	}
	
	/**
	 * Returns whether the associated interpreter is missing.
	 * 
	 * @return is interpreter missing
	 */
	public boolean isMissing()
	{
		return _missing;
	}
}
