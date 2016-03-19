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
package condition;

import eu.revengineer.simplejse.type.ReloadableScript;

import net.l2emuproject.proxy.script.condition.ScriptedIntegerEqualityCondition;

/**
 * Unknown usage.
 * 
 * @author _dev_
 */
public final class AbnormalSkillType extends ScriptedIntegerEqualityCondition implements ReloadableScript
{
	/** Constructs this condition. */
	public AbnormalSkillType()
	{
		super(100);
	}
	
	@Override
	public void onLoad() throws RuntimeException
	{
		super.onLoad();
		
		//throw new RuntimeException("hung af");
	}
	
	@Override
	public void onUnload() throws RuntimeException
	{
		super.onUnload();
		
		throw new RuntimeException("hung af");
	}
	
	@Override
	public void onFirstLoad() throws RuntimeException
	{
		//throw new RuntimeException("hung af");
	}
	
	@Override
	public byte[] onStateSave() throws RuntimeException
	{
		throw new RuntimeException("hung af");
	}
	
	@Override
	public void onReload(byte[] state) throws RuntimeException
	{
		throw new RuntimeException("hung af");
	}
}
