/*
 * Copyright 2011-2017 L2EMU UNIQUE
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
package interpreter.appearance;

import java.util.List;

import eu.revengineer.simplejse.HasScriptDependencies;

import net.l2emuproject.proxy.network.meta.EnumeratedPayloadField;
import net.l2emuproject.proxy.network.meta.RandomAccessMMOBuffer;

/**
 * @author _dev_
 */
@HasScriptDependencies("interpreter.appearance.BeautyTranslator")
public class BeautyHair extends BeautyTranslator
{
	/** Constructs this translator. */
	public BeautyHair()
	{
		super("beauty_hair.txt");
	}
	
	@Override
	public void reviewContext(RandomAccessMMOBuffer buf)
	{
		final List<EnumeratedPayloadField> classNames = buf.getFieldIndices("__INTERP_BEAUTY_CLASS_NAME_0");
		for (int i = 0; i < classNames.size(); ++i)
		{
			if (classNames.get(i).getOffset() == buf.getCurrentOffset())
			{
				_className.set(0L);
				return;
			}
		}
		
		super.reviewContext(buf);
	}
}
