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
package net.l2emuproject.proxy.script.analytics.user;

import static net.l2emuproject.proxy.script.analytics.SimpleEventListener.NO_TARGET;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.ArrayUtils;

import net.l2emuproject.lang.L2TextBuilder;
import net.l2emuproject.proxy.network.ForwardedNotificationManager;
import net.l2emuproject.proxy.network.game.client.L2GameClient;
import net.l2emuproject.proxy.network.game.server.L2GameServer;
import net.l2emuproject.proxy.network.meta.EnumeratedPayloadField;
import net.l2emuproject.proxy.network.meta.RandomAccessMMOBuffer;
import net.l2emuproject.proxy.script.ScriptFieldAlias;
import net.l2emuproject.proxy.script.analytics.SimpleEventListener;
import net.l2emuproject.proxy.script.analytics.user.impl.ActorSkillCast;
import net.l2emuproject.proxy.script.analytics.user.impl.InventoryItem;
import net.l2emuproject.proxy.script.analytics.user.impl.ItemAugmentationImpl;
import net.l2emuproject.proxy.script.analytics.user.impl.ItemEnchantEffectsImpl;
import net.l2emuproject.proxy.script.analytics.user.impl.ItemSpecialAbilitiesImpl;
import net.l2emuproject.proxy.script.analytics.user.impl.UserAbnormalStatusModifier;
import net.l2emuproject.proxy.script.analytics.user.impl.UserAbnormals;
import net.l2emuproject.proxy.script.analytics.user.impl.UserInventory;
import net.l2emuproject.proxy.script.analytics.user.impl.UserRelation;
import net.l2emuproject.proxy.script.analytics.user.impl.UserSkill;
import net.l2emuproject.proxy.script.analytics.user.impl.UserSkillCoolTime;
import net.l2emuproject.proxy.script.analytics.user.impl.UserSkillList;
import net.l2emuproject.proxy.script.analytics.user.impl.UserVisibleWorldObjects;
import net.l2emuproject.proxy.script.game.PpeEnabledGameScript;
import net.l2emuproject.proxy.script.interpreter.L2SkillTranslator;
import net.l2emuproject.proxy.script.packets.util.RestartResponseRecipient;
import net.l2emuproject.proxy.state.entity.L2ObjectInfo;
import net.l2emuproject.proxy.state.entity.L2ObjectInfoCache;
import net.l2emuproject.proxy.state.entity.ObjectInfo;
import net.l2emuproject.proxy.state.entity.context.ICacheServerID;
import net.l2emuproject.util.BitMaskUtils;

/**
 * Manages a set of basic information about each connected player's currently active character.
 * 
 * @author _dev_
 */
public final class LiveUserAnalytics extends PpeEnabledGameScript implements RestartResponseRecipient
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
	private static final String USER_WIDTH = "LUA_USER_W";
	@ScriptFieldAlias
	private static final String USER_HEIGHT = "LUA_USER_H";
	@ScriptFieldAlias
	private static final String USER_PERSONAL_STORE = "LUA_USER_STORE_TYPE";
	
	@ScriptFieldAlias
	private static final String USER_LEVEL = "LUA_USER_LEVEL";
	@ScriptFieldAlias
	private static final String USER_SP = "LUA_USER_SP";
	
	@ScriptFieldAlias
	private static final String WAIT_TYPE_ACTOR = "cwt_actor_oid";
	@ScriptFieldAlias
	private static final String WAIT_TYPE_SETTING = "cwt_new_wait_type";
	
	@ScriptFieldAlias
	private static final String EFFECT_COUNT = "LUA_SELF_EFFECT_COUNT";
	@ScriptFieldAlias
	private static final String EFFECT_SKILL = "LUA_SELF_EFFECT_SKILL_ID";
	@ScriptFieldAlias
	private static final String EFFECT_LEVEL = "LUA_SELF_EFFECT_SKILL_LVL";
	@ScriptFieldAlias
	private static final String EFFECT_TIME = "LUA_SELF_EFFECT_SECONDS_LEFT";
	@ScriptFieldAlias
	private static final String EFFECT_TIME_EX = "LUA_SELF_EFFECT_SECONDS_LEFT_EX";
	
	@ScriptFieldAlias
	private static final String OWNED_SKILL_COUNT = "LUA_OWNED_SKILL_COUNT";
	@ScriptFieldAlias
	private static final String OWNED_SKILL_PASSIVE = "LUA_OWNED_SKILL_PASSIVE";
	@ScriptFieldAlias
	private static final String OWNED_SKILL_LEVEL_SUBLEVEL = "LUA_OWNED_SKILL_LEVEL";
	@ScriptFieldAlias
	private static final String OWNED_SKILL = "LUA_OWNED_SKILL_ID";
	@ScriptFieldAlias
	private static final String OWNED_SKILL_DISABLED = "LUA_OWNED_SKILL_DISABLED";
	@ScriptFieldAlias
	private static final String OWNED_SKILL_ENCHANTABLE = "LUA_OWNED_SKILL_ENCHANTABLE";
	
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
	
	@ScriptFieldAlias
	private static final String INVENTORY_ITEM_EXTENSIONS = "LUA_IL_EX";
	@ScriptFieldAlias
	private static final String INVENTORY_ITEM_OID = "LUA_IL_OID";
	@ScriptFieldAlias
	private static final String INVENTORY_ITEM_TEMPLATE = "LUA_IL_TEMPLATE";
	@ScriptFieldAlias
	private static final String INVENTORY_ITEM_AMOUNT = "LUA_IL_AMOUNT";
	@ScriptFieldAlias
	private static final String INVENTORY_ITEM_ENCHANT = "LUA_IL_ENCHANT_LEVEL";
	@ScriptFieldAlias
	private static final String INVENTORY_ITEM_AUG_EFFECT_1 = "LUA_IL_AUG1";
	@ScriptFieldAlias
	private static final String INVENTORY_ITEM_AUG_EFFECT_2 = "LUA_IL_AUG2";
	@ScriptFieldAlias
	private static final String INVENTORY_ITEM_ENC_EFFECT_1 = "LUA_IL_ENC1";
	@ScriptFieldAlias
	private static final String INVENTORY_ITEM_ENC_EFFECT_2 = "LUA_IL_ENC2";
	@ScriptFieldAlias
	private static final String INVENTORY_ITEM_ENC_EFFECT_3 = "LUA_IL_ENC3";
	@ScriptFieldAlias
	private static final String INVENTORY_ITEM_APPEARANCE = "LUA_IL_APPEARANCE";
	@ScriptFieldAlias
	private static final String INVENTORY_ITEM_SA1_CNT = "LUA_IL_SA1_COUNT";
	@ScriptFieldAlias
	private static final String INVENTORY_ITEM_SA1 = "LUA_IL_SA1";
	@ScriptFieldAlias
	private static final String INVENTORY_ITEM_SA2_CNT = "LUA_IL_SA2_COUNT";
	@ScriptFieldAlias
	private static final String INVENTORY_ITEM_SA2 = "LUA_IL_SA2";
	
	@ScriptFieldAlias
	private static final String INVENTORY_UPDATE_ITEM_CHANGE_TYPE = "LUA_IU_CHANGE";
	@ScriptFieldAlias
	private static final String INVENTORY_UPDATE_ITEM_EXTENSIONS = "LUA_IU_EX";
	@ScriptFieldAlias
	private static final String INVENTORY_UPDATE_ITEM_OID = "LUA_IU_OID";
	@ScriptFieldAlias
	private static final String INVENTORY_UPDATE_ITEM_TEMPLATE = "LUA_IU_TEMPLATE";
	@ScriptFieldAlias
	private static final String INVENTORY_UPDATE_ITEM_AMOUNT = "LUA_IU_AMOUNT";
	@ScriptFieldAlias
	private static final String INVENTORY_UPDATE_ITEM_ENCHANT = "LUA_IU_ENCHANT_LEVEL";
	@ScriptFieldAlias
	private static final String INVENTORY_UPDATE_ITEM_AUG_EFFECT_1 = "LUA_IU_AUG1";
	@ScriptFieldAlias
	private static final String INVENTORY_UPDATE_ITEM_AUG_EFFECT_2 = "LUA_IU_AUG2";
	@ScriptFieldAlias
	private static final String INVENTORY_UPDATE_ITEM_ENC_EFFECT_1 = "LUA_IU_ENC1";
	@ScriptFieldAlias
	private static final String INVENTORY_UPDATE_ITEM_ENC_EFFECT_2 = "LUA_IU_ENC2";
	@ScriptFieldAlias
	private static final String INVENTORY_UPDATE_ITEM_ENC_EFFECT_3 = "LUA_IU_ENC3";
	@ScriptFieldAlias
	private static final String INVENTORY_UPDATE_ITEM_APPEARANCE = "LUA_IU_APPEARANCE";
	@ScriptFieldAlias
	private static final String INVENTORY_UPDATE_ITEM_SA1_CNT = "LUA_IU_SA1_COUNT";
	@ScriptFieldAlias
	private static final String INVENTORY_UPDATE_ITEM_SA1 = "LUA_IU_SA1";
	@ScriptFieldAlias
	private static final String INVENTORY_UPDATE_ITEM_SA2_CNT = "LUA_IU_SA2_COUNT";
	@ScriptFieldAlias
	private static final String INVENTORY_UPDATE_ITEM_SA2 = "LUA_IU_SA2";
	
	@ScriptFieldAlias
	private static final String MSU_CASTER_OID = "LUA_CASTER_OID";
	@ScriptFieldAlias
	private static final String MSU_SKILL_ID = "LUA_USED_SKILL_ID";
	@ScriptFieldAlias
	private static final String MSU_SKILL_JOINT_LEVEL = "LUA_USED_SKILL_LEVEL";
	@ScriptFieldAlias
	private static final String MSU_SKILL_TIME = "LUA_USED_SKILL_TOTAL_TIME";
	@ScriptFieldAlias
	private static final String MSU_SKILL_COOL_TIME = "LUA_USED_SKILL_COOL_TIME";
	@ScriptFieldAlias
	private static final String MSU_CASTER_X = "LUA_CASTER_LOC_X";
	@ScriptFieldAlias
	private static final String MSU_CASTER_Y = "LUA_CASTER_LOC_Y";
	@ScriptFieldAlias
	private static final String MSU_CASTER_Z = "LUA_CASTER_LOC_Z";
	
	@ScriptFieldAlias
	private static final String CI_OID = "ci_player_oid";
	@ScriptFieldAlias
	private static final String NI_OID = "OIC_NPC_OID";
	@ScriptFieldAlias
	private static final String PI_OID = "OIC_PET_OID";
	@ScriptFieldAlias
	private static final String SI_OID = "OIC_SUMMON_OID";
	@ScriptFieldAlias
	private static final String SOI_OID = "OIC_SO_OID";
	@ScriptFieldAlias
	private static final String ITEM_OID = "OIC_WORLD_ITEM_OID";
	private static final String[] VISIBLE_OID = { CI_OID, NI_OID, PI_OID, SI_OID, SOI_OID, ITEM_OID
	};
	
	@ScriptFieldAlias
	private static final String RC_OID = "rc_actor_oid";
	@ScriptFieldAlias
	private static final String RC_RELATION = "rc_relation";
	@ScriptFieldAlias
	private static final String RC_ATTACKABLE = "rc_attackable";
	@ScriptFieldAlias
	private static final String RC_REPUTATION = "rc_reputation";
	@ScriptFieldAlias
	private static final String RC_PVP = "rc_pvp";
	
	private static final String RESTART_DISCARDABLE_PREFIX = "user_bound_";
	private static final String USER_INFO_KEY = RESTART_DISCARDABLE_PREFIX + "user_info";
	private static final String USER_INVENTORY_KEY = RESTART_DISCARDABLE_PREFIX + "user_inv";
	private static final String USER_SKILL_KEY = RESTART_DISCARDABLE_PREFIX + "user_sl";
	private static final String USER_ABNORMAL_MODIFIER_KEY = RESTART_DISCARDABLE_PREFIX + "user_asm";
	private static final String USER_VISIBLE_OBJECTS_KEY = RESTART_DISCARDABLE_PREFIX + "user_vwo";
	
	LiveUserAnalytics()
	{
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
		return get(client, USER_INFO_KEY);
	}
	
	/**
	 * Retrieves item list of a connected player.<BR>
	 * <BR>
	 * If a player has not yet logged to the game world, or has already disconnected, returns {@code null}.
	 * 
	 * @param client game client connection endpoint
	 * @return user inventory or {@code null}
	 */
	public UserInventory getUserInventory(L2GameClient client)
	{
		return get(client, USER_INVENTORY_KEY);
	}
	
	/**
	 * Retrieves skill list of a connected player.<BR>
	 * <BR>
	 * If a player has not yet logged to the game world, or has already disconnected, returns {@code null}.
	 * 
	 * @param client game client connection endpoint
	 * @return user skills or {@code null}
	 */
	public UserSkillList getUserSkillList(L2GameClient client)
	{
		return get(client, USER_SKILL_KEY);
	}
	
	/**
	 * Retrieves effect list of a connected player.<BR>
	 * <BR>
	 * If a player has not yet logged to the game world, or has already disconnected, returns {@code null}.
	 * 
	 * @param client game client connection endpoint
	 * @return user skills or {@code null}
	 */
	public UserAbnormals getUserAbnormals(L2GameClient client)
	{
		return get(client, USER_ABNORMAL_MODIFIER_KEY);
	}
	
	/**
	 * Retrieves a list of all objects currently visible to a connected player.
	 * 
	 * @param client game client connection endpoint
	 * @return visible objects or {@code null}
	 */
	public UserVisibleWorldObjects getUserVisibleWorldObjects(L2GameClient client)
	{
		return get(client, USER_VISIBLE_OBJECTS_KEY);
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
	public void handleClientPacket(L2GameClient client, L2GameServer server, RandomAccessMMOBuffer buf) throws RuntimeException
	{
		// nothing expected from client
	}
	
	@Override
	public void handleServerPacket(L2GameClient client, L2GameServer server, RandomAccessMMOBuffer buf) throws RuntimeException
	{
		if (isReturningToLobby(buf))
		{
			ForwardedNotificationManager.getInstance().discardSessionStateByKey(k -> String.valueOf(k).startsWith(RESTART_DISCARDABLE_PREFIX));
			return;
		}
		
		for (final String visibleOID : VISIBLE_OID)
		{
			final EnumeratedPayloadField field = buf.getSingleFieldIndex(visibleOID);
			if (field != null)
				computeIfAbsent(client, USER_VISIBLE_OBJECTS_KEY, k -> new UserVisibleWorldObjects(getEntityContext(client))).add(buf.readInteger32(field));
		}
		
		relation:
		{
			final List<EnumeratedPayloadField> relations = buf.getFieldIndices(RC_RELATION);
			if (relations.isEmpty())
				break relation;
			
			final UserVisibleWorldObjects visibles = computeIfAbsent(client, USER_VISIBLE_OBJECTS_KEY, k -> new UserVisibleWorldObjects(getEntityContext(client)));
			final List<EnumeratedPayloadField> actors = buf.getFieldIndices(RC_OID), atks = buf.getFieldIndices(RC_ATTACKABLE);
			final List<EnumeratedPayloadField> reputations = buf.getFieldIndices(RC_REPUTATION), pvps = buf.getFieldIndices(RC_PVP);
			for (int i = 0; i < relations.size(); ++i)
			{
				final int playerOID = buf.readInteger32(actors.get(i));
				final int relation = buf.readInteger32(relations.get(i));
				final boolean attackable = buf.readInteger32(atks.get(i)) != 0;
				final int reputation = buf.readInteger32(reputations.get(i));
				final int pvp = buf.readInteger32(pvps.get(i));
				visibles.setCurrentRelation(playerOID, new UserRelation(playerOID, relation, attackable, reputation, pvp));
			}
			return;
		}
		inventory:
		{
			final List<EnumeratedPayloadField> exts = buf.getFieldIndices(INVENTORY_ITEM_EXTENSIONS);
			if (exts.isEmpty())
				break inventory;
			
			final List<EnumeratedPayloadField> oids = buf.getFieldIndices(INVENTORY_ITEM_OID), templates = buf.getFieldIndices(INVENTORY_ITEM_TEMPLATE),
					amounts = buf.getFieldIndices(INVENTORY_ITEM_AMOUNT), enchants = buf.getFieldIndices(INVENTORY_ITEM_ENCHANT),
					aug1s = buf.getFieldIndices(INVENTORY_ITEM_AUG_EFFECT_1), aug2s = buf.getFieldIndices(INVENTORY_ITEM_AUG_EFFECT_2),
					enc1s = buf.getFieldIndices(INVENTORY_ITEM_ENC_EFFECT_1), enc2s = buf.getFieldIndices(INVENTORY_ITEM_ENC_EFFECT_2),
					enc3s = buf.getFieldIndices(INVENTORY_ITEM_ENC_EFFECT_3), apps = buf.getFieldIndices(INVENTORY_ITEM_APPEARANCE),
					sa1cnts = buf.getFieldIndices(INVENTORY_ITEM_SA1_CNT), sa1s = buf.getFieldIndices(INVENTORY_ITEM_SA1),
					sa2cnts = buf.getFieldIndices(INVENTORY_ITEM_SA2_CNT), sa2s = buf.getFieldIndices(INVENTORY_ITEM_SA2);
			
			int enchantIndex = 0, augIndex = -1, encEffectIndex = -1, appIndex = 0, saCntIndex = -1, sa1Index = -1, sa2Index = -1;
			final List<InventoryItem> inventoryItems = new ArrayList<>(exts.size());
			for (int i = 0; i < exts.size(); ++i)
			{
				final Set<ItemExtension> ex = BitMaskUtils.setOf(buf.readInteger32(exts.get(i)), ItemExtension.class);
				final int objectID = buf.readInteger32(oids.get(i));
				final int templateID = buf.readInteger32(templates.get(i));
				final long amount = buf.readInteger(amounts.get(i));
				// extract enchant level here
				final int encLvl;
				if (enchantIndex < enchants.size())
				{
					int nextItemOffsetFromEnd = 0;
					if (i + 1 < exts.size())
						nextItemOffsetFromEnd = buf.seekField(exts.get(i + 1)).getAvailableBytes();
					if (buf.seekField(enchants.get(enchantIndex)).getAvailableBytes() > nextItemOffsetFromEnd)
						encLvl = buf.readInteger32(enchants.get(enchantIndex++));
					else
						encLvl = 0;
				}
				else
					encLvl = 0;
				final ItemAugmentation augmentation = ex.contains(ItemExtension.AUGMENTATION)
						? new ItemAugmentationImpl(buf.readInteger32(aug1s.get(++augIndex)), buf.readInteger32(aug2s.get(augIndex))) : ItemAugmentation.NO_AUGMENTATION;
				final ItemEnchantEffects encEff = ex.contains(ItemExtension.ENCHANT_EFFECT)
						? new ItemEnchantEffectsImpl(buf.readInteger32(enc1s.get(++encEffectIndex)), buf.readInteger32(enc2s.get(encEffectIndex)), buf.readInteger32(enc3s.get(encEffectIndex)))
						: ItemEnchantEffects.NO_EFFECTS;
				final int appearance;
				if (ex.contains(ItemExtension.APPEARANCE) && appIndex < apps.size())
				{
					int nextItemOffsetFromEnd = 0;
					if (i + 1 < exts.size())
						nextItemOffsetFromEnd = buf.seekField(exts.get(i + 1)).getAvailableBytes();
					if (buf.seekField(apps.get(appIndex)).getAvailableBytes() > nextItemOffsetFromEnd)
						appearance = buf.readInteger32(apps.get(appIndex++));
					else
						appearance = 0;
				}
				else
					appearance = 0;
				final ItemSpecialAbilities sa;
				if (ex.contains(ItemExtension.SPECIAL_ABILITIES))
				{
					final int sa1Count = buf.readInteger32(sa1cnts.get(++saCntIndex)), sa2Count = buf.readInteger32(sa2cnts.get(saCntIndex));
					final int[] sa1 = sa1Count > 0 ? new int[sa1Count] : ArrayUtils.EMPTY_INT_ARRAY, sa2 = sa2Count > 0 ? new int[sa2Count] : ArrayUtils.EMPTY_INT_ARRAY;
					for (int j = 0; j < sa1.length; ++j)
						sa1[j] = buf.readInteger32(sa1s.get(++sa1Index));
					for (int j = 0; j < sa2.length; ++j)
						sa2[j] = buf.readInteger32(sa2s.get(++sa2Index));
					sa = new ItemSpecialAbilitiesImpl(sa1, sa2);
				}
				else
					sa = ItemSpecialAbilities.NO_SPECIAL_ABILITY;
				inventoryItems.add(new InventoryItem(objectID, templateID, amount, encLvl, augmentation, ItemElementalAttributes.NO_ATTRIBUTES, encEff, appearance, sa));
			}
			computeIfAbsent(client, USER_INVENTORY_KEY, k -> new UserInventory()).setInventory(inventoryItems);
		}
		inventoryUpdate:
		{
			final List<EnumeratedPayloadField> changes = buf.getFieldIndices(INVENTORY_UPDATE_ITEM_CHANGE_TYPE), exts = buf.getFieldIndices(INVENTORY_UPDATE_ITEM_EXTENSIONS);
			if (exts.isEmpty())
				break inventoryUpdate;
			
			final List<EnumeratedPayloadField> oids = buf.getFieldIndices(INVENTORY_UPDATE_ITEM_OID), templates = buf.getFieldIndices(INVENTORY_UPDATE_ITEM_TEMPLATE),
					amounts = buf.getFieldIndices(INVENTORY_UPDATE_ITEM_AMOUNT), enchants = buf.getFieldIndices(INVENTORY_UPDATE_ITEM_ENCHANT),
					aug1s = buf.getFieldIndices(INVENTORY_UPDATE_ITEM_AUG_EFFECT_1), aug2s = buf.getFieldIndices(INVENTORY_UPDATE_ITEM_AUG_EFFECT_2),
					enc1s = buf.getFieldIndices(INVENTORY_UPDATE_ITEM_ENC_EFFECT_1), enc2s = buf.getFieldIndices(INVENTORY_UPDATE_ITEM_ENC_EFFECT_2),
					enc3s = buf.getFieldIndices(INVENTORY_UPDATE_ITEM_ENC_EFFECT_3), apps = buf.getFieldIndices(INVENTORY_UPDATE_ITEM_APPEARANCE),
					sa1cnts = buf.getFieldIndices(INVENTORY_UPDATE_ITEM_SA1_CNT), sa1s = buf.getFieldIndices(INVENTORY_UPDATE_ITEM_SA1),
					sa2cnts = buf.getFieldIndices(INVENTORY_UPDATE_ITEM_SA2_CNT), sa2s = buf.getFieldIndices(INVENTORY_UPDATE_ITEM_SA2);
			
			int enchantIndex = 0, augIndex = -1, encEffectIndex = -1, appIndex = 0, saCntIndex = -1, sa1Index = -1, sa2Index = -1;
			for (int i = 0; i < exts.size(); ++i)
			{
				final Set<ItemExtension> ex = BitMaskUtils.setOf(buf.readInteger32(exts.get(i)), ItemExtension.class);
				final int objectID = buf.readInteger32(oids.get(i));
				final int templateID = buf.readInteger32(templates.get(i));
				final long amount = buf.readInteger(amounts.get(i));
				// extract enchant level here
				final int encLvl;
				if (enchantIndex < enchants.size())
				{
					int nextItemOffsetFromEnd = 0;
					if (i + 1 < exts.size())
						nextItemOffsetFromEnd = buf.seekField(exts.get(i + 1)).getAvailableBytes();
					if (buf.seekField(enchants.get(enchantIndex)).getAvailableBytes() > nextItemOffsetFromEnd)
						encLvl = buf.readInteger32(enchants.get(enchantIndex++));
					else
						encLvl = 0;
				}
				else
					encLvl = 0;
				final ItemAugmentation augmentation = ex.contains(ItemExtension.AUGMENTATION)
						? new ItemAugmentationImpl(buf.readInteger32(aug1s.get(++augIndex)), buf.readInteger32(aug2s.get(augIndex))) : ItemAugmentation.NO_AUGMENTATION;
				final ItemEnchantEffects encEff = ex.contains(ItemExtension.ENCHANT_EFFECT)
						? new ItemEnchantEffectsImpl(buf.readInteger32(enc1s.get(++encEffectIndex)), buf.readInteger32(enc2s.get(encEffectIndex)), buf.readInteger32(enc3s.get(encEffectIndex)))
						: ItemEnchantEffects.NO_EFFECTS;
				final int appearance;
				if (ex.contains(ItemExtension.APPEARANCE) && appIndex < apps.size())
				{
					int nextItemOffsetFromEnd = 0;
					if (i + 1 < exts.size())
						nextItemOffsetFromEnd = buf.seekField(exts.get(i + 1)).getAvailableBytes();
					if (buf.seekField(apps.get(appIndex)).getAvailableBytes() > nextItemOffsetFromEnd)
						appearance = buf.readInteger32(apps.get(appIndex++));
					else
						appearance = 0;
				}
				else
					appearance = 0;
				final ItemSpecialAbilities sa;
				if (ex.contains(ItemExtension.SPECIAL_ABILITIES))
				{
					final int sa1Count = buf.readInteger32(sa1cnts.get(++saCntIndex)), sa2Count = buf.readInteger32(sa2cnts.get(saCntIndex));
					final int[] sa1 = sa1Count > 0 ? new int[sa1Count] : ArrayUtils.EMPTY_INT_ARRAY, sa2 = sa2Count > 0 ? new int[sa2Count] : ArrayUtils.EMPTY_INT_ARRAY;
					for (int j = 0; j < sa1.length; ++j)
						sa1[j] = buf.readInteger32(sa1s.get(++sa1Index));
					for (int j = 0; j < sa2.length; ++j)
						sa2[j] = buf.readInteger32(sa2s.get(++sa2Index));
					sa = new ItemSpecialAbilitiesImpl(sa1, sa2);
				}
				else
					sa = ItemSpecialAbilities.NO_SPECIAL_ABILITY;
				
				final InventoryItem item = new InventoryItem(objectID, templateID, amount, encLvl, augmentation, ItemElementalAttributes.NO_ATTRIBUTES, encEff, appearance, sa);
				final int change = buf.readInteger32(changes.get(i));
				try
				{
					final UserInventory inv = computeIfAbsent(client, USER_INVENTORY_KEY, k -> new UserInventory());
					switch (change)
					{
						case 1: // add
							inv.add(item);
							break;
						case 2: // update
							inv.update(item);
							break;
						case 3: // remove
							inv.remove(item);
							break;
						default:
							throw new IllegalArgumentException("IU: " + change);
					}
				}
				catch (final IllegalStateException e)
				{
					// whatever
				}
			}
		}
		user:
		{
			// user playable character OID detection
			final EnumeratedPayloadField userOID = buf.getSingleFieldIndex(USER_OID);
			if (userOID == null)
				break user;
			
			final int objectID = buf.readInteger32(userOID);
			UserInfo ui;
			try
			{
				ui = computeIfAbsent(client, USER_INFO_KEY, k -> new UserInfo(objectID, getEntityContext(server)));
				if (objectID != ui.getUserOID())
				{
					ui = new UserInfo(objectID, getEntityContext(server));
					set(client, USER_INFO_KEY, ui);
				}
			}
			catch (final IllegalStateException e)
			{
				// client already disconnected in the meantime
				return;
			}
			
			final EnumeratedPayloadField level = buf.getSingleFieldIndex(USER_LEVEL);
			if (level != null)
				ui._level = buf.readInteger32(level);
			
			final EnumeratedPayloadField sp = buf.getSingleFieldIndex(USER_SP);
			if (sp != null)
				ui._sp = buf.readInteger(sp);
			
			final EnumeratedPayloadField width = buf.getSingleFieldIndex(USER_WIDTH), height = buf.getSingleFieldIndex(USER_HEIGHT);
			if (width != null)
				ui._width = buf.readDecimal(width);
			if (height != null)
				ui._height = buf.readDecimal(height);
			
			final EnumeratedPayloadField store = buf.getSingleFieldIndex(USER_PERSONAL_STORE);
			if (store != null)
				ui._personalStore = buf.readInteger32(store);
			
			return;
		}
		allSkills:
		{
			final EnumeratedPayloadField skillCount = buf.getSingleFieldIndex(OWNED_SKILL_COUNT);
			if (skillCount == null)
				break allSkills;
			
			final int totalSkills = buf.readInteger32(skillCount);
			if (totalSkills < 1)
			{
				computeIfAbsent(client, USER_SKILL_KEY, k -> new UserSkillList()).setSkills(Collections.emptyList());
				return;
			}
			
			final List<EnumeratedPayloadField> passives = buf.getFieldIndices(OWNED_SKILL_PASSIVE), levels = buf.getFieldIndices(OWNED_SKILL_LEVEL_SUBLEVEL), skills = buf.getFieldIndices(OWNED_SKILL),
					disabled = buf.getFieldIndices(OWNED_SKILL_DISABLED), enchantable = buf.getFieldIndices(OWNED_SKILL_ENCHANTABLE);
			
			final List<SkillListSkill> skillList = new ArrayList<>(totalSkills);
			for (int i = 0; i < skills.size(); ++i)
			{
				final boolean passive = buf.readInteger32(passives.get(i)) != 0;
				final int levelAndSublevel = buf.readInteger32(levels.get(i));
				final int skillID = buf.readInteger32(skills.get(i));
				final boolean disabled_ = buf.readInteger32(disabled.get(i)) != 0;
				final boolean enchantable_ = buf.readInteger32(enchantable.get(i)) != 0;
				skillList.add(new UserSkill(passive, levelAndSublevel, skillID, disabled_, enchantable_));
			}
			computeIfAbsent(client, USER_SKILL_KEY, k -> new UserSkillList()).setSkills(skillList);
			return;
		}
		allEffects:
		{
			// user abnormal effect detection
			final EnumeratedPayloadField effectCount = buf.getSingleFieldIndex(EFFECT_COUNT);
			if (effectCount == null)
				break allEffects;
			
			final int totalEffects = buf.readInteger32(effectCount);
			if (totalEffects < 1)
			{
				computeIfAbsent(client, USER_ABNORMAL_MODIFIER_KEY, k -> new UserAbnormals()).setAbnormalModifiers(Collections.emptyList());
				return;
			}
			
			final List<EnumeratedPayloadField> skills = buf.getFieldIndices(EFFECT_SKILL), levels = buf.getFieldIndices(EFFECT_LEVEL);
			final List<EnumeratedPayloadField> times = buf.getFieldIndices(EFFECT_TIME), longTimes = buf.getFieldIndices(EFFECT_TIME_EX);
			int longTimeIndex = -1;
			
			final long now = System.nanoTime();
			final List<AbnormalStatusModifier> abnormals = new ArrayList<>(totalEffects);
			for (int i = 0; i < skills.size(); ++i)
			{
				final int skillID = buf.readInteger32(skills.get(i));
				final int skillLevelAndSublevel = buf.readInteger32(levels.get(i));
				int secondsRemaining = buf.readInteger32(times.get(i));
				if (secondsRemaining == Short.MAX_VALUE)
					secondsRemaining = buf.readInteger32(longTimes.get(++longTimeIndex));
				abnormals.add(new UserAbnormalStatusModifier(skillID, skillLevelAndSublevel, secondsRemaining, now));
			}
			computeIfAbsent(client, USER_ABNORMAL_MODIFIER_KEY, k -> new UserAbnormals()).setAbnormalModifiers(abnormals);
			return;
		}
		
		final UserInfo ui = get(client, USER_INFO_KEY);
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
		waitType:
		{
			final EnumeratedPayloadField actorOID = buf.getSingleFieldIndex(WAIT_TYPE_ACTOR);
			if (actorOID == null)
				break waitType;
			
			final int objectID = buf.readInteger32(actorOID);
			if (objectID != ui._objectID)
				return;
			
			ui._waitType = buf.readFirstInteger32(WAIT_TYPE_SETTING);
			return;
		}
		skillCast:
		{
			final EnumeratedPayloadField actorOID = buf.getSingleFieldIndex(MSU_CASTER_OID);
			if (actorOID == null)
				break skillCast;
			
			final int objectID = buf.readInteger32(actorOID);
			if (objectID != ui._objectID)
				return;
			
			final long start = System.nanoTime();
			final int jointLevel = buf.readFirstInteger32(MSU_SKILL_JOINT_LEVEL);
			final int skillID = buf.readFirstInteger32(MSU_SKILL_ID), skillLevel = L2SkillTranslator.getSkillLevel(jointLevel), skillSublevel = L2SkillTranslator.getSkillSublevel(jointLevel);
			ui._lastSkillCast = new ActorSkillCast(skillID, skillLevel, skillSublevel, start, buf.readFirstInteger32(MSU_SKILL_TIME), TimeUnit.MILLISECONDS);
			final int coolTime = buf.readFirstInteger32(MSU_SKILL_COOL_TIME);
			ui._skillCoolTime.setCoolTime(skillID, skillLevel, skillSublevel, start, coolTime, TimeUnit.MILLISECONDS);
		}
		/*
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
			return;
		}
		*/
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
		int _targetOID;
		final Set<Integer> _servitorOIDs;
		int _level;
		long _sp;
		double _width, _height;
		int _personalStore;
		int _waitType;
		SkillCast _lastSkillCast;
		UserSkillCoolTime _skillCoolTime; // because this isn't SCT packet
		
		UserInfo(int objectID, ICacheServerID context)
		{
			_objectID = objectID;
			_context = context;
			_targetOID = NO_TARGET;
			_servitorOIDs = new HashSet<>();
			_level = 1;
			_sp = 0;
			_width = _height = 8;
			_personalStore = 0;
			_waitType = 1;
			_lastSkillCast = null;
			_skillCoolTime = new UserSkillCoolTime();
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
		
		public double getWidth()
		{
			return _width;
		}
		
		public double getHeight()
		{
			return _height;
		}
		
		public int getPersonalStore()
		{
			return _personalStore;
		}
		
		public int getWaitType()
		{
			return _waitType;
		}
		
		public SkillCast getLastSkillCast()
		{
			return _lastSkillCast;
		}
		
		public UserSkillCoolTime getSkillCoolTime()
		{
			return _skillCoolTime;
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
			final UserInfo other = (UserInfo)obj;
			if (_objectID != other._objectID)
				return false;
			return true;
		}
		
		@Override
		public String toString()
		{
			final int target = _targetOID;
			final ObjectInfo<L2ObjectInfo> info = L2ObjectInfoCache.getOrAdd(_objectID, _context), targetInfo = target != NO_TARGET ? L2ObjectInfoCache.getOrAdd(target, _context) : null;
			
			final L2TextBuilder tb = new L2TextBuilder(info.getName()).append(info.getExtraInfo().getCurrentLocation());
			if (targetInfo != null)
				tb.append("; target: ").append(targetInfo.getName()).append(targetInfo.getExtraInfo().getCurrentLocation());
			if (!_servitorOIDs.isEmpty())
				tb.append("; ").append(_servitorOIDs.size()).append(" servitors");
			return tb.moveToString();
		}
	}
	
	private enum ItemExtension
	{
		AUGMENTATION, ELEMENTAL_ATTRIBUTES, ENCHANT_EFFECT, APPEARANCE, SPECIAL_ABILITIES;
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
