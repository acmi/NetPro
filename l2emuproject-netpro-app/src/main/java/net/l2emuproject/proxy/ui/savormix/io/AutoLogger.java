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
package net.l2emuproject.proxy.ui.savormix.io;

import java.awt.Window;
import java.io.File;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Set;

import net.l2emuproject.network.protocol.IProtocolVersion;
import net.l2emuproject.proxy.network.Proxy;
import net.l2emuproject.proxy.network.ServiceType;
import net.l2emuproject.proxy.network.listener.ConnectionListener;
import net.l2emuproject.proxy.network.listener.PacketListener;
import net.l2emuproject.proxy.ui.ReceivedPacket;
import net.l2emuproject.proxy.ui.savormix.component.ConnectionPane;
import net.l2emuproject.proxy.ui.savormix.io.base.IOConstants;
import net.l2emuproject.proxy.ui.savormix.loader.Loader;

/**
 * Manages automatic packet logging.
 * 
 * @author savormix
 */
public final class AutoLogger implements IOConstants, ConnectionListener, PacketListener
{
	// private static final L2Logger _log = L2Logger.getLogger(AutoLogger.class);
	
	// version 1 contains obfuscated CPs (can be converted, see version history for the conversion method)
	// version 2 contains wrong non-obfuscated D0 & 11 ops (not supported)
	// version 3 contains wrong non-obfuscated 74 2nd ops (not supported)
	// version 4 does not count how many packets have been written
	// version 5 does not track which packets have been written
	// version 6 does not provide support for optional packet flags
	
	private final PacketLogThread _ioThread;
	
	AutoLogger()
	{
		(_ioThread = new PacketLogThread()).start();
	}
	
	@Override
	public void onClientConnection(Proxy client)
	{
		getIoThread().fireConnection(client);
	}
	
	@Override
	public void onDisconnection(Proxy client, Proxy server)
	{
		getIoThread().fireDisconnection(client);
	}
	
	@Override
	public void onProtocolVersion(Proxy affected, IProtocolVersion version) throws RuntimeException
	{
		// perhaps add action to write to header now?
		// but perhaps it is just not worth it
	}
	
	@Override
	public void onClientPacket(Proxy sender, Proxy recipient, ByteBuffer packet, long time)
	{
		final byte[] body = new byte[packet.clear().limit()];
		packet.get(body);
		getIoThread().firePacket(sender, new ReceivedPacket(ServiceType.valueOf(sender.getProtocol()), sender.getType(), body, time), getPacketFlags(sender));
	}
	
	@Override
	public void onServerPacket(Proxy sender, Proxy recipient, ByteBuffer packet, long time)
	{
		final byte[] body = new byte[packet.clear().limit()];
		packet.get(body);
		getIoThread().firePacket(recipient, new ReceivedPacket(ServiceType.valueOf(sender.getProtocol()), sender.getType(), body, time), getPacketFlags(recipient));
	}
	
	private static final Set<LoggedPacketFlag> getPacketFlags(Proxy client)
	{
		final ConnectionPane cp = Loader.getActiveUIPane();
		if (cp == null)
			return Collections.emptySet();
		
		return cp.isCaptureDisabledFor(client) ? Collections.singleton(LoggedPacketFlag.HIDDEN) : Collections.emptySet();
	}
	
	@Override
	public void onServerConnection(Proxy server)
	{
		// ignore
	}
	
	/**
	 * Loads given packet logs and adds them to active connection pane.
	 * 
	 * @param owner main window
	 * @param defaultLoginProtocol a protocol version to be automatically selected for any legacy login packet logs
	 * @param defaultGameProtocol a protocol version to be automatically selected for any legacy game packet logs
	 * @param files packet logs
	 */
	public static void loadConnections(final Window owner, IProtocolVersion defaultLoginProtocol, IProtocolVersion defaultGameProtocol, final File... files)
	{
		final Path[] paths = new Path[files.length];
		for (int i = 0; i < paths.length; i++)
			paths[i] = files[i].toPath();
		
		//new LogIdentifyTask(owner, defaultLoginProtocol, defaultGameProtocol).execute(paths);
	}
	
	private PacketLogThread getIoThread()
	{
		return _ioThread;
	}
	
	/**
	 * Returns a singleton object.
	 * 
	 * @return an instance of this class
	 */
	public static AutoLogger getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static final class SingletonHolder
	{
		static final AutoLogger INSTANCE = new AutoLogger();
	}
}
