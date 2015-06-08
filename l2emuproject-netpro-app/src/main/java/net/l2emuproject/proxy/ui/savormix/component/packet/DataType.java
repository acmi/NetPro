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

import java.awt.Color;
import java.net.URL;
import java.util.Locale;

import net.l2emuproject.proxy.ui.savormix.IconUtils;
import net.l2emuproject.proxy.ui.savormix.SafeColor;
import net.l2emuproject.proxy.ui.savormix.io.ImageUrlUtils;

/**
 * Represents data types used in packet definitions.<BR>
 * <BR>
 * Pending rework. No JavaDoc.
 * 
 * @author savormix
 */
@SuppressWarnings("javadoc")
public enum DataType
{
	// @formatter:off
	BYTES('B', SafeColor.TURQUOISE.getColor()),
	BYTE('C', SafeColor.RED.getColor()),
	WORD('H', SafeColor.ORANGE.getColor()),
	DWORD('D', SafeColor.BLUE.getColor()),
	QWORD('Q', SafeColor.PURPLE.getColor()),
	FLOAT('F', SafeColor.GREY.getColor().darker()),
	STRING('S', SafeColor.LIME_GREEN.getColor());
	// @formatter:on
	
	//private final char _id;
	private final Color _color;
	private final URL _icon;
	
	private DataType(char id, Color color)
	{
		//_id = id;
		_color = color;
		_icon = ImageUrlUtils.getInstance().toUrl("data_type", IconUtils.drawDataType(14, 14, color, Character.toLowerCase(id), 14));
	}
	
	/*
	public char getId()
	{
		return _id;
	}
	*/
	public Color getColor()
	{
		return _color;
	}
	
	public URL getIcon()
	{
		return _icon;
	}
	
	@Override
	public String toString()
	{
		return super.toString().toLowerCase(Locale.ENGLISH);
	}
}
