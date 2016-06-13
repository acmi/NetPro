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
package condition.serverlist;

import net.l2emuproject.proxy.network.login.client.packets.RequestServerList;
import net.l2emuproject.proxy.network.meta.ServerListTypePublisher;
import net.l2emuproject.proxy.network.meta.condition.IntegerCondition;
import net.l2emuproject.proxy.script.condition.ScriptedFieldValueCondition;

/**
 * A fake condition to test the client-requested list type.
 * 
 * @author _dev_
 */
public final class ServerListFreya extends ScriptedFieldValueCondition implements IntegerCondition, ServerListTypePublisher
{
	@Override
	public boolean test(long value)
	{
		return LIST_TYPE.get() >= RequestServerList.TYPE_FREYA;
	}
}
