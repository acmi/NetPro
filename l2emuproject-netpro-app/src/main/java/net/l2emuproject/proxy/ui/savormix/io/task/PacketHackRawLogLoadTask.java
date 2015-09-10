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
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Map.Entry;

import javax.swing.SwingUtilities;

import net.l2emuproject.io.EmptyChecksum;
import net.l2emuproject.network.IGameProtocolVersion;
import net.l2emuproject.network.IProtocolVersion;
import net.l2emuproject.network.mmocore.DataSizeHolder;
import net.l2emuproject.network.mmocore.MMOBuffer;
import net.l2emuproject.proxy.config.ProxyConfig;
import net.l2emuproject.proxy.network.EndpointType;
import net.l2emuproject.proxy.network.ServiceType;
import net.l2emuproject.proxy.network.game.client.L2GameClient;
import net.l2emuproject.proxy.network.game.client.L2GameClientPackets;
import net.l2emuproject.proxy.network.game.server.L2GameServer;
import net.l2emuproject.proxy.network.game.server.L2GameServerPackets;
import net.l2emuproject.proxy.script.LogLoadScriptManager;
import net.l2emuproject.proxy.ui.ReceivedPacket;
import net.l2emuproject.proxy.ui.savormix.io.base.IOConstants;
import net.l2emuproject.proxy.ui.savormix.io.base.NewIOHelper;
import net.l2emuproject.proxy.ui.savormix.loader.Loader;
import net.l2emuproject.util.logging.L2Logger;

/**
 * Loads the specified L2PacketHack packet log files.
 * 
 * @author _dev_
 */
public class PacketHackRawLogLoadTask extends AbstractLogLoadTask<Entry<Path, IGameProtocolVersion>>implements IOConstants
{
	private static final L2Logger LOG = L2Logger.getLogger(PacketHackRawLogLoadTask.class);
	
	/**
	 * Constructs a historical packet log loading task.
	 * 
	 * @param owner associated window
	 */
	public PacketHackRawLogLoadTask(Window owner)
	{
		super(owner);
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
				
			try (final SeekableByteChannel channel = Files.newByteChannel(p, StandardOpenOption.READ);
					final NewIOHelper ioh = new NewIOHelper(channel, ByteBuffer.allocate(DEFAULT_BUFFER_SIZE).order(ByteOrder.LITTLE_ENDIAN), EmptyChecksum.getInstance()))
			{
				final long size = Files.size(p);
				
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
				final L2GameClient fakeClient = new L2GameClient(null, null);
				final L2GameServer fakeServer = new L2GameServer(null, null, fakeClient);
				final DataSizeHolder sz = new DataSizeHolder();
				final MMOBuffer buf = new MMOBuffer();
				// skip the desired amount
				for (int offset = 0/*llo.getOffset()*/; offset > 0 && size - ioh.getPositionInChannel(false) > 0; offset--)
				{
					final boolean client = ioh.readByte() == 4; // client/server
					final byte[] body = new byte[ioh.readChar() - 2];
					ioh.readLong(); // time
					ioh.readChar();
					ioh.read(body); // packet
					
					final ByteBuffer wrapper = ByteBuffer.wrap(body).order(ByteOrder.LITTLE_ENDIAN);
					sz.init(body.length);
					if (client)
					{
						fakeClient.decipher(wrapper, sz);
						fakeClient.setFirstTime(false);
						L2GameClientPackets.getInstance().handlePacket(wrapper, fakeClient, wrapper.get()).readAndChangeState(fakeClient, buf);
					}
					else
					{
						fakeServer.decipher(wrapper, sz);
						L2GameServerPackets.getInstance().handlePacket(wrapper, fakeServer, wrapper.get()).readAndChangeState(fakeServer, buf);
					}
					sm.onLoadedPacket(false, client, body, _list.getProtocol(), cacheContext);
				}
				
				if (isCancelled())
					break;
					
				if (size - ioh.getPositionInChannel(false) <= 0)
					continue;
					
				// load packets
				for (int count = Integer.MAX_VALUE; count > 0 && size - ioh.getPositionInChannel(false) > 0; count--)
				{
					final EndpointType type = EndpointType.valueOf(ioh.readByte() == 4);
					final byte[] body = new byte[ioh.readChar() - 2];
					final long time = ioh.readLong();
					ioh.readChar();
					ioh.read(body); // packet
					
					final ByteBuffer wrapper = ByteBuffer.wrap(body).order(ByteOrder.LITTLE_ENDIAN);
					sz.init(body.length);
					buf.setByteBuffer(wrapper);
					if (type.isClient())
					{
						fakeClient.decipher(wrapper, sz);
						fakeClient.setFirstTime(false);
						L2GameClientPackets.getInstance().handlePacket(wrapper, fakeClient, wrapper.get()).readAndChangeState(fakeClient, buf);
					}
					else
					{
						fakeServer.decipher(wrapper, sz);
						L2GameServerPackets.getInstance().handlePacket(wrapper, fakeServer, wrapper.get()).readAndChangeState(fakeServer, buf);
					}
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
}
