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
package interpreter.relation;

import net.l2emuproject.proxy.script.interpreter.ScriptedBitmaskInterpreter;

/**
 * Interprets the given bit mask as possible relationships between the user and other players.
 * This class was designed for Helios and above.
 * 
 * @author _dev_
 */
public class RelationTypeHelios extends ScriptedBitmaskInterpreter
{
	/** Constructs this interpreter. */
	public RelationTypeHelios()
	{
		// @formatter:off
		super("None", null,
				"Inside battlefield", // 0
				"In PvP",
				"Chaotic",
				"In party",
				"Party leader",
				"Same party", // 5
				"In pledge",
				"Pledge leader",
				"Same pledge",
				null,
				null, // 10
				null,
				null,
				null,
				// A war declaration was received from the player's pledge
				"Enemy pledge – attacker",
				// A war was declared on the player's pledge
				"Enemy pledge – under attack", // 15
				"In alliance",
				"Alliance leader"
				// TODO: fortress siege
				// clanhall siege
				// castle siege
		);
		// @formatter:on
	}
}
