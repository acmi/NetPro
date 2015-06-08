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
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.NumberFormat;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;

import net.l2emuproject.proxy.ui.savormix.loader.Loader;

/**
 * A dialog that displays the results of a garbage collection operation.
 * 
 * @author savormix
 */
public class GcInfoDialog extends JDialog implements ActionListener, SwingConstants
{
	private static final long serialVersionUID = -6211177651648823002L;
	
	private long _beforeFree;
	private long _beforeTotal;
	private long _afterFree;
	private long _afterTotal;
	private MemorySizeUnit _mode;
	
	private final JProgressBar _barBefore;
	private final JLabel _percBefore;
	private final JLabel _freeBefore;
	private final JLabel _totalBefore;
	private final JProgressBar _barAfter;
	private final JLabel _percAfter;
	private final JLabel _freeAfter;
	private final JLabel _totalAfter;
	
	/**
	 * Constructs this dialog.
	 * 
	 * @param owner owner window
	 */
	public GcInfoDialog(Window owner)
	{
		super(owner, "Memory usage", ModalityType.DOCUMENT_MODAL);
		
		setDisplayMode(null);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setLocationByPlatform(true);
		
		JPanel root = new JPanel();
		root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
		{
			JPanel before = new JPanel();
			before.setLayout(new BoxLayout(before, BoxLayout.Y_AXIS));
			before.setBorder(BorderFactory.createTitledBorder("Before"));
			{
				before.add(_barBefore = new JProgressBar(SwingConstants.HORIZONTAL, 0, 100));
				JPanel info = new JPanel(new BorderLayout());
				{
					JPanel perc = new JPanel();
					{
						perc.add(_percBefore = new JLabel(NumberFormat.getPercentInstance(Loader.getLocale()).format(0)));
					}
					info.add(perc, BorderLayout.WEST);
					JPanel count = new JPanel();
					{
						count.add(_freeBefore = new JLabel(_mode.format(0, false)));
						count.add(new JLabel("/"));
						count.add(_totalBefore = new JLabel(_mode.format(0, false)));
					}
					info.add(count, BorderLayout.EAST);
				}
				before.add(info);
			}
			root.add(before);
			JPanel after = new JPanel();
			after.setLayout(new BoxLayout(after, BoxLayout.Y_AXIS));
			after.setBorder(BorderFactory.createTitledBorder("After"));
			{
				after.add(_barAfter = new JProgressBar(HORIZONTAL, 0, 100));
				JPanel info = new JPanel(new BorderLayout());
				{
					JPanel perc = new JPanel();
					{
						perc.add(_percAfter = new JLabel(NumberFormat.getPercentInstance(Loader.getLocale()).format(0)));
					}
					info.add(perc, BorderLayout.WEST);
					JPanel count = new JPanel();
					{
						count.add(_freeAfter = new JLabel(_mode.format(0, false)));
						count.add(new JLabel("/"));
						count.add(_totalAfter = new JLabel(_mode.format(0, false)));
					}
					info.add(count, BorderLayout.EAST);
				}
				after.add(info);
			}
			root.add(after);
			JButton ok = new JButton("OK");
			ok.setAlignmentX(CENTER_ALIGNMENT);
			ok.addActionListener(this);
			root.add(ok);
			getRootPane().setDefaultButton(ok);
		}
		getContentPane().add(root);
	}
	
	/**
	 * Saves current free and total memory sizes.<BR>
	 * <BR>
	 * This method may be called from outside the UI thread.
	 * 
	 * @see #markAfter()
	 */
	public void markBefore()
	{
		Runtime r = Runtime.getRuntime();
		_beforeFree = r.totalMemory() - r.freeMemory();
		_beforeTotal = r.totalMemory();
	}
	
	/**
	 * Saves current free and total memory sizes.<BR>
	 * <BR>
	 * This method may be called from outside the UI thread.
	 * 
	 * @see #markBefore()
	 */
	public void markAfter()
	{
		Runtime r = Runtime.getRuntime();
		_afterFree = r.totalMemory() - r.freeMemory();
		_afterTotal = r.totalMemory();
	}
	
	/**
	 * Sets the memory size unit to be used for display.
	 * 
	 * @param mode memory size unit
	 * @return {@code this}
	 */
	public GcInfoDialog setDisplayMode(MemorySizeUnit mode)
	{
		if (mode == null)
			mode = MemorySizeUnit.BYTES;
		
		_mode = mode;
		return this;
	}
	
	/**
	 * Propagates stored values to the displayed components.
	 * 
	 * @return {@code this}
	 */
	public GcInfoDialog prepareDialog()
	{
		final NumberFormat formatter = NumberFormat.getPercentInstance(Loader.getLocale());
		{
			{
				final double perc = ((double)_beforeFree / _beforeTotal);
				_barBefore.setValue((int)(perc * 100));
				_percBefore.setText(formatter.format(perc));
			}
			_freeBefore.setText(_mode.format(_beforeFree, false));
			_totalBefore.setText(_mode.format(_beforeTotal, false));
		}
		{
			{
				final double perc = ((double)_afterFree / _afterTotal);
				_barAfter.setValue((int)(perc * 100));
				_percAfter.setText(formatter.format(perc));
			}
			_freeAfter.setText(_mode.format(_afterFree, false));
			_totalAfter.setText(_mode.format(_afterTotal, false));
		}
		pack();
		return this;
	}
	
	/** Describes typical memory size units for convenience. */
	public enum MemorySizeUnit
	{
		/** A byte is typically a minimal unit. */
		BYTES("B"),
		/** 1024 bytes */
		KIBIBYTES("KiB"),
		/** 1048576 bytes */
		MEBIBYTES("MiB"),
		/** 1073741824 bytes */
		GIBIBYTES("GiB");
		
		private final String _symbol;
		
		private MemorySizeUnit(String symbol)
		{
			_symbol = symbol;
		}
		
		/**
		 * Returns a short textual representation of this unit.
		 * 
		 * @return prefixed acronym
		 */
		public String getSymbol()
		{
			return _symbol;
		}
		
		/**
		 * Factor to advance from a smaller unit towards the larger one.
		 * 
		 * @return factor
		 */
		public long getDivider()
		{
			return 1L << (ordinal() * 10);
		}
		
		/**
		 * Returns a formatted string for the given number.
		 * 
		 * @param number a number
		 * @param decimal whether to format the result as floating point
		 * @return formatted string
		 */
		public String format(long number, boolean decimal)
		{
			StringBuilder sb = new StringBuilder();
			if (decimal)
				sb.append(NumberFormat.getInstance(Loader.getLocale()).format(number / (double)getDivider()));
			else
				sb.append(NumberFormat.getIntegerInstance(Loader.getLocale()).format(number / getDivider()));
			sb.append(' ').append(getSymbol());
			return sb.toString();
		}
	}
	
	@Override
	public void actionPerformed(ActionEvent e)
	{
		setVisible(false);
		dispose();
	}
}
