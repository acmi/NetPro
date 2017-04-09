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

import net.l2emuproject.geometry.point.IPoint2D;
import net.l2emuproject.geometry.point.PointGeometry;

/**
 * Stores info about a game world object in Lineage II.
 * 
 * @author _dev_
 */
public class L2ObjectInfo
{
	private static final long STOP_MOVE_GRACE_PERIOD = 500_000_000;
	private static final int[] INVALID_MOVE_SPEED = new int[3];
	
	private int _targetOID;
	
	private boolean _running;
	private int _environment;
	private double _speedMultiplier;
	private int[] _templateWalkSpeed, _templateRunSpeed;
	
	private ObjectLocation _location, _destination;
	private Long _movementStart;
	
	private Long _stopMoveReceivalTime;
	private boolean _teleporting, _lastMovementInterruptedByTeleport;
	
	/** Constructs this object. */
	public L2ObjectInfo()
	{
		_targetOID = 0; // no target
		
		_running = false; // walking
		_environment = 0; // ground
		_speedMultiplier = 0D;
		_templateWalkSpeed = INVALID_MOVE_SPEED;
		_templateRunSpeed = INVALID_MOVE_SPEED;
		
		_location = _destination = ObjectLocation.UNKNOWN_LOCATION;
		_movementStart = null;
		
		_stopMoveReceivalTime = null;
		_teleporting = false;
		_lastMovementInterruptedByTeleport = false;
	}
	
	/**
	 * Returns the current target of this object.
	 * 
	 * @return target ID
	 */
	public int getTargetOID()
	{
		return _targetOID;
	}
	
	/**
	 * Sets the target of this object.
	 * 
	 * @param targetOID target ID
	 */
	public void setTargetOID(int targetOID)
	{
		_targetOID = targetOID;
	}
	
	/**
	 * Sets the object's movement mode.
	 * 
	 * @param running whether this actor is running
	 * @return {@code this}
	 */
	public L2ObjectInfo setRunning(boolean running)
	{
		if (_running == running)
			return this;
		
		updateLocation(getCurrentLocation());
		_running = running;
		return this;
	}
	
	/**
	 * Sets the object's movement environment (Ground/Water/Air).
	 * 
	 * @param environment ground/water/air
	 * @return {@code this}
	 */
	public L2ObjectInfo setEnvironment(int environment)
	{
		if (_environment == environment)
			return this;
		
		updateLocation(getCurrentLocation());
		_environment = environment;
		return this;
	}
	
	/**
	 * Sets the object template's movement speed.
	 * 
	 * @param walkSpeed speed while walking
	 * @param runSpeed speed while running
	 * @return {@code this}
	 */
	public L2ObjectInfo setTemplateMovementSpeed(int[] walkSpeed, int[] runSpeed)
	{
		if (_running)
		{
			if (_templateRunSpeed[_environment] == runSpeed[_environment])
			{
				_templateWalkSpeed = walkSpeed;
				_templateRunSpeed = runSpeed;
				return this;
			}
		}
		else
		{
			if (_templateWalkSpeed[_environment] == walkSpeed[_environment])
			{
				_templateWalkSpeed = walkSpeed;
				_templateRunSpeed = runSpeed;
				return this;
			}
		}
		
		updateLocation(getCurrentLocation());
		_templateWalkSpeed = walkSpeed;
		_templateRunSpeed = runSpeed;
		return this;
	}
	
	/**
	 * Sets the object's movement speed multiplier.
	 * 
	 * @param speedMultiplier speed multiplier
	 * @return {@code this}
	 */
	public L2ObjectInfo setSpeedMultiplier(double speedMultiplier)
	{
		if (Double.compare(_speedMultiplier, speedMultiplier) == 0)
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
	public L2ObjectInfo updateLocation(ObjectLocation location)
	{
		_location = location;
		_movementStart = System.nanoTime();
		return this;
	}
	
	/**
	 * Sets the destination of this world object.
	 * 
	 * @param location 3D coordinates of the current location
	 * @param destination 3D coordinates of the destination
	 * @param type the particular way the new destination was set
	 * @return {@code this}
	 */
	public L2ObjectInfo setDestination(ObjectLocation location, ObjectLocation destination, DestinationType type)
	{
		switch (type)
		{
			case STOP_MOVE:
				_stopMoveReceivalTime = System.nanoTime();
				break;
			case TELEPORT:
				_teleporting = true;
				_lastMovementInterruptedByTeleport = true;
				break;
			case STANDARD:
				_stopMoveReceivalTime = null;
				_teleporting = false;
				_lastMovementInterruptedByTeleport = false;
				break;
		}
		
		updateLocation(location);
		_destination = location.equals(destination) ? ObjectLocation.UNKNOWN_LOCATION : destination;
		
		return this;
	}
	
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
		if (_destination == ObjectLocation.UNKNOWN_LOCATION || _movementStart == null)
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
		return _teleporting || (_stopMoveReceivalTime != null && (System.nanoTime() - _stopMoveReceivalTime) < STOP_MOVE_GRACE_PERIOD)
				|| (_destination != ObjectLocation.UNKNOWN_LOCATION && !isAtDestination());
	}
	
	/**
	 * Returns whether the last movement was abruptly terminated by setting an entirely different position.
	 * 
	 * @return whether last movement was terminated by a teleportation
	 */
	public boolean isLastMoveInterrupted()
	{
		return _lastMovementInterruptedByTeleport;
	}
	
	private boolean isAtDestination()
	{
		if (_movementStart == null)
			return false;
		
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
		return _speedMultiplier * (_running ? _templateRunSpeed[_environment] : _templateWalkSpeed[_environment]);
	}
	
	/**
	 * An enumeration of different methods that may set the object's movement destination.
	 * 
	 * @author _dev_
	 */
	public enum DestinationType
	{
		/** Destination is essentially retained, subject to minor alterations due to geo layers. */
		STANDARD,
		/** Destination is manually set to current position and movement is terminated. */
		STOP_MOVE,
		/** Destination is manually set to an entirely different position and movement is terminated. */
		TELEPORT;
	}
}
