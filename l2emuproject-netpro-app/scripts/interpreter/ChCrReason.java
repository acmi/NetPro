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

import net.l2emuproject.proxy.script.interpreter.ScriptedLegacyIntegerIdInterpreter;

/**
 * Interprets the given byte/word/dword as a reason why a character could not be created.
 * 
 * @author savormix
 */
public class ChCrReason extends ScriptedLegacyIntegerIdInterpreter
{
	@Override
	protected void loadImpl()
	{
		addInterpretation(-1, "Success");
		addInterpretation(0, "Your character creation has failed.");
		addInterpretation(1, "You cannot create another character. " + "Please delete the existing character and try again.");
		addInterpretation(2, "This name already exists.");
		addInterpretation(3, "Your title cannot exceed 16 characters in length. Please try again.");
		addInterpretation(4, "Incorrect name. Please try again.");
		addInterpretation(5, "Characters cannot be created from this server.");
		addInterpretation(6, "Unable to create character. " + "You are unable to create a new character on the selected server. " + "A restriction is in place which restricts users from creating "
				+ "characters on different servers where no previous character exists. " + "Please choose another server.");
	}
}
