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
package net.l2emuproject.proxy.ui.javafx.packet;

import java.util.Map;
import java.util.Set;

import net.l2emuproject.proxy.network.EndpointType;
import net.l2emuproject.proxy.network.meta.IPacketTemplate;

/**
 * Represents a packet table display configuration.
 * 
 * @author _dev_
 */
public interface IPacketHidingConfig
{
	/**
	 * Tests whether packets of the specified type should not be visible.
	 * 
	 * @param senderType packet sender type
	 * @param packetType packet type (template)
	 * @return is packet type hidden
	 */
	boolean isHidden(EndpointType senderType, IPacketTemplate packetType);
	
	/**
	 * Hides packets of the specified type.
	 * 
	 * @param senderType packet sender type
	 * @param packetType packet type (template)
	 */
	void setHidden(EndpointType senderType, IPacketTemplate packetType);
	
	/**
	 * Makes packets of the specified type visible.
	 * 
	 * @param senderType packet sender type
	 * @param packetType packet type (template)
	 */
	void setVisible(EndpointType senderType, IPacketTemplate packetType);
	
	/**
	 * Returns an equivalent of this packet display configuration suitable for {@link ProtocolPacketHidingManager}.
	 * 
	 * @return this configuration in an alternative format
	 */
	Map<EndpointType, Set<byte[]>> getSaveableFormat();
}
