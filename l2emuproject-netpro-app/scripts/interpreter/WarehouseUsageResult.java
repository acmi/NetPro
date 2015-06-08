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
 * Interprets the given byte/word/dword as a warehouse usage result.<BR>
 * <BR>
 * Both excess weight and full inventory do not cause a WH usage packet to be sent.
 * 
 * @author savormix
 */
public class WarehouseUsageResult extends ScriptedZeroBasedIntegerIdInterpreter
{
	/** Constructs this interpreter. */
	public WarehouseUsageResult()
	{
		super(new InterpreterMetadata(-2), "Not enough adena", "Warehouse is full", null, "Success");
	}
}
