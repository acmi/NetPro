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

import net.l2emuproject.network.protocol.IProtocolVersion;
import net.l2emuproject.proxy.io.definitions.VersionnedPacketTable;
import net.l2emuproject.proxy.network.meta.IPacketTemplate;
import net.l2emuproject.proxy.ui.ReceivedPacket;
import net.l2emuproject.proxy.ui.i18n.UIStrings;
import net.l2emuproject.util.HexUtil;

import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;

/**
 * A packet wrapper for the packet table view.
 * 
 * @author _dev_
 */
public final class PacketLogEntry
{
	private static final ReadOnlyStringProperty SENDER_CLIENT = new ReadOnlyStringWrapper("C").getReadOnlyProperty();
	private static final ReadOnlyStringProperty SENDER_SERVER = new ReadOnlyStringWrapper("S").getReadOnlyProperty();
	
	private final ReceivedPacket _packet;
	// precomputed values to avoid constant polling/generation
	private final ReadOnlyStringWrapper _opcode, _name;
	
	/**
	 * Creates a packet wrapper.
	 * 
	 * @param packet a packet
	 */
	public PacketLogEntry(ReceivedPacket packet)
	{
		_packet = packet;
		
		_opcode = new ReadOnlyStringWrapper("XX");
		_name = new ReadOnlyStringWrapper("PacketNameHere");
	}
	
	/**
	 * Returns the associated packet.
	 * 
	 * @return the packet
	 */
	public ReceivedPacket getPacket()
	{
		return _packet;
	}
	
	/**
	 * Returns the value to be displayed in the first column.
	 * 
	 * @return sender
	 */
	public String getSender()
	{
		return senderProperty().get();
	}
	
	/**
	 * Returns the property of the value to be displayed in the first column.
	 * 
	 * @return opcode(s)
	 */
	public ReadOnlyStringProperty senderProperty()
	{
		return _packet.getEndpoint().isClient() ? SENDER_CLIENT : SENDER_SERVER;
	}
	
	/**
	 * Returns the value to be displayed in the second column.
	 * 
	 * @return opcode(s)
	 */
	public String getOpcode()
	{
		return _opcode.get();
	}
	
	/**
	 * Returns the property of the value to be displayed in the second column.
	 * 
	 * @return opcode(s)
	 */
	public ReadOnlyStringProperty opcodeProperty()
	{
		return _opcode.getReadOnlyProperty();
	}
	
	/**
	 * Returns the value to be displayed in the third column.
	 * 
	 * @return name
	 */
	public String getName()
	{
		return _name.get();
	}
	
	/**
	 * Returns the property of the value to be displayed in the third column.
	 * 
	 * @return name
	 */
	public ReadOnlyStringProperty nameProperty()
	{
		return _name.getReadOnlyProperty();
	}
	
	/**
	 * (Generates and) caches the user-friendly packet opcode and name values, as defined by {@code version}.
	 * 
	 * @param version network protocol version
	 */
	public void updateView(IProtocolVersion version)
	{
		final IPacketTemplate packetTemplate = VersionnedPacketTable.getInstance().getTemplate(version, _packet.getEndpoint(), _packet.getBody());
		
		_opcode.set(HexUtil.bytesToHexString(packetTemplate.getPrefix(), ":").intern());
		_name.set(packetTemplate.isDefined() ? packetTemplate.getName() : UIStrings.get("packetdc.table.unknownpacket", _opcode.get()).intern());
	}
	
	@Override
	public String toString()
	{
		return "[" + getSender() + "] " + _opcode.get() + " " + _name.get();
	}
}
