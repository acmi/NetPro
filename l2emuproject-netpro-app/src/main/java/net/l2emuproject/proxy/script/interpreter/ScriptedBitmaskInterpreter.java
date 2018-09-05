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
package net.l2emuproject.proxy.script.interpreter;

import eu.revengineer.simplejse.type.UnloadableScript;

import net.l2emuproject.proxy.network.meta.container.MetaclassRegistry;
import net.l2emuproject.proxy.network.meta.interpreter.BitmaskTranslator;
import net.l2emuproject.proxy.script.ScriptedMetaclass;

/**
 * Enhances {@link BitmaskTranslator} with managed script capabilities.
 * 
 * @author _dev_
 */
public abstract class ScriptedBitmaskInterpreter extends BitmaskTranslator implements UnloadableScript
{
	/**
	 * Creates this interpreter.
	 * 
	 * @param zeroInterpretation interpretation of no enabled bits
	 * @param passNullHere {@code null}
	 * @param bitInterpretations known bit interpretations
	 */
	protected ScriptedBitmaskInterpreter(Object zeroInterpretation, Void passNullHere, Object... bitInterpretations)
	{
		super(zeroInterpretation, passNullHere, bitInterpretations);
	}
	
	/**
	 * Creates this interpreter.
	 * 
	 * @param bitInterpretations known bit interpretations
	 */
	protected ScriptedBitmaskInterpreter(Object... bitInterpretations)
	{
		super(bitInterpretations);
	}
	
	@Override
	public String getName()
	{
		return getClass().getSimpleName();
	}
	
	@Override
	public void onLoad() throws RuntimeException
	{
		MetaclassRegistry.getInstance().register(ScriptedMetaclass.getAlias(getClass()), this);
	}
	
	@Override
	public void onUnload() throws RuntimeException
	{
		MetaclassRegistry.getInstance().remove(ScriptedMetaclass.getAlias(getClass()), this);
	}
}
