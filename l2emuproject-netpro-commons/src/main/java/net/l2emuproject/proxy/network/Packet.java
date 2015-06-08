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
package net.l2emuproject.proxy.network;

import java.nio.ByteBuffer;

import org.apache.commons.lang3.ArrayUtils;

import net.l2emuproject.lang.L2TextBuilder;
import net.l2emuproject.network.mmocore.MMOLogger;
import net.l2emuproject.proxy.network.listener.PacketManipulator;

/**
 * A received packet wrapper to be passed to packet listeners.<BR>
 * <BR>
 * Provides a way to modify a packet and express transfer preferences in a transparent way as well
 * as to prevent unspecified behavior with multiple listeners and detect conflicts as they occur.
 * 
 * @author savormix
 */
public final class Packet
{
	private static final MMOLogger LOG = new MMOLogger(Packet.class, 1000);
	
	private static final PacketManipulator[] EMPTY_PACKET_MANIPULATOR_ARRAY = new PacketManipulator[0];
	private static final ByteBuffer[] EMPTY_BYTE_BUFFER_ARRAY = new ByteBuffer[0];
	
	private final long _receptionTime;
	private final ByteBuffer _receivedBody;
	private ByteBuffer _forwardedBody;
	private PacketManipulator[] _demandSend = EMPTY_PACKET_MANIPULATOR_ARRAY;
	private PacketManipulator[] _demandVanilla = EMPTY_PACKET_MANIPULATOR_ARRAY;
	private PacketManipulator[] _demandLoss = EMPTY_PACKET_MANIPULATOR_ARRAY;
	private ByteBuffer[] _additional = EMPTY_BYTE_BUFFER_ARRAY;
	
	/**
	 * Creates a wrapper for a received packet.
	 * 
	 * @param receivedBody
	 *            packet's body
	 */
	public Packet(ByteBuffer receivedBody)
	{
		_receptionTime = System.currentTimeMillis();
		ByteBuffer backed = ByteBufferUtils.asReadOnly(ByteBufferUtils.asBacked(receivedBody));
		_receivedBody = backed;
		_forwardedBody = backed;
	}
	
	/**
	 * Returns a buffer for inline modifications.<BR>
	 * <BR>
	 * Returned buffer will be a copy of {@link #getForwardedBody()} and it's position will be 0.
	 * After making modifications pass it to {@link #setForwardedBody(ByteBuffer)}.
	 * 
	 * @return empty buffer for inline mods
	 * @throws IllegalStateException
	 *             if {@link #isImmutable()} or {@link #isLossForced()}
	 */
	public ByteBuffer getDefaultBufferForModifications() throws IllegalStateException
	{
		if (isImmutable() || isLossForced())
			throw new IllegalStateException();
		
		ByteBuffer src = getForwardedBody();
		src.clear();
		ByteBuffer mod = ByteBuffer.allocate(src.capacity()).put(src);
		mod.clear();
		return mod.order(src.order());
	}
	
	/**
	 * Demands that this packet (with possible modifications) would be forwarded to the intended
	 * target, because <TT>listener</TT> depends on such behavior.<BR>
	 * <BR>
	 * This method should only be used if there is some absolute need to forward this packet.
	 * 
	 * @param manipulator
	 *            requester
	 * @throws IllegalStateException
	 *             if {@link #isLossForced()}
	 */
	public void demandSend(PacketManipulator manipulator) throws IllegalStateException
	{
		if (isLossForced())
			warnManipulatorConflict(manipulator, getDemandLoss());
		
		_demandSend = ArrayUtils.add(getDemandSend(), manipulator);
	}
	
	/**
	 * Demands that this packet, <B>without any modifications</B>, would be forwarded to the
	 * intended target, because <TT>listener</TT> depends on such behavior.<BR>
	 * <BR>
	 * This method is <U>extremely likely to produce conflicts</U>, use with extra care.<BR>
	 * <BR>
	 * This method should only be used if there is some absolute need to forward this packet without
	 * any modifications (even own).
	 * 
	 * @param manipulator
	 *            requester
	 * @throws IllegalStateException
	 *             if already modified or {@link #isLossForced()}
	 */
	public void demandVanilla(PacketManipulator manipulator) throws IllegalStateException
	{
		if (isLossForced())
			warnManipulatorConflict(manipulator, getDemandLoss());
		else if (getForwardedBody() != getReceivedBody()) // already modified
			warnManipulatorConflict(manipulator, manipulator);
		
		_demandVanilla = ArrayUtils.add(getDemandVanilla(), manipulator);
	}
	
	/**
	 * Demands that this packet (regardless of modifications) would not reach the intended target,
	 * because <TT>listener</TT> depends on such behavior.<BR>
	 * <BR>
	 * This method should only be used if no modifications to the packet's body nor any additional
	 * packets sent cannot be used instead (for example, if you want to send a different packet
	 * instead of this one).<BR>
	 * <BR>
	 * <I>Example: network protocol was updated and that resulted in changed packet opcodes (and
	 * possibly structure). It is necessary to provide legacy support, therefore you must make sure
	 * incompatible packets do not get through.</I>
	 * 
	 * @param manipulator
	 *            requester
	 * @throws IllegalStateException
	 *             if loss cannot be guaranteed because of a conflict
	 */
	public void demandLoss(PacketManipulator manipulator) throws IllegalStateException
	{
		if (isSendForced())
			warnManipulatorConflict(manipulator, getDemandSend());
		else if (isImmutable())
			warnManipulatorConflict(manipulator, getDemandVanilla());
		
		_demandLoss = ArrayUtils.add(getDemandLoss(), manipulator);
	}
	
	/**
	 * Returns the received packet body without any modifications. The body fills the entire
	 * read-only buffer with a backing array. The mark, position and limit are unspecified.
	 * 
	 * @return original packet's body
	 */
	public ByteBuffer getReceivedBody()
	{
		return _receivedBody;
	}
	
	/**
	 * Returns the desired packet body to be forwarded.<BR>
	 * <BR>
	 * The body fills the entire read-only buffer with a backing array. The mark, position and limit
	 * are unspecified.<BR>
	 * <BR>
	 * Modifications should be passed as a new packet body that fills an entire buffer to
	 * {@link #setForwardedBody(ByteBuffer)}.
	 * 
	 * @return body to forward
	 */
	public ByteBuffer getForwardedBody()
	{
		return _forwardedBody;
	}
	
	/**
	 * Specifies the desired packet body to be forwarded instead of the original.<BR>
	 * <BR>
	 * Used to alter the forwarded packet body as a listener wishes. However, there are some
	 * restrictions:
	 * <UL>
	 * <LI>If some listener has demanded that the original packet would not reach the intended target (
	 * {@link #isLossForced()}), it cannot be overridden and a listener conflict should be reported instead.</LI>
	 * <LI>You cannot pass <TT>null</TT> as the argument and should use {@link #demandLoss(PacketManipulator)} instead.</LI>
	 * <LI>You cannot pass an empty packet body.</LI>
	 * <LI>If the original body is an empty packet, you cannot modify it.</LI>
	 * <LI>You cannot pass a body that contains different opcodes.</LI>
	 * <LI>The packet body must fill the entire capacity of the passed buffer.</LI>
	 * </UL>
	 * 
	 * @param forwardedBody
	 *            body to forward
	 * @throws IllegalArgumentException
	 *             if <TT>forwardedBody</TT> violates any criteria
	 * @throws IllegalStateException
	 *             if {@link #getForwardedBody()} is an empty packet
	 */
	public void setForwardedBody(ByteBuffer forwardedBody) throws IllegalArgumentException, IllegalStateException
	{
		// let's check what we have first
		if (getForwardedBody().capacity() == 0)
			throw new IllegalStateException("Cannot alter an empty packet.");
		// and what is passed next
		else if (forwardedBody == null)
			throw new IllegalArgumentException("Must demand loss instead.");
		else if (forwardedBody.limit() == 0)
			throw new IllegalArgumentException("Cannot replace with an empty packet.");
		else if (forwardedBody.get(0) != getForwardedBody().get(0))
			throw new IllegalArgumentException("Cannot change the main opcode.");
		
		forwardedBody = ByteBufferUtils.asReadOnly(ByteBufferUtils.asBacked(forwardedBody));
		_forwardedBody = forwardedBody.order(getForwardedBody().order());
	}
	
	/**
	 * Returns all packets that will be sent in addition to (instead of) the forwarded packet. If
	 * you want to add packets, use {@link #sendAdditionalPacket(ByteBuffer)}.<BR>
	 * <BR>
	 * Modifications to this array will have no effect. Each packet body fills the entire read-only
	 * buffer with a backing array. The mark, position and limit are unspecified.
	 * 
	 * @return additional packets
	 */
	public ByteBuffer[] getAdditionalPackets()
	{
		return getAdditional().clone();
	}
	
	/**
	 * Requests given packet to be sent in addition to (instead of) the forwarded packet.<BR>
	 * <BR>
	 * The packet body must fill an entire buffer.<BR>
	 * <B>DO NOT</B> send the original packet (<U>regardless of modifications</U>) using this
	 * method.
	 * 
	 * @param body
	 *            packet body
	 * @throws IllegalArgumentException
	 *             if any {@code body} violates any criteria
	 */
	public void sendAdditionalPacket(ByteBuffer body) throws IllegalArgumentException
	{
		if (body == null)
			return;
		else if (isLossForced() && body == getReceivedBody())
			throw new IllegalArgumentException("Packet must not reach the target!");
		
		body = ByteBufferUtils.asReadOnly(ByteBufferUtils.asBacked(body));
		_additional = ArrayUtils.add(getAdditional(), body);
	}
	
	/**
	 * Returns whether any listener(s) rely on the fact that this packet will reach the intended
	 * target.
	 * 
	 * @return whether this packet will be forwarded
	 */
	public boolean isSendForced()
	{
		return ArrayUtils.isNotEmpty(getDemandSend());
	}
	
	/**
	 * Returns whether any listener(s) rely on the fact that this packet will not reach the intended
	 * target.
	 * 
	 * @return whether this packet will not be forwarded
	 */
	public boolean isLossForced()
	{
		return ArrayUtils.isNotEmpty(getDemandLoss());
	}
	
	/**
	 * Returns whether any listener(s) rely on the fact that this packet will reach the intended
	 * target without any modifications.
	 * 
	 * @return whether this packet will be forwarded
	 */
	public boolean isImmutable()
	{
		return ArrayUtils.isNotEmpty(getDemandVanilla());
	}
	
	/**
	 * Returns this packet's arrival timestamp.
	 * 
	 * @return reception timestamp
	 */
	public long getReceptionTime()
	{
		return _receptionTime;
	}
	
	private static void warnManipulatorConflict(PacketManipulator manipulator, PacketManipulator... pms) throws IllegalStateException
	{
		if (ArrayUtils.isEmpty(pms))
			return;
		
		final L2TextBuilder sb = new L2TextBuilder("Conflict between ");
		sb.append(manipulator.getName()).appendNewline("and");
		for (PacketManipulator pm : pms)
			sb.appendNewline(pm.getName());
		LOG.warn(sb);
		
		throw new IllegalStateException();
	}
	
	private PacketManipulator[] getDemandSend()
	{
		return _demandSend;
	}
	
	private PacketManipulator[] getDemandVanilla()
	{
		return _demandVanilla;
	}
	
	private PacketManipulator[] getDemandLoss()
	{
		return _demandLoss;
	}
	
	private ByteBuffer[] getAdditional()
	{
		return _additional;
	}
}
