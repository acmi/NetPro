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
package net.l2emuproject.proxy.network.meta.container;

import java.util.Arrays;
import java.util.Comparator;

import org.apache.commons.lang3.ArrayUtils;

import javolution.util.FastComparator;
import javolution.util.FastSet;

/**
 * Implementation of a set that knows how to differentiate packets by opcode(s).
 * 
 * @author NB4L1
 */
public class OpcodeOwnerSet extends FastSet<OpcodeOwnerSet.OpcodeOwner>
{
	/** Defines an interface for an opcode owner. */
	public interface OpcodeOwner extends Comparable<OpcodeOwner>
	{
		/**
		 * Returns a byte array that contains all opcodes, in order.
		 * 
		 * @return raw opcode array
		 */
		byte[] getPrefix();
	}
	
	private static final long serialVersionUID = -6865928644610736327L;
	
	/** This comparator can both distinguish packets as well as sort them in a user-friendly ascending order. */
	public static final FastComparator<OpcodeOwner> COMPARATOR = new FastComparator<OpcodeOwner>()
	{
		private static final long serialVersionUID = -8037053272233195878L;
		
		@Override
		public boolean areEqual(OpcodeOwner o1, OpcodeOwner o2)
		{
			if (o1 == o2) // quick reference check
				return true;
			
			if (o1 == null || o2 == null) // null != object
				return false;
			
			return Arrays.equals(o1.getPrefix(), o2.getPrefix()); // extended check
		}
		
		@Override
		public int compare(OpcodeOwner o1, OpcodeOwner o2)
		{
			final byte[] prefix1 = o1.getPrefix(), prefix2 = o2.getPrefix();
			return comparePacketPrefixes(prefix1, prefix2);
		}
		
		@Override
		public int hashCodeOf(OpcodeOwner o)
		{
			return 31 + Arrays.hashCode(o.getPrefix());
		}
	};
	
	/** Creates this set. */
	public OpcodeOwnerSet()
	{
		setValueComparator(COMPARATOR);
	}
	
	/**
	 * Orders packets by prefix in Lineage II opcode style.
	 * 
	 * @param prefix1 first packet prefix
	 * @param prefix2 second packet prefix
	 * @return {@link Comparator#compare(Object, Object)}
	 */
	public static final int comparePacketPrefixes(byte[] prefix1, byte[] prefix2)
	{
		if (ArrayUtils.isEmpty(prefix1))
		{
			if (ArrayUtils.isEmpty(prefix2))
			{
				return 0;
			}
			
			return -1;
		}
		else if (ArrayUtils.isEmpty(prefix2))
			return +1;
		
		if (prefix1.length < prefix2.length)
			return -1;
		if (prefix1.length > prefix2.length)
			return +1;
		
		{
			final int first1 = prefix1[0] & 0xFF, first2 = prefix2[0] & 0xFF;
			if (first1 < first2)
				return -1;
			if (first1 > first2)
				return +1;
		}
		
		if (prefix1.length != 3 && prefix1.length != 7)
		{
			for (int i = 1; i < prefix1.length; ++i)
			{
				final int e1 = prefix1[i] & 0xFF, e2 = prefix2[i] & 0xFF;
				if (e1 < e2)
					return -1;
				if (e1 > e2)
					return +1;
			}
			return 0;
		}
		
		final int second1 = prefix1[1] & 0xFF | ((prefix1[2] & 0xFF) << 8), second2 = prefix2[1] & 0xFF | ((prefix2[2] & 0xFF) << 8);
		if (second1 < second2)
			return -1;
		if (second1 > second2)
			return +1;
		
		for (int i = 3; prefix1.length - i >= 4 && prefix2.length - i >= 4; i += 4)
		{
			final int next1 = prefix1[i] & 0xFF | ((prefix1[i + 1] & 0xFF) << 8) | ((prefix1[i + 2] & 0xFF) << 16) | ((prefix1[i + 3] & 0xFF) << 24);
			final int next2 = prefix2[i] & 0xFF | ((prefix2[i + 1] & 0xFF) << 8) | ((prefix2[i + 2] & 0xFF) << 16) | ((prefix2[i + 3] & 0xFF) << 24);
			if (next1 < next2)
				return -1;
			if (next1 > next2)
				return +1;
		}
		
		return 0;
	}
}
