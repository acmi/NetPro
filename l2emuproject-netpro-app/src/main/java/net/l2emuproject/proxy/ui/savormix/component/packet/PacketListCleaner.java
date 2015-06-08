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
package net.l2emuproject.proxy.ui.savormix.component.packet;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;

import javax.swing.Timer;

import net.l2emuproject.proxy.ui.savormix.component.ConnectionPane;
import net.l2emuproject.util.concurrent.RunnableStatsManager;
import net.l2emuproject.util.logging.L2Logger;

/**
 * Automatically purges packet lists when low on memory.
 * 
 * @author savormix
 */
public class PacketListCleaner extends Timer
{
	private static final long serialVersionUID = -8677420931780127546L;
	
	static final L2Logger LOG = L2Logger.getLogger(PacketListCleaner.class);
	
	private static final int MEMORY_THRESHOLD = 10 * 1024 * 1024;
	private static final int CHECK_INTERVAL = 500;
	
	/**
	 * Creates this timer.
	 * 
	 * @param listContainer associated tab container
	 */
	public PacketListCleaner(ConnectionPane listContainer)
	{
		super(CHECK_INTERVAL, new ThresholdTester(listContainer));
		
		setCoalesce(true);
		setRepeats(true);
		setInitialDelay(5_000);
	}
	
	private static final class ThresholdTester implements ActionListener
	{
		private final ConnectionPane _listContainer;
		private Reference<?> _reserved;
		
		ThresholdTester(ConnectionPane listContainer)
		{
			_listContainer = listContainer;
			
			reserveMemory();
		}
		
		private void reserveMemory()
		{
			_reserved = new SoftReference<>(new byte[MEMORY_THRESHOLD]);
		}
		
		@Override
		public void actionPerformed(ActionEvent timerEvent)
		{
			if (_reserved.get() != null)
				return;
			
			final long start = System.nanoTime();
			
			_listContainer.onLowMemory(ConnectionPane.FLAG_LM_PURGE_LISTS);
			
			System.runFinalization();
			System.gc();
			
			try
			{
				reserveMemory();
				
				LOG.info("Memory cleanup has been performed.");
			}
			catch (OutOfMemoryError e)
			{
				_listContainer.onLowMemory(ConnectionPane.FLAG_LM_PURGE_LISTS | ConnectionPane.FLAG_LM_DROP_LISTS_INACTIVE | ConnectionPane.FLAG_LM_DROP_LISTS_LOGFILE);
				
				LOG.info("Out of memory failure imminent!");
			}
			
			final long end = System.nanoTime();
			RunnableStatsManager.handleStats(getClass(), "timerEvent()", end - start);
		}
	}
}
