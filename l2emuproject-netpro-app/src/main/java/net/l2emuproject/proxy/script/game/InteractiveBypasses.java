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
package net.l2emuproject.proxy.script.game;

import net.l2emuproject.proxy.script.ScriptFieldAlias;

/**
 * Allows a script to handle user input from HTML and other bypasses.
 * 
 * @author _dev_
 */
public interface InteractiveBypasses
{
	/** This is a normal HTML bypass */
	@ScriptFieldAlias
	String STANDARD_BYPASS = "INTERACTIVE_BYPASS";
	/** This is a HTML bypass from the tutorial window */
	@ScriptFieldAlias
	String TUTORIAL_BYPASS = "INTERACTIVE_TUTORIAL_BYPASS";
	/** This is a builder command (whatever trails {@code //}) */
	@ScriptFieldAlias
	String BUILDER_BYPASS = "INTERACTIVE_BUILDER_BYPASS";
}
