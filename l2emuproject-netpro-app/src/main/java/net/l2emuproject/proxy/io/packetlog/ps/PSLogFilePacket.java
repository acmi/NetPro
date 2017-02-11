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
package net.l2emuproject.proxy.io.packetlog.ps;

import net.l2emuproject.proxy.io.packetlog.AbstractLogFilePacket;
import net.l2emuproject.proxy.network.EndpointType;

/**
 * Represents a packet loaded from a PacketSamurai/YAL historical packet log file.
 * 
 * @author _dev_
 */
public class PSLogFilePacket extends AbstractLogFilePacket
{
	/**
	 * Constructs a historical packet wrapper.
	 * 
	 * @param endpoint packet type
	 * @param content packet body
	 * @param receivalTime time of reception
	 */
	public PSLogFilePacket(EndpointType endpoint, byte[] content, long receivalTime)
	{
		super(endpoint, content, receivalTime);
	}
}
