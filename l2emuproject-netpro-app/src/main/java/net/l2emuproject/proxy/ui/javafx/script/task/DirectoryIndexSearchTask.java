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
package net.l2emuproject.proxy.ui.javafx.script.task;

import java.util.Set;
import java.util.TreeSet;

import net.l2emuproject.proxy.script.NetProScriptCache;

import javafx.concurrent.Task;

/**
 * Searches the file system for script classes to be [re]loaded, given substrings of their FQCNs.
 * 
 * @author _dev_
 */
public final class DirectoryIndexSearchTask extends Task<Set<String>>
{
	private final String _fqcnPart;
	
	/**
	 * Constructs this task.
	 * 
	 * @param fqcnPart script name substring
	 */
	public DirectoryIndexSearchTask(String fqcnPart)
	{
		_fqcnPart = fqcnPart;
	}
	
	@Override
	protected Set<String> call() throws Exception
	{
		// Return a sorted view
		return new TreeSet<>(NetProScriptCache.getInstance().findIndexedScripts(_fqcnPart, Integer.MAX_VALUE, 0));
	}
}
