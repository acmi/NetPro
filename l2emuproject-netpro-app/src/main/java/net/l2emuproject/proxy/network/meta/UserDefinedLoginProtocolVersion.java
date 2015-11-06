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

import net.l2emuproject.network.protocol.ILoginProtocolVersion;

/**
 * A login protocol version constructed from user-supplied details.
 * 
 * @author savormix
 */
public class UserDefinedLoginProtocolVersion extends UserDefinedProtocolVersion implements ILoginProtocolVersion
{
	/**
	 * Constructs a login protocol version definition.
	 * 
	 * @param alias protocol name
	 * @param category protocol group
	 * @param version protocol revision number
	 * @param date protocol version introduction to NA data
	 */
	public UserDefinedLoginProtocolVersion(String alias, String category, int version, long date)
	{
		super(alias, category, version, date);
	}
}
