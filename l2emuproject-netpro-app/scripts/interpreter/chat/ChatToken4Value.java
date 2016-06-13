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
package interpreter.chat;

import eu.revengineer.simplejse.HasScriptDependencies;

/**
 * Interprets an integer as a type of a token to be filled in, as used in say packets.
 * 
 * @author _dev_
 */
@HasScriptDependencies("interpreter.chat.ChatTokenValue")
public final class ChatToken4Value extends ChatTokenValue
{
	/** Constructs this interpreter. */
	public ChatToken4Value()
	{
		super("__INTERP_ENABLER_TOKEN4_TYPE");
	}
}
