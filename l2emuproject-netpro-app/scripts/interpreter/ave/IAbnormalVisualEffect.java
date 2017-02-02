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
package interpreter.ave;

/**
 * Defines known abnormal visual effects.
 * 
 * @author _dev_
 */
public interface IAbnormalVisualEffect
{
	/** All known abnormal visual effects. {@code null} indicates lack of data (and is a valid value). */
	// @formatter:off
	String[] EFFECTS = {
		// 0
		"ave_dot_bleeding",
		"ave_dot_poison",
		"ave_dot_fire",
		"ave_dot_water",
		"ave_dot_wind",
		// 5
		"ave_dot_soil",
		"ave_stun",
		"ave_sleep",
		"ave_silence",
		"ave_root",
		// 10
		"ave_paralyze",
		"ave_flesh_stone",
		"ave_dot_mp",
		"ave_big_head",
		"ave_dot_fire_area",
		// 15
		"ave_change_texture",
		"ave_big_body",
		"ave_floating_root",
		"ave_dance_root",
		"ave_ghost_stun",
		// 20
		"ave_stealth",
		"ave_seizure1",
		"ave_seizure2",
		"ave_magic_square",
		"ave_freezing",
		// 25
		"ave_shake",
		"ave_blind",
		"ave_ultimate_defence",
		"ave_vp_up",
		"ave_real_target",
		// 30
		"ave_death_mark",
		"ave_turn_flee",
		
		// in legacy clients, effects below would overflow to an extension DWORD
		"ave_invincibility",
		"ave_air_battle_slow",
		"ave_air_battle_root",
		// 35
		"ave_change_wp",
		"ave_change_hair_g",
		"ave_change_hair_p",
		"ave_change_hair_b",
		"ave_vp_keep",
		// 40
		"ave_stigma_of_silen",
		"ave_speed_down",
		"ave_frozen_pillar",
		"ave_change_ves_s",
		"ave_change_ves_c",
		// 45
		"ave_change_ves_d",
		"ave_time_bomb",
		"ave_mp_shield",
		"ave_airbind",
		"ave_changebody",
		// 50
		"ave_knockdown",
		"ave_navit_advent", // unlisted
		"ave_knockback",
		"ave_change_7anniversary",
		"ave_pain_root",
		// 55
		"ave_deport",
		"ave_aura_buff",
		"ave_aura_buff_self",
		"ave_aura_debuff",
		"ave_aura_debuff_self",
		// 60
		"ave_hurricane",
		"ave_hurricane_self",
		"ave_black_mark",
		"br_ave_soul_avatar",
		"ave_change_grade_b",
		// 65
		"br_ave_beam_sword_onehand",
		"br_ave_beam_sword_dual",
		"nochat_ave",
		"ave_herb_pa_up",
		"ave_herb_ma_up",
		// 70
		"ave_seed_talisman1", // ave_seed_talisman1 is not used
		"ave_seed_talisman2",
		"ave_seed_talisman3",
		"ave_seed_talisman4",
		"ave_seed_talisman5",
		// 75
		"ave_seed_talisman6",
		"ave_curious_house",
		"ave_ngrade_change",
		"ave_dgrade_change",
		"ave_cgrade_change",
		// 80
		"ave_bgrade_change",
		"ave_agrade_change",
		null, // red beach swimsuit
		null, // blue beach swimsuit
		null, // santa costume
		// 85
		"card_pcdeco",
		null, // baseball outfit
		"wday_event_heart",
		"wday_event_choco",
		"wday_event_candy",
		// 90
		"wday_event_cookie",
		"gam_0star",
		"gam_1star",
		"gam_2star",
		"gam_3star",
		// 95
		"gam_4star",
		"gam_5star",
		"ave_dualing_buff",
		"ave_freezing", // new effect, duplicate name
		null, // metal armor
		// 100
		"yogi_event_ave",
		"hellocat1_event_ave",
		"hellocat2_event_ave",
		"hellocat3_event_ave",
		"10th_event_ave",
		// 105
		"santa_socks_ave",
		"santa_tree_ave",
		"santa_snowman_ave",
		"ro_bluff_ave",
		"he_protect_ave",
		// 110
		"su_sumcross_ave",
		"er_wi_windstun_ave",
		"er_wi_stormsign2_ave",
		"er_wi_stormsign1_ave",
		"er_wi_windhide_ave",
		// 115
		null,
		null,
		"er_wi_psypower_ave",
		"er_wi_squall_ave",
		"er_wi_illuwind_ave",
		// 120
		null,
		null,
		"er_wa_gstrong_up",
		"er_wa_pmental_hand_trail/er_wa_pmental_foot_trail",
		"er_wi_hold_ave/er_wi_lightning2_ave", // should be hold
		// 125
		null, // flattens character to a 2D sprite
		"er_wa_spaceref_ave",
		"he_aspect_ave",
	};
	// @formatter:on
}
