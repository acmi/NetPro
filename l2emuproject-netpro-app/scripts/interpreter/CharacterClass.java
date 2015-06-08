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
 * Interprets the given byte/word/dword as a character class (profession) ID.
 * 
 * @author savormix
 */
public class CharacterClass extends ScriptedZeroBasedIntegerIdInterpreter
{
	/** Constructs this interpreter. */
	public CharacterClass()
	{
		// @formatter:off
		super(
				// Harbingers of War (247-304)
				"Human Fighter", // 0
				"Warrior",
				"Gladiator",
				"Warlord",
				"Human Knight",
				"Paladin", // 5
				"Dark Avenger",
				"Rogue",
				"Treasure Hunter",
				"Hawkeye",
				"Human Mystic", // 10
				"Human Wizard",
				"Sorcerer",
				"Necromancer",
				"Warlock",
				"Cleric", // 15
				"Bishop",
				"Prophet",
				"Elven Fighter",
				"Elven Knight",
				"Temple Knight", // 20
				"Swordsinger",
				"Elven Scout",
				"Plains Walker",
				"Silver Ranger",
				"Elven Mystic", // 25
				"Elven Wizard",
				"Spellsinger",
				"Elemental Summoner",
				"Elven Oracle",
				"Elven Elder", // 30
				"Dark Fighter",
				"Palus Knight",
				"Shillien Knight",
				"Bladedancer",
				"Assassin", // 35
				"Abyss Walker",
				"Phantom Ranger",
				"Dark Mystic",
				"Dark Wizard",
				"Spellhowler", // 40
				"Phantom Summoner",
				"Shillien Oracle",
				"Shillen Elder",
				"Orc Fighter",
				"Orc Raider", // 45
				"Destroyer",
				"Orc Monk",
				"Tyrant",
				"Orc Mystic",
				"Orc Shaman", // 50
				"Overlord",
				"Warcryer",
				"Dwarven Fighter",
				"Scavenger",
				"Bounty Hunter", // 55
				"Artisan",
				"Warsmith",
				
				null,
				null,
				null, // 60
				null,
				null,
				null,
				null,
				null, // 65
				null,
				null,
				null,
				null,
				null, // 70
				null,
				null,
				null,
				null,
				null, // 75
				null,
				null,
				null,
				null,
				null, // 80
				null,
				null,
				null,
				null,
				null, // 85
				null,
				null,
				
				// Scions of Destiny (1159-1189)
				"Duelist",
				"Dreadnought",
				"Phoenix Knight", // 90
				"Hell Knight",
				"Sagittarius",
				"Adventurer",
				"Archmage",
				"Soultaker", // 95
				"Arcana Lord",
				"Cardinal",
				"Hierophant",
				"Eva's Templar",
				"Sword Muse", // 100
				"Wind Rider",
				"Moonlight Sentinel",
				"Mystic Muse",
				"Elemental Master",
				"Eva's Saint", // 105
				"Shillien Templar",
				"Spectral Dancer",
				"Ghost Hunter",
				"Ghost Sentinel",
				"Storm Screamer", // 110
				"Spectral Master",
				"Shillien Saint",
				"Titan",
				"Grand Khavatari",
				"Dominator", // 115
				"Doomcryer",
				"Fortune Seeker",
				"Maestro",
				
				null,
				null, // 120
				null,
				null,
				
				// Kamael (1561-1574)
				"Kamael Soldier (M)",
				"Kamael Soldier (F)",
				"Trooper", // 125
				"Warder",
				"Berserker",
				"Soul Breaker (M)",
				"Soul Breaker (F)",
				"Arbalester", // 130
				"Doombringer",
				"Soul Hound (M)",
				"Soul Hound (F)",
				"Trickster",
				"Inspector", // 135
				"Judicator",
				
				null,
				null,
				
				// GoD (2477-2484)
				"Sigel Knight",
				"Tyrr Warrior", // 140
				"Othell Rogue",
				"Yul Archer",
				"Feoh Wizard",
				"Iss Enchanter",
				"Wynn Summoner", // 145
				"Aeore Healer",
				
				null,
				
				// Lindvior (3032-3065)
				"Sigel Phoenix Knight",
				"Sigel Hell Knight",
				"Sigel Eva's Templar", // 150
				"Sigel Shillien Templar",
				"Tyrr Duelist",
				"Tyrr Dreadnought",
				"Tyrr Titan",
				"Tyrr Grand Khavatari", // 155
				"Tyrr Maestro",
				"Tyrr Doombringer",
				"Othell Adventurer",
				"Othell Wind Rider",
				"Othell Ghost Hunter", // 160
				"Othell Fortune Seeker",
				"Yul Sagittarius",
				"Yul Moonlight Sentinel",
				"Yul Ghost Sentinel",
				"Yul Trickster", // 165
				"Feoh Archmage",
				"Feoh Soultaker",
				"Feoh Mystic Muse",
				"Feoh Storm Screamer",
				"Feoh Soulhound", // 170
				"Iss Hierophant",
				"Iss Sword Muse",
				"Iss Spectral Dancer",
				"Iss Dominator",
				"Iss Doomcryer", // 175
				"Wynn Arcana Lord",
				"Wynn Elemental Master",
				"Wynn Spectral Master",
				"Aeore Cardinal",
				"Aeore Eva's Saint", // 180
				"Aeore Shillien Saint",
				
				// Ertheia (3303-3310)
				"Ertheia Fighter",
				"Ertheia Wizard",
				"Marauder",
				"Cloud Breaker", // 185
				"Ripper",
				"Stratomancer",
				"Eviscerator",
				"Sayha's Seer"
				);
		// @formatter:on
	}
}
