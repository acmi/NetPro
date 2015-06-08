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
package net.l2emuproject.proxy.network.meta.interpreter.impl;

import net.l2emuproject.proxy.network.meta.interpreter.ByteArrayInterpreter;
import net.l2emuproject.proxy.state.entity.context.ICacheServerID;

/**
 * Interprets the first 4 bytes of the given byte array as an IPv4 address.
 * 
 * @author savormix
 */
public class IPv4 implements ByteArrayInterpreter
{
	@Override
	public Object getInterpretation(byte[] value, ICacheServerID entityCacheContext)
	{
		final StringBuilder sb = new StringBuilder(String.valueOf(value[0] & 0xFF));
		for (int i = 1; i < 4; i++)
		{
			sb.append('.');
			sb.append(value[i] & 0xFF);
		}
		return sb.toString();
	}
}
