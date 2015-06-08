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
package net.l2emuproject.proxy.script.packets;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import eu.revengineer.simplejse.type.UnloadableScript;

import net.l2emuproject.util.logging.L2Logger;

/**
 * @author _dev_
 */
public abstract class PacketWriterScript implements PacketWriter, UnloadableScript
{
	private static final L2Logger LOG = L2Logger.getLogger(PacketWriterScript.class);
	
	@Override
	public void onLoad() throws RuntimeException
	{
		final PacketWriterRegistry registry = PacketWriterRegistry.getInstance();
		for (Class<?> c = getClass(); c != null; c = c.getSuperclass())
		{
			for (final Field f : c.getDeclaredFields())
			{
				final PacketIdentifier pi = f.getAnnotation(PacketIdentifier.class);
				if (pi == null || !Modifier.isFinal(f.getModifiers()))
					continue;
				
				final boolean access = f.isAccessible();
				try
				{
					if (!access)
						f.setAccessible(true);
					registry.registerWriter(pi.value(), String.valueOf(f.get(this)), this);
				}
				catch (Exception e)
				{
					LOG.error("Invalid packet identifier: " + c.getSimpleName() + "#" + f.getName(), e);
				}
				finally
				{
					if (!access)
						f.setAccessible(false);
				}
			}
		}
	}
	
	@Override
	public void onUnload() throws RuntimeException
	{
		final PacketWriterRegistry registry = PacketWriterRegistry.getInstance();
		for (Class<?> c = getClass(); c != null; c = c.getSuperclass())
		{
			for (final Field f : c.getDeclaredFields())
			{
				final PacketIdentifier pi = f.getAnnotation(PacketIdentifier.class);
				if (pi == null || !Modifier.isFinal(f.getModifiers()))
					continue;
				
				final boolean access = f.isAccessible();
				try
				{
					if (!access)
						f.setAccessible(true);
					registry.removeWriter(pi.value(), String.valueOf(f.get(this)), this);
				}
				catch (Exception e)
				{
					LOG.error("Invalid packet identifier: " + c.getSimpleName() + "#" + f.getName(), e);
				}
				finally
				{
					if (!access)
						f.setAccessible(false);
				}
			}
		}
	}
}
