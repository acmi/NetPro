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
package net.l2emuproject.proxy.ui.savormix.component.packet.config;

import java.awt.Window;

import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;

/**
 * Creates a dialog that reports indeterminate progress while a task is being executed.
 * 
 * @author savormix
 */
public class BlockingDialog extends JDialog implements SwingConstants
{
	private static final long serialVersionUID = 4179028689566257081L;
	
	BlockingDialog(Window owner, boolean modeless)
	{
		super(owner, modeless ? ModalityType.MODELESS : ModalityType.DOCUMENT_MODAL);
		
		final JPanel root = new JPanel();
		{
			final JProgressBar bar = new JProgressBar(HORIZONTAL);
			bar.setIndeterminate(true);
			root.add(bar);
		}
		getContentPane().add(root);
		
		setUndecorated(true);
		setLocationRelativeTo(owner);
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		pack();
		setResizable(false);
	}
	
	/** Closes this dialog. */
	public void close()
	{
		setVisible(false);
		dispose();
	}
}
