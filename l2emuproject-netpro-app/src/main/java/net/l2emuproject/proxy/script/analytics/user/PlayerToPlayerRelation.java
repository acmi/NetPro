/*
 * Copyright 2011-2017 L2EMU UNIQUE
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

import static net.l2emuproject.proxy.script.analytics.user.PlayerToPlayerRelation.KnownP2PRelation.ALLIANCE_LEADER;
import static net.l2emuproject.proxy.script.analytics.user.PlayerToPlayerRelation.KnownP2PRelation.ATTACKED_PLEDGE_MEMBER;
import static net.l2emuproject.proxy.script.analytics.user.PlayerToPlayerRelation.KnownP2PRelation.ATTACKING_PLEDGE_MEMBER;
import static net.l2emuproject.proxy.script.analytics.user.PlayerToPlayerRelation.KnownP2PRelation.CHAOTIC;
import static net.l2emuproject.proxy.script.analytics.user.PlayerToPlayerRelation.KnownP2PRelation.IN_ALLIANCE;
import static net.l2emuproject.proxy.script.analytics.user.PlayerToPlayerRelation.KnownP2PRelation.IN_PARTY;
import static net.l2emuproject.proxy.script.analytics.user.PlayerToPlayerRelation.KnownP2PRelation.IN_PVP;
import static net.l2emuproject.proxy.script.analytics.user.PlayerToPlayerRelation.KnownP2PRelation.ON_BATTLEFIELD;
import static net.l2emuproject.proxy.script.analytics.user.PlayerToPlayerRelation.KnownP2PRelation.PARTY_LEADER;
import static net.l2emuproject.proxy.script.analytics.user.PlayerToPlayerRelation.KnownP2PRelation.PLEDGE_LEADER;
import static net.l2emuproject.proxy.script.analytics.user.PlayerToPlayerRelation.KnownP2PRelation.SAME_PARTY;
import static net.l2emuproject.proxy.script.analytics.user.PlayerToPlayerRelation.KnownP2PRelation.SAME_PLEDGE;

import java.util.Set;

import net.l2emuproject.util.BitMaskUtils;

/**
 * @author _dev_
 */
public interface PlayerToPlayerRelation
{
	int getOtherPlayerOID();
	
	int getRelationMask();
	
	default Set<KnownP2PRelation> getKnownRelation()
	{
		return BitMaskUtils.setOf(getRelationMask(), KnownP2PRelation.class);
	}
	
	boolean isDefaultInteractionAttack();
	
	int getReputation();
	
	Boolean isInPvP();
	
	default String getShortRelationSummary()
	{
		final Set<KnownP2PRelation> relation = getKnownRelation();
		final StringBuilder result = new StringBuilder();
		if (relation.contains(ON_BATTLEFIELD))
			result.append("PvP[ZONE]");
		else if (relation.contains(CHAOTIC))
			result.append("PvP[-REP]");
		else if (relation.contains(IN_PVP))
			result.append("PvP[FLAG]");
		if (result.length() > 0)
			result.append(' ');
		
		if (relation.contains(SAME_PARTY))
			result.append("Your ");
		if (relation.contains(PARTY_LEADER))
			result.append("PL");
		else if (relation.contains(IN_PARTY))
			result.append("PT");
		if (result.length() > 0 && result.charAt(result.length() - 1) != ' ')
			result.append(' ');
		
		if (relation.contains(SAME_PLEDGE))
			result.append("Your ");
		else if (relation.contains(ATTACKING_PLEDGE_MEMBER) && relation.contains(ATTACKED_PLEDGE_MEMBER))
			result.append("War ");
		else if (relation.contains(ATTACKING_PLEDGE_MEMBER))
			result.append("AtkR ");
		else if (relation.contains(ATTACKED_PLEDGE_MEMBER))
			result.append("AtkD ");
		if (relation.contains(PLEDGE_LEADER))
			result.append("CL");
		else
			result.append("CM");
		if (result.length() > 0 && result.charAt(result.length() - 1) != ' ')
			result.append(' ');
		
		if (relation.contains(ALLIANCE_LEADER))
			result.append("AL");
		else if (relation.contains(IN_ALLIANCE))
			result.append("AM");
		
		if (result.length() < 1)
			return "";
		if (result.charAt(result.length() - 1) == ' ')
			result.setLength(result.length() - 1);
		return result.toString();
	}
	
	public enum KnownP2PRelation
	{
		ON_BATTLEFIELD, IN_PVP, CHAOTIC, IN_PARTY, PARTY_LEADER, SAME_PARTY, IN_PLEDGE, PLEDGE_LEADER, SAME_PLEDGE, RESERVED_9_512, RESERVED_10_1024, RESERVED_11_2048, RESERVED_12_4096, RESERVED_13_8192, ATTACKING_PLEDGE_MEMBER, ATTACKED_PLEDGE_MEMBER, IN_ALLIANCE, ALLIANCE_LEADER;
	}
}
