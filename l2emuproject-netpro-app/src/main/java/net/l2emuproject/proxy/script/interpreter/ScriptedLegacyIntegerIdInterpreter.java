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
package net.l2emuproject.proxy.script.interpreter;

import java.util.HashMap;
import java.util.Map;

import net.l2emuproject.network.protocol.IProtocolVersion;
import net.l2emuproject.proxy.network.meta.interpreter.IntegerTranslator;
import net.l2emuproject.proxy.state.entity.context.ICacheServerID;

/**
 * Allows legacy interpreters to be used for the time being.
 * 
 * @author _dev_
 */
@Deprecated
public abstract class ScriptedLegacyIntegerIdInterpreter extends ScriptedFieldValueInterpreter implements IntegerTranslator
{
	private final Map<Long, String> _interpretations;
	
	/** Constructs this interpreter. */
	public ScriptedLegacyIntegerIdInterpreter()
	{
		_interpretations = new HashMap<Long, String>();
		loadImpl();
	}
	
	protected abstract void loadImpl();
	
	protected final void addInterpretation(int id, String interpretation)
	{
		_interpretations.put(Long.valueOf(id), interpretation);
	}
	
	@Override
	public Object translate(long value, IProtocolVersion protocol, ICacheServerID entityCacheContext)
	{
		return _interpretations.get(value);
	}
}
