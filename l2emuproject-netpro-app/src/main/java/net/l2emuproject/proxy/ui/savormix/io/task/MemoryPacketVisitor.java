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

import net.l2emuproject.proxy.ui.ReceivedPacket;

/**
 * Defines a visitor of packets.
 * 
 * @author _dev_
 */
public interface MemoryPacketVisitor
{
	/**
	 * Called to report a start of visitation session.
	 * 
	 * @throws Exception if anything goes wrong during processing
	 */
	void onStart() throws Exception;
	
	/**
	 * Requests this visitor to process a packet.
	 * 
	 * @param packet packet to visit
	 * @throws Exception if anything goes wrong during processing
	 */
	void onPacket(ReceivedPacket packet) throws Exception;
	
	/**
	 * Called to report an end of visitation session.
	 * 
	 * @throws Exception if anything goes wrong during processing
	 */
	void onEnd() throws Exception;
}
