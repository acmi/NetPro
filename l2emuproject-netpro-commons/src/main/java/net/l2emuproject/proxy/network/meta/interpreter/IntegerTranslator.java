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
package net.l2emuproject.proxy.network.meta.interpreter;

import net.l2emuproject.network.protocol.IProtocolVersion;
import net.l2emuproject.proxy.network.meta.FieldValueTranslator;
import net.l2emuproject.proxy.state.entity.context.ICacheServerID;

/**
 * An interpreter for integer values.
 * 
 * @author _dev_
 */
public interface IntegerTranslator extends FieldValueTranslator
{
	default Boolean isKnown(long value, IProtocolVersion protocol, ICacheServerID entityCacheContext)
	{
		return null;
	}
	
	default Boolean isKnown(long value, IProtocolVersion protocol)
	{
		return isKnown(value, protocol, null);
	}
	
	/**
	 * Returns the given 8-64 bit integer value translated to an user-friendly object.
	 * 
	 * @param value value
	 * @param protocol protocol version
	 * @param entityCacheContext entity existence boundary defining context
	 * @return translation
	 */
	Object translate(long value, IProtocolVersion protocol, ICacheServerID entityCacheContext);
	
	/**
	 * Returns the given 8-64 bit integer value translated to an user-friendly object.
	 * 
	 * @param value value
	 * @param protocol protocol version
	 * @return translation
	 */
	default Object translate(long value, IProtocolVersion protocol)
	{
		return translate(value, protocol, null);
	}
}
