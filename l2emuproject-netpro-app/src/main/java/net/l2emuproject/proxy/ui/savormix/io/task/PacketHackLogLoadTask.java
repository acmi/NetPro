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
import java.io.BufferedReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.ClosedByInterruptException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map.Entry;

import javax.swing.SwingUtilities;

import net.l2emuproject.network.IGameProtocolVersion;
import net.l2emuproject.network.IProtocolVersion;
import net.l2emuproject.proxy.config.ProxyConfig;
import net.l2emuproject.proxy.network.EndpointType;
import net.l2emuproject.proxy.network.ServiceType;
import net.l2emuproject.proxy.script.LogLoadScriptManager;
import net.l2emuproject.proxy.ui.ReceivedPacket;
import net.l2emuproject.proxy.ui.savormix.io.base.IOConstants;
import net.l2emuproject.proxy.ui.savormix.loader.Loader;
import net.l2emuproject.util.HexUtil;
import net.l2emuproject.util.logging.L2Logger;

/**
 * Loads the specified L2PacketHack packet log files.
 * 
 * @author _dev_
 */
public class PacketHackLogLoadTask extends AbstractLogLoadTask<Entry<Path, IGameProtocolVersion>>implements IOConstants
{
	private static final L2Logger LOG = L2Logger.getLogger(PacketHackLogLoadTask.class);
	
	private final ByteBuffer _time;
	
	/**
	 * Constructs a historical packet log loading task.
	 * 
	 * @param owner associated window
	 */
	public PacketHackLogLoadTask(Window owner)
	{
		super(owner);
		
		_time = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN);
	}
	
	@Override
	protected Void doInBackground(@SuppressWarnings("unchecked") Entry<Path, IGameProtocolVersion>... params)
	{
		for (final Entry<Path, IGameProtocolVersion> log : params)
		{
			final Path p = log.getKey();
			final String name = p.getFileName().toString();
			
			// quick prepare
			SwingUtilities.invokeLater(() -> _dialog.setMaximum(name, Integer.MAX_VALUE));
			
			final LogLoadScriptManager sm = LogLoadScriptManager.getInstance();
			
			if (isCancelled())
				break;
				
			try (final BufferedReader br = Files.newBufferedReader(p, StandardCharsets.US_ASCII))
			{
				if (isCancelled())
					break;
					
				final IProtocolVersion protocol = log.getValue();
				
				try
				{
					SwingUtilities.invokeAndWait(new Runnable()
					{
						@Override
						public void run()
						{
							_list = Loader.getActiveUIPane().addConnection(false, -1, p, protocol);
							// remove any leftover publishes
							_dialog.setMaximum(name, Integer.MAX_VALUE);
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
				String line;
				for (int offset = 0/*llo.getOffset()*/; offset > 0 && (line = br.readLine()) != null; offset--)
				{
					final boolean client = isClientPacket(line); // client/server
					final byte[] body = getBody(line);
					
					sm.onLoadedPacket(false, client, body, protocol, cacheContext);
				}
				
				if (isCancelled())
					break;
					
				// load packets
				for (int count = Integer.MAX_VALUE; count > 0 && (line = br.readLine()) != null; count--)
				{
					final EndpointType type = EndpointType.valueOf(isClientPacket(line));
					final byte[] body = getBody(line);
					final long time = getTime(line);
					
					sm.onLoadedPacket(false, type.isClient(), body, protocol, cacheContext);
					
					publish(new ReceivedPacket(ServiceType.GAME, type, body, time));
					
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
	
	private boolean isClientPacket(String packet)
	{
		return packet.charAt(1) == '4';
	}
	
	private long getTime(String packet)
	{
		_time.position(0);
		_time.put(HexUtil.hexStringToBytes(packet.substring(2, 18)));
		return _time.getLong(0);
	}
	
	private byte[] getBody(String packet)
	{
		return HexUtil.hexStringToBytes(packet.substring(22));
	}
}
