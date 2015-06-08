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
package net.l2emuproject.proxy.ui.listener;

import net.l2emuproject.proxy.network.meta.IPacketTemplate;

/**
 * A simple listener that handles configuration changes as they are made.
 * 
 * @author savormix
 * @deprecated If displaying a large number of packets, using this listener may introduce noticeable stuttering when changing packet display configuration.
 */
@Deprecated
public interface PacketDisplayConfigListener
{
	/**
	 * Notifies that the given login client packet should either become displayed or removed from
	 * display. <BR>
	 * <BR>
	 * If the given packet is <TT>ANY_UNKNOWN_PACKET</TT>, apply the notification to EVERY unknown
	 * packet.
	 * 
	 * @param packet packet template
	 * @param display whether to show or not
	 * @see IPacketTemplate#ANY_DYNAMIC_PACKET
	 * @see IPacketTemplate#isDefined()
	 */
	void displayConfigChangedLoginClient(IPacketTemplate packet, boolean display);
	
	/**
	 * Notifies that the given login server packet should either become displayed or removed from
	 * display. <BR>
	 * <BR>
	 * If the given packet is <TT>ANY_UNKNOWN_PACKET</TT>, apply the notification to EVERY unknown
	 * packet.
	 * 
	 * @param packet packet template
	 * @param display whether to show or not
	 * @see IPacketTemplate#ANY_DYNAMIC_PACKET
	 * @see IPacketTemplate#isDefined()
	 */
	void displayConfigChangedLoginServer(IPacketTemplate packet, boolean display);
	
	/**
	 * Notifies that the given game client packet should either become displayed or removed from
	 * display. <BR>
	 * <BR>
	 * If the given packet is <TT>ANY_UNKNOWN_PACKET</TT>, apply the notification to EVERY unknown
	 * packet.
	 * 
	 * @param packet packet template
	 * @param display whether to show or not
	 * @see IPacketTemplate#ANY_DYNAMIC_PACKET
	 * @see IPacketTemplate#isDefined()
	 */
	void displayConfigChangedGameClient(IPacketTemplate packet, boolean display);
	
	/**
	 * Notifies that the given game server packet should either become displayed or removed from
	 * display. <BR>
	 * <BR>
	 * If the given packet is <TT>ANY_UNKNOWN_PACKET</TT>, apply the notification to EVERY unknown
	 * packet.
	 * 
	 * @param packet packet template
	 * @param display whether to show or not
	 * @see IPacketTemplate#ANY_DYNAMIC_PACKET
	 * @see IPacketTemplate#isDefined()
	 */
	void displayConfigChangedGameServer(IPacketTemplate packet, boolean display);
}
