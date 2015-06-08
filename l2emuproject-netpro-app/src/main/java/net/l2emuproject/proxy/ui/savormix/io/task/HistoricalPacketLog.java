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
import net.l2emuproject.proxy.state.entity.context.ICacheServerID;

/**
 * Represents a shared cache context for a historical packet log (packet log file).
 * 
 * @author _dev_
 */
public class HistoricalPacketLog extends NotARealProxyObject<Path> implements ICacheServerID
{
	/**
	 * Creates a shared context identifier for a historical packet log.
	 * 
	 * @param file complete path to a packet log file
	 */
	public HistoricalPacketLog(Path file)
	{
		super(file);
	}
	
	@Override
	public Path get()
	{
		return super.get();
	}
}
