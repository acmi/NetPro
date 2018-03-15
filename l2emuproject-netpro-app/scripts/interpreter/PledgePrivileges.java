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

import net.l2emuproject.proxy.script.interpreter.ScriptedBitmaskInterpreter;

/**
 * Interprets the given bit mask as pledge power.
 * 
 * @author savormix
 */
public class PledgePrivileges extends ScriptedBitmaskInterpreter
{
	/** Constructs this interpreter. */
	public PledgePrivileges()
	{
		// @formatter:off
		super(
				null,
				"Invite",
				"Manage titles",
				"Warehouse search",
				"Manage ranks",
				"Clan war", // 5
				"Dismiss",
				"Edit crest",
				"Use functions", // "Apprentice",
				"Set functions", // "Troops/Fame",
				"Summon airship", // 10
				"Clan hall: entry/exit",
				"Clan hall: use functions",
				"Clan hall: auction",
				"Clan hall: dismiss",
				"Clan hall: set functions", // 15
				"Castle/Fortress: entry/exit",
				"Castle/Fortress: manor admin",
				"Castle/Fortress: siege war",
				"Castle/Fortress: use functions",
				"Castle/Fortress: right to dismiss", // 20
				"Castle/Fortress: manage taxes",
				"Castle/Fortress: mercenaries",
				"Castle/Fortress: set functions",
				"Throne of heroes"
		);
		// @formatter:on
	}
}
