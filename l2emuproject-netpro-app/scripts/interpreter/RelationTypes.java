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
 * Interprets the given bit mask as possible relationships between the user and other players.
 * This class was designed for C1.
 * 
 * @author savormix
 */
public class RelationTypes extends ScriptedBitmaskInterpreter
{
	/** Constructs this interpreter. */
	public RelationTypes()
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
				"Siege participant",
				"Siege attacker", // 10
				"Siege ally",
				"Siege enemy",
				"In pledge war",
				"Enemy pledge – attackable",
				"Enemy pledge – yielded against", // 15
				"Enemy pledge – resigned",
				"In alliance",
				"Alliance leader",
				"Same alliance",
				"In alliance war",
				"Enemy alliance"
				);
		// @formatter:on
	}
}
