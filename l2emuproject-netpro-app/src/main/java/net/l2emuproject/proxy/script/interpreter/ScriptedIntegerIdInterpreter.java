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
import java.nio.file.NoSuchFileException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.LongBinaryOperator;

import org.apache.commons.lang3.tuple.Pair;

import eu.revengineer.simplejse.type.UnloadableScript;

import net.l2emuproject.network.protocol.ClientProtocolVersion;
import net.l2emuproject.network.protocol.IProtocolVersion;
import net.l2emuproject.network.protocol.ProtocolVersionManager;
import net.l2emuproject.proxy.io.IOConstants;
import net.l2emuproject.proxy.network.meta.FieldValueTranslator;
import net.l2emuproject.proxy.network.meta.container.MetaclassRegistry;
import net.l2emuproject.proxy.network.meta.interpreter.IntegerIdTranslator;
import net.l2emuproject.proxy.script.ScriptedMetaclass;
import net.l2emuproject.util.logging.L2Logger;

/**
 * Enhances {@link IntegerIdTranslator} with managed script capabilities.
 * 
 * @author _dev_
 */
public abstract class ScriptedIntegerIdInterpreter extends IntegerIdTranslator implements UnloadableScript
{
	private static final L2Logger LOG = L2Logger.getLogger(ScriptedIntegerIdInterpreter.class);
	
	/**
	 * Constructs this interpreter.
	 * 
	 * @param id2InterpretationWrapper known ID interpretations
	 * @param unknownKeyInterpretation unknown ID interpretation or {@code null}
	 */
	protected ScriptedIntegerIdInterpreter(Map<IProtocolVersion, Map<Long, ?>> id2InterpretationWrapper, Object unknownKeyInterpretation)
	{
		super(id2InterpretationWrapper, unknownKeyInterpretation);
	}
	
	/**
	 * Constructs this interpreter.
	 * 
	 * @param id2InterpretationWrapper known ID interpretations
	 */
	protected ScriptedIntegerIdInterpreter(Map<IProtocolVersion, Map<Long, ?>> id2InterpretationWrapper)
	{
		super(id2InterpretationWrapper);
	}
	
	@Override
	public boolean dependsOnLoadedProtocols()
	{
		return true;
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
	protected static final Map<IProtocolVersion, Map<Long, ?>> loadFromResource(String resourceName, Pair<Long, String>... additionalInterpretations)
	{
		if (!Boolean.getBoolean(FieldValueTranslator.PROPERTY_PROTOCOLS_LOADED))
			return Collections.emptyMap();
		
		final Map<IProtocolVersion, Map<Long, String>> wrapper = new HashMap<>();
		try (final BufferedReader br = IOConstants.openScriptResource("interpreter", resourceName))
		{
			final Map<Long, String> mapping = new HashMap<>();
			wrapper.put(null, Collections.unmodifiableMap(mapping));
			for (final Pair<Long, String> additional : additionalInterpretations)
				mapping.put(additional.getLeft(), additional.getRight());
			for (String line; (line = br.readLine()) != null;)
			{
				final int idx = line.indexOf('\t');
				if (idx == -1)
					continue;
				
				final Long id = Long.valueOf(line.substring(0, idx));
				final String name = line.substring(idx + 1);
				
				mapping.put(id, name.intern());
			}
			LOG.info("[LIVE]" + resourceName + ": " + mapping.size() + " entries loaded");
		}
		catch (final IOException e)
		{
			LOG.error("Could not load LIVE integer ID interpretations from " + resourceName, e);
		}
		
		final IProtocolVersion classicProtocol = ProtocolVersionManager.getInstance().getGameProtocolWithoutFallback(ClientProtocolVersion.ORFEN.getVersion(),
				ClientProtocolVersion.ORFEN.getAltModes());
		if (classicProtocol != null)
		{
			try (final BufferedReader br = IOConstants.openScriptResource("interpreter", "classic", resourceName))
			{
				final Map<Long, String> mapping = new HashMap<>();
				wrapper.put(classicProtocol, Collections.unmodifiableMap(mapping));
				for (String line; (line = br.readLine()) != null;)
				{
					final int idx = line.indexOf('\t');
					if (idx == -1)
						continue;
					
					final Long id = Long.valueOf(line.substring(0, idx));
					final String name = line.substring(idx + 1);
					
					mapping.put(id, name.intern());
				}
				LOG.info("[CLASSIC]" + resourceName + ": " + mapping.size() + " entries loaded");
			}
			catch (final NoSuchFileException e)
			{
				// no big deal
			}
			catch (final IOException e)
			{
				LOG.error("Could not load CLASSIC integer ID interpretations from " + resourceName, e);
			}
		}
		return wrapper.isEmpty() ? Collections.emptyMap() : Collections.unmodifiableMap(wrapper);
	}
	
	/**
	 * Loads the two ID mapping from a simple text file.
	 * 
	 * @param resourceName filename
	 * @param idMapper a mapper to convert two separate IDs to one unified ID
	 * @return loaded mapping
	 */
	protected static final Map<IProtocolVersion, Map<Long, ?>> loadFromResource2(String resourceName, LongBinaryOperator idMapper)
	{
		if (!Boolean.getBoolean(FieldValueTranslator.PROPERTY_PROTOCOLS_LOADED))
			return Collections.emptyMap();
		
		final Map<IProtocolVersion, Map<Long, String>> wrapper = new HashMap<>();
		try (final BufferedReader br = IOConstants.openScriptResource("interpreter", resourceName))
		{
			final Map<Long, String> mapping = new HashMap<>();
			wrapper.put(null, Collections.unmodifiableMap(mapping));
			for (String line; (line = br.readLine()) != null;)
			{
				final int idx = line.indexOf('\t'), idx2 = line.indexOf('\t', idx + 1);
				if (idx == -1 || idx2 == -1)
					continue;
				
				final long id1 = Integer.parseInt(line.substring(0, idx));
				final long id2 = Integer.parseInt(line.substring(idx + 1, idx2));
				final String name = line.substring(idx2 + 1);
				
				mapping.put(idMapper.applyAsLong(id1, id2), name.intern());
			}
			LOG.info("[LIVE]" + resourceName + ": " + mapping.size() + " entries loaded");
		}
		catch (final IOException e)
		{
			LOG.error("Could not load LIVE integer ID interpretations from " + resourceName, e);
		}
		
		final IProtocolVersion classicProtocol = ProtocolVersionManager.getInstance().getGameProtocolWithoutFallback(ClientProtocolVersion.ORFEN.getVersion(),
				ClientProtocolVersion.ORFEN.getAltModes());
		if (classicProtocol != null)
		{
			try (final BufferedReader br = IOConstants.openScriptResource("interpreter", "classic", resourceName))
			{
				final Map<Long, String> mapping = new HashMap<>();
				wrapper.put(classicProtocol, Collections.unmodifiableMap(mapping));
				for (String line; (line = br.readLine()) != null;)
				{
					final int idx = line.indexOf('\t'), idx2 = line.indexOf('\t', idx + 1);
					if (idx == -1 || idx2 == -1)
						continue;
					
					final long id1 = Integer.parseInt(line.substring(0, idx));
					final long id2 = Integer.parseInt(line.substring(idx + 1, idx2));
					final String name = line.substring(idx2 + 1);
					
					mapping.put((id1 << 32) | (id2 & 0xFF_FF_FF_FFL), name.intern());
				}
				LOG.info("[CLASSIC]" + resourceName + ": " + mapping.size() + " entries loaded");
			}
			catch (final NoSuchFileException e)
			{
				// no big deal
			}
			catch (final IOException e)
			{
				LOG.error("Could not load CLASSIC integer ID interpretations from " + resourceName, e);
			}
		}
		return wrapper.isEmpty() ? Collections.emptyMap() : Collections.unmodifiableMap(wrapper);
	}
	
	/**
	 * Loads the two ID mapping from a simple text file.
	 * 
	 * @param resourceName filename
	 * @return loaded mapping
	 */
	protected static final Map<IProtocolVersion, Map<Long, ?>> loadFromResource2(String resourceName)
	{
		return loadFromResource2(resourceName, (id1, id2) -> (id1 << 32) | (id2 & 0xFF_FF_FF_FFL));
	}
}
