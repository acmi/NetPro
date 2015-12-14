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

import net.l2emuproject.proxy.network.meta.FieldValueModifier;

/**
 * An exception notifying about an unknown value modifier type.
 * 
 * @author _dev_
 */
public class InvalidFieldValueModifierException extends InvalidMetaclassException
{
	private static final long serialVersionUID = -6556069888668510588L;
	
	/**
	 * Constructs this exception.
	 * 
	 * @param modifier modifier
	 * @param alias modifier type alias
	 * @param expectedSuperclass expected supertype
	 */
	public InvalidFieldValueModifierException(FieldValueModifier modifier, String alias, String expectedSuperclass)
	{
		super(modifier, alias, expectedSuperclass);
	}
}
