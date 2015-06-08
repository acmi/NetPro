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
package net.l2emuproject.proxy.state.entity;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.zip.Adler32;
import java.util.zip.Checksum;

import javax.imageio.ImageIO;

import net.l2emuproject.proxy.ui.dds.DDSReader;
import net.l2emuproject.proxy.ui.savormix.io.ImageUrlUtils;
import net.l2emuproject.util.concurrent.L2ThreadPool;
import net.l2emuproject.util.logging.L2Logger;

/**
 * Stores a crest.
 * 
 * @author savormix
 */
public abstract class CrestInfo extends EntityInfo
{
	static final L2Logger LOG = L2Logger.getLogger(CrestInfo.class);
	
	final String _type;
	URL _crest;
	long _crestCrc;
	private volatile Future<?/*Void*/> _ioTask;
	
	/**
	 * Creates a crest entity.
	 * 
	 * @param id crest ID
	 * @param type filename prefix
	 */
	public CrestInfo(int id, String type)
	{
		super(id);
		
		_type = type;
	}
	
	/**
	 * Returns the real crest width (as opposed to the surface width) in pixels.
	 * 
	 * @return crest width (px)
	 */
	protected abstract int getActualWidth();
	
	/**
	 * Returns the real crest height (as opposed to the surface height) in pixels.
	 * 
	 * @return crest height (px)
	 */
	protected abstract int getActualHeight();
	
	/**
	 * Returns an URL to the crest file.
	 * 
	 * @return crest URL
	 */
	public URL getCrest()
	{
		return _crest;
	}
	
	/**
	 * Sets the crest image.
	 * 
	 * @param crest crest image in DDS format
	 */
	public void setCrest(byte[] crest)
	{
		if (crest.length == 0)
		{
			_crest = null;
			return;
		}
		
		if (_ioTask != null)
			_ioTask.cancel(true);
		_ioTask = L2ThreadPool.submitLongRunning(new AsyncImageSaver(crest));
	}
	
	@Override
	public void release()
	{
		if (_ioTask != null)
		{
			_ioTask.cancel(true);
			try
			{
				if (!_ioTask.isDone())
					_ioTask.get();
			}
			catch (InterruptedException | ExecutionException | CancellationException e)
			{
				// ignore and continue
			}
		}
		if (_crest != null)
			ImageUrlUtils.getInstance().release(Collections.singleton(_crest));
	}
	
	private final class AsyncImageSaver implements Runnable
	{
		private final byte[] _ddsImage;
		
		AsyncImageSaver(byte[] ddsImage)
		{
			_ddsImage = ddsImage;
		}
		
		@Override
		public void run()
		{
			final long crc;
			{
				final Checksum cs = new Adler32();
				cs.update(_ddsImage, 0, _ddsImage.length);
				crc = cs.getValue();
			}
			
			if (crc == _crestCrc)
				return;
			
			release();
			
			BufferedImage image;
			try
			{
				image = DDSReader.getCrestTexture(_ddsImage);
			}
			catch (IllegalArgumentException e)
			{
				// legacy crests (BMP)
				try
				{
					image = ImageIO.read(new ByteArrayInputStream(_ddsImage));
				}
				catch (IOException e2)
				{
					LOG.error("Legacy crest image", e2);
					return;
				}
			}
			catch (UnsupportedOperationException e)
			{
				LOG.info("Unsupported DDS image (crest) received");
				return;
			}
			
			_crest = ImageUrlUtils.getInstance().toUrl(_type, image.getSubimage(image.getWidth() - getActualWidth(), image.getHeight() - getActualHeight(), getActualWidth(), getActualHeight()));
			_crestCrc = crc;
		}
	}
}
