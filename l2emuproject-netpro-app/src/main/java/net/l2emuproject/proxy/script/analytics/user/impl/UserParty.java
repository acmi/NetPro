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
package net.l2emuproject.proxy.script.analytics.user.impl;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author _dev_
 */
public final class UserParty
{
	private final Map<Integer, PartyMember> _members;
	private final int _leaderSID, _lootDistribution;
	
	public UserParty(List<PartyMember> members, int leaderSID, int lootDistribution)
	{
		_members = members.stream().collect(Collectors.toMap(PartyMember::getSmartID, Function.identity(), null, LinkedHashMap::new));
		_leaderSID = leaderSID;
		_lootDistribution = lootDistribution;
	}
	
	public Stream<PartyMember> getVisibleMembers(UserVisibleWorldObjects allVisibleObjects)
	{
		return _members.values().stream().filter(pm -> allVisibleObjects.isCurrentlyVisible(pm.getSmartID()));
	}
}
