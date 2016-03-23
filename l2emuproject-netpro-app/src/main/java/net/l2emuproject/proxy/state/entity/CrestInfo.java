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

import javax.imageio.ImageIO;

import net.l2emuproject.proxy.ui.dds.DDSReader;
import net.l2emuproject.proxy.ui.javafx.FXUtils;
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
	String _crestImgSrc;
	
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
	public String getCrestImgSrc()
	{
		return _crestImgSrc;
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
			_crestImgSrc = null;
			return;
		}
		
		BufferedImage image;
		try
		{
			image = DDSReader.getCrestTexture(crest);
		}
		catch (IllegalArgumentException e)
		{
			// legacy crests (BMP)
			try
			{
				image = ImageIO.read(new ByteArrayInputStream(crest));
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
		
		try
		{
			_crestImgSrc = FXUtils.getImageSrcForWebEngine(image);
		}
		catch (IOException e)
		{
			_crestImgSrc = null;
			LOG.error("Unexpected error", e);
		}
	}
}
