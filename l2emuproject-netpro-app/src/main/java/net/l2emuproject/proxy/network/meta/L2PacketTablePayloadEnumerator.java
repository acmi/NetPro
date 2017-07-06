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
package net.l2emuproject.proxy.network.meta;

import java.nio.BufferUnderflowException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import net.l2emuproject.lang.L2TextBuilder;
import net.l2emuproject.network.mmocore.MMOBuffer;
import net.l2emuproject.network.protocol.IProtocolVersion;
import net.l2emuproject.proxy.io.definitions.VersionnedPacketTable;
import net.l2emuproject.proxy.network.ByteBufferUtils;
import net.l2emuproject.proxy.network.EndpointType;
import net.l2emuproject.proxy.network.meta.exception.InvalidPacketOpcodeSchemeException;
import net.l2emuproject.proxy.network.meta.exception.PartialPayloadEnumerationException;
import net.l2emuproject.proxy.network.meta.exception.RunawayLoopException;
import net.l2emuproject.proxy.network.meta.structure.BranchElement;
import net.l2emuproject.proxy.network.meta.structure.FieldElement;
import net.l2emuproject.proxy.network.meta.structure.LoopElement;
import net.l2emuproject.proxy.network.meta.structure.field.FieldValue;
import net.l2emuproject.proxy.network.meta.structure.field.bytes.AbstractByteArrayFieldElement;
import net.l2emuproject.proxy.network.meta.structure.field.bytes.ByteArrayFieldValue;
import net.l2emuproject.proxy.network.meta.structure.field.decimal.AbstractDecimalFieldElement;
import net.l2emuproject.proxy.network.meta.structure.field.decimal.DecimalFieldValue;
import net.l2emuproject.proxy.network.meta.structure.field.integer.AbstractIntegerFieldElement;
import net.l2emuproject.proxy.network.meta.structure.field.integer.IntegerFieldValue;
import net.l2emuproject.proxy.network.meta.structure.field.string.AbstractStringFieldElement;
import net.l2emuproject.proxy.network.meta.structure.field.string.StringFieldValue;
import net.l2emuproject.proxy.network.packets.IPacketSource;
import net.l2emuproject.util.HexUtil;

/**
 * Enumerates packet content into a list of typed fields.
 * 
 * @author _dev_
 */
public class L2PacketTablePayloadEnumerator implements PacketPayloadEnumerator
{
	// should contain many duplicate messages
	// this should allow optimal memory & performance
	//private static final MMOLogger LOG = new MMOLogger(L2PacketTablePayloadEnumerator.class, 5000);
	
	@Override
	public RandomAccessMMOBuffer enumeratePacketPayload(IProtocolVersion protocol, MMOBuffer buf, IPacketSource packetAuthor) throws InvalidPacketOpcodeSchemeException,
			PartialPayloadEnumerationException
	{
		final RandomAccessMMOBuffer result;
		try
		{
			result = new RandomAccessMMOBuffer(buf, protocol, packetAuthor);
		}
		catch (NoSuchFieldException | IllegalAccessException e)
		{
			throw new InternalError("Failed instrumenting MMOBuffer", e);
		}
		final Map<String, List<EnumeratedPayloadField>> efields = new HashMap<>();
		result.setEnumeratedFields(efields);
		
		final int pos = result._buffer.position();
		try
		{
			final EndpointType endpoint = packetAuthor.getType();
			final IPacketTemplate template;
			try
			{
				//result._buffer.clear();
				result._buffer.position(0);
				template = VersionnedPacketTable.getInstance().getTemplate(protocol, endpoint, result._buffer);
			}
			catch (final RuntimeException e)
			{
				throw new InvalidPacketOpcodeSchemeException(endpoint, HexUtil.bytesToHexString(ByteBufferUtils.asMutable(result._buffer).array(), " "), e);
			}
			
			final EnumeratingVisitor visitor = new EnumeratingVisitor(template, result);
			template.visitStructureElements(visitor, result._buffer, Collections.emptyMap());
			if (visitor._partialEnumerationException != null)
				throw visitor._partialEnumerationException;
			if (visitor._failure != null)
				throw visitor._failure;
			if (template.isDefined())
				result.setPacketName(template.getName());
			return result;
		}
		finally
		{
			result._buffer.position(pos);
		}
	}
	
	private static final class EnumeratingVisitor implements /*ISODateTime, */PacketStructureElementVisitor
	{
		private final IPacketTemplate _template;
		//private final long _receivalTimestamp;
		
		private final RandomAccessMMOBuffer _result;
		private final Queue<Integer> _loopSizes;
		
		PartialPayloadEnumerationException _partialEnumerationException;
		RuntimeException _failure;
		
		EnumeratingVisitor(IPacketTemplate template/*, long receivalTimestamp*/, RandomAccessMMOBuffer result)
		{
			_template = template;
			//_receivalTimestamp = receivalTimestamp;
			
			_result = result;
			_loopSizes = new LinkedList<>();
		}
		
		@Override
		public void onStart(int bytesWithoutOpcodes) throws RuntimeException
		{
			// do nothing
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
			_loopSizes.add(expectedIterations);
		}
		
		@Override
		public void onLoopIterationStart(LoopElement element) throws RuntimeException
		{
			// do nothing
		}
		
		@Override
		public void onLoopIterationEnd(LoopElement element) throws RuntimeException
		{
			// do nothing
		}
		
		@Override
		public void onLoopEnd(LoopElement element) throws RuntimeException
		{
			_loopSizes.remove();
		}
		
		@Override
		public void onByteArrayField(AbstractByteArrayFieldElement element, ByteArrayFieldValue value) throws RuntimeException
		{
			onField(element, value);
		}
		
		@Override
		public void onDecimalField(AbstractDecimalFieldElement element, DecimalFieldValue value) throws RuntimeException
		{
			onField(element, value);
		}
		
		@Override
		public void onIntegerField(AbstractIntegerFieldElement element, IntegerFieldValue value) throws RuntimeException
		{
			onField(element, value);
		}
		
		@Override
		public void onStringField(AbstractStringFieldElement element, StringFieldValue value) throws RuntimeException
		{
			onField(element, value);
		}
		
		@Override
		public void onCompletion(int remainingBytes) throws RuntimeException
		{
			if (remainingBytes > 0)
				_partialEnumerationException = new PartialPayloadEnumerationException(_result, _template, remainingBytes);
		}
		
		@Override
		public void onAbruptTermination(BufferUnderflowException e, int remainingBytes) throws RuntimeException
		{
			_partialEnumerationException = new PartialPayloadEnumerationException(_result, _template, e);
		}
		
		@Override
		public void onException(Exception e, int remainingBytes)
		{
			if (e instanceof RunawayLoopException)
			{
				_partialEnumerationException = new PartialPayloadEnumerationException(_result, _template, (RunawayLoopException)e);
				return;
			}
			
			final L2TextBuilder tb = new L2TextBuilder("At offset ").append(_result._buffer.position());
			tb.append(" in ").append(HexUtil.bytesToHexString(_template.getPrefix(), " ")).append(" ").append(_template.getName());
			//tb.append(" (received on ").append(new SimpleDateFormat(ISO_TIME_MS).format(new Date(_receivalTimestamp))).append(')');
			_failure = new RuntimeException(tb.moveToString(), e);
			_failure.fillInStackTrace();
		}
		
		private <V extends FieldValue> void onField(FieldElement<V> element, V value)
		{
			if (value == null)
				return;
			
			final Set<String> aliases = element.getFieldAliases();
			if (aliases.isEmpty())
				return;
			
			for (final String alias : aliases)
			{
				List<EnumeratedPayloadField> list = _result.getEnumeratedFields().get(alias);
				if (list == null)
				{
					final Integer size = _loopSizes.peek();
					list = new ArrayList<>(size != null ? size : 1);
					_result.getEnumeratedFields().put(alias, list);
				}
				list.add(new EnumeratedPayloadField(element, _result._buffer.position() - value.raw().length));
			}
		}
	}
}
