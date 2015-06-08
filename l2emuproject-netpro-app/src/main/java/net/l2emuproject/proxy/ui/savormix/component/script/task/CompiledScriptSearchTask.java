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
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.JOptionPane;

import eu.revengineer.simplejse.exception.MutableOperationInProgressException;

import net.l2emuproject.proxy.script.NetProScriptCache;
import net.l2emuproject.proxy.ui.savormix.component.packet.config.BlockingTask;

/**
 * Searches for managed script instances available for unload, given substrings of their FQCNs.
 * 
 * @author _dev_
 */
public class CompiledScriptSearchTask extends BlockingTask<Window, String, Void, Object[]>
{
	private String _part, _failure;
	
	/**
	 * Constructs this task.
	 * 
	 * @param blocked associated window
	 */
	public CompiledScriptSearchTask(Window blocked)
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
			// Query all compiled scripts and sort them
			final Set<String> choices = new TreeSet<>(NetProScriptCache.getInstance().findCompiledScripts(_part = params[0], Integer.MAX_VALUE, 0));
			// Remove unmanaged or already unloaded scripts
			for (final Iterator<String> it = choices.iterator(); it.hasNext();)
				if (NetProScriptCache.getInitializer().getManagedScript(it.next()) == null)
					it.remove();
			return choices.toArray();
		}
		catch (MutableOperationInProgressException | InterruptedException ex)
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
				script = (String)JOptionPane.showInputDialog(getBlockedWindow(), "Select a script to unload: ", "Script unload – multiple matches", JOptionPane.QUESTION_MESSAGE, null, result, null);
			else
				script = result[0].toString();
			if (script != null)
				new ScriptUnloadTask(getBlockedWindow()).execute(script);
		}
		else
			JOptionPane.showMessageDialog(getBlockedWindow(), "No scripts match '" + _part + "'", "Nothing to unload", JOptionPane.WARNING_MESSAGE);
		super.onPostExecute(result);
	}
}
