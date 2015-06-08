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
package net.l2emuproject.proxy.script.interpreter.gen;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Node;

import net.l2emuproject.util.L2XMLUtils;

/**
 * @author _dev_
 */
@SuppressWarnings("javadoc")
public class ServerInterpGenerator extends EntityNameGenerator
{
	public ServerInterpGenerator(String... args)
	{
		super("id", "name", "servername-e.dat.xml", args);
	}
	
	@Override
	public void processEntry(Node entry)
	{
		final Integer k = L2XMLUtils.getFirstChildIntegerContent(entry, _idElem) + 1; // should add the 2nd dword instead
		
		final List<String> tokens = new ArrayList<String>(_tokenElems.size());
		for (final String elem : _tokenElems)
			tokens.add(L2XMLUtils.getFirstChildStringContent(entry, elem, ""));
		
		_descriptions.put(k, tokens);
	}
	
	public static void main(String[] args) throws Exception
	{
		final ServerInterpGenerator gen = new ServerInterpGenerator(args);
		gen.parseClientValues();
		gen.dumpToFile(Paths.get("server.txt"));
	}
}
