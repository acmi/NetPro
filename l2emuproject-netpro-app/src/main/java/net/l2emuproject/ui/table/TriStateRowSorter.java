/*
 * Copyright (c) 2012-2015, RaveN Network INC. and/or its affiliates. All rights reserved.
 * RAVEN NETWORK INC. PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * 
 * Licensed Materials - Property of RaveN Network INC.
 * Restricted Rights - Use, duplication or disclosure restricted.
 */
package net.l2emuproject.ui.table;

import java.util.ArrayList;
import java.util.List;

import javax.swing.SortOrder;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

/**
 * Allows to toggle to disable column sort.
 * 
 * @author _dev_
 * @param <M> the type of the model, which must be an implementation of
 *            <code>TableModel</code>
 */
public class TriStateRowSorter<M extends TableModel> extends TableRowSorter<M>
{
	/**
	 * Creates a row sorter.
	 * 
	 * @param model table model
	 */
	public TriStateRowSorter(M model)
	{
		super(model);
	}
	
	@Override
	public void toggleSortOrder(int column)
	{
		if (!isSortable(column))
			return;
		
		List<SortKey> keys = new ArrayList<SortKey>(getSortKeys());
		if (keys.isEmpty() || keys.get(0).getSortOrder() != SortOrder.DESCENDING)
		{
			super.toggleSortOrder(column);
			return;
		}
		
		keys.remove(0);
		setSortKeys(keys);
	}
}
