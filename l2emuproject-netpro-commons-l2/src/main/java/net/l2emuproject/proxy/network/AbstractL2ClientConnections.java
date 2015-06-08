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

import java.io.IOException;

import net.l2emuproject.network.mmocore.MMOConfig;

/**
 * This class manages connections between a client and the underlying proxy server.
 * 
 * @author NB4L1
 */
public abstract class AbstractL2ClientConnections extends ProxyConnections
{
	/** System propery used to set the connection acception interval */
	public static final String PROPERTY_ACC_INTERVAL = "AcceptionSelectorSleepTime";
	
	/**
	 * Creates a L2 connection manager.
	 * 
	 * @param config MMO networking configuration
	 * @param packetHandler received packet handler
	 * @throws IOException if the manager could not be set up
	 */
	protected AbstractL2ClientConnections(MMOConfig config, ProxyPacketHandler packetHandler) throws IOException
	{
		super(config, packetHandler);
	}
}
