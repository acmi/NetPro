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
package net.l2emuproject.proxy.network.game;

import java.net.InetAddress;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import net.l2emuproject.util.concurrent.L2ThreadPool;
import net.l2emuproject.util.logging.L2Logger;

/**
 * Tracks what game servers have been selected by which addresses to properly redirect newly connecting game clients.
 * 
 * @author savormix
 */
public class L2SessionManager
{
	private final Lock _authorizedSessionLock;
	private NewGameServerConnection _authorizedSession;
	private ScheduledFuture<?> _sessionExpiryTask;
	
	L2SessionManager()
	{
		_authorizedSessionLock = new ReentrantLock();
		_authorizedSession = null;
		_sessionExpiryTask = null;
	}
	
	/**
	 * Takes an authorized session details for the given client IP address.
	 * 
	 * @param clientAddress client IP
	 * @return authorized session details or {@code null}
	 */
	public NewGameServerConnection getAuthorizedSession(InetAddress clientAddress)
	{
		_authorizedSessionLock.lock();
		try
		{
			if (_authorizedSession == null)
				return null;
			
			if (!_authorizedSession.getAuthorizedClientAddress().equals(clientAddress))
				return null;
			
			final NewGameServerConnection result = _authorizedSession;
			_authorizedSession = null;
			_sessionExpiryTask.cancel(true);
			_sessionExpiryTask = null;
			return result;
		}
		finally
		{
			_authorizedSessionLock.unlock();
		}
	}
	
	/**
	 * Registers an authorized session for the taking.
	 * 
	 * @param authorizedSession authorized session details
	 * @return {@code true} if registered, {@code false} if there is a pending authorized session
	 */
	public boolean setAuthorizedSession(NewGameServerConnection authorizedSession)
	{
		_authorizedSessionLock.lock();
		try
		{
			if (_authorizedSession != null)
				return false;
			
			_authorizedSession = authorizedSession;
			_sessionExpiryTask = L2ThreadPool.schedule(() -> {
				try
				{
					_authorizedSessionLock.lockInterruptibly();
					try
					{
						L2Logger.getLogger(getClass()).info("Expired session: " + _authorizedSession);
						_authorizedSession = null;
						_sessionExpiryTask = null;
					}
					finally
					{
						_authorizedSessionLock.unlock();
					}
				}
				catch (final InterruptedException e)
				{
					// cancelled while waiting to discard active session
				}
			}, 6, TimeUnit.SECONDS);
			return true;
		}
		finally
		{
			_authorizedSessionLock.unlock();
		}
	}
	
	/**
	 * Returns a singleton instance of this type.
	 * 
	 * @return an instance of this class
	 */
	public static final L2SessionManager getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static final class SingletonHolder
	{
		static final L2SessionManager INSTANCE = new L2SessionManager();
	}
}
