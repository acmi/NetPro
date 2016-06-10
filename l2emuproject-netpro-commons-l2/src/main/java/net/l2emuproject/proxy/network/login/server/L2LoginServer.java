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
package net.l2emuproject.proxy.network.login.server;

import static net.l2emuproject.network.protocol.LoginProtocolVersion.MODERN;
import static net.l2emuproject.network.protocol.LoginProtocolVersion.PRELUDE_BETA;
import static net.l2emuproject.network.security.LoginCipher.READ_ONLY_MODERN_KEY;

import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SocketChannel;

import net.l2emuproject.network.protocol.ILoginProtocolVersion;
import net.l2emuproject.network.security.ICipher;
import net.l2emuproject.network.security.LoginCipher;
import net.l2emuproject.proxy.network.AbstractL2ServerProxy;
import net.l2emuproject.proxy.network.login.client.L2LoginClient;
import net.l2emuproject.proxy.network.login.server.packets.SetEncryption;

/**
 * Internally represents a login server connected to a L2 client.
 * 
 * @author savormix
 */
public final class L2LoginServer extends AbstractL2ServerProxy
{
	private ICipher _cipher;
	private boolean _firstTime, _fail2j;
	
	/**
	 * Creates an internal object representing a login server connection.
	 * 
	 * @param mmoController connection manager
	 * @param socketChannel connection
	 * @param target client that originally initiated this connection
	 * @throws ClosedChannelException if the given channel was closed during operations
	 */
	protected L2LoginServer(L2LoginServerConnections mmoController, SocketChannel socketChannel, L2LoginClient target) throws ClosedChannelException
	{
		super(mmoController, socketChannel, target);
		
		_cipher = null;
		_firstTime = true;
		_fail2j = false;
	}
	
	@Override
	protected void onDisconnectionImpl()
	{
		// TODO Auto-generated method stub
		
	}
	
	@Override
	protected void onForcedDisconnection()
	{
		// TODO Auto-generated method stub
		
	}
	
	@Override
	protected boolean decipher(ByteBuffer buf)
	{
		final boolean first = isFirstTime();
		if (first)
		{
			// special case: legacy protocols had first packet not enciphered
			final int pos = buf.position();
			if (buf.get(pos) == SetEncryption.OPCODE && buf.remaining() >= 9)
			{
				final int protocol = buf.getInt(pos + 5);
				if (protocol == 0 || protocol == PRELUDE_BETA.getVersion())
					return true;
			}
		}
		
		final ICipher cipher = first ? new LoginCipher(READ_ONLY_MODERN_KEY) : getCipher();
		cipher.decipher(buf);
		if (first)
			LoginCipher.complementDecipherInitialPacket(buf);
		else if (!LoginCipher.testChecksum(buf, 8)) // server packet checksum scheme
		{
			LOG.info("Malformed server packet received from " + getHostAddress());
			_fail2j = true;
			// do nothing, since hAuthD & generic l2j login servers send such packets
			// return false;
		}
		return true;
	}
	
	@Override
	protected void encipher(ByteBuffer buf)
	{
		// at this point, we could choose two possible paths:
		// 1. This is a bare, self-built packet
		// 2. This is a complete packet, as received from the client
		
		// as you can understand, we simply assume the latter
		//size += (8 - (size & 7)) & 7; // padding
		
		LoginCipher.injectChecksum(buf, getProtocol().isOlderThan(MODERN) ? 8 : 16); // [legacy] client packet checksum scheme
		getCipher().encipher(buf);
	}
	
	@Override
	protected String getUID()
	{
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	protected boolean isAuthed()
	{
		return getCipher() != null;
	}
	
	@Override
	public ILoginProtocolVersion getProtocol()
	{
		return (ILoginProtocolVersion)getTarget().getProtocol();
	}
	
	/**
	 * Initializes the cipher with the Blowfish key received from the login server.
	 * 
	 * @param blowfishKey Blowfish Key
	 */
	public void initCipher(byte[] blowfishKey)
	{
		_cipher = new LoginCipher(blowfishKey);
	}
	
	/**
	 * Returns whether it is safe to precompute packet checksums ahead of time (e.g. for transmitted packet visualization purposes).
	 * 
	 * @return whether a server represented by this endpoint has sent at least one damaged packet
	 */
	public boolean isFail2j()
	{
		return _fail2j;
	}
	
	private ICipher getCipher()
	{
		return _cipher;
	}
	
	private boolean isFirstTime()
	{
		boolean ft = _firstTime;
		_firstTime = false;
		return ft;
	}
	
	@Override
	public boolean ___supportsAheadOfTimeIntervention()
	{
		return !_fail2j;
	}
}
