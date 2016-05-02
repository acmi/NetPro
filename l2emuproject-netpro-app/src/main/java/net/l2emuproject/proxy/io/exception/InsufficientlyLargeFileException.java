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
 * Reports a file that is not even large enough to contain the magic value (and perhaps other mandatory header fields).
 * 
 * @author _dev_
 */
public final class InsufficientlyLargeFileException extends Exception
{
	private static final long serialVersionUID = -8021493507274867323L;
	
	/** Creates this exception. */
	public InsufficientlyLargeFileException()
	{
		super();
	}
}
