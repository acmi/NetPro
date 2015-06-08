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

import java.awt.image.RenderedImage;
import java.net.URL;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.mutable.MutableInt;

import net.l2emuproject.lang.L2TextBuilder;
import net.l2emuproject.network.mmocore.MMOBuffer;
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
import net.l2emuproject.proxy.ui.ReceivedPacket;
import net.l2emuproject.proxy.ui.savormix.io.ImageUrlUtils;
import net.l2emuproject.proxy.ui.savormix.io.VersionnedPacketTable;
import net.l2emuproject.proxy.ui.savormix.loader.Loader;
import net.l2emuproject.ui.AsyncTask;
import net.l2emuproject.util.HexUtil;
import net.l2emuproject.util.ISODateTime;
import net.l2emuproject.util.logging.L2Logger;

/**
 * Interprets a packet (if a proper definition has been loaded) and displays the results.
 * 
 * @author savormix
 */
public class PacketDisplayTask extends AsyncTask<ReceivedPacket, String, Set<URL>> implements ISODateTime
{
	static final L2Logger LOG = L2Logger.getLogger(PacketDisplayTask.class);
	private static final Pattern HTML_TAG = Pattern.compile("<html", Pattern.LITERAL | Pattern.CASE_INSENSITIVE);
	
	final PacketDisplay _owner;
	
	final L2TextBuilder _packetBuilder, _packetBodyBuilder;
	
	byte[] _packet;
	private Set<URL> _displayedImages;
	private Map<DataType, MutableInt> _currentHyperlinkID;
	
	/**
	 * Creates a packet information display task.
	 * 
	 * @param owner associated component
	 */
	public PacketDisplayTask(PacketDisplay owner)
	{
		_owner = owner;
		
		_packetBodyBuilder = new L2TextBuilder();
		_packetBuilder = new L2TextBuilder();
		
		_packet = ArrayUtils.EMPTY_BYTE_ARRAY;
	}
	
	@Override
	protected void onPreExecute()
	{
		_owner.setContent(publish(0), null, PacketDisplay.FLAG_BODY | PacketDisplay.FLAG_CONTENT);
	}
	
	@Override
	protected Set<URL> doInBackground(ReceivedPacket... params)
	{
		_currentHyperlinkID = new HashMap<>();
		for (DataType dt : DataType.values())
			_currentHyperlinkID.put(dt, new MutableInt());
		_displayedImages = new HashSet<>();
		try
		{
			displayParsedPacket(params[0]);
		}
		catch (Throwable t)
		{
			try
			{
				LOG.error(VersionnedPacketTable.getInstance().getTemplate(_owner.getProtocol(), params[0].getEndpoint(), params[0].getBody()), t);
			}
			catch (Throwable t1)
			{
				LOG.error("", t);
			}
		}
		return _displayedImages;
	}
	
	@Override
	protected void onPostExecute(Set<URL> result)
	{
		final String html = _packetBuilder.moveToString();
		final String htmlBytes = _packetBodyBuilder.moveToString();
		_owner.setContent(html, htmlBytes, PacketDisplay.FLAG_BODY | PacketDisplay.FLAG_CONTENT);
		_owner.unsetDisplayTask(this);
		
		/*
		try (final BufferedWriter bw = Files.newBufferedWriter(Paths.get("CONTENT_INT.txt")))
		{
			bw.write(html);
		}
		catch (IOException e)
		{
			// ignore
		}
		try (final BufferedWriter bw = Files.newBufferedWriter(Paths.get("CONTENT_RAW.txt")))
		{
			bw.write(htmlBytes);
		}
		catch (IOException e)
		{
			// ignore
		}
		*/
	}
	
	@Override
	protected void process(List<String> chunks)
	{
		final String progress = chunks.get(chunks.size() - 1);
		_owner.setContent(progress, progress, PacketDisplay.FLAG_BODY | PacketDisplay.FLAG_CONTENT);
	}
	
	String publish(int progress)
	{
		final L2TextBuilder sb = new L2TextBuilder("<center>Reading bytes...<br /><font size=\"+5\">");
		sb.append(NumberFormat.getPercentInstance(Loader.getLocale()).format((double)progress / _packet.length));
		final NumberFormat nf = NumberFormat.getIntegerInstance(Loader.getLocale());
		sb.append("</font><br />").append(nf.format(progress)).append('/').append(nf.format(_packet.length));
		sb.append("</center>");
		return sb.moveToString();
	}
	
	private ByteBuffer createBuffer()
	{
		return ByteBuffer.wrap(_packet).asReadOnlyBuffer().order(ByteOrder.LITTLE_ENDIAN);
	}
	
	private void displayParsedPacket(ReceivedPacket packet)
	{
		_owner.releaseImages(_displayedImages);
		
		_packet = packet.getBody();
		
		final ByteBuffer body = createBuffer();
		
		final boolean unsent = packet.getReceived() == ReceivedPacket.UNSENT_PACKET_TIMESTAMP;
		
		if (!unsent)
		{
			_packetBuilder.append("<em>Received on: ");
			_packetBuilder.appendNewline(new SimpleDateFormat(ISO_TIME_MS).format(new Date(packet.getReceived())));
			_packetBuilder.appendNewline("</em><br /><br />");
		}
		
		final InterpreterContext ctx;
		{
			RandomAccessMMOBuffer wireframe;
			try
			{
				ServerListTypePublisher.LIST_TYPE.set(_owner.getServerListType());
				final ByteBuffer independentWrapper = body.duplicate().order(body.order());
				wireframe = L2PpeProvider.getPacketPayloadEnumerator().enumeratePacketPayload(_owner.getProtocol(), new MMOBuffer().setByteBuffer(independentWrapper), packet::getEndpoint);
			}
			catch (InvalidPacketOpcodeSchemeException e)
			{
				// 2ez4me
				_packetBuilder.append("<center><strong>Invalid packet</strong></center><br />");
				_packetBodyBuilder.append(HexUtil.bytesToHexString(_packet, " ")); // incomplete opcode(s)
				return;
			}
			catch (PartialPayloadEnumerationException e)
			{
				wireframe = e.getBuffer();
				
				final IPacketTemplate template = e.getTemplate();
				if (!unsent && template.isDefined())
				{
					final String message = (packet.getEndpoint().isClient() ? "[C] " : "[S] ") + e.getTemplate();
					if (e.getUnusedBytes() != -1)
						LOG.info(message + " " + e.getMessage());
					else
						LOG.error(message, e);
				}
			}
			ctx = new InterpreterContext(_owner.getCacheContext(), wireframe);
		}
		
		final IPacketTemplate template = VersionnedPacketTable.getInstance().getTemplate(_owner.getProtocol(), packet.getEndpoint(), _packet);
		_packetBodyBuilder.append(HexUtil.bytesToHexString(template.getPrefix(), " ")); // opcodes
		body.position(template.getPrefix().length);
		
		if (unsent)
		{
			_packetBuilder.append("<center><em>").append(_owner.getProtocol()).append("<br />[");
			_packetBuilder.append(packet.getEndpoint().isClient() ? 'C' : 'S').append("] ");
			_packetBuilder.append(HexUtil.bytesToHexString(template.getPrefix(), ":"));
			if (template.getName() != null)
				_packetBuilder.append(' ').append(template.getName());
			_packetBuilder.appendNewline("</em></center><hr />");
		}
		
		final Map<FieldValueReadOption, Object> options = new EnumMap<>(FieldValueReadOption.class);
		options.put(FieldValueReadOption.APPLY_MODIFICATIONS, null);
		options.put(FieldValueReadOption.COMPUTE_INTERPRETATION, ctx);
		template.visitStructureElements(new PacketStructureElementVisitor()
		{
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
				testCancel();
				displayAsHtml(element, value, value != null ? value.value() : null, DataType.BYTES);
			}
			
			@Override
			public void onDecimalField(AbstractDecimalFieldElement element, DecimalFieldValue value) throws RuntimeException
			{
				testCancel();
				displayAsHtml(element, value, value != null ? value.value() : null, DataType.FLOAT);
			}
			
			@Override
			public void onIntegerField(AbstractIntegerFieldElement element, IntegerFieldValue value) throws RuntimeException
			{
				testCancel();
				
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
				displayAsHtml(element, value, value != null ? value.value() : null, visualDataType);
			}
			
			@Override
			public void onStringField(AbstractStringFieldElement element, StringFieldValue value) throws RuntimeException
			{
				testCancel();
				displayAsHtml(element, value, value != null ? value.value() : null, DataType.STRING);
			}
			
			@Override
			public void onAbruptTermination(BufferUnderflowException e, int remainingBytes) throws RuntimeException
			{
				_packetBuilder.appendNewline("<br /><center><strong>EXPECTED MORE DATA</strong><br />");
				_packetBuilder.appendNewline("(according to packet definition)</center><br />");
				if (remainingBytes > 0)
					_packetBodyBuilder.append(' ').append(HexUtil.bytesToHexString(_packet, body.position(), " "));
			}
			
			@Override
			public void onException(Exception e, int remainingBytes)
			{
				if (!(e instanceof TaskCancelledException))
					LOG.error("", e);
				
				_packetBuilder.append("<br /><center><strong>").append(e.getMessage()).appendNewline("<br />");
				if (remainingBytes > 0)
				{
					_packetBuilder.append("<br />Unidentified bytes: ").append(remainingBytes).appendNewline("</strong></center><br />");
					_packetBodyBuilder.append(' ').append(HexUtil.bytesToHexString(_packet, body.position(), " "));
				}
			}
			
			@Override
			public void onCompletion(int remainingBytes) throws RuntimeException
			{
				if (remainingBytes < 1)
					return;
				
				_packetBuilder.append("<br /><center><strong>Unidentified bytes: ").append(remainingBytes).appendNewline("</strong></center><br />");
				_packetBodyBuilder.append(' ').append(HexUtil.bytesToHexString(_packet, body.position(), " "));
			}
			
			private void testCancel() throws TaskCancelledException
			{
				if (isCancelled())
					throw new TaskCancelledException();
				
				publish(body.position());
			}
		}, body, options);
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
				final URL url = ImageUrlUtils.getInstance().toUrl(field.getAlias(), (RenderedImage)interpretation);
				_displayedImages.add(url);
				interpretation = new StringBuilder("<img src=\"").append(url).append("\" border=\"0\" />").toString();
			}
			else if (field.getID() != null)
				wrapTag = "em"; // emphasize a named field
		}
		
		if (interpretation == null)
			interpretation = "N/A";
		else if (interpretation instanceof byte[])
			interpretation = HexUtil.bytesToHexString((byte[])interpretation, " ");
		
		final MutableInt hyperID = _currentHyperlinkID.get(visualDataType);
		hyperID.increment();
		_packetBuilder.append("<a href=\"").append(visualDataType).append("__").append(hyperID.intValue()).append("\">");
		_packetBuilder.append("<img src=\"").append(visualDataType.getIcon()).append("\" border=\"0\" />");
		_packetBuilder.append(field.getAlias()).append(": ").append(interpretation);
		if (readValue != null)
		{
			if (readValue instanceof byte[])
				readValue = HexUtil.bytesToHexString((byte[])readValue, " ");
			
			final String read = String.valueOf(readValue), interp = String.valueOf(interpretation);
			if (!interp.equals(read) && !HTML_TAG.matcher(read).find())
				_packetBuilder.append(" (").append(read).append(")");
		}
		_packetBuilder.appendNewline("</a><br />");
		
		if (value != null)
		{
			_packetBodyBuilder.append(" <font class=\"").append(visualDataType).append("_\">");
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
	
	private static final class TaskCancelledException extends RuntimeException
	{
		private static final long serialVersionUID = 1456795209939227370L;
		
		TaskCancelledException()
		{
			super("Display task has been cancelled");
		}
	}
}
