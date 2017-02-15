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

import java.util.List;

import net.l2emuproject.network.mmocore.MMOBuffer;
import net.l2emuproject.proxy.io.definitions.VersionnedPacketTable;
import net.l2emuproject.proxy.network.EndpointType;
import net.l2emuproject.proxy.network.game.server.L2GameServer;
import net.l2emuproject.proxy.network.meta.EnumeratedPayloadField;
import net.l2emuproject.proxy.network.meta.IPacketTemplate;
import net.l2emuproject.proxy.network.meta.RandomAccessMMOBuffer;
import net.l2emuproject.proxy.network.meta.container.MetaclassRegistry;
import net.l2emuproject.proxy.network.meta.exception.InvalidFieldValueInterpreterException;
import net.l2emuproject.proxy.network.meta.interpreter.IntegerInterpreter;
import net.l2emuproject.proxy.script.ScriptFieldAlias;
import net.l2emuproject.proxy.script.analytics.user.LiveUserAnalytics;
import net.l2emuproject.proxy.script.analytics.user.LiveUserAnalytics.UserInfo;
import net.l2emuproject.proxy.state.entity.L2ObjectInfo;
import net.l2emuproject.proxy.state.entity.L2ObjectInfoCache;
import net.l2emuproject.proxy.state.entity.ObjectInfo;
import net.l2emuproject.proxy.state.entity.context.ICacheServerID;
import net.l2emuproject.proxy.state.entity.type.ItemType;
import net.l2emuproject.proxy.state.entity.type.NonPlayerControllable;
import net.l2emuproject.proxy.state.entity.type.ObjectType;
import net.l2emuproject.proxy.state.entity.type.PetType;
import net.l2emuproject.proxy.state.entity.type.PlayerControllable;
import net.l2emuproject.proxy.state.entity.type.StaticObjectType;
import net.l2emuproject.proxy.state.entity.type.SummonType;
import net.l2emuproject.util.logging.L2Logger;

/**
 * Allows OIDs to be interpreted in a packet log file.
 * 
 * @author savormix
 */
public class ObjectAnalytics extends PpeAnalyticsScript
{
	private static final L2Logger LOG = L2Logger.getLogger(ObjectAnalytics.class);
	
	@ScriptFieldAlias
	private static final String PLAYER_ID = "OIC_PLAYER_OID";
	@ScriptFieldAlias
	private static final String PLAYER_NAME = "OIC_PLAYER_NAME";
	// private static final String PLAYER_TITLE = "OIC_PLAYER_TITLE";
	@ScriptFieldAlias
	private static final String NPC_ID = "OIC_NPC_OID";
	@ScriptFieldAlias
	private static final String PET_ID = "OIC_PET_OID";
	@ScriptFieldAlias
	private static final String SUMMON_ID = "OIC_SUMMON_OID";
	@ScriptFieldAlias
	private static final String NPC_NAME_ID = "OIC_NPC_TEMPLATE";
	@ScriptFieldAlias
	private static final String NPC_NAME_STRING = "OIC_GIVEN_NAME";
	@ScriptFieldAlias
	private static final String NPC_TITLE_STRING = "OIC_NPC_TITLE";
	/*
	private static final String NPC_NAME_NPCSTRING = "OIC_NPC_NAME_D";
	private static final String NPC_TITLE_NPCSTRING = "OIC_NPC_TITLE_D";
	*/
	@ScriptFieldAlias
	private static final String ITEM_ID = "OIC_ITEM_OID";
	@ScriptFieldAlias
	private static final String ITEM_NAME_ID = "OIC_ITEM_TEMPLATE";
	@ScriptFieldAlias
	private static final String STATIC_OBJECT_ID = "OIC_SO_OID";
	@ScriptFieldAlias
	private static final String EDITOR_ID = "OIC_SO_TEMPLATE";
	
	@ScriptFieldAlias
	private static final String TARGET_SETTER_OID = "HLE_TARGET_SELECTOR_OID";
	@ScriptFieldAlias
	private static final String TARGET_OID = "HLE_TARGET_OID";
	@ScriptFieldAlias
	private static final String USER_TARGET_OID = "HLE_SELF_TARGET_OID";
	@ScriptFieldAlias
	private static final String TARGET_UNSETTER_OID = "HLE_TARGET_CANCELER_OID";
	
	private static boolean handleTargetChange(RandomAccessMMOBuffer rab, ICacheServerID cacheContext)
	{
		final EnumeratedPayloadField selfTarget = rab.getSingleFieldIndex(USER_TARGET_OID);
		if (selfTarget != null)
		{
			final L2GameServer server = rab.getInteractivePacketSource(L2GameServer.class);
			if (server != null)
			{
				final UserInfo ui = LiveUserAnalytics.getInstance().getUserInfo(server.getTargetClient());
				if (ui != null)
				{
					final int selector = ui.getUserOID();
					L2ObjectInfoCache.getOrAdd(selector, cacheContext).getExtraInfo().setTargetOID(rab.readInteger32(selfTarget));
				}
			}
			return true;
		}
		
		final EnumeratedPayloadField lostTarget = rab.getSingleFieldIndex(TARGET_UNSETTER_OID);
		if (lostTarget != null)
		{
			L2ObjectInfoCache.getOrAdd(rab.readInteger32(lostTarget), cacheContext).getExtraInfo().setTargetOID(SimpleEventListener.NO_TARGET);
			return true;
		}
		
		final EnumeratedPayloadField setter = rab.getSingleFieldIndex(TARGET_SETTER_OID), target = rab.getSingleFieldIndex(TARGET_OID);
		if (setter == null || target == null)
			return false;
		
		L2ObjectInfoCache.getOrAdd(rab.readInteger32(setter), cacheContext).getExtraInfo().setTargetOID(rab.readInteger32(target));
		return true;
	}
	
	private static boolean handlePlayerInfo(RandomAccessMMOBuffer rab, ICacheServerID cacheContext)
	{
		final List<EnumeratedPayloadField> oids = rab.getFieldIndices(PLAYER_ID);
		final List<EnumeratedPayloadField> names = rab.getFieldIndices(PLAYER_NAME);
		if (oids.isEmpty() || names.isEmpty())
			return false;
		
		// final List<EnumeratedPayloadField> titles = rab.getFieldIndices(PLAYER_TITLE);
		for (int i = 0; i < oids.size(); ++i)
		{
			final int oid = rab.readInteger32(oids.get(i));
			final String name = rab.readString(names.get(i));
			
			final ObjectInfo<L2ObjectInfo> oi = L2ObjectInfoCache.getOrAdd(oid, cacheContext).setType(new PlayerControllable());
			oi.setName(name);
		}
		
		return true;
	}
	
	private static boolean handleNpcInfo(RandomAccessMMOBuffer rab, ICacheServerID cacheContext)
	{
		int type = 0;
		List<EnumeratedPayloadField> oids = rab.getFieldIndices(NPC_ID);
		if (oids.isEmpty())
		{
			type = 1;
			oids = rab.getFieldIndices(PET_ID);
		}
		if (oids.isEmpty())
		{
			type = 2;
			oids = rab.getFieldIndices(SUMMON_ID);
		}
		final List<EnumeratedPayloadField> templates = rab.getFieldIndices(NPC_NAME_ID);
		if (oids.isEmpty() || templates.isEmpty())
			return false;
		
		final List<EnumeratedPayloadField> givenNames = rab.getFieldIndices(NPC_NAME_STRING);
		final List<EnumeratedPayloadField> ownerNames = rab.getFieldIndices(NPC_TITLE_STRING);
		
		IntegerInterpreter interp = null;
		try
		{
			interp = MetaclassRegistry.getInstance().getInterpreter("Npc", IntegerInterpreter.class);
		}
		catch (final InvalidFieldValueInterpreterException e)
		{
			// whatever, template ID will suffice
		}
		for (int i = 0, t = -1; i < oids.size(); ++i)
		{
			final int oid = rab.readInteger32(oids.get(i));
			if (oid == 0)
				continue;
			
			final int id = rab.readInteger32(templates.get(++t));
			String name = interp != null ? String.valueOf(interp.getInterpretation(id, cacheContext)) : String.valueOf(id);
			
			final ObjectType objType;
			switch (type)
			{
				case 1:
					objType = new PetType(id);
					break;
				case 2:
					objType = new SummonType(id);
					break;
				default:
					objType = new NonPlayerControllable(id);
					break;
			}
			final ObjectInfo<L2ObjectInfo> oi = L2ObjectInfoCache.getOrAdd(oid, cacheContext).setType(objType);
			setRealName:
			{
				if (type > 0)
				{
					if (givenNames.size() <= t || ownerNames.size() <= t)
						break setRealName;
					
					final String petName = rab.readString(givenNames.get(t));
					if (!petName.isEmpty())
						name = petName + " (" + name + ")";
					
					final String ownerName = rab.readString(ownerNames.get(t));
					if (!ownerName.isEmpty())
						name += ", owner " + ownerName;
				}
				oi.setName(name);
			}
		}
		
		return true;
	}
	
	private static boolean handleItemInfo(RandomAccessMMOBuffer rab, ICacheServerID cacheContext)
	{
		final List<EnumeratedPayloadField> oids = rab.getFieldIndices(ITEM_ID);
		if (oids.isEmpty())
			return false;
		
		final List<EnumeratedPayloadField> names = rab.getFieldIndices(ITEM_NAME_ID);
		IntegerInterpreter interp = null;
		try
		{
			interp = MetaclassRegistry.getInstance().getInterpreter("Item", IntegerInterpreter.class);
		}
		catch (final InvalidFieldValueInterpreterException e)
		{
			// whatever, template ID will suffice
		}
		for (int i = 0; i < oids.size(); ++i)
		{
			final int oid = rab.readInteger32(oids.get(i));
			if (oid == 0)
				continue;
			
			final int id = rab.readInteger32(names.get(i));
			final String name = interp != null ? String.valueOf(interp.getInterpretation(id, cacheContext)) : String.valueOf(id);
			final ObjectInfo<L2ObjectInfo> oi = L2ObjectInfoCache.getOrAdd(oid, cacheContext).setType(new ItemType(id));
			oi.setName(name);
		}
		
		return true;
	}
	
	private static boolean handleStaticObjectInfo(RandomAccessMMOBuffer rab, ICacheServerID cacheContext)
	{
		final List<EnumeratedPayloadField> oids = rab.getFieldIndices(STATIC_OBJECT_ID);
		if (oids.isEmpty())
			return false;
		
		final List<EnumeratedPayloadField> names = rab.getFieldIndices(EDITOR_ID);
		IntegerInterpreter interp = null;
		try
		{
			interp = MetaclassRegistry.getInstance().getInterpreter("StaticObject", IntegerInterpreter.class);
		}
		catch (final InvalidFieldValueInterpreterException e)
		{
			// whatever, template ID will suffice
		}
		for (int i = 0; i < oids.size(); ++i)
		{
			final int oid = rab.readInteger32(oids.get(i));
			if (oid == 0)
				continue;
			
			final int id = rab.readInteger32(names.get(i));
			final String name = interp != null ? String.valueOf(interp.getInterpretation(id, cacheContext)) : String.valueOf(id);
			final ObjectInfo<L2ObjectInfo> oi = L2ObjectInfoCache.getOrAdd(oid, cacheContext).setType(new StaticObjectType(id));
			oi.setName(name);
		}
		
		return true;
	}
	
	@Override
	public void handleClientPacket(RandomAccessMMOBuffer buf, ICacheServerID cacheContext) throws RuntimeException
	{
		// none handled
	}
	
	@Override
	public void handleServerPacket(RandomAccessMMOBuffer buf, ICacheServerID cacheContext) throws RuntimeException
	{
		try
		{
			if (handleTargetChange(buf, cacheContext))
				return;
			
			if (handleItemInfo(buf, cacheContext))
			{
				// due to legacy packets
				handlePlayerInfo(buf, cacheContext);
				return;
			}
			
			if (handleStaticObjectInfo(buf, cacheContext))
				return;
			
			handlePlayerInfo(buf, cacheContext);
			handleNpcInfo(buf, cacheContext);
		}
		catch (final IndexOutOfBoundsException e)
		{
			final MMOBuffer wrapper = buf.seekFirstOpcode();
			final byte[] packet = wrapper.readB(wrapper.getAvailableBytes());
			final IPacketTemplate template = VersionnedPacketTable.getInstance().getTemplate(buf.getProtocol(), EndpointType.SERVER, packet);
			LOG.warn("Invalid packet OIC definition for " + template);
		}
	}
	
	@Override
	public String getName()
	{
		return "Analytics – OID interpretation";
	}
	
	@Override
	public double getPriority()
	{
		return -Double.longBitsToDouble(0x7fefffffffffffffL);
	}
}
