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
package condition;

import eu.revengineer.simplejse.HasScriptDependencies;

/**
 * Tests that the given integer is not equal to 1.
 * 
 * @author _dev_
 */
@HasScriptDependencies("condition.EngineObjectUpdate")
public final class EngineObjectAdd extends EngineObjectUpdate
{
	@Override
	public boolean test(long value)
	{
		return !super.test(value);
	}
}
