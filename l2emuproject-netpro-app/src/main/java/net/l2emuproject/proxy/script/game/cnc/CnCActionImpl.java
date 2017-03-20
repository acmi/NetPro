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
final class CnCActionImpl implements CnCAction
{
	private final String _label;
	private final Consumer<L2GameClient> _action;
	
	public CnCActionImpl(String label, Consumer<L2GameClient> action)
	{
		_label = label;
		_action = action;
	}
	
	@Override
	public String getLabel()
	{
		return _label;
	}
	
	@Override
	public void execute(L2GameClient client)
	{
		_action.accept(client);
	}
	
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((_label == null) ? 0 : _label.hashCode());
		return result;
	}
	
	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final CnCActionImpl other = (CnCActionImpl)obj;
		if (_label == null)
		{
			if (other._label != null)
				return false;
		}
		else if (!_label.equals(other._label))
			return false;
		return true;
	}
	
	@Override
	public String toString()
	{
		return _label;
	}
}
