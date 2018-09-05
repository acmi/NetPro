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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.tuple.ImmutablePair;

import net.l2emuproject.lang.L2TextBuilder;
import net.l2emuproject.network.protocol.IProtocolVersion;
import net.l2emuproject.proxy.script.interpreter.ScriptedIntegerIdInterpreter;

/**
 * Interprets the given byte/word/dword as a system message.
 * 
 * @author savormix
 */
public class ImmutableSystemMessage extends ScriptedIntegerIdInterpreter
{
	private final Pattern[] _tokens;
	
	/** Constructs this interpreter. */
	public ImmutableSystemMessage()
	{
		super(loadFromResource("sm.txt", ImmutablePair.of(-1L, "N/A")));
		_tokens = new Pattern[9];
		for (int i = 1; i <= 9; ++i)
			_tokens[i - 1] = Pattern.compile("\\$[cs]" + i);
	}
	
	/**
	 * Returns a system message with given tokens inserted into it.
	 * 
	 * @param protocol protocol version
	 * @param sm system message ID
	 * @param tokens tokens
	 * @return complete message as string
	 */
	public String getRepresentation(IProtocolVersion protocol, int sm, String... tokens)
	{
		final L2TextBuilder tb = new L2TextBuilder(String.valueOf(translate(sm, protocol, null)));
		for (int i = 0; i < _tokens.length && i < tokens.length; ++i)
		{
			final Matcher m = _tokens[i].matcher(tb);
			if (!m.find())
				break;
			
			tb.replace(m.start(), m.end(), tokens[i]);
		}
		return tb.moveToString();
	}
}
