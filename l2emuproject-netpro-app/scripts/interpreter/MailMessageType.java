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
 * @author _dev_
 */
public class MailMessageType extends ScriptedLegacyIntegerIdInterpreter
{
	@Override
	protected void loadImpl()
	{
		// Regular mail
		addInterpretation(0, "Standard");
		addInterpretation(1, "Private/noreply"); // sender name is '****' and reply/return buttons are removed
		addInterpretation(2, "Private"); // sender name is 'None'
		addInterpretation(3, "Anniversary"); // sender name is 'Alegria' and reply/return buttons are removed
		addInterpretation(4, "Hidden"); // all fields are empty and all buttons (except contact list) are removed
		addInterpretation(5, "Invisible"); // dialog window does not open
		addInterpretation(6, "Mentorship rewards"); // sender name is 'Mentor Guide' and reply/return buttons are removed
		// Other types
		addInterpretation(7, "Gift mailbox");
		addInterpretation(9, "Contact list prohibited"); // Contact list button is disabled
	}
}
