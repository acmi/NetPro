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

import eu.revengineer.simplejse.type.UnloadableScript;

import net.l2emuproject.proxy.network.meta.container.MetaclassRegistry;
import net.l2emuproject.proxy.network.meta.interpreter.impl.SecondsSinceEpoch;
import net.l2emuproject.proxy.script.ScriptedMetaclass;
import net.l2emuproject.proxy.state.entity.context.ICacheServerID;

/**
 * Interprets the given byte/word/dword as a siege time declaration. <BR>
 * <BR>
 * If the given value is zero/negative, it means the siege time is not yet chosen and, if nothing
 * changes, will be chosen automatically.
 * 
 * @author savormix
 */
public class DeclaredSiegeTime extends SecondsSinceEpoch implements UnloadableScript
{
	@Override
	public Object getInterpretation(long value, ICacheServerID entityCacheContext)
	{
		if (value <= 0)
			return "Undecided/Automatic";
		
		return super.getInterpretation(value, entityCacheContext);
	}
	
	@Override
	public String getName()
	{
		return getClass().getSimpleName();
	}
	
	@Override
	public void onLoad() throws RuntimeException
	{
		MetaclassRegistry.getInstance().register(ScriptedMetaclass.getAlias(getClass()), this);
	}
	
	@Override
	public void onUnload() throws RuntimeException
	{
		MetaclassRegistry.getInstance().remove(ScriptedMetaclass.getAlias(getClass()), this);
	}
}
