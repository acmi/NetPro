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
package net.l2emuproject.proxy.network.meta;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.l2emuproject.network.mmocore.MMOBuffer;
import net.l2emuproject.network.protocol.IProtocolVersion;
import net.l2emuproject.proxy.network.Proxy;
import net.l2emuproject.proxy.network.meta.exception.InvalidFieldValueInterpreterException;
import net.l2emuproject.proxy.network.meta.exception.InvalidFieldValueModifierException;
import net.l2emuproject.proxy.network.meta.structure.FieldElement;
import net.l2emuproject.proxy.network.meta.structure.field.bytes.DynamicSizeByteArrayFieldElement;
import net.l2emuproject.proxy.network.meta.structure.field.bytes.FixedSizeByteArrayFieldElement;
import net.l2emuproject.proxy.network.meta.structure.field.decimal.AbstractDecimalFieldElement;
import net.l2emuproject.proxy.network.meta.structure.field.integer.AbstractIntegerFieldElement;
import net.l2emuproject.proxy.network.meta.structure.field.string.AbstractStringFieldElement;
import net.l2emuproject.proxy.network.packets.IPacketSource;

/**
 * This class is subject to change.
 * 
 * @author _dev_
 */
public class RandomAccessMMOBuffer
{
	final ByteBuffer _buffer;
	private final MMOBuffer _buf;
	private Map<String, List<EnumeratedPayloadField>> _fields;
	private String _packetName;
	
	// extension fields
	private final IProtocolVersion _protocol;
	private final IPacketSource _packetSource;
	
	/**
	 * Creates a buffer wrapper with enumerated field support.
	 * 
	 * @param buffer a buffer wrapper to read known data types
	 * @param protocol associated protocol version
	 * @param packetAuthor packet source identifier
	 * @throws IllegalAccessException if {@link MMOBuffer} API changes
	 * @throws NoSuchFieldException if {@link MMOBuffer} API changes
	 */
	public RandomAccessMMOBuffer(final MMOBuffer buffer, IProtocolVersion protocol, IPacketSource packetAuthor)
			throws IllegalAccessException, NoSuchFieldException
	{
		_buf = buffer;
		_fields = Collections.emptyMap();
		_packetName = null;
		
		// we do not want MMOBuffer to depend on this class
		final Field buf = MMOBuffer.class.getDeclaredField("_buffer");
		buf.setAccessible(true);
		_buffer = (ByteBuffer)buf.get(buffer);
		
		_protocol = protocol;
		_packetSource = packetAuthor;
	}
	
	/**
	 * Returns whether a specific field alias has an offset mapping (at least one).
	 * 
	 * @param field field alias
	 * @return whether a field can be seeked in this buffer
	 */
	public boolean isEnumerated(String field)
	{
		return _fields.containsKey(field);
	}
	
	/**
	 * Reads a byte array from the given field. The field must define a byte array of a fixed size.
	 * 
	 * @param field fixed size byte array field
	 * @return byte array
	 * @throws IllegalArgumentException if a field of invalid type is passed
	 */
	public byte[] readBytes(EnumeratedPayloadField field) throws IllegalArgumentException
	{
		final FieldElement<?> elem = field.getElement();
		if (!(elem instanceof FixedSizeByteArrayFieldElement))
			throw new IllegalArgumentException("Not a fixed size byte array field: " + field);
		
		try
		{
			return ((FixedSizeByteArrayFieldElement)elem).readValue(seekField(field), Collections.emptyMap()).value();
		}
		catch (InvalidFieldValueInterpreterException | InvalidFieldValueModifierException e)
		{
			// mod/interp disabled, cannot happen
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Reads a byte array of the given size from the given field. The field must define a byte array without a fixed size.
	 * 
	 * @param field dynamic size byte array field
	 * @param bytesize amount of bytes to read
	 * @return byte array
	 * @throws IllegalArgumentException if a field of invalid type is passed
	 */
	public byte[] readBytes(EnumeratedPayloadField field, int bytesize) throws IllegalArgumentException
	{
		final FieldElement<?> elem = field.getElement();
		if (!(elem instanceof DynamicSizeByteArrayFieldElement))
			throw new IllegalArgumentException("Not a dynamic size byte array field: " + field);
		
		try
		{
			return ((DynamicSizeByteArrayFieldElement)elem).readValue(seekField(field), Collections.emptyMap(), bytesize).value();
		}
		catch (InvalidFieldValueInterpreterException | InvalidFieldValueModifierException e)
		{
			// mod/interp disabled, cannot happen
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Reads a floating point value from the first field with the given name.
	 * 
	 * @param fieldName field name
	 * @return floating point value
	 * @throws IllegalArgumentException if a nonexistent field or a field of invalid type is passed
	 */
	public double readFirstDecimal(String fieldName) throws IllegalArgumentException
	{
		final EnumeratedPayloadField field = getSingleFieldIndex(fieldName);
		if (field == null)
			throw new IllegalArgumentException("No such field: " + fieldName);
		return readDecimal(field);
	}
	
	/**
	 * Reads a floating point value from the given field.
	 * 
	 * @param field decimal field
	 * @return floating point value
	 * @throws IllegalArgumentException if a field of invalid type is passed
	 */
	public double readDecimal(EnumeratedPayloadField field) throws IllegalArgumentException
	{
		final FieldElement<?> elem = field.getElement();
		if (!(elem instanceof AbstractDecimalFieldElement))
			throw new IllegalArgumentException("Not a floating point field: " + field);
		
		try
		{
			return ((AbstractDecimalFieldElement)elem).readValue(seekField(field), Collections.emptyMap()).value();
		}
		catch (InvalidFieldValueInterpreterException | InvalidFieldValueModifierException e)
		{
			// mod/interp disabled, cannot happen
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Reads an integer value from the first field with the given name and truncates it to 32 bits.
	 * 
	 * @param fieldName field name
	 * @return integer value
	 * @throws IllegalArgumentException if a field of invalid type is passed
	 */
	public int readFirstInteger32(String fieldName) throws IllegalArgumentException
	{
		return (int)readFirstInteger(fieldName);
	}
	
	/**
	 * Reads an integer value from the first field with the given name.
	 * 
	 * @param fieldName field name
	 * @return integer value
	 * @throws IllegalArgumentException if a field of invalid type is passed
	 */
	public long readFirstInteger(String fieldName) throws IllegalArgumentException
	{
		final EnumeratedPayloadField field = getSingleFieldIndex(fieldName);
		if (field == null)
			throw new IllegalArgumentException("No such field: " + fieldName);
		return readInteger(field);
	}
	
	/**
	 * Reads an integer value from the given field and truncates it to 32 bits.
	 * 
	 * @param field integer field
	 * @return integer value
	 * @throws IllegalArgumentException if a field of invalid type is passed
	 */
	public int readInteger32(EnumeratedPayloadField field) throws IllegalArgumentException
	{
		return (int)readInteger(field);
	}
	
	/**
	 * Reads an integer value from the given field.
	 * 
	 * @param field integer field
	 * @return integer value
	 * @throws IllegalArgumentException if a field of invalid type is passed
	 */
	public long readInteger(EnumeratedPayloadField field) throws IllegalArgumentException
	{
		final FieldElement<?> elem = field.getElement();
		if (!(elem instanceof AbstractIntegerFieldElement))
			throw new IllegalArgumentException("Not an integer field: " + field);
		
		try
		{
			return ((AbstractIntegerFieldElement)elem).readValue(seekField(field), Collections.emptyMap()).value();
		}
		catch (InvalidFieldValueInterpreterException | InvalidFieldValueModifierException e)
		{
			// mod/interp disabled, cannot happen
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Reads a string value from the first field with the given name.
	 * 
	 * @param fieldName field name
	 * @return a string
	 * @throws IllegalArgumentException if a field of invalid type is passed
	 */
	public String readFirstString(String fieldName) throws IllegalArgumentException
	{
		final EnumeratedPayloadField field = getSingleFieldIndex(fieldName);
		if (field == null)
			throw new IllegalArgumentException("No such field: " + fieldName);
		return readString(field);
	}
	
	/**
	 * Reads a string value from the given field.
	 * 
	 * @param field string field
	 * @return a string
	 * @throws IllegalArgumentException if a field of invalid type is passed
	 */
	public String readString(EnumeratedPayloadField field) throws IllegalArgumentException
	{
		final FieldElement<?> elem = field.getElement();
		if (!(elem instanceof AbstractStringFieldElement))
			throw new IllegalArgumentException("Not a string field: " + field);
		
		try
		{
			return ((AbstractStringFieldElement)elem).readValue(seekField(field), Collections.emptyMap()).value();
		}
		catch (InvalidFieldValueInterpreterException | InvalidFieldValueModifierException e)
		{
			// mod/interp disabled, cannot happen
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Returns all offset mappings valid in this buffer for the given field alias.
	 * If there are none, the returned list will be empty.
	 * 
	 * @param field field alias
	 * @return all associated offset mappings
	 */
	public List<EnumeratedPayloadField> getFieldIndices(String field)
	{
		final List<EnumeratedPayloadField> list = _fields.get(field);
		if (list != null)
			return list;
		
		return Collections.emptyList();
	}
	
	/**
	 * Returns the first offset mapping for the given field alias.
	 * If there are none, returns {@code null}.
	 * 
	 * @param field field alias
	 * @return field offset mapping
	 */
	public EnumeratedPayloadField getSingleFieldIndex(String field)
	{
		final List<EnumeratedPayloadField> list = getFieldIndices(field);
		if (!list.isEmpty())
			return list.get(0);
		
		return null;
	}
	
	/**
	 * Changes the internal buffer's position to the given field's offset.
	 * 
	 * @param field field offset mapping
	 * @return buffer wrapper
	 */
	public MMOBuffer seekField(EnumeratedPayloadField field)
	{
		_buffer.position(field.getOffset());
		return _buf;
	}
	
	/**
	 * Changes the internal buffer's position so that the next read byte is the packet's first opcode.
	 * 
	 * @return buffer wrapper
	 */
	public MMOBuffer seekFirstOpcode()
	{
		_buffer.position(0);
		return _buf;
	}
	
	/**
	 * Returns current internal buffer's position.
	 * 
	 * @return buffer's position
	 */
	public int getCurrentOffset()
	{
		return _buffer.position();
	}
	
	/**
	 * Returns a buffer wrapper that supports known data type reading operations.
	 * 
	 * @return buffer wrapper
	 */
	public MMOBuffer getMMOBuffer()
	{
		return _buf;
	}
	
	/**
	 * Returns all enumerated field aliases, whose offsets are known by this buffer.
	 * 
	 * @return enumerated field aliases
	 */
	public Set<String> getAllFields()
	{
		return _fields.keySet();
	}
	
	Map<String, List<EnumeratedPayloadField>> getEnumeratedFields()
	{
		return _fields;
	}
	
	/**
	 * Stores the enumeration result in this buffer wrapper.
	 * 
	 * @param fields enumeration result
	 */
	public void setEnumeratedFields(Map<String, List<EnumeratedPayloadField>> fields)
	{
		_fields = fields;
	}
	
	/**
	 * Returns the packet name (if applicable).
	 * 
	 * @return packet name
	 */
	public String getPacketName()
	{
		return _packetName;
	}
	
	/**
	 * Specifies the name of the packet template used during enumeration.
	 * 
	 * @param packetName packet name
	 */
	public void setPacketName(String packetName)
	{
		_packetName = packetName;
	}
	
	// extension fields
	/**
	 * Returns the protocol version used to enumerate fields in this buffer.
	 * 
	 * @return network protocol version in use
	 */
	public IProtocolVersion getProtocol()
	{
		return _protocol;
	}
	
	/**
	 * Returns a connection endpoint, which sent this packet (if applicable). Returns {@code null} if this packet is from a historical packet log.
	 * This will also return {@code null} if the wrong endpoint type class is passed (e.g. server for a client packet).
	 * 
	 * @param clazz connection endpoint class
	 * @return interactive author or {@code null}
	 * @param <T> expected return type
	 */
	public <T extends Proxy> T getInteractivePacketSource(Class<? extends T> clazz)
	{
		return clazz.isInstance(_packetSource) ? clazz.cast(_packetSource) : null;
	}
	
	@Override
	public String toString()
	{
		return _buf + " " + _fields;
	}
}
