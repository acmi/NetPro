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
package net.l2emuproject.proxy.script.interpreter;

import net.l2emuproject.network.protocol.IProtocolVersion;
import net.l2emuproject.proxy.network.meta.container.MetaclassRegistry;
import net.l2emuproject.proxy.network.meta.exception.InvalidFieldValueInterpreterException;
import net.l2emuproject.proxy.network.meta.interpreter.IntegerTranslator;

/**
 * This class operates with IO or later skill name IDs.
 * 
 * @author _dev_
 */
public final class L2SkillTranslator
{
	/** An interpreter that returns skill names when calling the {@link IntegerTranslator#translate(long, IProtocolVersion)} method with a skill ID. */
	public static String SKILL_INTERPRETER = "Skill";
	/** An interpreter that returns skill names when calling the {@link IntegerTranslator#translate(long, IProtocolVersion)} method with a skill name ID. */
	public static String SKILL_NAME_ID_INTERPRETER = "SkillNameID";
	
	private L2SkillTranslator()
	{
		// utility class
	}
	
	/**
	 * Returns a string interpretation of the specified skill ID (at the first skill level).<BR>
	 * Depends on the {@link #SKILL_INTERPRETER} interpreter.
	 * 
	 * @param protocol protocol version
	 * @param skillID skill ID
	 * @return skill name
	 */
	public static final String translate(IProtocolVersion protocol, int skillID)
	{
		try
		{
			return String.valueOf(MetaclassRegistry.getInstance().getTranslator(SKILL_INTERPRETER, IntegerTranslator.class).translate(skillID, protocol, null));
		}
		catch (final InvalidFieldValueInterpreterException e)
		{
			return String.valueOf(skillID);
		}
	}
	
	/**
	 * Returns a string interpretation of the specified skill ID at the specified skill level.<BR>
	 * Depends on the {@link #SKILL_NAME_ID_INTERPRETER} interpreter.
	 * 
	 * @param protocol protocol version
	 * @param skillID skill ID
	 * @param level skill level
	 * @return skill name
	 */
	public static final String translate(IProtocolVersion protocol, int skillID, int level)
	{
		return translate(protocol, skillID, level, 0);
	}
	
	/**
	 * Returns a string interpretation of the specified skill ID at the specified skill level and sublevel.<BR>
	 * Depends on the {@link #SKILL_NAME_ID_INTERPRETER} interpreter.
	 * 
	 * @param protocol protocol version
	 * @param skillID skill ID
	 * @param level skill level
	 * @param sublevel skill sublevel
	 * @return skill name
	 */
	public static final String translate(IProtocolVersion protocol, int skillID, int level, int sublevel)
	{
		return translate(protocol, getSkillNameID(skillID, level, 0), null);
	}
	
	/**
	 * Returns a string interpretation of the specified skill ID at the specified skill level.<BR>
	 * Depends on the {@link #SKILL_NAME_ID_INTERPRETER} interpreter.
	 * 
	 * @param protocol protocol version
	 * @param skillID skill ID
	 * @param jointLevel skill level and sublevel
	 * @param passNullHere {@code null}
	 * @return skill name
	 */
	public static final String translate(IProtocolVersion protocol, int skillID, int jointLevel, Void passNullHere)
	{
		return translate(protocol, ((long)skillID << 32) | jointLevel, passNullHere);
	}
	
	/**
	 * Returns a string interpretation of the specified skill ID at the specified skill level.<BR>
	 * Depends on the {@link #SKILL_NAME_ID_INTERPRETER} interpreter.
	 * 
	 * @param protocol protocol version
	 * @param skillNameID skill ID and level
	 * @param passNullHere {@code null}
	 * @return skill name
	 */
	public static final String translate(IProtocolVersion protocol, long skillNameID, Void passNullHere)
	{
		try
		{
			return String.valueOf(MetaclassRegistry.getInstance().getTranslator(SKILL_NAME_ID_INTERPRETER, IntegerTranslator.class).translate(skillNameID, protocol, null));
		}
		catch (final InvalidFieldValueInterpreterException e)
		{
			return getSkillID(skillNameID) + "_" + getSkillLevel(skillNameID) + "_" + getSkillSublevel(skillNameID);
		}
	}
	
	/**
	 * Converts given skill ID and level to a skill level ID.
	 * 
	 * @param skillID skill ID
	 * @param skillLevel skill level
	 * @param sublevel skill sublevel
	 * @return skill name ID
	 */
	public static final long getSkillNameID(int skillID, int skillLevel, int sublevel)
	{
		return ((long)skillID << 32) | ((long)sublevel << 16) | skillLevel;
	}
	
	/**
	 * Converts given skill ID and level to a skill level ID.
	 * 
	 * @param skillID skill ID
	 * @param jointLevel skill level and sublevel
	 * @param passNullHere {@code null}
	 * @return skill name ID
	 */
	public static final long getSkillNameID(int skillID, int jointLevel, Void passNullHere)
	{
		return ((long)skillID << 32) | (jointLevel & 0xFF_FF_FF_FFL);
	}
	
	/**
	 * Converts given skill name ID to a skill ID.
	 * 
	 * @param skillNameID skill name ID
	 * @return skill ID
	 */
	public static final int getSkillID(long skillNameID)
	{
		return (int)(skillNameID >>> 32);
	}
	
	/**
	 * Converts given skill name ID to a skill sublevel.
	 * 
	 * @param skillNameID skill name ID
	 * @return skill level
	 */
	public static final int getSkillSublevel(long skillNameID)
	{
		return getSkillLevel(skillNameID >>> 16);
	}
	
	/**
	 * Converts given skill name ID to a skill level.
	 * 
	 * @param skillNameID skill name ID
	 * @return skill level
	 */
	public static final int getSkillLevel(long skillNameID)
	{
		return (int)skillNameID & 0xFF_FF;
	}
}
