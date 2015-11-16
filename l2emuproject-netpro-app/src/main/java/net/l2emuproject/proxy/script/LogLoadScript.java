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

import java.util.Arrays;

import org.apache.commons.lang3.ArrayUtils;

import net.l2emuproject.network.mmocore.MMOBuffer;
import net.l2emuproject.network.protocol.IProtocolVersion;
import net.l2emuproject.proxy.ui.savormix.io.task.HistoricalPacketLog;

/**
 * A script that is notified about packets as they are loaded from a packet log.
 * 
 * @author savormix
 */
public abstract class LogLoadScript
{
	/** Indicates that all packets should be processed by this script, regardless of opcodes. */
	protected static final int[] WILDCARD = {};
	
	private final String _name;
	private final boolean _login;
	private final int[] _handledClient;
	private final int[] _handledServer;
	
	/**
	 * Creates a script to analyze a historical packet log.
	 * 
	 * @param name script name
	 * @param login whether to handle login service logs
	 * @param handledClient client packet 1st opcodes to be handled
	 * @param handledServer server packet 1st opcodes to be handled
	 */
	protected LogLoadScript(String name, boolean login, int[] handledClient, int[] handledServer)
	{
		_name = name;
		_login = login;
		
		_handledClient = handledClient != null ? handledClient : ArrayUtils.EMPTY_INT_ARRAY;
		_handledServer = handledServer != null ? handledServer : ArrayUtils.EMPTY_INT_ARRAY;
		
		Arrays.sort(_handledClient);
		Arrays.sort(_handledServer);
	}
	
	/**
	 * Performs actions based on a loaded packet's content.<BR>
	 * <BR>
	 * This method runs on the same thread that performs log loading. This guarantees that for the
	 * same {@code log}, packets are loaded synchronously. Therefore, expensive operations should be
	 * delegated to a background thread by the script itself.<BR>
	 * <BR>
	 * Moreover, concurrent calls to this method are possible, as logs may be loaded in different
	 * threads.<BR>
	 * <BR>
	 * {@link RuntimeException}s may be thrown by this method without interrupting the packet log
	 * loading sequence.
	 * 
	 * @param buf client packet
	 * @param version protocol version
	 * @param cacheContext cache context
	 * @throws RuntimeException if an unexpected situation happens
	 */
	protected abstract void handleClientPacket(MMOBuffer buf, IProtocolVersion version, HistoricalPacketLog cacheContext) throws RuntimeException;
	
	/**
	 * Performs actions based on a loaded packet's content.<BR>
	 * <BR>
	 * This method runs on the same thread that performs log loading. This guarantees that for the
	 * same {@code log}, packets are loaded synchronously. Therefore, expensive operations should be
	 * delegated to a background thread by the script itself.<BR>
	 * <BR>
	 * Moreover, concurrent calls to this method are possible, as logs may be loaded in different
	 * threads.<BR>
	 * <BR>
	 * {@link RuntimeException}s may be thrown by this method without interrupting the packet log
	 * loading sequence.
	 * 
	 * @param buf client packet
	 * @param version protocol version
	 * @param cacheContext cache context
	 * @throws RuntimeException if an unexpected situation happens
	 */
	protected abstract void handleServerPacket(MMOBuffer buf, IProtocolVersion version, HistoricalPacketLog cacheContext) throws RuntimeException;
	
	/**
	 * Returns whether a specific 1-byte opcode is handled by this script.
	 * 
	 * @param client whether this is a client packet
	 * @param opcode packet's 1st opcode
	 * @return whether to call the respective method for this packet
	 */
	protected boolean isHandled(boolean client, int opcode)
	{
		final int[] tmp = client ? _handledClient : _handledServer;
		return tmp == WILDCARD || Arrays.binarySearch(tmp, opcode) >= 0;
	}
	
	/**
	 * Returns whether this script handles login service logs.
	 * 
	 * @return whether login logs are handled
	 */
	public final boolean isLogin()
	{
		return _login;
	}
	
	@Override
	public final String toString()
	{
		return _name;
	}
}
