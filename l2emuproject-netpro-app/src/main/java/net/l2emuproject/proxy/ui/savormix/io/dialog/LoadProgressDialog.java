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
package net.l2emuproject.proxy.ui.savormix.io.dialog;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.text.NumberFormat;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;

import net.l2emuproject.proxy.ui.savormix.io.task.AbstractLogLoadTask;
import net.l2emuproject.proxy.ui.savormix.loader.Loader;

/**
 * A dialog that tracks how many packets have been loaded.
 * 
 * @author savormix
 */
public class LoadProgressDialog extends JDialog implements WindowListener, KeyListener
{
	private static final long serialVersionUID = 7356528346907866660L;
	
	private final AbstractLogLoadTask<?> _task;
	
	private final JProgressBar _bar;
	private final JLabel _file;
	private final JLabel _percent;
	private final JLabel _current;
	private final JLabel _maximum;
	
	/**
	 * Constructs this dialog.
	 * 
	 * @param owner parent window
	 * @param title dialog title
	 * @param task associated I/O task
	 */
	public LoadProgressDialog(Window owner, String title, AbstractLogLoadTask<?> task)
	{
		super(owner, title, ModalityType.MODELESS);
		
		_task = task;
		
		setResizable(false);
		setLocationByPlatform(true);
		setAlwaysOnTop(true);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		addWindowListener(this);
		addKeyListener(this);
		
		JPanel root = new JPanel(new BorderLayout());
		{
			{
				final JPanel n = new JPanel();
				{
					final Dimension size = new Dimension(260, 40);
					_bar = new JProgressBar(SwingConstants.HORIZONTAL);
					_bar.setPreferredSize(size);
					
					n.add(_bar);
				}
				root.add(n, BorderLayout.NORTH);
			}
			{
				final JPanel south = new JPanel(new BorderLayout());
				{
					NumberFormat nf = NumberFormat.getPercentInstance(Loader.getLocale());
					JPanel left = new JPanel();
					left.add(_percent = new JLabel(nf.format(0)));
					south.add(left, BorderLayout.WEST);
				}
				{
					NumberFormat nf = NumberFormat.getIntegerInstance(Loader.getLocale());
					JPanel right = new JPanel();
					right.add(_current = new JLabel(nf.format(0)));
					right.add(new JLabel("/"));
					right.add(_maximum = new JLabel(nf.format(0)));
					south.add(right, BorderLayout.EAST);
				}
				south.add(_file = new JLabel("PACKET LOG FILE", SwingConstants.CENTER), BorderLayout.SOUTH);
				root.add(south, BorderLayout.SOUTH);
			}
		}
		getContentPane().add(root);
		pack();
	}
	
	/**
	 * Specifies the maximum amount of packets to be loaded.
	 * 
	 * @param file filename
	 * @param max total amount of packets
	 */
	public void setMaximum(String file, int max)
	{
		_file.setText(file);
		_maximum.setText(NumberFormat.getIntegerInstance(Loader.getLocale()).format(max));
		_bar.setMaximum(max);
		setProgress(0);
		
		if (max == Integer.MAX_VALUE)
			_bar.setIndeterminate(true);
	}
	
	/**
	 * Sets the amount of packets that have already been loaded.
	 * 
	 * @param val amount of packets loaded
	 */
	public void setProgress(int val)
	{
		_bar.setValue(val);
		_current.setText(NumberFormat.getIntegerInstance(Loader.getLocale()).format(val));
		_percent.setText(NumberFormat.getPercentInstance(Loader.getLocale()).format((double)val / _bar.getMaximum()));
	}
	
	/**
	 * Acknowledges that a given of number of packets have been loaded since last update.
	 * 
	 * @param val amount of packets newly loaded since last time
	 */
	public void addProgress(int val)
	{
		setProgress(_bar.getValue() + val);
	}
	
	@Override
	public void keyTyped(KeyEvent e)
	{
		// ignored
	}
	
	@Override
	public void keyPressed(KeyEvent e)
	{
		windowClosing(null);
		
		setVisible(false);
		dispose();
	}
	
	@Override
	public void keyReleased(KeyEvent e)
	{
		// ignored
	}
	
	@Override
	public void windowOpened(WindowEvent e)
	{
		// ignored
	}
	
	@Override
	public void windowClosing(WindowEvent e)
	{
		_task.cancel(true);
	}
	
	@Override
	public void windowClosed(WindowEvent e)
	{
		// ignored
	}
	
	@Override
	public void windowIconified(WindowEvent e)
	{
		// ignored
	}
	
	@Override
	public void windowDeiconified(WindowEvent e)
	{
		// ignored
	}
	
	@Override
	public void windowActivated(WindowEvent e)
	{
		// ignored
	}
	
	@Override
	public void windowDeactivated(WindowEvent e)
	{
		// ignored
	}
}
