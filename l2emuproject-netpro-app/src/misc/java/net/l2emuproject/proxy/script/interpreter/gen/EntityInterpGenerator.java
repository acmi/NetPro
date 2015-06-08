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
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import net.l2emuproject.util.L2XMLUtils;

/**
 * @author _dev_
 * @param <E> entity description type
 */
@SuppressWarnings("javadoc")
public abstract class EntityInterpGenerator<E>
{
	private final Path _xmlFile;
	protected final String _separator;
	
	protected final Map<Integer, E> _descriptions;
	
	public EntityInterpGenerator(String defaultFile, String... args)
	{
		_xmlFile = Paths.get(args.length > 0 ? args[0] : defaultFile);
		_separator = args.length > 1 ? args[1] : "\t";
		
		_descriptions = new TreeMap<Integer, E>();
	}
	
	public void parseClientValues() throws Exception
	{
		final Document doc = L2XMLUtils.getXMLFile(_xmlFile);
		final Node list = L2XMLUtils.getChildNodeByName(doc, "entries");
		for (Node entry : L2XMLUtils.listNodesByNodeName(list, "entry"))
			processEntry(entry);
	}
	
	public void dumpToFile(Path file) throws IOException
	{
		try (final Writer out = Files.newBufferedWriter(file, Charset.forName("UTF-8")))
		{
			for (Entry<Integer, E> e : _descriptions.entrySet())
			{
				out.append(e.getKey().toString()).append(_separator);
				appendDescription(out, e.getValue());
				out.append("\r\n");
			}
		}
	}
	
	public abstract void processEntry(Node entry);
	
	public abstract void appendDescription(Writer out, E entity) throws IOException;
}
