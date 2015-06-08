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
package net.l2emuproject.proxy.network;

import java.nio.ByteBuffer;

import net.l2emuproject.proxy.network.listener.PacketListener;

/**
 * A task that asynchronously notifies a packet listener about a packet that was sent through a proxy.
 * 
 * @author savormix
 */
public class ListenerForwardNotifier extends ForwardNotifier
{
	private final PacketListener _listener;
	private final long _time;
	
	/**
	 * Constructs the notification task.
	 * 
	 * @param sender packet sender
	 * @param sent packet body [complete buffer]
	 * @param listener a listener to be notified
	 * @param time packet arrival timestamp
	 */
	public ListenerForwardNotifier(Proxy sender, ByteBuffer sent, PacketListener listener, long time)
	{
		super(sender, sent);
		
		_listener = listener;
		_time = time;
	}
	
	@Override
	public void run()
	{
		getSent().clear();
		if (getSender().getType().isClient())
			_listener.onClientPacket(getSender(), getSender().getTarget(), getSent(), _time);
		else
			_listener.onServerPacket(getSender(), getSender().getTarget(), getSent(), _time);
	}
	
	PacketListener getListener()
	{
		return _listener;
	}
}
