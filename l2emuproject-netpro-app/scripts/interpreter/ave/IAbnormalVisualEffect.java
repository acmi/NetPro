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
		"ave_air_bind",
		"ave_change_body",
		// 50
		"ave_knock_down",
		"ave_navit_advent",
		
		// for the rest one would need to rely on LineageEffect, which mostly stores different names
		// well, whatever then
		null,
		null,
		null,
		// 55
		null,
		null,
		null,
		null,
		null,
		// 60
		null,
		null,
		null,
		null,
		null,
		// 65
		null,
		null,
		"nochat_ave",
		"herb_power_ave",
		"herb_magic_ave",
		// 70
		null,
		"d_hellfire (Destruction)",
		"d_hellfire (Annihilation)",
		"d_hellfire (Hellfire)",
		"d_hellfire (Desire)",
		// 75
		"d_hellfire (Longing)",
		null,
		null,
		null,
		null,
		null,
		// 80
		null,
		null,
		null,
		null,
		null,
		// 85
		"card_pcdeco",
		null,
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
		null,
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
