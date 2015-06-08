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
package net.l2emuproject.proxy.network.meta.condition.impl;

import net.l2emuproject.proxy.network.meta.condition.DecimalCondition;
import net.l2emuproject.proxy.network.meta.condition.IntegerCondition;

/**
 * Tests whether the value of a byte/word/dword/qword is non negative (greater than or equal to zero).
 * 
 * @author savormix
 */
public final class NonNegative implements IntegerCondition, DecimalCondition
{
	@Override
	public boolean test(double value)
	{
		return Double.compare(value, 0D) >= 0;
	}
	
	@Override
	public boolean test(long value)
	{
		return value >= 0;
	}
}
