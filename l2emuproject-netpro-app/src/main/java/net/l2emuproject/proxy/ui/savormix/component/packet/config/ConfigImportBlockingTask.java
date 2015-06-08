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

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.swing.JFileChooser;

import net.l2emuproject.proxy.ui.savormix.io.base.IOConstants;
import net.l2emuproject.ui.file.BetterExtensionFilter;

/**
 * A task that loads the given packet display configuration.
 * 
 * @author savormix
 */
public class ConfigImportBlockingTask extends BlockingTask<PacketDisplayConfig, Void, IOException, Void>
{
	private File _dir;
	private PacketContainerLoadTask _delegate;
	private final boolean _skipSelect;
	
	/**
	 * Constructs a saved packet display configuration selection &amp; loading task.
	 * 
	 * @param blocked associated dialog
	 * @param dir initial directory
	 */
	public ConfigImportBlockingTask(PacketDisplayConfig blocked, File dir)
	{
		this(blocked, dir, false);
	}
	
	/**
	 * Constructs a saved packet display configuration selection &amp; loading task.
	 * 
	 * @param blocked associated dialog
	 * @param dir initial directory (or the file to load)
	 * @param skipSelect whether treat {@code dir} as the selected file
	 */
	public ConfigImportBlockingTask(PacketDisplayConfig blocked, File dir, boolean skipSelect)
	{
		super(blocked);
		
		_dir = dir;
		_skipSelect = skipSelect;
	}
	
	private File showSelectDialog()
	{
		final JFileChooser fc = new JFileChooser(_dir);
		fc.setDialogType(JFileChooser.OPEN_DIALOG);
		fc.setMultiSelectionEnabled(false);
		fc.setFileFilter(BetterExtensionFilter.create("Packet display configuration", IOConstants.DISPLAY_CONFIG_EXTENSION));
		final int result = fc.showOpenDialog(getBlockedWindow());
		if (result != JFileChooser.APPROVE_OPTION)
			return null;
		
		return fc.getSelectedFile();
	}
	
	@Override
	protected void onPreExecute()
	{
		final File selected = _skipSelect ? _dir : showSelectDialog();
		if (selected == null)
		{
			cancel(true);
			return;
		}
		
		super.onPreExecute();
		
		getBlockedWindow().setLastConfig(_dir = selected);
		_delegate = new PacketContainerLoadTask(getBlockedWindow(), _dir.toPath());
		// delegate does not specify a onPreExecute method
	}
	
	@Override
	protected Void doInBackground(Void... params)
	{
		return _delegate.doInBackground(_skipSelect);
	}
	
	@Override
	protected void process(List<IOException> chunks)
	{
		_delegate.process(chunks);
	}
	
	@Override
	protected void onPostExecute(Void result)
	{
		if (_delegate != null)
			_delegate.onPostExecute(result);
		super.onPostExecute(result);
	}
}
