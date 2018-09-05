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
import net.l2emuproject.network.protocol.IProtocolVersion;
import net.l2emuproject.proxy.network.meta.EnumeratedPayloadField;
import net.l2emuproject.proxy.network.meta.RandomAccessMMOBuffer;
import net.l2emuproject.proxy.network.meta.container.MetaclassRegistry;
import net.l2emuproject.proxy.network.meta.exception.InvalidFieldValueInterpreterException;
import net.l2emuproject.proxy.network.meta.interpreter.ContextualFieldValueTranslator;
import net.l2emuproject.proxy.network.meta.interpreter.StringTranslator;
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
public abstract class ChatTokenValue extends ScriptedFieldValueInterpreter implements ContextualFieldValueTranslator, StringTranslator
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
		_type = new ThreadLocal<TokenType>(){
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
		final EnumeratedPayloadField field = buffer.getSingleFieldIndex(_typeFieldAlias);
		_type.set(field != null ? ChatTokenType.getType(buffer.readInteger32(field)) : TokenType.STRING);
	}
	
	@Override
	public Object translate(String value, IProtocolVersion protocol, ICacheServerID entityCacheContext)
	{
		try
		{
			switch (_type.get())
			{
				case FSTRING:
					return MetaclassRegistry.getInstance().getTranslator("ImmutableNpcString", ScriptedIntegerIdInterpreter.class).translate(Long.parseLong(value), protocol, entityCacheContext);
				case ITEM:
					return MetaclassRegistry.getInstance().getTranslator("Item", ScriptedIntegerIdInterpreter.class).translate(Long.parseLong(value), protocol, entityCacheContext);
				case NPC:
					return MetaclassRegistry.getInstance().getTranslator("Npc", ScriptedIntegerIdInterpreter.class).translate(Long.parseLong(value), protocol, entityCacheContext);
				case QUEST:
					return MetaclassRegistry.getInstance().getTranslator("Quest", ScriptedIntegerIdInterpreter.class).translate(Long.parseLong(value), protocol, entityCacheContext);
				case SKILL:
					return MetaclassRegistry.getInstance().getTranslator("Skill", ScriptedIntegerIdInterpreter.class).translate(Long.parseLong(value), protocol, entityCacheContext);
				case SYSSTRING:
					return MetaclassRegistry.getInstance().getTranslator("SysString", ScriptedIntegerIdInterpreter.class).translate(Long.parseLong(value), protocol, entityCacheContext);
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
