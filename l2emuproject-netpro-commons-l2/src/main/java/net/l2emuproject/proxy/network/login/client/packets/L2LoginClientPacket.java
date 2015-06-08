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
package net.l2emuproject.proxy.network.login.client.packets;

import net.l2emuproject.proxy.network.login.client.L2LoginClient;
import net.l2emuproject.proxy.network.login.server.L2LoginServer;
import net.l2emuproject.proxy.network.packets.ProxyReceivedPacket;

/**
 * A wrapper class representing a packet sent by a L2 client to a login server.
 * 
 * @author savormix
 */
public abstract class L2LoginClientPacket extends ProxyReceivedPacket
{
	/**
	 * Constructs a login client packet that is ready for forwarding.
	 * 
	 * @param opcode main opcode
	 */
	protected L2LoginClientPacket(int opcode)
	{
		super(opcode);
	}
	
	@Override
	protected L2LoginClient getReceiver()
	{
		return (L2LoginClient)super.getReceiver();
	}
	
	@Override
	protected L2LoginServer getRecipient()
	{
		return (L2LoginServer)super.getRecipient();
	}
}
