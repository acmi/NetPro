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
import java.nio.ByteBuffer;
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
import java.util.zip.Deflater;

import org.apache.commons.lang3.mutable.MutableInt;

import net.l2emuproject.io.UnmanagedResource;
import net.l2emuproject.lang.NetProThreadPriority;
import net.l2emuproject.network.protocol.IProtocolVersion;
import net.l2emuproject.proxy.StartupOption;
import net.l2emuproject.proxy.io.IOConstants;
import net.l2emuproject.proxy.io.NewIOHelper;
import net.l2emuproject.proxy.network.EndpointType;
import net.l2emuproject.proxy.network.Proxy;
import net.l2emuproject.proxy.network.ServiceType;
import net.l2emuproject.proxy.network.listener.ConnectionListener;
import net.l2emuproject.proxy.network.listener.PacketListener;
import net.l2emuproject.proxy.ui.ReceivedPacket;
import net.l2emuproject.util.BitMaskUtils;
import net.l2emuproject.util.Rnd;
import net.l2emuproject.util.logging.L2Logger;

/**
 * This class manages historical packet log file creation and generation.
 * 
 * @author savormix
 */
public class HistoricalLogIOThread extends Thread implements IOConstants, ConnectionListener, PacketListener, NetProThreadPriority
{
	static final L2Logger LOG = L2Logger.getLogger(HistoricalLogIOThread.class);
	
	private final DateFormat _filenameFormat;
	private final Map<Proxy, PacketLog> _files;
	private final Map<Proxy, List<PacketWrapper>> _delayed;
	
	private final BlockingQueue<Object> _actions;
	
	private CaptureController _captureController;
	
	HistoricalLogIOThread()
	{
		super(HistoricalLogIOThread.class.getSimpleName());
		
		setPriority(HISTORICAL_LOG_IO);
		
		_filenameFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.ENGLISH);
		_files = new HashMap<>();
		_delayed = new HashMap<>();
		
		_actions = new LinkedBlockingQueue<>();
		
		net.l2emuproject.lang.management.ShutdownManager.addShutdownHook(() -> {
			LOG.info("Interrupting...");
			HistoricalLogIOThread.this.interrupt();
		});
		
		_captureController = c -> false;
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
				catch (final InterruptedException e)
				{
					break;
				}
				
				if (action instanceof Proxy)
				{
					closeFile(_files.remove(action), ((Proxy)action).getProtocol());
				}
				else if (action instanceof ConnectionWrapper)
				{
					final ConnectionWrapper ci = (ConnectionWrapper)action;
					final Proxy client = ci._client;
					if (client.getTarget() == null)
					{
						// _log.warn("Unfinished connection; delaying logging for " + ci.getClient());
						if (client.isDced())
							continue;
						
						_actions.add(ci);
						
						// let's waste some CPU to see if we should stop using CPU
						// because all that can be done is to keep waiting
						for (final Object pendingAction : _actions)
						{
							// a connection that is still pending completion; safe to wait
							if (pendingAction instanceof ConnectionWrapper && ((ConnectionWrapper)pendingAction)._client.getTarget() == null)
								continue;
							
							// any other action should be processed immediately
							continue actionHandling;
						}
						
						// there's no real work; yield the CPU
						try
						{
							Thread.sleep(1);
						}
						catch (final InterruptedException e)
						{
							break;
						}
						continue;
					}
					openFile(ci);
				}
				else if (action instanceof PacketWrapper)
				{
					final PacketLog log;
					final NewIOHelper ioh;
					List<PacketWrapper> delayed;
					{
						final PacketWrapper pi = (PacketWrapper)action;
						final Proxy client = pi._client;
						log = _files.get(client);
						if (log == null)
						{
							if (_actions.isEmpty())
							{
								LOG.warn("No open log for " + client + ", discarding packet.");
								continue;
							}
							delayed = _delayed.computeIfAbsent(client, c -> new LinkedList<>());
							delayed.add(pi);
							continue;
						}
						
						ioh = log.getWriter();
						
						delayed = _delayed.remove(client);
						if (delayed != null)
							delayed.add(pi);
						else
							delayed = Collections.singletonList(pi);
					}
					
					try
					{
						for (final PacketWrapper pi : delayed)
						{
							final ReceivedPacket packet = pi._packet;
							final byte[] buf = packet.getBody();
							
							Deflater deflater = log.getDeflater();
							if (deflater != null)
							{
								ByteBuffer deflateBuffer = log.getDeflateBuffer();
								deflateBuffer.put((byte)(packet.getEndpoint().isClient() ? 1 : 0));
								deflateBuffer.putChar((char)buf.length);
								deflateBuffer.put(buf);
								deflateBuffer.putLong(packet.getReceived());
								deflateBuffer.put((byte)BitMaskUtils.maskOf(pi._flags));
								if (deflateBuffer.position() > (deflateBuffer.capacity() >> 1))
									writeBlock(log);
							}
							else
							{
								ioh.writeBoolean(packet.getEndpoint().isClient());
								ioh.writeChar(buf.length);
								ioh.write(buf);
								ioh.writeLong(packet.getReceived());
								ioh.writeByte((int)BitMaskUtils.maskOf(pi._flags));
							}
							log.onPacket(packet);
						}
					}
					catch (final IOException e)
					{
						_files.remove(((PacketWrapper)action)._client);
						UnmanagedResource.close(ioh);
						LOG.error("A packet cannot be logged to file!", e);
					}
				}
				else
					LOG.error("Unknown action: " + action);
			}
			catch (final Throwable t)
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
		for (final Entry<Proxy, PacketLog> e : _files.entrySet())
			closeFile(e.getValue(), e.getKey().getProtocol());
		
		LOG.info("Packet logging terminated successfully.");
	}
	
	/**
	 * Opens a file to log packets to.<BR>
	 * <BR>
	 * Does nothing if connection is already being logged.
	 * 
	 * @param connection
	 *            pending connection
	 */
	private void openFile(ConnectionWrapper connection)
	{
		final Proxy provider = connection._client;
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
		
		final ServiceType type = ServiceType.valueOf(connection._client.getProtocol());
		
		final Path log;
		{
			Path dir = type.isLogin() ? LOGIN_LOG_DIRECTORY : GAME_LOG_DIRECTORY;
			dir = dir.resolve(provider.getTarget().getHostAddress());
			try
			{
				Files.createDirectories(dir);
			}
			catch (final IOException e)
			{
				LOG.error("Cannot create packet log directory.", e);
				return;
			}
			
			final StringBuilder fn = new StringBuilder();
			
			{
				if (StartupOption.FORCE_FULL_LOG_FILENAME.isSet())
					fn.append(type.isLogin() ? 'L' : 'G').append(provider.getTarget().getHostAddress()).append('_');
				
				final Date d = new Date(connection._time);
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
			ioh.writeInt(8 + 1 + 4 + 4 + 8 + 8 + 1 + 4 + 8 + 1).writeInt(0).writeLong(-1); // header size, footer size, footer start
			ioh.writeLong(connection._time).writeBoolean(type.isLogin()).writeInt(-1); // protocol version
			ioh.writeLong(0).writeByte(COMPRESSION_TYPE); // total packet data size, compression type
			_files.put(provider, new PacketLog(ioh, new Deflater(Deflater.DEFAULT_COMPRESSION, true)));
		}
		catch (final IOException e)
		{
			UnmanagedResource.close(chan);
			LOG.error("Cannot open packet log file!", e);
		}
	}
	
	private static void writeBlock(PacketLog log) throws IOException
	{
		NewIOHelper ioh = log.getWriter();
		final long totalSizePos = ioh.getPositionInChannel(true);
		ioh.writeInt(0); // deflated size
		int totalDeflatedSize = 0;
		
		final Deflater deflater = log.getDeflater();
		final ByteBuffer deflateBuffer = log.getDeflateBuffer();
		final byte[] outputBuffer = log.getOutputBuffer();
		deflater.setInput(deflateBuffer.array(), 0, deflateBuffer.position());
		int deflatedSize;
		// UNLIKE zlib, do not initiate with SYNC_FLUSH and continue with NO_FLUSH until the marker is flushed; a different approach must be used according to the JavaDoc:
		// In the case of FULL_FLUSH or SYNC_FLUSH, if the return value is len, the space available in outputbuffer b, this method should be invoked again with the same flush parameter and more output space.
		do
		{
			deflatedSize = deflater.deflate(outputBuffer, 0, outputBuffer.length, Deflater.SYNC_FLUSH);
			ioh.write(outputBuffer, 0, deflatedSize);
			totalDeflatedSize += deflatedSize;
			//int toOutput = Math.min(deflatedSize, 4);
			//LOG.info("Flush ended in " + HexUtil.bytesToHexString(outputBuffer, deflatedSize - toOutput, toOutput, " "));
		}
		while (deflatedSize == outputBuffer.length);
		deflateBuffer.clear();
		ioh.flush();
		final long blockEndPos = ioh.getPositionInChannel(true) - 4;
		ioh.setPositionInChannel(totalSizePos);
		ioh.writeInt(totalDeflatedSize - 4).flush();
		ioh.setPositionInChannel(blockEndPos);
	}
	
	private static void closeFile(PacketLog log, IProtocolVersion protocol)
	{
		if (log == null)
			return;
		
		try (final NewIOHelper ioh = log.getWriter())
		{
			if (log.getDeflater() != null)
			{
				writeBlock(log);
				ioh.writeInt(-1);
			}
			
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
			
			final Set<String> altModes = protocol.getAltModes();
			final int modeCount = Math.min(0xFF, altModes.size());
			ioh.writeByte(modeCount);
			for (final String altMode : altModes)
			{
				final int modeLength = Math.min(0xFF, altMode.length());
				ioh.writeByte(modeLength);
				for (int i = 0; i < modeLength; ++i)
					ioh.writeChar(altMode.charAt(i));
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
				ioh.writeLong(log.getTotalPacketBytes());
			}
			ioh.flush();
			{
				// log successfully finalized
				ioh.setPositionInChannel(0L);
				ioh.writeLong(LOG_MAGIC);
			}
			ioh.flush();
		}
		catch (final IOException e)
		{
			LOG.error("Cannot finalize packet log file!", e);
		}
	}
	
	private static class ConnectionWrapper
	{
		final Proxy _client;
		final long _time;
		
		ConnectionWrapper(Proxy client)
		{
			_client = client;
			_time = System.currentTimeMillis();
		}
	}
	
	private static class PacketWrapper
	{
		final Proxy _client;
		final ReceivedPacket _packet;
		final Set<LoggedPacketFlag> _flags;
		
		PacketWrapper(Proxy provider, ReceivedPacket packet, Set<LoggedPacketFlag> flags)
		{
			_client = provider;
			_packet = packet;
			_flags = flags;
		}
	}
	
	@Override
	public void onClientPacket(Proxy sender, Proxy recipient, ByteBuffer packet, long time) throws RuntimeException
	{
		final byte[] body = new byte[packet.clear().limit()];
		packet.get(body);
		_actions.add(new PacketWrapper(sender, new ReceivedPacket(ServiceType.valueOf(sender.getProtocol()), sender.getType(), body, time), getPacketFlags(sender)));
	}
	
	@Override
	public void onServerPacket(Proxy sender, Proxy recipient, ByteBuffer packet, long time) throws RuntimeException
	{
		final byte[] body = new byte[packet.clear().limit()];
		packet.get(body);
		_actions.add(new PacketWrapper(recipient, new ReceivedPacket(ServiceType.valueOf(sender.getProtocol()), sender.getType(), body, time), getPacketFlags(recipient)));
	}
	
	@Override
	public void onProtocolVersion(Proxy affected, IProtocolVersion version) throws RuntimeException
	{
		// ignore
	}
	
	@Override
	public void onClientConnection(Proxy client)
	{
		_actions.add(new ConnectionWrapper(client));
	}
	
	@Override
	public void onServerConnection(Proxy server)
	{
		// ignore
	}
	
	@Override
	public void onDisconnection(Proxy client, Proxy server)
	{
		_actions.add(client);
	}
	
	public void setCaptureController(CaptureController controller)
	{
		_captureController = controller;
	}
	
	private static final Set<LoggedPacketFlag> CAPTURE_DISABLED_FLAGS = Collections.singleton(LoggedPacketFlag.HIDDEN);
	
	private final Set<LoggedPacketFlag> getPacketFlags(Proxy client)
	{
		return _captureController.isCaptureDisabledFor(client) ? CAPTURE_DISABLED_FLAGS : Collections.emptySet();
	}
	
	public interface CaptureController
	{
		boolean isCaptureDisabledFor(Proxy client);
	}
	
	/**
	 * Returns a singleton instance of this type.
	 * 
	 * @return an instance of this class
	 */
	public static final HistoricalLogIOThread getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static final class SingletonHolder
	{
		static final HistoricalLogIOThread INSTANCE;
		static
		{
			(INSTANCE = new HistoricalLogIOThread()).start();
		}
	}
}
