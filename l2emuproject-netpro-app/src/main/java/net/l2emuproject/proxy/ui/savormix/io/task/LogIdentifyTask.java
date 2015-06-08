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
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.l2emuproject.network.IProtocolVersion;
import net.l2emuproject.proxy.network.EndpointType;
import net.l2emuproject.proxy.ui.savormix.io.LogFileHeader;
import net.l2emuproject.proxy.ui.savormix.io.base.IOConstants;
import net.l2emuproject.proxy.ui.savormix.io.base.NewIOHelper;
import net.l2emuproject.proxy.ui.savormix.io.dialog.LogIdentificationProgress;
import net.l2emuproject.proxy.ui.savormix.io.dialog.LogLoadHelper;
import net.l2emuproject.ui.AsyncTask;
import net.l2emuproject.util.logging.L2Logger;

/**
 * A task that constructs a {@link LogFileHeader} object from a log file.
 * 
 * @author savormix
 */
public class LogIdentifyTask extends AsyncTask<Path, Object, LogFileHeader[]> implements IOConstants
{
	private static final L2Logger _log = L2Logger.getLogger(LogIdentifyTask.class);
	
	private final Window _owner;
	private final IProtocolVersion _defaultLoginProtocol, _defaultGameProtocol;
	private LogIdentificationProgress _dialog;
	
	/**
	 * Constructs this task.
	 * 
	 * @param owner associated window
	 * @param defaultLoginProtocol assumed protocol version for [legacy] login packet logs
	 * @param defaultGameProtocol assumed protocol version for [legacy] game packet logs
	 */
	public LogIdentifyTask(Window owner, IProtocolVersion defaultLoginProtocol, IProtocolVersion defaultGameProtocol)
	{
		_owner = owner;
		_defaultLoginProtocol = defaultLoginProtocol;
		_defaultGameProtocol = defaultGameProtocol;
	}
	
	static interface HeadHook
	{
		void publish(String message, int version, boolean login, int protocol);
		
		void publish(String message);
		
		void publishException(Exception e) throws RuntimeException;
		
		boolean isCancelled();
	}
	
	static final class TaskHook implements HeadHook
	{
		private final LogIdentifyTask _task;
		private int _index;
		
		TaskHook(LogIdentifyTask task)
		{
			_task = task;
			_index = 0;
		}
		
		public void next()
		{
			++_index;
		}
		
		@SuppressWarnings("synthetic-access")
		@Override
		public void publish(String message, int version, boolean login, int protocol)
		{
			_task.publish(_index, message, version, login, protocol);
		}
		
		@SuppressWarnings("synthetic-access")
		@Override
		public void publish(String message)
		{
			_task.publish(_index, message, null, null, null);
		}
		
		@Override
		public void publishException(Exception e) throws RuntimeException
		{
			publish(e.getLocalizedMessage());
		}
		
		@Override
		public boolean isCancelled()
		{
			return _task.isCancelled();
		}
	}
	
	static final class EmptyHook implements HeadHook
	{
		@Override
		public void publish(String message, int version, boolean login, int protocol)
		{
			// nothing
		}
		
		@Override
		public void publish(String message)
		{
			// nothing
		}
		
		@Override
		public void publishException(Exception e) throws RuntimeException
		{
			throw new RuntimeException(e);
		}
		
		@Override
		public boolean isCancelled()
		{
			return false;
		}
	}
	
	/**
	 * Reads and returns a packet log file's header.
	 * 
	 * @param f path to a packet log file
	 * @return packet log header
	 */
	public static LogFileHeader getHeader(Path f)
	{
		return getHeader(new EmptyHook(), f);
	}
	
	private static LogFileHeader getHeader(HeadHook hook, Path f)
	{
		final long size;
		try
		{
			size = Files.size(f);
			if (size < 9)
				throw new IOException("Truncated");
		}
		catch (IOException e)
		{
			hook.publish("Invalid file.");
			// _log.error(f.getName() + " is definitely not a log file. Remove it.");
			return null;
		}
		
		if (hook.isCancelled())
			return null;
		
		try (final SeekableByteChannel channel = Files.newByteChannel(f, StandardOpenOption.READ); final NewIOHelper ioh = new NewIOHelper(channel))
		{
			final long mag = ioh.readLong();
			if (mag == LOG_MAGIC_TRUNCATED)
			{
				hook.publish("Incomplete packet log file.");
				return null;
			}
			else if (mag != LOG_MAGIC)
			{
				hook.publish("Invalid magic value.");
				return null;
			}
			
			final int ver = ioh.readByte() & 0xFF; // version
			
			final int hSz, fSz;
			final long fStart;
			
			if (ver >= 6)
			{
				hSz = ioh.readInt();
				fSz = ioh.readInt();
				fStart = ioh.readLong(); // kind of useless?
			}
			else
			{
				hSz = LOG_HEADER_SIZE_PRE_6;
				if (ver == 5)
					fSz = LOG_FOOTER_SIZE_5;
				else
					fSz = LOG_FOOTER_SIZE_PRE_5;
				fStart = size - fSz;
			}
			
			final long headerPos = 0;
			
			// default header fields
			boolean login = false;
			if (ver < 6)
				login = ioh.readBoolean();
			final long time = ioh.readLong();
			final int protocol;
			if (ver >= 6)
			{
				login = ioh.readBoolean();
				protocol = ioh.readInt();
			}
			else
				protocol = -1;
			
			hook.publish("Packet log", ver, login, protocol);
			
			if (hook.isCancelled())
				return null;
			
			final long unreadHeaderBytes = hSz - (ioh.getPositionInChannel(false) - headerPos);
			if (unreadHeaderBytes < 0)
			{
				hook.publish("Damaged or invalid packet log.", ver, login, protocol);
				//_log.error(f.getName() + " is not a valid log file. It may be damaged.");
				return null;
			}
			if (unreadHeaderBytes > 0)
			{
				StringBuilder sb = new StringBuilder();
				sb.append(unreadHeaderBytes);
				sb.append(" unknown header bytes in a version ");
				sb.append(ver);
				sb.append(" packet log.");
				if (ver > LOG_VERSION)
				{
					sb.append("Please consider updating this application, since only version <= ");
					sb.append(LOG_VERSION);
					sb.append(" packet logs are supported.");
				}
				_log.warn(sb);
				hook.publish("Unsupported", ver, login, protocol);
			}
			
			if (hook.isCancelled())
				return null;
			
			ioh.read(new byte[(int)unreadHeaderBytes]);
			
			if (hook.isCancelled())
				return null;
			
			int totalPackets = -1;
			
			Map<Integer, Integer> cp = Collections.emptyMap(), sp = Collections.emptyMap();
			
			footer:
			{
				if (ver < 5)
					break footer;
				
				ioh.setPositionInChannel(fStart);
				totalPackets = ioh.readInt();
				
				if (ver < 6)
					break footer;
				
				if (hook.isCancelled())
					return null;
				
				cp = new HashMap<>();
				sp = new HashMap<>();
				
				final int blocks = ioh.readByte();
				for (int i = 0; i < blocks; ++i)
				{
					final EndpointType endpoint = EndpointType.valueOf(ioh.readBoolean());
					final Map<Integer, Integer> tracker = endpoint.isClient() ? cp : sp;
					final int cnt = ioh.readInt();
					for (int j = 0; j < cnt; ++j)
					{
						final int packet = ioh.readInt();
						final int count = ioh.readInt();
						tracker.put(packet, count);
					}
					
					if (hook.isCancelled())
						return null;
				}
			}
			
			return new LogFileHeader(f, ver, hSz, fSz, fStart, time, login, protocol, totalPackets, cp, sp);
		}
		catch (IOException e)
		{
			hook.publishException(e);
			// _log.error("Could not load logged packets!", e);
			// return null;
		}
		catch (RuntimeException e)
		{
			hook.publishException(e);
			// _log.error("Could not load logged packets!", e);
			// return null;
		}
		
		return null;
	}
	
	@Override
	protected void onPreExecute()
	{
		_dialog = new LogIdentificationProgress(_owner, this);
		_dialog.setVisible(true);
	}
	
	@Override
	protected LogFileHeader[] doInBackground(Path... params)
	{
		publish(-1, params);
		
		final TaskHook hook = new TaskHook(this);
		final LogFileHeader[] headers = new LogFileHeader[params.length];
		for (int p = 0; p < params.length; p++)
		{
			if (isCancelled())
				break;
			
			final Path f = params[p];
			headers[p] = getHeader(hook, f);
			hook.next();
		}
		return headers;
	}
	
	@Override
	public void process(List<Object> params)
	{
		for (int i = 0; i < params.size(); i++)
		{
			int file = (Integer)params.get(i);
			if (file != -1)
			{
				final String status = (String)params.get(++i);
				final Integer version = (Integer)params.get(++i);
				final Boolean login = (Boolean)params.get(++i);
				final Integer protocol = (Integer)params.get(++i);
				_dialog.setFileStatus(file, status, version, login, protocol);
			}
			else
				_dialog.assignFiles((Path[])params.get(++i));
		}
	}
	
	@Override
	public void onPostExecute(LogFileHeader[] result)
	{
		_dialog.disableCancel();
		
		if (result == null)
			return;
		
		if (result.length == 1)
		{
			_dialog.setVisible(false);
			_dialog.dispose();
		}
		
		for (LogFileHeader lfh : result)
		{
			if (lfh == null)
				continue;
			
			new LogLoadHelper(_owner, lfh, lfh.isLogin() ? _defaultLoginProtocol : _defaultGameProtocol).setVisible(true);
		}
	}
}
