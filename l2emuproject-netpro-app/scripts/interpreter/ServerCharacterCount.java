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

import net.l2emuproject.lang.L2TextBuilder;
import net.l2emuproject.network.protocol.IProtocolVersion;
import net.l2emuproject.proxy.network.meta.container.MetaclassRegistry;
import net.l2emuproject.proxy.network.meta.exception.InvalidFieldValueInterpreterException;
import net.l2emuproject.proxy.network.meta.interpreter.ByteArrayTranslator;
import net.l2emuproject.proxy.network.meta.interpreter.IntegerTranslator;
import net.l2emuproject.proxy.script.interpreter.ScriptedFieldValueInterpreter;
import net.l2emuproject.proxy.state.entity.context.ICacheServerID;

/**
 * @author _dev_
 */
public class ServerCharacterCount extends ScriptedFieldValueInterpreter implements ByteArrayTranslator
{
	@Override
	public Object translate(byte[] value, IProtocolVersion protocol, ICacheServerID entityCacheContext)
	{
		if (value.length < 1 || value[0] == 0)
			return "None";
			
		IntegerTranslator interp = null;
		try
		{
			interp = MetaclassRegistry.getInstance().getTranslator("GameServerName", IntegerTranslator.class);
		}
		catch (InvalidFieldValueInterpreterException e)
		{
			// whatever, template ID will suffice
		}
		
		final L2TextBuilder tb = new L2TextBuilder();
		for (int i = 1; i < value.length; i += 2)
		{
			final int id = value[i - 1], cnt = value[i];
			if (id == 0)
				break;
				
			tb.append(cnt).append(" in ");
			if (interp != null)
				tb.append(interp.translate(id, protocol, entityCacheContext));
			else
				tb.append("Server").append(id);
			tb.append(", ");
		}
		return tb.setLength(tb.length() - 2).moveToString();
	}
}
