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
import java.awt.Color;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JComponent;
import javax.swing.plaf.LayerUI;

/**
 * An UI that will darken an underlying component.
 * 
 * @author savormix
 */
public class DisabledComponentUI extends LayerUI<Container>
{
	private static final long serialVersionUID = -6056055953222961303L;
	
	//private final BufferedImageOp _op;
	
	private boolean _active;
	
	//private BufferedImage _offscreen;
	
	/** Creates an overlay painter. */
	public DisabledComponentUI()
	{
		// final Kernel k = new Kernel(3, 3, new float[] { 0.0f, -1.0f, 0.0f, -1.0f, 5.0f, -1.0f, 0.0f, -1.0f, 0.0f });
		// _op = new ConvolveOp(k, ConvolveOp.EDGE_NO_OP, null);
		//_op = new GaussianFilter(1.5f);
		
		//_offscreen = new BufferedImage(1, 1, BufferedImage.TYPE_BYTE_BINARY);
	}
	
	@Override
	public void paint(Graphics g, JComponent c)
	{
		if (!_active)
		{
			super.paint(g, c);
			return;
		}
		
		final int w = c.getWidth(), h = c.getHeight();
		if (w == 0 || h == 0)
			return;
		
		/*if (_offscreen.getWidth() != w || _offscreen.getHeight() != h)
		{
			//if (g instanceof Graphics2D)
			//_offscreen = ((Graphics2D)g).getDeviceConfiguration().createCompatibleImage(w, h, Transparency.TRANSLUCENT);
			//else
			//	_offscreen = new BufferedImage(w,h, BufferedImage.TYPE_INT_ARGB);
		}
		
		{
			final Graphics2D g2 = _offscreen.createGraphics();
			try
			{
				g2.setClip(g.getClip());
				super.paint(g2, c);
			}
			finally
			{
				g2.dispose();
			}
		}
		*/
		{
			super.paint(g, c);
			final Graphics2D g2 = (Graphics2D)g.create();
			try
			{
				//g2.drawImage(_offscreen, _op, 0, 0);
				// and darken
				g2.setColor(Color.GRAY);
				g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .55f));
				g2.fillRect(0, 0, w, h);
			}
			finally
			{
				g2.dispose();
			}
		}
	}
	
	/**
	 * Enables or disables the overlay painting.
	 * 
	 * @param active whether to paint the overlay
	 */
	public void setActive(boolean active)
	{
		_active = active;
	}
}
