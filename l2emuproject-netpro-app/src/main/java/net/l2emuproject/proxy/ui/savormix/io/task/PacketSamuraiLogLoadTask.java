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
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import javax.swing.SwingUtilities;

import net.l2emuproject.io.EmptyChecksum;
import net.l2emuproject.network.mmocore.MMOBuffer;
import net.l2emuproject.network.protocol.IProtocolVersion;
import net.l2emuproject.network.protocol.ProtocolVersionManager;
import net.l2emuproject.proxy.config.ProxyConfig;
import net.l2emuproject.proxy.io.IOConstants;
import net.l2emuproject.proxy.io.NewIOHelper;
import net.l2emuproject.proxy.network.EndpointType;
import net.l2emuproject.proxy.network.ServiceType;
import net.l2emuproject.proxy.network.game.client.L2GameClient;
import net.l2emuproject.proxy.network.game.client.L2GameClientPackets;
import net.l2emuproject.proxy.network.game.server.L2GameServer;
import net.l2emuproject.proxy.network.game.server.L2GameServerPackets;
import net.l2emuproject.proxy.script.LogLoadScriptManager;
import net.l2emuproject.proxy.ui.ReceivedPacket;
import net.l2emuproject.proxy.ui.savormix.loader.Loader;
import net.l2emuproject.util.logging.L2Logger;

/**
 * Loads the specified L2PacketHack packet log files.
 * 
 * @author _dev_
 */
public class PacketSamuraiLogLoadTask extends AbstractLogLoadTask<File> implements IOConstants
{
	private static final L2Logger LOG = L2Logger.getLogger(PacketSamuraiLogLoadTask.class);
	
	/**
	 * Constructs a historical packet log loading task.
	 * 
	 * @param owner associated window
	 */
	public PacketSamuraiLogLoadTask(Window owner)
	{
		super(owner);
	}
	
	@Override
	protected Void doInBackground(File... params)
	{
		for (final File f : params)
		{
			final Path p = f.toPath();
			final String name = p.getFileName().toString();
			
			final int totalPackets;
			final boolean multipart, enc;
			
			final IProtocolVersion protocol;
			try (final SeekableByteChannel channel = Files.newByteChannel(p, StandardOpenOption.READ);
					final NewIOHelper ioh = new NewIOHelper(channel, ByteBuffer.allocate(1024).order(ByteOrder.LITTLE_ENDIAN), EmptyChecksum.getInstance()))
			{
				if (isCancelled())
					break;
				
				if (ioh.readByte() != 7)
					continue;
				
				totalPackets = ioh.readInt();
				// quick prepare
				SwingUtilities.invokeLater(() -> _dialog.setMaximum(name, totalPackets));
				
				// this functions more like 'has one more log file', so the last part will always be unmarked
				multipart = ioh.readBoolean();
				final int part = ioh.readChar(); // current part number, completely useless
				// disallow opening individual parts (cannot decipher/recover opcodes that way)
				if (part > 0)
					continue;
				
				ioh.readChar();
				ioh.readInt();
				ioh.readInt();
				while (ioh.readChar() != 0)
					continue;
				while (ioh.readChar() != 0)
					continue;
				while (ioh.readChar() != 0)
					continue;
				
				// undocumented feature
				if (ioh.readLong() != 0)
					continue;
				
				ioh.readLong();
				enc = ioh.readBoolean();
				
				ioh.skip(12, false);
				
				protocol = ProtocolVersionManager.getInstance().getProtocol(ioh.readInt(), false);
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
			
			if (isCancelled())
				break;
			
			try
			{
				SwingUtilities.invokeAndWait(new Runnable()
				{
					@Override
					public void run()
					{
						_list = Loader.getActiveUIPane().addConnection(false, -1, p, protocol);
						_dialog.setMaximum(name, totalPackets);
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
			
			try
			{
				final PacketLogContext ctx = new PacketLogContext(p, protocol, enc);
				if (parseSinglePart(p, ctx) == Boolean.TRUE && multipart)
				{
					final String filename = p.getFileName().toString();
					final int idx = filename.lastIndexOf('.');
					for (int i = 1;; ++i)
						if (parseSinglePart(p.resolveSibling(filename.substring(0, idx) + "-" + i + filename.substring(idx)), ctx) != Boolean.TRUE)
							break;
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
	
	private Boolean parseSinglePart(Path p, PacketLogContext ctx) throws IOException
	{
		Boolean result = Boolean.FALSE;
		final String name = p.getFileName().toString();
		try (final SeekableByteChannel channel = Files.newByteChannel(p, StandardOpenOption.READ);
				final NewIOHelper ioh = new NewIOHelper(channel, ByteBuffer.allocate(DEFAULT_BUFFER_SIZE).order(ByteOrder.LITTLE_ENDIAN), EmptyChecksum.getInstance()))
		{
			final long size = Files.size(p);
			
			if (isCancelled())
				return null;
			
			ioh.readByte();
			final int totalPackets = ioh.readInt();
			result = ioh.readBoolean();
			try
			{
				SwingUtilities.invokeAndWait(() -> _dialog.setMaximum(name, totalPackets));
			}
			catch (InterruptedException e)
			{
				return null;
			}
			catch (InvocationTargetException e)
			{
				// do nothing
			}
			
			ioh.readChar();
			ioh.readChar();
			ioh.readInt();
			ioh.readInt();
			while (ioh.readChar() != 0)
				continue;
			while (ioh.readChar() != 0)
				continue;
			while (ioh.readChar() != 0)
				continue;
			ioh.readLong();
			ioh.readLong();
			ioh.readBoolean();
			
			if (isCancelled())
				return null;
			
			final LogLoadScriptManager sm = LogLoadScriptManager.getInstance();
			// load packets
			for (int count = totalPackets; count > 0 && size - ioh.getPositionInChannel(false) > 0; count--)
			{
				final EndpointType type = EndpointType.valueOf(!ioh.readBoolean());
				final byte[] body = new byte[ioh.readChar() - 2];
				final long time = ioh.readLong();
				ioh.read(body);
				
				final ByteBuffer wrapper = ByteBuffer.wrap(body).order(ByteOrder.LITTLE_ENDIAN);
				ctx._buf.setByteBuffer(wrapper);
				if (type.isClient())
				{
					if (ctx._enciphered)
					{
						ctx._fakeClient.decipher(wrapper);
						ctx._fakeClient.setFirstTime(false);
					}
					else
					{
						ctx._fakeClient.getDeobfuscator().decodeOpcodes(wrapper);
						wrapper.clear();
					}
					L2GameClientPackets.getInstance().handlePacket(wrapper, ctx._fakeClient, ctx._buf.readUC()).readAndChangeState(ctx._fakeClient, ctx._buf);
				}
				else
				{
					if (ctx._enciphered)
						ctx._fakeServer.decipher(wrapper);
					L2GameServerPackets.getInstance().handlePacket(wrapper, ctx._fakeServer, ctx._buf.readUC()).readAndChangeState(ctx._fakeServer, ctx._buf);
				}
				sm.onLoadedPacket(false, type.isClient(), body, ctx._protocol, ctx._cacheContext, time);
				publish(new ReceivedPacket(ServiceType.GAME, type, body, time));
				
				if (isCancelled())
					return null;
				
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
			
			return result;
		}
		catch (NoSuchFileException | FileNotFoundException e)
		{
			return Boolean.FALSE;
		}
	}
	
	private static final class PacketLogContext
	{
		final IProtocolVersion _protocol;
		final boolean _enciphered;
		
		final HistoricalPacketLog _cacheContext;
		final L2GameClient _fakeClient;
		final L2GameServer _fakeServer;
		final MMOBuffer _buf;
		
		PacketLogContext(Path logFile, IProtocolVersion protocol, boolean enciphered) throws IOException
		{
			_protocol = protocol;
			_enciphered = enciphered;
			
			_cacheContext = new HistoricalPacketLog(logFile);
			_fakeClient = new L2GameClient(null, null);
			_fakeServer = new L2GameServer(null, null, _fakeClient);
			_buf = new MMOBuffer();
		}
	}
}
