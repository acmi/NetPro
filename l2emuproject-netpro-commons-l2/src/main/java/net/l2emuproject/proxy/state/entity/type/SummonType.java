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
package net.l2emuproject.proxy.state.entity.type;

/**
 * Defines a world object type – summon.
 * 
 * @author _dev_
 */
public class SummonType extends EntityWithTemplateType
{
	/**
	 * Creates a summon descriptor.
	 * 
	 * @param templateID summon template/npc class ID
	 */
	public SummonType(int templateID)
	{
		super(templateID > 1_000_000 ? templateID - 1_000_000 : templateID);
	}
	
	@Override
	public String describe()
	{
		return "Summon";
	}
}
