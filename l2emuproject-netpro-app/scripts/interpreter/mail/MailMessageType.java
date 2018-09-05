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
package interpreter.mail;

import eu.revengineer.simplejse.HasScriptDependencies;
import eu.revengineer.simplejse.init.InitializationPriority;

import net.l2emuproject.proxy.network.meta.container.MetaclassRegistry;
import net.l2emuproject.proxy.network.meta.exception.InvalidFieldValueInterpreterException;
import net.l2emuproject.proxy.network.meta.interpreter.IntegerTranslator;
import net.l2emuproject.proxy.script.interpreter.ScriptedZeroBasedIntegerIdInterpreter;

/**
 * @author _dev_
 */
@HasScriptDependencies("interpreter.Npc")
@InitializationPriority(1)
public final class MailMessageType extends ScriptedZeroBasedIntegerIdInterpreter
{
	/** Constructs this translator. */
	public MailMessageType()
	{
		super(getKnownTypes());
	}
	
	private static Object[] getKnownTypes()
	{
		try
		{
			final IntegerTranslator npc = MetaclassRegistry.getInstance().getTranslator("Npc", IntegerTranslator.class);
			return new Object[] { "Standard", "Private", "NPC", npc.translate(32_600, null), "Commission expiry", "Commission success", npc.translate(33_587, null), "Gift", null, null
			};
		}
		catch (final InvalidFieldValueInterpreterException e)
		{
			throw new AssertionError();
		}
	}
}
