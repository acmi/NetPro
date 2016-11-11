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
package net.l2emuproject.proxy.script.packets.util;

import net.l2emuproject.proxy.script.ScriptFieldAlias;

/**
 * @author _dev_
 */
public interface ChatHtmlRecipient extends NpcHtmlRecipient
{
	/** Identifies a field that contains quest HTML shown to client. */
	@ScriptFieldAlias
	String QUEST_HTML_CONTENT = "quest_html_content";
	/** Identifies a field that contains quest ID when a quest HTML is shown to client. */
	@ScriptFieldAlias
	String QUEST_HTML_QUEST_ID = "quest_html_quest";
	/** Identifies a field that contains tutorial HTML (or a file name) shown to client. */
	@ScriptFieldAlias
	String TUTORIAL_HTML_CONTENT = "tutorial_html_content";
}
