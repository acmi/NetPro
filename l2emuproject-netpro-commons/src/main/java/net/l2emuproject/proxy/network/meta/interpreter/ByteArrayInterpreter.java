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

import net.l2emuproject.proxy.network.meta.FieldValueInterpreter;
import net.l2emuproject.proxy.state.entity.context.ICacheServerID;

/**
 * An interpreter for byte array values.
 * 
 * @author _dev_
 */
public interface ByteArrayInterpreter extends FieldValueInterpreter
{
	/**
	 * Returns an interpretation of the given byte array.
	 * 
	 * @param value byte array
	 * @param entityCacheContext entity existence boundary defining context
	 * @return interpretation
	 */
	Object getInterpretation(byte[] value, ICacheServerID entityCacheContext);
	
	/**
	 * Returns an interpretation of the given byte array.
	 * 
	 * @param value byte array
	 * @return interpretation
	 */
	default Object getInterpretation(byte[] value)
	{
		return getInterpretation(value, null);
	}
}
