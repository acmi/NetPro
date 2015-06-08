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

import java.nio.ByteBuffer;

import net.l2emuproject.proxy.network.meta.IPacketTemplate;

/**
 * A packet resolution result that specifies a concrete packet template.
 * 
 * @author _dev_
 */
final class PacketResolution implements IPacketResolver
{
	private final IPacketTemplate _result;
	
	PacketResolution(IPacketTemplate result)
	{
		_result = result;
	}
	
	@Override
	public IPacketTemplate resolve(byte[] packet, int offset, int length)
	{
		return _result;
	}
	
	@Override
	public IPacketTemplate resolve(byte[] packet)
	{
		return _result;
	}
	
	@Override
	public IPacketTemplate resolve(ByteBuffer packet)
	{
		return _result;
	}
	
	@Override
	public String toString()
	{
		return _result.toString();
	}
}
