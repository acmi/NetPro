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
package util.packet;

import static net.l2emuproject.proxy.network.EndpointType.CLIENT;
import static net.l2emuproject.proxy.network.EndpointType.SERVER;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import net.l2emuproject.geometry.IPoint3D;
import net.l2emuproject.network.ClientProtocolVersion;
import net.l2emuproject.network.IProtocolVersion;
import net.l2emuproject.network.mmocore.MMOBuffer;
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
		return ClientProtocolVersion.UNDERGROUND;
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
				default:
					throw new UnknownPacketIdentifierException(packet);
			}
		}
		catch (RuntimeException e)
		{
			throw new InvalidPacketWriterArgumentsException(args, e);
		}
	}
	
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
		buf.writeD(-1);
		buf.writeS(message);
		
		client.sendPacket(new ProxyRepeatedPacket(bb));
	}
	
	public static final void sendPrivateMessage(L2GameClient client, String talker, String message, int talkerLevel, int talkerFlags)
	{
		final int size = 1 + 4 + 4 + stdStringSize(talker) + 4 + stdStringSize(message) + 1 + 1;
		final ByteBuffer bb = allocate(size);
		final MMOBuffer buf = allocate(bb);
		
		buf.writeC(0x4A);
		buf.writeD(0);
		buf.writeD(2);
		buf.writeS(talker);
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
	
	public static final void sendScreenMessage(L2GameClient client, int position, int unk1, boolean small, int unk2, int unk3, boolean deco, int duration, boolean fade, String message)
	{
		sendScreenMessage(client, -1, position, unk1, small, unk2, unk3, deco, duration, fade, message);
	}
	
	public static final void sendImmutableScreenSystemMessage(L2GameClient client, int sm, int position, int unk1, boolean small, int unk2, int unk3, boolean deco, int duration, boolean fade)
	{
		sendScreenMessage(client, sm, position, unk1, small, unk2, unk3, deco, duration, fade, "");
	}
	
	public static final void sendHTML(L2GameClient client, String html)
	{
		final int size = 1 + 4 + stdStringSize(html) + 4 + 4;
		final ByteBuffer bb = allocate(size);
		final MMOBuffer buf = allocate(bb);
		
		buf.writeC(0x19);
		buf.writeD(0);
		buf.writeS(html);
		buf.writeD(0);
		buf.writeD(1);
		
		client.sendPacket(new ProxyRepeatedPacket(bb));
	}
	
	public static final void sendTutorialHTML(L2GameClient client, String html)
	{
		final int size = 1 + 4 + stdStringSize(html);
		final ByteBuffer bb = allocate(size);
		final MMOBuffer buf = allocate(bb);
		
		buf.writeC(0xA6);
		buf.writeD(1); // standard (textual) HTML
		buf.writeS(html);
		
		client.sendPacket(new ProxyRepeatedPacket(bb));
	}
	
	public static final void sendTutorialCloseHTML(L2GameClient client)
	{
		client.sendPacket(new ProxyRepeatedPacket((byte)0xA9));
	}
	
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
}
