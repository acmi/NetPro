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
package net.l2emuproject.proxy;

import net.l2emuproject.L2AutoInitialization;
import net.l2emuproject.proxy.config.ConfigMarker;

/**
 * This class allows to set up configuration, logging, thread pool, data source and other features
 * before entering the {@code main(String[])} method.
 * 
 * @author savormix
 */
public class Config extends L2AutoInitialization
{
	static
	{
		ProxyInfo.showStartupInfo();
		
		initApplication(ConfigMarker.class.getPackage(), L2ProxyThreadPools.class);
	}
}
