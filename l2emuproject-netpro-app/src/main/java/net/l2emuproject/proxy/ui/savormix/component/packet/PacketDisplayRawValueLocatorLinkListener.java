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
package net.l2emuproject.proxy.ui.savormix.component.packet;

import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Element;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;

import net.l2emuproject.lang.L2TextBuilder;

import javafx.scene.web.WebEngine;

/**
 * Handles hyperlink events, locates requested values in binary form.
 * 
 * @author _dev_
 */
final class PacketDisplayRawValueLocatorLinkListener implements EventListener
{
	private final WebEngine _engine;
	
	PacketDisplayRawValueLocatorLinkListener(WebEngine engine)
	{
		_engine = engine;
	}
	
	@Override
	public void handleEvent(Event ev)
	{
		//String domEventType = ev.getType();
		//if (!domEventType.equals(EVENT_CLICK))
		//	return;
		
		final String href = ((Element)ev.getTarget()).getAttribute("href");
		if (StringUtils.isEmpty(href))
			return;
		
		final String[] args = href.split("__");
		
		final L2TextBuilder tb = new L2TextBuilder();
		tb.append("function endsWith(str, suffix){return str.indexOf(suffix, str.length - suffix.length) !== -1;}");
		tb.append("function findPos(obj){var curtop = 0;if(obj.offsetParent){do{curtop += obj.offsetTop;}");
		tb.append("while (obj = obj.offsetParent);return [curtop];}}\r\n");
		tb.append("var fields = document.getElementsByTagName(\"font\");");
		tb.append("var cnt = 0;");
		tb.append("for(var i = 0; i < fields.length; i++){\r\n");
		tb.append("var cls = fields[i].getAttribute(\"class\");\r\n");
		tb.append("if (endsWith(cls, \"_selected\")) {\r\n");
		tb.append("fields[i].setAttribute(\"class\", cls.substr(0, cls.length - \"selected\".length));\r\n");
		tb.append("} if (cls.lastIndexOf(\"").append(args[0]).append("_\", 0) === 0){\r\n");
		tb.append("if (++cnt == ").append(args[1]).append("){\r\n");
		tb.append("fields[i].setAttribute(\"class\", \"").append(args[0]).append("_selected\");\r\n");
		tb.append("fields[i].scrollIntoView(true);");
		tb.append("}}}");
		_engine.executeScript(tb.moveToString());
	}
}
