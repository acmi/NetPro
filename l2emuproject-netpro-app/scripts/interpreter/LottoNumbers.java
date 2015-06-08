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
package interpreter;

import net.l2emuproject.proxy.script.interpreter.ScriptedBitmaskInterpreter;

/**
 * Interprets two and a half bytes in a DWORD as selected lotto ticket numbers.
 * 
 * @author _dev_
 */
public class LottoNumbers extends ScriptedBitmaskInterpreter
{
	/** Constructs this interpreter. */
	public LottoNumbers()
	{
		super(generate());
	}
	
	private static final Object[] generate()
	{
		final String[] result = new String[20];
		for (int i = 0; i < result.length; ++i)
			result[i] = String.valueOf(i + 1);
		return result;
	}
}
