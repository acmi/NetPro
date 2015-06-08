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

import net.l2emuproject.proxy.script.interpreter.ScriptedLegacyIntegerIdInterpreter;

/**
 * Interprets the given byte/word/dword as a type of flight.
 * 
 * @author savormix
 */
public class FlyType extends ScriptedLegacyIntegerIdInterpreter
{
	@Override
	protected void loadImpl()
	{
		addInterpretation(0, "Vertical Throw");
		addInterpretation(1, "Horizontal Throw");
		addInterpretation(3, "Charge");
		addInterpretation(4, "Knockback");
		addInterpretation(5, "Vertical Hold");
		addInterpretation(6, "Not Used");
		addInterpretation(7, "Knockdown");
		addInterpretation(8, "Warp Backward");
		addInterpretation(9, "Warp Forward");
	}
}
