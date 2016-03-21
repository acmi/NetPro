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
package net.l2emuproject.proxy.io;

import java.util.Set;

import net.l2emuproject.proxy.network.EndpointType;
import net.l2emuproject.proxy.ui.savormix.io.LoggedPacketFlag;

/**
 * @author _dev_
 */
public class LogFilePacket
{
	private final EndpointType _endpoint;
	private final byte[] _content;
	private final long _receivalTime;
	private final Set<LoggedPacketFlag> _flags;
	
	public LogFilePacket(EndpointType endpoint, byte[] content, long receivalTime, Set<LoggedPacketFlag> flags)
	{
		_endpoint = endpoint;
		_content = content;
		_receivalTime = receivalTime;
		_flags = flags;
	}
	
	public EndpointType getEndpoint()
	{
		return _endpoint;
	}
	
	public byte[] getContent()
	{
		return _content;
	}
	
	public long getReceivalTime()
	{
		return _receivalTime;
	}
	
	public Set<LoggedPacketFlag> getFlags()
	{
		return _flags;
	}
}
