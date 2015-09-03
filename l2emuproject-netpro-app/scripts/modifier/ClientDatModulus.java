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
package modifier;

import java.nio.ByteBuffer;

import net.l2emuproject.proxy.network.meta.modifier.ByteArrayModifier;
import net.l2emuproject.proxy.script.modifier.ScriptedFieldValueModifier;

/**
 * Unmasks the modulus sent by the client.
 * 
 * @author _dev_
 */
public abstract class ClientDatModulus extends ScriptedFieldValueModifier implements ByteArrayModifier
{
	private final long _key;
	
	/**
	 * Constructs this modifier.
	 * 
	 * @param key XOR key
	 */
	protected ClientDatModulus(long key)
	{
		_key = key;
	}
	
	@Override
	public byte[] apply(byte[] value)
	{
		final ByteBuffer buf = ByteBuffer.wrap(value);
		for (int pos = 0; pos < buf.limit(); pos += 8)
			buf.putLong(pos, buf.getLong(pos) ^ _key);
		return value;
	}
}
