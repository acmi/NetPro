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
package net.l2emuproject.proxy.io.definitions;

import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import net.l2emuproject.network.protocol.IGameProtocolVersion;
import net.l2emuproject.network.protocol.ILoginProtocolVersion;
import net.l2emuproject.network.protocol.IProtocolVersion;
import net.l2emuproject.network.protocol.UnknownGameProtocolVersion;
import net.l2emuproject.network.protocol.UnknownLoginProtocolVersion;
import net.l2emuproject.proxy.network.EndpointType;
import net.l2emuproject.proxy.network.ServiceType;
import net.l2emuproject.proxy.network.meta.IPacketTemplate;
import net.l2emuproject.proxy.network.meta.container.VersionnedPacketTemplateContainer;
import net.l2emuproject.util.logging.L2Logger;

/**
 * Allows an easy way to swap known protocol versions on reload.
 * 
 * @author savormix
 */
class ProtocolDefinitions
{
	private final Map<Integer, ? extends ILoginProtocolVersion> _loginProtocols;
	private final Map<Integer, ? extends IGameProtocolVersion> _gameProtocols;
	
	private final VersionnedPacketTemplateContainer<? extends ILoginProtocolVersion> _loginPackets;
	private final VersionnedPacketTemplateContainer<? extends IGameProtocolVersion> _gamePackets;
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	<L extends ILoginProtocolVersion, G extends IGameProtocolVersion> ProtocolDefinitions(Collection<L> loginProtocols, Collection<G> gameProtocols, VersionnedPacketTemplateContainer<L> loginPackets,
			VersionnedPacketTemplateContainer<G> gamePackets)
	{
		final ProtocolCollisionResolver merger = new ProtocolCollisionResolver();
		_loginProtocols = loginProtocols.stream().collect(Collectors.toMap(IProtocolVersion::getVersion, Function.<L> identity(), /*(u, v) -> v*/merger, LinkedHashMap::new));
		_gameProtocols = gameProtocols.stream().collect(Collectors.toMap(IProtocolVersion::getVersion, Function.<G> identity(), /*(u, v) -> v*/merger, LinkedHashMap::new));
		
		_loginPackets = loginPackets;
		_gamePackets = gamePackets;
	}
	
	ILoginProtocolVersion getLoginProtocol(int version)
	{
		if (version == -1)
			return _loginPackets.getFallback();
		
		ILoginProtocolVersion ver = _loginProtocols.get(version);
		if (ver == null)
			ver = new UnknownLoginProtocolVersion(version, _loginPackets.getFallback());
		
		return ver;
	}
	
	IGameProtocolVersion getGameProtocol(int version)
	{
		if (version == -1)
			return _gamePackets.getFallback();
		
		IGameProtocolVersion ver = _gameProtocols.get(version);
		if (ver == null)
			ver = new UnknownGameProtocolVersion(version, _gamePackets.getFallback());
		
		return ver;
	}
	
	Collection<? extends IProtocolVersion> getKnownProtocols(ServiceType service)
	{
		return service.isLogin() ? _loginProtocols.values() : _gameProtocols.values();
	}
	
	@SuppressWarnings("unchecked")
	private VersionnedPacketTemplateContainer<IProtocolVersion> table(IProtocolVersion protocol)
	{
		if (protocol instanceof IGameProtocolVersion)
			return (VersionnedPacketTemplateContainer<IProtocolVersion>)(VersionnedPacketTemplateContainer<?>)_gamePackets;
		return (VersionnedPacketTemplateContainer<IProtocolVersion>)(VersionnedPacketTemplateContainer<?>)_loginPackets;
	}
	
	public IPacketTemplate getTemplate(IProtocolVersion protocol, EndpointType endpoint, byte[] packet)
	{
		return table(protocol).getTemplate(protocol, endpoint, packet, 0, packet.length);
	}
	
	public IPacketTemplate getTemplate(IProtocolVersion protocol, EndpointType endpoint, byte[] packet, int packetOffset, int packetLength)
	{
		return table(protocol).getTemplate(protocol, endpoint, packet, packetOffset, packetLength);
	}
	
	public IPacketTemplate getTemplate(IProtocolVersion protocol, EndpointType endpoint, ByteBuffer packet)
	{
		return table(protocol).getTemplate(protocol, endpoint, packet);
	}
	
	public Stream<IPacketTemplate> getTemplates(IProtocolVersion protocol, EndpointType endpoint)
	{
		return table(protocol).getTemplates(protocol, endpoint);
	}
	
	private static final class ProtocolCollisionResolver<T extends IProtocolVersion> implements BinaryOperator<T>
	{
		private static final L2Logger LOG = L2Logger.getLogger(ProtocolCollisionResolver.class);
		
		ProtocolCollisionResolver()
		{
		}
		
		@Override
		public T apply(T t, T u)
		{
			LOG.warn("Collision! " + t + " -> " + u);
			return u;
		}
	}
}
