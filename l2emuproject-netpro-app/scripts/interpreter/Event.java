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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import net.l2emuproject.network.protocol.IProtocolVersion;
import net.l2emuproject.proxy.script.interpreter.ScriptedIntegerIdInterpreter;

/**
 * Interprets the given byte/word/dword as an event type.
 * 
 * @author savormix
 */
public class Event extends ScriptedIntegerIdInterpreter
{
	/** Constructs this interpreter. */
	public Event()
	{
		super(build());
	}
	
	private static final Map<IProtocolVersion, Map<Long, ?>> build()
	{
		final Map<IProtocolVersion, Map<Long, ?>> wrapper = new HashMap<>();
		final Map<Long, String> result = new HashMap<>();
		result.put(20090401L, "April fool's 2009");
		result.put(20090801L, "Eva's inferno");
		result.put(20091031L, "Haloween 2009");
		result.put(20091225L, "Raising rudolph");
		result.put(20100214L, "Lover's jubilee");
		result.put(20100401L, "Player commendation");
		wrapper.put(null, Collections.unmodifiableMap(result));
		return Collections.unmodifiableMap(wrapper);
	}
}
