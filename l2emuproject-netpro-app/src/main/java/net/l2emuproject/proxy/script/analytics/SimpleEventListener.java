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

import net.l2emuproject.proxy.network.game.client.L2GameClient;
import net.l2emuproject.proxy.script.analytics.user.AbnormalStatusModifier;
import net.l2emuproject.proxy.script.analytics.user.LiveUserAnalytics;

/**
 * Allows scripts to process high-level events.
 * 
 * @author _dev_
 */
public interface SimpleEventListener
{
	/** Indicates that no target has been selected for an action. */
	int NO_TARGET = 0;
	
	/**
	 * Called when some character targets an object. If an existing target is cancelled, then {@code targetOID} is {@value #NO_TARGET}.
	 * 
	 * @param client client (that received this event)
	 * @param selectorOID character
	 * @param targetOID target or {@value #NO_TARGET}
	 */
	default void onTargetSelected(L2GameClient client, int selectorOID, int targetOID)
	{
	}
	
	/**
	 * Called when some character is hit by a physical attack (not a skill).
	 * 
	 * @param client client (that received this event)
	 * @param attackerOID attacking character
	 * @param targetOID target character
	 */
	default void onPhysicalAttack(L2GameClient client, int attackerOID, int targetOID)
	{
	}
	
	/**
	 * Called when some character starts casting a skill.
	 * 
	 * @param client client (that received this event)
	 * @param casterOID casting character
	 * @param targetOID target character
	 * @param skill skill ID
	 * @param level skill level
	 * @param castTime milliseconds to skill use
	 * @param coolTime milliseconds to next cast
	 */
	default void onCast(L2GameClient client, int casterOID, int targetOID, int skill, int level, int castTime, int coolTime)
	{
	}
	
	/**
	 * Called when some character finishes casting a skill. This will only be called for skills that have to be targeted manually, so do not expect to receive this for
	 * toggles, soul/spiritshots, certain buffs and some other skills.<BR>
	 * <BR>
	 * This call is generated from a {@code MagicSkillLaunched} packet; one call for each affected target.<BR>
	 * It is possible that the amount of affected targets is 0; then a single call is issued with {@code targetOID} = {@value #NO_TARGET}.
	 * 
	 * @param client client (that received this event)
	 * @param casterOID casting character
	 * @param targetOID target character or {@value #NO_TARGET}
	 * @param skill skill ID
	 * @param level skill level
	 */
	@Deprecated
	default void onCastSuccess(L2GameClient client, int casterOID, int targetOID, int skill, int level)
	{
	}
	
	/**
	 * Called when some character finishes casting a skill. This will only be called for skills that have to be targeted manually, so do not expect to receive this for
	 * toggles, soul/spiritshots, certain buffs and some other skills.<BR>
	 * <BR>
	 * This call is generated from a {@code MagicSkillLaunched} packet; one call for each affected target.<BR>
	 * It is possible that the amount of affected targets is 0; then a single call is issued with {@code targetOID} = {@value #NO_TARGET}.
	 * 
	 * @param client client (that received this event)
	 * @param casterOID casting character
	 * @param targetOIDs target characters or an empty list
	 * @param skill skill ID
	 * @param level skill level
	 */
	default void onCastSuccess(L2GameClient client, int casterOID, List<Integer> targetOIDs, int skill, int level)
	{
		if (targetOIDs.isEmpty())
		{
			onCastSuccess(client, casterOID, NO_TARGET, skill, level);
			return;
		}
		for (final Integer targetOID : targetOIDs)
			onCastSuccess(client, casterOID, targetOID, skill, level);
	}
	
	/**
	 * Called when a skill cast is interrupted, either manually or due to excessive damage to the caster.
	 * 
	 * @param client client (that received this event)
	 * @param casterOID previously casting character
	 */
	default void onCastFailure(L2GameClient client, int casterOID)
	{
	}
	
	/**
	 * Called when some character dies.
	 * 
	 * @param client client (that received this event)
	 * @param deceasedOID dead character
	 * @param sweepable whether spoil is enabled
	 */
	default void onDeath(L2GameClient client, int deceasedOID, boolean sweepable)
	{
	}
	
	/**
	 * Called when some character is successfully resurrected.
	 * 
	 * @param client client (that received this event)
	 * @param revivedOID resurrected (alive) character
	 */
	default void onRevive(L2GameClient client, int revivedOID)
	{
	}
	
	/**
	 * Called when some object is removed from a client's knownlist.
	 * 
	 * @param client client (that received this event)
	 * @param deletedOID removed object
	 */
	default void onDelete(L2GameClient client, int deletedOID)
	{
	}
	
	/**
	 * Called when some character (either monster or player/pet) drops an item.
	 * 
	 * @param client client (that received this event)
	 * @param dropperOID dropping character
	 * @param itemOID dropped item
	 * @param amount item amount
	 */
	default void onItemDrop(L2GameClient client, int dropperOID, int itemOID, long amount)
	{
	}
	
	/**
	 * Called when some item is added to a client's knownlist.
	 * 
	 * @param client client (that received this event)
	 * @param itemOID item on ground
	 * @param amount item amount
	 */
	default void onItemSpawn(L2GameClient client, int itemOID, long amount)
	{
	}
	
	/**
	 * Called when some item is picked up by a character.
	 * 
	 * @param client client (that received this event)
	 * @param finderOID looting character
	 * @param itemOID looted item
	 */
	default void onItemPickup(L2GameClient client, int finderOID, int itemOID)
	{
	}
	
	/**
	 * Called whenever a certain abnormal effect is either lost (incl. expiry) or newly acquired. This will not be called if the only changes are effect timers changing.
	 * 
	 * @param client client (that received this event)
	 * @param removed removed effects
	 * @param added added effects
	 */
	default void onAbnormalListChange(L2GameClient client, List<AbnormalStatusModifier> removed, List<AbnormalStatusModifier> added)
	{
	}
	
	/**
	 * Called at the smallest typical abnormal effect duration unit. Use {@link LiveUserAnalytics#getUserAbnormals(L2GameClient)} to decide if action needs to be taken.
	 * 
	 * @param client client (that received this event)
	 * @param removed removed effects
	 * @param added added effects
	 */
	default void onAbnormalListTick(L2GameClient client, List<AbnormalStatusModifier> removed, List<AbnormalStatusModifier> added)
	{
		if (!removed.isEmpty() || !added.isEmpty())
			onAbnormalListChange(client, removed, added);
	}
	
	/**
	 * Called when some character finishes moving and stands still.
	 * 
	 * @param client client (that received this event)
	 * @param stopperOID previously moving character
	 * @param x character's current X coordinate
	 * @param y character's current Y coordinate
	 * @param z character's current Z coordinate
	 */
	default void onMovementEnd(L2GameClient client, int stopperOID, int x, int y, int z)
	{
	}
}
