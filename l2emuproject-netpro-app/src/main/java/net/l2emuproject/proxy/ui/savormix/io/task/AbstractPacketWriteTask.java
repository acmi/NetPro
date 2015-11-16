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
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

import net.l2emuproject.proxy.ui.ReceivedPacket;
import net.l2emuproject.util.logging.L2Logger;

/**
 * @author _dev_
 */
public abstract class AbstractPacketWriteTask extends AbstractLogFileTask<ReceivedPacket>
{
	private static final L2Logger LOG = L2Logger.getLogger(AbstractPacketWriteTask.class);
	
	private final Path _file;
	
	protected AbstractPacketWriteTask(Window owner, String desc, Path file)
	{
		super(owner, desc);
		
		_file = file;
	}
	
	@Override
	protected Void doInBackground(ReceivedPacket... params)
	{
		_dialog.setMaximum("", params.length);
		try (final Writer writer = Files.newBufferedWriter(_file))
		{
			writeHeader(writer);
			for (final ReceivedPacket packet : params)
			{
				writePacket(writer, packet);
				publish(packet);
			}
			writeFooter(writer);
		}
		catch (IOException e)
		{
			LOG.error("", e);
		}
		return null;
	}
	
	protected abstract void writeHeader(Writer writer) throws IOException;
	
	protected abstract void writePacket(Writer writer, ReceivedPacket packet) throws IOException;
	
	protected abstract void writeFooter(Writer writer) throws IOException;
}
