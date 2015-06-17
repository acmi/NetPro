/*
 * Copyright (c) 2012-2015, RaveN Network INC. and/or its affiliates. All rights reserved.
 * RAVEN NETWORK INC. PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * 
 * Licensed Materials - Property of RaveN Network INC.
 * Restricted Rights - Use, duplication or disclosure restricted.
 */
package net.l2emuproject.ui.table;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.plaf.UIResource;
import javax.swing.table.TableCellRenderer;

/**
 * A special component that renders as a {@code JTable} header, just with an additional text field for each column.
 * 
 * @author _dev_
 */
public class FilterableTableCellHeaderRenderer extends JPanel implements TableCellRenderer, UIResource
{
	private static final long serialVersionUID = 7322679579638624404L;
	
	final TableCellRenderer _colNameRenderer;
	final JComponent _colNameRendererComponent;
	
	final List<String> _filters;
	final JTextField _tfFilterRenderer;
	
	final MouseListener _clickListener;
	
	/**
	 * Creates table header component.
	 * 
	 * @param colNameRenderer associated table cell renderer
	 */
	public FilterableTableCellHeaderRenderer(TableCellRenderer colNameRenderer)
	{
		super(new BorderLayout());
		
		_colNameRenderer = colNameRenderer;
		_colNameRendererComponent = (JComponent)colNameRenderer;
		
		add(_colNameRendererComponent, BorderLayout.NORTH);
		
		_filters = new ArrayList<>();
		_tfFilterRenderer = new JTextField();
		FilterableTableHeader.setupFilterField(_tfFilterRenderer);
		add(_tfFilterRenderer, BorderLayout.SOUTH);
		
		_clickListener = new MouseListener() {
			@Override
			public void mouseReleased(MouseEvent e)
			{
				//consumeMouseEvent(e);
			}
			
			@Override
			public void mousePressed(MouseEvent e)
			{
				consumeMouseEvent(e);
			}
			
			@Override
			public void mouseExited(MouseEvent e)
			{
				//consumeMouseEvent(e);
			}
			
			@Override
			public void mouseEntered(MouseEvent e)
			{
				//consumeMouseEvent(e);
			}
			
			@Override
			public void mouseClicked(MouseEvent e)
			{
				consumeMouseEvent(e);
			}
			
			private boolean consumeMouseEvent(MouseEvent e)
			{
				if (e.getY() <= _colNameRendererComponent.getHeight())
					return false;
				
				e.consume();
				return true;
			}
		};
	}
	
	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
	{
		// deal with the label
		_colNameRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		// and with the text field
		String text = null;
		if (column >= 0 && column < _filters.size())
			text = _filters.get(column);
		_tfFilterRenderer.setText(text != null ? text : "…");//"<ANY>");
		return this;
	}
}
