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
package net.l2emuproject.proxy.script;

import eu.revengineer.simplejse.logging.GenericLogger;

import net.l2emuproject.util.logging.L2Logger;

final class NetProScriptLog implements GenericLogger
{
	private final L2Logger _log;
	
	NetProScriptLog(L2Logger log)
	{
		_log = log;
	}
	
	@Override
	public boolean isDebugEnabled()
	{
		return _log.isDebugEnabled();
	}
	
	@Override
	public void debug(Object message, Throwable exception)
	{
		_log.debug(message, exception);
	}
	
	@Override
	public void error(Object message, Throwable exception)
	{
		_log.error(message, exception);
	}
	
	@Override
	public void fatal(Object message, Throwable exception)
	{
		_log.fatal(message, exception);
	}
	
	@Override
	public void info(Object message, Throwable exception)
	{
		_log.info(message, exception);
	}
	
	@Override
	public boolean isTraceEnabled()
	{
		return _log.isTraceEnabled();
	}
	
	@Override
	public void trace(Object message, Throwable exception)
	{
		_log.trace(message, exception);
	}
	
	@Override
	public void warn(Object message, Throwable exception)
	{
		_log.warn(message, exception);
	}
}
