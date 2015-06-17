/*
 * Copyright (c) 2012-2015, RaveN Network INC. and/or its affiliates. All rights reserved.
 * RAVEN NETWORK INC. PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * 
 * Licensed Materials - Property of RaveN Network INC.
 * Restricted Rights - Use, duplication or disclosure restricted.
 */
package net.l2emuproject.ui.table;

import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;

/**
 * A table header implementation which exposes jdk-default renderer creation. 
 * 
 * @author _dev_
 */
final class DefaultTableHeaderRendererAccessor extends JTableHeader
{
	private static final long serialVersionUID = -5547777262728875818L;
	
	@Override
	public TableCellRenderer createDefaultRenderer()
	{
		return super.createDefaultRenderer();
	}
}
