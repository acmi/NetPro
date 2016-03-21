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

import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.apache.commons.lang3.ArrayUtils;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringExpression;
import javafx.beans.property.SimpleStringProperty;

/**
 * @author _dev_
 */
public final class UIStrings
{
	/** Effective locale */
	public static volatile Locale CURRENT_LOCALE = Locale.getDefault();
	/** All locales that have translated strings */
	public static final Map<String, Locale> SUPPORTED_LOCALES = Collections.singletonMap("English", Locale.ENGLISH);
	
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
			return String.format(CURRENT_LOCALE, getBundle().getString(name), tokens);
		}
		catch (MissingResourceException e)
		{
			final int idx = name.indexOf('.') + 1;
			// NOTE: normal underscore has a special meaning related to JavaFX mnemonic parsing
			// A0 for non-breaking space
			return name.substring(idx).replace('.', '\u00B7').toUpperCase(Locale.ENGLISH);
		}
	}
	
	/**
	 * Retrieves a localized version of the specified text as a JavaFX-compatible expression.
	 * 
	 * @param name text identifier
	 * @param tokens input tokens
	 * @return localized string expression
	 */
	public static final StringExpression getEx(String name, Object... tokens)
	{
		try
		{
			return Bindings.format(CURRENT_LOCALE, getBundle().getString(name), tokens);
		}
		catch (MissingResourceException e)
		{
			final int idx = name.indexOf('.') + 1;
			// NOTE: normal underscore has a special meaning related to JavaFX mnemonic parsing
			// A0 for non-breaking space
			return new SimpleStringProperty(name.substring(idx).replace('.', '\u00B7').toUpperCase(Locale.ENGLISH));
		}
	}
	
	/**
	 * Retrieves the localized resource bundle currently in use.
	 * 
	 * @return resource bundle
	 */
	public static final ResourceBundle getBundle()
	{
		return ResourceBundle.getBundle(UIStrings.class.getName(), CURRENT_LOCALE);
	}
}
