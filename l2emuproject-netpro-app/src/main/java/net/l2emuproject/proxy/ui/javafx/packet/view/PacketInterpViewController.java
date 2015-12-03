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
package net.l2emuproject.proxy.ui.javafx.packet.view;

import java.net.URL;
import java.util.ResourceBundle;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;

import net.l2emuproject.lang.L2TextBuilder;
import net.l2emuproject.proxy.ui.savormix.component.packet.DataType;
import net.l2emuproject.proxy.ui.savormix.component.packet.PacketDisplayRawValueLocatorLinkListener;
import net.l2emuproject.util.HexUtil;

import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.web.WebView;

/**
 * @author _dev_
 */
public class PacketInterpViewController implements Initializable
{
	/** Indicates that the interpretation of the packet body should be updated. */
	public static final int FLAG_CONTENT = 1 << 0;
	/** Indicates that the raw packet body display should be updated. */
	public static final int FLAG_BODY = 1 << 1;
	
	private static final String EVENT_CLICK = "click";
	private static final String CONTENT_STYLE, RAW_STYLE;
	
	@FXML
	private WebView _interpretedContentView;
	@FXML
	private WebView _hexOctetView;
	
	@Override
	public void initialize(URL location, ResourceBundle resources)
	{
		final EventListener listener = new PacketDisplayRawValueLocatorLinkListener(_hexOctetView.getEngine());
		_interpretedContentView.getEngine().getLoadWorker().stateProperty().addListener((ov, oldState, newState) ->
		{
			if (newState != Worker.State.SUCCEEDED)
				return;
				
			final Document doc = _interpretedContentView.getEngine().getDocument();
			final NodeList nodes = doc.getElementsByTagName("a");
			for (int i = 0; i < nodes.getLength(); i++)
				((EventTarget)nodes.item(i)).addEventListener(EVENT_CLICK, listener, false);
		});
	}
	
	/**
	 * Updates components related to packet content display.
	 * 
	 * @param content interpretation or {@code null}
	 * @param body raw body or {@code null}
	 * @param flags components to update
	 */
	public void setContent(final String content, final String body, final int flags)
	{
		if ((flags & FLAG_CONTENT) == FLAG_CONTENT)
			_interpretedContentView.getEngine().loadContent(content != null ? CONTENT_STYLE + content : null);
		if ((flags & FLAG_BODY) == FLAG_BODY)
			_hexOctetView.getEngine().loadContent(body != null ? RAW_STYLE + body : null);
	}
	
	static
	{
		final L2TextBuilder cs = new L2TextBuilder("<head><style type=\"text/css\">\r\n");
		{
			cs.append("body {font-family:Tahoma,sans-serif;font-size:small;}\r\n"); // Tahoma is the font used in L2 from CT1
			cs.append("a {text-decoration:none;color:inherit;}\r\n");
			cs.append("font.loopLabel {font-size:x-small;color:#343434;}\r\n");
		}
		CONTENT_STYLE = cs.append("</style></head>\r\n").moveToString();
		final L2TextBuilder bs = new L2TextBuilder("<head><style type=\"text/css\">\r\n");
		{
			bs.append("body {font-family:Consolas,monospace;font-size:small;}\r\n");
			bs.append("a {text-decoration:none;color:inherit;}\r\n");
			for (DataType dt : DataType.values())
			{
				final String color = HexUtil.fillHex(dt.getColor().getRGB() & 0xffffff, 6);
				
				bs.append("font.").append(dt).append('_');
				//bs.append(" {background-color:#").append(color).append(";border:1px #").append(color).append(" solid;}\r\n");
				bs.append(" {background-color:#").append(color).append(";}\r\n");
				
				bs.append("font.").append(dt).append("_selected");
				bs.append(" {background-color:#").append(color).append(";border:1px black solid;}\r\n");
			}
		}
		RAW_STYLE = bs.append("</style></head>\r\n").moveToString();
	}
}
