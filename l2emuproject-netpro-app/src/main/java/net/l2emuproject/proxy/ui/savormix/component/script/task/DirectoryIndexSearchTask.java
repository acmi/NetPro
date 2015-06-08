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
import java.util.Set;
import java.util.TreeSet;

import javax.swing.JOptionPane;

import eu.revengineer.simplejse.exception.MutableOperationInProgressException;

import net.l2emuproject.proxy.script.NetProScriptCache;
import net.l2emuproject.proxy.ui.savormix.component.packet.config.BlockingTask;

/**
 * Searches the file system for script classes to be [re]loaded, given substrings of their FQCNs.
 * 
 * @author _dev_
 */
public class DirectoryIndexSearchTask extends BlockingTask<Window, String, Void, Object[]>
{
	private String _part, _failure;
	
	/**
	 * Constructs this task.
	 * 
	 * @param blocked associated window
	 */
	public DirectoryIndexSearchTask(Window blocked)
	{
		super(blocked);
		
		_part = null;
		_failure = null;
	}
	
	@Override
	protected Object[] doInBackground(String... params)
	{
		try
		{
			// Sort all scripts found in the script directory
			final Set<String> choices = new TreeSet<>(NetProScriptCache.getInstance().findIndexedScripts(_part = params[0], Integer.MAX_VALUE, 0));
			return choices.toArray();
		}
		catch (MutableOperationInProgressException | InterruptedException | IllegalStateException | IOException ex)
		{
			_failure = ex.getLocalizedMessage();
			return null;
		}
	}
	
	@Override
	protected void onPostExecute(Object[] result)
	{
		if (_failure != null)
			JOptionPane.showMessageDialog(getBlockedWindow(), _failure, "Failure", JOptionPane.ERROR_MESSAGE);
		else if (result.length > 0)
		{
			final String script;
			if (result.length > 1)
				script = (String)JOptionPane.showInputDialog(getBlockedWindow(), "Select a script to load: ", "Script load – multiple matches", JOptionPane.QUESTION_MESSAGE, null, result, null);
			else
				script = result[0].toString();
			
			if (script != null)
			{
				final boolean neu = NetProScriptCache.getInitializer().getManagedScript(script) == null;
				if (!neu
						|| JOptionPane.showConfirmDialog(getBlockedWindow(), "A new (or unmanaged) script '" + script + "' will now be loaded. Continue?", "Confirm new script",
								JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION)
					new ScriptLoadTask(getBlockedWindow()).execute(script);
			}
		}
		else
			JOptionPane.showMessageDialog(getBlockedWindow(), "No scripts match '" + _part + "'", "Nothing to load", JOptionPane.WARNING_MESSAGE);
		super.onPostExecute(result);
	}
}
