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
package net.l2emuproject.proxy.script.interpreter.gen.ex;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import net.l2emuproject.proxy.script.interpreter.gen.QuestNameInterpGenerator;
import net.l2emuproject.proxy.script.interpreter.gen.QuestStateInterpGenerator;

/**
 * @author _dev_
 */
@SuppressWarnings("javadoc")
public final class CompleteQuestInterpGenerator
{
	public static void main(String[] args) throws Exception
	{
		{
			QuestNameInterpGenerator gen = new QuestNameInterpGenerator();
			Files.copy(Paths.get("HF_questname-e.dat.xml"), Paths.get("questname-e.dat.xml"), StandardCopyOption.REPLACE_EXISTING);
			gen.parseClientValues();
			Files.copy(Paths.get("NEW_questname-e.dat.xml"), Paths.get("questname-e.dat.xml"), StandardCopyOption.REPLACE_EXISTING);
			gen.parseClientValues();
			gen.dumpToFile(Paths.get("quest.txt"));
		}
		{
			QuestStateInterpGenerator gen = new QuestStateInterpGenerator();
			Files.copy(Paths.get("HF_questname-e.dat.xml"), Paths.get("questname-e.dat.xml"), StandardCopyOption.REPLACE_EXISTING);
			gen.parseClientValues();
			Files.copy(Paths.get("NEW_questname-e.dat.xml"), Paths.get("questname-e.dat.xml"), StandardCopyOption.REPLACE_EXISTING);
			gen.parseClientValues();
			gen.dumpToFile(Paths.get("qstate.txt"));
		}
		Files.deleteIfExists(Paths.get("questname-e.dat.xml"));
	}
}
