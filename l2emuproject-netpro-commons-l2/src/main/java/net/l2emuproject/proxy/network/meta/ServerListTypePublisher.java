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
package net.l2emuproject.proxy.network.meta;

/**
 * Makes the game server list type accessible to PPE, either for proper IP/port injection or when displaying the ServerList packet.
 * 
 * @author _dev_
 */
public interface ServerListTypePublisher
{
	/** Allows ServerList structure requiring implementors to pass the list type to the PPE system */
	ThreadLocal<Integer> LIST_TYPE = new ThreadLocal<Integer>();
}
