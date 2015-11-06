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

import java.io.IOException;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.ArrayUtils;

import net.l2emuproject.network.protocol.ILoginProtocolVersion;
import net.l2emuproject.network.protocol.IProtocolVersion;
import net.l2emuproject.network.protocol.ProtocolVersionManager;
import net.l2emuproject.proxy.network.EndpointType;
import net.l2emuproject.proxy.network.ServiceType;
import net.l2emuproject.proxy.network.meta.IPacketTemplate;
import net.l2emuproject.proxy.network.meta.container.OpcodeOwnerSet;
import net.l2emuproject.proxy.network.meta.container.OpcodeOwnerSet.OpcodeOwner;
import net.l2emuproject.proxy.ui.ReceivedPacket;
import net.l2emuproject.proxy.ui.savormix.io.base.IOConstants;
import net.l2emuproject.proxy.ui.savormix.io.base.NewIOHelper;
import net.l2emuproject.proxy.ui.savormix.io.exception.InvalidFileException;
import net.l2emuproject.proxy.ui.savormix.io.exception.UnsupportedFileException;

/**
 * Enables persistent user-specific packet display configuration storage.<BR>
 * <BR>
 * This class is a part of a reference GUI provided for education purposes only. These classes
 * should help to understand how to interact with the underlying proxy core.<BR>
 * <BR>
 * Creating an own GUI is the preferred way to start using this application.
 * 
 * @author savormix
 */
public final class PacketDisplayConfigManager implements IOConstants
{
	// private static final L2Logger _log = L2Logger.getLogger(PacketDisplayConfigManager.class);
	
	PacketDisplayConfigManager()
	{
		// singleton
	}
	
	/**
	 * Saves a packet display configuration to file.
	 * 
	 * @param file path to file
	 * @param protocol associated network protocol version
	 * @param displayedClient client packets to be displayed
	 * @param displayedServer server packets to be displayed
	 * @param login service type
	 * @throws IOException if the configuration cannot be saved
	 */
	public void save(Path file, IProtocolVersion protocol, Set<IPacketTemplate> displayedClient, Set<IPacketTemplate> displayedServer, boolean login) throws IOException
	{
		try (final SeekableByteChannel channel = Files.newByteChannel(file, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
				final NewIOHelper ioh = new NewIOHelper(channel))
		{
			ioh.writeLong(DISPLAY_CONFIG_MAGIC).writeByte(DISPLAY_CONFIG_VERSION);
			ioh.writeBoolean(ServiceType.valueOf(protocol).isLogin()).writeInt(protocol.getVersion());
			ioh.writeByte(2);
			{
				save(ioh, protocol, displayedClient, EndpointType.CLIENT);
				save(ioh, protocol, displayedServer, EndpointType.SERVER);
			}
			
			ioh.flush();
		}
	}
	
	@SuppressWarnings("static-method")
	private void save(NewIOHelper ioh, IProtocolVersion protocol, Set<IPacketTemplate> displayed, EndpointType type) throws IOException
	{
		final Set<IPacketTemplate> unselected = VersionnedPacketTable.getInstance().getKnownTemplates(protocol, type).collect(Collectors.toCollection(HashSet::new));
		unselected.removeAll(displayed);
		if (!displayed.contains(IPacketTemplate.ANY_DYNAMIC_PACKET))
			unselected.add(IPacketTemplate.ANY_DYNAMIC_PACKET);
		
		final int proxyType = ReceivedPacket.getLegacyProxyTypeOrdinal(ServiceType.valueOf(protocol), type);
		ioh.writeByte(proxyType);
		final long sectionSizePos = ioh.getPositionInChannel(true);
		ioh.writeInt(-1); // size of section
		ioh.writeInt(unselected.size());
		for (final IPacketTemplate pt : unselected)
		{
			final byte[] prefix = pt.getPrefix();
			if (ArrayUtils.isEmpty(prefix))
				ioh.writeChar(-1).writeByte(0);
			else
				ioh.writeChar(prefix[0] & 0xFF).writeByte(prefix.length - 1).write(prefix, 1, prefix.length - 1);
		}
		final long endPos = ioh.getPositionInChannel(true);
		{
			ioh.flush();
			ioh.setPositionInChannel(sectionSizePos);
			ioh.writeInt((int)(endPos - sectionSizePos - 4));
			ioh.flush();
		}
		ioh.setPositionInChannel(endPos);
	}
	
	/**
	 * Loads a packet display configuration from file.
	 * 
	 * @param file path to file
	 * @param type service type
	 * @param clientPackets all client packets
	 * @param serverPackets all server packets
	 * @throws IOException if the configuration cannot be loaded
	 * @throws InvalidFileException if the specified file is not a packet display configuration
	 * @throws UnsupportedFileException if the specified file is of an unsupported format
	 */
	public void load(Path file, ServiceType type, OpcodeOwnerSet clientPackets, OpcodeOwnerSet serverPackets) throws IOException, InvalidFileException, UnsupportedFileException
	{
		if (file == null)
			return;
		
		try (final SeekableByteChannel channel = Files.newByteChannel(file, StandardOpenOption.READ); final NewIOHelper ioh = new NewIOHelper(channel))
		{
			if (ioh.readLong() != DISPLAY_CONFIG_MAGIC)
				throw new InvalidFileException();
			final int version = ioh.readByte() & 0xFF;
			if (version > DISPLAY_CONFIG_VERSION)
				throw new UnsupportedFileException("Configuration version " + version + " is not supported by this application.");
			
			final IProtocolVersion protocol;
			if (version >= 3)
			{
				final boolean login = ioh.readBoolean();
				final int pv = ioh.readInt();
				
				final ProtocolVersionManager pvm = ProtocolVersionManager.getInstance();
				if (login)
					protocol = pvm.getLoginProtocol(pv);
				else
					protocol = pvm.getGameProtocol(pv);
			}
			else
				protocol = null;
			
			final int blocks = (version >= 2) ? ioh.readByte() & 0xFF : 4;
			
			for (int i = 0; i < blocks; ++i)
			{
				final int blockType = version >= 2 ? ioh.readByte() & 0xFF : i;
				final int size = (version >= 2) ? ioh.readInt() : -1;
				
				final EndpointType endType = EndpointType.valueOf((blockType & 1) == 0);
				final ServiceType svcType = ServiceType.valueOf((blockType / 2) == 0);
				
				final OpcodeOwnerSet displayed;
				if (protocol != null && svcType.isLogin() != (protocol instanceof ILoginProtocolVersion))
					displayed = null;
				else
					displayed = endType.isClient() ? clientPackets : serverPackets;
				
				load(ioh, displayed, size);
			}
		}
	}
	
	@SuppressWarnings("static-method")
	private void load(NewIOHelper ioh, OpcodeOwnerSet displayed, int size) throws IOException
	{
		if (displayed == null && size != -1)
		{
			ioh.skip(size, false);
			return;
		}
		
		final int count = ioh.readInt();
		for (int i = 0; i < count; ++i)
		{
			final int op = (short)ioh.readChar(); // retain -1
			final int len = ioh.readByte() & 0xFF;
			if (op == -1)
			{
				// len will be 0, so nothing else to read
				if (displayed != null)
					displayed.remove(IPacketTemplate.ANY_DYNAMIC_PACKET);
				continue;
			}
			
			final byte[] prefix = new byte[1 + len];
			prefix[0] = (byte)op;
			ioh.read(prefix, 1, prefix.length - 1);
			
			if (displayed != null)
				displayed.remove(new OpcodeWrapper(prefix));
		}
	}
	
	private static class OpcodeWrapper implements OpcodeOwner
	{
		private final byte[] _prefix;
		
		OpcodeWrapper(byte[] prefix)
		{
			_prefix = prefix;
		}
		
		@Override
		public byte[] getPrefix()
		{
			return _prefix;
		}
		
		@Override
		public int hashCode()
		{
			return OpcodeOwnerSet.COMPARATOR.hashCodeOf(this);
		}
		
		@Override
		public boolean equals(Object obj)
		{
			if (!(obj instanceof OpcodeOwner))
				return false;
			
			return OpcodeOwnerSet.COMPARATOR.areEqual(this, (OpcodeOwner)obj);
		}
		
		@Override
		public int compareTo(OpcodeOwner other)
		{
			return OpcodeOwnerSet.COMPARATOR.compare(this, other);
		}
	}
	
	/**
	 * Returns a singleton object.
	 * 
	 * @return an instance of this class
	 */
	public static PacketDisplayConfigManager getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static final class SingletonHolder
	{
		static final PacketDisplayConfigManager INSTANCE = new PacketDisplayConfigManager();
	}
}
