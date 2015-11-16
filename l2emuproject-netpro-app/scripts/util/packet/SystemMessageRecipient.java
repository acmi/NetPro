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
package util.packet;

import net.l2emuproject.network.mmocore.MMOBuffer;
import net.l2emuproject.network.protocol.ClientProtocolVersion;
import net.l2emuproject.network.protocol.IGameProtocolVersion;
import net.l2emuproject.proxy.script.ScriptFieldAlias;

/**
 * Provides access to system message packets and related convenience methods.
 * 
 * @author _dev_
 */
public interface SystemMessageRecipient
{
	/** This is the system message ID integer */
	@ScriptFieldAlias
	String SYSMSG_ID = "SYS_MESSAGE_ID";
	/** This is the system message token type integer */
	@ScriptFieldAlias
	String SYSMSG_TOKENS = "SYS_MESSAGE_PARAMS";
	
	/** Indicates a string token. */
	int SYSMSG_TOKEN_STRING = 0;
	/** Indicates a NPC template token. */
	int SYSMSG_TOKEN_NPC = 2;
	/** Indicates a skill [level] template token. */
	int SYSMSG_TOKEN_SKILL = 4;
	/** Indicates a player name token. */
	int SYSMSG_TOKEN_PLAYER = 12;
	
	/**
	 * Reads the NPC template ID from the given buffer.
	 * 
	 * @param protocol network protocol version
	 * @param buf buffer wrapper
	 * @return NPC template ID
	 */
	static int getSysMsgNPCID(IGameProtocolVersion protocol, MMOBuffer buf)
	{
		return buf.readD();
	}
	
	/**
	 * Reads the skill template ID (skill ID and skill level) from the given buffer.
	 * 
	 * @param protocol network protocol version
	 * @param buf buffer wrapper
	 * @return skill template ID
	 */
	static long getSysMsgSkillNameID(IGameProtocolVersion protocol, MMOBuffer buf)
	{
		final long result = (long)buf.readD() << 32;
		final int level;
		if (protocol.isOlderThan(ClientProtocolVersion.ERTHEIA) || protocol.isNewerThanOrEqualTo(ClientProtocolVersion.INFINITE_ODYSSEY))
			level = buf.readD();
		else
			level = buf.readH();
		return result | level;
	}
}
