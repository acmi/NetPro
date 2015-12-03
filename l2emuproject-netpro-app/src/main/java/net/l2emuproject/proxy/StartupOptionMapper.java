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
package net.l2emuproject.proxy;

import java.util.HashMap;
import java.util.Map;

/**
 * Stores a lookup table of {@link StartupOption} aliases.
 * 
 * @author savormix
 */
final class StartupOptionMapper
{
	private final Map<String, StartupOption> _lookup;
	
	StartupOptionMapper()
	{
		_lookup = new HashMap<>();
		for (final StartupOption lo : StartupOption.class.getEnumConstants())
		{
			for (final String alias : lo.getAlias())
			{
				final StartupOption old = _lookup.put(alias, lo);
				if (old != null)
					throw new InternalError(alias + " is used by both " + old + " and " + lo);
			}
		}
	}
	
	Map<String, StartupOption> getLookup()
	{
		return _lookup;
	}
}
