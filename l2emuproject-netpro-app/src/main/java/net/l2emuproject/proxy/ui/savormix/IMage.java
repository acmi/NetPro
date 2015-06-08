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
package net.l2emuproject.proxy.ui.savormix;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import net.l2emuproject.util.logging.L2Logger;

/**
 * Marks a package where images are located.
 * 
 * @author savormix
 */
@SuppressWarnings("javadoc")
public interface IMage
{
	// marker interface
	int[] ICON_SIZES = { 16, 20, 24, 32, 40, 48, 64, 256 };
	
	default List<? extends Image> getIconList()
	{
		final List<BufferedImage> icons = new ArrayList<>(ICON_SIZES.length);
		final L2Logger log = L2Logger.getLogger(getClass());
		for (int sz : ICON_SIZES)
		{
			try
			{
				icons.add(ImageIO.read(IMage.class.getResource("icon-" + sz + ".png")));
			}
			catch (IOException e)
			{
				log.error("Could not read icon of size " + sz, e);
			}
		}
		return icons;
	}
}
