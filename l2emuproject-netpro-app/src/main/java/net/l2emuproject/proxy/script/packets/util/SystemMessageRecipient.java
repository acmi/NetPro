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
package net.l2emuproject.proxy.script.packets.util;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;

import net.l2emuproject.geometry.point.IPoint3D;
import net.l2emuproject.geometry.point.impl.Point3D;
import net.l2emuproject.proxy.network.meta.EnumeratedPayloadField;
import net.l2emuproject.proxy.network.meta.RandomAccessMMOBuffer;
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
	String SYSMSG_TOKEN_TYPE = "SYS_MESSAGE_TOKEN_TYPE";
	
	@ScriptFieldAlias
	String SYSMSG_VALUE_SINGLE = "SYS_MESSAGE_TOKEN_VALUE_SINGLE";
	@ScriptFieldAlias
	String SYSMSG_VALUE_SKILL_ID = "SYS_MESSAGE_TOKEN_TYPE4_VALUE1";
	@ScriptFieldAlias
	String SYSMSG_VALUE_SKILL_LEVEL_SUBLEVEL = "SYS_MESSAGE_TOKEN_TYPE4_VALUE2";
	@ScriptFieldAlias
	String SYSMSG_VALUE_POINT_X = "SYS_MESSAGE_TOKEN_TYPE7_VALUE1";
	@ScriptFieldAlias
	String SYSMSG_VALUE_POINT_Y = "SYS_MESSAGE_TOKEN_TYPE7_VALUE2";
	@ScriptFieldAlias
	String SYSMSG_VALUE_POINT_Z = "SYS_MESSAGE_TOKEN_TYPE7_VALUE3";
	@ScriptFieldAlias
	String SYSMSG_VALUE_AUGMENTABLE_ITEM_TEMPLATE = "SYS_MESSAGE_TOKEN_TYPE8_VALUE1";
	@ScriptFieldAlias
	String SYSMSG_VALUE_AUGMENTABLE_ITEM_OPTION1 = "SYS_MESSAGE_TOKEN_TYPE8_VALUE2";
	@ScriptFieldAlias
	String SYSMSG_VALUE_AUGMENTABLE_ITEM_OPTION2 = "SYS_MESSAGE_TOKEN_TYPE8_VALUE3";
	@ScriptFieldAlias
	String SYSMSG_VALUE_MUTABLE_FSTRING_TEMPLATE = "SYS_MESSAGE_TOKEN_TYPE14_VALUE1";
	@ScriptFieldAlias
	String SYSMSG_VALUE_MUTABLE_FSTRING_TOKEN1 = "SYS_MESSAGE_TOKEN_TYPE14_VALUE2";
	@ScriptFieldAlias
	String SYSMSG_VALUE_MUTABLE_FSTRING_TOKEN2 = "SYS_MESSAGE_TOKEN_TYPE14_VALUE3";
	@ScriptFieldAlias
	String SYSMSG_VALUE_MUTABLE_FSTRING_TOKEN3 = "SYS_MESSAGE_TOKEN_TYPE14_VALUE4";
	@ScriptFieldAlias
	String SYSMSG_VALUE_MUTABLE_FSTRING_TOKEN4 = "SYS_MESSAGE_TOKEN_TYPE14_VALUE5";
	@ScriptFieldAlias
	String SYSMSG_VALUE_MUTABLE_FSTRING_TOKEN5 = "SYS_MESSAGE_TOKEN_TYPE14_VALUE6";
	@ScriptFieldAlias
	String SYSMSG_VALUE_DMG_ATTACKER = "SYS_MESSAGE_TOKEN_TYPE16_VALUE1";
	@ScriptFieldAlias
	String SYSMSG_VALUE_DMG_TARGET = "SYS_MESSAGE_TOKEN_TYPE16_VALUE2";
	@ScriptFieldAlias
	String SYSMSG_VALUE_DMG_HPDIFF = "SYS_MESSAGE_TOKEN_TYPE16_VALUE3";
	
	/** Indicates a string token. */
	int SYSMSG_TOKEN_STRING = 0;
	/** Indicates an integer token. */
	int SYSMSG_TOKEN_INTEGER = 1;
	
	/** Indicates a NPC template token. */
	int SYSMSG_TOKEN_NPC = 2;
	/** Indicates an item template token. */
	int SYSMSG_TOKEN_ITEM = 3;
	/** Indicates a skill [level] template token. */
	int SYSMSG_TOKEN_SKILL = 4;
	int SYSMSG_TOKEN_RESIDENCE = 5;
	int SYSMSG_TOKEN_AMOUNT = 6;
	int SYSMSG_TOKEN_REGION = 7;
	/** Indicates an item template with variation options token. */
	int SYSMSG_TOKEN_AUGMENTABLE_ITEM = 8;
	int SYSMSG_TOKEN_ELEMENT = 9;
	int SYSMSG_TOKEN_INZONE = 10;
	int SYSMSG_TOKEN_FSTRING_IMMUTABLE = 11;
	/** Indicates a player name token. */
	int SYSMSG_TOKEN_PLAYER = 12;
	int SYSMSG_TOKEN_SYSSTRING = 13;
	int SYSMSG_TOKEN_FSTRING_MUTABLE = 14;
	int SYSMSG_TOKEN_CLASS = 15;
	int SYSMSG_TOKEN_DAMAGE = 16;
	int SYSMSG_TOKEN_DWORD = 17;
	int SYSMSG_TOKEN_BYTE1 = 19;
	int SYSMSG_TOKEN_BYTE2 = 20;
	int SYSMSG_TOKEN_WORD = 21;
	int SYSMSG_TOKEN_FACTION = 24;
	
	@SuppressWarnings("unchecked")
	static <T> T readToken(RandomAccessMMOBuffer buf) throws IllegalStateException
	{
		final EnumeratedPayloadField currentToken = currentField(buf, SYSMSG_TOKEN_TYPE);
		if (currentToken == null)
			throw new IllegalStateException("Current buffer's position is not at any system message token type field");
		
		final int tokenType = buf.readInteger32(currentToken);
		switch (tokenType)
		{
			case SYSMSG_TOKEN_STRING:
			case SYSMSG_TOKEN_PLAYER:
				return (T)readStringToken(buf);
			case SYSMSG_TOKEN_INTEGER:
			case SYSMSG_TOKEN_NPC:
			case SYSMSG_TOKEN_ITEM:
			case SYSMSG_TOKEN_RESIDENCE:
			case SYSMSG_TOKEN_ELEMENT:
			case SYSMSG_TOKEN_INZONE:
			case SYSMSG_TOKEN_FSTRING_IMMUTABLE:
			case SYSMSG_TOKEN_SYSSTRING:
			case SYSMSG_TOKEN_CLASS:
			case SYSMSG_TOKEN_DWORD:
			case SYSMSG_TOKEN_BYTE1:
			case SYSMSG_TOKEN_BYTE2:
			case SYSMSG_TOKEN_WORD:
			case SYSMSG_TOKEN_FACTION:
				return (T)Integer.valueOf(readIntegerToken(buf));
			case SYSMSG_TOKEN_SKILL:
				return (T)Long.valueOf(readSkillToken(buf));
			case SYSMSG_TOKEN_AMOUNT:
				return (T)Long.valueOf(readAmountToken(buf));
			case SYSMSG_TOKEN_REGION:
				return (T)readRegionToken(buf);
			case SYSMSG_TOKEN_AUGMENTABLE_ITEM:
				return (T)readItemWithVariationToken(buf);
			case SYSMSG_TOKEN_FSTRING_MUTABLE:
				return (T)readMutableFString(buf);
			case SYSMSG_TOKEN_DAMAGE:
				return (T)readDamage(buf);
			default:
				throw new IllegalArgumentException("Unknown token type: " + tokenType);
		}
	}
	
	static void skipToken(RandomAccessMMOBuffer buf) throws IllegalStateException
	{
		readToken(buf);
	}
	
	/**
	 * Reads a string token value.
	 * 
	 * @param buf a buffer pointing to a token value field
	 * @return string value
	 */
	static String readStringToken(RandomAccessMMOBuffer buf)
	{
		final EnumeratedPayloadField currentTokenValue = currentField(buf, SYSMSG_VALUE_SINGLE);
		if (currentTokenValue != null)
			return buf.readString(currentTokenValue);
		
		throw new IllegalStateException("Current buffer's position is not at any system message token value field");
	}
	
	/**
	 * Reads an integer token value. This should be used for reading template IDs unless there is a designated method, e.g. {@link #readSkillToken(RandomAccessMMOBuffer)}.
	 * 
	 * @param buf a buffer pointing to a token value field
	 * @return integer value
	 */
	static int readIntegerToken(RandomAccessMMOBuffer buf)
	{
		final EnumeratedPayloadField currentTokenValue = currentField(buf, SYSMSG_VALUE_SINGLE);
		if (currentTokenValue != null)
			return buf.readInteger32(currentTokenValue);
		
		throw new IllegalStateException("Current buffer's position is not at any system message token value field");
	}
	
	/**
	 * Reads an integer token value.
	 * 
	 * @param buf a buffer pointing to a token value field
	 * @return integer value
	 */
	static long readAmountToken(RandomAccessMMOBuffer buf)
	{
		final EnumeratedPayloadField currentTokenValue = currentField(buf, SYSMSG_VALUE_SINGLE);
		if (currentTokenValue != null)
			return buf.readInteger(currentTokenValue);
		
		throw new IllegalStateException("Current buffer's position is not at any system message token value field");
	}
	
	static long readSkillToken(RandomAccessMMOBuffer buf)
	{
		final EnumeratedPayloadField skillID = currentField(buf, SYSMSG_VALUE_SKILL_ID);
		if (skillID == null)
			throw new IllegalStateException("Current buffer's position is not at any system message token value field");
		
		final long result = buf.readInteger(skillID) << 32;
		final EnumeratedPayloadField skillLevelSublevel = currentField(buf, SYSMSG_VALUE_SKILL_LEVEL_SUBLEVEL);
		return result | buf.readInteger32(skillLevelSublevel);
	}
	
	static IPoint3D readRegionToken(RandomAccessMMOBuffer buf)
	{
		final EnumeratedPayloadField x = currentField(buf, SYSMSG_VALUE_POINT_X);
		if (x == null)
			throw new IllegalStateException("Current buffer's position is not at any system message token value field");
		final EnumeratedPayloadField y = currentField(buf, SYSMSG_VALUE_POINT_Y), z = currentField(buf, SYSMSG_VALUE_POINT_Z);
		return new Point3D(buf.readInteger32(x), buf.readInteger32(y), buf.readInteger32(z));
	}
	
	static Triple<Integer, Integer, Integer> readItemWithVariationToken(RandomAccessMMOBuffer buf)
	{
		final EnumeratedPayloadField template = currentField(buf, SYSMSG_VALUE_AUGMENTABLE_ITEM_TEMPLATE);
		if (template == null)
			throw new IllegalStateException("Current buffer's position is not at any system message token value field");
		final EnumeratedPayloadField op1 = currentField(buf, SYSMSG_VALUE_AUGMENTABLE_ITEM_OPTION1), op2 = currentField(buf, SYSMSG_VALUE_AUGMENTABLE_ITEM_OPTION2);
		return Triple.of(buf.readInteger32(template), buf.readInteger32(op1), buf.readInteger32(op2));
	}
	
	static Pair<Integer, String[]> readMutableFString(RandomAccessMMOBuffer buf)
	{
		final EnumeratedPayloadField template = currentField(buf, SYSMSG_VALUE_MUTABLE_FSTRING_TEMPLATE);
		if (template == null)
			throw new IllegalStateException("Current buffer's position is not at any system message token value field");
		final EnumeratedPayloadField token1 = currentField(buf, SYSMSG_VALUE_MUTABLE_FSTRING_TOKEN1), token2 = currentField(buf, SYSMSG_VALUE_MUTABLE_FSTRING_TOKEN2),
				token3 = currentField(buf, SYSMSG_VALUE_MUTABLE_FSTRING_TOKEN3);
		final EnumeratedPayloadField token4 = currentField(buf, SYSMSG_VALUE_MUTABLE_FSTRING_TOKEN4), token5 = currentField(buf, SYSMSG_VALUE_MUTABLE_FSTRING_TOKEN5);
		return Pair.of(buf.readInteger32(template), new String[] { buf.readString(token1), buf.readString(token2), buf.readString(token3), buf.readString(token4), buf.readString(token5) });
	}
	
	static Triple<Integer, Integer, Integer> readDamage(RandomAccessMMOBuffer buf)
	{
		final EnumeratedPayloadField attacker = currentField(buf, SYSMSG_VALUE_DMG_ATTACKER);
		if (attacker == null)
			throw new IllegalStateException("Current buffer's position is not at any system message token value field");
		final EnumeratedPayloadField target = currentField(buf, SYSMSG_VALUE_DMG_TARGET), hpDiff = currentField(buf, SYSMSG_VALUE_DMG_HPDIFF);
		return Triple.of(buf.readInteger32(attacker), buf.readInteger32(target), buf.readInteger32(hpDiff));
	}
	
	static EnumeratedPayloadField currentField(RandomAccessMMOBuffer buf, String fieldName)
	{
		for (final EnumeratedPayloadField field : buf.getFieldIndices(fieldName))
			if (field.getOffset() == buf.getCurrentOffset())
				return field;
		return null;
	}
}
