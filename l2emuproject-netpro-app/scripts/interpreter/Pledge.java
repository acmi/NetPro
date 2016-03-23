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
import net.l2emuproject.proxy.network.meta.interpreter.IntegerInterpreter;
import net.l2emuproject.proxy.script.interpreter.ScriptedFieldValueInterpreter;
import net.l2emuproject.proxy.state.entity.PledgeCrestInfo;
import net.l2emuproject.proxy.state.entity.PledgeInfo;
import net.l2emuproject.proxy.state.entity.cache.PledgeCrestInfoCache;
import net.l2emuproject.proxy.state.entity.cache.PledgeInfoCache;
import net.l2emuproject.proxy.state.entity.context.ICacheServerID;

/**
 * Interprets the given byte/word/dword as a pledge ID.
 * 
 * @author savormix
 */
public class Pledge extends ScriptedFieldValueInterpreter implements IntegerInterpreter
{
	@Override
	public Object getInterpretation(long value, ICacheServerID entityCacheContext)
	{
		final PledgeInfo pi = PledgeInfoCache.getInstance().getOrAdd((int)value, entityCacheContext);
		
		final L2TextBuilder sb = new L2TextBuilder();
		if (pi.getCrestID() != 0)
		{
			final PledgeCrestInfo pci = PledgeCrestInfoCache.getInstance().getOrAdd(pi.getCrestID(), entityCacheContext);
			if (pci.getCrestImgSrc() != null)
				sb.append("<img src=\"").append(pci.getCrestImgSrc()).append("\" border=\"0\" />");
		}
		sb.append(pi.getName());
		
		return sb.toString();
	}
}
