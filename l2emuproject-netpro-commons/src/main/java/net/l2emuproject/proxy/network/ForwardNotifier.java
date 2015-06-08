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

/**
 * A base class for implementing sent packet notifications to listeners.
 * 
 * @author savormix
 */
public abstract class ForwardNotifier implements Runnable
{
	private final Proxy _sender;
	private final ByteBuffer _sent;
	
	/**
	 * Stores minimal information about a sent packet.
	 * 
	 * @param sender packet sender
	 * @param sent packet body [complete buffer]
	 */
	protected ForwardNotifier(Proxy sender, ByteBuffer sent)
	{
		_sender = sender;
		_sent = sent;
	}
	
	/**
	 * Returns the endpoint that sent data.
	 * 
	 * @return packet sender
	 */
	protected Proxy getSender()
	{
		return _sender;
	}
	
	/**
	 * Returns the data that was sent.
	 * 
	 * @return packet body [complete buffer]
	 */
	protected ByteBuffer getSent()
	{
		return _sent;
	}
}
