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
package net.l2emuproject.proxy.ui.i18n;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.apache.commons.lang3.ArrayUtils;

/**
 * @author _dev_
 */
public final class UIStrings
{
	/** Effective locale */
	public static volatile Locale CURRENT_LOCALE = Locale.getDefault();
	
	private UIStrings()
	{
		// utility class
	}
	
	/**
	 * Retrieves a localized version of the specified text.
	 * 
	 * @param name text identifier
	 * @return localized string
	 */
	public static final String get(String name)
	{
		return get(name, ArrayUtils.EMPTY_OBJECT_ARRAY);
	}
	
	/**
	 * Retrieves a localized version of the specified text.
	 * 
	 * @param name text identifier
	 * @param tokens input tokens
	 * @return localized string
	 */
	public static final String get(String name, Object... tokens)
	{
		try
		{
			return String.format(CURRENT_LOCALE, ResourceBundle.getBundle(UIStrings.class.getName(), CURRENT_LOCALE).getString(name), tokens);
		}
		catch (MissingResourceException e)
		{
			final int idx = name.indexOf('.') + 1;
			return name.substring(idx).replace('.', '_').toUpperCase(Locale.ENGLISH);
		}
	}
}
