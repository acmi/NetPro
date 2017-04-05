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

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.l2emuproject.proxy.network.meta.EnumeratedPayloadField;
import net.l2emuproject.proxy.network.meta.RandomAccessMMOBuffer;
import net.l2emuproject.proxy.network.meta.interpreter.ContextualFieldValueInterpreter;
import net.l2emuproject.proxy.script.interpreter.ScriptedIntegerIdInterpreter;
import net.l2emuproject.proxy.state.entity.context.ICacheServerID;
import net.l2emuproject.proxy.ui.savormix.io.base.IOConstants;
import net.l2emuproject.util.logging.L2Logger;

/**
 * Interprets the given byte/word/dword as a quest state.
 * 
 * @author savormix
 */
public abstract class BeautyTranslator extends ScriptedIntegerIdInterpreter implements ContextualFieldValueInterpreter
{
	private static final L2Logger LOG = L2Logger.getLogger(BeautyTranslator.class);
	
	/** Class name to use. */
	protected final ThreadLocal<Long> _className;
	
	/**
	 * Constructs this translator.
	 * 
	 * @param filename mapping file name
	 */
	protected BeautyTranslator(String filename)
	{
		super(loadInterpretations(filename));
		
		_className = new ThreadLocal<Long>(){
			@Override
			protected Long initialValue()
			{
				return -1L;
			}
		};
	}
	
	private static final Map<Long, String> loadInterpretations(String filename)
	{
		final Map<Long, String> mapping = new HashMap<>();
		try (final BufferedReader br = IOConstants.openScriptResource("interpreter", filename))
		{
			for (String line; (line = br.readLine()) != null;)
			{
				final int idx = line.indexOf('\t'), idx2 = line.indexOf('\t', idx + 1);
				if (idx == -1 || idx2 == -1)
					continue;
				
				final long className = Integer.parseInt(line.substring(0, idx));
				final long id = Integer.parseInt(line.substring(idx + 1, idx2));
				final String name = line.substring(idx2 + 1);
				
				mapping.put((className << 32) | (id & 0xFF_FF_FF_FFL), name.intern());
			}
		}
		catch (final IOException e)
		{
			LOG.error("Could not load beauty shop color interpretations", e);
		}
		return mapping.isEmpty() ? Collections.emptyMap() : mapping;
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
	public Object getInterpretation(long value, ICacheServerID entityCacheContext)
	{
		try
		{
			value = (_className.get() << 32) | value;
			final Object result = super.getInterpretation(value, entityCacheContext);
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
