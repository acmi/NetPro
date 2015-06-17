/*
 * Copyright (c) 2012-2015, RaveN Network INC. and/or its affiliates. All rights reserved.
 * RAVEN NETWORK INC. PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * 
 * Licensed Materials - Property of RaveN Network INC.
 * Restricted Rights - Use, duplication or disclosure restricted.
 */
package net.l2emuproject.ui.table;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.PatternSyntaxException;

import javax.swing.BorderFactory;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.RowFilter;
import javax.swing.Timer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

import net.l2emuproject.ui.listener.NoMulticastOfConsumedMouseEvents;

/**
 * A special {@code JTable} header component to allow built-in by-regex table content filtering.
 * 
 * @author _dev_
 */
public class FilterableTableHeader extends JTableHeader
{
	private static final long serialVersionUID = -3934215646724315452L;
	
	private final List<ColumnEditor> _editors;
	
	/**
	 * Creates a table header component.
	 * 
	 * @param cm table's columns
	 */
	public FilterableTableHeader(TableColumnModel cm)
	{
		super(cm);
		
		_editors = new ArrayList<>();
		
		final MouseListener[] cheaters = getMouseListeners();
		for (final MouseListener cheater : cheaters)
			removeMouseListener(cheater);
		addMouseListener(new MouseListener() {
			@Override
			public void mouseReleased(MouseEvent e)
			{
				getDefaultRenderer()._clickListener.mouseReleased(e);
			}
			
			@Override
			public void mousePressed(MouseEvent e)
			{
				getDefaultRenderer()._clickListener.mousePressed(e);
			}
			
			@Override
			public void mouseExited(MouseEvent e)
			{
				getDefaultRenderer()._clickListener.mouseExited(e);
			}
			
			@Override
			public void mouseEntered(MouseEvent e)
			{
				getDefaultRenderer()._clickListener.mouseEntered(e);
			}
			
			@Override
			public void mouseClicked(MouseEvent e)
			{
				getDefaultRenderer()._clickListener.mouseClicked(e);
				
				if (e.isConsumed())
					editFilter(e.getPoint());
			}
		});
		for (final MouseListener cheater : cheaters)
			addMouseListener(cheater instanceof NoMulticastOfConsumedMouseEvents ? cheater : new NoMulticastOfConsumedMouseEvents(cheater));
	}
	
	/*
	public FilterableTableHeader()
	{
		this(null);
	}
	*/
	
	@Override
	public FilterableTableCellHeaderRenderer getDefaultRenderer()
	{
		return (FilterableTableCellHeaderRenderer)super.getDefaultRenderer();
	}
	
	@Override
	public void setDefaultRenderer(TableCellRenderer defaultRenderer)
	{
		super.setDefaultRenderer(new FilterableTableCellHeaderRenderer(defaultRenderer));
	}
	
	void stopEditing(int column)
	{
		final ColumnEditor ce = _editors.get(column);
		ce._pmOverlay.setVisible(false);
		repaint();
	}
	
	void onEditEnd(int column)
	{
		final ColumnEditor ce = _editors.get(column);
		addFilter(ce._tfRegex.getText(), column, true);
	}
	
	void editFilter(Point p)
	{
		int columnIndex = columnAtPoint(p);
		
		if (columnIndex < 0)
			return;
		
		for (int i = _editors.size(); i <= columnIndex; ++i)
			_editors.add(new ColumnEditor(i));
		final ColumnEditor ce = _editors.get(columnIndex);
		
		final List<String> vals = getDefaultRenderer()._filters;
		ce._tfRegex.setText(columnIndex < vals.size() ? vals.get(columnIndex) : null);
		
		final Rectangle columnRectangle = getHeaderRect(columnIndex);
		ce._pmOverlay.setPreferredSize(new Dimension(columnRectangle.width, getDefaultRenderer()._tfFilterRenderer.getHeight()));
		ce._pmOverlay.show(this, columnRectangle.x, getDefaultRenderer()._tfFilterRenderer.getY());
		
		ce._tfRegex.requestFocusInWindow();
		ce._tfRegex.selectAll();
	}
	
	void addFilter(String regex, int column, boolean persist)
	{
		if (regex.isEmpty())
			regex = null;
		
		final Timer updater = _editors.get(column)._updater;
		final boolean alreadyApplied = persist && !updater.isRunning();
		updater.stop();
		
		final List<String> vals = getDefaultRenderer()._filters;
		for (int i = vals.size() - 1; i <= column; ++i)
			vals.add(null);
		
		final String old = vals.get(column);
		vals.set(column, regex);
		if (!alreadyApplied)
			applyFilters();
		if (!persist)
			vals.set(column, old);
		else
			stopEditing(column);
	}
	
	void applyFilters()
	{
		final List<RowFilter<Object, Object>> filters = new ArrayList<>();
		
		final List<String> vals = getDefaultRenderer()._filters;
		for (int i = 0; i < vals.size(); ++i)
		{
			final String next = vals.get(i);
			if (next == null)
				continue;
			try
			{
				filters.add(RowFilter.regexFilter("(?i)" + next, i));
			}
			catch (PatternSyntaxException e)
			{
				continue;
			}
		}
		
		@SuppressWarnings("unchecked")
		final TableRowSorter<TableModel> trs = (TableRowSorter<TableModel>)getTable().getRowSorter();
		trs.setRowFilter(RowFilter.andFilter(filters));
	}
	
	static void setupFilterField(JTextField textField)
	{
		textField.setForeground(textField.getForeground().brighter().brighter());
		textField.setFont(textField.getFont().deriveFont(textField.getFont().getSize2D() - 2));
	}
	
	private final class ColumnEditor
	{
		final int _column;
		final JTextField _tfRegex;
		final JPopupMenu _pmOverlay;
		final Timer _updater;
		
		ColumnEditor(int column)
		{
			_column = column;
			
			_updater = new Timer(250, e -> addFilter(e.getActionCommand(), _column, false));
			_updater.setRepeats(false);
			
			_tfRegex = new JTextField();
			_tfRegex.getDocument().addDocumentListener(new DocumentListener() {
				@Override
				public void removeUpdate(DocumentEvent e)
				{
					scheduleFilter(e);
				}
				
				@Override
				public void insertUpdate(DocumentEvent e)
				{
					scheduleFilter(e);
				}
				
				@Override
				public void changedUpdate(DocumentEvent e)
				{
					// TODO Auto-generated method stub
					
				}
				
				private void scheduleFilter(DocumentEvent e)
				{
					final Document doc = e.getDocument();
					try
					{
						_updater.stop();
						_updater.setActionCommand(doc.getText(0, doc.getLength()));
						_updater.restart();
					}
					catch (BadLocationException e1)
					{
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			});
			_tfRegex.addActionListener(e -> onEditEnd(_column));
			setupFilterField(_tfRegex);
			
			_pmOverlay = new JPopupMenu();
			//_editorPopup.setLayout(new BorderLayout());
			_pmOverlay.add(_tfRegex/*, BorderLayout.CENTER*/);
			_pmOverlay.setBorder(BorderFactory.createEmptyBorder());
			_pmOverlay.addPopupMenuListener(new PopupMenuListener() {
				@Override
				public void popupMenuWillBecomeVisible(PopupMenuEvent e)
				{
					// nothing
				}
				
				@Override
				public void popupMenuWillBecomeInvisible(PopupMenuEvent e)
				{
					// nothing
				}
				
				@Override
				public void popupMenuCanceled(PopupMenuEvent e)
				{
					onEditEnd(_column);
				}
			});
		}
	}
}
