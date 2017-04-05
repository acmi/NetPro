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

import java.util.regex.Pattern;

/**
 * @author _dev_
 */
public interface CnCMenu extends Iterable<CnCAction>
{
	Pattern getOpenCommand();
	
	String getActionBypassPrefix();
	
	void add(CnCAction action);
	
	void remove(CnCAction action);
	
	default CnCAction get(String label)
	{
		for (final CnCAction action : this)
			if (action.getLabel().equals(label))
				return action;
		return null;
	}
}
