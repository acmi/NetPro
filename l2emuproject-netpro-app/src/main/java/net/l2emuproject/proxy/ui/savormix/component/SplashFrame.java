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
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsDevice.WindowTranslucency;
import java.awt.GraphicsEnvironment;
import java.awt.Transparency;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import net.l2emuproject.proxy.ui.javafx.FXUtils;

/**
 * A splash dialog with reflection effect. Just a few tutorials clashed together.
 * 
 * @author savormix
 */
public class SplashFrame extends JFrame
{
	private static final long serialVersionUID = 8598763124585822805L;
	
	/** Constructs an undecorated splash screen with direct reflection effect. */
	public SplashFrame()
	{
		super("Splash");
		
		setResizable(false);
		setAlwaysOnTop(true);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setUndecorated(true);
		
		//setIconImages(getIconList());
		
		try
		{
			final BufferedImage avatar = ImageIO.read(FXUtils.class.getResource("icon-256.png"));
			final int width = avatar.getWidth();
			final int height = avatar.getHeight();
			setSize(width, height);
			setLocationRelativeTo(null);
			
			GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
			GraphicsDevice gd = ge.getDefaultScreenDevice();
			if (!gd.isWindowTranslucencySupported(WindowTranslucency.PERPIXEL_TRANSLUCENT))
			{
				final JPanel root = new JPanel();
				{
					final JLabel img = new JLabel(new ImageIcon(avatar));
					root.add(img);
				}
				getContentPane().add(root);
				return;
			}
			
			setSize(width, height * 2);
			
			final JPanel root = new JPanel()
			{
				private static final long serialVersionUID = 8367379366396197025L;
				
				@Override
				protected void paintComponent(Graphics g)
				{
					Graphics2D g2d = (Graphics2D)g.create();
					
					// code from
					// http://weblogs.java.net/blog/campbell/archive/2006/07/java_2d_tricker.html
					GraphicsConfiguration gc = g2d.getDeviceConfiguration();
					BufferedImage img = gc.createCompatibleImage(width, height, Transparency.TRANSLUCENT);
					Graphics2D g2 = img.createGraphics();
					
					g2.setComposite(AlphaComposite.Clear);
					g2.fillRect(0, 0, width, height);
					
					/*
					g2.setComposite(AlphaComposite.Src);
					g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
					g2.setColor(Color.WHITE);
					g2.fillRoundRect(0, 0, width, height + 10, 10, 10);
					*/
					
					g2.setComposite(AlphaComposite.Src);
					g2.drawImage(avatar, 0, 0, null);
					g2.dispose();
					
					// at this point the 'img' contains a soft
					// clipped round rectangle with the avatar
					
					// do the reflection with the code from
					// http://www.jroller.com/gfx/entry/swing_glint
					final int avatarWidth = img.getWidth();
					final int avatarHeight = img.getHeight();
					
					BufferedImage reflection = gc.createCompatibleImage(avatarWidth, avatarHeight, Transparency.TRANSLUCENT);
					Graphics2D reflectionGraphics = reflection.createGraphics();
					
					AffineTransform tranform = AffineTransform.getScaleInstance(1.0, -1.0);
					tranform.translate(0, -avatarHeight);
					reflectionGraphics.drawImage(img, tranform, this);
					
					GradientPaint painter = new GradientPaint(0.0f, 0.0f, new Color(0.0f, 0.0f, 0.0f, 0.5f), 0.0f, avatarHeight / 1.55f, new Color(0.0f, 0.0f, 0.0f, 1.0f));
					
					reflectionGraphics.setComposite(AlphaComposite.DstOut);
					reflectionGraphics.setPaint(painter);
					reflectionGraphics.fill(new Rectangle2D.Double(0, 0, avatarWidth, avatarHeight));
					
					reflectionGraphics.dispose();
					
					g2d.drawImage(img, 0, 0, this);
					g2d.drawImage(reflection, 0, avatarHeight, this);
					
					g2d.dispose();
				}
			};
			root.setDoubleBuffered(false);
			root.setOpaque(false);
			getContentPane().add(root);
		}
		catch (Exception e)
		{
			// oh no
		}
		
		Color/* bkg = getBackground();
				if (bkg == null) */
		bkg = Color.BLACK;
		final Color nBkg = new Color(bkg.getRed(), bkg.getGreen(), bkg.getBlue(), 0);
		setBackground(nBkg); // install per-pixel translucency
	}
}
