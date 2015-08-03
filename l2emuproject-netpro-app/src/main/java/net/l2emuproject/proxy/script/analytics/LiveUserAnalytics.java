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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.TimeUnit;

import net.l2emuproject.lang.L2TextBuilder;
import net.l2emuproject.proxy.network.game.client.L2GameClient;
import net.l2emuproject.proxy.network.game.server.L2GameServer;
import net.l2emuproject.proxy.network.meta.EnumeratedPayloadField;
import net.l2emuproject.proxy.network.meta.RandomAccessMMOBuffer;
import net.l2emuproject.proxy.script.ScriptFieldAlias;
import net.l2emuproject.proxy.script.analytics.LiveUserAnalytics.UserInfo.EffectInfo;
import net.l2emuproject.proxy.script.game.PpeEnabledGameScript;
import net.l2emuproject.proxy.script.interpreter.L2SkillTranslator;
import net.l2emuproject.proxy.state.entity.ObjectInfo;
import net.l2emuproject.proxy.state.entity.ObjectLocation;
import net.l2emuproject.proxy.state.entity.cache.ObjectInfoCache;
import net.l2emuproject.proxy.state.entity.context.ICacheServerID;
import net.l2emuproject.util.ImmutableSortedArraySet;

/**
 * Manages a set of basic information about each connected player's currently active character.
 * 
 * @author _dev_
 */
public final class LiveUserAnalytics extends PpeEnabledGameScript
{
	@ScriptFieldAlias
	private static final String USER_OID = "LUA_USER_OID";
	@ScriptFieldAlias
	private static final String PET_OID = "LUA_USER_PET_OID";
	@ScriptFieldAlias
	private static final String PET_REMOVE_OID = "LUA_USER_REMOVED_PET_OID";
	@ScriptFieldAlias
	private static final String TARGET_OID = "LUA_TARGET_OID";
	@ScriptFieldAlias
	private static final String TARGET_CANCELER_OID = "LUA_CANCELER_OID";
	
	@ScriptFieldAlias
	private static final String USER_X = "LUA_CLIENT_X";
	@ScriptFieldAlias
	private static final String USER_Y = "LUA_CLIENT_Y";
	@ScriptFieldAlias
	private static final String USER_Z = "LUA_CLIENT_Z";
	@ScriptFieldAlias
	private static final String USER_HEADING = "LUA_CLIENT_HEADING";
	
	@ScriptFieldAlias
	private static final String USER_LEVEL = "LUA_USER_LEVEL";
	@ScriptFieldAlias
	private static final String USER_SP = "LUA_USER_SP";
	
	@ScriptFieldAlias
	private static final String EFFECT_COUNT = "LUA_SELF_EFFECT_COUNT";
	@ScriptFieldAlias
	private static final String EFFECT_SKILL = "LUA_SELF_EFFECT_SKILL_ID";
	@ScriptFieldAlias
	private static final String EFFECT_LEVEL = "LUA_SELF_EFFECT_SKILL_LVL";
	@ScriptFieldAlias
	private static final String EFFECT_TIME = "LUA_SELF_EFFECT_SECONDS_LEFT";
	
	@ScriptFieldAlias
	private static final String OWNED_SKILL_COUNT = "LUA_OWNED_SKILL_COUNT";
	@ScriptFieldAlias
	private static final String OWNED_SKILL = "LUA_OWNED_SKILL_ID";
	@ScriptFieldAlias
	private static final String OWNED_SKILL_DISABLED = "LUA_OWNED_SKILL_DISABLED";
	
	@ScriptFieldAlias
	private static final String LEARN_SKILL_ID = "LUA_LEARNABLE_SKILL_ID";
	@ScriptFieldAlias
	private static final String LEARN_SKILL_LVL = "LUA_LEARNABLE_SKILL_LEVEL";
	@ScriptFieldAlias
	private static final String LEARN_SKILL_REQ_SP = "LUA_LEARNABLE_SKILL_REQ_SP";
	@ScriptFieldAlias
	private static final String LEARN_SKILL_REQ_LVL = "LUA_LEARNABLE_SKILL_REQ_ACTIVE_LEVEL";
	@ScriptFieldAlias
	private static final String LEARN_SKILL_REQ_DUAL_LVL = "LUA_LEARNABLE_SKILL_REQ_DUAL_LEVEL";
	@ScriptFieldAlias
	private static final String LEARN_SKILL_REQ_ITEMS = "LUA_LEARNABLE_SKILL_REQ_ITEM_COUNT";
	@ScriptFieldAlias
	private static final String LEARN_SKILL_REQ_SKILLS = "LUA_LEARNABLE_SKILL_REQ_SKILL_COUNT";
	
	private final Map<L2GameClient, UserInfo> _liveUsers;
	
	LiveUserAnalytics()
	{
		_liveUsers = new ConcurrentHashMap<L2GameClient, UserInfo>();
	}
	
	/**
	 * Retrieves basic information about a connected player.<BR>
	 * <BR>
	 * If a player has not yet logged to the game world, or has already disconnected, returns {@code null}.
	 * 
	 * @param client game client connection endpoint
	 * @return user information or {@code null}
	 */
	public UserInfo getUserInfo(L2GameClient client)
	{
		return _liveUsers.get(client);
	}
	
	@Override
	public String getName()
	{
		return "LIVE User Analytics Script";
	}
	
	@Override
	public double getPriority()
	{
		return -Double.longBitsToDouble(0x7feffffffffffffdL);
	}
	
	@Override
	public void handleDisconnection(L2GameClient client)
	{
		_liveUsers.remove(client);
	}
	
	@Override
	public void handleClientPacket(L2GameClient client, L2GameServer server, RandomAccessMMOBuffer buf) throws RuntimeException
	{
		final int x = (int)buf.readFirstInteger(USER_X);
		final int y = (int)buf.readFirstInteger(USER_Y);
		final int z = (int)buf.readFirstInteger(USER_Z);
		final int yaw = (int)buf.readFirstInteger(USER_HEADING);
		
		final UserInfo ui = _liveUsers.get(client);
		if (ui == null)
			return;
			
		final ObjectInfo oi = ObjectInfoCache.getInstance().getOrAdd(ui._objectID, getEntityContext(server));
		oi.updateLocation(new ObjectLocation(x, y, z, yaw));
	}
	
	@Override
	public void handleServerPacket(L2GameClient client, L2GameServer server, RandomAccessMMOBuffer buf) throws RuntimeException
	{
		user:
		{
			// user playable character OID detection
			final EnumeratedPayloadField userOID = buf.getSingleFieldIndex(USER_OID);
			if (userOID == null)
				break user;
				
			final int objectID = buf.readInteger32(userOID);
			UserInfo ui = _liveUsers.get(client);
			if (ui == null || ui._objectID != objectID)
				_liveUsers.put(client, ui = new UserInfo(objectID, getEntityContext(server)));
				
			final EnumeratedPayloadField level = buf.getSingleFieldIndex(USER_LEVEL);
			if (level != null)
				ui._level = buf.readInteger32(level);
				
			final EnumeratedPayloadField sp = buf.getSingleFieldIndex(USER_SP);
			if (sp != null)
				ui._sp = buf.readInteger(sp);
				
			return;
		}
		
		final UserInfo ui = _liveUsers.get(client);
		if (ui == null)
			return;
			
		target:
		{
			// user target OID detection
			final EnumeratedPayloadField targetOID = buf.getSingleFieldIndex(TARGET_OID);
			if (targetOID == null)
				break target;
				
			ui._targetOID = buf.readInteger32(targetOID);
			return;
		}
		targetCancel:
		{
			// user target OID detection
			final EnumeratedPayloadField userOID = buf.getSingleFieldIndex(TARGET_CANCELER_OID);
			if (userOID == null)
				break targetCancel;
				
			final int objectID = buf.readInteger32(userOID);
			if (ui._objectID != objectID)
				return;
				
			ui._targetOID = NO_TARGET;
			return;
		}
		pet:
		{
			// user pet OID detection
			final EnumeratedPayloadField petOID = buf.getSingleFieldIndex(PET_OID);
			if (petOID == null)
				break pet;
				
			final int objectID = buf.readInteger32(petOID);
			ui.getServitorOIDs().add(objectID);
			return;
		}
		petRemoved:
		{
			// user pet OID detection
			final EnumeratedPayloadField petOID = buf.getSingleFieldIndex(PET_REMOVE_OID);
			if (petOID == null)
				break petRemoved;
				
			final int objectID = buf.readInteger32(petOID);
			ui.getServitorOIDs().remove(objectID);
			return;
		}
		allEffects:
		{
			// user abnormal effect detection
			if (buf.getSingleFieldIndex(EFFECT_COUNT) == null)
				break allEffects;
				
			final List<EnumeratedPayloadField> skills = buf.getFieldIndices(EFFECT_SKILL);
			final List<EnumeratedPayloadField> levels = buf.getFieldIndices(EFFECT_LEVEL);
			final List<EnumeratedPayloadField> times = buf.getFieldIndices(EFFECT_TIME);
			
			final long now = System.currentTimeMillis();
			final List<Effect> effects = new ArrayList<>(skills.size());
			final Integer[] effectSkills = new Integer[skills.size()];
			final Map<Integer, Effect> effectsBySkillID = new LinkedHashMap<>();
			for (int i = 0; i < skills.size(); ++i)
			{
				final int skill = buf.readInteger32(skills.get(i));
				effectSkills[i] = skill;
				final int level = buf.readInteger32(levels.get(i));
				final int time = buf.readInteger32(times.get(i));
				
				final Effect e = new Effect(skill, level, time != -1 ? now + time * 1_000 : Long.MAX_VALUE);
				effects.add(e);
				effectsBySkillID.putIfAbsent(skill, e);
			}
			
			ui._activeEffects = new EffectInfo(/*L2Collections.compactImmutableList*/(effects), ImmutableSortedArraySet.of(effectSkills), Collections.unmodifiableMap(effectsBySkillID));
		}
		allSkills:
		{
			if (buf.getSingleFieldIndex(OWNED_SKILL_COUNT) == null)
				break allSkills;
				
			final List<EnumeratedPayloadField> skills = buf.getFieldIndices(OWNED_SKILL);
			final List<EnumeratedPayloadField> states = buf.getFieldIndices(OWNED_SKILL_DISABLED);
			
			final Set<Integer> availableSkills = new HashSet<>();
			for (int i = 0; i < skills.size(); ++i)
			{
				final int skill = buf.readInteger32(skills.get(i));
				final boolean disabled = states.isEmpty() ? false : buf.readInteger32(states.get(i)) != 0;
				
				if (!disabled)
					availableSkills.add(skill);
			}
			
			ui._availableSkills = availableSkills;
		}
		learnableSkills:
		{
			final List<EnumeratedPayloadField> ids = buf.getFieldIndices(LEARN_SKILL_ID);
			if (ids.isEmpty())
				break learnableSkills;
				
			final List<EnumeratedPayloadField> lvls = buf.getFieldIndices(LEARN_SKILL_LVL), sps = buf.getFieldIndices(LEARN_SKILL_REQ_SP);
			final List<EnumeratedPayloadField> reqLvls = buf.getFieldIndices(LEARN_SKILL_REQ_LVL), reqDualLvls = buf.getFieldIndices(LEARN_SKILL_REQ_DUAL_LVL);
			final List<EnumeratedPayloadField> reqItemCnts = buf.getFieldIndices(LEARN_SKILL_REQ_ITEMS), reqSkillCnts = buf.getFieldIndices(LEARN_SKILL_REQ_SKILLS);
			
			final Map<Integer, LearnableSkill> skills = new HashMap<>();
			for (int i = 0; i < ids.size(); ++i)
			{
				if (buf.readInteger32(reqDualLvls.get(i)) > 0 || buf.readInteger32(reqItemCnts.get(i)) > 0 || buf.readInteger32(reqSkillCnts.get(i)) > 0)
					continue;
					
				final int id = buf.readInteger32(ids.get(i)), lvl = buf.readInteger32(lvls.get(i));
				final long sp = buf.readInteger(sps.get(i));
				final int reqLevel = buf.readInteger32(reqLvls.get(i));
				skills.put(id, new LearnableSkill(id, lvl, sp, reqLevel));
			}
			ui._learnableSkills = new LearnableSkills(skills);
		}
	}
	
	/**
	 * Stores basic information about the playable character.
	 * 
	 * @author _dev_
	 */
	public static final class UserInfo
	{
		final int _objectID;
		final ICacheServerID _context; // for debugging toString only
		volatile int _targetOID;
		final Set<Integer> _servitorOIDs;
		volatile int _level;
		volatile long _sp;
		volatile LearnableSkills _learnableSkills;
		/** Non-disabled active & passive skills */
		volatile Set<Integer> _availableSkills;
		volatile EffectInfo _activeEffects;
		
		UserInfo(int objectID, ICacheServerID context)
		{
			_objectID = objectID;
			_context = context;
			_targetOID = NO_TARGET;
			_servitorOIDs = new CopyOnWriteArraySet<>();
			_level = 1;
			_sp = 0;
			_learnableSkills = new LearnableSkills(Collections.emptyMap());
			_availableSkills = Collections.emptySet();
			_activeEffects = new EffectInfo(Collections.emptyList(), Collections.emptySet(), Collections.emptyMap());
		}
		
		/**
		 * Returns the associated PC's world object ID.
		 * 
		 * @return own world object ID
		 */
		public int getUserOID()
		{
			return _objectID;
		}
		
		/**
		 * Returns the associated PC's targeted world object ID. If no target is currently selected, returns {@value SimpleEventListener#NO_TARGET}.
		 * 
		 * @return target world object ID or {@value SimpleEventListener#NO_TARGET}
		 */
		public int getTargetOID()
		{
			return _targetOID;
		}
		
		/**
		 * Returns the associated PC's currently active servitor world object IDs. Returns an empty set if the player has neither pets nor summons currently active.
		 * 
		 * @return servitor world object IDs
		 */
		public Set<Integer> getServitorOIDs()
		{
			return _servitorOIDs;
		}
		
		/**
		 * Returns IDs of skills that can be used by the associated PC in the current context.
		 * This will include skills currently on cooldown.
		 * 
		 * @return available skills
		 */
		public Set<Integer> getEnabledSkills()
		{
			return _availableSkills;
		}
		
		/**
		 * Returns effects, currently active on the associated PC.
		 * 
		 * @return active effects
		 */
		public List<Effect> getEffects()
		{
			return _activeEffects._effects;
		}
		
		/**
		 * Returns skill IDs associated with effects, currently active on the associated PC.
		 * 
		 * @return active effect skill IDs
		 */
		public Set<Integer> getEffectSkillIDs()
		{
			return _activeEffects._effectSkillIDs;
		}
		
		/**
		 * Returns effects, currently active on the associated PC, mapped to skill IDs.
		 * 
		 * @return active effect to skill mapping
		 */
		public Map<Integer, Effect> getEffectsBySkillID()
		{
			return _activeEffects._effectsBySkillID;
		}
		
		@Override
		public int hashCode()
		{
			return 31 + Integer.hashCode(_objectID);
		}
		
		@Override
		public boolean equals(Object obj)
		{
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			UserInfo other = (UserInfo)obj;
			if (_objectID != other._objectID)
				return false;
			return true;
		}
		
		@Override
		public String toString()
		{
			final int target = _targetOID;
			final ObjectInfoCache cache = ObjectInfoCache.getInstance();
			final ObjectInfo info = cache.getOrAdd(_objectID, _context), targetInfo = target != NO_TARGET ? cache.getOrAdd(target, _context) : null;
			
			final L2TextBuilder tb = new L2TextBuilder(info.getName()).append(info.getCurrentLocation());
			if (targetInfo != null)
				tb.append("; target: ").append(targetInfo.getName()).append(targetInfo.getCurrentLocation());
			if (!_servitorOIDs.isEmpty())
				tb.append("; ").append(_servitorOIDs.size()).append(" servitors");
			if (!_activeEffects._effects.isEmpty())
				tb.append("; ").append(_activeEffects._effects.size()).append(" effects");
			if (!_availableSkills.isEmpty())
				tb.append("; ").append(_availableSkills.size()).append(" enabled skills");
			return tb.moveToString();
		}
		
		static final class EffectInfo
		{
			/** All effects as last sent by server (may be out of date due to latency) */
			final List<Effect> _effects;
			/**
			 * All unique effects as last sent by server (may be out of date due to latency).
			 * Used for improved performance due to client limitations preventing abuse.
			 */
			final Set<Integer> _effectSkillIDs;
			final Map<Integer, Effect> _effectsBySkillID;
			
			EffectInfo(List<Effect> effects, Set<Integer> effectSkillIDs, Map<Integer, Effect> effectsBySkillID)
			{
				_effects = effects;
				_effectSkillIDs = effectSkillIDs;
				_effectsBySkillID = effectsBySkillID;
			}
			
			@Override
			public String toString()
			{
				return _effects.toString();
			}
		}
	}
	
	/**
	 * Stores basic information about an active effect.
	 * 
	 * @author _dev_
	 */
	public static final class Effect
	{
		private final int _skill, _level;
		private final long _expiry;
		
		Effect(int skill, int level, long expiry)
		{
			_skill = skill;
			_level = level;
			_expiry = expiry;
		}
		
		/**
		 * Returns the associated skill's ID.
		 * 
		 * @return skill ID
		 */
		public int getSkill()
		{
			return _skill;
		}
		
		/**
		 * Returns the associated skill's level.
		 * 
		 * @return skill level
		 */
		public int getLevel()
		{
			return _level;
		}
		
		/**
		 * Returns this effect's automatic expiration timestamp.
		 * 
		 * @return expiration time
		 */
		public long getExpiry()
		{
			return _expiry;
		}
		
		/**
		 * Returns the amount of time left until this effect will automatically expire.
		 * 
		 * @param unit returned amount time unit
		 * @return time left to expiration
		 */
		public long getTimeLeft(TimeUnit unit)
		{
			final long diff = _expiry - System.currentTimeMillis();
			if (diff <= 0)
				return 0;
				
			return unit.convert(diff, TimeUnit.MILLISECONDS);
		}
		
		@Override
		public String toString()
		{
			final long seconds = getTimeLeft(TimeUnit.SECONDS);
			return "[" + (seconds / 60) + ":" + (seconds % 60) + "]" + L2SkillTranslator.getInterpretation(_skill, _level);
		}
	}
	
	public static final class LearnableSkill
	{
		final int _skill, _level, _requiredCharacterLevel;
		final long _sp;
		
		LearnableSkill(int skill, int level, long sp, int requiredCharacterLevel)
		{
			_skill = skill;
			_level = level;
			_sp = sp;
			_requiredCharacterLevel = requiredCharacterLevel;
		}
		
		public int getSkill()
		{
			return _skill;
		}
		
		public int getLevel()
		{
			return _level;
		}
		
		public long getSp()
		{
			return _sp;
		}
		
		public int getRequiredCharacterLevel()
		{
			return _requiredCharacterLevel;
		}
		
		@Override
		public String toString()
		{
			return "[" + _requiredCharacterLevel + "]" + L2SkillTranslator.getInterpretation(_skill, _level) + " " + _sp + "SP";
		}
	}
	
	public static final class LearnableSkills
	{
		private final Map<Integer, LearnableSkill> _skills;
		
		LearnableSkills(Map<Integer, LearnableSkill> skills)
		{
			_skills = skills;
		}
		
		public LearnableSkill getLearnInfo(int skillID)
		{
			return _skills.get(skillID);
		}
		
		@Override
		public String toString()
		{
			return _skills.values().toString();
		}
	}
	
	/**
	 * Returns a singleton instance of this type.
	 * 
	 * @return an instance of this class
	 */
	public static final LiveUserAnalytics getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static final class SingletonHolder
	{
		static final LiveUserAnalytics INSTANCE = new LiveUserAnalytics();
	}
}
