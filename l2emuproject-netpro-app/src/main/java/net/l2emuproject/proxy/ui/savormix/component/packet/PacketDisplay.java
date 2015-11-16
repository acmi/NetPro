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

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.Collections;
import java.util.Set;

import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.web.WebView;

import javax.swing.JSplitPane;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;

import net.l2emuproject.lang.L2TextBuilder;
import net.l2emuproject.network.protocol.IProtocolVersion;
import net.l2emuproject.proxy.state.entity.context.ICacheServerID;
import net.l2emuproject.proxy.ui.ReceivedPacket;
import net.l2emuproject.proxy.ui.savormix.io.ImageUrlUtils;
import net.l2emuproject.ui.AsyncTask;
import net.l2emuproject.util.HexUtil;

/**
 * Displays a packet in two text boxes: one for raw hex octets delimited by spaces, another for a
 * field-value list based on the packet's structure definition. <BR>
 * <BR>
 * This class is a part of a reference GUI provided for education purposes only. These classes
 * should help to understand how to interact with the underlying proxy core. <BR>
 * <BR>
 * Creating an own GUI is the preferred way to start using this application.
 * 
 * @author savormix
 */
@SuppressWarnings("restriction")
public class PacketDisplay extends JSplitPane
{
	private static final long serialVersionUID = -4507197091627669116L;
	private static final String EVENT_CLICK = "click";
	
	private IProtocolVersion _protocol;
	private ICacheServerID _entityCacheContext;
	
	// special case
	int _serverListType;
	
	final String _contentStyle, _bodyStyle;
	WebView _contentDisplay, _bodyDisplay;
	String _body;
	
	private AsyncTask<?, ?, ?> _displayTask;
	private Reference<ReceivedPacket> _currentlyVisiblePacket;
	private Set<URL> _displayedImages;
	
	/**
	 * Creates a split pane to display packet body and contents.<BR>
	 * <BR>
	 * If using as a standalone component, it is mandatory to call {@link #setProtocol(IProtocolVersion)} and {@link #setCacheContext(ICacheServerID)} before executing
	 * a packet display task.
	 * 
	 * @param owner associated packet list (possibly {@code null})
	 */
	public PacketDisplay(PacketList owner)
	{
		super(JSplitPane.VERTICAL_SPLIT, true);
		
		if (owner != null)
		{
			_protocol = owner.getProtocol();
			_entityCacheContext = owner.getCacheContext();
		}
		
		setResizeWeight(0.75);
		
		final L2TextBuilder cs = new L2TextBuilder("<head><style type=\"text/css\">\r\n");
		{
			cs.append("body {font-family:Tahoma,sans-serif;font-size:small;}\r\n"); // Tahoma is the font used in L2 from CT1
			cs.append("a {text-decoration:none;color:inherit;}\r\n");
			cs.append("font.loopLabel {font-size:x-small;color:#343434;}\r\n");
		}
		_contentStyle = cs.append("</style></head>\r\n").moveToString();
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
		_bodyStyle = bs.append("</style></head>\r\n").moveToString();
		
		Platform.runLater(new Runnable()
		{
			final JFXPanel _tp, _bp;
			{
				setTopComponent(_tp = new JFXPanel());
				setBottomComponent(_bp = new JFXPanel());
			}
			
			@Override
			public void run()
			{
				{
					_bodyDisplay = new WebView();
					_bp.setScene(new Scene(_bodyDisplay));
				}
				
				final EventListener listener = new PacketDisplayRawValueLocatorLinkListener(_bodyDisplay.getEngine());
				{
					_contentDisplay = new WebView();
					_contentDisplay.getEngine().getLoadWorker().stateProperty().addListener((ov, oldState, newState) ->
					{
						if (newState != Worker.State.SUCCEEDED)
							return;
						
						final Document doc = _contentDisplay.getEngine().getDocument();
						final NodeList nodes = doc.getElementsByTagName("a");
						for (int i = 0; i < nodes.getLength(); i++)
							((EventTarget)nodes.item(i)).addEventListener(EVENT_CLICK, listener, false);
					});
					_tp.setScene(new Scene(_contentDisplay));
				}
			}
		});
		
		_displayedImages = Collections.emptySet();
		
		_currentlyVisiblePacket = new WeakReference<>(null);
	}
	
	/** Indicates that the interpretation of the packet body should be updated. */
	public static final int FLAG_CONTENT = 1 << 0;
	/** Indicates that the raw packet body display should be updated. */
	public static final int FLAG_BODY = 1 << 1;
	
	/**
	 * Updates components related to packet content display.
	 * 
	 * @param content interpretation or {@code null}
	 * @param body raw body or {@code null}
	 * @param flags components to update
	 */
	public void setContent(final String content, final String body, final int flags)
	{
		Platform.runLater(() ->
		{
			if ((flags & FLAG_CONTENT) == FLAG_CONTENT)
			{
				_contentDisplay.getEngine().loadContent(content != null ? _contentStyle + content : null);
			}
			if ((flags & FLAG_BODY) == FLAG_BODY)
			{
				_bodyDisplay.getEngine().loadContent(body != null ? _bodyStyle + body : null);
			}
		});
	}
	
	/**
	 * Changes the currently displayed packet.
	 * 
	 * @param packet packet body wrapper or {@code null}
	 */
	public void displayPacket(ReceivedPacket packet)
	{
		if (_displayTask != null)
		{
			_displayTask.cancel(true);
			_displayTask = null;
		}
		
		if (packet == null)
		{
			_currentlyVisiblePacket.clear();
			setContent(null, null, FLAG_CONTENT | FLAG_BODY);
			return;
		}
		
		if (_currentlyVisiblePacket.get() == packet)
			return;
		
		_currentlyVisiblePacket = new WeakReference<>(packet);
		_displayTask = new PacketDisplayTask(this).execute(packet);
	}
	
	/**
	 * Dissociates currently active display task from this component, if applicable.
	 * 
	 * @param task currently active task
	 */
	public void unsetDisplayTask(PacketDisplayTask task)
	{
		if (_displayTask == task)
			_displayTask = null;
	}
	
	@Override
	public void finalize() throws Throwable
	{
		super.finalize();
		
		releaseImages();
	}
	
	/** Releases resources associated with this component. */
	public void onRemove()
	{
		setTopComponent(null);
		setBottomComponent(null);
		
		releaseImages();
	}
	
	private void releaseImages()
	{
		releaseImages(Collections.emptySet());
	}
	
	/**
	 * Removes all files created to display the previously selected packet and marks {@code newSet} as files created for the currently selected packet.
	 * 
	 * @param newSet temp files associated with the currently displayed packet
	 */
	public void releaseImages(Set<URL> newSet)
	{
		ImageUrlUtils.getInstance().release(_displayedImages);
		_displayedImages = newSet;
	}
	
	/**
	 * Returns the network protocol version used to create the packet content interpretation.
	 * 
	 * @return network protocol version
	 */
	public IProtocolVersion getProtocol()
	{
		return _protocol;
	}
	
	/**
	 * Sets the network protocol version to use when creating the packet content interpretation.
	 * 
	 * @param protocol network protocol version
	 */
	public void setProtocol(IProtocolVersion protocol)
	{
		_protocol = protocol;
	}
	
	/**
	 * Returns the associated entity existence boundary defining context, used when creating the packet content interpretation.
	 * 
	 * @return entity context
	 */
	public ICacheServerID getCacheContext()
	{
		return _entityCacheContext;
	}
	
	/**
	 * Sets the associated entity existence boundary defining context to use when creating the packet content interpretation.
	 * 
	 * @param cacheContext entity context
	 */
	public void setCacheContext(ICacheServerID cacheContext)
	{
		_entityCacheContext = cacheContext;
	}
	
	@SuppressWarnings("javadoc")
	public int getServerListType()
	{
		return _serverListType;
	}
}
