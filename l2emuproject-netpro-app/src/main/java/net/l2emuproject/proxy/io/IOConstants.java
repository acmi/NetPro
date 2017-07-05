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
package net.l2emuproject.proxy.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Container for useful I/O constants.<BR>
 * <BR>
 * This class is a part of a reference GUI provided for education purposes only. These classes
 * should help to understand how to interact with the underlying proxy core.<BR>
 * <BR>
 * Creating an own GUI is the preferred way to start using this application.
 * 
 * @author savormix
 */
public interface IOConstants
{
	/** A path to {@code user.home} */
	Path DESTINATION_DIRECTORY = Paths.get(System.getProperty("user.home", "."));
	/** Specifies the root directory dedicated for this application's files */
	Path APPLICATION_DIRECTORY = DESTINATION_DIRECTORY.resolve("l2emu-unique-netpro");
	/** Current runtime's working directory */
	Path WORKING_DIRECTORY = Paths.get(".");
	/** NetPro configuration file directory */
	Path CONFIG_DIRECTORY = WORKING_DIRECTORY.resolve("config");
	/** NetPro default script configuration file directory */
	Path SCRIPT_CONFIG_DIRECTORY = CONFIG_DIRECTORY.resolve("scripts");
	/** NetPro interpreter script configuration file directory */
	Path INTERPRETER_CONFIG_DIRECTORY = SCRIPT_CONFIG_DIRECTORY.resolve("interpreter");
	
	/** Default buffer size to use for new I/O */
	int DEFAULT_BUFFER_SIZE = 256 * 1024;
	
	/** Specifies the directory where packet logs are stored */
	Path LOG_DIRECTORY = APPLICATION_DIRECTORY.resolve("packet_logs");
	/** Specifies the directory where login server packet logs are stored */
	Path LOGIN_LOG_DIRECTORY = LOG_DIRECTORY.resolve("login");
	/** Specifies the directory where game server packet logs are stored */
	Path GAME_LOG_DIRECTORY = LOG_DIRECTORY.resolve("game");
	
	/** Specifies the directory to store data that is harvested from the game server */
	Path DATA_MINING_DIRECTORY = APPLICATION_DIRECTORY.resolve("data_mining");
	
	/** Prefix for a valid packet log file. */
	long LOG_MAGIC = 0xFFFF00ACDC000FEEL;
	/** Prefix for a valid packet log file that was not successfully finalized (application crash, power outage, etc). [indicates that recovery is possible] */
	long LOG_MAGIC_INCOMPLETE = 0xF411_F411_F411_F411L;
	/** Default packet log file extension */
	String LOG_EXTENSION = "plog";
	/** Version of packet log file format for files written by this application. */
	int LOG_VERSION = 7;
	/** Absolute position of the header size field in file */
	int LOG_HEADER_SIZE_POS = 8 + 1;
	/** Absolute position of the footer size field in file */
	int LOG_FOOTER_SIZE_POS = LOG_HEADER_SIZE_POS + 4;
	/** Absolute position of the first header field in file */
	int LOG_HEADER_START_POS = LOG_FOOTER_SIZE_POS + 4;
	/** Absolute position of the protocol version field in file */
	int LOG_PROTOCOL_POS = LOG_HEADER_START_POS + 8 + 8 + 1;
	
	/** [legacy] Fixed header size in previous file format versions. */
	int LOG_HEADER_SIZE_PRE_6 = 8 + 1 + 8 + 1;
	/** [legacy] Fixed footer size in previous file format versions. */
	int LOG_FOOTER_SIZE_PRE_5 = 8;
	/** [legacy] Fixed footer size in previous file format versions. */
	int LOG_FOOTER_SIZE_5 = LOG_FOOTER_SIZE_PRE_5 + 4;
	
	/** Default directory for packet hiding configurations. */
	Path PROTOCOL_PACKET_HIDING_DIR = APPLICATION_DIRECTORY.resolve("packet_hiding");
	/** Prefix for a valid packet display configuration file. */
	long PROTOCOL_PACKET_HIDING_MAGIC = 0x4E50205048494445L;
	/** Default packet display configuration file extension */
	String PROTOCOL_PACKET_HIDING_EXTENSION = "nph";
	/** Version of packet display configuration file format for files written by this application. */
	int PROTOCOL_PACKET_HIDING_VERSION = 1;
	
	long HISTORICAL_LOG_MAGIC_COMPLETE = 0x4E5020504C4F472BL, HISTORICAL_LOG_MAGIC_INCOMPLETE = 0x4E5020504C4F472DL;
	String HISTORICAL_LOG_EXTENSION = "npl";
	int HISTORICAL_LOG_FILE_VERSION_MAJOR = 1, HISTORICAL_LOG_FILE_VERSION_MINOR = 1;
	String HISTORICAL_LOG_CREATOR = "NetPro 2.0-SNAPSHOT";
	int INDEX_ABNORMAL_TYPES = stringToMagic('A', 'B', 'N', 'T'), INDEX_PLAYER_CHAR_NAMES = stringToMagic('P', 'C', 'H', 'N'), INDEX_PLAYER_CHAR_SUBJOBS = stringToMagic('P', 'C', 'H', 'S');
	int INDEX_NPC_TEMPLATES = stringToMagic('N', 'P', 'C', 'T'), INDEX_PET_TEMPLATES = stringToMagic('P', 'E', 'T', 'T'), INDEX_SUMMON_TEMPLATES = stringToMagic('S', 'U', 'M', 'T');
	int INDEX_STATIC_OBJECT_TEMPLATES = stringToMagic('S', 'T', 'O', 'T'), INDEX_CASTED_OR_LAUNCHED_SKILLS = stringToMagic('S', 'K', 'L', 'C'), INDEX_OWNED_SKILLS = stringToMagic('S', 'K', 'L', 'O');
	int INDEX_SKILL_TO_CASTER_TEMPLATE = stringToMagic('C', 'A', 'S', 'T'), INDEX_ROLES = stringToMagic('R', 'O', 'L', 'E'), INDEX_RELATIONS = stringToMagic('R', 'L', 'T', 'N');
	int INDEX_ABNORMAL_VISUAL_EFFECTS = stringToMagic('A', 'V', 'E', 'S'), INDEX_MULTISELL = stringToMagic('M', 'T', 'S', 'L'), INDEX_USERS = stringToMagic('U', 'S', 'E', 'R');
	
	/**
	 * Opens a resource file.
	 * 
	 * @param relativePath relative path to resource
	 * @return resource reader (UTF-8)
	 * @throws IOException if the specified resource cannot be read
	 */
	static BufferedReader openScriptResource(String... relativePath) throws IOException
	{
		return Files.newBufferedReader(SCRIPT_CONFIG_DIRECTORY.resolve(Paths.get(".", relativePath)));
	}
	
	static long stringToMagic(char c1, char c2, char c3, char c4, char c5, char c6, char c7, char c8)
	{
		return 0L;
	}
	
	static int stringToMagic(char c1, char c2, char c3, char c4)
	{
		return 0;
	}
}
