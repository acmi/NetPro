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

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.l2emuproject.proxy.io.IOConstants;
import net.l2emuproject.proxy.network.meta.EnumeratedPayloadField;
import net.l2emuproject.proxy.network.meta.RandomAccessMMOBuffer;
import net.l2emuproject.proxy.network.meta.interpreter.ContextualFieldValueInterpreter;
import net.l2emuproject.proxy.script.interpreter.ScriptedIntegerIdInterpreter;
import net.l2emuproject.proxy.state.entity.context.ICacheServerID;
import net.l2emuproject.util.logging.L2Logger;

/**
 * Interprets the given byte/word/dword as a quest state.
 * 
 * @author savormix
 */
public class QuestState extends ScriptedIntegerIdInterpreter implements ContextualFieldValueInterpreter
{
	private static final L2Logger LOG = L2Logger.getLogger(QuestState.class);
	
	private final ThreadLocal<Long> _quest;
	
	/** Constructs this interpreter. */
	public QuestState()
	{
		super(loadInterpretations());
		
		_quest = new ThreadLocal<Long>()
		{
			@Override
			protected Long initialValue()
			{
				return -1L;
			}
		};
	}
	
	private static final Map<Long, String> loadInterpretations()
	{
		final Map<Long, String> mapping = new HashMap<>();
		try (final BufferedReader br = IOConstants.openScriptResource("interpreter", "qstate.txt"))
		{
			for (String line; (line = br.readLine()) != null;)
			{
				final int idx = line.indexOf('\t'), idx2 = line.indexOf('\t', idx + 1);
				if (idx == -1 || idx2 == -1)
					continue;
				
				final long quest = Integer.parseInt(line.substring(0, idx));
				final long state = Integer.parseInt(line.substring(idx + 1, idx2));
				final String desc = line.substring(idx2 + 1);
				
				mapping.put((quest << 32) | (state & 0xFF_FF_FF_FFL), desc.intern());
			}
		}
		catch (IOException e)
		{
			LOG.error("Could not load quest state interpretations", e);
		}
		return mapping.isEmpty() ? Collections.emptyMap() : mapping;
	}
	
	@Override
	public void reviewContext(RandomAccessMMOBuffer buf)
	{
		final List<EnumeratedPayloadField> states = buf.getFieldIndices("__INTERP_QUEST_STATE"), quests = buf.getFieldIndices("__INTERP_QUEST_ID");
		for (int i = 0; i < states.size(); ++i)
		{
			if (states.get(i).getOffset() != buf.getCurrentOffset())
				continue;
			
			_quest.set(buf.readInteger(quests.get(i)));
			return;
		}
		
		// no quest supplied, default to failure
		_quest.set(-1L);
	}
	
	@Override
	public Object getInterpretation(long value, ICacheServerID entityCacheContext)
	{
		try
		{
			int state = (int)value;
			if (state < 0)
			{
				state &= 0x7F_FF_FF_FF;
				state = Integer.toBinaryString(state).length();
			}
			value = (_quest.get() << 32) | state;
			final Object result = super.getInterpretation(value, entityCacheContext);
			if (result instanceof String)
				return result;
			// fallback: interpret as quest level
			return Long.valueOf(state);
		}
		finally
		{
			_quest.set(-1L);
		}
	}
}
