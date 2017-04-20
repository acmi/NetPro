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
package net.l2emuproject.proxy.script;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import net.l2emuproject.network.mmocore.MMOBuffer;
import net.l2emuproject.network.mmocore.MMOLogger;
import net.l2emuproject.network.protocol.IProtocolVersion;
import net.l2emuproject.proxy.network.EndpointType;
import net.l2emuproject.proxy.network.meta.L2PpeProvider;
import net.l2emuproject.proxy.network.meta.RandomAccessMMOBuffer;
import net.l2emuproject.proxy.network.meta.exception.InvalidPacketOpcodeSchemeException;
import net.l2emuproject.proxy.network.meta.exception.PartialPayloadEnumerationException;
import net.l2emuproject.proxy.script.game.PpeGameScriptRegistry;
import net.l2emuproject.proxy.ui.savormix.io.task.HistoricalPacketLog;
import net.l2emuproject.proxy.ui.savormix.io.task.HistoricalPacketLogAsAuthor;
import net.l2emuproject.util.StackTraceUtil;

/**
 * Allows PPE scripts to deal with historical packet logs.
 * 
 * @author _dev_
 */
public class PpeEnabledLoaderScriptRegistry extends LogLoadScript
{
	private static final MMOLogger LOG = new MMOLogger(PpeGameScriptRegistry.class, 3000);
	
	private final Set<PpeEnabledLoaderScript> _registry;
	
	PpeEnabledLoaderScriptRegistry()
	{
		super("PPE Loader GameScript Root", false, WILDCARD, WILDCARD);
		
		_registry = new CopyOnWriteArraySet<>();
	}
	
	/**
	 * Registers a PPE script to handle historical packet logs.
	 * 
	 * @param script a PPE script
	 */
	public void register(PpeEnabledLoaderScript script)
	{
		_registry.add(script);
	}
	
	/**
	 * Deregisters a PPE script from handling historical packet logs.
	 * 
	 * @param script a PPE script
	 */
	public void remove(PpeEnabledLoaderScript script)
	{
		_registry.remove(script);
	}
	
	@Override
	protected void handleClientPacket(MMOBuffer buf, IProtocolVersion version, HistoricalPacketLog cacheContext, long receivedOn) throws RuntimeException
	{
		final RandomAccessMMOBuffer rab;
		try
		{
			rab = L2PpeProvider.getPacketPayloadEnumerator().enumeratePacketPayload(version, buf, new HistoricalPacketLogAsAuthor(cacheContext, EndpointType.CLIENT));
		}
		catch (final InvalidPacketOpcodeSchemeException e)
		{
			LOG.error("Invalid client packet", e);
			return;
		}
		catch (final PartialPayloadEnumerationException e)
		{
			LOG.error("Invalid client packet " + e.getTemplate(), e);
			return;
		}
		
		for (final PpeEnabledLoaderScript script : _registry)
		{
			try
			{
				if (!Collections.disjoint(script.getHandledScriptFieldAliases(), rab.getAllFields()))
					script.handleClientPacket(rab, cacheContext);
			}
			catch (final RuntimeException e)
			{
				LOG.error("PPE Loader game script '" + script.getName() + "' failed handling client packet", e);
			}
		}
	}
	
	@Override
	protected void handleServerPacket(MMOBuffer buf, IProtocolVersion version, HistoricalPacketLog cacheContext, long receivedOn) throws RuntimeException
	{
		final RandomAccessMMOBuffer rab;
		try
		{
			rab = L2PpeProvider.getPacketPayloadEnumerator().enumeratePacketPayload(version, buf, new HistoricalPacketLogAsAuthor(cacheContext, EndpointType.SERVER));
		}
		catch (final InvalidPacketOpcodeSchemeException e)
		{
			LOG.error("Invalid server packet", e);
			return;
		}
		catch (final PartialPayloadEnumerationException e)
		{
			LOG.error("Invalid server packet " + e.getTemplate(), StackTraceUtil.stripRunnable(e));
			return;
		}
		
		for (final PpeEnabledLoaderScript script : _registry)
		{
			try
			{
				if (!Collections.disjoint(script.getHandledScriptFieldAliases(), rab.getAllFields()))
					script.handleServerPacket(rab, cacheContext);
			}
			catch (final RuntimeException e)
			{
				LOG.error("PPE Loader game script '" + script.getName() + "' failed handling server packet", e);
			}
		}
	}
	
	@Override
	protected boolean isHandled(boolean client, int opcode)
	{
		if (_registry.isEmpty())
			return false;
		
		return super.isHandled(client, opcode);
	}
	
	/**
	 * Returns a singleton instance of this type.
	 * 
	 * @return an instance of this class
	 */
	public static final PpeEnabledLoaderScriptRegistry getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static final class SingletonHolder
	{
		static final PpeEnabledLoaderScriptRegistry INSTANCE = new PpeEnabledLoaderScriptRegistry();
	}
}
