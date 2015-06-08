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
package net.l2emuproject.proxy.network.meta;

/**
 * An interface that tracks all field aliases that are required for this application to function properly.
 * If packet definitions do not provide fields with these aliases, NetPro will default to precompiled behavior,
 * which is OK for some versions between HF and Valiance.
 * 
 * @author _dev_
 */
public interface RequiredInvasiveOperations
{
	// Login
	/** Identifies the blowfish key byte array */
	String BLOWFISH_KEY = "__INVASIVE_AUTO_EXTRACT_BF_KEY";
	/** Identifies the game server list type */
	String SERVER_LIST_TYPE = "__INVASIVE_AUTO_EXTRACT_GS_LIST_TYPE";
	/** Identifies the game server ID integer */
	String GAME_SERVER_ID = "__INVASIVE_AUTO_EXTRACT_GS_ID";
	/** Identifies the game server IPv4 byte array */
	String GAME_SERVER_IP = "__INVASIVE_AUTO_REPLACE_GS_IP";
	/** Identifies the game server port integer */
	String GAME_SERVER_PORT = "__INVASIVE_AUTO_REPLACE_GS_PORT";
	
	// Game
	/** Identifies the client packet obfuscator key integer */
	String OBFUSCATION_KEY = "__INVASIVE_AUTO_EXTRACT_OBFUSCATION_KEY";
	/** Identifies the cipher key part (as integer) */
	String CIPHER_KEY_PART = "__INVASIVE_AUTO_EXTRACT_CIPHER_KEY_HALF";
	/** Identifies the cipher state (enabled/disabled) boolean */
	String CIPHER_STATE = "__INVASIVE_AUTO_EXTRACT_CIPHER_STATE";
}
