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

import net.l2emuproject.proxy.network.meta.RandomAccessMMOBuffer;
import net.l2emuproject.proxy.network.meta.container.MetaclassRegistry;
import net.l2emuproject.proxy.network.meta.exception.InvalidFieldValueInterpreterException;
import net.l2emuproject.proxy.network.meta.interpreter.ContextualFieldValueInterpreter;
import net.l2emuproject.proxy.network.meta.interpreter.StringInterpreter;
import net.l2emuproject.proxy.script.interpreter.ScriptedFieldValueInterpreter;
import net.l2emuproject.proxy.script.interpreter.ScriptedIntegerIdInterpreter;
import net.l2emuproject.proxy.state.entity.context.ICacheServerID;

import interpreter.chat.ChatTokenType.TokenType;

/**
 * Interprets an integer as a type of a token to be filled in, as used in say packets.
 * 
 * @author _dev_
 */
@HasScriptDependencies("interpreter.chat.ChatTokenType")
public abstract class ChatTokenValue extends ScriptedFieldValueInterpreter implements ContextualFieldValueInterpreter, StringInterpreter
{
	private final String _typeFieldAlias;
	private final ThreadLocal<TokenType> _type;
	
	/**
	 * Constructs this interpreter.
	 * 
	 * @param typeFieldAlias alias of a field that describes token type
	 */
	protected ChatTokenValue(String typeFieldAlias)
	{
		_typeFieldAlias = typeFieldAlias;
		_type = new ThreadLocal<TokenType>()
		{
			@Override
			protected TokenType initialValue()
			{
				return TokenType.STRING;
			}
		};
	}
	
	@Override
	public void reviewContext(RandomAccessMMOBuffer buffer)
	{
		_type.set(ChatTokenType.getType(buffer.readFirstInteger32(_typeFieldAlias)));
	}
	
	@Override
	public Object getInterpretation(String value, ICacheServerID entityCacheContext)
	{
		try
		{
			switch (_type.get())
			{
				case FSTRING:
					return MetaclassRegistry.getInstance().getInterpreter("ImmutableNpcString", ScriptedIntegerIdInterpreter.class).getInterpretation(Long.parseLong(value), entityCacheContext);
				case ITEM:
					return MetaclassRegistry.getInstance().getInterpreter("Item", ScriptedIntegerIdInterpreter.class).getInterpretation(Long.parseLong(value), entityCacheContext);
				case NPC:
					return MetaclassRegistry.getInstance().getInterpreter("Npc", ScriptedIntegerIdInterpreter.class).getInterpretation(Long.parseLong(value), entityCacheContext);
				case QUEST:
					return MetaclassRegistry.getInstance().getInterpreter("Quest", ScriptedIntegerIdInterpreter.class).getInterpretation(Long.parseLong(value), entityCacheContext);
				case SKILL:
					return MetaclassRegistry.getInstance().getInterpreter("Skill", ScriptedIntegerIdInterpreter.class).getInterpretation(Long.parseLong(value), entityCacheContext);
				case SYSSTRING:
					return MetaclassRegistry.getInstance().getInterpreter("SysString", ScriptedIntegerIdInterpreter.class).getInterpretation(Long.parseLong(value), entityCacheContext);
				case STRING:
				default:
					break;
			}
		}
		catch (InvalidFieldValueInterpreterException | NumberFormatException e)
		{
			// just use the token value as is
		}
		return value;
	}
}
