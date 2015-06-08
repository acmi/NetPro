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
package examples.login;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import net.l2emuproject.proxy.network.Packet;
import net.l2emuproject.proxy.network.login.client.L2LoginClient;
import net.l2emuproject.proxy.network.login.client.packets.RequestServerLogin;
import net.l2emuproject.proxy.network.login.server.L2LoginServer;
import net.l2emuproject.proxy.script.login.LoginScript;

/**
 * A script that does not allow more than one {@link RequestServerLogin} packet to be sent by the client in the same
 * session.
 * 
 * @author savormix
 */
public final class ForceSingularLogin extends LoginScript
{
	private final Set<Reference<L2LoginClient>> _requestors;
	
	/** Constructs this script. */
	public ForceSingularLogin()
	{
		super(new int[] { RequestServerLogin.OPCODE }, null);
		
		_requestors = new HashSet<>();
	}
	
	@Override
	protected void clientPacketArrived(L2LoginClient sender, L2LoginServer recipient, Packet packet)
	{
		synchronized (ForceSingularLogin.class)
		{
			for (Iterator<Reference<L2LoginClient>> it = _requestors.iterator(); it.hasNext();)
			{
				final Reference<L2LoginClient> ref = it.next();
				final L2LoginClient client = ref.get();
				if (client == null)
					it.remove();
				else if (client == sender)
				{
					packet.demandLoss(this);
					return;
				}
			}
			
			_requestors.add(new WeakReference<>(sender));
		}
	}
	
	@Override
	public String getScriptName()
	{
		return "Discard duplicate game server login requests";
	}
	
	@Override
	public String getAuthor()
	{
		return "savormix";
	}
	
	@Override
	public String getVersionString()
	{
		return "Valiance";
	}
}
