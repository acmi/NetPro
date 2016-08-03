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

import net.l2emuproject.geometry.IRotatable;
import net.l2emuproject.geometry.point.impl.Point3D;

/**
 * Represents a game world object's location.
 * 
 * @author _dev_
 */
public final class ObjectLocation extends Point3D implements IRotatable
{
	private static final long serialVersionUID = 6987849734512077341L;
	
	/** Indicates that a location has not yet been set for a world object descriptor. */
	public static final ObjectLocation UNKNOWN_LOCATION = new ObjectLocation(Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE);
	
	private final int _yaw;
	
	/**
	 * Creates a world object location, including the object's rotation value.
	 * 
	 * @param x Coordinate X
	 * @param y Coordinate Y
	 * @param z Coordinate Z
	 * @param yaw rotation value (degrees * 182)
	 */
	public ObjectLocation(int x, int y, int z, int yaw)
	{
		super(x, y, z);
		
		_yaw = yaw;
	}
	
	@Override
	public int getDirection()
	{
		return _yaw / 182;
	}
	
	@Override
	public int getYaw()
	{
		return _yaw;
	}
	
	@Override
	public String toString()
	{
		return super.toString() + " " + _yaw;
	}
}
