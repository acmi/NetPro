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

import static eu.revengineer.simplejse.logging.BytesizeInterpreter.BytesizeUnit.BYTES;
import static eu.revengineer.simplejse.logging.BytesizeInterpreter.BytesizeUnit.GIBIBYTES;
import static eu.revengineer.simplejse.logging.BytesizeInterpreter.BytesizeUnit.KIBIBYTES;
import static eu.revengineer.simplejse.logging.BytesizeInterpreter.BytesizeUnit.MEBIBYTES;
import static eu.revengineer.simplejse.logging.BytesizeInterpreter.BytesizeUnit.TEBIBYTES;

import java.text.NumberFormat;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import eu.revengineer.simplejse.logging.BytesizeInterpreter.BytesizeUnit;

/**
 * User-oriented bytesize formatting methods.
 * 
 * @author _dev_
 */
public final class BytesizeFormat
{
	private static final List<BytesizeUnit> UNITS_LARGEST_TO_SMALLEST = Arrays.asList(TEBIBYTES, GIBIBYTES, MEBIBYTES, KIBIBYTES, BYTES);
	private static final Map<BytesizeUnit, String> UNIT_SYMBOLS;
	static
	{
		UNIT_SYMBOLS = new EnumMap<>(BytesizeUnit.class);
		UNIT_SYMBOLS.put(BYTES, "B");
		UNIT_SYMBOLS.put(KIBIBYTES, "KiB");
		UNIT_SYMBOLS.put(MEBIBYTES, "MiB");
		UNIT_SYMBOLS.put(GIBIBYTES, "GiB");
		UNIT_SYMBOLS.put(TEBIBYTES, "TiB");
	}
	
	private BytesizeFormat()
	{
		// utility class
	}
	
	/**
	 * Formats a bytesize to a locale-specific, user-friendly approximate value with the decimal part truncated, e.g. passing 1100 would result in "{@code 1 KiB}".
	 * 
	 * @param amountInBytes bytesize
	 * @return user-friendly approximation
	 */
	public static final String formatAsInteger(long amountInBytes)
	{
		final BytesizeUnit unit = getLargestApplicableUnit(amountInBytes);
		return NumberFormat.getIntegerInstance(UIStrings.CURRENT_LOCALE).format(unit.convert(amountInBytes, BYTES)) + " " + UNIT_SYMBOLS.getOrDefault(unit, "???");
	}
	
	/**
	 * Formats a bytesize to a locale-specific, user-friendly approximate value with the decimal part represented using locale specific rules, e.g. passing 1100
	 * with the english language selected would result in "{@code 1.074 KiB}".
	 * 
	 * @param amountInBytes bytesize
	 * @return user-friendly approximation
	 */
	public static final String formatAsDecimal(long amountInBytes)
	{
		final BytesizeUnit unit = getLargestApplicableUnit(amountInBytes);
		return NumberFormat.getNumberInstance(UIStrings.CURRENT_LOCALE).format(unit.convertRounded(amountInBytes, BYTES)) + " " + UNIT_SYMBOLS.getOrDefault(unit, "???");
	}
	
	private static final BytesizeUnit getLargestApplicableUnit(long amountInBytes)
	{
		for (BytesizeUnit unit : UNITS_LARGEST_TO_SMALLEST)
			if (unit.convert(amountInBytes, BYTES) > 0)
				return unit;
		return BYTES;
	}
}
