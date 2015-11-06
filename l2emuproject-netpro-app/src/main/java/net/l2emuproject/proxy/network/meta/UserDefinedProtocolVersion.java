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
package net.l2emuproject.proxy.network.meta;

import java.text.SimpleDateFormat;
import java.util.Date;

import net.l2emuproject.network.protocol.IProtocolVersion;
import net.l2emuproject.network.protocol.UnknownProtocolVersion;

/**
 * A class indicating an user-defined protocol version.<BR>
 * <BR>
 * Natural order is imposed exclusively via {@link #getReleaseDate()}.
 * 
 * @author savormix
 */
public abstract class UserDefinedProtocolVersion implements IProtocolVersion, Comparable<UserDefinedProtocolVersion>
{
	private final String _alias;
	private final String _category;
	private final int _version;
	private final long _date;
	
	/**
	 * Constructs a protocol version definition.
	 * 
	 * @param alias protocol name
	 * @param category protocol group
	 * @param version protocol revision number
	 * @param date protocol version introduction to NA data
	 */
	public UserDefinedProtocolVersion(String alias, String category, int version, long date)
	{
		_alias = alias;
		_category = category;
		_version = version;
		_date = date;
	}
	
	/**
	 * Returns the name of this protocol version.
	 * 
	 * @return protocol name
	 */
	public String getAlias()
	{
		return _alias;
	}
	
	/**
	 * Returns the group this protocol version is a part of.
	 * 
	 * @return protocol group
	 */
	public String getCategory()
	{
		return _category;
	}
	
	@Override
	public int getVersion()
	{
		return _version;
	}
	
	@Override
	public boolean isOlderThan(IProtocolVersion version)
	{
		return getReleaseDate() < version.getReleaseDate();
	}
	
	@Override
	public boolean isOlderThanOrEqualTo(IProtocolVersion version)
	{
		return getReleaseDate() <= version.getReleaseDate();
	}
	
	@Override
	public boolean isNewerThan(IProtocolVersion version)
	{
		return getReleaseDate() > version.getReleaseDate();
	}
	
	@Override
	public boolean isNewerThanOrEqualTo(IProtocolVersion version)
	{
		return getReleaseDate() >= version.getReleaseDate();
	}
	
	@Override
	public long getReleaseDate()
	{
		return _date;
	}
	
	@Override
	public String toString()
	{
		return "[" + _version + "] " + _alias + " " + new SimpleDateFormat("yyyy-MM-dd").format(new Date(_date));
	}
	
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + (int)(_date ^ (_date >>> 32));
		result = prime * result + _version;
		return result;
	}
	
	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (obj instanceof UnknownProtocolVersion)
			return obj.equals(this);
		if (getClass() != obj.getClass())
			return false;
		UserDefinedProtocolVersion other = (UserDefinedProtocolVersion)obj;
		if (_date != other._date)
			return false;
		if (_version != other._version)
			return false;
		return true;
	}
	
	@Override
	public int compareTo(UserDefinedProtocolVersion other)
	{
		if (_date < other.getReleaseDate())
			return -1;
		if (_date > other.getReleaseDate())
			return +1;
		
		return 0;
	}
}
