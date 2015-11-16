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
package interpreter.daily;

import net.l2emuproject.proxy.script.interpreter.ScriptedZeroBasedIntegerIdInterpreter;

/**
 * Interprets an integer as a day of week.
 * 
 * @author _dev_
 */
public class DayOfWeek extends ScriptedZeroBasedIntegerIdInterpreter
{
	/** Constructs this interpreter. */
	public DayOfWeek()
	{
		super("Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday");
	}
}
