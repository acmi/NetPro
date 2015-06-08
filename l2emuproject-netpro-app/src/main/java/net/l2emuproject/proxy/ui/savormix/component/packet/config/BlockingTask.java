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

import javax.swing.JDialog;

import net.l2emuproject.proxy.ui.savormix.EventSink;
import net.l2emuproject.ui.AsyncTask;

/**
 * A task that blocks the given window while it is executed.
 * 
 * @author savormix
 * @param <T> type of blocked window
 * @param <Params> {@link AsyncTask}
 * @param <Progress> {@link AsyncTask}
 * @param <Result> {@link AsyncTask}
 */
public abstract class BlockingTask<T extends Window, Params, Progress, Result> extends AsyncTask<Params, Progress, Result>
{
	private final T _blocked;
	
	private JDialog _dialog;
	
	/**
	 * Creates a blocking task that emulates blocking on {@code blocked} while being executed.
	 * 
	 * @param blocked a window to stop receiving input
	 */
	public BlockingTask(T blocked)
	{
		_blocked = blocked;
		
		if (!(_blocked instanceof EventSink))
			throw new UnsupportedOperationException("Only emulated blocking supported.");
	}
	
	/**
	 * Returns the component to prevent from receiving input events.
	 * 
	 * @return blocked window
	 */
	protected T getBlockedWindow()
	{
		return _blocked;
	}
	
	/**
	 * Create a progress dialog to be shown while this task is being executed.<BR>
	 * <BR>
	 * If you want a task to run silently, you can return {@code null}.
	 * 
	 * @return a dialog or {@code null}
	 */
	protected JDialog createProgressDialog()
	{
		return new BlockingDialog(_blocked, true);
	}
	
	/**
	 * {@inheritDoc} A subclass that overrides this method should always invoke the super
	 * implementation.
	 */
	@Override
	protected void onPreExecute()
	{
		((EventSink)_blocked).startIgnoringEvents();
		_dialog = createProgressDialog();
		if (_dialog != null)
			_dialog.setVisible(true);
	}
	
	/**
	 * {@inheritDoc} A subclass that overrides this method should always invoke the super
	 * implementation.
	 */
	@Override
	protected void onPostExecute(Result result)
	{
		// if this task was cancelled prior to execution
		if (_dialog != null)
		{
			_dialog.setVisible(false);
			_dialog.dispose();
		}
		((EventSink)_blocked).stopIgnoringEvents();
	}
}
