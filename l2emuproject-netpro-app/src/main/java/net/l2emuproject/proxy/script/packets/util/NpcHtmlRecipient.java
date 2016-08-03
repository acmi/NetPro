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
 * Implement this to receive {@code NpcHtmlMessage} packets.
 * 
 * @author _dev_
 */
public interface NpcHtmlRecipient
{
	/** Associated world object ID */
	@ScriptFieldAlias
	String HTML_OWNER = "html_owner_oid";
	/** Identifies a field with standard NPC/item HTML shown to client. */
	@ScriptFieldAlias
	String HTML_CONTENT = "html_content";
	/** Identifies a field with HTML dialog title. */
	@ScriptFieldAlias
	String HTML_TITLE = "item_html_title";
	/** Identifies if the html is initial char or a reply */
	@ScriptFieldAlias
	String HTML_REPLY = "html_reply_flag";
}
