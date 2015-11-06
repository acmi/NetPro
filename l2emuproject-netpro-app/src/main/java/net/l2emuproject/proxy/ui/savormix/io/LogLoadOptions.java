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
package net.l2emuproject.proxy.ui.savormix.io;

import net.l2emuproject.network.protocol.IProtocolVersion;

/**
 * A class that specifies options to be used when loading a historical packet log file.
 * 
 * @author savormix
 */
public class LogLoadOptions
{
	private final LogFileHeader _header;
	private final IProtocolVersion _protocol;
	private final boolean _displayable;
	/*
	private final boolean _topDown;
	private final int _offset;
	*/
	private final int _count;
	
	/**
	 * Constructs this option wrapper.
	 * 
	 * @param header packet log header
	 * @param protocol protocol version to use
	 * @param displayable whether to only load displayable packets
	 * @param count amount of packets to load (maximal)
	 */
	public LogLoadOptions(LogFileHeader header, IProtocolVersion protocol, boolean displayable, /*boolean topDown, int offset, */int count)
	{
		_header = header;
		_protocol = protocol;
		_displayable = displayable;
		//_topDown = topDown;
		//_offset = offset;
		_count = count;
	}
	
	/**
	 * Returns meta information about the packet log.
	 * 
	 * @return packet log header
	 */
	public LogFileHeader getHeader()
	{
		return _header;
	}
	
	/**
	 * Returns the protocol version to use for the associated packet log.
	 * 
	 * @return protocol to use
	 */
	public IProtocolVersion getProtocol()
	{
		return _protocol;
	}
	
	/**
	 * Returns whether only packets selected in the associated protocol's packet display configuration should be loaded.
	 * 
	 * @return whether whether to only load displayable packets
	 */
	public boolean isDisplayable()
	{
		return _displayable;
	}
	
	/*
	public boolean isTopDown()
	{
		return _topDown;
	}
	
	public int getOffset()
	{
		return _offset;
	}
	*/
	/**
	 * Returns the maximal amount of packets to be loaded.
	 * 
	 * @return amount of packets to load
	 */
	public int getCount()
	{
		return _count;
	}
}
