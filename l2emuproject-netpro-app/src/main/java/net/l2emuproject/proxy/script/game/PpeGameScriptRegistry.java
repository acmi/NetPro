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
package net.l2emuproject.proxy.script.game;

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import net.l2emuproject.network.mmocore.MMOBuffer;
import net.l2emuproject.network.mmocore.MMOLogger;
import net.l2emuproject.proxy.network.game.client.L2GameClient;
import net.l2emuproject.proxy.network.game.server.L2GameServer;
import net.l2emuproject.proxy.network.meta.IPacketTemplate;
import net.l2emuproject.proxy.network.meta.L2PpeProvider;
import net.l2emuproject.proxy.network.meta.RandomAccessMMOBuffer;
import net.l2emuproject.proxy.network.meta.exception.InvalidPacketOpcodeSchemeException;
import net.l2emuproject.proxy.network.meta.exception.PartialPayloadEnumerationException;
import net.l2emuproject.proxy.script.ScriptFieldAlias;
import net.l2emuproject.proxy.script.ScriptManager;
import net.l2emuproject.util.concurrent.RunnableStatsManager;

/**
 * @author _dev_
 */
public final class PpeGameScriptRegistry extends GameScript
{
	private static final MMOLogger LOG = new MMOLogger(PpeGameScriptRegistry.class, 3_000);
	
	private final List<PpeEnabledGameScript> _registry, _registryEx;
	
	/** Creates this script manager script. */
	public PpeGameScriptRegistry()
	{
		super(WILDCARD, WILDCARD);
		
		_registry = new CopyOnWriteArrayList<>();
		_registryEx = new CopyOnWriteArrayList<>();
	}
	
	/**
	 * Registers a PPE script to receive notifications based on its {@link ScriptFieldAlias} annotated fields.
	 * Injected packets will not be enumerated for the given script.
	 * 
	 * @param script a PPE script
	 */
	public void register(PpeEnabledGameScript script)
	{
		final int ip = Collections.binarySearch(_registry, script, PriorityComparator.getInstance());
		if (ip >= 0)
			return;
		
		_registry.add(-ip - 1, script);
		setEnabled(true);
	}
	
	//!! UNDOCUMENTED FEATURE: !! allows scripts to see injected packets
	// injected server packets are NOT SUPPORTED at this time
	/**
	 * Registers a PPE script to receive notifications based on its {@link ScriptFieldAlias} annotated fields.
	 * Injected client packets will be enumerated for the given script.
	 * 
	 * @param script a PPE script
	 */
	public void registerEx(PpeEnabledGameScript script)
	{
		final int ip = Collections.binarySearch(_registryEx, script, PriorityComparator.getInstance());
		if (ip >= 0)
			return;
		
		_registryEx.add(-ip - 1, script);
		setEnabled(true);
	}
	
	/**
	 * Removes a PPE script, so that it would no longer receive packet notifications.
	 * 
	 * @param script a PPE script
	 */
	public void remove(PpeEnabledGameScript script)
	{
		_registry.remove(script);
		_registryEx.remove(script);
	}
	
	@Override
	protected void connectionTerminated(L2GameClient client, L2GameServer server)
	{
		for (final PpeEnabledGameScript script : _registry)
		{
			try
			{
				script.handleDisconnection(client);
			}
			catch (final RuntimeException e)
			{
				LOG.error(this, e);
			}
		}
	}
	
	@Override
	protected void clientPacketForwarded(L2GameClient client, MMOBuffer buf)
	{
		final RandomAccessMMOBuffer rab;
		try
		{
			rab = L2PpeProvider.getPacketPayloadEnumerator().enumeratePacketPayload(client.getProtocol(), buf, client);
		}
		catch (final InvalidPacketOpcodeSchemeException e)
		{
			LOG.error("Invalid client packet", e);
			return;
		}
		catch (final PartialPayloadEnumerationException e)
		{
			final IPacketTemplate template = e.getTemplate();
			if (!template.isDefined())
			{
				LOG.warn("Unknown packet [C] " + template);
				return;
			}
			
			final String message = "[C] " + template;
			if (e.getUnusedBytes() != -1)
				LOG.warn(message + " " + e.getMessage());
			else
				LOG.error(message, e);
			return;
		}
		
		if (rab.getAllFields().isEmpty())
		{
			if (rab.getPacketName() == null)
				return;
			
			for (final PpeEnabledGameScript script : _registry)
			{
				try
				{
					if (script.getHandledPacketNames().contains(rab.getPacketName()))
					{
						final long start = System.nanoTime();
						script.handleClientPacket(client, (L2GameServer)client.getTarget(), rab);
						final long end = System.nanoTime();
						
						RunnableStatsManager.handleStats(script.getClass(), "handleClientPacket(L2GameClient, L2GameServer, RandomAccessMMOBuffer)", end - start, 50);
					}
				}
				catch (final RuntimeException e)
				{
					LOG.error("PPE Game script '" + script.getName() + "' failed handling client packet", e);
				}
			}
			return;
		}
		
		for (final PpeEnabledGameScript script : _registry)
		{
			try
			{
				if (!Collections.disjoint(script.getHandledScriptFieldAliases(), rab.getAllFields()) || script.getHandledPacketNames().contains(rab.getPacketName()))
				{
					final long start = System.nanoTime();
					script.handleClientPacket(client, (L2GameServer)client.getTarget(), rab);
					final long end = System.nanoTime();
					
					RunnableStatsManager.handleStats(script.getClass(), "handleClientPacket(L2GameClient, L2GameServer, RandomAccessMMOBuffer)", end - start, 50);
				}
			}
			catch (final RuntimeException e)
			{
				LOG.error("PPE Game script '" + script.getName() + "' failed handling client packet", e);
			}
		}
	}
	
	@Override
	protected void serverPacketForwarded(L2GameServer server, MMOBuffer buf)
	{
		final RandomAccessMMOBuffer rab;
		try
		{
			rab = L2PpeProvider.getPacketPayloadEnumerator().enumeratePacketPayload(server.getProtocol(), buf, server);
		}
		catch (final InvalidPacketOpcodeSchemeException e)
		{
			LOG.error("Invalid server packet", e);
			return;
		}
		catch (final PartialPayloadEnumerationException e)
		{
			final IPacketTemplate template = e.getTemplate();
			if (!template.isDefined())
			{
				LOG.warn("Unknown packet [S] " + template);
				return;
			}
			
			final String message = "[S] " + template;
			if (e.getUnusedBytes() != -1)
				LOG.warn(message + " " + e.getMessage());
			else
				LOG.error(message, e);
			return;
		}
		
		if (rab.getAllFields().isEmpty())
		{
			if (rab.getPacketName() == null)
				return;
			
			for (final PpeEnabledGameScript script : _registry)
			{
				try
				{
					if (script.getHandledPacketNames().contains(rab.getPacketName()))
					{
						final long start = System.nanoTime();
						script.handleServerPacket(server.getTargetClient(), server, rab);
						final long end = System.nanoTime();
						
						RunnableStatsManager.handleStats(script.getClass(), "handleServerPacket(L2GameClient, L2GameServer, RandomAccessMMOBuffer)", end - start, 50);
					}
				}
				catch (final RuntimeException e)
				{
					LOG.error("PPE Game script '" + script.getName() + "' failed handling server packet", e);
				}
			}
			return;
		}
		
		for (final PpeEnabledGameScript script : _registry)
		{
			try
			{
				if (!Collections.disjoint(script.getHandledScriptFieldAliases(), rab.getAllFields()) || script.getHandledPacketNames().contains(rab.getPacketName()))
				{
					final long start = System.nanoTime();
					script.handleServerPacket(server.getTargetClient(), server, rab);
					final long end = System.nanoTime();
					
					RunnableStatsManager.handleStats(script.getClass(), "handleServerPacket(L2GameClient, L2GameServer, RandomAccessMMOBuffer)", end - start, 50);
				}
			}
			catch (final RuntimeException e)
			{
				LOG.error("PPE Game script '" + script.getName() + "' failed handling server packet", e);
			}
		}
	}
	
	@Override
	protected void clientPacketForwarded(L2GameClient sender, L2GameServer recipient, ByteBuffer received, ByteBuffer sent)
	{
		if (received != null)
		{
			super.clientPacketForwarded(sender, recipient, received, sent);
			return;
		}
		
		final WrapperPool wp = ScriptManager.getInstance().getWrapperPool();
		final MMOBuffer buf = wp.get();
		try
		{
			final RandomAccessMMOBuffer rab;
			try
			{
				buf.setByteBuffer(sent);
				rab = L2PpeProvider.getPacketPayloadEnumerator().enumeratePacketPayload(sender.getProtocol(), buf, sender);
			}
			catch (final InvalidPacketOpcodeSchemeException e)
			{
				return;
			}
			catch (final PartialPayloadEnumerationException e)
			{
				final IPacketTemplate template = e.getTemplate();
				if (!template.isDefined())
					return;
				
				final String message = "Injected [C] " + template;
				if (e.getUnusedBytes() != -1)
					LOG.error(message + " " + e.getMessage());
				else
					LOG.error(message, e);
				return;
			}
			
			if (rab.getAllFields().isEmpty())
				return;
			
			for (final PpeEnabledGameScript script : _registryEx)
			{
				try
				{
					if (!Collections.disjoint(script.getHandledScriptFieldAliases(), rab.getAllFields()))
					{
						final long start = System.nanoTime();
						script.handleClientPacket(sender, recipient, rab);
						final long end = System.nanoTime();
						
						RunnableStatsManager.handleStats(script.getClass(), "handleInjectedClientPacket(L2GameClient, L2GameServer, RandomAccessMMOBuffer)", end - start, 50);
					}
				}
				catch (final RuntimeException e)
				{
					LOG.error("PPE Game script '" + script.getName() + "' failed handling injected client packet", e);
				}
			}
		}
		finally
		{
			wp.store(buf);
		}
	}
	
	@Override
	public final String getVersionString()
	{
		return "All/PPE enabled";
	}
	
	@Override
	public String getScriptName()
	{
		return "PPE GameScript Root [multiple scripts]";
	}
	
	@Override
	public String getAuthor()
	{
		return "savormix";
	}
	
	private static final class PriorityComparator implements Comparator<PpeEnabledGameScript>
	{
		PriorityComparator()
		{
		}
		
		@Override
		public int compare(PpeEnabledGameScript o1, PpeEnabledGameScript o2)
		{
			if (o1.getPriority() < o2.getPriority())
				return -1;
			if (o1.getPriority() > o2.getPriority())
				return +1;
			return o1.getName().compareTo(o2.getName());
		}
		
		/**
		 * Returns a singleton instance of this type.
		 * 
		 * @return an instance of this class
		 */
		public static final PriorityComparator getInstance()
		{
			return SingletonHolder.INSTANCE;
		}
		
		private static final class SingletonHolder
		{
			static final PpeGameScriptRegistry.PriorityComparator INSTANCE = new PriorityComparator();
		}
	}
	
	/**
	 * Returns a singleton instance of this type.
	 * 
	 * @return an instance of this class
	 */
	public static final PpeGameScriptRegistry getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static final class SingletonHolder
	{
		static final PpeGameScriptRegistry INSTANCE = new PpeGameScriptRegistry();
	}
}
