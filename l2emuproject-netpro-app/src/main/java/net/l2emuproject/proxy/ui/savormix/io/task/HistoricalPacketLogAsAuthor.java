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
package net.l2emuproject.proxy.ui.savormix.io.task;

import java.nio.file.Path;

import net.l2emuproject.lang.NotARealProxyObject;
import net.l2emuproject.proxy.network.EndpointType;
import net.l2emuproject.proxy.network.packets.IPacketSource;

/**
 * Represents a historical packet log as a packet author.
 * 
 * @author _dev_
 */
public class HistoricalPacketLogAsAuthor extends NotARealProxyObject<Path> implements IPacketSource
{
	private final EndpointType _type;
	
	/**
	 * Constructs this wrapper.
	 * 
	 * @param log wrapped object
	 * @param type packet author's type
	 */
	public HistoricalPacketLogAsAuthor(HistoricalPacketLog log, EndpointType type)
	{
		super(log.get());
		
		_type = type;
	}
	
	@Override
	public EndpointType getType()
	{
		return _type;
	}
}
