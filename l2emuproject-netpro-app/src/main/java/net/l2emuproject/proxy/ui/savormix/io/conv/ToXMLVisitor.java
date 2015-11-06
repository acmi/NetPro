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
package net.l2emuproject.proxy.ui.savormix.io.conv;

import java.awt.image.RenderedImage;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import net.l2emuproject.network.mmocore.MMOBuffer;
import net.l2emuproject.network.protocol.IProtocolVersion;
import net.l2emuproject.proxy.network.ServiceType;
import net.l2emuproject.proxy.network.meta.IPacketTemplate;
import net.l2emuproject.proxy.network.meta.L2PpeProvider;
import net.l2emuproject.proxy.network.meta.PacketStructureElementVisitor;
import net.l2emuproject.proxy.network.meta.RandomAccessMMOBuffer;
import net.l2emuproject.proxy.network.meta.exception.InvalidFieldValueInterpreterException;
import net.l2emuproject.proxy.network.meta.exception.InvalidFieldValueModifierException;
import net.l2emuproject.proxy.network.meta.exception.InvalidPacketOpcodeSchemeException;
import net.l2emuproject.proxy.network.meta.exception.PartialPayloadEnumerationException;
import net.l2emuproject.proxy.network.meta.structure.BranchElement;
import net.l2emuproject.proxy.network.meta.structure.FieldElement;
import net.l2emuproject.proxy.network.meta.structure.LoopElement;
import net.l2emuproject.proxy.network.meta.structure.field.FieldValue;
import net.l2emuproject.proxy.network.meta.structure.field.FieldValueReadOption;
import net.l2emuproject.proxy.network.meta.structure.field.InterpreterContext;
import net.l2emuproject.proxy.network.meta.structure.field.bytes.AbstractByteArrayFieldElement;
import net.l2emuproject.proxy.network.meta.structure.field.bytes.ByteArrayFieldValue;
import net.l2emuproject.proxy.network.meta.structure.field.decimal.AbstractDecimalFieldElement;
import net.l2emuproject.proxy.network.meta.structure.field.decimal.DecimalFieldValue;
import net.l2emuproject.proxy.network.meta.structure.field.integer.AbstractIntegerFieldElement;
import net.l2emuproject.proxy.network.meta.structure.field.integer.IntegerFieldValue;
import net.l2emuproject.proxy.network.meta.structure.field.string.AbstractStringFieldElement;
import net.l2emuproject.proxy.network.meta.structure.field.string.StringFieldValue;
import net.l2emuproject.proxy.script.LogLoadScriptManager;
import net.l2emuproject.proxy.ui.ReceivedPacket;
import net.l2emuproject.proxy.ui.savormix.component.packet.DataType;
import net.l2emuproject.proxy.ui.savormix.io.LogFileHeader;
import net.l2emuproject.proxy.ui.savormix.io.VersionnedPacketTable;
import net.l2emuproject.proxy.ui.savormix.io.base.IOConstants;
import net.l2emuproject.proxy.ui.savormix.io.task.HistoricalLogPacketVisitor;
import net.l2emuproject.proxy.ui.savormix.io.task.HistoricalPacketLog;
import net.l2emuproject.util.HexUtil;
import net.l2emuproject.util.logging.L2Logger;

/**
 * Writes a raw data stream based on visited packets.
 * 
 * @author _dev_
 */
public class ToXMLVisitor implements HistoricalLogPacketVisitor, IOConstants
{
	static final L2Logger LOG = L2Logger.getLogger(ToXMLVisitor.class);
	
	private final MMOBuffer _buf;
	
	private IProtocolVersion _protocol;
	private HistoricalPacketLog _cacheContext;
	BufferedWriter _writer;
	
	/** Constructs this visitor. */
	public ToXMLVisitor()
	{
		_buf = new MMOBuffer();
	}
	
	@Override
	public void onStart(LogFileHeader logHeader) throws Exception
	{
		final Path logFile = logHeader.getLogFile();
		
		_protocol = logHeader.getProtocolVersion();
		_cacheContext = new HistoricalPacketLog(logFile);
		
		_writer = Files.newBufferedWriter(logFile.resolveSibling(logFile.getFileName() + ".xml"));
		_writer.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n<packetLog>\r\n");
	}
	
	@Override
	public void onPacket(ReceivedPacket packet) throws Exception
	{
		final boolean client = packet.getEndpoint().isClient();
		_writer.append("\t<").append(client ? "client" : "server").append("Packet timestamp=\"").append(String.valueOf(packet.getReceived())).append("\" opcodes=\"");
		
		packetContent:
		{
			final ByteBuffer body = ByteBuffer.wrap(packet.getBody()).order(ByteOrder.LITTLE_ENDIAN);
			
			final InterpreterContext ctx;
			{
				RandomAccessMMOBuffer wireframe;
				try
				{
					//ServerListTypePublisher.LIST_TYPE.set(_owner.getServerListType());
					final ByteBuffer independentWrapper = body.duplicate().order(body.order());
					wireframe = L2PpeProvider.getPacketPayloadEnumerator().enumeratePacketPayload(_protocol, _buf.setByteBuffer(independentWrapper), packet::getEndpoint);
				}
				catch (InvalidPacketOpcodeSchemeException e)
				{
					_writer.append("\">\r\n\t\t<remainder>").append(HexUtil.bytesToHexString(body.array(), " ")).append("</remainder>\r\n");
					break packetContent;
				}
				catch (PartialPayloadEnumerationException e)
				{
					wireframe = e.getBuffer();
					
					final IPacketTemplate template = e.getTemplate();
					if (template.isDefined())
					{
						final String message = (packet.getEndpoint().isClient() ? "[C] " : "[S] ") + e.getTemplate();
						if (e.getUnusedBytes() != -1)
							LOG.info(message + " " + e.getMessage());
						else
							LOG.error(message, e);
					}
				}
				ctx = new InterpreterContext(_cacheContext, wireframe);
			}
			
			// Enable object analytics and whatnot
			LogLoadScriptManager.getInstance().onLoadedPacket(ServiceType.valueOf(_protocol).isLogin(), client, body.array(), _protocol, _cacheContext);
			
			final IPacketTemplate template = VersionnedPacketTable.getInstance().getTemplate(_protocol, packet.getEndpoint(), body.array());
			_writer.append(HexUtil.bytesToHexString(template.getPrefix(), " ")).append("\">\r\n");
			body.position(template.getPrefix().length);
			
			final Map<FieldValueReadOption, Object> options = new EnumMap<>(FieldValueReadOption.class);
			options.put(FieldValueReadOption.APPLY_MODIFICATIONS, null);
			options.put(FieldValueReadOption.COMPUTE_INTERPRETATION, ctx);
			template.visitStructureElements(new PacketStructureElementVisitor()
			{
				private final Map<LoopElement, LoopVisitation> _loops = new HashMap<>();
				
				@Override
				public void onStart(int bytesWithoutOpcodes) throws RuntimeException
				{
					try
					{
						if (bytesWithoutOpcodes < 1)
							_writer.append("\t\t<!-- Trigger packet -->\r\n");
					}
					catch (IOException e)
					{
						throw new RuntimeException(e);
					}
				}
				
				@Override
				public void onBranch(BranchElement element, boolean conditionMet) throws RuntimeException
				{
					// do nothing
				}
				
				@Override
				public void onBranchEnd(BranchElement element) throws RuntimeException
				{
					// do nothing
				}
				
				@Override
				public void onLoopStart(LoopElement element, int expectedIterations) throws RuntimeException
				{
					_loops.put(element, new LoopVisitation(expectedIterations));
					
					try
					{
						_writer.append("\t\t");
						for (int i = 1; i < _loops.size(); ++i)
							_writer.append('\t');
						_writer.append(expectedIterations > 0 ? "<loop>\r\n" : "<loop />\r\n");
					}
					catch (IOException e)
					{
						throw new RuntimeException(e);
					}
				}
				
				@Override
				public void onLoopIterationStart(LoopElement element) throws RuntimeException
				{
					try
					{
						_writer.append("\t\t");
						for (int i = 0; i < _loops.size(); ++i)
							_writer.append('\t');
						_writer.append("<iteration>\r\n");
					}
					catch (IOException e)
					{
						throw new RuntimeException(e);
					}
				}
				
				@Override
				public void onLoopIterationEnd(LoopElement element) throws RuntimeException
				{
					try
					{
						_writer.append("\t\t");
						for (int i = 0; i < _loops.size(); ++i)
							_writer.append('\t');
						_writer.append("</iteration>\r\n");
					}
					catch (IOException e)
					{
						throw new RuntimeException(e);
					}
				}
				
				@Override
				public void onLoopEnd(LoopElement element) throws RuntimeException
				{
					if (_loops.remove(element)._iterationCount < 1)
						return;
						
					try
					{
						_writer.append("\t\t");
						for (int i = 0; i < _loops.size(); ++i)
							_writer.append('\t');
						_writer.append("</loop>\r\n");
					}
					catch (IOException e)
					{
						throw new RuntimeException(e);
					}
				}
				
				@Override
				public void onByteArrayField(AbstractByteArrayFieldElement element, ByteArrayFieldValue value) throws RuntimeException
				{
					writeToXML(element, value, value != null ? value.value() : null, DataType.BYTES, _loops.size());
				}
				
				@Override
				public void onDecimalField(AbstractDecimalFieldElement element, DecimalFieldValue value) throws RuntimeException
				{
					writeToXML(element, value, value != null ? value.value() : null, DataType.FLOAT, _loops.size());
				}
				
				@Override
				public void onIntegerField(AbstractIntegerFieldElement element, IntegerFieldValue value) throws RuntimeException
				{
					int fieldWidth = 4;
					if (value == null)
					{
						final ByteBuffer fallback = ByteBuffer.allocate(8);
						try
						{
							element.readValue(new MMOBuffer().setByteBuffer(fallback), Collections.emptyMap());
							fieldWidth = fallback.position();
						}
						catch (BufferUnderflowException e)
						{
							// what
						}
						catch (InvalidFieldValueInterpreterException | InvalidFieldValueModifierException e)
						{
							// interp/mod explicitly disabled, cannot happen
						}
					}
					else
						fieldWidth = value.raw().length;
						
					final DataType visualDataType;
					switch (fieldWidth)
					{
						case 1:
							visualDataType = DataType.BYTE;
							break;
						case 2:
							visualDataType = DataType.WORD;
							break;
						case 4:
							visualDataType = DataType.DWORD;
							break;
						case 8:
							visualDataType = DataType.QWORD;
							break;
						default:
							throw new IllegalArgumentException(element + " = " + value);
					}
					writeToXML(element, value, value != null ? value.value() : null, visualDataType, _loops.size());
				}
				
				@Override
				public void onStringField(AbstractStringFieldElement element, StringFieldValue value) throws RuntimeException
				{
					writeToXML(element, value, value != null ? value.value() : null, DataType.STRING, _loops.size());
				}
				
				@Override
				public void onAbruptTermination(BufferUnderflowException e, int remainingBytes) throws RuntimeException
				{
					try
					{
						if (remainingBytes > 0)
							_writer.append("\t\t<remainder>").append(HexUtil.bytesToHexString(body.array(), body.position() - (remainingBytes - body.remaining()), " ")).append("</remainder>\r\n");
					}
					catch (IOException e2)
					{
						throw new RuntimeException(e2);
					}
				}
				
				@Override
				public void onException(Exception e, int remainingBytes)
				{
					LOG.error("", e);
					
					try
					{
						_writer.append("\t\t<remainder>").append(HexUtil.bytesToHexString(body.array(), body.position(), " ")).append("</remainder>\r\n");
					}
					catch (IOException e2)
					{
						throw new RuntimeException(e2);
					}
				}
				
				@Override
				public void onCompletion(int remainingBytes) throws RuntimeException
				{
					if (remainingBytes < 1)
						return;
						
					try
					{
						_writer.append("\t\t<remainder>").append(HexUtil.bytesToHexString(body.array(), body.position(), " ")).append("</remainder>\r\n");
					}
					catch (IOException e)
					{
						throw new RuntimeException(e);
					}
				}
			}, body, options);
		}
		_writer.append("\t</").append(client ? "client" : "server").append("Packet>\r\n");
	}
	
	void writeToXML(FieldElement<?> field, FieldValue value, Object readValue, DataType visualDataType, int loopIndent)
	{
		loopIndent <<= 1;
		
		Object interpretation = null;
		
		if (value != null)
		{
			interpretation = value.interpreted();
			if (interpretation instanceof RenderedImage)
				interpretation = "[IMAGE]";
		}
		
		if (interpretation == null)
			interpretation = "N/A";
		else if (interpretation instanceof byte[])
			interpretation = HexUtil.bytesToHexString((byte[])interpretation, " ");
			
		try
		{
			_writer.append("\t\t");
			for (int i = 0; i < loopIndent; ++i)
				_writer.append('\t');
			_writer.append("<").append(String.valueOf(visualDataType)).append(" name=\"").append(field.getAlias()).append("\">\r\n");
			
			_writer.append("\t\t\t");
			for (int i = 0; i < loopIndent; ++i)
				_writer.append('\t');
			_writer.append("<interpretedValue>").append(String.valueOf(interpretation)).append("</interpretedValue>\r\n");
			if (readValue != null)
			{
				if (readValue instanceof byte[])
					readValue = HexUtil.bytesToHexString((byte[])readValue, " ");
					
				_writer.append("\t\t\t");
				for (int i = 0; i < loopIndent; ++i)
					_writer.append('\t');
				_writer.append("<value>").append(String.valueOf(readValue)).append("</value>\r\n");
			}
			if (value != null)
			{
				_writer.append("\t\t\t");
				for (int i = 0; i < loopIndent; ++i)
					_writer.append('\t');
				_writer.append("<rawBytes>").append(HexUtil.bytesToHexString(value.raw(), " ")).append("</rawBytes>\r\n");
			}
			
			_writer.append("\t\t");
			for (int i = 0; i < loopIndent; ++i)
				_writer.append('\t');
			_writer.append("</").append(String.valueOf(visualDataType)).append(">\r\n");
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public void onEnd() throws Exception
	{
		_writer.append("</packetLog>");
		_writer.flush();
		_writer.close();
	}
	
	private static final class LoopVisitation
	{
		final int _iterationCount;
		
		LoopVisitation(int iterationCount)
		{
			_iterationCount = iterationCount;
		}
	}
}
