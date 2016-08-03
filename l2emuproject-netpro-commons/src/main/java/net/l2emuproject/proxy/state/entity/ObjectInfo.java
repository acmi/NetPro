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

import net.l2emuproject.proxy.state.entity.type.EntityWithTemplateType;
import net.l2emuproject.proxy.state.entity.type.ObjectType;
import net.l2emuproject.util.logging.L2Logger;

/**
 * Stores info about a game world object.
 * 
 * @author savormix
 * @param <T> game-specific data wrapper type
 */
public class ObjectInfo<T> extends EntityInfo
{
	private static final L2Logger LOG = L2Logger.getLogger(ObjectInfo.class);
	
	private ObjectType _type;
	private final T _extraInfo;
	
	/**
	 * Creates a game world object descriptor.
	 * 
	 * @param id unique world object ID
	 * @param extraInfo game specific data wrapper
	 */
	public ObjectInfo(int id, T extraInfo)
	{
		super(id);
		
		_extraInfo = extraInfo;
	}
	
	@Override
	public void setName(String name)
	{
		final StringBuilder sb = new StringBuilder();
		sb.append(name).append(" [").append(_type.describe()).append(']');
		super.setName(sb.toString());
	}
	
	/**
	 * Returns the template ID of this object. Template ID is not the class ID: if two objects of different type may share the same template ID and be completely different.
	 * 
	 * @return object template ID
	 */
	public int getTemplateID()
	{
		final ObjectType type = getType();
		if (!(type instanceof EntityWithTemplateType))
			return -1;
		
		return ((EntityWithTemplateType)type).getTemplateID();
	}
	
	/**
	 * Sets the type of this world object.
	 * 
	 * @param type world object type
	 * @return {@code this}
	 */
	public ObjectInfo<T> setType(ObjectType type)
	{
		if (getID() == 0)
			LOG.warn("Trying to set type of nonexistent object", new RuntimeException());
		
		_type = type;
		return this;
	}
	
	/**
	 * Returns the type of this world object.
	 * 
	 * @return world object type
	 */
	public ObjectType getType()
	{
		return _type;
	}
	
	/**
	 * Returns game specific data for this object.
	 * 
	 * @return extra data wrapper
	 */
	public T getExtraInfo()
	{
		return _extraInfo;
	}
}
