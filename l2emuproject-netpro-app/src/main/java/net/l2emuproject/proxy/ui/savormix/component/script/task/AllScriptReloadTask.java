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

import javax.swing.JOptionPane;

import eu.revengineer.simplejse.exception.DependencyResolutionException;
import eu.revengineer.simplejse.exception.MutableOperationInProgressException;

import net.l2emuproject.proxy.script.NetProScriptCache;
import net.l2emuproject.proxy.ui.savormix.component.packet.config.BlockingTask;
import net.l2emuproject.util.logging.L2Logger;

/**
 * Attempts to newly compile and [re]initialize all script classes.
 * 
 * @author _dev_
 */
public class AllScriptReloadTask extends BlockingTask<Window, Void, Void, Void>
{
	private static final L2Logger LOG = L2Logger.getLogger(AllScriptReloadTask.class);
	
	private String _failure;
	
	/**
	 * Constructs this task.
	 * 
	 * @param blocked associated window
	 */
	public AllScriptReloadTask(Window blocked)
	{
		super(blocked);
		
		_failure = null;
	}
	
	@Override
	protected Void doInBackground(Void... params)
	{
		final NetProScriptCache cache = NetProScriptCache.getInstance();
		try
		{
			cache.compileAllScripts();
			cache.writeToCache();
		}
		catch (MutableOperationInProgressException | DependencyResolutionException | IOException ex)
		{
			LOG.error("Cannot reload scripts", ex);
			_failure = ex.getLocalizedMessage();
		}
		return null;
	}
	
	@Override
	protected void onPostExecute(Void result)
	{
		if (_failure != null)
			JOptionPane.showMessageDialog(getBlockedWindow(), _failure, "Scripts not reloaded", JOptionPane.ERROR_MESSAGE);
		else
			JOptionPane.showMessageDialog(getBlockedWindow(), "All scripts have been reloaded.", "Success", JOptionPane.INFORMATION_MESSAGE);
		
		super.onPostExecute(result);
	}
}
