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
import net.l2emuproject.proxy.network.meta.IPacketTemplate;
import net.l2emuproject.proxy.ui.i18n.UIStrings;
import net.l2emuproject.util.HexUtil;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleBooleanProperty;

/**
 * @author _dev_
 */
public class PacketTemplateEntry
{
	private final IPacketTemplate _template;
	private final boolean _visibleInTab, _visibleInProtocol;
	private final BooleanProperty _visibleInTabProperty, _visibleInProtocolProperty;
	// precomputed values to avoid constant polling/generation
	private final ReadOnlyStringWrapper _opcode, _name;
	
	public PacketTemplateEntry(IPacketTemplate template, boolean visibleInTab, boolean visibleInProtocol, IProtocolVersion protocolVersion)
	{
		_template = template;
		_visibleInTab = visibleInTab;
		_visibleInProtocol = visibleInProtocol;
		_visibleInTabProperty = new SimpleBooleanProperty(visibleInTab);
		_visibleInProtocolProperty = new SimpleBooleanProperty(visibleInProtocol);
		_opcode = new ReadOnlyStringWrapper(HexUtil.bytesToHexString(template.getPrefix(), ":").intern());
		_name = new ReadOnlyStringWrapper(template.isDefined() ? template.getName() : UIStrings.get("packetdc.table.unknownpacket", _opcode.get()).intern());
	}
	
	public BooleanProperty visibleInTabProperty()
	{
		return _visibleInTabProperty;
	}
	
	public BooleanProperty visibleInProtocolProperty()
	{
		return _visibleInProtocolProperty;
	}
	
	/**
	 * Returns the property of the value to be displayed in the opcode column.
	 * 
	 * @return opcode(s)
	 */
	public ReadOnlyStringProperty opcodeProperty()
	{
		return _opcode.getReadOnlyProperty();
	}
	
	/**
	 * Returns the property of the value to be displayed in the template name column.
	 * 
	 * @return name
	 */
	public ReadOnlyStringProperty nameProperty()
	{
		return _name.getReadOnlyProperty();
	}
	
	@Override
	public String toString()
	{
		return _opcode.get() + " " + _name.get();
	}
}
