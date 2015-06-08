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
package net.l2emuproject.proxy.ui.savormix.io;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import net.l2emuproject.proxy.network.meta.FieldValueCondition;
import net.l2emuproject.proxy.network.meta.FieldValueInterpreter;
import net.l2emuproject.proxy.network.meta.FieldValueModifier;
import net.l2emuproject.proxy.network.meta.IPacketTemplate;
import net.l2emuproject.proxy.network.meta.PacketTemplate;
import net.l2emuproject.proxy.network.meta.container.MetaclassRegistry;
import net.l2emuproject.proxy.network.meta.exception.InvalidFieldValueConditionException;
import net.l2emuproject.proxy.network.meta.exception.InvalidFieldValueInterpreterException;
import net.l2emuproject.proxy.network.meta.exception.InvalidFieldValueModifierException;
import net.l2emuproject.proxy.network.meta.structure.BranchElement;
import net.l2emuproject.proxy.network.meta.structure.LoopElement;
import net.l2emuproject.proxy.network.meta.structure.PacketStructureElement;
import net.l2emuproject.proxy.network.meta.structure.field.bytes.DynamicSizeByteArrayFieldElement;
import net.l2emuproject.proxy.network.meta.structure.field.bytes.FixedSizeByteArrayFieldElement;
import net.l2emuproject.proxy.network.meta.structure.field.decimal.DoublePrecisionFPElement;
import net.l2emuproject.proxy.network.meta.structure.field.decimal.SinglePrecisionFPElement;
import net.l2emuproject.proxy.network.meta.structure.field.integer.Int16FieldElement;
import net.l2emuproject.proxy.network.meta.structure.field.integer.Int32FieldElement;
import net.l2emuproject.proxy.network.meta.structure.field.integer.Int64FieldElement;
import net.l2emuproject.proxy.network.meta.structure.field.integer.Int8FieldElement;
import net.l2emuproject.proxy.network.meta.structure.field.integer.UInt16FieldElement;
import net.l2emuproject.proxy.network.meta.structure.field.integer.UInt8FieldElement;
import net.l2emuproject.proxy.network.meta.structure.field.string.NulTerminatedUTF16StringElement;
import net.l2emuproject.proxy.network.meta.structure.field.string.SizedUTF16StringElement;
import net.l2emuproject.util.HexUtil;
import net.l2emuproject.util.L2XMLUtils;
import net.l2emuproject.util.logging.L2Logger;

/**
 * @author _dev_
 */
class EndpointPacketLoader extends SimpleFileVisitor<Path>
{
	private static final L2Logger LOG = L2Logger.getLogger(EndpointPacketLoader.class);
	
	private final Map<String, byte[]> _id2Prefix;
	private final Map<String, String> _id2Name;
	private final Map<String, IPacketTemplate> _packetsByID;
	private final Set<IPacketTemplate> _packets;
	
	private int _added, _updated;
	
	EndpointPacketLoader(Map<String, byte[]> id2Prefix, Map<String, String> id2Name, Map<String, IPacketTemplate> packetsByID, ByteBuffer structureBuffer)
	{
		_id2Prefix = id2Prefix;
		_id2Name = id2Name;
		{
			_packetsByID = packetsByID;
			_packets = new TreeSet<>();
			
			final Map<byte[], String> prefix2TemplateName = new HashMap<>();
			for (final Entry<String, IPacketTemplate> e : packetsByID.entrySet())
			{
				final byte[] realPrefix = _id2Prefix.get(e.getKey());
				if (realPrefix == null)
					continue;
				
				final IPacketTemplate template = e.getValue();
				_packets.add(new PacketTemplate(realPrefix, template.getName(), template.getStructure()));
				
				final String first = prefix2TemplateName.putIfAbsent(realPrefix, template.getName());
				if (first == null)
					continue;
				
				LOG.warn(HexUtil.bytesToHexString(realPrefix, ":") + " " + first + " conflicts with " + template.getName());
			}
		}
	}
	
	@Override
	public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException
	{
		final FileVisitResult result = super.visitFile(file, attrs);
		
		final Node packet;
		try
		{
			packet = L2XMLUtils.getChildNodeByName(L2XMLUtils.getXMLFile(file), "packet");
		}
		catch (ParserConfigurationException | SAXException e)
		{
			LOG.fatal(file.getFileName(), e);
			return result;
		}
		
		final String id = L2XMLUtils.getString(packet, "id");
		final String name = _id2Name.get(id);
		if (name == null)
		{
			// Wrong ID
			LOG.warn("No such packet: " + id + " in " + file);
			return result;
		}
		
		final byte[] prefix = _id2Prefix.get(id);
		if (prefix == null)
		{
			// Defining a packet that's not even used in this protocol? Cool…
			LOG.warn("Opcode(s) are not mapped for packet " + id + " (" + name + ")");
			return result;
		}
		
		final IPacketTemplate template = new PacketTemplate(prefix, name, childrenOf(packet, name));
		final IPacketTemplate previousDefinition = _packetsByID.remove(id);
		if (previousDefinition != null)
		{
			if (template.getStructure().equals(previousDefinition.getStructure()))
			{
				// copy-paste with 0 adjustments; keep the old (== new) template
				_packetsByID.put(id, previousDefinition);
				LOG.warn("Redundant packet definition: " + file.getParent().getParent().getParent().relativize(file));
				// Files.delete(file); // allows automated clone pruning
				return result;
			}
			
			// the previous definition might be mapped to a different set of opcodes
			_packets.remove(template);
			
			++_updated;
		}
		else
			++_added;
		
		_packetsByID.put(id, template);
		_packets.add(template);
		
		return result;
	}
	
	private static final List<PacketStructureElement> childrenOf(Node xmlNode, String packetName)
	{
		final ArrayList<PacketStructureElement> children = new ArrayList<>();
		for (final Node n : L2XMLUtils.listNodes(xmlNode))
		{
			final PacketStructureElement elem = toStructureElement(n, packetName);
			if (elem != null)
				children.add(elem);
		}
		children.trimToSize();
		return children;
	}
	
	private static final PacketStructureElement toStructureElement(Node xmlNode, String packetName)
	{
		if ("loop".equals(xmlNode.getNodeName()))
		{
			final String id = L2XMLUtils.getAttribute(xmlNode, "id");
			return new LoopElement(id, childrenOf(xmlNode, packetName));
		}
		else if ("branch".equals(xmlNode.getNodeName()))
		{
			final List<PacketStructureElement> elements = childrenOf(xmlNode, packetName);
			
			final String id = L2XMLUtils.getAttribute(xmlNode, "id", null);
			final String condition = L2XMLUtils.getAttribute(xmlNode, "condition", null);
			if (id != null && condition != null)
			{
				try
				{
					MetaclassRegistry.getInstance().getCondition(condition, FieldValueCondition.class);
					return new BranchElement(id, condition, elements);
				}
				catch (InvalidFieldValueConditionException e)
				{
					LOG.error(packetName, e);
					return null;
				}
			}
			
			return new BranchElement(elements);
		}
		
		final String alias = L2XMLUtils.getAttribute(xmlNode, "alias", null);
		if (alias == null)
			return null;
		
		final String id = L2XMLUtils.getAttribute(xmlNode, "id", null);
		String valueModifier = L2XMLUtils.getAttribute(xmlNode, "mod", null);
		if (valueModifier != null)
		{
			try
			{
				MetaclassRegistry.getInstance().getModifier(valueModifier, FieldValueModifier.class);
			}
			catch (InvalidFieldValueModifierException e)
			{
				LOG.error(packetName, e);
				// do not re-report this error during runtime
				valueModifier = null;
			}
		}
		String valueInterpreter = L2XMLUtils.getAttribute(xmlNode, "type", null);
		if (valueInterpreter != null)
		{
			try
			{
				MetaclassRegistry.getInstance().getInterpreter(valueInterpreter, FieldValueInterpreter.class);
			}
			catch (InvalidFieldValueInterpreterException e)
			{
				LOG.error(packetName, e);
				// do not re-report this error during runtime
				valueInterpreter = null;
			}
		}
		final boolean optional = L2XMLUtils.getNodeAttributeBooleanValue(xmlNode, "optional", false);
		final Set<String> fieldAliases = new HashSet<>();
		for (final Node n : L2XMLUtils.listNodesByNodeName(xmlNode, "scriptAlias"))
			fieldAliases.add(L2XMLUtils.getAttribute(n, "id"));
		
		// now, select type of element
		switch (xmlNode.getNodeName())
		{
			case "bytes":
				final Node len = L2XMLUtils.getChildNodeByName(xmlNode, "length");
				if (len == null)
					return new DynamicSizeByteArrayFieldElement(id, alias, optional, fieldAliases, valueModifier, valueInterpreter);
				
				final String str = len.getTextContent().trim();
				int length = 0;
				try
				{
					length = Integer.parseInt(str);
				}
				catch (NumberFormatException e)
				{
					LOG.error("'" + alias + "' length: " + str, e);
				}
				return new FixedSizeByteArrayFieldElement(id, alias, optional, fieldAliases, valueModifier, valueInterpreter, length);
			case "byte":
				return new Int8FieldElement(id, alias, optional, fieldAliases, valueModifier, valueInterpreter);
			case "unsignedByte":
				return new UInt8FieldElement(id, alias, optional, fieldAliases, valueModifier, valueInterpreter);
			case "word":
				return new Int16FieldElement(id, alias, optional, fieldAliases, valueModifier, valueInterpreter);
			case "unsignedWord":
				return new UInt16FieldElement(id, alias, optional, fieldAliases, valueModifier, valueInterpreter);
			case "dword":
				return new Int32FieldElement(id, alias, optional, fieldAliases, valueModifier, valueInterpreter);
			case "qword":
				return new Int64FieldElement(id, alias, optional, fieldAliases, valueModifier, valueInterpreter);
			case "single":
				return new SinglePrecisionFPElement(id, alias, optional, fieldAliases, valueModifier, valueInterpreter);
			case "double":
				return new DoublePrecisionFPElement(id, alias, optional, fieldAliases, valueModifier, valueInterpreter);
			case "string":
			case "ntstring":
				return new NulTerminatedUTF16StringElement(id, alias, optional, fieldAliases, valueModifier, valueInterpreter);
			case "sstring":
				return new SizedUTF16StringElement(id, alias, optional, fieldAliases, valueModifier, valueInterpreter);
			default: // whitespace, comment, etc
				return null;
		}
	}
	
	int getAdded()
	{
		return _added;
	}
	
	int getUpdated()
	{
		return _updated;
	}
	
	Set<IPacketTemplate> getPackets()
	{
		return _packets;
	}
}
