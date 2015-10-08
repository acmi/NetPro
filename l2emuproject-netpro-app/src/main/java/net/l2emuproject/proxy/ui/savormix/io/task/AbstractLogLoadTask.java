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

import java.awt.Window;
import java.util.List;

import net.l2emuproject.proxy.ui.ReceivedPacket;
import net.l2emuproject.proxy.ui.savormix.component.packet.PacketList;
import net.l2emuproject.proxy.ui.savormix.io.base.IOConstants;

/**
 * Loads specified packet log files according to given log loading options. Loading is performed in a background thread, with a shared progress dialog.
 * 
 * @author savormix
 * @param <T> log descriptor
 */
public abstract class AbstractLogLoadTask<T> extends AbstractLogFileTask<T>implements IOConstants
{
	/**
	 * Constructs a historical packet log loading task.
	 * 
	 * @param owner associated window
	 */
	protected AbstractLogLoadTask(Window owner)
	{
		super(owner, "Loading");
	}
	
	PacketList _list;
	
	@Override
	protected void process(List<ReceivedPacket> packets)
	{
		super.process(packets);
		
		_list.addPackets(packets, false);
	}
}
