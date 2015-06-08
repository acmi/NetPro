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
package net.l2emuproject.proxy.state.entity;

/**
 * Stores info about an entity that has a crest.
 * 
 * @author savormix
 */
public abstract class EntityInfoWithCrest extends EntityInfo
{
	private int _crestID;
	
	/**
	 * Creates an entity descriptor.
	 * 
	 * @param id entity ID
	 */
	public EntityInfoWithCrest(int id)
	{
		super(id);
	}
	
	/**
	 * Returns the associated crest ID.
	 * 
	 * @return crest ID
	 */
	public int getCrestID()
	{
		return _crestID;
	}
	
	/**
	 * Sets the associated crest ID.
	 * 
	 * @param crestID crest ID
	 */
	public void setCrestID(int crestID)
	{
		_crestID = crestID;
	}
}
