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

import java.nio.ByteBuffer;

/**
 * Various utilities to ensure specific <TT>ByteBuffer</TT> properties without allocating a new one
 * every time.
 * 
 * @author savormix
 */
public final class ByteBufferUtils
{
	/**
	 * Returns a read-only version of <TT>buf</TT>. <BR>
	 * <BR>
	 * Order and position/mark/limit will be retained.<BR>
	 * Content will be and position/mark/limit may be shared.<BR>
	 * 
	 * @param buf byte buffer
	 * @return read-only view of given buffer
	 */
	public static final ByteBuffer asReadOnly(ByteBuffer buf)
	{
		if (buf == null || buf.isReadOnly())
			return buf;
		else
			return buf.asReadOnlyBuffer().order(buf.order());
	}
	
	/**
	 * Returns a mutable version of <TT>buf</TT>. <BR>
	 * <BR>
	 * Order will be retained, but position/mark/limit will not.<BR>
	 * Content and position/mark/limit may be shared.<BR>
	 * The returned buffer will be direct if and only if the given buffer is direct. <BR>
	 * <BR>
	 * This method is not thread safe!
	 * 
	 * @param buf byte buffer
	 * @return mutable version of given buffer
	 */
	public static final ByteBuffer asMutable(ByteBuffer buf)
	{
		if (buf == null || !buf.isReadOnly())
			return buf;
		
		buf.clear();
		final ByteBuffer mutable;
		if (buf.isDirect())
			mutable = ByteBuffer.allocateDirect(buf.capacity());
		else
			mutable = ByteBuffer.allocate(buf.capacity());
		
		return mutable.put(buf).order(buf.order());
	}
	
	/**
	 * Returns an array backed version of <TT>buf</TT>. <BR>
	 * <BR>
	 * Order will be retained, but position/mark/limit will not.<BR>
	 * Content and position/mark/limit may be shared.<BR>
	 * The returned buffer will be read-only if and only if the given buffer is read-only. <BR>
	 * <BR>
	 * This method is not thread safe!
	 * 
	 * @param buf byte buffer
	 * @return mutable version of given buffer
	 */
	public static final ByteBuffer asBacked(ByteBuffer buf)
	{
		if (buf == null || !buf.isDirect())
			return buf;
		
		buf.clear();
		ByteBuffer backed = ByteBuffer.allocate(buf.capacity()).put(buf);
		if (buf.isReadOnly())
			backed = backed.asReadOnlyBuffer();
		
		return backed.order(buf.order());
	}
	
	private ByteBufferUtils()
	{
		// utility class
	}
}
