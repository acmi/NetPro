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

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import javax.swing.JOptionPane;

import net.l2emuproject.proxy.ui.savormix.io.exception.InvalidFileException;
import net.l2emuproject.proxy.ui.savormix.io.exception.UnsupportedFileException;
import net.l2emuproject.ui.AsyncTask;
import net.l2emuproject.util.logging.L2Logger;

/**
 * Creates packet containers, loads a packet display configuration and creates UI components.
 * 
 * @author savormix
 */
public class PacketContainerLoadTask extends AsyncTask<Boolean, IOException, Void>
{
	private static final L2Logger LOG = L2Logger.getLogger(PacketContainerLoadTask.class);
	
	private final PacketDisplayConfig _window;
	private final Path _config;
	
	/**
	 * Constructs this task.
	 * 
	 * @param window associated window
	 * @param config associated display config file
	 */
	public PacketContainerLoadTask(PacketDisplayConfig window, Path config)
	{
		_window = window;
		_config = config;
	}
	
	@Override
	protected Void doInBackground(Boolean... params)
	{
		try
		{
			_window.loadInBackground(_config, params[0]);
		}
		catch (InvalidFileException | UnsupportedFileException e)
		{
			LOG.warn(e.getLocalizedMessage());
			
			publish(e);
		}
		catch (IOException e)
		{
			LOG.error("Cannot load packet display configuration.", e);
			
			publish(e);
		}
		return null;
	}
	
	@Override
	protected void process(List<IOException> chunks)
	{
		cancel(false);
		
		JOptionPane.showMessageDialog(_window, chunks.get(0).getLocalizedMessage(), "Loading unsuccessful", JOptionPane.ERROR_MESSAGE);
	}
	
	@Override
	protected void onPostExecute(Void result)
	{
		_window.propagateConfig();
		// _window.notifyCurrentState();
		
		_window.setupUIAfterLoad();
	}
}
