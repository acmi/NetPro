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
package condition.event;

import net.l2emuproject.proxy.script.condition.ScriptedIntegerEqualityCondition;

/**
 * Tests whether the value of a byte/word/dword is equal to 20090801.<BR>
 * <BR>
 * Used to check the type of parameter in <TT>EventInfo</TT>.
 * 
 * @author savormix
 */
public class EvaInferno extends ScriptedIntegerEqualityCondition
{
	/** Constructs this interpreter. */
	public EvaInferno()
	{
		super(20090801);
	}
}
