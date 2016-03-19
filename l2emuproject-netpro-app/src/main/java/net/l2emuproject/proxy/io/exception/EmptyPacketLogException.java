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
package net.l2emuproject.proxy.io.exception;

/**
 * Thrown when an empty packet log is discovered (header+footer OK, but 0 packets).
 * 
 * @author _dev_
 */
public final class EmptyPacketLogException extends Exception
{
	private static final long serialVersionUID = -7232182953722193409L;
	
	/** Constructs this exception. */
	public EmptyPacketLogException()
	{
		super();
	}
}
