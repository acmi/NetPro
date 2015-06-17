/*
 * Copyright (c) 2012-2015, RaveN Network INC. and/or its affiliates. All rights reserved.
 * RAVEN NETWORK INC. PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * 
 * Licensed Materials - Property of RaveN Network INC.
 * Restricted Rights - Use, duplication or disclosure restricted.
 */
package net.l2emuproject.ui.listener;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

/**
 * Makes sure that no events that were already consumed would propagate to the delegate.
 * 
 * @author _dev_
 */
public class NoMulticastOfConsumedMouseEvents implements MouseListener
{
	private final MouseListener _listener;
	
	/**
	 * Filters consumed events from reaching {@code listener}.
	 * 
	 * @param listener a mouse event listener
	 */
	public NoMulticastOfConsumedMouseEvents(MouseListener listener)
	{
		_listener = listener;
	}
	
	@Override
	public void mouseClicked(MouseEvent e)
	{
		if (!e.isConsumed())
			_listener.mouseClicked(e);
	}
	
	@Override
	public void mousePressed(MouseEvent e)
	{
		if (!e.isConsumed())
			_listener.mousePressed(e);
	}
	
	@Override
	public void mouseReleased(MouseEvent e)
	{
		if (!e.isConsumed())
			_listener.mouseReleased(e);
	}
	
	@Override
	public void mouseEntered(MouseEvent e)
	{
		if (!e.isConsumed())
			_listener.mouseEntered(e);
	}
	
	@Override
	public void mouseExited(MouseEvent e)
	{
		if (!e.isConsumed())
			_listener.mouseExited(e);
	}
}
