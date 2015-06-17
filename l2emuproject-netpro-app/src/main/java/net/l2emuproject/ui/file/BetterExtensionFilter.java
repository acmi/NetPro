/*
 * Copyright (c) 2012-2015, RaveN Network INC. and/or its affiliates. All rights reserved.
 * RAVEN NETWORK INC. PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * 
 * Licensed Materials - Property of RaveN Network INC.
 * Restricted Rights - Use, duplication or disclosure restricted.
 */
package net.l2emuproject.ui.file;

import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * A class that creates swing extension filters.
 * 
 * @author savormix
 */
public final class BetterExtensionFilter
{
	private BetterExtensionFilter()
	{
		// utility class
	}
	
	/**
	 * Generates a typical file chooser extension filter description.
	 * 
	 * @param description file format description
	 * @param extensions possible file extensions
	 * @return filter with a proper description
	 */
	public static FileNameExtensionFilter create(String description, String... extensions)
	{
		final StringBuilder sb = new StringBuilder(description);
		sb.append(" (.").append(extensions[0]);
		for (int i = 1; i < extensions.length; ++i)
			sb.append(", .").append(extensions[i]);
		sb.append(')');
		return new FileNameExtensionFilter(sb.toString(), extensions);
	}
}
