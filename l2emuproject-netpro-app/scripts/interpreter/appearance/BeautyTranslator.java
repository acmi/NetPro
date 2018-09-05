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
package interpreter.appearance;

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
public abstract class BeautyTranslator extends ScriptedIntegerIdInterpreter implements ContextualFieldValueTranslator
{
	/** Class name to use. */
	protected final ThreadLocal<Long> _className;
	
	/**
	 * Constructs this translator.
	 * 
	 * @param filename mapping file name
	 */
	protected BeautyTranslator(String filename)
	{
		super(loadFromResource2(filename));
		
		_className = new ThreadLocal<Long>(){
			@Override
			protected Long initialValue()
			{
				return -1L;
			}
		};
	}
	
	@Override
	public void reviewContext(RandomAccessMMOBuffer buf)
	{
		final List<EnumeratedPayloadField> classNames = buf.getFieldIndices("__INTERP_BEAUTY_CLASS_NAME"), ids = buf.getFieldIndices("__INTERP_BEAUTY_ID");
		for (int i = 0; i < ids.size(); ++i)
		{
			if (ids.get(i).getOffset() != buf.getCurrentOffset())
				continue;
			
			_className.set(buf.readInteger(classNames.get(i)));
			return;
		}
		
		_className.set(-1L);
	}
	
	@Override
	public Object translate(long value, IProtocolVersion protocol, ICacheServerID entityCacheContext)
	{
		try
		{
			value = (_className.get() << 32) | value;
			final Object result = super.translate(value, protocol, entityCacheContext);
			if (result instanceof String)
				return result;
			// fallback: interpret as ID
			return Long.valueOf(value);
		}
		finally
		{
			_className.set(-1L);
		}
	}
}
