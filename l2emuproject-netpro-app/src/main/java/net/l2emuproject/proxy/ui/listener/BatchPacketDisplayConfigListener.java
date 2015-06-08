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

import java.util.Set;

import net.l2emuproject.network.IProtocolVersion;
import net.l2emuproject.proxy.network.meta.IPacketTemplate;

/**
 * Processes configuration changes in batches and thus does not introduce lag when changing display configuration for each individual packet template (like PHX does).
 * 
 * @author savormix
 */
public interface BatchPacketDisplayConfigListener
{
	/**
	 * Notifies that different packets should be displayed.<BR>
	 * The supplied sets should be treated as immutable (unmodifiable). <BR>
	 * <BR>
	 * <TT>added</TT> and <TT>removed</TT> sets may be <TT>null</TT> to indicate that no packets
	 * were added/removed.<BR>
	 * If all <TT>added</TT> and <TT>removed</TT> sets are <TT>null</TT> then the notifier does not
	 * supply this kind of information and <TT>displayed</TT> sets should be used to apply changes. <BR>
	 * <BR>
	 * If present, <TT>ANY_UNKNOWN_PACKET</TT> indicates what to do with every unknown packet.
	 * 
	 * @param version protocol version affected
	 * @param displayedClientPackets all client packets that must be displayed
	 * @param addedClientPackets client packets to include
	 * @param removedClientPackets client packets to exclude
	 * @param displayedServerPackets all server packets that must be displayed
	 * @param addedServerPackets server packets to include
	 * @param removedServerPackets server packets to exclude
	 * @see IPacketTemplate#ANY_DYNAMIC_PACKET
	 * @see IPacketTemplate#isDefined()
	 */
	void displayConfigChanged(IProtocolVersion version, Set<IPacketTemplate> displayedClientPackets, Set<IPacketTemplate> addedClientPackets, Set<IPacketTemplate> removedClientPackets, Set<IPacketTemplate> displayedServerPackets, Set<IPacketTemplate> addedServerPackets, Set<IPacketTemplate> removedServerPackets);
}
