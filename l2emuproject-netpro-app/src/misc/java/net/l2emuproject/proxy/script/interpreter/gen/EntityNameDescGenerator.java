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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.w3c.dom.Node;

import net.l2emuproject.util.L2XMLUtils;

/**
 * @author _dev_
 */
@SuppressWarnings("javadoc")
public class EntityNameDescGenerator extends EntityInterpGenerator<List<String>>
{
	protected final String _idElem;
	protected final List<String> _tokenElems;
	
	public EntityNameDescGenerator(String idElementName, List<String> tokenElementNames, String defaultFile, String... args)
	{
		super(defaultFile, args);
		
		_idElem = idElementName;
		_tokenElems = tokenElementNames;
	}
	
	@Override
	public void processEntry(Node entry)
	{
		final Integer k = L2XMLUtils.getFirstChildIntegerContent(entry, _idElem);
		
		final List<String> tokens = new ArrayList<String>(_tokenElems.size());
		for (final String elem : _tokenElems)
			tokens.add(L2XMLUtils.getFirstChildStringContent(entry, elem, "").trim());
		
		_descriptions.put(k, tokens);
	}
	
	@Override
	public void appendDescription(Writer out, List<String> entity) throws IOException
	{
		final Iterator<String> it = entity.iterator();
		out.write(it.next());
		while (it.hasNext())
		{
			final String ex = it.next();
			if (!ex.trim().isEmpty())
				out.append(' ').append(ex);
		}
	}
}
