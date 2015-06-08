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

/**
 * Hosts a changeable PPE instance.
 * 
 * @author _dev_
 */
public final class SimplePpeProvider
{
	private SimplePpeProvider()
	{
		// utility class
	}
	
	private static PacketPayloadEnumerator INSTANCE;
	
	/**
	 * Returns a PPE instance.<BR>
	 * <BR>
	 * Returns {@code null} if {@link #initialize(PacketPayloadEnumerator)} has not been called.
	 * 
	 * @return a packet payload enumerator
	 */
	public static PacketPayloadEnumerator getPacketPayloadEnumerator()
	{
		return INSTANCE;
	}
	
	/**
	 * Changes the provided PPE instance to the given one.
	 * 
	 * @param ppe a packet payload enumerator
	 */
	public static void initialize(PacketPayloadEnumerator ppe)
	{
		INSTANCE = ppe;
	}
}
