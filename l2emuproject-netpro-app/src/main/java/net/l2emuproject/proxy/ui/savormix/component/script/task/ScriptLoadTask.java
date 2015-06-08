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
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.swing.JOptionPane;

import eu.revengineer.simplejse.exception.DependencyResolutionException;
import eu.revengineer.simplejse.exception.MutableOperationInProgressException;

import net.l2emuproject.proxy.script.NetProScriptCache;
import net.l2emuproject.proxy.ui.savormix.component.packet.config.BlockingTask;

/**
 * A task that attempts to compile (and initialize) script classes, given their FQCNs, and displays execution results.
 * 
 * @author _dev_
 */
public class ScriptLoadTask extends BlockingTask<Window, String, Void, Void>
{
	private final Map<String, String> _wrongParams;
	
	private String[] _params;
	
	/**
	 * Constructs this task.
	 * 
	 * @param blocked associated window
	 */
	public ScriptLoadTask(Window blocked)
	{
		super(blocked);
		
		_wrongParams = new TreeMap<>();
	}
	
	@Override
	protected Void doInBackground(String... params)
	{
		final NetProScriptCache cache = NetProScriptCache.getInstance();
		for (final String script : _params = params)
		{
			try
			{
				cache.compileSingleScript(script);
			}
			catch (IllegalArgumentException | MutableOperationInProgressException | IOException | DependencyResolutionException e)
			{
				_wrongParams.put(script, e.getMessage());
			}
		}
		return null;
	}
	
	@Override
	protected void onPostExecute(Void result)
	{
		if (!_wrongParams.isEmpty())
		{
			for (Entry<String, String> e : _wrongParams.entrySet())
				JOptionPane.showMessageDialog(getBlockedWindow(), "Cannot [re]load script '" + e.getKey() + "': \n" + e.getValue(), "Script not loaded", JOptionPane.WARNING_MESSAGE);
		}
		else
			JOptionPane.showMessageDialog(getBlockedWindow(), "Loaded " + Arrays.toString(_params), "Success", JOptionPane.INFORMATION_MESSAGE);
		
		super.onPostExecute(result);
	}
}
