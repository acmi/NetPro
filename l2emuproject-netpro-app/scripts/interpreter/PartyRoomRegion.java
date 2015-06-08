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
 * Interprets the given byte/word/dword as one of Aden's regions (this includes the Gracia continent).
 * 
 * @author savormix
 */
public class PartyRoomRegion extends ScriptedZeroBasedIntegerIdInterpreter
{
	/** Constructs this interpreter. */
	public PartyRoomRegion()
	{
		// @formatter:off
		super(new InterpreterMetadata(-1),
				"Any",
				"Offline", // 0
				"Talking island",
				"Gludio",
				"Dark elven",
				"Elven",
				"Dion", // 5
				"Giran",
				"Neutral zone",
				null,
				"Schuttgart",
				"Oren", // 10
				"Hunters village",
				"Innadril",
				"Aden",
				"Rune",
				"Goddard" // 15
				);
		// @formatter:on
	}
}
