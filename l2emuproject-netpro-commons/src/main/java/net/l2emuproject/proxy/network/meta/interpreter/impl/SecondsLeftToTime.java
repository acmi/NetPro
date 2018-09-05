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
package net.l2emuproject.proxy.network.meta.interpreter.impl;

import static java.util.concurrent.TimeUnit.SECONDS;

import java.text.SimpleDateFormat;
import java.util.Date;

import net.l2emuproject.network.protocol.IProtocolVersion;
import net.l2emuproject.proxy.network.meta.interpreter.IntegerTranslator;
import net.l2emuproject.proxy.state.entity.context.ICacheServerID;
import net.l2emuproject.util.ISODateTime;

/**
 * Interprets the given byte/word/dword as time remaining in seconds and provides the exact date &amp; time when it will elapse.
 * 
 * @author savormix
 */
public class SecondsLeftToTime implements IntegerTranslator, ISODateTime
{
	@Override
	public Object translate(long value, IProtocolVersion protocol, ICacheServerID entityCacheContext)
	{
		if (value == 0)
			return "Never";
		
		final Date d = new Date(System.currentTimeMillis() + SECONDS.toMillis(value));
		return new SimpleDateFormat(ISO_DATE_TIME_ZONE).format(d);
	}
}
