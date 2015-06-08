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
package interpreter.pledge;

import interpreter.DirectDrawSurface;

import java.awt.image.BufferedImage;

import eu.revengineer.simplejse.HasScriptDependencies;

import net.l2emuproject.proxy.state.entity.context.ICacheServerID;

/**
 * Properly adjusts the DDS texture height for a specific type of crest.
 * 
 * @author _dev_
 */
@HasScriptDependencies("interpreter.DirectDrawSurface")
public class AbstractCrestDDS extends DirectDrawSurface
{
	private final int _width, _height;
	
	/**
	 * Constructs this interpreter.
	 * 
	 * @param width actual crest width
	 * @param height actual crest height
	 */
	protected AbstractCrestDDS(int width, int height)
	{
		_width = width;
		_height = height;
	}
	
	@Override
	public Object getInterpretation(byte[] value, ICacheServerID cacheContext)
	{
		final Object image = super.getInterpretation(value, cacheContext);
		if (image instanceof BufferedImage)
		{
			final BufferedImage x16 = (BufferedImage)image;
			return x16.getSubimage(x16.getWidth() - _width, x16.getHeight() - _height, _width, _height);
		}
		return image;
	}
}
