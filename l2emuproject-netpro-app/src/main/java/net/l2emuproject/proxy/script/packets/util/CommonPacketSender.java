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
package net.l2emuproject.proxy.script.packets.util;

import static net.l2emuproject.proxy.network.EndpointType.CLIENT;
import static net.l2emuproject.proxy.network.EndpointType.SERVER;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;

import net.l2emuproject.geometry.point.IPoint2D;
import net.l2emuproject.geometry.point.IPoint3D;
import net.l2emuproject.geometry.point.impl.Point3D;
import net.l2emuproject.network.mmocore.MMOBuffer;
import net.l2emuproject.network.mmocore.MMOBuffer.ReservedFieldType;
import net.l2emuproject.network.protocol.ClientProtocolVersion;
import net.l2emuproject.network.protocol.IProtocolVersion;
import net.l2emuproject.proxy.network.Proxy;
import net.l2emuproject.proxy.network.game.client.L2GameClient;
import net.l2emuproject.proxy.network.game.server.L2GameServer;
import net.l2emuproject.proxy.network.packets.ProxyRepeatedPacket;
import net.l2emuproject.proxy.script.packets.InvalidPacketWriterArgumentsException;
import net.l2emuproject.proxy.script.packets.PacketIdentifier;
import net.l2emuproject.proxy.script.packets.PacketWriterScript;
import net.l2emuproject.proxy.script.packets.UnknownPacketIdentifierException;
import net.l2emuproject.util.ObjectPool;

/**
 * A convenience class that provides commonly used methods to interface with the client and/or the server.
 * 
 * @author _dev_
 */
public final class CommonPacketSender extends PacketWriterScript
{
	@PacketIdentifier(SERVER)
	private static final String SPAWN_ITEM = "SM_SPAWN_ITEM";
	@PacketIdentifier(SERVER)
	private static final String DELETE_OBJECT = "SM_DELETE_OBJECT";
	@PacketIdentifier(SERVER)
	private static final String SAY2 = "SM_SAY2";
	@PacketIdentifier(SERVER)
	private static final String NORMAL_HTML = "SM_NPC_HTML_MESSAGE";
	@PacketIdentifier(SERVER)
	private static final String TUTORIAL_HTML = "SM_TUTORIAL_SHOW_HTML";
	@PacketIdentifier(SERVER)
	private static final String TUTORIAL_CLOSE_HTML = "SM_TUTORIAL_CLOSE_HTML";
	@PacketIdentifier(SERVER)
	private static final String SCREEN_MSG = "SM_EX_SHOW_SCREEN_MESSAGE";
	@PacketIdentifier(SERVER)
	private static final String SERVER_PRIMITIVE = "SM_EX_SERVER_PRIMITIVE";
	
	@PacketIdentifier(CLIENT)
	private static final String REQ_MSU = "CM_REQ_MAGIC_SKILL_USE";
	@PacketIdentifier(CLIENT)
	private static final String REQ_ACTION_USE = "CM_REQ_ACTION_USE";
	@PacketIdentifier(CLIENT)
	private static final String REQ_ATT = "CM_REQ_ATTACK";
	@PacketIdentifier(CLIENT)
	private static final String REQ_ACTION = "CM_ACTION";
	@PacketIdentifier(CLIENT)
	private static final String REQ_USER_CMD = "CM_REQ_BYPASS_USER_CMD";
	@PacketIdentifier(CLIENT)
	private static final String REQ_TUTORIAL_LINK = "CM_REQ_TUTORIAL_LINK_HTML";
	@PacketIdentifier(CLIENT)
	private static final String REQ_BYPASS = "CM_REQ_BYPASS_TO_SERVER";
	@PacketIdentifier(CLIENT)
	private static final String REQ_ESC_SCENE = "CM_REQ_EX_ESCAPE_SCENE";
	@PacketIdentifier(CLIENT)
	private static final String REQ_DESTROY_ITEM = "CM_REQ_DESTROY_ITEM";
	@PacketIdentifier(CLIENT)
	private static final String REQ_USE_ITEM = "CM_REQ_USE_ITEM";
	@PacketIdentifier(CLIENT)
	private static final String MOVE_WITH_DELTA = "CM_MOVE_WITH_DELTA";
	@PacketIdentifier(CLIENT)
	private static final String REQ_EX_AUTO_FISH = "CM_REQ_EX_AUTO_FISH";
	
	private static final int CHAT_PM = 2;
	private static final WriterPool POOL = new WriterPool();
	
	@Override
	public String getName()
	{
		return "Common packet sender";
	}
	
	@Override
	public IProtocolVersion oldestSupportedProtocolVersion()
	{
		return ClientProtocolVersion.VALIANCE;
	}
	
	@Override
	public IProtocolVersion newestSupportedProtocolVersion()
	{
		return ClientProtocolVersion.GRAND_CRUSADE_UPDATE_1;
	}
	
	@Override
	public void sendPacket(Proxy recipient, String packet, Object[] args) throws InvalidPacketWriterArgumentsException
	{
		try
		{
			switch (packet)
			{
				case SPAWN_ITEM:
					sendSpawnItem((L2GameClient)recipient, ((Number)args[0]).intValue(), ((Number)args[1]).intValue(), (IPoint3D)args[2], (Boolean)args[3], ((Number)args[4]).longValue(),
							((Number)args[5]).intValue());
					break;
				case DELETE_OBJECT:
					sendDeleteObject((L2GameClient)recipient, ((Number)args[0]).intValue(), ((Number)args[1]).intValue());
					break;
				case SAY2:
					final int chat = ((Number)args[0]).intValue();
					final String talker = String.valueOf(args[1]), message = String.valueOf(args[2]);
					if (chat == CHAT_PM)
						sendPrivateMessage((L2GameClient)recipient, talker, message, ((Number)args[3]).intValue(), ((Number)args[4]).intValue());
					else
						sendChatMessage((L2GameClient)recipient, chat, talker, message);
					break;
				case NORMAL_HTML:
					sendHTML((L2GameClient)recipient, String.valueOf(args[0]));
					break;
				case TUTORIAL_HTML:
					sendTutorialHTML((L2GameClient)recipient, String.valueOf(args[0]));
					break;
				case TUTORIAL_CLOSE_HTML:
					sendTutorialCloseHTML((L2GameClient)recipient);
					break;
				case SCREEN_MSG:
					if (args[2] instanceof Number)
						sendImmutableScreenSystemMessage((L2GameClient)recipient, ((Number)args[0]).intValue(), ((Number)args[1]).intValue(), ((Number)args[2]).intValue(), (Boolean)args[3],
								((Number)args[4]).intValue(), ((Number)args[5]).intValue(), (Boolean)args[6], ((Number)args[7]).intValue(), (Boolean)args[8]);
					else
						sendScreenMessage((L2GameClient)recipient, ((Number)args[0]).intValue(), ((Number)args[1]).intValue(), (Boolean)args[2], ((Number)args[3]).intValue(),
								((Number)args[4]).intValue(), (Boolean)args[5], ((Number)args[6]).intValue(), (Boolean)args[7], String.valueOf(args[8]));
					break;
				case REQ_MSU:
					sendRequestMSU((L2GameServer)recipient, ((Number)args[0]).intValue(), (Boolean)args[1], (Boolean)args[2]);
					break;
				case REQ_ACTION_USE:
					sendRequestActionUse((L2GameServer)recipient, ((Number)args[0]).intValue(), (Boolean)args[1], (Boolean)args[2]);
					break;
				case REQ_ATT:
					sendRequestAttack((L2GameServer)recipient, ((Number)args[0]).intValue(), (IPoint3D)args[1], (Boolean)args[2]);
					break;
				case REQ_ACTION:
					sendRequestAction((L2GameServer)recipient, ((Number)args[0]).intValue(), (IPoint3D)args[1], (Boolean)args[2]);
					break;
				case REQ_USER_CMD:
					sendUserCmd((L2GameServer)recipient, ((Number)args[0]).intValue());
					break;
				case REQ_TUTORIAL_LINK:
					sendRequestTutorialLink((L2GameServer)recipient, String.valueOf(args[0]));
					break;
				case REQ_BYPASS:
					sendRequestBypassToServer((L2GameServer)recipient, String.valueOf(args[0]));
					break;
				case REQ_ESC_SCENE:
					sendRequestEscapeScene((L2GameServer)recipient);
					break;
				case REQ_DESTROY_ITEM:
					sendRequestDestroyItem((L2GameServer)recipient, ((Number)args[0]).intValue(), ((Number)args[1]).longValue());
					break;
				case REQ_USE_ITEM:
					sendRequestUseItem((L2GameServer)recipient, ((Number)args[0]).intValue(), (Boolean)args[1]);
					break;
				case MOVE_WITH_DELTA:
					sendMoveWithDelta((L2GameServer)recipient, (IPoint3D)args[0]);
					break;
				case REQ_EX_AUTO_FISH:
					sendRequestAutoFish((L2GameServer)recipient, (Boolean)args[0]);
					break;
				default:
					throw new UnknownPacketIdentifierException(packet);
			}
		}
		catch (final RuntimeException e)
		{
			throw new InvalidPacketWriterArgumentsException(args, e);
		}
	}
	
	/**
	 * Make an item appear in the game world.
	 * 
	 * @param client A client connected to the game world
	 * @param oid Item runtime ID
	 * @param item Item template ID
	 * @param location A location within the game world
	 * @param stackable Whether multiple items take a single slot
	 * @param amount Item amount
	 * @param unk 0 by default
	 */
	public static final void sendSpawnItem(L2GameClient client, int oid, int item, IPoint3D location, boolean stackable, long amount, int unk)
	{
		final int size = 1 + 7 * 4 + 8;
		final ByteBuffer bb = allocate(size);
		final MMOBuffer buf = allocate(bb);
		
		buf.writeC(0x05);
		buf.writeD(oid);
		buf.writeD(item);
		buf.writeD(location.getX());
		buf.writeD(location.getY());
		buf.writeD(location.getZ());
		buf.writeD(stackable);
		buf.writeQ(amount);
		buf.writeD(unk);
		
		client.sendPacket(new ProxyRepeatedPacket(bb));
	}
	
	/**
	 * Make an object disappear from the game world.
	 * 
	 * @param client A client connected to the game world
	 * @param oid Object runtime ID
	 * @param unk 0 by default
	 */
	public static final void sendDeleteObject(L2GameClient client, int oid, int unk)
	{
		final int size = 1 + 4 + 1;
		final ByteBuffer bb = allocate(size);
		final MMOBuffer buf = allocate(bb);
		
		buf.writeC(0x08);
		buf.writeD(oid);
		buf.writeC(unk);
		
		client.sendPacket(new ProxyRepeatedPacket(bb));
	}
	
	/**
	 * Display a generic chat message. Cannot be used to send private messages or ferry announcements.
	 * 
	 * @param client A client connected to the game world
	 * @param chat Chat channel (type)
	 * @param talker Message author
	 * @param message Message
	 * @throws InvalidPacketWriterArgumentsException if a private message or a ferry announcement is being sent
	 */
	public static final void sendChatMessage(L2GameClient client, int chat, String talker, String message) throws InvalidPacketWriterArgumentsException
	{
		if (chat == CHAT_PM)
			throw new InvalidPacketWriterArgumentsException("Use the designated method for private messages");
		if (chat == 13)
			throw new InvalidPacketWriterArgumentsException("Method only accepts talker as string");
		
		final int size = 1 + 4 + 4 + stdStringSize(talker) + 4 + stdStringSize(message);
		final ByteBuffer bb = allocate(size);
		final MMOBuffer buf = allocate(bb);
		
		buf.writeC(0x4A);
		buf.writeD(0);
		buf.writeD(chat);
		buf.writeS(talker);
		if (client.getProtocol().isNewerThanOrEqualTo(ClientProtocolVersion.FREYA))
			buf.writeD(-1);
		buf.writeS(message);
		
		client.sendPacket(new ProxyRepeatedPacket(bb));
	}
	
	/**
	 * Display a private chat message.
	 * 
	 * @param client A client connected to the game world
	 * @param talker Message author
	 * @param message Message
	 * @param talkerLevel Author character's level
	 * @param talkerFlags Relations to the author's character
	 */
	public static final void sendPrivateMessage(L2GameClient client, String talker, String message, int talkerLevel, int talkerFlags)
	{
		final int size = 1 + 4 + 4 + stdStringSize(talker) + 4 + stdStringSize(message) + 1 + 1;
		final ByteBuffer bb = allocate(size);
		final MMOBuffer buf = allocate(bb);
		
		buf.writeC(0x4A);
		buf.writeD(0);
		buf.writeD(2);
		buf.writeS(talker);
		if (client.getProtocol().isNewerThanOrEqualTo(ClientProtocolVersion.FREYA))
			buf.writeD(-1);
		buf.writeS(message);
		buf.writeC(talkerLevel);
		buf.writeC(talkerFlags);
		
		client.sendPacket(new ProxyRepeatedPacket(bb));
	}
	
	private static final void sendScreenMessage(L2GameClient client, int sm, int position, int unk1, boolean small, int unk2, int unk3, boolean deco, int duration, boolean fade, String message)
	{
		final int size = 3 + 4 * 10 + 4 + stdStringSize(message) + stdStringSize("") * 4;
		final ByteBuffer bb = allocate(size);
		final MMOBuffer buf = allocate(bb);
		
		buf.writeC(0xFE);
		buf.writeH(0x39);
		
		buf.writeD(sm == -1);
		buf.writeD(sm);
		buf.writeD(position);
		buf.writeD(unk1);
		buf.writeD(small);
		buf.writeD(unk2);
		buf.writeD(unk3);
		buf.writeD(deco);
		buf.writeD(duration);
		buf.writeD(fade);
		
		buf.writeD(-1);
		buf.writeS(message);
		for (int i = 0; i < 4; ++i)
			buf.writeS("");
		
		client.sendPacket(new ProxyRepeatedPacket(bb));
	}
	
	/**
	 * Display a message on screen.
	 * 
	 * @param client A client connected to the game world
	 * @param position Message position
	 * @param unk1 0 by default
	 * @param small Whether this is a primary ({@code false}) or a secondary ({@code true}) message
	 * @param unk2 0 by default
	 * @param unk3 1 by default
	 * @param deco Whether a decorative texture should be shown above the message
	 * @param duration Message display duration, in milliseconds
	 * @param fade Whether this message should fade out when it expires
	 * @param message Message
	 */
	public static final void sendScreenMessage(L2GameClient client, int position, int unk1, boolean small, int unk2, int unk3, boolean deco, int duration, boolean fade, String message)
	{
		sendScreenMessage(client, -1, position, unk1, small, unk2, unk3, deco, duration, fade, message);
	}
	
	/**
	 * Display a predefined message on screen.
	 * 
	 * @param client A client connected to the game world
	 * @param sm Predefined system message ID
	 * @param position Message position
	 * @param unk1 0 by default
	 * @param small Whether this is a primary ({@code false}) or a secondary ({@code true}) message
	 * @param unk2 0 by default
	 * @param unk3 1 by default
	 * @param deco Whether a decorative texture should be shown above the message
	 * @param duration Message display duration, in milliseconds
	 * @param fade Whether this message should fade out when it expires
	 */
	public static final void sendImmutableScreenSystemMessage(L2GameClient client, int sm, int position, int unk1, boolean small, int unk2, int unk3, boolean deco, int duration, boolean fade)
	{
		sendScreenMessage(client, sm, position, unk1, small, unk2, unk3, deco, duration, fade, "");
	}
	
	/**
	 * Sends a HTML message to be displayed in the NPC chat window.
	 * 
	 * @param client A client connected to the game world
	 * @param html HTML message
	 */
	public static final void sendHTML(L2GameClient client, String html)
	{
		final int size = 1 + 4 + stdStringSize(html) + 4 + 4;
		if (size > 0xFF_FF - 2)
			throw new IllegalArgumentException("Packet requires fragmentation");
		final ByteBuffer bb = allocate(size);
		final MMOBuffer buf = allocate(bb);
		
		buf.writeC(0x19);
		buf.writeD(0);
		buf.writeS(html);
		buf.writeD(0);
		buf.writeD(1);
		
		client.sendPacket(new ProxyRepeatedPacket(bb));
	}
	
	/**
	 * Sends a HTML message to be displayed in the tutorial chat window.
	 * 
	 * @param client A client connected to the game world
	 * @param html A client connected to the game world
	 */
	public static final void sendTutorialHTML(L2GameClient client, String html)
	{
		final int size = 1 + 4 + stdStringSize(html);
		if (size > 0xFF_FF - 2)
			throw new IllegalArgumentException("Packet requires fragmentation");
		final ByteBuffer bb = allocate(size);
		final MMOBuffer buf = allocate(bb);
		
		buf.writeC(0xA6);
		buf.writeD(1); // standard (textual) HTML
		buf.writeS(html);
		
		client.sendPacket(new ProxyRepeatedPacket(bb));
	}
	
	/**
	 * Closes the tutorial chat window (if it is open).
	 * 
	 * @param client A client connected to the game world
	 */
	public static final void sendTutorialCloseHTML(L2GameClient client)
	{
		client.sendPacket(new ProxyRepeatedPacket((byte)0xA9));
	}
	
	/**
	 * Displays a waypoint on the radar, map and shows an overhead indicator.
	 * 
	 * @param client A client connected to the game world
	 * @param location Destination
	 */
	public static final void sendWaypoint(L2GameClient client, IPoint3D location)
	{
		sendRadarControl(client, 2, 2, location);
		sendRadarControl(client, 0, 1, location);
	}
	
	private static final void sendRadarControl(L2GameClient client, int action, int marker, IPoint3D location)
	{
		final int size = 1 + 4 + 4 + 4 + 4 + 4;
		final ByteBuffer bb = allocate(size);
		final MMOBuffer buf = allocate(bb);
		
		buf.writeC(0xF1);
		buf.writeD(action);
		buf.writeD(marker);
		buf.writeD(location.getX());
		buf.writeD(location.getY());
		buf.writeD(location.getZ());
		
		client.sendPacket(new ProxyRepeatedPacket(bb));
	}
	
	// the most convenient one, plus it can be reused to actually show real territories
	public static final void sendTerritoryServerPrimitive(L2GameClient client, String alias, IPoint3D centroid, int clipX, int clipY, int minZ, int maxZ, IPoint2D... vertices)
	{
		int dynamicPartSize = 0;
		
		final int nvert = vertices.length;
		final List<Triple<PrimitiveExtras, IPoint3D, IPoint3D>> segments = new ArrayList<>(nvert * 3);
		for (int i = 0; i < nvert; ++i)
		{
			final int v1 = i, v2 = (i + 1) % nvert;
			final String labelEx = alias + "(" + v1 + "->" + v2 + ")";
			final PrimitiveExtras ex = new PrimitiveExtras(labelEx, 0, 255, 0, 255);
			segments.add(Triple.of(ex, new Point3D(vertices[v1], maxZ), new Point3D(vertices[v2], maxZ)));
			segments.add(Triple.of(ex, new Point3D(vertices[v1], minZ), new Point3D(vertices[v2], minZ)));
			segments.add(Triple.of(new PrimitiveExtras("", 0, 255, 0, 255), new Point3D(vertices[v1], minZ), new Point3D(vertices[v1], maxZ)));
			dynamicPartSize += 3; // type
			dynamicPartSize += 2 * stdStringSize(labelEx) + stdStringSize(""); // alias
			dynamicPartSize += 3 * (4 * 4); // extras
			dynamicPartSize += 3 * (6 * 4); // line segment
		}
		final int size = 1 + 2 + stdStringSize(alias) + (3 * 4) + 4 + 4 + 4 + dynamicPartSize;
		final ByteBuffer bb = allocate(size);
		final MMOBuffer buf = allocate(bb);
		
		buf.writeC(0xFE);
		buf.writeH(0x11);
		buf.writeS(alias);
		buf.writeD(centroid.getX());
		buf.writeD(centroid.getY());
		buf.writeD(centroid.getZ());
		buf.writeD(clipX);
		buf.writeD(clipY);
		buf.startLoop(ReservedFieldType.DWORD);
		for (final Triple<PrimitiveExtras, IPoint3D, IPoint3D> segment : segments)
		{
			buf.writeC(2); // line segment
			segment.getLeft().write(buf);
			final IPoint3D start = segment.getMiddle(), end = segment.getRight();
			buf.writeD(start.getX());
			buf.writeD(start.getY());
			buf.writeD(start.getZ());
			buf.writeD(end.getX());
			buf.writeD(end.getY());
			buf.writeD(end.getZ());
			
			buf.countLoopElement();
		}
		buf.endLoop();
		client.sendPacket(new ProxyRepeatedPacket(bb));
		//client.notifyPacketForwarded(null, allocate(size).put(bb.array()), System.currentTimeMillis());
	}
	
	public static final MutableInt sendLineExServerPrimitive(L2GameClient client, MutableInt counter, IPoint3D centroid, int clipX, int clipY, int r, int g, int b,
			List<Pair<IPoint3D, IPoint3D>> segments)
	{
		if (counter == null)
			counter = new MutableInt(0);
		
		final String alias = "ServerPrimitive_line" + counter.incrementAndGet();
		
		final PrimitiveExtras ex = new PrimitiveExtras("", r, g, b, 255);
		final int dynamicPartSize = segments.size() * (1 + 6 * 4 + 3 * (4 * 4));
		final int size = 1 + 2 + stdStringSize(alias) + (3 * 4) + 4 + 4 + 4 + dynamicPartSize;
		final ByteBuffer bb = allocate(size);
		final MMOBuffer buf = allocate(bb);
		
		buf.writeC(0xFE);
		buf.writeH(0x11);
		buf.writeS(alias);
		buf.writeD(centroid.getX());
		buf.writeD(centroid.getY());
		buf.writeD(centroid.getZ());
		buf.writeD(clipX);
		buf.writeD(clipY);
		buf.startLoop(ReservedFieldType.DWORD);
		for (final Pair<IPoint3D, IPoint3D> segment : segments)
		{
			buf.writeC(2); // line segment
			ex.write(buf);
			final IPoint3D start = segment.getLeft(), end = segment.getRight();
			buf.writeD(start.getX());
			buf.writeD(start.getY());
			buf.writeD(start.getZ());
			buf.writeD(end.getX());
			buf.writeD(end.getY());
			buf.writeD(end.getZ());
			
			buf.countLoopElement();
		}
		buf.endLoop();
		client.sendPacket(new ProxyRepeatedPacket(bb));
		//client.notifyPacketForwarded(null, allocate(size).put(bb.array()), System.currentTimeMillis());
		return counter;
	}
	
	private static final void sendShortRequest(L2GameServer server, int opcode, int entityID, boolean forceAttack, boolean prohibitMovement)
	{
		final int size = 1 + 4 + 4 + 1;
		final ByteBuffer bb = allocate(size);
		final MMOBuffer buf = allocate(bb);
		
		buf.writeC(opcode);
		buf.writeD(entityID);
		buf.writeD(forceAttack);
		buf.writeC(prohibitMovement);
		
		server.sendPacket(new ProxyRepeatedPacket(bb));
	}
	
	public static final void sendRequestMSU(L2GameServer server, int skill, boolean forceAttack, boolean prohibitMovement)
	{
		sendShortRequest(server, 0x39, skill, forceAttack, prohibitMovement);
	}
	
	public static final void sendRequestActionUse(L2GameServer server, int action, boolean forceAttack, boolean prohibitMovement)
	{
		sendShortRequest(server, 0x56, action, forceAttack, prohibitMovement);
	}
	
	private static final void sendSimpleRequest(L2GameServer server, int opcode, int targetOID, IPoint3D origin, boolean prohibitMovement)
	{
		final int size = 1 + 4 + 4 + 4 + 4 + 1;
		final ByteBuffer bb = allocate(size);
		final MMOBuffer buf = allocate(bb);
		
		buf.writeC(opcode);
		buf.writeD(targetOID);
		buf.writeD(origin.getX());
		buf.writeD(origin.getY());
		buf.writeD(origin.getZ());
		buf.writeC(prohibitMovement);
		
		server.sendPacket(new ProxyRepeatedPacket(bb));
	}
	
	public static final void sendRequestAttack(L2GameServer server, int targetOID, IPoint3D origin, boolean prohibitMovement)
	{
		sendSimpleRequest(server, 0x01, targetOID, origin, prohibitMovement);
	}
	
	public static final void sendRequestAction(L2GameServer server, int targetOID, IPoint3D origin, boolean prohibitMovement)
	{
		sendSimpleRequest(server, 0x1F, targetOID, origin, prohibitMovement);
	}
	
	public static final void sendSay2(L2GameServer server, String message, int chat)
	{
		sendSay2(server, message, chat, null);
	}
	
	public static final void sendSay2(L2GameServer server, String message, int chat, String recipient)
	{
		if ((chat == CHAT_PM) != (recipient != null))
			throw new IllegalArgumentException(chat + " " + recipient);
		
		final int size = 1 + stdStringSize(message) + 4 + (recipient != null ? stdStringSize(recipient) : 0);
		final ByteBuffer bb = allocate(size);
		final MMOBuffer buf = allocate(bb);
		
		buf.writeC(0x49);
		buf.writeS(message);
		buf.writeD(chat);
		if (recipient != null)
			buf.writeS(recipient);
		
		server.sendPacket(new ProxyRepeatedPacket(bb));
	}
	
	public static final void sendUserCmd(L2GameServer server, int command)
	{
		final int size = 1 + 4;
		final ByteBuffer bb = allocate(size);
		final MMOBuffer buf = allocate(bb);
		
		buf.writeC(0xB3);
		buf.writeD(command);
		
		server.sendPacket(new ProxyRepeatedPacket(bb));
	}
	
	public static final void sendRequestTutorialLink(L2GameServer server, String action)
	{
		final int size = 1 + 4 + stdStringSize(action);
		final ByteBuffer bb = allocate(size);
		final MMOBuffer buf = allocate(bb);
		
		buf.writeC(0xA6);
		buf.writeD(1);
		buf.writeS(action);
		
		server.sendPacket(new ProxyRepeatedPacket(bb));
	}
	
	public static final void sendRequestBypassToServer(L2GameServer server, String action)
	{
		final int size = 1 + stdStringSize(action);
		final ByteBuffer bb = allocate(size);
		final MMOBuffer buf = allocate(bb);
		
		buf.writeC(0x23);
		buf.writeS(action);
		
		server.sendPacket(new ProxyRepeatedPacket(bb));
	}
	
	public static final void sendRequestEscapeScene(L2GameServer server)
	{
		server.sendPacket(new ProxyRepeatedPacket((byte)0xD0, (byte)0x90, (byte)0x00));
	}
	
	public static final void sendMoveWithDelta(L2GameServer server, IPoint3D delta)
	{
		final int size = 1 + 4 + 4 + 4;
		final ByteBuffer bb = allocate(size);
		final MMOBuffer buf = allocate(bb);
		
		buf.writeC(0x52);
		buf.writeD(delta.getX());
		buf.writeD(delta.getY());
		buf.writeD(delta.getZ());
		
		server.sendPacket(new ProxyRepeatedPacket(bb));
	}
	
	public static final void sendRequestDestroyItem(L2GameServer server, int itemOID, long amount)
	{
		final int size = 1 + 4 + 8;
		final ByteBuffer bb = allocate(size);
		final MMOBuffer buf = allocate(bb);
		
		buf.writeC(0x60);
		buf.writeD(itemOID);
		buf.writeQ(amount);
		
		server.sendPacket(new ProxyRepeatedPacket(bb));
	}
	
	public static final void sendRequestUseItem(L2GameServer server, int itemOID, boolean forceAttack)
	{
		final int size = 1 + 4 + 4;
		final ByteBuffer bb = allocate(size);
		final MMOBuffer buf = allocate(bb);
		
		buf.writeC(0x19);
		buf.writeD(itemOID);
		buf.writeD(forceAttack);
		
		server.sendPacket(new ProxyRepeatedPacket(bb));
	}
	
	public static final void sendRequestShortcutRegItem(L2GameServer server, int slot, int itemOID, int unk1, int executor, int unk2, int unk3)
	{
		final int size = 1 + 4 + 4 + 4 + 4 + 4 + 4 + 4;
		final ByteBuffer bb = allocate(size);
		final MMOBuffer buf = allocate(bb);
		
		buf.writeC(0x3D);
		buf.writeD(1);
		buf.writeD(slot);
		buf.writeD(itemOID);
		buf.writeD(unk1);
		buf.writeD(executor);
		buf.writeD(unk2);
		buf.writeD(unk3);
		
		server.sendPacket(new ProxyRepeatedPacket(bb));
	}
	
	public static final void sendRequestAutoSoulShot(L2GameServer server, int itemTemplateID, boolean enable)
	{
		final int size = 3 + 4 + 4;
		final ByteBuffer bb = allocate(size);
		final MMOBuffer buf = allocate(bb);
		
		buf.writeC(0xD0);
		buf.writeH(0x0D);
		buf.writeD(itemTemplateID);
		buf.writeD(enable);
		
		server.sendPacket(new ProxyRepeatedPacket(bb));
	}
	
	public static final void sendRequestAutoFish(L2GameServer server, boolean enable)
	{
		final byte[] content = new byte[] { (byte)0xD0, (byte)0x05, (byte)0x01, (byte)(enable ? 0x01 : 0x00) };
		server.sendPacket(new ProxyRepeatedPacket(content));
		//server.getTargetClient().notifyPacketForwarded(null, ByteBuffer.wrap(content).order(ByteOrder.LITTLE_ENDIAN), System.currentTimeMillis());
	}
	
	public static final void sendRequestSkillList(L2GameServer server)
	{
		server.sendPacket(new ProxyRepeatedPacket((byte)0x50));
	}
	
	public static final void sendRequestAcquireSkillInfo(L2GameServer server, int skillID, int skillLevel, int learnType)
	{
		sendLearnableSkillRequest(server, 0x73, skillID, skillLevel, learnType);
	}
	
	public static final void sendRequestAcquireSkill(L2GameServer server, int skillID, int skillLevel, int learnType)
	{
		sendLearnableSkillRequest(server, 0x7C, skillID, skillLevel, learnType);
	}
	
	private static final void sendRequestExD(L2GameServer server, int exOpcode, int value)
	{
		final int size = 1 + 2 + 4;
		final ByteBuffer bb = allocate(size);
		final MMOBuffer buf = allocate(bb);
		
		buf.writeC(0xD0);
		buf.writeH(exOpcode);
		buf.writeD(value);
		
		server.sendPacket(new ProxyRepeatedPacket(bb));
	}
	
	public static final void sendRequestExRqItemLink(L2GameServer server, int objectID)
	{
		sendRequestExD(server, 0x1E, objectID);
	}
	
	public static final void sendAnswerJoinPartyRoom(L2GameServer server, boolean accepted)
	{
		sendRequestExD(server, 0x30, accepted ? 1 : 0);
	}
	
	public static final void sendRequestExAcceptJoinMPCC(L2GameServer server, boolean accepted, int unknown)
	{
		final int size = 1 + 2 + 4 + 4;
		final ByteBuffer bb = allocate(size);
		final MMOBuffer buf = allocate(bb);
		
		buf.writeC(0xD0);
		buf.writeH(0x07);
		buf.writeD(accepted);
		buf.writeD(unknown);
		
		server.sendPacket(new ProxyRepeatedPacket(bb));
	}
	
	public static final void sendRequestAnswerCoupleAction(L2GameServer server, int action, boolean accepted, int requestorID)
	{
		final int size = 1 + 2 + 4 + 4 + 4;
		final ByteBuffer bb = allocate(size);
		final MMOBuffer buf = allocate(bb);
		
		buf.writeC(0xD0);
		buf.writeH(0x77);
		buf.writeD(action);
		buf.writeD(accepted);
		buf.writeD(requestorID);
		
		server.sendPacket(new ProxyRepeatedPacket(bb));
	}
	
	public static final void sendRequestFriendAddReply(L2GameServer server, int unknown, boolean accepted)
	{
		final int size = 1 + 1 + 4;
		final ByteBuffer bb = allocate(size);
		final MMOBuffer buf = allocate(bb);
		
		buf.writeC(0x78);
		buf.writeC(unknown);
		buf.writeD(accepted);
		
		server.sendPacket(new ProxyRepeatedPacket(bb));
	}
	
	public static final void sendRequestAnswerJoinParty(L2GameServer server, boolean accepted)
	{
		sendRequestAnswer(server, 0x43, accepted);
	}
	
	public static final void sendAnswerTradeRequest(L2GameServer server, boolean accepted)
	{
		sendRequestAnswer(server, 0x55, accepted);
	}
	
	private static final void sendRequestAnswer(L2GameServer server, int opcode, boolean accepted)
	{
		final int size = 1 + 4;
		final ByteBuffer bb = allocate(size);
		final MMOBuffer buf = allocate(bb);
		
		buf.writeC(opcode);
		buf.writeD(accepted);
		
		server.sendPacket(new ProxyRepeatedPacket(bb));
	}
	
	public static final void sendRequestExTryToPutEnchantTargetItem(L2GameServer server, int itemOID)
	{
		sendRequestExD(server, 0x49, itemOID);
	}
	
	public static final void sendRequestExCancelEnchantItem(L2GameServer server)
	{
		sendRequestEx(server, 0x4B);
	}
	
	public static final void sendRequestExAddEnchantScrollItem(L2GameServer server, int scrollOID, int itemOID)
	{
		final int size = 1 + 2 + 4 + 4;
		final ByteBuffer bb = allocate(size);
		final MMOBuffer buf = allocate(bb);
		
		buf.writeC(0xD0);
		buf.writeH(0xE3);
		buf.writeD(scrollOID);
		buf.writeD(itemOID);
		
		server.sendPacket(new ProxyRepeatedPacket(bb));
	}
	
	public static final void sendRequestExRemoveEnchantSupportItem(L2GameServer server)
	{
		sendRequestEx(server, 0xE4);
	}
	
	public static final void sendRequestEnchantItem(L2GameServer server, int itemOID, int supportItemOID)
	{
		final int size = 1 + 4 + 4;
		final ByteBuffer bb = allocate(size);
		final MMOBuffer buf = allocate(bb);
		
		buf.writeC(0x5F);
		buf.writeD(itemOID);
		buf.writeD(supportItemOID);
		
		server.sendPacket(new ProxyRepeatedPacket(bb));
	}
	
	public static final void sendRequestCuriousHouseHtml(L2GameServer server)
	{
		sendRequestEx(server, 0xC1);
	}
	
	public static final void sendRequestAnswerPartyLootingModify(L2GameServer server, boolean accepted)
	{
		sendRequestExD(server, 0x76, accepted ? 1 : 0);
	}
	
	public static final void sendRequestRaidBossSpawnInfo(L2GameServer server, Collection<Integer> templateIDs)
	{
		final int size = 1 + 2 + 4 + 4 * templateIDs.size();
		final ByteBuffer bb = allocate(size);
		final MMOBuffer buf = allocate(bb);
		
		buf.writeC(0xD0);
		buf.writeH(0x129);
		buf.writeD(templateIDs.size());
		for (final Integer id : templateIDs)
			buf.writeD(id);
		
		server.sendPacket(new ProxyRepeatedPacket(bb));
	}
	
	public static final void sendConfirmDlg(L2GameServer server, int message, boolean answer, int ref)
	{
		final int size = 1 + 4 + 4 + 4;
		final ByteBuffer bb = CommonPacketSender.allocate(size);
		final MMOBuffer buf = CommonPacketSender.allocate(bb);
		
		buf.writeC(0xC6);
		buf.writeD(message);
		buf.writeD(answer);
		buf.writeD(ref);
		
		server.sendPacket(new ProxyRepeatedPacket(bb));
	}
	
	public static final void sendRequestPledgeMissionReward(L2GameServer server, int mission)
	{
		sendRequestExD(server, 0x01_43, mission);
	}
	
	private static final void sendRequestEx(L2GameServer server, int secondOp)
	{
		server.sendPacket(new ProxyRepeatedPacket((byte)0xD0, (byte)(secondOp & 0xFF), (byte)(secondOp >> 8)));
	}
	
	private static final void sendLearnableSkillRequest(L2GameServer server, int opcode, int skillID, int skillLevel, int learnType)
	{
		final int size = 1 + 4 + 4 + 4;
		final ByteBuffer bb = allocate(size);
		final MMOBuffer buf = allocate(bb);
		
		buf.writeC(opcode);
		buf.writeD(skillID);
		buf.writeD(skillLevel);
		buf.writeD(learnType);
		
		server.sendPacket(new ProxyRepeatedPacket(bb));
	}
	
	public static final int stdStringSize(String str)
	{
		return (str.length() + 1) << 1;
	}
	
	public static final MMOBuffer allocate(ByteBuffer bb)
	{
		final MMOBuffer buf = POOL.get();
		buf.setByteBuffer(bb);
		return buf;
	}
	
	public static final ByteBuffer allocate(int capacity) throws IllegalArgumentException
	{
		if (capacity > 0xFF_FC)
			throw new IllegalArgumentException("Invalid packet size: " + capacity);
		
		return ByteBuffer.allocate(capacity).order(ByteOrder.LITTLE_ENDIAN);
	}
	
	private static final class WriterPool extends ObjectPool<MMOBuffer>
	{
		WriterPool()
		{
		}
		
		@Override
		protected MMOBuffer create()
		{
			return new MMOBuffer();
		}
		
		@Override
		protected void reset(MMOBuffer e)
		{
			while (e.stopTrackingWrites() != -1 || e.endLoop() != -1)
			{
				// discard internal data
			}
			e.setByteBuffer(null);
		}
	}
	
	private static final class PrimitiveExtras
	{
		private final String _label;
		private final int _red, _green, _blue, _alpha;
		
		PrimitiveExtras(String label, int red, int green, int blue, int alpha)
		{
			_label = label;
			_red = red;
			_green = green;
			_blue = blue;
			_alpha = alpha;
		}
		
		void write(MMOBuffer buf)
		{
			buf.writeS(_label);
			buf.writeD(_red);
			buf.writeD(_green);
			buf.writeD(_blue);
			buf.writeD(_alpha);
		}
	}
}
