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

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;

import eu.revengineer.simplejse.type.UnloadableScript;

import net.l2emuproject.proxy.io.IOConstants;
import net.l2emuproject.proxy.network.meta.container.MetaclassRegistry;
import net.l2emuproject.proxy.network.meta.interpreter.IntegerIdInterpreter;
import net.l2emuproject.proxy.script.ScriptedMetaclass;
import net.l2emuproject.util.logging.L2Logger;

/**
 * Enhances {@link IntegerIdInterpreter} with managed script capabilities.
 * 
 * @author _dev_
 */
public abstract class ScriptedIntegerIdInterpreter extends IntegerIdInterpreter implements UnloadableScript
{
	private static final L2Logger LOG = L2Logger.getLogger(ScriptedIntegerIdInterpreter.class);
	
	/**
	 * Constructs this interpreter.
	 * 
	 * @param id2Interpretation known ID interpretations
	 * @param unknownKeyInterpretation unknown ID interpretation or {@code null}
	 */
	protected ScriptedIntegerIdInterpreter(Map<Long, ?> id2Interpretation, Object unknownKeyInterpretation)
	{
		super(id2Interpretation, unknownKeyInterpretation);
	}
	
	/**
	 * Constructs this interpreter.
	 * 
	 * @param id2Interpretation known ID interpretations
	 */
	protected ScriptedIntegerIdInterpreter(Map<Long, ?> id2Interpretation)
	{
		super(id2Interpretation);
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
	
	/**
	 * Loads the ID mapping from a simple text file.
	 * 
	 * @param resourceName filename
	 * @param additionalInterpretations special interpretations to be included
	 * @return loaded mapping
	 */
	@SafeVarargs
	protected static final Map<Long, String> loadFromResource(String resourceName, Pair<Long, String>... additionalInterpretations)
	{
		final Map<Long, String> mapping = new HashMap<>();
		for (final Pair<Long, String> additional : additionalInterpretations)
			mapping.put(additional.getLeft(), additional.getRight());
		try (final BufferedReader br = IOConstants.openScriptResource("interpreter", resourceName))
		{
			for (String line; (line = br.readLine()) != null;)
			{
				final int idx = line.indexOf('\t');
				if (idx == -1)
					continue;
				
				final Long id = Long.valueOf(line.substring(0, idx));
				final String name = line.substring(idx + 1);
				
				mapping.put(id, name.intern());
			}
		}
		catch (IOException e)
		{
			LOG.error("Could not load integer ID interpretations from " + resourceName, e);
		}
		return mapping.isEmpty() ? Collections.emptyMap() : mapping;
	}
}
