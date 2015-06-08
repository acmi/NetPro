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
import java.util.List;

/**
 * @author _dev_
 */
@SuppressWarnings("javadoc")
public class UserCmdInterpGenerator extends EntityNameGenerator
{
	public UserCmdInterpGenerator(String... args)
	{
		super("action", "name", "commandname-e.dat.xml", args);
	}
	
	@Override
	public void appendDescription(Writer out, List<String> entity) throws IOException
	{
		out.append('/').append(entity.get(0));
	}
	
	public static void main(String[] args) throws Exception
	{
		final UserCmdInterpGenerator gen = new UserCmdInterpGenerator(args);
		gen.parseClientValues();
		gen.dumpToFile(Paths.get("usercmd.txt"));
	}
}
