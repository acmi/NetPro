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

import net.l2emuproject.network.protocol.IProtocolVersion;
import net.l2emuproject.proxy.network.meta.RandomAccessMMOBuffer;
import net.l2emuproject.proxy.network.meta.interpreter.ContextualFieldValueTranslator;
import net.l2emuproject.proxy.network.meta.interpreter.IntegerTranslator;
import net.l2emuproject.proxy.script.interpreter.ScriptedIntegerIdInterpreter;
import net.l2emuproject.proxy.state.entity.context.ICacheServerID;

/**
 * @author _dev_
 */
public final class FlyPathNode extends ScriptedIntegerIdInterpreter implements ContextualFieldValueTranslator, IntegerTranslator
{
	private final ThreadLocal<Integer> _path;
	
	/** Constructs this interpreter. */
	public FlyPathNode()
	{
		super(loadFromResource2("fly.txt"));
		
		_path = new ThreadLocal<Integer>()
		{
			@Override
			protected Integer initialValue()
			{
				return 0;
			}
		};
	}
	
	@Override
	public void reviewContext(RandomAccessMMOBuffer buf)
	{
		_path.set(buf.readFirstInteger32("__INTERP_ENABLING_FLY_PATH"));
	}
	
	@Override
	public Object translate(long value, IProtocolVersion protocol, ICacheServerID entityCacheContext)
	{
		if (value == -1)
			return "Ground level";
		
		final Object result = super.translate((_path.get().longValue() << 32) | value, protocol, entityCacheContext);
		return result instanceof String ? result : value;
	}
}
