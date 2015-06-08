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
 * An interpreter for integer values.
 * 
 * @author _dev_
 */
public interface IntegerInterpreter extends FieldValueInterpreter
{
	/**
	 * Returns an interpretation of the given 8-64 bit integer value.
	 * 
	 * @param value value
	 * @param entityCacheContext entity existence boundary defining context
	 * @return interpretation
	 */
	Object getInterpretation(long value, ICacheServerID entityCacheContext);
	
	/**
	 * Returns an interpretation of the given 8-64 bit integer value.
	 * 
	 * @param value value
	 * @return interpretation
	 */
	default Object getInterpretation(long value)
	{
		return getInterpretation(value, null);
	}
}
