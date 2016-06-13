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

import net.l2emuproject.proxy.network.meta.interpreter.IntegerInterpreter;
import net.l2emuproject.proxy.script.interpreter.ScriptedFieldValueInterpreter;
import net.l2emuproject.proxy.state.entity.context.ICacheServerID;
import net.l2emuproject.util.EnumValues;

/**
 * Interprets an integer as a type of a token to be filled in, as used in say packets.
 * 
 * @author _dev_
 */
public final class ChatTokenType extends ScriptedFieldValueInterpreter implements IntegerInterpreter
{
	@Override
	public Object getInterpretation(long value, ICacheServerID entityCacheContext)
	{
		return String.valueOf(getType((int)value));
	}
	
	static final TokenType getType(int tokenType)
	{
		return TokenType.VALUES.valueOf(tokenType);
	}
	
	enum TokenType
	{
		STRING("String"), ITEM("Item"), NPC("NPC"), SKILL("Skill"), QUEST("Quest"), FSTRING("fstring (immutable)"), SYSSTRING("sysstring");
		
		private final String _desc;
		
		private TokenType(String desc)
		{
			_desc = desc;
		}
		
		@Override
		public String toString()
		{
			return _desc;
		}
		
		static final EnumValues<TokenType> VALUES = new EnumValues<TokenType>(TokenType.class)
		{
			@Override
			protected TokenType defaultValue()
			{
				return STRING;
			}
		};
	}
}
