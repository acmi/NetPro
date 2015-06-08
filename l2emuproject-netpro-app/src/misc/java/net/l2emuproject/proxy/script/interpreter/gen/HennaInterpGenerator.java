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
import java.util.Arrays;

/**
 * @author _dev_
 */
@SuppressWarnings("javadoc")
public class HennaInterpGenerator extends EntityNameDescGenerator
{
	public HennaInterpGenerator(String... args)
	{
		super("id", Arrays.asList("name", "symbol_add_name"), "hennagrp-e.dat.xml", args);
	}
	
	public static void main(String[] args) throws Exception
	{
		final HennaInterpGenerator gen = new HennaInterpGenerator(args);
		gen.parseClientValues();
		gen.dumpToFile(Paths.get("henna.txt"));
	}
}
