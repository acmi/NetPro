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
package net.l2emuproject.proxy.io;

import java.io.Closeable;
import java.io.EOFException;
import java.io.IOException;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.util.zip.Checksum;

import net.l2emuproject.io.EmptyChecksum;

/**
 * An analogue to {@code DataOutputStream} for NIO.<BR>
 * <BR>
 * Despite what it might look like, all operations are blocking.
 * 
 * @author savormix
 */
public class NewIOHelper implements IOConstants, Closeable
{
	private final SeekableByteChannel _channel;
	private final ByteBuffer _buffer;
	private final Checksum _checksum;
	
	/**
	 * Constructs a wrapper for the given channel.
	 * 
	 * @param channel input source
	 */
	public NewIOHelper(SeekableByteChannel channel)
	{
		this(channel, DEFAULT_BUFFER_SIZE);
	}
	
	/**
	 * Constructs a wrapper for the given channel.
	 * 
	 * @param channel input source
	 * @param bufferSize heap I/O buffer size
	 */
	public NewIOHelper(SeekableByteChannel channel, int bufferSize)
	{
		this(channel, ByteBuffer.allocate/*Direct*/(bufferSize), EmptyChecksum.getInstance());
	}
	
	/**
	 * Constructs a wrapper for the given channel.
	 * 
	 * @param channel input source
	 * @param checksum checksum filter used on all operations
	 */
	public NewIOHelper(SeekableByteChannel channel, Checksum checksum)
	{
		this(channel, ByteBuffer.allocate/*Direct*/(DEFAULT_BUFFER_SIZE), checksum);
	}
	
	/**
	 * Constructs a wrapper for the given channel.
	 * 
	 * @param channel input source
	 * @param buffer heap I/O buffer
	 * @param checksum checksum filter used on all operations
	 */
	public NewIOHelper(SeekableByteChannel channel, ByteBuffer buffer, Checksum checksum)
	{
		_channel = channel;
		_buffer = buffer;
		_checksum = checksum;
		
		discardBufferContent();
	}
	
	private void discardBufferContent()
	{
		_buffer.clear().position(0).limit(0);
	}
	
	/**
	 * Returns current position in the underlying channel.
	 * 
	 * @param write output mode
	 * @return position in file
	 * @throws IOException if the underlying channel is not accessible
	 */
	public long getPositionInChannel(boolean write) throws IOException
	{
		final int diff = write ? _buffer.position() : -_buffer.remaining();
		return _channel.position() + diff;
	}
	
	/**
	 * Changes position of a channel that is associated with this helper.<BR>
	 * <BR>
	 * Calling this method will discard buffer contents. Therefore, if used for writing, {@link #flush()} must be called
	 * prior to this method.
	 * 
	 * @param newPosition position in channel
	 * @throws IOException {@link SeekableByteChannel#position(long)}
	 */
	public void setPositionInChannel(long newPosition) throws IOException
	{
		discardBufferContent();
		_channel.position(newPosition);
	}
	
	/**
	 * Returns the checksum value, calculated for operations since last reset.
	 * 
	 * @return checksum
	 */
	public long getChecksumValue()
	{
		return _checksum.getValue();
	}
	
	/**
	 * Non-blocking flush operation.
	 * 
	 * @throws IOException if the underlying channel is not accessible
	 */
	public void flushQuickly() throws IOException
	{
		_buffer.flip();
		_channel.write(_buffer);
		_buffer.compact();
	}
	
	/**
	 * Non-blocking fetch operation.
	 * 
	 * @throws IOException if the underlying channel is not accessible
	 */
	public void fetchQuickly() throws IOException
	{
		if (_channel.read(_buffer.compact()) == -1)
			throw new EOFException("Attempt to read past EOF");
		_buffer.flip();
	}
	
	private void checkOverflow(int bytes)
	{
		if (bytes > _buffer.capacity())
			throw new BufferOverflowException();
	}
	
	/**
	 * Busy-waiting flush operation.
	 * 
	 * @param bytes minimal amount of bytes to flush
	 * @throws IOException if the underlying channel is not accessible
	 */
	public void flushUntilAvailable(int bytes) throws IOException
	{
		checkOverflow(bytes);
		
		while (_buffer.remaining() < bytes)
			flushQuickly();
	}
	
	/**
	 * Busy-waiting fetch operation.
	 * 
	 * @param bytes minimal amount of bytes to fetch
	 * @throws IOException if the underlying channel is not accessible
	 */
	public void fetchUntilAvailable(int bytes) throws IOException
	{
		checkOverflow(bytes);
		
		while (_buffer.remaining() < bytes)
			fetchQuickly();
	}
	
	/**
	 * Flushes all bytes currently in the heap I/O buffer.
	 * 
	 * @throws IOException if the underlying channel is not accessible
	 */
	public void flush() throws IOException
	{
		_buffer.flip();
		while (_buffer.hasRemaining())
			_channel.write(_buffer);
		discardBufferContent();
	}
	
	/**
	 * Reads a single byte as a boolean value.
	 * 
	 * @return boolean
	 * @throws IOException if the underlying channel is not accessible
	 */
	public boolean readBoolean() throws IOException
	{
		return readByte() == 0 ? false : true;
	}
	
	/**
	 * Reads a single byte.
	 * 
	 * @return byte
	 * @throws IOException if the underlying channel is not accessible
	 */
	public int readByte() throws IOException
	{
		fetchUntilAvailable(1);
		
		final int b = _buffer.get();
		_checksum.update(b);
		return b;
	}
	
	/**
	 * Reads two bytes as an UTF16 character.
	 * 
	 * @return an unicode character
	 * @throws IOException if the underlying channel is not accessible
	 */
	public int readChar() throws IOException
	{
		fetchUntilAvailable(2);
		
		final int pos = _buffer.position();
		final int c = _buffer.getChar();
		_checksum.update(_buffer.array(), pos, 2);
		return c;
	}
	
	/**
	 * Reads four bytes as an integer value.
	 * 
	 * @return integer
	 * @throws IOException if the underlying channel is not accessible
	 */
	public int readInt() throws IOException
	{
		fetchUntilAvailable(4);
		
		final int pos = _buffer.position();
		final int i = _buffer.getInt();
		_checksum.update(_buffer.array(), pos, 4);
		return i;
	}
	
	/**
	 * Reads eight bytes as an integer value.
	 * 
	 * @return integer
	 * @throws IOException if the underlying channel is not accessible
	 */
	public long readLong() throws IOException
	{
		fetchUntilAvailable(8);
		
		final int pos = _buffer.position();
		final long l = _buffer.getLong();
		_checksum.update(_buffer.array(), pos, 8);
		return l;
	}
	
	/**
	 * Fills the given buffer with bytes from the underlying channel.
	 * 
	 * @param buf destination
	 * @throws IOException if the underlying channel is not accessible
	 */
	public void read(final ByteBuffer buf) throws IOException
	{
		final int len = buf.remaining();
		fetchUntilAvailable(len);
		
		final int pos = _buffer.position();
		final int lim = _buffer.limit();
		_buffer.limit(_buffer.position() + len);
		buf.put(_buffer);
		_buffer.limit(lim);
		_checksum.update(_buffer.array(), pos, len);
	}
	
	/**
	 * Fills the given buffer with bytes from the underlying channel.
	 * 
	 * @param buf destination
	 * @throws IOException if the underlying channel is not accessible
	 */
	public void read(final byte[] buf) throws IOException
	{
		read(buf, 0, buf.length);
	}
	
	/**
	 * Reads {@code length} bytes into the given buffer.
	 * 
	 * @param buf destination
	 * @param offset destination offset
	 * @param length amount of bytes to read
	 * @throws IOException if the underlying channel is not accessible
	 */
	public void read(final byte[] buf, int offset, int length) throws IOException
	{
		if (length < 1)
			return;
		
		fetchUntilAvailable(length);
		
		final int pos = _buffer.position();
		_buffer.get(buf, offset, length);
		_checksum.update(_buffer.array(), pos, length);
	}
	
	/**
	 * Discards {@code bytes} bytes without reading them.
	 * 
	 * @param bytes amount of bytes to skip
	 * @param write output mode
	 * @throws IOException if the underlying channel is not accessible
	 */
	public void skip(final long bytes, boolean write) throws IOException
	{
		if (bytes <= _buffer.remaining())
			_buffer.position(_buffer.position() + (int)bytes);
		else
			setPositionInChannel(getPositionInChannel(write) + bytes);
	}
	
	/**
	 * Writes a boolean value as a single byte.
	 * 
	 * @param b boolean
	 * @return {@code this}
	 * @throws IOException if the underlying channel is not accessible
	 */
	public NewIOHelper writeBoolean(final boolean b) throws IOException
	{
		return writeByte(b ? 1 : 0);
	}
	
	/**
	 * Writes a single byte.
	 * 
	 * @param b byte
	 * @return {@code this}
	 * @throws IOException if the underlying channel is not accessible
	 */
	public NewIOHelper writeByte(final int b) throws IOException
	{
		flushUntilAvailable(1);
		
		_buffer.put((byte)b);
		_checksum.update(b);
		return this;
	}
	
	/**
	 * Writes an unicode character in UTF16.
	 * 
	 * @param c character
	 * @return {@code this}
	 * @throws IOException if the underlying channel is not accessible
	 */
	public NewIOHelper writeChar(final int c) throws IOException
	{
		flushUntilAvailable(2);
		
		final int pos = _buffer.position();
		_buffer.putChar((char)c);
		_checksum.update(_buffer.array(), pos, 2);
		return this;
	}
	
	/**
	 * Writes an integer value into four bytes.
	 * 
	 * @param i integer
	 * @return {@code this}
	 * @throws IOException if the underlying channel is not accessible
	 */
	public NewIOHelper writeInt(final int i) throws IOException
	{
		flushUntilAvailable(4);
		
		final int pos = _buffer.position();
		_buffer.putInt(i);
		_checksum.update(_buffer.array(), pos, 4);
		return this;
	}
	
	/**
	 * Writes an integer value into eight bytes.
	 * 
	 * @param l integer
	 * @return {@code this}
	 * @throws IOException if the underlying channel is not accessible
	 */
	public NewIOHelper writeLong(final long l) throws IOException
	{
		flushUntilAvailable(8);
		
		final int pos = _buffer.position();
		_buffer.putLong(l);
		_checksum.update(_buffer.array(), pos, 8);
		return this;
	}
	
	/**
	 * Writes {@code buf.remaining()} bytes into the underlying channel.
	 * 
	 * @param buf source
	 * @return {@code this}
	 * @throws IOException if the underlying channel is not accessible
	 */
	public NewIOHelper write(final ByteBuffer buf) throws IOException
	{
		final int len = buf.remaining();
		flushUntilAvailable(len);
		
		final int pos = _buffer.position();
		_buffer.put(buf);
		_checksum.update(_buffer.array(), pos, len);
		return this;
	}
	
	/**
	 * Writes {@code buf.length} bytes into the underlying channel.
	 * 
	 * @param buf source
	 * @return {@code this}
	 * @throws IOException if the underlying channel is not accessible
	 */
	public NewIOHelper write(final byte[] buf) throws IOException
	{
		return write(buf, 0, buf.length);
	}
	
	/**
	 * Writes {@code length} bytes into the underlying channel.
	 * 
	 * @param buf source
	 * @param offset source offset
	 * @param length bytes to write
	 * @return {@code this}
	 * @throws IOException if the underlying channel is not accessible
	 */
	public NewIOHelper write(final byte[] buf, int offset, int length) throws IOException
	{
		flushUntilAvailable(length);
		
		final int pos = _buffer.position();
		_buffer.put(buf, offset, length);
		_checksum.update(_buffer.array(), pos, length);
		return this;
	}
	
	/**
	 * {@inheritDoc} Due to the ambiguous nature of this class, {@link #flush()} is not called from
	 * this method.
	 */
	@Override
	public void close() throws IOException
	{
		_channel.close();
	}
}
