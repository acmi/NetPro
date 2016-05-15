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
package net.l2emuproject.proxy.io.packetlog;

import java.io.IOException;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.commons.lang3.mutable.MutableInt;

import net.l2emuproject.io.UnmanagedResource;
import net.l2emuproject.network.protocol.IProtocolVersion;
import net.l2emuproject.proxy.io.IOConstants;
import net.l2emuproject.proxy.io.NewIOHelper;
import net.l2emuproject.proxy.network.EndpointType;
import net.l2emuproject.proxy.network.Proxy;
import net.l2emuproject.proxy.network.ServiceType;
import net.l2emuproject.proxy.ui.ReceivedPacket;
import net.l2emuproject.proxy.ui.savormix.loader.LoadOption;
import net.l2emuproject.util.BitMaskUtils;
import net.l2emuproject.util.Rnd;
import net.l2emuproject.util.logging.L2Logger;

/**
 * This class manages packet log file creation and generation.
 * 
 * @author savormix
 */
// FIXME: if
public class PacketLogThread extends Thread implements IOConstants
{
	static final L2Logger LOG = L2Logger.getLogger(PacketLogThread.class);
	
	private final DateFormat _filenameFormat;
	private final Map<Proxy, PacketLog> _files;
	private final Map<Proxy, List<PackInfo>> _delayed;
	
	private final BlockingQueue<Object> _actions;
	
	PacketLogThread()
	{
		super("PacketLogThread");
		
		setPriority(NORM_PRIORITY - 1);
		
		_filenameFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.ENGLISH);
		_files = new HashMap<>();
		_delayed = new HashMap<>();
		
		_actions = new LinkedBlockingQueue<>();
		
		net.l2emuproject.lang.management.ShutdownManager.addShutdownHook(() ->
		{
			LOG.info("Interrupting...");
			PacketLogThread.this.interrupt();
		});
	}
	
	@Override
	public void run()
	{
		actionHandling: while (true)
		{
			try
			{
				if (isInterrupted())
					break;
					
				final Object action;
				try
				{
					action = _actions.take();
				}
				catch (InterruptedException e)
				{
					break;
				}
				
				if (action instanceof Proxy)
				{
					closeFile(_files.remove(action), ((Proxy)action).getProtocol());
				}
				else if (action instanceof ConInfo)
				{
					final ConInfo ci = (ConInfo)action;
					final Proxy client = ci.getClient();
					if (client.getTarget() == null)
					{
						// _log.warn("Unfinished connection; delaying logging for " + ci.getClient());
						if (client.isDced())
							continue;
							
						_actions.add(ci);
						
						// let's waste some CPU to see if we should stop using CPU
						// because all that can be done is to keep waiting
						for (Object pendingAction : _actions)
						{
							// a connection that is still pending completion; safe to wait 
							if (pendingAction instanceof ConInfo && ((ConInfo)pendingAction).getClient().getTarget() == null)
								continue;
								
							// any other action should be processed immediately
							continue actionHandling;
						}
						
						// there's no real work; yield the CPU
						try
						{
							Thread.sleep(1);
						}
						catch (InterruptedException e)
						{
							break;
						}
						continue;
					}
					openFile(ci);
				}
				else if (action instanceof PackInfo)
				{
					final PacketLog log;
					final NewIOHelper ioh;
					List<PackInfo> delayed;
					{
						final PackInfo pi = (PackInfo)action;
						log = _files.get(pi.getClient());
						if (log == null)
						{
							if (_actions.isEmpty())
							{
								LOG.warn("No open log for " + pi.getClient() + ", discarding packet.");
								continue;
							}
							delayed = _delayed.get(pi.getClient());
							if (delayed == null)
							{
								delayed = new LinkedList<>();
								_delayed.put(pi.getClient(), delayed);
							}
							delayed.add(pi);
							continue;
						}
						
						ioh = log.getWriter();
						
						delayed = _delayed.remove(pi.getClient());
						if (delayed != null)
							delayed.add(pi);
						else
							delayed = Collections.singletonList(pi);
					}
					
					try
					{
						for (final PackInfo pi : delayed)
						{
							final ReceivedPacket packet = pi.getPacket();
							final byte[] buf = packet.getBody(); // DO NOT LINK!
							
							ioh.writeBoolean(packet.getEndpoint().isClient());
							ioh.writeChar(buf.length);
							ioh.write(buf);
							ioh.writeLong(packet.getReceived());
							ioh.writeByte((int)BitMaskUtils.maskOf(pi.getFlags()));
							
							log.onPacket(packet);
						}
					}
					catch (IOException e)
					{
						_files.remove(((PackInfo)action).getClient());
						UnmanagedResource.close(ioh);
						LOG.error("A packet cannot be logged to file!", e);
					}
				}
				else
					LOG.error("Unknown action: " + action);
			}
			catch (Throwable t)
			{
				LOG.error("Generic error", t);
			}
		}
		
		if (!_actions.isEmpty())
		{
			LOG.info("There are " + _actions.size() + " unfinished tasks that will not be completed.");
			_actions.clear();
		}
		_delayed.clear();
		
		LOG.info("Finalizing open files.");
		for (Entry<Proxy, PacketLog> e : _files.entrySet())
			closeFile(e.getValue(), e.getKey().getProtocol());
			
		LOG.info("Packet logging terminated successfully.");
	}
	
	/**
	 * Notifies the packet log I/O thread about a new log to be created.
	 * 
	 * @param client newly connected client
	 */
	public void fireConnection(Proxy client)
	{
		_actions.add(new ConInfo(client));
	}
	
	/**
	 * Notifies the packet log I/O thread about a completed log that requires finalization.
	 * 
	 * @param client disconnected client
	 */
	public void fireDisconnection(Proxy client)
	{
		_actions.add(client);
	}
	
	/**
	 * Notifies the packet log I/O thread about a new packet to be logged.
	 * 
	 * @param client connected client
	 * @param packet received packet
	 * @param flags additional info about the packet
	 */
	public void firePacket(Proxy client, ReceivedPacket packet, Set<LoggedPacketFlag> flags)
	{
		_actions.add(new PackInfo(client, packet, flags));
	}
	
	/**
	 * Opens a file to log packets to.<BR>
	 * <BR>
	 * Does nothing if connection is already being logged.
	 * 
	 * @param connection
	 *            pending connection
	 */
	private void openFile(ConInfo connection)
	{
		final Proxy provider = connection.getClient();
		if (provider.isDced())
		{
			LOG.warn("Log will not be opened for " + provider);
			return;
		}
		
		{
			final PacketLog log = _files.get(provider);
			if (log != null)
			{
				LOG.warn("Log already open for " + provider);
				return;
			}
		}
		
		final ServiceType type = ServiceType.valueOf(connection.getClient().getProtocol());
		
		final Path log;
		{
			Path dir = type.isLogin() ? LOGIN_LOG_DIRECTORY : GAME_LOG_DIRECTORY;
			dir = dir.resolve(provider.getTarget().getHostAddress());
			try
			{
				Files.createDirectories(dir);
			}
			catch (IOException e)
			{
				LOG.error("Cannot create packet log directory.", e);
				return;
			}
			
			final StringBuilder fn = new StringBuilder();
			
			{
				if (LoadOption.FORCE_FULL_LOG_FILENAME.isSet())
					fn.append(type.isLogin() ? 'L' : 'G').append(provider.getTarget().getHostAddress()).append('_');
					
				final Date d = new Date(connection.getTime());
				fn.append(_filenameFormat.format(d)).append('_');
				fn.append(Rnd.getString(5, Rnd.LETTERS_AND_DIGITS)).append('.').append(LOG_EXTENSION);
			}
			log = dir.resolve(fn.toString());
		}
		
		SeekableByteChannel chan = null;
		try
		{
			chan = Files.newByteChannel(log, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
			final NewIOHelper ioh = new NewIOHelper(chan);
			
			ioh.writeLong(LOG_MAGIC_INCOMPLETE).writeByte(LOG_VERSION);
			ioh.writeInt(8 + 1 + 4 + 4 + 8 + 8 + 1 + 4).writeInt(0).writeLong(-1); // header size, footer size, footer start
			ioh.writeLong(connection.getTime()).writeBoolean(type.isLogin()).writeInt(-1); // protocol version
			
			_files.put(provider, new PacketLog(ioh));
		}
		catch (IOException e)
		{
			UnmanagedResource.close(chan);
			LOG.error("Cannot open packet log file!", e);
		}
	}
	
	@SuppressWarnings("static-method")
	private void closeFile(PacketLog log, IProtocolVersion protocol)
	{
		if (log == null)
			return;
			
		try (final NewIOHelper ioh = log.getWriter())
		{
			final long footerStartPos = ioh.getPositionInChannel(true);
			
			ioh.writeInt(log.getTotal()); // total packets in file
			
			ioh.writeByte(EndpointType.values().length);
			{
				final Map<Integer, MutableInt> map = log.getCp();
				ioh.writeBoolean(EndpointType.CLIENT.isClient()).writeInt(map.size());
				for (final Entry<Integer, MutableInt> e : map.entrySet())
					ioh.writeInt(e.getKey()).writeInt(e.getValue().intValue()); // packet & count
			}
			{
				final Map<Integer, MutableInt> map = log.getSp();
				ioh.writeBoolean(EndpointType.SERVER.isClient()).writeInt(map.size());
				for (final Entry<Integer, MutableInt> e : map.entrySet())
					ioh.writeInt(e.getKey()).writeInt(e.getValue().intValue()); // packet & count
			}
			
			final int fs = (int)(ioh.getPositionInChannel(true) - footerStartPos);
			
			ioh.flush();
			{
				ioh.setPositionInChannel(LOG_FOOTER_SIZE_POS);
				ioh.writeInt(fs).writeLong(footerStartPos);
			}
			ioh.flush();
			{
				ioh.setPositionInChannel(LOG_PROTOCOL_POS);
				ioh.writeInt(protocol.getVersion());
			}
			ioh.flush();
			{
				// log successfully finalized
				ioh.setPositionInChannel(0L);
				ioh.writeLong(LOG_MAGIC);
			}
			ioh.flush();
		}
		catch (IOException e)
		{
			LOG.error("Cannot finalize packet log file!", e);
		}
	}
	
	private static class ConInfo
	{
		private final Proxy _client;
		private final long _time;
		
		ConInfo(Proxy client)
		{
			_client = client;
			_time = System.currentTimeMillis();
		}
		
		public Proxy getClient()
		{
			return _client;
		}
		
		public long getTime()
		{
			return _time;
		}
	}
	
	private static class PackInfo
	{
		private final Proxy _client;
		private final ReceivedPacket _packet;
		private final Set<LoggedPacketFlag> _flags;
		
		PackInfo(Proxy provider, ReceivedPacket packet, Set<LoggedPacketFlag> flags)
		{
			_client = provider;
			_packet = packet;
			_flags = flags;
		}
		
		public Proxy getClient()
		{
			return _client;
		}
		
		public ReceivedPacket getPacket()
		{
			return _packet;
		}
		
		public Set<LoggedPacketFlag> getFlags()
		{
			return _flags;
		}
	}
}
