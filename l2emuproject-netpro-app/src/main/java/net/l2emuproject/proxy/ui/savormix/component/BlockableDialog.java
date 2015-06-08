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

import java.awt.Container;
import java.awt.Window;
import java.awt.image.BufferedImage;

import javax.swing.JDialog;
import javax.swing.JLayer;

import net.l2emuproject.proxy.ui.savormix.EventSink;

/**
 * A dialog that supports emulated blocking.
 * 
 * @author savormix
 */
public abstract class BlockableDialog extends JDialog implements EventSink
{
	private static final long serialVersionUID = 1901085608412599973L;
	
	private final Container _content;
	
	private final WatermarkPane _sink;
	private final DisabledComponentUI _blockFeedback;
	
	/**
	 * Creates a dialog.
	 * 
	 * @param owner parent window
	 * @param title dialog title
	 * @param modality dialog modality
	 * @param watermark overlay image
	 */
	public BlockableDialog(Window owner, String title, ModalityType modality, BufferedImage watermark)
	{
		super(owner, title, modality);
		
		_blockFeedback = new DisabledComponentUI();
		setContentPane(new JLayer<>(_content = getContentPane(), _blockFeedback));
		
		setGlassPane(_sink = new WatermarkPane(watermark));
		getGlassPane().setVisible(true);
	}
	
	/**
	 * Allows direct access to the underlying content pane container (bypasses {@link JLayer}.
	 * 
	 * @return content pane
	 */
	public Container getActualContentPane()
	{
		return _content;
	}
	
	@Override
	public void startIgnoringEvents()
	{
		_sink.startIgnoringEvents();
		_blockFeedback.setActive(true);
		
		repaint();
	}
	
	@Override
	public void stopIgnoringEvents()
	{
		_sink.stopIgnoringEvents();
		_blockFeedback.setActive(false);
		
		repaint();
	}
}
