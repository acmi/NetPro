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
 * Interprets the given byte/word/dword as a reason why a character could not be deleted.
 * 
 * @author savormix
 */
public class ChDeReason extends ScriptedZeroBasedIntegerIdInterpreter
{
	/** Constructs this interpreter. */
	public ChDeReason()
	{
		// @formatter:off
		super(new InterpreterMetadata(1),
				"You have failed to delete the character.",
				"You may not delete a clan member. Withdraw from the clan first and try again.",
				"Clan leaders may not be deleted. Dissolve the clan first and try again.",
				"You cannot delete characters on this server right now."
				);
		// @formatter:on
	}
}
