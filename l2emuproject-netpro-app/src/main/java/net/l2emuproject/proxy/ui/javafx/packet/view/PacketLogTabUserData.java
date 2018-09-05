/*
 * Copyright 2011-2017 L2EMU UNIQUE
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

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;

import javafx.beans.property.StringProperty;
import net.l2emuproject.proxy.network.Proxy;

/**
 * @author _dev_
 */
public final class PacketLogTabUserData
{
	private final PacketLogTabController _controller;
	private Reference<Proxy> _client, _server;
	private boolean _captureDisabled, _online;
	private final StringProperty _charNameProperty;
	
	public PacketLogTabUserData(PacketLogTabController controller, StringProperty charNameProperty)
	{
		_controller = controller;
		_charNameProperty = charNameProperty;
	}
	
	public Proxy getClient()
	{
		return _client != null ? _client.get() : null;
	}
	
	public void setClient(Proxy client)
	{
		_client = new WeakReference<>(client);
	}
	
	public Proxy getServer()
	{
		return _server != null ? _server.get() : null;
	}
	
	public void setServer(Proxy server)
	{
		_server = new WeakReference<>(server);
	}
	
	public boolean isCaptureDisabled()
	{
		return _captureDisabled;
	}
	
	public void setCaptureDisabled(boolean captureDisabled)
	{
		_captureDisabled = captureDisabled;
	}
	
	public boolean isOnline()
	{
		return _online;
	}
	
	public void setOnline(boolean online)
	{
		_online = online;
	}
	
	public PacketLogTabController getController()
	{
		return _controller;
	}
	
	public StringProperty charNameProperty()
	{
		return _charNameProperty;
	}
}
