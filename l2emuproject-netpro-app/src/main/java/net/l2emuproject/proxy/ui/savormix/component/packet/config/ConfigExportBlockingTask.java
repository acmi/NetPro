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
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import net.l2emuproject.proxy.network.ServiceType;
import net.l2emuproject.proxy.network.meta.IPacketTemplate;
import net.l2emuproject.proxy.ui.savormix.io.PacketDisplayConfigManager;
import net.l2emuproject.proxy.ui.savormix.io.base.IOConstants;
import net.l2emuproject.ui.file.BetterExtensionFilter;
import net.l2emuproject.util.logging.L2Logger;

/**
 * A task that exports the current packet display configuration.
 * 
 * @author savormix
 */
public class ConfigExportBlockingTask extends BlockingTask<PacketDisplayConfig, PacketContainer, IOException, File>
{
	private static final L2Logger _log = L2Logger.getLogger(ConfigExportBlockingTask.class);
	
	private File _dir;
	
	/**
	 * Constructs this task.
	 * 
	 * @param blocked associated dialog
	 * @param dir initial directory
	 */
	public ConfigExportBlockingTask(PacketDisplayConfig blocked, File dir)
	{
		super(blocked);
		
		_dir = dir;
	}
	
	@Override
	protected void onPreExecute()
	{
		final JFileChooser fc = new JFileChooser(_dir);
		fc.setDialogType(JFileChooser.SAVE_DIALOG);
		fc.setMultiSelectionEnabled(false);
		fc.setFileFilter(BetterExtensionFilter.create("Packet display configuration", IOConstants.DISPLAY_CONFIG_EXTENSION));
		final int result = fc.showSaveDialog(getBlockedWindow());
		if (result != JFileChooser.APPROVE_OPTION)
		{
			cancel(true);
			return;
		}
		
		_dir = fc.getSelectedFile();
		{
			final Path path = _dir.toPath();
			final String file = path.getFileName().toString();
			if (!file.contains("."))
				_dir = path.resolveSibling(file + "." + IOConstants.DISPLAY_CONFIG_EXTENSION).toFile();
		}
		getBlockedWindow().setLastConfig(_dir);
		
		super.onPreExecute();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	protected File doInBackground(PacketContainer... params)
	{
		final PacketContainer[] byTypeC = new PacketContainer[2];
		for (final PacketContainer pc : params)
			byTypeC[pc.getProxyType().ordinal() & 1] = pc;
		
		final Set<IPacketTemplate>[] byTypeS = (Set<IPacketTemplate>[])new Set<?>[byTypeC.length];
		for (int i = 0; i < byTypeS.length; ++i)
		{
			final PacketContainer pc = byTypeC[i];
			if (pc == null)
				continue;
			
			final Set<IPacketTemplate> set = new HashSet<>(byTypeC[i].getCommitted());
			// apply uncommitted modifications
			set.removeAll(pc.getDeselected());
			set.addAll(pc.getSelected());
			byTypeS[i] = set;
		}
		
		try
		{
			PacketDisplayConfigManager.getInstance().save(_dir.toPath(), getBlockedWindow().getVersion(), byTypeS[0], byTypeS[1], ServiceType.valueOf(getBlockedWindow().getVersion()).isLogin());
		}
		catch (IOException e)
		{
			_log.error("Cannot export packet display configuration.", e);
			publish(e);
		}
		
		return _dir;
	}
	
	@Override
	protected void process(List<IOException> chunks)
	{
		JOptionPane.showMessageDialog(getBlockedWindow(), chunks.get(0).getLocalizedMessage(), "Export failed", JOptionPane.ERROR_MESSAGE);
	}
}
