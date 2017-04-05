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
package interpreter;

import net.l2emuproject.proxy.script.interpreter.ScriptedZeroBasedIntegerIdInterpreter;

/**
 * Interprets the given byte/word/dword as a type of a skill list.
 * 
 * @author savormix
 */
public final class SkillListType extends ScriptedZeroBasedIntegerIdInterpreter
{
	/** Constructs this interpreter. */
	public SkillListType()
	{
		// @formatter:off
		super("skill_acquire", "fishing_skill_acquire", "pledge_skill_acquire", "sub_pledge_skill_acquire", "transform_skill_acquire", "subjob_skill_acquire",
				"collect_skill_acquire", "skill_bishop_sharing_acquire", "skill_elder_sharing_acquire", "skill_silen_elder_sharing_acquire", "fishing_nondwarf_skill_acquire",
				"reward_duelist", "reward_dreadnought", "reward_phoenix_knight", "reward_hell_knight", "reward_sagittarius", "reward_adventurer", "reward_archmage",
				"reward_soultaker", "reward_arcana_lord", "reward_cardinal", "reward_hierophant", "reward_evas_templar", "reward_sword_muse", "reward_wind_rider",
				"reward_moonlight_sentinel", "reward_mystic_muse", "reward_elemental_master", "reward_evas_saint", "reward_shillien_templar", "reward_spectral_dancer",
				"reward_ghost_hunter", "reward_ghost_sentinel", "reward_storm_screamer", "reward_spectral_master", "reward_shillien_saint", "reward_titan",
				"reward_grand_khavatari", "reward_dominator", "reward_doomcryer", "reward_fortune_seeker", "reward_maestro", "reward_doombringer", "reward_m_soul_hound",
				"reward_f_soul_hound", "reward_trickster", "reward_judicator", "dualjob_skill_acquire", "dualjob_sharing_acquire",
				null,
				null, // 50
				null,
				null,
				null,
				null,
				null, // 55
				null,
				"Revelation of Chaos (Main)",
				null,
				"Revelation of Chaos (Dual Class)");
		// @formatter:on
	}
}
