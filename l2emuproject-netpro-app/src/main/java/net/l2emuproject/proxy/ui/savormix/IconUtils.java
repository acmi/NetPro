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
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

/**
 * Draws various UI icons.<BR>
 * <BR>
 * FIXME: pick unix-compatible fonts!
 * 
 * @author savormix
 */
public class IconUtils
{
	private static final Map<Object, Object> HINTS;
	static
	{
		HINTS = new HashMap<>();
		HINTS.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		HINTS.put(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
	}
	
	private IconUtils()
	{
		// utility class
	}
	
	/**
	 * Draws an oval of the specified size and solid color. Writes given unstyled white 8pt text aligned to the center of the oval.
	 * 
	 * @param w oval width
	 * @param h oval height
	 * @param color oval color
	 * @param text oval text
	 * @return oval with text as image
	 */
	public static BufferedImage drawOval(int w, int h, Color color, String text)
	{
		return drawOval(w, h, color, text, Color.WHITE, 8, "PLAIN");
	}
	
	/**
	 * Draws an oval of the specified size and solid color. Writes given text aligned to the center of the oval.
	 * 
	 * @param w oval width
	 * @param h oval height
	 * @param color oval color
	 * @param text oval text
	 * @param textColor text color
	 * @param textSize text size
	 * @param style text style modifiers
	 * @return oval with text as image
	 */
	public static BufferedImage drawOval(int w, int h, Color color, String text, Color textColor, int textSize, String style)
	{
		return drawOval(w, h, HINTS, color, text, textColor, textSize, style);
	}
	
	/**
	 * Draws an oval of the specified size and solid color. Writes given text aligned to the center of the oval.
	 * 
	 * @param w oval width
	 * @param h oval height
	 * @param hints rendering hints
	 * @param color oval color
	 * @param text oval text
	 * @param textColor text color
	 * @param textSize text size
	 * @param style text style modifiers
	 * @return oval with text as image
	 */
	public static BufferedImage drawOval(int w, int h, Map<?, ?> hints, Color color, String text, Color textColor, int textSize, String style)
	{
		BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_4BYTE_ABGR);
		Graphics2D g = image.createGraphics();
		g.addRenderingHints(hints);
		g.setColor(color);
		final int top = 0, bottom = h;
		g.fillOval(0, top, w, bottom);
		g.setColor(color.darker());
		g.drawOval(0, top, w - 1, bottom - 1);
		g.setColor(textColor);
		g.setFont(Font.decode("Consolas-" + style + "-" + textSize));
		FontMetrics fm = g.getFontMetrics();
		final int fw = fm.stringWidth(text);
		final int baseline = top + ((bottom + 1 - top) / 2) - ((fm.getAscent() + fm.getDescent()) / 2) + fm.getAscent();
		g.drawString(text, (w - fw) / 2, baseline);
		g.dispose();
		return image;
	}
	
	/**
	 * Draws an icon to visually distinguish a data type.
	 * 
	 * @param w icon width
	 * @param h icon height
	 * @param color background color
	 * @param text textual data type identifier
	 * @param textSize text size (points)
	 * @return data type icon
	 */
	public static BufferedImage drawDataType(int w, int h, Color color, Character text, int textSize)
	{
		BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_4BYTE_ABGR);
		Graphics2D g = image.createGraphics();
		g.addRenderingHints(HINTS);
		g.setColor(color);
		final int top = 0, bottom = h;
		g.fillOval(0, top, w, bottom);
		g.setColor(color.darker());
		g.drawOval(0, top, w - 1, bottom - 1);
		g.setColor(Color.BLACK);
		g.setFont(Font.decode("Consolas-BOLD-" + textSize));
		FontMetrics fm = g.getFontMetrics();
		final int fw = fm.charWidth(text);
		final int baseline = top + ((bottom + 1 - top) / 2) - ((fm.getAscent() + fm.getDescent()) / 2) + fm.getAscent();
		g.drawChars(new char[] { text }, 0, 1, (w - fw) / 2, baseline);
		g.dispose();
		return image;
	}
	
	/**
	 * Draws a rounded rectangle with two lines of monospaced, white, 8pt text (center-aligned).
	 * 
	 * @param w rectangle width
	 * @param h rectangle height
	 * @param color rectangle color
	 * @param text text at the top
	 * @param subText text at the bottom
	 * @return rounded rectangle icon
	 */
	public static BufferedImage drawRoundedRect(int w, int h, Color color, String text, String subText)
	{
		return drawRoundedRect(w, h, HINTS, color, text, subText, Color.WHITE, 8);
	}
	
	/**
	 * Draws a rounded rectangle with two lines of monospaced text (center-aligned).
	 * 
	 * @param w rectangle width
	 * @param h rectangle height
	 * @param hints rendering hints
	 * @param color rectangle color
	 * @param text text at the top
	 * @param subText text at the bottom
	 * @param textColor text color
	 * @param textSize text size (points)
	 * @return rounded rectangle icon
	 */
	public static BufferedImage drawRoundedRect(int w, int h, Map<?, ?> hints, Color color, String text, String subText, Color textColor, int textSize)
	{
		BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_4BYTE_ABGR);
		Graphics2D g = image.createGraphics();
		g.addRenderingHints(hints);
		g.setColor(color);
		final int top = 0, bottom = h;
		g.fillRoundRect(0, top, w, bottom, 4, 4);
		g.setColor(textColor);
		g.setFont(Font.decode("Consolas-PLAIN-" + textSize));
		FontMetrics fm = g.getFontMetrics();
		int fw = fm.stringWidth(text);
		int baseline = top + ((bottom + 1 - top) / 2);
		baseline -= ((fm.getAscent() + fm.getDescent()) / (subText != null ? 1 : 2));
		baseline += fm.getAscent();
		g.drawString(text, (w - fw) / 2, baseline);
		if (subText != null)
		{
			fw = fm.stringWidth(subText);
			g.drawString(subText, (w - fw) / 2, baseline + fm.getAscent()/* + fm.getDescent()*/);
		}
		g.dispose();
		return image;
	}
}
