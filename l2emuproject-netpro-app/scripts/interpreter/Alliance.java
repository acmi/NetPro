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

import net.l2emuproject.proxy.network.meta.interpreter.IntegerInterpreter;
import net.l2emuproject.proxy.script.interpreter.ScriptedFieldValueInterpreter;
import net.l2emuproject.proxy.state.entity.AllianceCrestInfo;
import net.l2emuproject.proxy.state.entity.AllianceInfo;
import net.l2emuproject.proxy.state.entity.cache.AllianceCrestInfoCache;
import net.l2emuproject.proxy.state.entity.cache.AllianceInfoCache;
import net.l2emuproject.proxy.state.entity.context.ICacheServerID;

/**
 * Interprets the given byte/word/dword as an alliance ID.
 * 
 * @author savormix
 */
public class Alliance extends ScriptedFieldValueInterpreter implements IntegerInterpreter
{
	@Override
	public Object getInterpretation(long value, ICacheServerID entityCacheContext)
	{
		final AllianceInfo ai = AllianceInfoCache.getInstance().getOrAdd((int)value, entityCacheContext);
		
		final StringBuilder sb = new StringBuilder();
		if (ai.getCrestID() != 0)
		{
			final AllianceCrestInfo aci = AllianceCrestInfoCache.getInstance().getOrAdd(ai.getCrestID(), entityCacheContext);
			if (aci.getCrestImgSrc() != null)
				sb.append("<img src=\"").append(aci.getCrestImgSrc()).append("\" border=\"0\" />");
		}
		sb.append(ai.getName());
		
		return sb.toString();
	}
}
