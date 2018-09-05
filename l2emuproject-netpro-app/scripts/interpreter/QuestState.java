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

import java.util.List;

import net.l2emuproject.network.protocol.IProtocolVersion;
import net.l2emuproject.proxy.network.meta.EnumeratedPayloadField;
import net.l2emuproject.proxy.network.meta.RandomAccessMMOBuffer;
import net.l2emuproject.proxy.network.meta.interpreter.ContextualFieldValueTranslator;
import net.l2emuproject.proxy.script.interpreter.ScriptedIntegerIdInterpreter;
import net.l2emuproject.proxy.state.entity.context.ICacheServerID;

/**
 * Interprets the given byte/word/dword as a quest state.
 * 
 * @author savormix
 */
public class QuestState extends ScriptedIntegerIdInterpreter implements ContextualFieldValueTranslator
{
	private final ThreadLocal<Long> _quest;
	
	/** Constructs this interpreter. */
	public QuestState()
	{
		super(loadFromResource2("qstate.txt"));
		
		_quest = new ThreadLocal<Long>()
		{
			@Override
			protected Long initialValue()
			{
				return -1L;
			}
		};
	}
	
	public Object translate(int questID, int value, IProtocolVersion protocol)
	{
		_quest.set(Long.valueOf(questID));
		return translate(value, protocol, null);
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
	public Object translate(long value, IProtocolVersion protocol, ICacheServerID entityCacheContext)
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
			final Object result = super.translate(value, protocol, entityCacheContext);
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
