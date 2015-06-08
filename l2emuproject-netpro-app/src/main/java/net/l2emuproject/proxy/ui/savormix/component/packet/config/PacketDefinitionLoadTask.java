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
package net.l2emuproject.proxy.ui.savormix.component.packet.config;

import java.awt.Window;

import net.l2emuproject.proxy.ui.savormix.io.VersionnedPacketTable;
import net.l2emuproject.proxy.ui.savormix.loader.Loader;

/**
 * A task that reloads packet definitions.
 * 
 * @author savormix
 */
public class PacketDefinitionLoadTask extends BlockingTask</*PacketDisplayConfig*/Window, Void, Void, Void>
{
	/**
	 * Constructs this task.
	 * 
	 * @param blocked associated window
	 */
	public PacketDefinitionLoadTask(/*PacketDisplayConfig*/Window blocked)
	{
		super(blocked);
	}
	
	@Override
	protected Void doInBackground(Void... params)
	{
		VersionnedPacketTable.getInstance().reloadConfig();
		return null;
	}
	
	@Override
	protected void onPostExecute(Void result)
	{
		Loader.getActiveUIPane().notifyDefinitionsChanged();
		Loader.getActiveFrontend().initReload(getBlockedWindow() instanceof PacketDisplayConfig ? ((PacketDisplayConfig)getBlockedWindow()).getVersion() : null);
		
		super.onPostExecute(result);
	}
}
