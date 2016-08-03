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
 * Interprets the given byte/word/dword as a reason why a character could not be created.
 * 
 * @author savormix
 */
public class ChCrReason extends ScriptedZeroBasedIntegerIdInterpreter
{
	/** Constructs this interpreter. */
	public ChCrReason()
	{
		super(new InterpreterMetadata(-1), "Success", "Your character creation has failed.", "You cannot create another character. Please delete the existing character and try again.",
				"This name already exists.", "Your title cannot exceed 16 characters in length. Please try again.", "Incorrect name. Please try again.",
				"Characters cannot be created from this server.",
				"Unable to create character. You are unable to create a new character on the selected server. A restriction is in place which restricts users from creating characters on different servers where no previous character exists. Please choose another server.");
	}
}
