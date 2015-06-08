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

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

/**
 * @author _dev_
 */
@SuppressWarnings("javadoc")
public class NpcInterpGenerator extends EntityNameDescGenerator
{
	public NpcInterpGenerator(String... args)
	{
		super("npc_id", Arrays.asList("name", "title"), "npcname-e.dat.xml", args);
	}
	
	@Override
	public void appendDescription(Writer out, List<String> entity) throws IOException
	{
		final String name = entity.get(0);
		out.append(name.isEmpty() ? "[not named in client]" : name);
		
		final String title = entity.get(1);
		if (!title.isEmpty())
			out.append(", ").append(title);
		
		entity.clear();
	}
	
	public static void main(String[] args) throws Exception
	{
		final NpcInterpGenerator gen = new NpcInterpGenerator(args);
		gen.parseClientValues();
		gen.dumpToFile(Paths.get("npc.txt"));
	}
}
