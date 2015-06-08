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
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.w3c.dom.Node;

import net.l2emuproject.util.L2XMLUtils;

/**
 * @author _dev_
 */
@SuppressWarnings("javadoc")
public class SkillLevelInterpGenerator extends EntityInterpGenerator<Map<Integer, String>>
{
	public SkillLevelInterpGenerator(String... args)
	{
		super("skillname-e.dat.xml", args);
	}
	
	@Override
	public void processEntry(Node entry)
	{
		final Integer k1 = L2XMLUtils.getFirstChildIntegerContent(entry, "skill_id");
		final Integer k2 = L2XMLUtils.getFirstChildIntegerContent(entry, "skill_level");
		
		String name = L2XMLUtils.getFirstChildStringContent(entry, "name");
		{
			final String desc = L2XMLUtils.getFirstChildStringContent(entry, "desc_add1", "");
			if (!desc.isEmpty())
				name += " " + desc;
		}
		
		Map<Integer, String> map = _descriptions.get(k1);
		if (map == null)
		{
			map = new TreeMap<>();
			_descriptions.put(k1, map);
		}
		map.put(k2, name);
	}
	
	@Override
	public void dumpToFile(Path file) throws IOException
	{
		try (final Writer out = Files.newBufferedWriter(file, Charset.forName("UTF-8")))
		{
			for (Entry<Integer, Map<Integer, String>> e : _descriptions.entrySet())
			{
				for (Entry<Integer, String> e2 : e.getValue().entrySet())
				{
					out.append(e.getKey().toString()).append(_separator);
					appendDescription(out, Collections.singletonMap(e2.getKey(), e2.getValue()));
					out.append("\r\n");
				}
			}
		}
	}
	
	@Override
	public void appendDescription(Writer out, Map<Integer, String> entity) throws IOException
	{
		final Entry<Integer, String> e = entity.entrySet().iterator().next();
		out.append(e.getKey().toString()).append(_separator).append(e.getValue());
	}
	
	public static void main(String[] args) throws Exception
	{
		final SkillLevelInterpGenerator gen = new SkillLevelInterpGenerator(args);
		gen.parseClientValues();
		gen.dumpToFile(Paths.get("skill.txt"));
	}
}
