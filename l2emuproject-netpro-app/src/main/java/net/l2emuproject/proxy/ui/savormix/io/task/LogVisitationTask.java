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
import java.lang.reflect.InvocationTargetException;
import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.Set;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import net.l2emuproject.proxy.config.ProxyConfig;
import net.l2emuproject.proxy.io.LogFileHeader;
import net.l2emuproject.proxy.network.EndpointType;
import net.l2emuproject.proxy.ui.ReceivedPacket;
import net.l2emuproject.proxy.ui.savormix.io.LoggedPacketFlag;
import net.l2emuproject.proxy.ui.savormix.io.base.NewIOHelper;
import net.l2emuproject.util.BitMaskUtils;
import net.l2emuproject.util.logging.L2Logger;

/**
 * Performs packet visitation for the given historical log files, displaying progress in a shared dialog window.
 * 
 * @author _dev_
 */
public class LogVisitationTask extends AbstractLogFileTask<Path>
{
	private static final L2Logger LOG = L2Logger.getLogger(LogVisitationTask.class);
	
	private final HistoricalLogPacketVisitor _visitor;
	
	/**
	 * Constructs this task.
	 * 
	 * @param owner associated window
	 * @param desc task description
	 * @param visitor packet visitor
	 */
	public LogVisitationTask(Window owner, String desc, HistoricalLogPacketVisitor visitor)
	{
		super(owner, desc);
		
		_visitor = visitor;
	}
	
	@Override
	protected Void doInBackground(Path... params)
	{
		for (final Path p : params)
		{
			final String name = p.getFileName().toString();
			
			final LogFileHeader header = /*LogIdentifyTask.getHeader(p)*/null;
			if (header == null)
				continue;
			
			final int displayedPacketAmount = header.getPackets() != -1 ? header.getPackets() : Integer.MAX_VALUE;
			
			try (final SeekableByteChannel channel = Files.newByteChannel(p, StandardOpenOption.READ); final NewIOHelper ioh = new NewIOHelper(channel))
			{
				_visitor.onStart(header);
				final long size = Files.size(p);
				
				ioh.skip(header.getHeaderSize(), false);
				
				if (isCancelled())
					break;
				
				try
				{
					SwingUtilities.invokeAndWait(new Runnable()
					{
						@Override
						public void run()
						{
							// remove any leftover publishes
							_dialog.setMaximum(name, displayedPacketAmount);
						}
					});
				}
				catch (InterruptedException e)
				{
					break;
				}
				catch (InvocationTargetException e)
				{
					continue;
				}
				
				if (isCancelled())
					break;
				
				for (int count = displayedPacketAmount; count > 0 && size - ioh.getPositionInChannel(false) > header.getFooterSize(); count--)
				{
					final EndpointType type = EndpointType.valueOf(ioh.readBoolean());
					final byte[] body = new byte[ioh.readChar()];
					ioh.read(body);
					final long time = ioh.readLong();
					final Set<LoggedPacketFlag> flags = header.getVersion() >= 7 ? BitMaskUtils.setOf(ioh.readByte(), LoggedPacketFlag.class) : Collections.emptySet();
					
					final ReceivedPacket rp = new ReceivedPacket(header.getService(), type, body, time);
					_visitor.onPacket(rp, flags);
					publish(rp);
					
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
							LOG.info("Cancelled visitation of " + name);
							break;
						}
					}
				}
			}
			catch (ClosedByInterruptException e)
			{
				LOG.info("Cancelled visitation of " + name);
				continue;
			}
			catch (UnsupportedOperationException e)
			{
				LOG.info(getClass(), "Operation cannot be applied to file " + name);
				continue;
			}
			//catch (IOException | RuntimeException e)
			catch (Exception e)
			{
				LOG.error("Failed visiting " + name, e);
				continue;
			}
			finally
			{
				try
				{
					_visitor.onEnd();
				}
				catch (Exception e)
				{
					LOG.error("Failed completing visitation of " + name, e);
				}
			}
		}
		return null;
	}
	
	@Override
	protected void onPostExecute(Void result)
	{
		JOptionPane.showMessageDialog(_dialog, "All selected files have been processed.", "Processing complete", JOptionPane.INFORMATION_MESSAGE);
		
		super.onPostExecute(result);
	}
}
