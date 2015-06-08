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

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Locale;
import java.util.Queue;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTextArea;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.commons.lang3.ArrayUtils;

import net.l2emuproject.proxy.ui.savormix.loader.Loader;

/**
 * A panel that contains a mini GUI console.
 * 
 * @author savormix
 */
public class ConsolePanel extends JPanel implements ActionListener, ChangeListener, SwingConstants
{
	private static final long serialVersionUID = 7914203388304975752L;
	
	private final Queue<String> _messages;
	
	private final Timer _timer;
	
	private final JTextArea _console;
	
	private final JButton _clear;
	private final JToggleButton _wrap;
	private final JToggleButton _lock;
	private final JSlider _size;
	
	/** Creates this component. */
	public ConsolePanel()
	{
		super(new BorderLayout());
		
		_messages = Loader.getLogMessages();
		
		_timer = new Timer(350, this);
		
		{
			final JPanel ctrl = new JPanel(new GridLayout(1, 0));
			{
				_clear = new JButton("Clear");
				_clear.addActionListener(this);
				ctrl.add(_clear);
				
				_wrap = new JToggleButton("Wrap lines", true);
				_wrap.addActionListener(this);
				ctrl.add(_wrap);
				
				_lock = new JToggleButton("Scroll lock", false);
				ctrl.add(_lock);
				
				_size = new JSlider(HORIZONTAL, 5, 28, 12);
				_size.addChangeListener(this);
				ctrl.add(_size);
			}
			
			add(ctrl, BorderLayout.SOUTH);
		}
		
		{
			_console = new JTextArea();
			_console.setEditable(false);
			
			final String[] fonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames(Locale.ENGLISH);
			
			final String font = ArrayUtils.contains(fonts, "Consolas") ? "Consolas" : Font.MONOSPACED;
			final int size = _console.getFont().getSize();
			_console.setFont(new Font(font, Font.PLAIN, size));
			{
				_size.setValue(size);
			}
			
			_console.setLineWrap(_wrap.isSelected());
			_console.setWrapStyleWord(true);
			
			add(new JScrollPane(_console), BorderLayout.CENTER);
		}
	}
	
	@Override
	public void actionPerformed(ActionEvent e)
	{
		if (e.getSource() == _timer)
			processLogMessages();
		else if (e.getSource() == _clear)
			_console.setText(null);
		else if (e.getSource() == _wrap)
			_console.setLineWrap(_wrap.isSelected());
	}
	
	@Override
	public void stateChanged(ChangeEvent e)
	{
		_console.setFont(_console.getFont().deriveFont((float)_size.getValue()));
	}
	
	@Override
	public void setVisible(boolean visible)
	{
		super.setVisible(visible);
		
		// _lock.requestFocusInWindow();
		
		_timer.start(); // additional starts are ignored
	}
	
	private void processLogMessages()
	{
		for (String msg; (msg = _messages.poll()) != null;)
		{
			_console.append(msg);
			_console.append("\r\n");
		}
		
		if (!_lock.isSelected())
			_console.setCaretPosition(_console.getText().length());
	}
}
