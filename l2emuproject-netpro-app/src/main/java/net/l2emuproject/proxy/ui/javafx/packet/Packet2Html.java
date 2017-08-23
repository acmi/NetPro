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

import java.awt.image.RenderedImage;
import java.io.IOException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import net.l2emuproject.lang.L2TextBuilder;
import net.l2emuproject.network.mmocore.MMOBuffer;
import net.l2emuproject.network.protocol.IProtocolVersion;
import net.l2emuproject.proxy.io.definitions.VersionnedPacketTable;
import net.l2emuproject.proxy.network.meta.IPacketTemplate;
import net.l2emuproject.proxy.network.meta.L2PpeProvider;
import net.l2emuproject.proxy.network.meta.PacketStructureElementVisitor;
import net.l2emuproject.proxy.network.meta.RandomAccessMMOBuffer;
import net.l2emuproject.proxy.network.meta.ServerListTypePublisher;
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
import net.l2emuproject.proxy.state.entity.context.ICacheServerID;
import net.l2emuproject.proxy.ui.ReceivedPacket;
import net.l2emuproject.proxy.ui.i18n.UIStrings;
import net.l2emuproject.proxy.ui.javafx.FXUtils;
import net.l2emuproject.proxy.ui.savormix.component.packet.DataType;
import net.l2emuproject.util.HexUtil;
import net.l2emuproject.util.ISODateTime;
import net.l2emuproject.util.logging.L2Logger;

/**
 * A dedicated class to control how received packets are rendered in HTML.
 * 
 * @author _dev_
 */
public final class Packet2Html implements ISODateTime
{
	static final L2Logger LOG = L2Logger.getLogger(Packet2Html.class);
	private static final Pattern HTML_TAG = Pattern.compile("<html", Pattern.LITERAL | Pattern.CASE_INSENSITIVE);
	
	final L2TextBuilder _packetBuilder, _packetBodyBuilder;
	private final Map<DataType, MutableInt> _currentHyperlinkID;
	
	final ReceivedPacket _packet;
	private final IProtocolVersion _protocol;
	private final ICacheServerID _cacheContext;
	
	private Packet2Html(ReceivedPacket packet, IProtocolVersion protocol, ICacheServerID cacheContext)
	{
		_packetBodyBuilder = new L2TextBuilder();
		_packetBuilder = new L2TextBuilder();
		_currentHyperlinkID = new HashMap<>();
		for (final DataType dt : DataType.values())
			_currentHyperlinkID.put(dt, new MutableInt());
		
		_packet = packet;
		_protocol = protocol;
		_cacheContext = cacheContext;
	}
	
	/**
	 * Returns a fully interpreted view over the given packet (left) along with an associated raw data view (right).<BR>
	 * Please manually set {@link ServerListTypePublisher#LIST_TYPE} as necessary prior to calling this method.
	 * 
	 * @param packet a packet
	 * @param protocol network protocol version
	 * @param cacheContext entity cache context
	 * @return packet in HTML format
	 */
	public static final Pair<String, String> getHTML(ReceivedPacket packet, IProtocolVersion protocol, ICacheServerID cacheContext)
	{
		return new Packet2Html(packet, protocol, cacheContext).parsePacket();
	}
	
	private Pair<String, String> result()
	{
		return ImmutablePair.of(_packetBuilder.moveToString(), _packetBodyBuilder.moveToString());
	}
	
	private Pair<String, String> parsePacket()
	{
		final boolean unsent = _packet.getReceived() == ReceivedPacket.UNSENT_PACKET_TIMESTAMP;
		if (!unsent)
		{
			_packetBuilder.append("<em>Received on: ");
			_packetBuilder.appendNewline(new SimpleDateFormat(ISO_TIME_MS).format(new Date(_packet.getReceived())));
			_packetBuilder.appendNewline("</em><br /><br />");
		}
		
		final ByteBuffer body = ByteBuffer.wrap(_packet.getBody()).asReadOnlyBuffer().order(ByteOrder.LITTLE_ENDIAN);
		
		final InterpreterContext ctx;
		{
			RandomAccessMMOBuffer wireframe;
			try
			{
				final ByteBuffer independentWrapper = body.duplicate().order(body.order());
				wireframe = L2PpeProvider.getPacketPayloadEnumerator().enumeratePacketPayload(_protocol, new MMOBuffer().setByteBuffer(independentWrapper), _packet::getEndpoint);
			}
			catch (final InvalidPacketOpcodeSchemeException e)
			{
				// 2ez4me
				_packetBuilder.append("<center><strong>Invalid packet</strong></center><br />");
				_packetBodyBuilder.append(HexUtil.bytesToHexString(_packet.getBody(), " ")); // incomplete opcode(s)
				return result();
			}
			catch (final PartialPayloadEnumerationException e)
			{
				wireframe = e.getBuffer();
				
				final IPacketTemplate template = e.getTemplate();
				if (!unsent && template.isDefined())
				{
					final String message = (_packet.getEndpoint().isClient() ? "[C] " : "[S] ") + e.getTemplate();
					if (e.getUnusedBytes() != -1)
						LOG.info(message + " " + e.getMessage());
					else
						LOG.error(message, e);
				}
			}
			ctx = new InterpreterContext(_cacheContext, wireframe);
		}
		
		final IPacketTemplate template = VersionnedPacketTable.getInstance().getTemplate(_protocol, _packet.getEndpoint(), _packet.getBody());
		final String opcodes = HexUtil.bytesToHexString(template.getPrefix(), " ");
		if (template.getName() != null)
			_packetBodyBuilder.append("<div title=\"").append(opcodes).append(": ").append(template.getName()).append("\">").append(opcodes).append("</div>"); // opcodes
		else
			_packetBodyBuilder.append(opcodes); // opcodes
		body.position(template.getPrefix().length);
		
		if (unsent)
		{
			_packetBuilder.append("<center><em>").append(_protocol).append("<br />[");
			_packetBuilder.append(_packet.getEndpoint().isClient() ? 'C' : 'S').append("] ");
			_packetBuilder.append(HexUtil.bytesToHexString(template.getPrefix(), ":"));
			if (template.getName() != null)
				_packetBuilder.append(' ').append(template.getName());
			_packetBuilder.appendNewline("</em></center><hr />");
		}
		
		final Map<FieldValueReadOption, Object> options = new EnumMap<>(FieldValueReadOption.class);
		options.put(FieldValueReadOption.APPLY_MODIFICATIONS, null);
		options.put(FieldValueReadOption.COMPUTE_INTERPRETATION, ctx);
		template.visitStructureElements(new PacketStructureElementVisitor(){
			private final Map<LoopElement, LoopVisitation> _loops = new HashMap<>();
			
			@Override
			public void onStart(int bytesWithoutOpcodes) throws RuntimeException
			{
				if (bytesWithoutOpcodes < 1)
					_packetBuilder.appendNewline("<center>Trigger packet</center><br />");
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
				
				_packetBuilder.append("<font class=\"loopLabel\">");
				if (expectedIterations > 0)
				{
					_packetBuilder.append("~~~~ Loop start ~~~~");
					_packetBodyBuilder.appendNewline("<p>");
				}
				else
					_packetBuilder.append("~~~~ Empty loop ~~~~");
				_packetBuilder.appendNewline("</font><br />");
			}
			
			@Override
			public void onLoopIterationStart(LoopElement element) throws RuntimeException
			{
				final LoopVisitation lv = _loops.get(element);
				if (!lv._useDelimiter)
				{
					lv._useDelimiter = true;
					return;
				}
				
				_packetBuilder.append("<font class=\"loopLabel\">");
				_packetBuilder.appendNewline("~~ Loop element delimiter ~~</font><br />");
				_packetBodyBuilder.appendNewline("<br />");
			}
			
			@Override
			public void onLoopIterationEnd(LoopElement element) throws RuntimeException
			{
				// do nothing
			}
			
			@Override
			public void onLoopEnd(LoopElement element) throws RuntimeException
			{
				if (_loops.remove(element)._iterationCount < 1)
					return;
				
				_packetBuilder.append("<font class=\"loopLabel\">");
				_packetBuilder.appendNewline("~~~~ Loop end ~~~~</font><br />");
				_packetBodyBuilder.appendNewline("<br /></p>");
			}
			
			@Override
			public void onByteArrayField(AbstractByteArrayFieldElement element, ByteArrayFieldValue value) throws RuntimeException
			{
				displayAsHtml(element, value, value != null ? value.value() : null, DataType.BYTES);
			}
			
			@Override
			public void onDecimalField(AbstractDecimalFieldElement element, DecimalFieldValue value) throws RuntimeException
			{
				displayAsHtml(element, value, value != null ? value.value() : null, DataType.FLOAT);
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
					catch (final BufferUnderflowException e)
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
				displayAsHtml(element, value, value != null ? value.value() : null, visualDataType);
			}
			
			@Override
			public void onStringField(AbstractStringFieldElement element, StringFieldValue value) throws RuntimeException
			{
				displayAsHtml(element, value, value != null ? value.value() : null, DataType.STRING);
			}
			
			@Override
			public void onAbruptTermination(BufferUnderflowException e, int remainingBytes) throws RuntimeException
			{
				_packetBuilder.appendNewline("<br /><center><strong>EXPECTED MORE DATA</strong><br />");
				_packetBuilder.appendNewline("(according to packet definition)</center><br />");
				if (remainingBytes > 0)
					_packetBodyBuilder.append(' ').append(HexUtil.bytesToHexString(_packet.getBody(), body.position() - (remainingBytes - body.remaining()), " "));
			}
			
			@Override
			public void onException(Exception e, int remainingBytes)
			{
				LOG.error("", e);
				
				_packetBuilder.append("<br /><center><strong>").append(e.getMessage()).appendNewline("<br />");
				if (remainingBytes > 0)
				{
					_packetBuilder.append("<br />Unidentified bytes: ").append(remainingBytes).appendNewline("</strong></center><br />");
					_packetBodyBuilder.append(' ').append(HexUtil.bytesToHexString(_packet.getBody(), body.position(), " "));
				}
			}
			
			@Override
			public void onCompletion(int remainingBytes) throws RuntimeException
			{
				if (remainingBytes < 1)
					return;
				
				_packetBuilder.append("<br /><center><strong>Unidentified bytes: ").append(remainingBytes).appendNewline("</strong></center><br />");
				_packetBodyBuilder.append(' ').append(HexUtil.bytesToHexString(_packet.getBody(), body.position(), " "));
			}
		}, body, options);
		return result();
	}
	
	void displayAsHtml(FieldElement<?> field, FieldValue value, Object readValue, DataType visualDataType)
	{
		String wrapTag = null;
		Object interpretation = null;
		
		if (value != null)
		{
			interpretation = value.interpreted();
			if (interpretation instanceof RenderedImage)
			{
				String imgSrc = FXUtils.class.getResource("icon-16.png").toString();
				try
				{
					imgSrc = FXUtils.getImageSrcForWebEngine((RenderedImage)interpretation);
				}
				catch (final IOException e)
				{
					LOG.error("Unexpected error", e);
				}
				interpretation = new StringBuilder("<img src=\"").append(imgSrc).append("\" border=\"0\" />").toString();
			}
			else if (field.getID() != null)
				wrapTag = "em"; // emphasize a named field
		}
		
		if (interpretation == null)
			interpretation = "N/A";
		else if (interpretation instanceof byte[])
			interpretation = HexUtil.bytesToHexString((byte[])interpretation, " ");
		
		_packetBuilder.append("<img src=\"").append(visualDataType.getIconImgSrc()).append("\" title=\"").append(UIStrings.get(visualDataType.getIconTooltip())).append("\" border=\"0\" />");
		
		final MutableInt hyperID = _currentHyperlinkID.get(visualDataType);
		hyperID.increment();
		_packetBuilder.append("<a href=\"").append(visualDataType).append("__").append(hyperID.intValue()).append("\">");
		_packetBuilder.append(field.getAlias()).append(": ").append(interpretation);
		String appendedReadValue = null;
		if (readValue != null)
		{
			if (readValue instanceof byte[])
				readValue = HexUtil.bytesToHexString((byte[])readValue, " ");
			
			final String read = String.valueOf(readValue), interp = String.valueOf(interpretation);
			if (!interp.equals(read) && !HTML_TAG.matcher(read).find())
				_packetBuilder.append(" (").append(appendedReadValue = read).append(")");
		}
		_packetBuilder.appendNewline("</a><br />");
		
		if (value != null)
		{
			_packetBodyBuilder.append(" <font class=\"").append(visualDataType).append("_\"").append(" title=\"").append(field.getAlias());
			if (visualDataType != DataType.BYTES && !(value.interpreted() instanceof RenderedImage))
			{
				_packetBodyBuilder.append(": ");
				if (!String.valueOf(interpretation).startsWith("<"))
				{
					_packetBodyBuilder.append(interpretation);
					if (appendedReadValue != null)
						_packetBodyBuilder.append(" (").append(appendedReadValue).append(")");
				}
				else if (appendedReadValue != null)
					_packetBodyBuilder.append(appendedReadValue);
			}
			_packetBodyBuilder.append("\">");
			if (wrapTag != null)
				_packetBodyBuilder.append('<').append(wrapTag).append('>');
			_packetBodyBuilder.append(HexUtil.bytesToHexString(value.raw(), " "));
			if (wrapTag != null)
				_packetBodyBuilder.append("</").append(wrapTag).append('>');
			_packetBodyBuilder.append("</font>");
		}
	}
	
	private static final class LoopVisitation
	{
		final int _iterationCount;
		boolean _useDelimiter;
		
		LoopVisitation(int iterationCount)
		{
			_iterationCount = iterationCount;
			_useDelimiter = false;
		}
	}
}
