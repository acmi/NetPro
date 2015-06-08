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
package net.l2emuproject.proxy.network.packets;

import java.nio.ByteBuffer;

import net.l2emuproject.network.mmocore.MMOBuffer;
import net.l2emuproject.network.mmocore.SendablePacket;
import net.l2emuproject.proxy.network.Proxy;

/**
 * A generic packet that can be sent to any connection endpoint.
 * 
 * @author savormix
 */
public final class ProxyRepeatedPacket extends SendablePacket<Proxy, ProxyReceivedPacket, ProxyRepeatedPacket>
{
	private final byte[] _body;
	
	/**
	 * Constructs a sendable packet.
	 * 
	 * @param body
	 *            packet body without header
	 */
	public ProxyRepeatedPacket(byte... body)
	{
		_body = body;
	}
	
	/**
	 * Constructs a sendable packet.
	 * 
	 * @param body
	 *            packet body without header
	 */
	public ProxyRepeatedPacket(ByteBuffer body)
	{
		if (body.isReadOnly())
		{
			final byte[] bytes = new byte[body.capacity()];
			
			body.clear();
			body.get(bytes);
			
			_body = bytes;
		}
		else
			_body = body.array();
	}
	
	@Override
	protected void write(Proxy client, MMOBuffer buf) throws RuntimeException
	{
		buf.writeB(_body);
	}
}
