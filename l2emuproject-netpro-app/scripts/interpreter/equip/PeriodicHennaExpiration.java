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
package interpreter.equip;

import net.l2emuproject.network.protocol.IProtocolVersion;
import net.l2emuproject.proxy.network.meta.container.MetaclassRegistry;
import net.l2emuproject.proxy.network.meta.exception.InvalidFieldValueInterpreterException;
import net.l2emuproject.proxy.network.meta.interpreter.IntegerTranslator;
import net.l2emuproject.proxy.network.meta.interpreter.impl.SecondsSinceEpoch;
import net.l2emuproject.proxy.network.meta.interpreter.impl.TimeRemaining;
import net.l2emuproject.proxy.script.interpreter.ScriptedFieldValueInterpreter;
import net.l2emuproject.proxy.state.entity.context.ICacheServerID;

/**
 * @author _dev_
 */
public final class PeriodicHennaExpiration extends ScriptedFieldValueInterpreter implements IntegerTranslator
{
	private static final IntegerTranslator TIMESTAMP, DIFFERENCE;
	static
	{
		try
		{
			TIMESTAMP = MetaclassRegistry.getInstance().getTranslator(SecondsSinceEpoch.class.getSimpleName(), IntegerTranslator.class);
			DIFFERENCE = MetaclassRegistry.getInstance().getTranslator(TimeRemaining.class.getSimpleName(), IntegerTranslator.class);
		}
		catch (InvalidFieldValueInterpreterException e)
		{
			// missing internal interpreters? I don't think so.
			throw new IllegalStateException(e);
		}
	}
	
	@Override
	public Object translate(long value, IProtocolVersion protocol, ICacheServerID entityCacheContext)
	{
		if (value > 1_000_000_000) // initial packet
			return TIMESTAMP.translate(value, protocol, entityCacheContext);
		
		return DIFFERENCE.translate(value, protocol, entityCacheContext);
	}
}
