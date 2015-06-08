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
package net.l2emuproject.proxy.ui.savormix.component.packet.config;

import java.util.Locale;

import net.l2emuproject.lang.L2TextBuilder;
import net.l2emuproject.proxy.network.meta.IPacketTemplate;
import net.l2emuproject.util.HexUtil;

/**
 * A packet display config list entry.
 * 
 * @author savormix
 */
class PacketConfig implements Comparable<PacketConfig>
{
	private final IPacketTemplate _packet;
	private final String _opcodes;
	private boolean _enabled;
	
	public PacketConfig(IPacketTemplate packet, boolean selected)
	{
		_packet = packet;
		
		if (packet != IPacketTemplate.ANY_DYNAMIC_PACKET)
		{
			final byte[] prefix = packet.getPrefix();
			final L2TextBuilder sb = HexUtil.fillHex(new L2TextBuilder(2 + 3 * (prefix.length - 1)), prefix[0] & 0xFF, 2, null);
			for (int i = 1; i < prefix.length; ++i)
				HexUtil.fillHex(sb.append(':'), prefix[i] & 0xFF, 2, null);
			_opcodes = sb.toString().toUpperCase(Locale.ENGLISH).intern();
		}
		else
			_opcodes = "???";
		_enabled = selected;
	}
	
	@Override
	public int compareTo(PacketConfig pc)
	{
		return _packet.compareTo(pc.getPacket());
	}
	
	public IPacketTemplate getPacket()
	{
		return _packet;
	}
	
	public String getOpcodes()
	{
		return _opcodes;
	}
	
	public boolean isEnabled()
	{
		return _enabled;
	}
	
	/**
	 * Toggles display.
	 * 
	 * @return {@code !isEnabled()}
	 */
	public boolean toggle()
	{
		boolean val = !isEnabled();
		setEnabled(val);
		return val;
	}
	
	private void setEnabled(boolean enabled)
	{
		_enabled = enabled;
	}
}
