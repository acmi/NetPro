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

import net.l2emuproject.network.mmocore.MMOLogger;
import net.l2emuproject.proxy.network.listener.PacketManipulator;

/**
 * A task that asynchronously notifies a packet manipulator about a packet that was sent through a proxy.<BR>
 * <BR>
 * Despite the fact that this does not allow the manipulator to alter the packet, additional details are exposed to it:
 * a manipulator (vs a listener) may examine the differences between what was received and what was sent.
 * 
 * @author savormix
 */
public class ManipForwardNotifier extends ForwardNotifier
{
	private static final MMOLogger LOG = new MMOLogger(ManipForwardNotifier.class, 1000);
	
	private final Proxy _recipient;
	private final ByteBuffer _received;
	private final PacketManipulator _manip;
	
	/**
	 * Constructs the notification task.
	 * 
	 * @param sender packet sender
	 * @param recipient packet recipient
	 * @param received received packet body [complete buffer] (can be {@code null}, if proxy injected this packet)
	 * @param sent sent packet body [complete buffer] (can be {@code null}, if proxy withheld this packet)
	 * @param manip a manipulator to be notified
	 */
	public ManipForwardNotifier(Proxy sender, Proxy recipient, ByteBuffer received, ByteBuffer sent,
			PacketManipulator manip)
	{
		super(sender, sent);
		
		_recipient = recipient;
		_received = received;
		_manip = manip;
	}
	
	@Override
	public void run()
	{
		if (_received != null)
			_received.clear();
		if (getSent() != null)
			getSent().clear();
		
		try
		{
			_manip.packetForwarded(getSender(), _recipient, _received, getSent());
		}
		catch (RuntimeException e)
		{
			LOG.error("Problematic packet manipulator: " + _manip.getName(), e);
		}
	}
	
	PacketManipulator getManip()
	{
		return _manip;
	}
}
