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
package examples.fastlogin;

import static java.nio.charset.StandardCharsets.US_ASCII;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.file.NoSuchFileException;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.spec.RSAKeyGenParameterSpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang3.ArrayUtils;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import util.packet.CommonPacketSender;

import eu.revengineer.simplejse.HasScriptDependencies;
import eu.revengineer.simplejse.type.UnloadableScript;

import net.l2emuproject.network.ClientProtocolVersion;
import net.l2emuproject.network.IGameProtocolVersion;
import net.l2emuproject.network.mmocore.MMOBuffer;
import net.l2emuproject.network.security.LoginCipher;
import net.l2emuproject.network.security.ScrambledRSAKeyPair;
import net.l2emuproject.proxy.network.IPv4AddressPrefix;
import net.l2emuproject.proxy.network.Packet;
import net.l2emuproject.proxy.network.game.client.L2GameClient;
import net.l2emuproject.proxy.network.game.server.L2GameServer;
import net.l2emuproject.proxy.network.login.client.L2LoginClient;
import net.l2emuproject.proxy.network.login.server.L2LoginServer;
import net.l2emuproject.proxy.network.meta.RandomAccessMMOBuffer;
import net.l2emuproject.proxy.network.packets.ProxyRepeatedPacket;
import net.l2emuproject.proxy.script.ScriptFieldAlias;
import net.l2emuproject.proxy.script.game.GameScript;
import net.l2emuproject.proxy.script.game.PpeEnabledGameScript;
import net.l2emuproject.proxy.script.login.LoginScript;
import net.l2emuproject.proxy.ui.savormix.io.base.IOConstants;
import net.l2emuproject.util.L2XMLUtils;
import net.l2emuproject.util.concurrent.L2ThreadPool;
import net.l2emuproject.util.logging.L2Logger;

/**
 * A script that allows users to input fake credentials while logging in. Obscure e-mail addresses and secure passwords may be used without
 * sacrificing the ability to 9+ box different account sets in seconds.<BR>
 * <BR>
 * In other words, you can input 'a' as user and 'b' as password, and NetPro will supply 'username@email.xxx' & 'real_long_password' to the login server.<BR>
 * Configuration file is located in {@code [user.home]/l2emu-unique-netpro/fast_login_credentials.xml}.<BR>
 * <BR>
 * You may also configure your character selection PINs and/or security card codes for automatic input (if you play on a server where such are used).<BR>
 * There is no XSD/schema at this time, so look at {@code loadCredentials()} on how to write the configuration XML.<BR>
 * <BR>
 * This script is not subject to fragile binary offset issues (neither the way AuthD or hAuthD are, nor the way pre-C5u1 l2j login servers were).
 * 
 * @author _dev_
 */
@HasScriptDependencies("util.packet.CommonPacketSender")
public final class FastLogin extends LoginScript implements IOConstants, UnloadableScript
{
	private static final L2Logger LOG = L2Logger.getLogger(FastLogin.class);
	
	private static final int OP_AQ_LOGIN = 0x00, OP_REQ_SC_CHECK = 0x06;
	private static final int OP_SET_ENC = 0x00, OP_SC_CHECK_REQ = 0x0A;
	
	// Client: handle user data, handle SC key input (if card is not configured)
	// Server: handle RSA key, [optionally] handle SC
	private static final int[] OPCODES_CLIENT = { OP_AQ_LOGIN, OP_REQ_SC_CHECK }, OPCODES_SERVER = { OP_SET_ENC, OP_SC_CHECK_REQ };
	static final int[] STEALTH_OPCODES = { 0x2B };
	
	// FYI: ID max length: 14, e-mail max length: 50, password max length: 16
	private static final int BLOCK_SIZE_PLAIN_USER = 14, BLOCK_SIZE_PLAIN_PASSWORD = 16;
	private static final int BLOCK_SIZE_RSA = 128;
	private static final int SUBBLOCK_SIZE_MIXED = 16;
	private static final int BLOCK_OFFSET_SC_KEY = 0x05;
	
	private final Set<IPv4AddressPrefix> _servedIPs;
	private final Map<String, Credentials> _fakeUsers;
	private final Map<String, String> _accountPINs;
	
	private final ScrambledRSAKeyPair _interceptionKeyPair;
	
	private final Map<L2LoginClient, LoginAttempt> _rsaKeys;
	
	// helper classes
	private final AccountDataProvider _inputToRealAccountNameTranslator;
	private final HideFastLogin _stealthPlugin;
	private final FastPIN _characterPinPlugin;
	
	/** Constructs this script. */
	public FastLogin()
	{
		super(OPCODES_CLIENT, OPCODES_SERVER);
		
		_servedIPs = new HashSet<>();
		_fakeUsers = new HashMap<>();
		_accountPINs = new HashMap<>();
		loadCredentials();
		
		KeyPair kp = null;
		try
		{
			final KeyPairGenerator rsa = KeyPairGenerator.getInstance("RSA");
			rsa.initialize(new RSAKeyGenParameterSpec(1024, RSAKeyGenParameterSpec.F4));
			
			kp = rsa.generateKeyPair();
		}
		catch (GeneralSecurityException e)
		{
			LOG.error("Fast login will not be available.", e);
		}
		
		_interceptionKeyPair = kp != null ? new ScrambledRSAKeyPair(kp) : null;
		
		_rsaKeys = new ConcurrentHashMap<>();
		
		_inputToRealAccountNameTranslator = new AccountDataProvider();
		_stealthPlugin = new HideFastLogin();
		_characterPinPlugin = new FastPIN();
		
		// attempt to minimize impact when having to modify the first packet
		try
		{
			Cipher.getInstance("RSA/ECB/nopadding");
			KeyFactory.getInstance("RSA");
		}
		catch (NoSuchAlgorithmException | NoSuchPaddingException e)
		{
			throw new RuntimeException(e);
		}
	}
	
	private void loadCredentials()
	{
		try
		{
			final Node creds = L2XMLUtils.getChildNodeByName(L2XMLUtils.getXMLFile(APPLICATION_DIRECTORY.resolve("fast_login_credentials.xml")), "credentials");
			restrictIPs:
			{
				final Node ipconfig = L2XMLUtils.getChildNodeByName(creds, "servedIPs");
				if (ipconfig == null)
					break restrictIPs;
				
				if (L2XMLUtils.getNodeAttributeBooleanValue(ipconfig, "includeLAN", false))
				{
					_servedIPs.add(new IPv4AddressPrefix(new byte[] { 127, 0, 0, 0 }, 8));
					_servedIPs.add(new IPv4AddressPrefix(new byte[] { 10, 0, 0, 0 }, 8));
					_servedIPs.add(new IPv4AddressPrefix(new byte[] { (byte)172, 16, 0, 0 }, 12));
					_servedIPs.add(new IPv4AddressPrefix(new byte[] { (byte)192, (byte)168, 0, 0 }, 16));
				}
				
				for (final Node ip : L2XMLUtils.listNodesByNodeName(ipconfig, "IPv4"))
				{
					final String[] octets = ip.getTextContent().split("\\.");
					final byte[] ipv4 = new byte[4];
					for (int i = 0; i < ipv4.length; ++i)
						ipv4[i] = (byte)Integer.parseInt(octets[i]);
					_servedIPs.add(new IPv4AddressPrefix(ipv4, 32));
				}
			}
			
			final int[] scKeyList = new int[32];
			for (final Node acc : L2XMLUtils.listNodesByNodeName(creds, "account"))
			{
				final String alias = L2XMLUtils.getString(acc, "fakeName").toLowerCase(Locale.ENGLISH);
				final String user = L2XMLUtils.getString(acc, "user").toLowerCase(Locale.ENGLISH);
				final String pass = L2XMLUtils.getString(acc, "pass");
				final Node sc = L2XMLUtils.getChildNodeByName(acc, "card");
				int sz = 0;
				if (sc != null)
				{
					for (Node key : L2XMLUtils.listNodesByNodeName(sc, "key"))
					{
						try
						{
							scKeyList[sz] = Integer.parseInt(key.getTextContent());
						}
						catch (NumberFormatException e)
						{
							scKeyList[sz] = -1; // emulated client behavior
						}
						++sz;
					}
				}
				final int[] scKeys;
				if (sz > 0)
				{
					scKeys = new int[sz];
					System.arraycopy(scKeyList, 0, scKeys, 0, sz);
				}
				else
					scKeys = ArrayUtils.EMPTY_INT_ARRAY;
				_fakeUsers.put(alias, new Credentials(user, pass, scKeys));
				
				final String pin = L2XMLUtils.getString(acc, "pin", null);
				if (pin != null)
					_accountPINs.put(user, pin);
			}
		}
		catch (NoSuchFileException | FileNotFoundException e)
		{
			LOG.info("Fast login configuration file is missing.");
		}
		catch (IOException | ParserConfigurationException | SAXException e)
		{
			LOG.error("Fast login will not be available.", e);
			_fakeUsers.clear();
			_accountPINs.clear();
		}
	}
	
	private boolean isServed(L2LoginClient client)
	{
		if (_servedIPs.isEmpty())
			return true;
		
		for (final IPv4AddressPrefix ipv4 : _servedIPs)
			if (ipv4.isIncluded(client.getInetAddress()))
				return true;
		
		return false;
	}
	
	@Override
	protected void connectionTerminated(L2LoginClient client, L2LoginServer server)
	{
		super.connectionTerminated(client, server);
		
		if (client != null)
			_rsaKeys.remove(client);
	}
	
	@Override
	protected void serverPacketArrived(L2LoginServer sender, L2LoginClient recipient, Packet packet)
	{
		if (_interceptionKeyPair == null || _fakeUsers.isEmpty() || !isServed(recipient))
			return;
		
		final ByteBuffer buf = packet.getDefaultBufferForModifications();
		if (buf.get(0) == OP_SC_CHECK_REQ)
		{
			// TODO: deal with legacy SC (unenciphered)
			
			buf.position(1);
			final int accountID = buf.getInt();
			final int key = buf.get() - 1;
			
			final LoginAttempt la = _rsaKeys.get(recipient);
			if (la._userData == null || key >= la._userData._scKeys.length)
				return; // unknown user or SC key, await manual entry
				
			// as proxy only deals with client-constructed packets, we must pre-allocate padding
			final ByteBuffer response = ByteBuffer.allocate(BLOCK_OFFSET_SC_KEY + BLOCK_SIZE_RSA + 3 + 16).order(buf.order());
			response.put((byte)OP_REQ_SC_CHECK).putInt(accountID).limit(BLOCK_OFFSET_SC_KEY + BLOCK_SIZE_RSA).position(response.limit() - BLOCK_OFFSET_SC_KEY);
			response.put((byte)4).putInt(la._userData._scKeys[key]);
			try
			{
				final Cipher rsa = Cipher.getInstance("RSA/ECB/nopadding");
				final KeyFactory kf = KeyFactory.getInstance("RSA");
				rsa.init(Cipher.ENCRYPT_MODE, kf.generatePublic(new RSAPublicKeySpec(la.getModulus(), RSAKeyGenParameterSpec.F4)));
				response.position(BLOCK_OFFSET_SC_KEY);
				response.put(rsa.doFinal(response.array(), BLOCK_OFFSET_SC_KEY, BLOCK_SIZE_RSA));
			}
			catch (GeneralSecurityException e)
			{
				LOG.error("Failed to decipher/reencipher a security card key!", e);
				return;
			}
			// prematurely inject checksum so that it would be available for notifications
			{
				response.clear();
				LoginCipher.injectChecksum(response, 16); // client packet checksum scheme
			}
			sender.sendPacket(new ProxyRepeatedPacket(response.array()));
			// we have not fired an async notification for the packet that is being currently handled, so fire the response packet notification later
			final long time = System.currentTimeMillis();
			L2ThreadPool.execute(() -> recipient.notifyPacketForwarded(null, response, time));
			return;
		}
		
		buf.position(1 + 8);
		if (buf.remaining() < 128)
			return;
		
		final byte[] modulus = new byte[BLOCK_SIZE_RSA];
		{
			//buf.position(1 + 8);
			buf.get(modulus);
		}
		_rsaKeys.put(recipient, new LoginAttempt(modulus));
		{
			buf.position(1 + 8);
			buf.put(_interceptionKeyPair.getScrambledModulus());
		}
		packet.setForwardedBody(buf);
	}
	
	@Override
	protected void clientPacketArrived(L2LoginClient sender, L2LoginServer recipient, Packet packet)
	{
		if (_interceptionKeyPair == null || _fakeUsers.isEmpty() || !isServed(sender))
			return;
		
		final ByteBuffer buf = packet.getDefaultBufferForModifications();
		
		if (buf.get(0) == OP_REQ_SC_CHECK)
		{
			// TODO: deal with legacy SC (unenciphered)
			// manual SC key entry, must ALWAYS reencipher
			byte[] keyBlock = new byte[BLOCK_SIZE_RSA];
			buf.position(BLOCK_OFFSET_SC_KEY);
			buf.get(keyBlock);
			
			try
			{
				final Cipher rsa = Cipher.getInstance("RSA/ECB/nopadding");
				rsa.init(Cipher.DECRYPT_MODE, _interceptionKeyPair.getPair().getPrivate());
				keyBlock = rsa.doFinal(keyBlock, 0, BLOCK_SIZE_RSA);
				
				final KeyFactory kf = KeyFactory.getInstance("RSA");
				rsa.init(Cipher.ENCRYPT_MODE, kf.generatePublic(new RSAPublicKeySpec(_rsaKeys.get(sender).getModulus(), RSAKeyGenParameterSpec.F4)));
				buf.position(BLOCK_OFFSET_SC_KEY);
				buf.put(rsa.doFinal(keyBlock));
				
				packet.setForwardedBody(buf);
			}
			catch (GeneralSecurityException e)
			{
				LOG.error("Failed to decipher/reencipher a security card key!", e);
				return;
			}
			return;
		}
		
		buf.position(1);
		
		if (buf.remaining() < BLOCK_SIZE_RSA)
		{
			// legacy protocol, easier to understand when not an integral part of the algorithm
			// therefore some copy-pasted code
			final byte[] userBlock = new byte[BLOCK_SIZE_PLAIN_USER], passBlock = new byte[BLOCK_SIZE_PLAIN_PASSWORD];
			buf.get(userBlock).get(passBlock);
			final String fakeName = new String(userBlock, US_ASCII).trim().toLowerCase();
			final Credentials realData = _fakeUsers.get(fakeName);
			if (realData == null)
				return;
			
			ByteBuffer writer;
			{
				writer = ByteBuffer.wrap(userBlock);
				for (int i = 0; i < writer.remaining(); ++i)
					writer.put(writer.position() + i, (byte)0);
				writer.put(realData._account.getBytes(US_ASCII));
			}
			{
				writer = ByteBuffer.wrap(passBlock);
				for (int i = 0; i < writer.remaining(); ++i)
					writer.put(writer.position() + i, (byte)0);
				writer.put(realData._password.getBytes(US_ASCII));
			}
			
			buf.position(1);
			buf.put(userBlock).put(passBlock);
			packet.setForwardedBody(buf);
			return;
		}
		
		byte[] userBlock = new byte[BLOCK_SIZE_RSA], passBlock = null;
		buf.get(userBlock);
		if (buf.remaining() >= BLOCK_SIZE_RSA)
		{
			passBlock = new byte[BLOCK_SIZE_RSA];
			buf.get(passBlock);
		}
		
		try
		{
			final Cipher rsa = Cipher.getInstance("RSA/ECB/nopadding");
			rsa.init(Cipher.DECRYPT_MODE, _interceptionKeyPair.getPair().getPrivate());
			
			userBlock = rsa.doFinal(userBlock, 0, BLOCK_SIZE_RSA);
			final int userBlockOffset = getInputOffset(userBlock);
			
			final Credentials realData;
			replaceData: if (passBlock != null)
			{
				passBlock = rsa.doFinal(passBlock, 0, BLOCK_SIZE_RSA);
				final int passBlockOffset = getInputOffset(passBlock);
				
				final String fakeName = new String(userBlock, userBlockOffset, BLOCK_SIZE_RSA - userBlockOffset, US_ASCII).trim().toLowerCase();
				realData = _fakeUsers.get(fakeName);
				if (realData == null)
					break replaceData;
				
				ByteBuffer writer;
				int additionalOffset;
				{
					writer = ByteBuffer.wrap(userBlock, userBlockOffset, BLOCK_SIZE_RSA - userBlockOffset);
					for (additionalOffset = 0; writer.get() == 0; ++additionalOffset)
					{
						// ignore preceding nul bytes in the original packet
					}
					while (writer.hasRemaining())
						writer.put((byte)0);
					writer.position(userBlockOffset + additionalOffset);
					writer.put(realData._account.getBytes(US_ASCII));
				}
				{
					writer = ByteBuffer.wrap(passBlock, passBlockOffset, BLOCK_SIZE_RSA - passBlockOffset);
					for (additionalOffset = 0; writer.get() == 0; ++additionalOffset)
					{
						// ignore preceding nul bytes in the original packet
					}
					while (writer.hasRemaining())
						writer.put((byte)0);
					writer.position(passBlockOffset + additionalOffset);
					writer.put(realData._password.getBytes(US_ASCII));
				}
			}
			else
			{
				final String fakeName = new String(userBlock, userBlockOffset, BLOCK_SIZE_RSA - userBlockOffset, US_ASCII).trim().toLowerCase();
				realData = _fakeUsers.get(fakeName);
				if (realData == null)
					break replaceData;
				
				ByteBuffer writer;
				int additionalOffset;
				{
					writer = ByteBuffer.wrap(userBlock, userBlockOffset, BLOCK_SIZE_RSA - userBlockOffset);
					for (additionalOffset = 0; writer.get() == 0; ++additionalOffset)
					{
						// ignore preceding nul bytes in the original packet
					}
					while (writer.hasRemaining())
						writer.put((byte)0);
					writer.position(userBlockOffset + additionalOffset);
					writer.put(realData._account.getBytes(US_ASCII));
				}
				{
					writer = ByteBuffer.wrap(userBlock, userBlockOffset + SUBBLOCK_SIZE_MIXED, BLOCK_SIZE_RSA - userBlockOffset - SUBBLOCK_SIZE_MIXED);
					for (additionalOffset = 0; writer.get() == 0; ++additionalOffset)
					{
						// ignore preceding nul bytes in the original packet
					}
					while (writer.hasRemaining())
						writer.put((byte)0);
					writer.position(userBlockOffset + SUBBLOCK_SIZE_MIXED + additionalOffset);
					writer.put(realData._password.getBytes(US_ASCII));
				}
			}
			
			CLIENT_AUTH_HELPER.adjustMixedBlock(sender, userBlock);
			
			final LoginAttempt la = _rsaKeys.get(sender);
			la._userData = realData;
			final KeyFactory kf = KeyFactory.getInstance("RSA");
			rsa.init(Cipher.ENCRYPT_MODE, kf.generatePublic(new RSAPublicKeySpec(la.getModulus(), RSAKeyGenParameterSpec.F4)));
			
			buf.position(1);
			buf.put(userBlock = rsa.doFinal(userBlock));
			if (passBlock != null)
				buf.put(passBlock = rsa.doFinal(passBlock));
			
			packet.setForwardedBody(buf);
		}
		catch (GeneralSecurityException | IllegalArgumentException e)
		{
			LOG.error("Failed to decipher/reencipher account credentials!", e);
			return;
		}
	}
	
	private static final int getInputOffset(byte[] rsaBlock) throws IllegalArgumentException
	{
		int sizeOffset = -1;
		while (rsaBlock[++sizeOffset] == 0)
		{
			// skip preceding nul bytes
		}
		
		if (rsaBlock[sizeOffset] != BLOCK_SIZE_RSA - ++sizeOffset)
			throw new IllegalArgumentException();
		
		return sizeOffset;
	}
	
	@Override
	public String getScriptName()
	{
		return "Fast login with fake credentials";
	}
	
	@Override
	public String getAuthor()
	{
		return "_dev_";
	}
	
	@Override
	public String getVersionString()
	{
		return "All";
	}
	
	Map<String, String> getAccountPINs()
	{
		return _accountPINs;
	}
	
	String toRealAccount(String clientLogonName)
	{
		final Credentials realData = _fakeUsers.get(clientLogonName);
		return realData != null ? realData._account : clientLogonName;
	}
	
	@Override
	public void onLoad() throws RuntimeException
	{
		super.onLoad();
		
		_inputToRealAccountNameTranslator.onLoad();
		_stealthPlugin.onLoad();
		_characterPinPlugin.onLoad();
	}
	
	@Override
	public void onUnload() throws RuntimeException
	{
		super.onUnload();
		
		_inputToRealAccountNameTranslator.onUnload();
		_stealthPlugin.onUnload();
		_characterPinPlugin.onUnload();
	}
	
	private static final class Credentials
	{
		final String _account, _password;
		final int[] _scKeys;
		
		Credentials(String account, String password, int... scKeys)
		{
			_account = account;
			_password = password;
			_scKeys = scKeys;
		}
	}
	
	private static final class LoginAttempt
	{
		private final byte[] _rsaKey;
		Credentials _userData;
		
		LoginAttempt(byte[] rsaKey)
		{
			_rsaKey = rsaKey;
		}
		
		BigInteger getModulus()
		{
			return ScrambledRSAKeyPair.unscrambleModulus(_rsaKey.clone());
		}
	}
	
	/**
	 * Associates an account name with the game client.
	 * 
	 * @author _dev_
	 */
	class AccountDataProvider extends PpeEnabledGameScript
	{
		@ScriptFieldAlias
		private static final String ACCOUNT_NAME = "adp_account_name";
		
		@Override
		public String getName()
		{
			return "Account data provider";
		}
		
		@Override
		public void handleClientPacket(L2GameClient client, L2GameServer server, RandomAccessMMOBuffer buf) throws RuntimeException
		{
			final String clientLogonName = buf.readFirstString(ACCOUNT_NAME).toLowerCase(Locale.ENGLISH);
			client.setAccount(toRealAccount(clientLogonName));
		}
		
		@Override
		public void handleServerPacket(L2GameClient client, L2GameServer server, RandomAccessMMOBuffer buf) throws RuntimeException
		{
			// do nothing here
		}
	}
	
	/**
	 * Automatically inputs character selection PIN code, if applicable.
	 * 
	 * @author _dev_
	 */
	class FastPIN extends PpeEnabledGameScript
	{
		@ScriptFieldAlias
		private static final String DIALOG_TYPE = "fast_login_pin_dialog";
		
		private final Set<L2GameClient> _attempted;
		
		public FastPIN()
		{
			_attempted = Collections.newSetFromMap(new ConcurrentHashMap<>());
		}
		
		@Override
		public void handleDisconnection(L2GameClient client)
		{
			super.handleDisconnection(client);
			
			_attempted.remove(client);
		}
		
		@Override
		public void handleClientPacket(L2GameClient client, L2GameServer server, RandomAccessMMOBuffer buf) throws RuntimeException
		{
			// do nothing here
		}
		
		@Override
		public void handleServerPacket(L2GameClient client, L2GameServer server, RandomAccessMMOBuffer rab) throws RuntimeException
		{
			final String account = client.getAccount();
			if (account == null || rab.readFirstInteger32(DIALOG_TYPE) != 1 || !_attempted.add(client))
				return;
			
			final String pin = getAccountPINs().get(account);
			if (pin == null)
				return;
			
			final IGameProtocolVersion pv = client.getProtocol();
			if (!pv.isNewerThanOrEqualTo(ClientProtocolVersion.VALIANCE))
				return;
			
			final int size = 3 + CommonPacketSender.stdStringSize(pin);
			final ByteBuffer bb = CommonPacketSender.allocate(size);
			final MMOBuffer buf = CommonPacketSender.allocate(bb);
			buf.writeC(0xD0);
			buf.writeH(pv.isNewerThanOrEqualTo(ClientProtocolVersion.ERTHEIA) ? 0xA7 : 0xAB);
			buf.writeS(pin);
			server.sendPacket(new ProxyRepeatedPacket(bb));
			client.notifyPacketForwarded(null, bb, System.currentTimeMillis());
		}
		
		@Override
		public String getName()
		{
			return "Fast PIN";
		}
	}
	
	/**
	 * Hides the actual username input via the client from the game server. Replaces with the real username (email) instead.
	 * 
	 * @author _dev_
	 */
	class HideFastLogin extends GameScript
	{
		public HideFastLogin()
		{
			super(STEALTH_OPCODES, null);
		}
		
		@Override
		protected void clientPacketArrived(L2GameClient sender, L2GameServer recipient, Packet packet)
		{
			if (recipient.getProtocol().isOlderThan(ClientProtocolVersion.THE_KAMAEL))
				return; // FIXME: test opcodes!
				
			final String clientLoginName;
			final ByteBuffer old = packet.getForwardedBody();
			final MMOBuffer buf = new MMOBuffer();
			{
				old.position(1);
				buf.setByteBuffer(old);
				clientLoginName = buf.readS().toLowerCase(Locale.ENGLISH);
			}
			
			final String realName = toRealAccount(clientLoginName);
			if (realName == null || clientLoginName.equals(realName))
				return;
			
			final int extraBytes = CommonPacketSender.stdStringSize(realName) - CommonPacketSender.stdStringSize(clientLoginName);
			final ByteBuffer bb = ByteBuffer.allocate(old.capacity() + extraBytes).order(old.order());
			bb.put(old.get(0));
			{
				buf.setByteBuffer(bb);
				buf.writeS(realName);
			}
			bb.put(old);
			
			packet.setForwardedBody(bb);
		}
		
		@Override
		public String getScriptName()
		{
			return "Fast login stealth plugin";
		}
		
		@Override
		public String getAuthor()
		{
			return "_dev_";
		}
		
		@Override
		public String getVersionString()
		{
			return "Any throne GS";
		}
	}
	
	/**
	 * Allows implementors to enable client support for auth daemons subject to the fragile binary offset issue.<BR>
	 * <BR>
	 * Reminder: Mixed RSA block always contains two 16 byte ASCII strings.
	 * 
	 * @author _dev_
	 */
	public interface FragileAuthSupport
	{
		/**
		 * Changes id/pass offsets within the data block.
		 * 
		 * @param client associated client
		 * @param block data block
		 */
		void adjustMixedBlock(L2LoginClient client, byte[] block);
	}
	
	private static final FragileAuthSupport NO_SUPPORT = (c, b) -> Objects.requireNonNull(c);
	private static FragileAuthSupport CLIENT_AUTH_HELPER = NO_SUPPORT;
	
	/**
	 * As long as FastLogin is active (enabled and >= 1 account configuration, which may be fake if necessary), gives access
	 * to the ID/pass block of legacy (C3 557 to GoD) clients.<BR>
	 * Originally designed to walk C4 through modern (single data block) AuthGateD. May be extended to generally allow clients pass
	 * through unsupported login servers.
	 * 
	 * @param helper helper script
	 */
	public static final void setAuthHelper(FragileAuthSupport helper)
	{
		if (helper == null)
			helper = NO_SUPPORT;
		CLIENT_AUTH_HELPER = helper;
	}
}
