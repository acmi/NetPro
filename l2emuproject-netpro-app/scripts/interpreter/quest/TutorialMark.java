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

import java.util.List;

import net.l2emuproject.network.protocol.IProtocolVersion;
import net.l2emuproject.proxy.network.meta.EnumeratedPayloadField;
import net.l2emuproject.proxy.network.meta.RandomAccessMMOBuffer;
import net.l2emuproject.proxy.network.meta.interpreter.ContextualFieldValueTranslator;
import net.l2emuproject.proxy.script.interpreter.ScriptedIntegerIdInterpreter;
import net.l2emuproject.proxy.state.entity.context.ICacheServerID;

/**
 * Interprets the given byte/word/dword as a tutorial mark ID.
 * 
 * @author _dev_
 */
public class TutorialMark extends ScriptedIntegerIdInterpreter implements ContextualFieldValueTranslator
{
	private final ThreadLocal<Integer> _type;
	
	/** Constructs this interpreter. */
	public TutorialMark()
	{
		super(loadFromResource2("tutorial.txt"));
		
		_type = new ThreadLocal<Integer>() {
			@Override
			protected Integer initialValue()
			{
				return 1; // default to quest
			}
		};
	}
	
	@Override
	public void reviewContext(RandomAccessMMOBuffer buf)
	{
		final List<EnumeratedPayloadField> types = buf.getFieldIndices("__INTERP_TUTORIAL_MARK_TYPE"), marks = buf.getFieldIndices("__INTERP_TUTORIAL_MARK");
		for (int i = 0; i < marks.size(); ++i)
		{
			if (marks.get(i).getOffset() != buf.getCurrentOffset())
				continue;
			
			_type.set(buf.readInteger32(types.get(i)));
			return;
		}
		
		_type.set(1);
	}
	
	@Override
	public Object translate(long value, IProtocolVersion protocol, ICacheServerID entityCacheContext)
	{
		try
		{
			final long mark = value;
			value = mark | (_type.get().longValue() << 32);
			final Object result = super.translate(value, protocol, entityCacheContext);
			if (result instanceof String)
				return result;
			return Long.valueOf(mark);
		}
		finally
		{
			_type.set(1);
		}
	}
}
