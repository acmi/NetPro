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
 * Interprets the given bit mask as tutorial events that server listens for from the client.
 * 
 * @author savormix
 */
public class TutorialEvent extends ScriptedBitmaskInterpreter
{
	/** Constructs this interpreter. */
	public TutorialEvent()
	{
		// @formatter:off
		super("All notifications disabled", null,
				"MOVE_CLICK", // 0 | LMB click; moving with WASD will not cause client to send anything
				"VIEW_CHANGE", // RMB + motion
				"WHEELING", // MW scroll
				"RIGHT_CLICK", // RMB click
				"WHEEL_CLICK", // MW/MMB click
				"LEVEL_UP_49", // 5
				"LEVEL_UP_61",
				"LEVEL_UP_73",
				"HP_UNDER_1over3", // Aside from the oldest clients, this is indicated by a flashing HP bar
				"DIE_1_9",
				"LEVEL_UP_5", // 10
				"LEVEL_UP_7",
				"LEVEL_UP_20",
				"OPEN_QUEST_WINDOW", // ALT+U, then 'Item' tab
				"LEVEL_UP_39",
				"LEVEL_UP_76", // 15
				null,
				null,
				null,
				null,
				"PICK_BLUE_GEM", // 20
				"PICK_ADENA",
				"LEVEL_UP_4",
				"SIT_DOWN", // Sitting/idling for HP regen
				"LEVEL_UP_35",
				"LEVEL_UP_75", // 25
				"LEVEL_UP_15",
				"LEVEL_UP_6",
				"LEVEL_UP_9",
				"LEVEL_UP_10",
				"LEVEL_UP_12" // 30
		);
		// @formatter:on
	}
}
