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
package net.l2emuproject.proxy.ui.savormix;

import java.util.regex.Pattern;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

/**
 * @author savormix
 */
public class IntegerInput extends PlainDocument
{
	private static final long serialVersionUID = 3409190630224776734L;
	private static final Pattern UNSIGNED_INTEGER = Pattern.compile("[0-9]+");
	
	@Override
	public void insertString(int offs, String str, AttributeSet a) throws BadLocationException
	{
		if (UNSIGNED_INTEGER.matcher(str).matches())
			super.insertString(offs, str, a);
	}
}
