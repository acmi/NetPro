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
 * Represents an entity (character, npc, pledge, alliance, etc.).
 * 
 * @author savormix
 */
public abstract class EntityInfo
{
	private final int _id;
	private String _name;
	
	/**
	 * Creates an entity with the given ID. An ID is only required to be unique among entities of the same supertype.
	 * 
	 * @param id entity ID
	 */
	protected EntityInfo(int id)
	{
		_id = id;
		_name = "N/A";
	}
	
	/**
	 * Describes this entity in a developer-friendly string.<BR>
	 * Used in error reporting.
	 * 
	 * @return key information about this entity
	 */
	public String describe()
	{
		final StringBuilder sb = new StringBuilder();
		sb.append('[').append(getClass().getSimpleName()).append("] ").append(getName());
		return sb.toString();
	}
	
	/**
	 * An unique ID for this entity.
	 * 
	 * @return entity ID
	 */
	public int getID()
	{
		return _id;
	}
	
	/**
	 * Returns a user-friendly name of this entity.
	 * 
	 * @return entity name
	 */
	public String getName()
	{
		return _name;
	}
	
	/**
	 * Changes (updates) the name of this entity.
	 * 
	 * @param name entity name
	 */
	public void setName(String name)
	{
		_name = name;
	}
	
	/**
	 * Release any system resources used by this entity info object.<BR>
	 * <BR>
	 * This method is called right after a related packet log was closed.
	 */
	public void release()
	{
		// do nothing by default
	}
	
	@Override
	public String toString()
	{
		return _id + ": " + _name;
	}
}
