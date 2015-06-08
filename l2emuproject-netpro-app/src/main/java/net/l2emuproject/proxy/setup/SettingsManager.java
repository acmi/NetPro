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
package net.l2emuproject.proxy.setup;

import javax.swing.UIManager;

/**
 * Not implemented yet.
 * 
 * @author _dev_
 */
public final class SettingsManager
{
	SettingsManager()
	{
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * Returns the user-selected Look and Feel class name.
	 * 
	 * @return LnF class name
	 */
	public String getLookAndFeelName()
	{
		// TODO: implement
		return UIManager.getSystemLookAndFeelClassName();
	}
	
	/**
	 * Returns a singleton object.
	 * 
	 * @return an instance of this class
	 */
	public static SettingsManager getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static final class SingletonHolder
	{
		static final SettingsManager INSTANCE = new SettingsManager();
	}
}
