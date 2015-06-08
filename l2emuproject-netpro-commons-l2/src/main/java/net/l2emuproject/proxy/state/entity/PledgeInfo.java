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
 * Stores info about a pledge (clan).
 * 
 * @author savormix
 */
public class PledgeInfo extends EntityInfoWithCrest
{
	private int _insigniaID;
	private int _allianceID;
	
	/**
	 * Creates a pledge descriptor.
	 * 
	 * @param id pledge ID
	 */
	public PledgeInfo(int id)
	{
		super(id);
	}
	
	/**
	 * Returns the insignia ID for this pledge.
	 * 
	 * @return pledge insignia's ID
	 */
	public int getInsigniaID()
	{
		return _insigniaID;
	}
	
	/**
	 * Sets the insignia ID for this pledge.
	 * 
	 * @param insigniaID insignia ID
	 */
	public void setInsigniaID(int insigniaID)
	{
		_insigniaID = insigniaID;
	}
	
	/**
	 * Returns the alliance ID of this pledge.
	 * 
	 * @return pledge's alliance ID
	 */
	public int getAllianceID()
	{
		return _allianceID;
	}
	
	/**
	 * Sets the alliance ID of this pledge.
	 * 
	 * @param allianceID alliance ID
	 */
	public void setAllianceID(int allianceID)
	{
		_allianceID = allianceID;
	}
}
