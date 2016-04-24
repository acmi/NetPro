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
package net.l2emuproject.proxy.network.meta.container;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import net.l2emuproject.network.protocol.ClientProtocolVersion;
import net.l2emuproject.proxy.network.meta.IPacketTemplate;
import net.l2emuproject.proxy.network.meta.PacketTemplate;

/**
 * @author _dev_
 */
public class PacketPrefixResolverTest
{
	private static final byte[] OP_DIE = { 0x00 }, OP_EX_EMUI = { (byte)0xFE, 0x02, 0x00 }, OP_EX_EMGMT = { (byte)0xFE, 0x07, 0x00 };
	private static final byte[] OP_REVIVE = { 0x01 };
	
	private static final IPacketTemplate DIE = new PacketTemplate(OP_DIE, "Die", Collections.emptyList(), ClientProtocolVersion.THE_ANCESTOR_PROTOCOL_VERSION);
	private static final IPacketTemplate EX_EMUI = new PacketTemplate(OP_EX_EMUI, "ExEventMatchUserInfo", Collections.emptyList(), ClientProtocolVersion.THE_ANCESTOR_PROTOCOL_VERSION);
	private static final IPacketTemplate EX_EMGMT = new PacketTemplate(OP_EX_EMGMT, "ExEventMatchGMTest", Collections.emptyList(), ClientProtocolVersion.THE_ANCESTOR_PROTOCOL_VERSION);
	private static final IPacketTemplate REVIVE = new PacketTemplate(OP_REVIVE);
	
	private static final IPacketTemplate[] SERVER_PACKETS = { DIE, EX_EMUI, EX_EMGMT };
	private static final Collection<IPacketTemplate> SERVER_PACKET_COLLECTION = Arrays.asList(SERVER_PACKETS);
	
	/**
	 * Test method for {@link net.l2emuproject.proxy.network.meta.container.PacketPrefixResolver#PacketPrefixResolver(java.util.Collection)}.
	 */
	@Test
	public void testPacketPrefixResolver()
	{
		final PacketPrefixResolver resolver = new PacketPrefixResolver(SERVER_PACKET_COLLECTION);
		assertThat(resolver.getAllTemplates(), is(equalTo(new HashSet<>(SERVER_PACKET_COLLECTION))));
	}
	
	/**
	 * Test method for {@link net.l2emuproject.proxy.network.meta.container.PacketPrefixResolver#resolve(byte[], int, int)}.
	 */
	@Test
	public void testResolveByteArrayIntInt()
	{
		final PacketPrefixResolver resolver = new PacketPrefixResolver(SERVER_PACKET_COLLECTION);
		assertThat(resolver.resolve(OP_DIE), is(equalTo(DIE)));
		assertThat(resolver.resolve(OP_EX_EMUI), is(equalTo(EX_EMUI)));
		assertThat(resolver.resolve(OP_EX_EMGMT), is(equalTo(EX_EMGMT)));
	}
	
	/**
	 * Test method for {@link net.l2emuproject.proxy.network.meta.container.PacketPrefixResolver#resolve(java.nio.ByteBuffer)}.
	 */
	@Test
	public void testResolveByteBuffer()
	{
		final PacketPrefixResolver resolver = new PacketPrefixResolver(SERVER_PACKET_COLLECTION);
		assertThat(resolver.resolve(ByteBuffer.wrap(OP_DIE).asReadOnlyBuffer()), is(equalTo(DIE)));
		assertThat(resolver.resolve(ByteBuffer.wrap(OP_EX_EMUI).asReadOnlyBuffer()), is(equalTo(EX_EMUI)));
		assertThat(resolver.resolve(ByteBuffer.wrap(OP_EX_EMGMT).asReadOnlyBuffer()), is(equalTo(EX_EMGMT)));
	}
	
	/**
	 * Test method for corner case of {@link net.l2emuproject.proxy.network.meta.container.PacketPrefixResolver#resolve(byte[], int, int)}.
	 */
	@Test
	public void testImplicitRegistration()
	{
		final PacketPrefixResolver resolver = new PacketPrefixResolver(SERVER_PACKET_COLLECTION);
		{
			final IPacketTemplate template = resolver.resolve(OP_REVIVE);
			assertThat(template.isDefined(), is(Boolean.FALSE));
			assertThat(template.getPrefix(), is(equalTo(OP_REVIVE)));
			assertThat(template.getDefinitionVersion(), is(nullValue()));
		}
		
		final Set<IPacketTemplate> expected = new HashSet<>(SERVER_PACKET_COLLECTION);
		expected.add(REVIVE);
		assertThat(resolver.getAllTemplates(), is(equalTo(expected)));
		
		assertThat(resolver.resolve(Arrays.copyOf(OP_EX_EMUI, 2)), is(nullValue()));
	}
	
	/**
	 * Test method for corner case of {@link net.l2emuproject.proxy.network.meta.container.PacketPrefixResolver#resolve(java.nio.ByteBuffer)}.
	 */
	@Test
	public void testImplicitRegistrationWithByteBuffer()
	{
		final PacketPrefixResolver resolver = new PacketPrefixResolver(SERVER_PACKET_COLLECTION);
		{
			final IPacketTemplate template = resolver.resolve(ByteBuffer.wrap(OP_REVIVE).asReadOnlyBuffer());
			assertThat(template.isDefined(), is(Boolean.FALSE));
			assertThat(template.getPrefix(), is(equalTo(OP_REVIVE)));
			assertThat(template.getDefinitionVersion(), is(nullValue()));
		}
		
		final Set<IPacketTemplate> expected = new HashSet<>(SERVER_PACKET_COLLECTION);
		expected.add(REVIVE);
		assertThat(resolver.getAllTemplates(), is(equalTo(expected)));
		
		assertThat(resolver.resolve(ByteBuffer.wrap(Arrays.copyOf(OP_EX_EMUI, 2)).asReadOnlyBuffer()), is(nullValue()));
	}
	
	/**
	 * Test method for invalid argument(s) being passed to {@link net.l2emuproject.proxy.network.meta.container.PacketPrefixResolver#PacketPrefixResolver(java.util.Collection)}.
	 */
	@Test
	public void testInitializationFail()
	{
		{
			final Collection<IPacketTemplate> invalid = new ArrayList<>(SERVER_PACKET_COLLECTION);
			invalid.add(new PacketTemplate(Arrays.copyOf(OP_EX_EMGMT, 4), "PacketWithOverlappingPrefix", Collections.emptyList(), ClientProtocolVersion.THE_ANCESTOR_PROTOCOL_VERSION));
			final PacketPrefixResolver resolver = new PacketPrefixResolver(invalid);
			assertThat(resolver.getAllTemplates(), is(equalTo(new HashSet<>(SERVER_PACKET_COLLECTION))));
		}
		{
			final List<IPacketTemplate> invalid = new ArrayList<>(SERVER_PACKET_COLLECTION);
			final IPacketTemplate retained2 = new PacketTemplate(Arrays.copyOf(OP_EX_EMGMT, 4), "RetainedPacketWithOverlappingPrefix_2", Collections.emptyList(),
					ClientProtocolVersion.THE_ANCESTOR_PROTOCOL_VERSION);
			invalid.add(0, retained2);
			final byte[] op = Arrays.copyOf(OP_EX_EMGMT, 4);
			op[3] = 1;
			final IPacketTemplate retained1 = new PacketTemplate(op, "RetainedPacketWithOverlappingPrefix_1", Collections.emptyList(), ClientProtocolVersion.THE_ANCESTOR_PROTOCOL_VERSION);
			invalid.add(0, retained1);
			final PacketPrefixResolver resolver = new PacketPrefixResolver(invalid);
			final Set<IPacketTemplate> expected = new HashSet<>(SERVER_PACKET_COLLECTION);
			expected.remove(EX_EMGMT);
			expected.add(retained1);
			expected.add(retained2);
			assertThat(resolver.getAllTemplates(), is(equalTo(expected)));
		}
	}
}
