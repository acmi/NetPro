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

import static net.l2emuproject.proxy.io.packetlog.LoggedPacketFlag.HIDDEN;

import java.awt.Window;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.Set;

import javax.swing.SwingUtilities;

import net.l2emuproject.network.protocol.IProtocolVersion;
import net.l2emuproject.network.protocol.ProtocolVersionManager;
import net.l2emuproject.proxy.config.ProxyConfig;
import net.l2emuproject.proxy.io.IOConstants;
import net.l2emuproject.proxy.io.NewIOHelper;
import net.l2emuproject.proxy.io.definitions.VersionnedPacketTable;
import net.l2emuproject.proxy.io.packetlog.LogFileHeader;
import net.l2emuproject.proxy.io.packetlog.LoggedPacketFlag;
import net.l2emuproject.proxy.network.EndpointType;
import net.l2emuproject.proxy.network.ServiceType;
import net.l2emuproject.proxy.network.meta.IPacketTemplate;
import net.l2emuproject.proxy.network.meta.container.OpcodeOwnerSet;
import net.l2emuproject.proxy.script.LogLoadScriptManager;
import net.l2emuproject.proxy.ui.ReceivedPacket;
import net.l2emuproject.proxy.ui.savormix.io.LogLoadOptions;
import net.l2emuproject.proxy.ui.savormix.loader.Frontend;
import net.l2emuproject.proxy.ui.savormix.loader.Loader;
import net.l2emuproject.util.BitMaskUtils;
import net.l2emuproject.util.logging.L2Logger;

/**
 * Loads specified packet log files according to given log loading options.<BR>
 * <BR>
 * Loading is performed in a background thread, with a shared progress dialog.
 * 
 * @author savormix
 */
public class LogLoadTask extends AbstractLogLoadTask<LogLoadOptions> implements IOConstants
{
	private static final L2Logger LOG = L2Logger.getLogger(LogLoadTask.class);
	
	/**
	 * Constructs a historical packet log loading task.
	 * 
	 * @param owner associated window
	 */
	public LogLoadTask(Window owner)
	{
		super(owner);
	}
	
	@Override
	protected Void doInBackground(LogLoadOptions... params)
	{
		for (final LogLoadOptions llo : params)
		{
			final LogFileHeader lfh = llo.getHeader();
			final Path p = lfh.getLogFile();
			final String name = p.getFileName().toString();
			
			// quick prepare
			SwingUtilities.invokeLater(new Runnable()
			{
				@Override
				public void run()
				{
					//_dialog.setTitle(name);
					_dialog.setMaximum(name, llo.getCount());
				}
			});
			
			final LogLoadScriptManager sm = LogLoadScriptManager.getInstance();
			
			if (isCancelled())
				break;
			
			try (final SeekableByteChannel channel = Files.newByteChannel(p, StandardOpenOption.READ); final NewIOHelper ioh = new NewIOHelper(channel))
			{
				final long size = Files.size(p);
				
				ioh.read(new byte[lfh.getHeaderSize()]);
				
				if (isCancelled())
					break;
				
				final IProtocolVersion protocol = llo.getProtocol() != null ? llo.getProtocol() : ProtocolVersionManager.getInstance().getProtocol(lfh.getProtocol(), lfh.getService().isLogin());
				
				try
				{
					SwingUtilities.invokeAndWait(new Runnable()
					{
						@Override
						public void run()
						{
							_list = Loader.getActiveUIPane().addConnection(lfh.getService().isLogin(), lfh.getPackets(), p, protocol);
							// remove any leftover publishes
							_dialog.setMaximum(name, llo.getCount());
						}
					});
				}
				catch (InterruptedException e)
				{
					break;
				}
				catch (InvocationTargetException e)
				{
					LOG.error("Could not add the packet list container! " + name, e);
					continue;
				}
				
				if (isCancelled())
					break;
				
				final HistoricalPacketLog cacheContext = new HistoricalPacketLog(p);
				// skip the desired amount
				for (int offset = 0/*llo.getOffset()*/; offset > 0 && size - ioh.getPositionInChannel(false) > lfh.getFooterSize(); offset--)
				{
					final boolean client = ioh.readBoolean(); // client/server
					final byte[] body = new byte[ioh.readChar()];
					ioh.read(body); // packet
					ioh.readLong(); // time
					if (lfh.getVersion() >= 7)
						ioh.readByte();
					
					sm.onLoadedPacket(lfh.getService().isLogin(), client, body, _list.getProtocol(), cacheContext);
				}
				
				if (isCancelled())
					break;
				
				if (size - ioh.getPositionInChannel(false) <= lfh.getFooterSize())
					continue;
				
				// load packets
				final OpcodeOwnerSet cps, sps;
				if (llo.isDisplayable())
				{
					cps = new OpcodeOwnerSet();
					sps = new OpcodeOwnerSet();
					
					{
						final Frontend ui = Loader.getActiveFrontend();
						cps.addAll(ui.getCurrentlyDisplayedPackets(protocol, EndpointType.CLIENT));
						sps.addAll(ui.getCurrentlyDisplayedPackets(protocol, EndpointType.SERVER));
					}
				}
				else
					cps = sps = null;
				
				// former packet list entry point
				
				final VersionnedPacketTable table = VersionnedPacketTable.getInstance();
				for (int count = llo.getCount(); count > 0 && size - ioh.getPositionInChannel(false) > lfh.getFooterSize(); count--)
				{
					final EndpointType type = EndpointType.valueOf(ioh.readBoolean());
					final byte[] body = new byte[ioh.readChar()];
					ioh.read(body);
					final long time = ioh.readLong();
					final Set<LoggedPacketFlag> flags = lfh.getVersion() >= 7 ? BitMaskUtils.setOf(ioh.readByte(), LoggedPacketFlag.class) : Collections.emptySet();
					
					sm.onLoadedPacket(lfh.getService().isLogin(), type.isClient(), body, protocol, cacheContext);
					
					if (flags.contains(HIDDEN))
						continue;
					
					if (cps != null && sps != null)
					{
						final IPacketTemplate pt = table.getTemplate(protocol, type, body);
						final OpcodeOwnerSet target = type.isClient() ? cps : sps;
						if (!target.contains(pt.isDefined() ? pt : IPacketTemplate.ANY_DYNAMIC_PACKET))
							continue;
					}
					
					publish(new ReceivedPacket(ServiceType.valueOf(lfh.getService().isLogin()), type, body, time));
					
					if (isCancelled())
						break;
					
					// avoid I/O congestion and CPU overload
					// modulo must be low enough and sleep must be large enough
					// to avoid DPC blackouts (e.g. no media skipping when listening to music)
					if (ProxyConfig.PACKET_LOG_LOADING_CPU_YIELD_THRESHOLD > 0 && count % ProxyConfig.PACKET_LOG_LOADING_CPU_YIELD_THRESHOLD == 0)
					{
						try
						{
							Thread.sleep(ProxyConfig.PACKET_LOG_LOADING_CPU_YIELD_DURATION);
						}
						catch (InterruptedException e)
						{
							LOG.info("Cancelled loading " + name);
							break;
						}
					}
				}
			}
			catch (ClosedByInterruptException e)
			{
				LOG.info("Cancelled loading " + name);
				continue;
			}
			catch (IOException | RuntimeException e)
			{
				LOG.error("Failed loading " + name, e);
				continue;
			}
		}
		return null;
	}
}
