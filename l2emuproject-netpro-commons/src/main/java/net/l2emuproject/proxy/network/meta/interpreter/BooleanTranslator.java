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
package net.l2emuproject.proxy.network.meta.interpreter;

import net.l2emuproject.network.protocol.IProtocolVersion;
import net.l2emuproject.proxy.state.entity.context.ICacheServerID;

/**
 * Interprets a number as a boolean using C logic:<BR>
 * 0 - {@code false}<BR>
 * !0 - {@code true}<BR>
 * <BR>
 * The interpreting class specifies how to interpret {@code true} and {@code false} instead of
 * interpreting a value directly.
 * 
 * @author savormix
 */
public abstract class BooleanTranslator implements IntegerTranslator
{
	private final Object _falseEquivalent, _trueEquivalent;
	
	/**
	 * Constructs this interpreter.
	 * 
	 * @param falseEquivalent interpretation for {@code false}
	 * @param trueEquivalent interpretation for {@code true}
	 */
	protected BooleanTranslator(Object falseEquivalent, Object trueEquivalent)
	{
		_falseEquivalent = falseEquivalent;
		_trueEquivalent = trueEquivalent;
	}
	
	@Override
	public Object translate(long value, IProtocolVersion protocol, ICacheServerID entityCacheContext)
	{
		return value != 0 ? _trueEquivalent : _falseEquivalent;
	}
}
