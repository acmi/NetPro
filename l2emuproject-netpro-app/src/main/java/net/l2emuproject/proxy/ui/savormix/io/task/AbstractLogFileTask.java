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
package net.l2emuproject.proxy.ui.savormix.io.task;

import java.awt.Window;
import java.util.List;

import net.l2emuproject.proxy.ui.ReceivedPacket;
import net.l2emuproject.proxy.ui.savormix.io.base.IOConstants;
import net.l2emuproject.proxy.ui.savormix.io.dialog.LoadProgressDialog;
import net.l2emuproject.ui.AsyncTask;

/**
 * Processes specified packet log files in a background thread, with a shared progress dialog.
 * 
 * @author savormix
 * @param <T> log descriptor
 */
public abstract class AbstractLogFileTask<T> extends AsyncTask<T, ReceivedPacket, Void>implements IOConstants
{
	private final Window _owner;
	private final String _desc;
	
	LoadProgressDialog _dialog;
	
	/**
	 * Constructs a historical packet log processing task.
	 * 
	 * @param owner associated window
	 * @param desc task description
	 */
	protected AbstractLogFileTask(Window owner, String desc)
	{
		_owner = owner;
		_desc = desc;
	}
	
	@Override
	protected void onPreExecute()
	{
		_dialog = new LoadProgressDialog(_owner, _desc + " packets...", this);
		_dialog.setVisible(true);
	}
	
	@Override
	protected void process(List<ReceivedPacket> packets)
	{
		// TODO: do not count loaded packets here, count proc'd ones!
		_dialog.addProgress(packets.size());
	}
	
	@Override
	protected void onPostExecute(Void result)
	{
		_dialog.setVisible(false);
		_dialog.dispose();
	}
}
