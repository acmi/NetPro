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
package net.l2emuproject.proxy.script.game;

import static net.l2emuproject.proxy.script.analytics.SimpleEventListener.NO_TARGET;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.stream.Collectors;

import org.apache.commons.lang3.ArrayUtils;

import net.l2emuproject.proxy.network.game.client.L2GameClient;
import net.l2emuproject.proxy.network.game.server.L2GameServer;
import net.l2emuproject.proxy.network.meta.EnumeratedPayloadField;
import net.l2emuproject.proxy.network.meta.RandomAccessMMOBuffer;
import net.l2emuproject.proxy.script.ScriptFieldAlias;
import net.l2emuproject.proxy.script.analytics.SimpleEventListener;
import net.l2emuproject.proxy.script.analytics.user.LiveUserAnalytics;
import net.l2emuproject.proxy.script.analytics.user.LiveUserAnalytics.UserInfo;

import javolution.util.FastMap;

/**
 * @author _dev_
 */
public class HighLevelEventGenerator extends PpeEnabledGameScript
{
	@ScriptFieldAlias
	private static final String DECEASED_OID = "HLE_DECEASED_OID";
	@ScriptFieldAlias
	private static final String DECEASED_SWEEPABLE = "HLE_DECEASED_SWEEP";
	@ScriptFieldAlias
	private static final String REVIVED_OID = "HLE_REVIVED_OID";
	@ScriptFieldAlias
	private static final String DELETED_OID = "HLE_DELETED_OID";
	@ScriptFieldAlias
	private static final String TARGET_SETTER_OID = "HLE_TARGET_SELECTOR_OID";
	@ScriptFieldAlias
	private static final String TARGET_OID = "HLE_TARGET_OID";
	@ScriptFieldAlias
	private static final String USER_TARGET_OID = "HLE_SELF_TARGET_OID";
	@ScriptFieldAlias
	private static final String TARGET_UNSETTER_OID = "HLE_TARGET_CANCELER_OID";
	@ScriptFieldAlias
	private static final String ATTACKER_OID = "HLE_ATTACKER_OID";
	@ScriptFieldAlias
	private static final String MULTI_TARGET_OID = "HLE_TARGETS_OID";
	@ScriptFieldAlias
	private static final String CASTER_OID = "HLE_CASTER_OID";
	@ScriptFieldAlias
	private static final String SKILL_ID = "HLE_SKILL_ID";
	@ScriptFieldAlias
	private static final String SKILL_LVL = "HLE_SKILL_LEVEL";
	@ScriptFieldAlias
	private static final String CAST_TIME = "HLE_CAST_TIME";
	@ScriptFieldAlias
	private static final String COOL_TIME = "HLE_COOL_TIME";
	@ScriptFieldAlias
	private static final String CASTER_CANCEL_OID = "HLE_FAIL_CASTER_OID";
	@ScriptFieldAlias
	private static final String CASTER_DONE_OID = "HLE_SUCC_CASTER_OID";
	@ScriptFieldAlias
	private static final String WORLD_ITEM_OID = "HLE_WORLD_ITEM_OID";
	@ScriptFieldAlias
	private static final String WORLD_ITEM_AMOUNT = "HLE_WORLD_ITEM_AMOUNT";
	@ScriptFieldAlias
	private static final String WORLD_ITEM_DROPPER_OID = "HLE_WORLD_ITEM_DROPPER_OID";
	@ScriptFieldAlias
	private static final String WORLD_ITEM_FINDER_OID = "HLE_WORLD_ITEM_OWNER_OID";
	@ScriptFieldAlias
	private static final String EFFECT_SKILL_COUNT = "HLE_SELF_EFFECT_COUNT";
	@ScriptFieldAlias
	private static final String EFFECT_SKILL_ID = "HLE_SELF_EFFECT_SKILL";
	@ScriptFieldAlias
	private static final String STOPPED_OID = "HLE_STOPPED_OID";
	@ScriptFieldAlias
	private static final String STOPPED_X = "HLE_STOPPED_X";
	@ScriptFieldAlias
	private static final String STOPPED_Y = "HLE_STOPPED_Y";
	@ScriptFieldAlias
	private static final String STOPPED_Z = "HLE_STOPPED_Z";
	
	private final Set<SimpleEventListener> _listeners;
	
	private final Map<L2GameClient, Set<Integer>> _effects;
	
	HighLevelEventGenerator()
	{
		_listeners = new CopyOnWriteArraySet<>();
		_effects = new FastMap<L2GameClient, Set<Integer>>().setShared(true);
	}
	
	/**
	 * Adds a basic listener that expects to handle high-level events.
	 * 
	 * @param listener a listener
	 */
	public void addListener(SimpleEventListener listener)
	{
		_listeners.add(listener);
	}
	
	/**
	 * Removes a basic listener to stop handling high-level events.
	 * 
	 * @param listener a listener
	 */
	public void removeListener(SimpleEventListener listener)
	{
		_listeners.remove(listener);
	}
	
	@Override
	public String getName()
	{
		return "High Level Event Generator";
	}
	
	@Override
	public void handleClientPacket(L2GameClient client, L2GameServer server, RandomAccessMMOBuffer buf) throws RuntimeException
	{
		// nothing to do here
	}
	
	@Override
	public void handleServerPacket(L2GameClient client, L2GameServer server, RandomAccessMMOBuffer buf) throws RuntimeException
	{
		onDeath:
		{
			final EnumeratedPayloadField oid = buf.getSingleFieldIndex(DECEASED_OID);
			if (oid == null)
				break onDeath;
			
			final int objectID = buf.readInteger32(oid);
			final boolean spoiled = buf.readFirstInteger32(DECEASED_SWEEPABLE) != 0;
			for (final SimpleEventListener listener : _listeners)
				listener.onDeath(client, objectID, spoiled);
			
			return;
		}
		onRevive:
		{
			final EnumeratedPayloadField oid = buf.getSingleFieldIndex(REVIVED_OID);
			if (oid == null)
				break onRevive;
			
			final int objectID = buf.readInteger32(oid);
			for (final SimpleEventListener listener : _listeners)
				listener.onRevive(client, objectID);
			
			return;
		}
		onDelete:
		{
			final EnumeratedPayloadField oid = buf.getSingleFieldIndex(DELETED_OID);
			if (oid == null)
				break onDelete;
			
			final int objectID = buf.readInteger32(oid);
			for (final SimpleEventListener listener : _listeners)
				listener.onDelete(client, objectID);
			
			return;
		}
		onUserTarget:
		{
			final EnumeratedPayloadField oid = buf.getSingleFieldIndex(USER_TARGET_OID);
			if (oid == null)
				break onUserTarget;
			
			final UserInfo ui = LiveUserAnalytics.getInstance().getUserInfo(client);
			if (ui == null)
				return;
			
			final int objectID = buf.readInteger32(oid);
			for (final SimpleEventListener listener : _listeners)
				listener.onTargetSelected(client, ui.getUserOID(), objectID);
			
			return;
		}
		onOtherTarget:
		{
			final EnumeratedPayloadField oid = buf.getSingleFieldIndex(TARGET_SETTER_OID);
			if (oid == null)
				break onOtherTarget;
			
			final int objectID = buf.readInteger32(oid);
			final int targetID = buf.readFirstInteger32(TARGET_OID);
			for (final SimpleEventListener listener : _listeners)
				listener.onTargetSelected(client, objectID, targetID);
			
			return;
		}
		onTargetCancel:
		{
			final EnumeratedPayloadField oid = buf.getSingleFieldIndex(TARGET_UNSETTER_OID);
			if (oid == null)
				break onTargetCancel;
			
			final int objectID = buf.readInteger32(oid);
			for (final SimpleEventListener listener : _listeners)
				listener.onTargetSelected(client, objectID, NO_TARGET);
			
			return;
		}
		onAttack:
		{
			final EnumeratedPayloadField oid = buf.getSingleFieldIndex(ATTACKER_OID);
			if (oid == null)
				break onAttack;
			
			final int[] ex;
			{
				final List<EnumeratedPayloadField> addTargets = buf.getFieldIndices(MULTI_TARGET_OID);
				ex = addTargets.isEmpty() ? ArrayUtils.EMPTY_INT_ARRAY : new int[addTargets.size()];
				for (int i = 0; i < ex.length; ++i)
					ex[i] = buf.readInteger32(addTargets.get(i));
			}
			
			final int objectID = buf.readInteger32(oid);
			final int targetID = buf.readFirstInteger32(TARGET_OID);
			for (final SimpleEventListener listener : _listeners)
			{
				listener.onPhysicalAttack(client, objectID, targetID);
				for (final int addTargetID : ex)
					listener.onPhysicalAttack(client, objectID, addTargetID);
			}
			
			return;
		}
		onCast:
		{
			final EnumeratedPayloadField oid = buf.getSingleFieldIndex(CASTER_OID);
			if (oid == null)
				break onCast;
			
			final int objectID = buf.readInteger32(oid);
			final int targetID = buf.readFirstInteger32(TARGET_OID);
			final int skillID = buf.readFirstInteger32(SKILL_ID);
			final int skillLvl = buf.readFirstInteger32(SKILL_LVL);
			final int castTime = buf.readFirstInteger32(CAST_TIME);
			final int coolTime = buf.readFirstInteger32(COOL_TIME);
			for (final SimpleEventListener listener : _listeners)
				listener.onCast(client, objectID, targetID, skillID, skillLvl, castTime, coolTime);
			
			return;
		}
		onCastCancel:
		{
			final EnumeratedPayloadField oid = buf.getSingleFieldIndex(CASTER_CANCEL_OID);
			if (oid == null)
				break onCastCancel;
			
			final int objectID = buf.readInteger32(oid);
			for (final SimpleEventListener listener : _listeners)
				listener.onCastFailure(client, objectID);
			
			return;
		}
		onCastFinished:
		{
			final EnumeratedPayloadField oid = buf.getSingleFieldIndex(CASTER_DONE_OID);
			if (oid == null)
				break onCastFinished;
			
			final List<Integer> targets;
			{
				final List<EnumeratedPayloadField> list = buf.getFieldIndices(MULTI_TARGET_OID);
				targets = list.isEmpty() ? Collections.emptyList() : new ArrayList<>(list.size());
				for (int i = 0; i < list.size(); ++i)
					targets.add(buf.readInteger32(list.get(i)));
			}
			
			final int objectID = buf.readInteger32(oid);
			final int skillID = buf.readFirstInteger32(SKILL_ID);
			final int skillLvl = buf.readFirstInteger32(SKILL_LVL);
			/*
			if (targets.length == 0)
			{
				for (final SimpleEventListener listener : _listeners)
					listener.onCastSuccess(client, objectID, NO_TARGET, skillID, skillLvl);
				
				return;
			}
			
			for (final SimpleEventListener listener : _listeners)
			{
				for (final int targetID : targets)
					listener.onCastSuccess(client, objectID, targetID, skillID, skillLvl);
			}
			*/
			for (final SimpleEventListener listener : _listeners)
				listener.onCastSuccess(client, objectID, targets, skillID, skillLvl);
			
			return;
		}
		onItemDropped:
		{
			final EnumeratedPayloadField oid = buf.getSingleFieldIndex(WORLD_ITEM_DROPPER_OID);
			if (oid == null)
				break onItemDropped;
			
			final int objectID = buf.readInteger32(oid);
			final int itemOID = buf.readFirstInteger32(WORLD_ITEM_OID);
			final long amount = buf.readFirstInteger(WORLD_ITEM_AMOUNT);
			for (final SimpleEventListener listener : _listeners)
				listener.onItemDrop(client, objectID, itemOID, amount);
			
			return;
		}
		onItemTaken:
		{
			final EnumeratedPayloadField oid = buf.getSingleFieldIndex(WORLD_ITEM_FINDER_OID);
			if (oid == null)
				break onItemTaken;
			
			final int objectID = buf.readInteger32(oid);
			final int itemOID = buf.readFirstInteger32(WORLD_ITEM_OID);
			for (final SimpleEventListener listener : _listeners)
				listener.onItemPickup(client, objectID, itemOID);
			
			return;
		}
		onItemInWorld:
		{
			final EnumeratedPayloadField oid = buf.getSingleFieldIndex(WORLD_ITEM_OID);
			if (oid == null)
				break onItemInWorld;
			
			final int objectID = buf.readInteger32(oid);
			final long amount = buf.readFirstInteger(WORLD_ITEM_AMOUNT);
			for (final SimpleEventListener listener : _listeners)
				listener.onItemSpawn(client, objectID, amount);
			
			return;
		}
		onEffectList:
		{
			final EnumeratedPayloadField cnt = buf.getSingleFieldIndex(EFFECT_SKILL_COUNT);
			if (cnt == null)
				break onEffectList;
			
			final Set<Integer> current = LiveUserAnalytics.getInstance().getUserAbnormals(client).getActiveModifiers().map(m -> m.getSkillID()).collect(Collectors.toSet());
			final Set<Integer> old = _effects.put(client, current);
			if (old == null)
				return;
			
			for (final Integer skill : old)
			{
				if (current.contains(skill))
					continue;
				
				for (final SimpleEventListener listener : _listeners)
					listener.onEffectRemoved(client, skill);
			}
			for (final Integer skill : current)
			{
				if (old.contains(skill))
					continue;
				
				for (final SimpleEventListener listener : _listeners)
					listener.onEffectAdded(client, skill);
			}
		}
		onStopMove:
		{
			final EnumeratedPayloadField oid = buf.getSingleFieldIndex(STOPPED_OID);
			if (oid == null)
				break onStopMove;
			
			final int objectID = buf.readInteger32(oid);
			final int x = buf.readFirstInteger32(STOPPED_X);
			final int y = buf.readFirstInteger32(STOPPED_Y);
			final int z = buf.readFirstInteger32(STOPPED_Z);
			for (final SimpleEventListener listener : _listeners)
				listener.onMovementEnd(client, objectID, x, y, z);
			
			return;
		}
	}
	
	@Override
	public double getPriority()
	{
		return -Double.longBitsToDouble(1L);
	}
	
	/**
	 * Returns a singleton instance of this type.
	 * 
	 * @return an instance of this class
	 */
	public static final HighLevelEventGenerator getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static final class SingletonHolder
	{
		static final HighLevelEventGenerator INSTANCE = new HighLevelEventGenerator();
	}
}
