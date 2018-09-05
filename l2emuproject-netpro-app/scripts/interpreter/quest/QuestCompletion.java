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
package interpreter.quest;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;

import net.l2emuproject.lang.L2TextBuilder;
import net.l2emuproject.network.protocol.IProtocolVersion;
import net.l2emuproject.proxy.io.IOConstants;
import net.l2emuproject.proxy.network.meta.interpreter.ByteArrayTranslator;
import net.l2emuproject.proxy.script.interpreter.ScriptedFieldValueInterpreter;
import net.l2emuproject.proxy.state.entity.context.ICacheServerID;

/**
 * Interprets the given byte array as quest completion flags.
 * 
 * @author _dev_
 */
public class QuestCompletion extends ScriptedFieldValueInterpreter implements ByteArrayTranslator, IOConstants
{
	private final String[] _questNames;
	
	/** Constructs this interpreter. */
	public QuestCompletion()
	{
		_questNames = new String[1024];
		for (int i = 0; i < _questNames.length; ++i)
			_questNames[i] = String.valueOf(i);
		try (final BufferedReader br = Files.newBufferedReader(INTERPRETER_CONFIG_DIRECTORY.resolve("quest.txt")))
		{
			for (String line; (line = br.readLine()) != null;)
			{
				final String[] quest = line.split("\t");
				int id = Integer.parseInt(quest[0]);
				if (id > 10_000)
					id -= 10_000;
				_questNames[id] = quest[1].intern();
			}
		}
		catch (IOException e)
		{
			// just use prefilled quest IDs
		}
	}
	
	@Override
	public Object translate(byte[] value, IProtocolVersion protocol, ICacheServerID entityCacheContext)
	{
		final L2TextBuilder tb = new L2TextBuilder();
		final int sz = Math.min(value.length, _questNames.length);
		for (int i = 0; i < sz; ++i)
		{
			for (int b = 0; b < 8; ++b)
			{
				if ((value[i] & (1 << b)) == 0)
					continue;
					
				tb.append('[').append(_questNames[(i << 3) | b]).append("], ");
			}
		}
		
		if (tb.length() < 2)
		{
			tb.moveToString();
			return "None";
		}
		
		tb.setLength(tb.length() - 2);
		return tb.moveToString();
	}
}
