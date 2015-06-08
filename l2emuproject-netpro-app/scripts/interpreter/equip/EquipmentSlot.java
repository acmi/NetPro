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
package interpreter.equip;

import net.l2emuproject.proxy.script.interpreter.ScriptedZeroBasedIntegerIdInterpreter;

/**
 * Interprets the given byte/word/dword as a legacy equipment slot.
 * 
 * @author _dev_
 */
public class EquipmentSlot extends ScriptedZeroBasedIntegerIdInterpreter
{
	/** Constructs this interpreter. */
	public EquipmentSlot()
	{
		// @formatter:off
		super(
				"Underwear", // 0
				"Earring (bottom/left)",
				"Earring (top/right)",
				"Necklace",
				"Ring (bottom/left)",
				"Ring (top/right)", // 5
				"Headgear",
				"Weapon",
				"Shield [Sigil]",
				"Gloves",
				"Upper Body", // 10
				"Lower Body",
				"Boots",
				"Cloak",
				"Weapon / Two Handed",
				// TODO: confirm/disprove
				"Hair Accessory (top)", // 15
				"Hair Accessory (bottom)",
				"Right Bracelet",
				"Left Bracelet",
				"Talisman (1)",
				"Talisman (2)", // 20
				"Talisman (3)",
				"Talisman (4)",
				"Talisman (5)",
				"Talisman (6)",
				"Belt", //25
				"Brooch",
				"Jewel (1)",
				"Jewel (2)",
				"Jewel (3)",
				"Jewel (4)", // 30
				"Jewel (5)"
		);
		// @formatter:on
	}
}
