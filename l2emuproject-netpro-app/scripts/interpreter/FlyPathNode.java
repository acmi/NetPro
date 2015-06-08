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
import java.util.HashMap;
import java.util.Map;

import net.l2emuproject.proxy.network.meta.RandomAccessMMOBuffer;
import net.l2emuproject.proxy.network.meta.interpreter.ContextualFieldValueInterpreter;
import net.l2emuproject.proxy.network.meta.interpreter.IntegerInterpreter;
import net.l2emuproject.proxy.script.interpreter.ScriptedIntegerIdInterpreter;
import net.l2emuproject.proxy.state.entity.context.ICacheServerID;
import net.l2emuproject.proxy.ui.savormix.io.base.IOConstants;
import net.l2emuproject.util.logging.L2Logger;

/**
 * @author _dev_
 */
public final class FlyPathNode extends ScriptedIntegerIdInterpreter implements ContextualFieldValueInterpreter, IntegerInterpreter
{
	private static final L2Logger LOG = L2Logger.getLogger(FlyPathNode.class);
	
	private final ThreadLocal<Integer> _path;
	
	/** Constructs this interpreter. */
	public FlyPathNode()
	{
		super(loadInterpretations());
		
		_path = new ThreadLocal<Integer>()
		{
			@Override
			protected Integer initialValue()
			{
				return 0;
			}
		};
	}
	
	private static final Map<Long, String> loadInterpretations()
	{
		final Map<Long, String> mapping = new HashMap<>();
		try (final BufferedReader br = IOConstants.openScriptResource("interpreter", "fly.txt"))
		{
			for (String line; (line = br.readLine()) != null;)
			{
				final int idx = line.indexOf('\t'), idx2 = line.indexOf('\t', idx + 1);
				if (idx == -1 || idx2 == -1)
					continue;
				
				final long path = Integer.parseInt(line.substring(0, idx));
				final int node = Integer.parseInt(line.substring(idx + 1, idx2));
				final String name = line.substring(idx2 + 1);
				
				mapping.put(Long.valueOf((path << 32) | node), name);
			}
		}
		catch (IOException e)
		{
			LOG.error("Could not load sayune interpretations", e);
		}
		return mapping;
	}
	
	@Override
	public void reviewContext(RandomAccessMMOBuffer buf)
	{
		_path.set(buf.readFirstInteger32("__INTERP_ENABLING_FLY_PATH"));
	}
	
	@Override
	public Object getInterpretation(long value, ICacheServerID entityCacheContext)
	{
		if (value == -1)
			return "Ground level";
		
		final Object result = super.getInterpretation((_path.get().longValue() << 32) | value, entityCacheContext);
		return result instanceof String ? result : value;
	}
}
