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
import java.io.IOException;
import java.util.Locale;

import net.l2emuproject.proxy.ui.javafx.FXUtils;
import net.l2emuproject.proxy.ui.savormix.IconUtils;
import net.l2emuproject.proxy.ui.savormix.SafeColor;
import net.l2emuproject.util.logging.L2Logger;

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
	
	private final char _id;
	private final Color _color;
	private final String _iconImgSrc;
	private final String _iconTooltip;
	
	private DataType(char id, Color color)
	{
		_id = id;
		_color = color;
		String iconImgSrc = FXUtils.class.getResource("icon-16.png").toString();
		try
		{
			iconImgSrc = FXUtils.getImageSrcForWebEngine(IconUtils.drawDataType(14, 14, color, Character.toLowerCase(id), 14));
		}
		catch (IOException e)
		{
			L2Logger.getLogger(DataType.class).error("Unexpected error", e);
		}
		finally
		{
			_iconImgSrc = iconImgSrc;
		}
		_iconTooltip = "packethtml.datatype.tooltip." + this;
	}
	
	public char getId()
	{
		return _id;
	}
	
	public Color getColor()
	{
		return _color;
	}
	
	public String getIconImgSrc()
	{
		return _iconImgSrc;
	}
	
	public String getIconTooltip()
	{
		return _iconTooltip;
	}
	
	@Override
	public String toString()
	{
		return super.toString().toLowerCase(Locale.ENGLISH);
	}
}
