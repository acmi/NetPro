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
package net.l2emuproject.proxy.ui.savormix.component.script.task;

import java.awt.Window;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JOptionPane;

import eu.revengineer.simplejse.init.UnloadableScriptInitializer;

import net.l2emuproject.proxy.script.NetProScriptCache;
import net.l2emuproject.proxy.ui.savormix.component.packet.config.BlockingTask;

/**
 * Attempts to uninstall managed script instances and allow them to be garbage-collected, given the FQCNs. Displays execution results.
 * 
 * @author _dev_
 */
public class ScriptUnloadTask extends BlockingTask<Window, String, Void, Void>
{
	private final List<String> _wrongParams;
	
	private String[] _params;
	
	/**
	 * Constructs this task.
	 * 
	 * @param blocked associated window
	 */
	public ScriptUnloadTask(Window blocked)
	{
		super(blocked);
		
		_wrongParams = new ArrayList<>();
	}
	
	@Override
	protected Void doInBackground(String... params)
	{
		final UnloadableScriptInitializer init = NetProScriptCache.getInitializer();
		for (final String script : _params = params)
		{
			try
			{
				init.unloadScript(script);
			}
			catch (IllegalArgumentException e)
			{
				_wrongParams.add(script);
			}
		}
		return null;
	}
	
	@Override
	protected void onPostExecute(Void result)
	{
		if (!_wrongParams.isEmpty())
			JOptionPane.showMessageDialog(getBlockedWindow(), "Cannot unload script(s): " + _wrongParams, "Scripts not unloaded", JOptionPane.WARNING_MESSAGE);
		else
			JOptionPane.showMessageDialog(getBlockedWindow(), "Unloaded " + Arrays.toString(_params), "Success", JOptionPane.INFORMATION_MESSAGE);
		
		super.onPostExecute(result);
	}
}
