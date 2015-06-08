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

import net.l2emuproject.proxy.script.interpreter.ScriptedBitmaskInterpreter;

/**
 * Interprets the given bit mask as a legacy paperdoll mask.<BR>
 * <BR>
 * This mask is used on legacy packets, such as ItemList, InventoryUpdate and the like.
 * A few things worth knowing about this mask:<BR>
 * All earrings and all rings have both slots set.<BR>
 * All talismans specify the 1st generic talisman slot, even though bits 23-27 are reserved.
 * 
 * @author savormix
 */
public class PaperdollMask extends ScriptedBitmaskInterpreter
{
	/** Constructs this interpreter. */
	public PaperdollMask()
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
				"One-piece armor", // 15
				"Hair Accessory (top)",
				"Formal wear",
				"Hair Accessory (bottom)",
				"Hair Accessory (both)",
				"Right Bracelet", // 20
				"Left Bracelet",
				"Talisman",
				null,
				null,
				null, // 25
				null,
				null,
				"Belt",
				"Brooch",
				"Jewel" // 30
		);
		// @formatter:on
	}
}
