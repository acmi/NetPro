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
package net.l2emuproject.proxy.network;

import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import net.l2emuproject.lang.L2TextBuilder;

/**
 * Allows to specify the prefix part of an IPv4 address.
 * 
 * @author _dev_
 */
public class IPv4AddressPrefix
{
	private final int _prefix, _prefixLength;
	
	/**
	 * Constructs a prefix.
	 * 
	 * @param address address or prefix
	 * @param prefixLength length of prefix
	 */
	public IPv4AddressPrefix(int address, int prefixLength)
	{
		_prefix = prefixOf(address, prefixLength);
		_prefixLength = prefixLength;
	}
	
	/**
	 * Constructs a prefix.
	 * 
	 * @param address address
	 * @param prefixLength length of prefix
	 */
	public IPv4AddressPrefix(byte[] address, int prefixLength)
	{
		this(convert(address), prefixLength);
	}
	
	/**
	 * Constructs a prefix.
	 * 
	 * @param address address
	 * @param prefixLength length of prefix
	 */
	public IPv4AddressPrefix(InetAddress address, int prefixLength)
	{
		this(address.getAddress(), prefixLength);
	}
	
	/**
	 * Returns whether the given IPv4 address matches this prefix.
	 * 
	 * @param address an address
	 * @return {@code true} if the given address is IPv4 and matches this prefix, {@code false} otherwise
	 */
	public boolean isIncluded(InetAddress address)
	{
		final byte[] addr = address.getAddress();
		if (addr.length != 4)
			return false;
		if (_prefixLength < 1)
			return true;
		
		return _prefix == prefixOf(convert(addr), _prefixLength);
	}
	
	/**
	 * Returns whether this prefix is a part of the given prefix or vice versa.
	 * 
	 * @param other IPv4 address prefix
	 * @return whether prefixes overlap each other
	 */
	public boolean isOverlapping(IPv4AddressPrefix other)
	{
		final int minLength = Math.min(_prefixLength, other._prefixLength);
		return prefixOf(_prefix, minLength) == prefixOf(other._prefix, minLength);
	}
	
	private static final int prefixOf(int address, int prefixLength)
	{
		final int shift = 32 - prefixLength;
		return (address << shift) >>> shift;
	}
	
	private static final int convert(byte[] address) throws IllegalArgumentException
	{
		if (address.length != 4)
			throw new IllegalArgumentException();
		
		return ByteBuffer.wrap(address).order(ByteOrder.LITTLE_ENDIAN).getInt();
	}
	
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + _prefix;
		result = prime * result + _prefixLength;
		return result;
	}
	
	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		IPv4AddressPrefix other = (IPv4AddressPrefix)obj;
		if (_prefix != other._prefix)
			return false;
		if (_prefixLength != other._prefixLength)
			return false;
		return true;
	}
	
	@Override
	public String toString()
	{
		final L2TextBuilder tb = new L2TextBuilder().append(_prefix & 0xFF);
		for (int i = 1; i < 4; ++i)
			tb.append('.').append((_prefix >> (i << 3)) & 0xFF);
		return tb.append('/').append(_prefixLength).moveToString();
	}
}
