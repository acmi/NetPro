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

import net.l2emuproject.geometry.IPoint2D;
import net.l2emuproject.geometry.PointGeometry;
import net.l2emuproject.proxy.state.entity.type.EntityWithTemplateType;
import net.l2emuproject.proxy.state.entity.type.ObjectType;
import net.l2emuproject.util.logging.L2Logger;

/**
 * Stores info about a game world object.
 * 
 * @author savormix
 */
public class ObjectInfo extends EntityInfo
{
	private static final L2Logger LOG = L2Logger.getLogger(ObjectInfo.class);
	
	private ObjectType _type;
	private volatile boolean _running;
	private volatile int _walkSpeed, _runSpeed;
	private volatile double _speedMultiplier;
	private volatile ObjectLocation _location, _destination;
	private volatile long _movementStart;
	
	/**
	 * Creates a game world object descriptor.
	 * 
	 * @param id unique world object ID
	 */
	public ObjectInfo(int id)
	{
		super(id);
		
		_running = true; // currently, only players are tracked
		_walkSpeed = _runSpeed = 50;
		_speedMultiplier = 1D;
		_location = _destination = ObjectLocation.UNKNOWN_LOCATION;
		_movementStart = -1;
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
	public ObjectInfo setType(ObjectType type)
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
	 * Sets the object's movement mode.
	 * 
	 * @param running whether this actor is running
	 * @return {@code this}
	 */
	public ObjectInfo setRunning(boolean running)
	{
		if (_running == running)
			return this;
		
		updateLocation(getCurrentLocation());
		_running = running;
		return this;
	}
	
	/**
	 * Sets the object's movement speed.
	 * 
	 * @param walkSpeed speed while walking
	 * @param runSpeed speed while running
	 * @return {@code this}
	 */
	public ObjectInfo setMovementSpeed(int walkSpeed, int runSpeed)
	{
		if (_running && _runSpeed == runSpeed)
		{
			_walkSpeed = walkSpeed;
			return this;
		}
		if (!_running && _walkSpeed == walkSpeed)
		{
			_runSpeed = runSpeed;
			return this;
		}
		
		updateLocation(getCurrentLocation());
		_walkSpeed = walkSpeed;
		_runSpeed = runSpeed;
		return this;
	}
	
	/**
	 * Sets the object's movement speed multiplier.
	 * 
	 * @param speedMultiplier speed multiplier
	 * @return {@code this}
	 */
	public ObjectInfo setSpeedMultiplier(double speedMultiplier)
	{
		if (_speedMultiplier == speedMultiplier)
			return this;
		
		updateLocation(getCurrentLocation());
		_speedMultiplier = speedMultiplier;
		return this;
	}
	
	/**
	 * Sets the location of this world object.
	 * 
	 * @param location 3D coordinates
	 * @return {@code this}
	 */
	public ObjectInfo updateLocation(ObjectLocation location)
	{
		if (getID() == 0)
			LOG.warn("Trying to set location of nonexistent object", new RuntimeException());
		
		_location = location;
		_movementStart = System.nanoTime();
		return this;
	}
	
	/**
	 * Sets the destination of this world object.
	 * 
	 * @param location 3D coordinates of the current location
	 * @param destination 3D coordinates of the destination
	 * @return {@code this}
	 */
	public ObjectInfo setDestination(ObjectLocation location, ObjectLocation destination)
	{
		if (getID() == 0)
			LOG.warn("Trying to set destination of nonexistent object", new RuntimeException());
		
		updateLocation(location);
		_destination = location.equals(destination) ? ObjectLocation.UNKNOWN_LOCATION : destination;
		
		//if (_destination != ObjectLocation.UNKNOWN_LOCATION && getName().startsWith("YOUR_NAME_HERE "))
		//	L2ThreadPool.schedule(new DebugWriter(), 100);
		
		return this;
	}
	
	/*
	final class DebugWriter implements Runnable
	{
		@Override
		public void run()
		{
			if (_destination == ObjectLocation.UNKNOWN_LOCATION)
				return;
			
			final double distanceLeftToTravel = PointGeometry.getRawPlanarDistance(_location, _destination);
			final double distanceTraveled = getMovementSpeed() * ((System.nanoTime() - _movementStart) / 1_000_000_000D);
			LOG.info("Travelled " + distanceTraveled + " in " + ((System.nanoTime() - _movementStart) / 1_000_000_000D) + " seconds");
			if (distanceTraveled >= distanceLeftToTravel)
			{
				LOG.info("Finally arrived");
				return;
			}
			
			final IPoint2D loc = PointGeometry.getNextPointInPlanarSegment(_location, _destination, distanceTraveled);
			LOG.info("Apparently now at " + loc.getX() + " " + loc.getY());
			
			L2ThreadPool.schedule(this, 50);
		}
	}
	*/
	/**
	 * Returns the current movement destination for this object.
	 * 
	 * @return movement destination
	 */
	public ObjectLocation getDestination()
	{
		return _destination;
	}
	
	/**
	 * Returns the location of this world object.
	 * 
	 * @return 3D coordinates
	 */
	public ObjectLocation getCurrentLocation()
	{
		if (_destination == ObjectLocation.UNKNOWN_LOCATION)
			return _location;
		
		if (isAtDestination())
			return _destination;
		
		final double distanceTraveled = getMovementSpeed() * (System.nanoTime() - _movementStart) / 1_000_000_000L;
		final IPoint2D loc = PointGeometry.getNextPointInPlanarSegment(_location, _destination, distanceTraveled);
		return new ObjectLocation(loc.getX(), loc.getY(), _location.getZ(), _location.getYaw());
	}
	
	/**
	 * Returns whether this object is currently moving or not.
	 * 
	 * @return is object moving
	 */
	public boolean isMoving()
	{
		return _destination != ObjectLocation.UNKNOWN_LOCATION && !isAtDestination();
	}
	
	private boolean isAtDestination()
	{
		final double distanceLeftToTravel = PointGeometry.getRawPlanarDistance(_location, _destination);
		final double distanceTraveled = getMovementSpeed() * (System.nanoTime() - _movementStart) / 1_000_000_000L;
		if (distanceTraveled >= distanceLeftToTravel)
			return true;
		
		return _location.getX() == _destination.getX() && _location.getY() == _destination.getY();
	}
	
	/**
	 * Returns object's movement speed.
	 * 
	 * @return movement speed
	 */
	public double getMovementSpeed()
	{
		return _speedMultiplier * (_running ? _runSpeed : _walkSpeed);
	}
}
