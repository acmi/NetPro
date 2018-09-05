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

import eu.revengineer.simplejse.HasScriptDependencies;

import net.l2emuproject.lang.L2TextBuilder;
import net.l2emuproject.network.protocol.IProtocolVersion;
import net.l2emuproject.proxy.state.entity.context.ICacheServerID;

/**
 * Used to preserve tags in L2 HTMLs.
 * 
 * @author savormix
 */
@HasScriptDependencies("interpreter.PreformattedHtml")
public class CommunityBoardHtml extends PreformattedHtml
{
	@Override
	public Object translate(String value, IProtocolVersion protocol, ICacheServerID entityCacheContext)
	{
		final L2TextBuilder tb = new L2TextBuilder();
		int contentIdx = 0;
		for (int idx = 0; (idx = value.indexOf(8, contentIdx) + 1) != 0; contentIdx = idx)
			tb.append('[').append(value.substring(contentIdx, idx)).append(']');
		return tb.append(super.translate(value.substring(contentIdx), protocol, entityCacheContext)).moveToString();
	}
}
