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
package net.l2emuproject.proxy.ui.savormix.component;

import java.awt.AlphaComposite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.JComponent;

import net.l2emuproject.proxy.ui.savormix.EventSink;

/**
 * Shows a watermark over a window.
 * 
 * @author savormix
 */
public class WatermarkPane extends JComponent implements MouseListener, KeyListener, EventSink
{
	private static final long serialVersionUID = 3088583090844824575L;
	
	private final BufferedImage _tile;
	
	/** Constructs a completely transparent pane. */
	public WatermarkPane()
	{
		this((BufferedImage)null);
	}
	
	/**
	 * Constructs a pane overlayed with semitransparent tiles of the given image.
	 * 
	 * @param tile tile image
	 * @throws IOException if the given image cannot be read
	 */
	public WatermarkPane(URL tile) throws IOException
	{
		this(ImageIO.read(tile));
	}
	
	/**
	 * Constructs a pane overlayed with semitransparent tiles of the given image.
	 * 
	 * @param tile tile image
	 */
	public WatermarkPane(BufferedImage tile)
	{
		_tile = tile;
	}
	
	@Override
	public void paintComponent(Graphics g)
	{
		super.paintComponent(g);
		
		if (_tile == null)
			return;
		
		final Graphics2D g2 = (Graphics2D)g.create();
		try
		{
			
			final int w = getWidth(), h = getHeight();
			final int width = _tile.getWidth();
			final int height = _tile.getHeight();
			final int horizontal = w / width + (w % width > 0 ? 1 : 0);
			final int vertical = h / height + (h % height > 0 ? 1 : 0);
			g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.06f));
			for (int i = 0; i < horizontal; ++i)
			{
				final int totalWidth = width * horizontal;
				final int startX = ((w - totalWidth) >> 1) + width * i;
				for (int j = 0; j < vertical; ++j)
				{
					final int totalHeight = height * vertical;
					final int startY = ((h - totalHeight) >> 1) + height * j;
					g2.drawImage(_tile, null, startX, startY);
				}
			}
		}
		finally
		{
			g2.dispose();
		}
	}
	
	@Override
	public void keyTyped(KeyEvent e)
	{
		e.consume();
	}
	
	@Override
	public void keyPressed(KeyEvent e)
	{
		e.consume();
	}
	
	@Override
	public void keyReleased(KeyEvent e)
	{
		e.consume();
	}
	
	@Override
	public void mouseClicked(MouseEvent e)
	{
		e.consume();
	}
	
	@Override
	public void mousePressed(MouseEvent e)
	{
		e.consume();
	}
	
	@Override
	public void mouseReleased(MouseEvent e)
	{
		e.consume();
	}
	
	@Override
	public void mouseEntered(MouseEvent e)
	{
		// nothing
	}
	
	@Override
	public void mouseExited(MouseEvent e)
	{
		// nothing
	}
	
	@Override
	public void startIgnoringEvents()
	{
		addMouseListener(this);
		addKeyListener(this);
	}
	
	@Override
	public void stopIgnoringEvents()
	{
		removeMouseListener(this);
		removeKeyListener(this);
	}
}
