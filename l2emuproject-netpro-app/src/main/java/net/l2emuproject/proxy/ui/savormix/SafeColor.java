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

import java.awt.Color;

/**
 * Sample selection of 10 distinguishable colors (even for color-blind users).
 * 
 * @author savormix
 */
@SuppressWarnings("javadoc")
public enum SafeColor
{
	// @formatter:off
	LIME_GREEN(0x66, 0xFF, 0x33),
	ORANGE(0xFF, 0x99, 0x33),
	RED(0xFF, 0x33, 0x33),
	MOSS_GREEN(0x33, 0x66, 0x00),
	DUSKY_PINK(0xCC, 0x99, 0x99),
	GREY(0xCC, 0xCC, 0xCC),
	BLACK(0x00, 0x00, 0x00),
	BLUE(0x00, 0xCC, 0xFF),
	PURPLE(0x99, 0x00, 0xCC),
	TURQUOISE(0x00, 0x99, 0x99);
	// @formatter:on
	
	private final Color _color;
	
	private SafeColor(int r, int g, int b)
	{
		_color = new Color(r, g, b);
	}
	
	/**
	 * Returns the associated {@link Color} object.
	 * 
	 * @return the color
	 */
	public Color getColor()
	{
		return _color;
	}
}
