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
package net.l2emuproject.proxy.script.analytics;

import static net.l2emuproject.proxy.script.analytics.SimpleEventListener.NO_TARGET;

import java.util.List;

import net.l2emuproject.geometry.point.PointGeometry;
import net.l2emuproject.lang.L2TextBuilder;
import net.l2emuproject.proxy.network.game.client.L2GameClient;
import net.l2emuproject.proxy.network.game.server.L2GameServer;
import net.l2emuproject.proxy.network.meta.EnumeratedPayloadField;
import net.l2emuproject.proxy.network.meta.RandomAccessMMOBuffer;
import net.l2emuproject.proxy.script.ScriptFieldAlias;
import net.l2emuproject.proxy.script.analytics.user.LiveUserAnalytics;
import net.l2emuproject.proxy.script.analytics.user.LiveUserAnalytics.UserInfo;
import net.l2emuproject.proxy.script.game.InteractiveChatCommands;
import net.l2emuproject.proxy.script.game.PpeEnabledGameScript;
import net.l2emuproject.proxy.script.packets.InvalidPacketWriterArgumentsException;
import net.l2emuproject.proxy.script.packets.PacketWriterRegistry;
import net.l2emuproject.proxy.script.packets.UnknownPacketIdentifierException;
import net.l2emuproject.proxy.script.packets.UnknownPacketStructureException;
import net.l2emuproject.proxy.state.entity.L2ObjectInfo;
import net.l2emuproject.proxy.state.entity.L2ObjectInfo.DestinationType;
import net.l2emuproject.proxy.state.entity.L2ObjectInfoCache;
import net.l2emuproject.proxy.state.entity.ObjectInfo;
import net.l2emuproject.proxy.state.entity.ObjectLocation;
import net.l2emuproject.proxy.state.entity.context.ICacheServerID;
import net.l2emuproject.util.logging.L2Logger;

/**
 * Allows OIDs to be interpreted in a packet log file.
 * 
 * @author savormix
 */
public class ObjectLocationAnalytics extends PpeEnabledGameScript implements InteractiveChatCommands
{
	private static final L2Logger LOG = L2Logger.getLogger(ObjectLocationAnalytics.class);
	
	@ScriptFieldAlias
	private static final String OBJECT_ID = "OIL_OBJECT_ID";
	@ScriptFieldAlias
	private static final String OBJECT_X = "OIL_OBJECT_X_SERVER";
	@ScriptFieldAlias
	private static final String OBJECT_Y = "OIL_OBJECT_Y_SERVER";
	@ScriptFieldAlias
	private static final String OBJECT_Z = "OIL_OBJECT_Z_SERVER";
	@ScriptFieldAlias
	private static final String OBJECT_YAW = "OIL_OBJECT_YAW";
	@ScriptFieldAlias
	private static final String OBJECT_X_DEST = "OIL_OBJECT_DESTINATION_X";
	@ScriptFieldAlias
	private static final String OBJECT_Y_DEST = "OIL_OBJECT_DESTINATION_Y";
	@ScriptFieldAlias
	private static final String OBJECT_Z_DEST = "OIL_OBJECT_DESTINATION_Z";
	@ScriptFieldAlias
	private static final String OBJECT_MOVE_MODE = "OIL_OBJECT_MOVE_MODE";
	@ScriptFieldAlias
	private static final String OBJECT_MOVE_ENV = "OIL_OBJECT_MOVE_ENVIRONMENT";
	@ScriptFieldAlias
	private static final String OBJECT_RUN_SPD = "OIL_OBJECT_RUN_SPEED";
	@ScriptFieldAlias
	private static final String OBJECT_WALK_SPD = "OIL_OBJECT_WALK_SPEED";
	@ScriptFieldAlias
	private static final String OBJECT_SPEED_MULTIPLIER = "OIL_OBJECT_SPEED_MULTI";
	
	private static final int CM_SEND_APPERING = 0x3A;
	
	@Override
	public void handleClientPacket(L2GameClient client, L2GameServer server, RandomAccessMMOBuffer buf) throws RuntimeException
	{
		if (buf.seekFirstOpcode().readUC() == CM_SEND_APPERING)
		{
			// if currently teleporting, mark as not moving and last move interrupted by teleporting
		}
		
		final String msg = buf.readFirstString(CHAT_COMMAND);
		if (!"\\\\oil".equals(msg))
			return;
		
		final UserInfo ui = LiveUserAnalytics.getInstance().getUserInfo(client);
		if (ui == null)
			return;
		
		final ICacheServerID context = getEntityContext(server);
		final L2ObjectInfo user = L2ObjectInfoCache.getOrAdd(ui.getUserOID(), context).getExtraInfo();
		final ObjectLocation loc = user.getCurrentLocation();
		final L2TextBuilder tb = new L2TextBuilder("Your OID is ").append(ui.getUserOID());
		{
			tb.append(", location (").append(loc.getX()).append("; ").append(loc.getY()).append("; ");
			tb.append(loc.getZ()).append(").");
		}
		try
		{
			PacketWriterRegistry.getInstance().sendPacket(client, "SM_SAY2", 5, "SYS", tb.toString());
		}
		catch (InvalidPacketWriterArgumentsException | UnknownPacketStructureException | UnknownPacketIdentifierException e)
		{
			LOG.error("", e);
			return;
		}
		
		{
			tb.setLength(0);
			tb.append("Speed: ").append(user.getMovementSpeed()).append(", moving: ").append(user.isMoving());
		}
		try
		{
			PacketWriterRegistry.getInstance().sendPacket(client, "SM_SAY2", 5, "SYS", tb.toString());
		}
		catch (InvalidPacketWriterArgumentsException | UnknownPacketStructureException | UnknownPacketIdentifierException e)
		{
			LOG.error("", e);
			return;
		}
		
		for (final Integer pet : ui.getServitorOIDs())
		{
			tb.setLength(0);
			final ObjectInfo<L2ObjectInfo> trg = L2ObjectInfoCache.getOrAdd(pet, context);
			final ObjectLocation loc2 = trg.getExtraInfo().getCurrentLocation();
			tb.append("Your servitor is ").append(trg).append(", location (").append(loc2.getX()).append("; ");
			tb.append(loc2.getY()).append("; ").append(loc2.getZ()).append("), dist: ");
			tb.append((int)PointGeometry.getRawSolidDistance(loc, loc2));
			
			try
			{
				PacketWriterRegistry.getInstance().sendPacket(client, "SM_SAY2", 5, "SYS", tb.toString());
			}
			catch (InvalidPacketWriterArgumentsException | UnknownPacketStructureException | UnknownPacketIdentifierException e)
			{
				LOG.error("", e);
				return;
			}
		}
		
		tb.setLength(0);
		final int targetOID = ui.getTargetOID();
		if (targetOID != NO_TARGET)
		{
			final ObjectInfo<L2ObjectInfo> trg = L2ObjectInfoCache.getOrAdd(targetOID, context);
			final ObjectLocation loc2 = trg.getExtraInfo().getCurrentLocation();
			tb.append("Your target is ").append(trg).append(", location (").append(loc2.getX()).append("; ");
			tb.append(loc2.getY()).append("; ").append(loc2.getZ()).append("), dist: ");
			tb.append((int)PointGeometry.getRawSolidDistance(loc, loc2));
		}
		else
			tb.append("You have no target selected at this time.");
		
		try
		{
			PacketWriterRegistry.getInstance().sendPacket(client, "SM_SAY2", 5, "SYS", tb.toString());
		}
		catch (InvalidPacketWriterArgumentsException | UnknownPacketStructureException | UnknownPacketIdentifierException e)
		{
			LOG.error("", e);
			return;
		}
	}
	
	@Override
	public void handleServerPacket(L2GameClient client, L2GameServer server, RandomAccessMMOBuffer buf) throws RuntimeException
	{
		final List<EnumeratedPayloadField> oids = buf.getFieldIndices(OBJECT_ID);
		if (oids.isEmpty())
			return;
		
		final List<EnumeratedPayloadField> xs = buf.getFieldIndices(OBJECT_X), ys = buf.getFieldIndices(OBJECT_Y), zs = buf.getFieldIndices(OBJECT_Z);
		final List<EnumeratedPayloadField> headings = buf.getFieldIndices(OBJECT_YAW);
		
		final List<EnumeratedPayloadField> xds = buf.getFieldIndices(OBJECT_X_DEST), yds = buf.getFieldIndices(OBJECT_Y_DEST), zds = buf.getFieldIndices(OBJECT_Z_DEST);
		
		final List<EnumeratedPayloadField> walkSpds = buf.getFieldIndices(OBJECT_WALK_SPD), runSpds = buf.getFieldIndices(OBJECT_RUN_SPD);
		final List<EnumeratedPayloadField> speedMultipliers = buf.getFieldIndices(OBJECT_SPEED_MULTIPLIER);
		final List<EnumeratedPayloadField> modes = buf.getFieldIndices(OBJECT_MOVE_MODE);
		
		final ICacheServerID cacheContext = getEntityContext(server);
		for (int i = 0; i < oids.size(); ++i)
		{
			final L2ObjectInfo oi = L2ObjectInfoCache.getOrAdd(buf.readInteger32(oids.get(i)), cacheContext).getExtraInfo();
			if (!xs.isEmpty())
			{
				final int x = buf.readInteger32(xs.get(i));
				final int y = buf.readInteger32(ys.get(i));
				final int z = buf.readInteger32(zs.get(i));
				final int yaw = headings.isEmpty() ? -1 : buf.readInteger32(headings.get(i));
				
				final ObjectLocation currentLocation = new ObjectLocation(x, y, z, yaw);
				
				if (!xds.isEmpty())
				{
					final int xd = buf.readInteger32(xds.get(i));
					final int yd = buf.readInteger32(yds.get(i));
					final int zd = buf.readInteger32(zds.get(i));
					
					oi.setDestination(currentLocation, new ObjectLocation(xd, yd, zd, yaw), DestinationType.STANDARD);
				}
				else
					oi.updateLocation(currentLocation);
			}
			
			if (!walkSpds.isEmpty())
			{
				final int[] templateWalkSpeeds = new int[3], templateRunSpeeds = new int[3];
				for (int j = 0; j < 3; ++j)
				{
					templateWalkSpeeds[j] = buf.readInteger32(walkSpds.get(i * 3 + j));
					templateRunSpeeds[j] = buf.readInteger32(runSpds.get(i * 3 + j));
				}
				oi.setTemplateMovementSpeed(templateWalkSpeeds, templateRunSpeeds);
			}
			
			if (!speedMultipliers.isEmpty())
				oi.setSpeedMultiplier(buf.readDecimal(speedMultipliers.get(i)));
			
			if (!modes.isEmpty())
			{
				final boolean running = buf.readInteger32(modes.get(i)) != 0;
				oi.setRunning(running);
			}
		}
	}
	
	@Override
	public String getName()
	{
		return "Analytics – object movement";
	}
	
	@Override
	public double getPriority()
	{
		return -Double.longBitsToDouble(0x7feffffffffffffeL);
	}
}
