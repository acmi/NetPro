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
import net.l2emuproject.proxy.state.entity.context.ICacheServerID;

/**
 * Interprets the given byte/word/dword value as a bit mask.<BR>
 * <BR>
 * For every bit in 'on' state, it's interpretation is added to the returned string, delimited by a
 * space and a comma.<BR>
 * <BR>
 * If all bits in the mask are in 'off' state, a specific 'zero' interpretation is returned instead.
 * 
 * @author savormix
 */
public class BitmaskTranslator implements IntegerTranslator
{
	private final Object _zeroInterpretation;
	private final Object[] _bitInterpretations;
	
	/**
	 * Creates this interpreter.
	 * 
	 * @param bitInterpretations known bit interpretations
	 * @param passNullHere {@code null}
	 * @param zeroInterpretation interpretation of no enabled bits
	 */
	protected BitmaskTranslator(Object zeroInterpretation, Void passNullHere, Object[] bitInterpretations)
	{
		_bitInterpretations = bitInterpretations;
		_zeroInterpretation = zeroInterpretation;
	}
	
	/**
	 * Creates this interpreter.
	 * 
	 * @param bitInterpretations known bit interpretations
	 */
	protected BitmaskTranslator(Object... bitInterpretations)
	{
		this("None", null, bitInterpretations);
	}
	
	@Override
	public Object translate(long value, IProtocolVersion protocol, ICacheServerID entityCacheContext)
	{
		final StringBuilder sb = new StringBuilder();
		final int maxBit = Math.min(_bitInterpretations.length, 64);
		for (int bit = 0; bit < maxBit; ++bit)
		{
			final long mask = 1L << bit;
			if ((value & mask) == 0)
				continue;
			
			if (sb.length() > 0)
				sb.append(", ");
			
			final Object i = _bitInterpretations[bit];
			if (i != null)
			{
				sb.append(i);
				continue;
			}
			
			sb.append(bit).append('_').append(mask);
		}
		for (int bit = maxBit; bit < 64; ++bit)
		{
			final long mask = 1L << bit;
			if ((value & mask) == 0)
				continue;
			
			if (sb.length() > 0)
				sb.append(", ");
			
			sb.append(bit).append('_').append(mask);
		}
		return sb.length() > 0 ? sb.toString() : _zeroInterpretation;
	}
}
