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
package net.l2emuproject.proxy.ui.savormix.component.packet;

import java.util.Locale;

import net.l2emuproject.lang.L2TextBuilder;
import net.l2emuproject.network.IProtocolVersion;
import net.l2emuproject.proxy.network.meta.IPacketTemplate;
import net.l2emuproject.proxy.ui.ReceivedPacket;
import net.l2emuproject.proxy.ui.savormix.io.VersionnedPacketTable;
import net.l2emuproject.util.HexUtil;

/**
 * A packet list entry with precomputed fields to speed up table rendering.
 * 
 * @author _dev_
 */
final class PacketListEntry
{
	// Keep fields at minimum!
	private final ReceivedPacket _packet;
	private String _opcode;
	private String _name;
	
	PacketListEntry(IProtocolVersion version, ReceivedPacket packet)
	{
		_packet = packet;
		
		queryTemplate(version);
	}
	
	void queryTemplate(IProtocolVersion version)
	{
		final IPacketTemplate packetTemplate = VersionnedPacketTable.getInstance().getTemplate(version, _packet.getEndpoint(), _packet.getBody());
		
		final byte[] prefix = packetTemplate.getPrefix();
		final L2TextBuilder sb = HexUtil.fillHex(new L2TextBuilder(2 + 3 * (prefix.length - 1)), prefix[0] & 0xFF, 2, null);
		for (int i = 1; i < prefix.length; ++i)
			HexUtil.fillHex(sb.append(':'), prefix[i] & 0xFF, 2, null);
		_opcode = sb.toString().toUpperCase(Locale.ENGLISH).intern();
		
		_name = packetTemplate.getName() != null ? packetTemplate.getName() : ("Unknown " + _opcode.toUpperCase(Locale.ENGLISH)).intern();
	}
	
	ReceivedPacket getPacket()
	{
		return _packet;
	}
	
	String getName()
	{
		return _name;
	}
	
	String getSender()
	{
		return getPacket().getEndpoint().isClient() ? "C" : "S";
	}
	
	String getOpcode()
	{
		return _opcode;
	}
}
