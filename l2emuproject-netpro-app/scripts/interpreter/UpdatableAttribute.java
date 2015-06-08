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
 * Interprets the given byte/word/dword as an updatable player's attribute.
 * 
 * @author savormix
 */
public class UpdatableAttribute extends ScriptedZeroBasedIntegerIdInterpreter
{
	/** Constructs this interpreter. */
	public UpdatableAttribute()
	{
		// @formatter:off
		super(new InterpreterMetadata(1),
				"Level",
				"XP",
				"STR",
				"DEX",
				"CON", // 5
				"INT",
				"WIT",
				"MEN",
				"Current HP",
				"Maximum HP", // 10
				"Current MP",
				"Maximum MP",
				"SP",
				"Current carried weight",
				"Maximum carried weight", // 15
				null,
				"P. Atk.",
				"Attack speed",
				"P. Def.",
				"Evasion", // 20
				"Accuracy",
				"Critical",
				"M. Atk.",
				"Casting speed",
				"M. Def.", // 25
				"In PvP",
				"Reputation",
				null,
				null,
				null, // 30
				null,
				null,
				"Current CP",
				"Maximum CP"
				);
		// @formatter:on
	}
}
