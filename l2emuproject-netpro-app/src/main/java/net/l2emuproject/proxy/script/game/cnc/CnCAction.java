/*
 * Copyright 2011-2017 L2EMU UNIQUE
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
package net.l2emuproject.proxy.script.game.cnc;

import java.util.function.Consumer;

import net.l2emuproject.proxy.network.game.client.L2GameClient;

/**
 * @author _dev_
 */
public interface CnCAction
{
	/**
	 * Returns a name or an identifier to be used in a C&amp;C menu.
	 * 
	 * @return name/identifier
	 */
	String getLabel();
	
	/**
	 * Applies this action on the given client.
	 * 
	 * @param client a connected user
	 */
	void execute(L2GameClient client);
	
	/**
	 * Creates a named action.
	 * 
	 * @param label name/identifier
	 * @param action action
	 * @return named action
	 */
	static CnCAction of(String label, Consumer<L2GameClient> action)
	{
		return new CnCActionImpl(label, action);
	}
}
