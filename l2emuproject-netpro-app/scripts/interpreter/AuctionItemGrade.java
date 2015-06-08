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

import net.l2emuproject.proxy.script.interpreter.ScriptedZeroBasedIntegerIdInterpreter;

/**
 * Interprets the given byte/word/dword as an item grade.
 * 
 * @author savormix
 */
public class AuctionItemGrade extends ScriptedZeroBasedIntegerIdInterpreter
{
	/** Constructs this interpreter. */
	public AuctionItemGrade()
	{
		// @formatter:off
		super(new InterpreterMetadata(-1),
				"All",
				"No grade", // 0
				"D grade",
				"C grade",
				"B grade",
				"A grade",
				"S grade", // 5
				"S80 grade",
				"S84 grade (N/A)",
				"R grade",
				"R95 grade",
				"R99 grade" // 10
		);
		// @formatter:on
	}
}
