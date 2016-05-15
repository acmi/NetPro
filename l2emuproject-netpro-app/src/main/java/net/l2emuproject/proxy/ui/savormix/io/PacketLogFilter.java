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
package net.l2emuproject.proxy.ui.savormix.io;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Map.Entry;
import java.util.concurrent.Future;

import javax.swing.filechooser.FileFilter;

import net.l2emuproject.network.protocol.IProtocolVersion;
import net.l2emuproject.network.protocol.ProtocolVersionManager;
import net.l2emuproject.proxy.io.NewIOHelper;
import net.l2emuproject.proxy.io.definitions.VersionnedPacketTable;
import net.l2emuproject.proxy.io.packetlog.LogFileHeader;
import net.l2emuproject.proxy.network.EndpointType;
import net.l2emuproject.proxy.network.meta.IPacketTemplate;
import net.l2emuproject.proxy.network.meta.container.OpcodeOwnerSet;
import net.l2emuproject.proxy.ui.savormix.loader.Frontend;
import net.l2emuproject.proxy.ui.savormix.loader.Loader;
import net.l2emuproject.ui.file.BetterExtensionFilter;
import net.l2emuproject.util.concurrent.L2ThreadPool;
import net.l2emuproject.util.logging.L2Logger;

/**
 * @author savormix
 */
public class PacketLogFilter extends FileFilter
{
	static final L2Logger _log = L2Logger.getLogger(PacketLogFilter.class);
	
	private final FileFilter _extFilter;
	private boolean _displayable;
	
	LegacyLogFilter _legacyFilter;
	String _method;
	boolean _result;
	
	PacketLogFilter()
	{
		_extFilter = BetterExtensionFilter.create("PA&VT/NetPro packet log", "plog");
		_displayable = false;
		_legacyFilter = null;
	}
	
	void setDisplayable(boolean displayable)
	{
		_displayable = displayable;
	}
	
	boolean isDisplayable()
	{
		return _displayable;
	}
	
	boolean isLegacyFiltered()
	{
		return _legacyFilter != null;
	}
	
	void setLegacyFilter(LegacyLogFilter legacyFilter)
	{
		_legacyFilter = legacyFilter;
	}
	
	@Override
	public boolean accept(final File pathname)
	{
		{
			final boolean ext = _extFilter.accept(pathname);
			if (!ext || !isDisplayable())
				return ext;
		}
		
		// Illegal char <:> at index 0: ::{20D04FE0-3AEA-1069-A2D8-08002B30309D}
		// when opening Desktop, Win x64 Java 7u45
		
		//final Path path = pathname.toPath();
		//if (Files.isDirectory(path))
		//	return true;
		
		if (pathname.isDirectory())
			return true;
		
		// passed extension check, now test log footer
		_result = false;
		_method = "accept(File)";
		
		//final long start = System.nanoTime();
		final Future<?> managed = L2ThreadPool.submitLongRunning(new Runnable()
		{
			private Path _p;
			private LogFileHeader _header;
			private IProtocolVersion _protocol;
			private Frontend _ui;
			private VersionnedPacketTable _pt;
			
			@Override
			public void run()
			{
				_result = accept();
			}
			
			private boolean acceptLegacy()
			{
				_method = "acceptLegacy()";
				
				final OpcodeOwnerSet cps = getDisplayable(EndpointType.CLIENT), sps = getDisplayable(EndpointType.SERVER);
				
				if (cps.isEmpty() && sps.isEmpty())
					return false;
				
				try (final SeekableByteChannel channel = Files.newByteChannel(_p, StandardOpenOption.READ); final NewIOHelper ioh = new NewIOHelper(channel))
				{
					final long size = Files.size(_p);
					
					ioh.read(new byte[_header.getHeaderSize()]);
					
					while (size - ioh.getPositionInChannel(false) > _header.getFooterSize())
					{
						final EndpointType type = EndpointType.valueOf(ioh.readBoolean());
						final byte[] body = new byte[ioh.readChar()];
						ioh.read(body);
						
						final IPacketTemplate pt = _pt.getTemplate(_protocol, type, body);
						final OpcodeOwnerSet target = type.isClient() ? cps : sps;
						if (target.contains(pt.isDefined() ? pt : IPacketTemplate.ANY_DYNAMIC_PACKET))
							return true;
						
						ioh.readLong();
					}
				}
				catch (IOException | RuntimeException e)
				{
					_log.error("Could not read legacy packet log: " + _p.getFileName(), e);
				}
				
				return false;
			}
			
			private boolean accept()
			{
				_header = /*LogIdentifyTask.getHeader(_p = pathname.toPath())*/null;
				if (_header == null)
					return false;
				
				if (_header.getVersion() < 6)
				{
					if (!isLegacyFiltered())
						return false;
					
					preinit();
					return acceptLegacy();
				}
				
				preinit();
				for (final EndpointType type : EndpointType.values())
				{
					final OpcodeOwnerSet disp = getDisplayable(type);
					if (disp.isEmpty())
						continue;
					
					final ByteBuffer buf = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN);
					for (final Entry<Integer, Integer> e : (type.isClient() ? _header.getCp() : _header.getSp()).entrySet())
					{
						if (e.getValue() < 1)
							continue;
						
						buf.putInt(0, e.getKey());
						final IPacketTemplate pt = _pt.getTemplate(_protocol, type, buf.array());
						if (disp.contains(pt.isDefined() ? pt : IPacketTemplate.ANY_DYNAMIC_PACKET))
							return true;
					}
				}
				
				return false;
			}
			
			private void preinit()
			{
				if (_header.getProtocol() == -1 && isLegacyFiltered())
					_protocol = _header.getService().isLogin() ? _legacyFilter._loginProtocol : _legacyFilter._gameProtocol;
				else
					_protocol = ProtocolVersionManager.getInstance().getProtocol(_header.getProtocol(), _header.getService().isLogin());
				
				_ui = Loader.getActiveFrontend();
				_pt = VersionnedPacketTable.getInstance();
			}
			
			private OpcodeOwnerSet getDisplayable(EndpointType type)
			{
				final OpcodeOwnerSet disp = new OpcodeOwnerSet();
				disp.addAll(_ui.getCurrentlyDisplayedPackets(_protocol, type));
				return disp;
			}
		});
		
		try
		{
			managed.get();
			// TODO: time limit/log etc
			
			//final long end = System.nanoTime();
			//RunnableStatsManager.handleStats(getClass(), _method, end - start, 50_000_000);
			// _log.info("Verified in " + ((end - start) / 1_000_000) + " ms.");
		}
		catch (Exception e)
		{
			_log.info("Could not check displayable packets for " + pathname.getName(), e);
		}
		
		return _result;
	}
	
	@Override
	public String getDescription()
	{
		return _extFilter.getDescription();
	}
	
	static final class LegacyLogFilter
	{
		final IProtocolVersion _loginProtocol, _gameProtocol;
		
		LegacyLogFilter(IProtocolVersion loginProtocol, IProtocolVersion gameProtocol)
		{
			_loginProtocol = loginProtocol;
			_gameProtocol = gameProtocol;
		}
	}
}
