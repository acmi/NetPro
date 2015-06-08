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
 * Interprets the given bitmask as user's possible social roles.
 * 
 * @author _dev_
 */
public final class UserRoles extends ScriptedBitmaskInterpreter
{
	/** Constructs this interpreter. */
	public UserRoles()
	{
		// @formatter:off
		super("None", null,
				"Inside battlefield", // 0
				"In PvP",
				"Chaotic",
				"In party",
				"Party leader",
				"In pledge", // 5
				"Pledge leader",
				"Siege participant",
				"Siege attacker",
				"In pledge war",
				"In alliance", // 10
				"Alliance leader",
				"In alliance war"
				);
		// @formatter:on
	}
}
