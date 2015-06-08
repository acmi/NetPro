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
package net.l2emuproject.proxy.config;

import net.l2emuproject.L2Initialization.ConfigPropertiesLoader;
import net.l2emuproject.config.annotation.ConfigClass;
import net.l2emuproject.config.annotation.ConfigField;
import net.l2emuproject.config.annotation.ConfigGroupBeginning;
import net.l2emuproject.config.annotation.ConfigGroupEnding;

/**
 * L2EmuProject Packet Analysis &amp; Visualization Tool configuration provider.
 * 
 * @author savormix
 */
@ConfigClass(fileName = "proxy", comment = { "Copyright 2011-2015 L2EMU UNIQUE", "", "Licensed under the Apache License, Version 2.0 (the \"License\");", "you may not use this file except in compliance with the License.", "You may obtain a copy of the License at", "", "    http://www.apache.org/licenses/LICENSE-2.0", "", "Unless required by applicable law or agreed to in writing, software", "distributed under the License is distributed on an \"AS IS\" BASIS,", "WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.", "See the License for the specific language governing permissions and", "limitations under the License." })
public final class ProxyConfig extends ConfigPropertiesLoader
{
	/** RW thread sleep time for login client connections. */
	@ConfigGroupBeginning(name = "Performance")
	@ConfigField(name = "PacketIOIntervalForLoginClients", value = "100", eternal = true, comment = { "How often (ms) to do I/O for packets sent by/to login (authd) clients.", "A relatively high value (like the default one) can be safely used here." })
	public static int RW_SELECTOR_INTERVAL_LC;
	/** RW thread sleep time for login server connections. */
	@ConfigField(name = "PacketIOIntervalForLoginServers", value = "100", eternal = true, comment = { "How often (ms) to do I/O for packets sent by/to login (authd) servers.", "A relatively high value (like the default one) can be safely used here." })
	public static int RW_SELECTOR_INTERVAL_LS;
	/** RW thread sleep time for game client connections. */
	@ConfigField(name = "PacketIOIntervalForGameClients", value = "3", eternal = true, comment = { "How often (ms) to do I/O for packets sent by/to game clients.", "Directly affects how fast packets are forwarded through the proxy.", "The value should generally be as low as your CPU can handle." })
	public static int RW_SELECTOR_INTERVAL_GC;
	/** RW thread sleep time for game server connections. */
	@ConfigField(name = "PacketIOIntervalForGameServers", value = "3", eternal = true, comment = { "How often (ms) to do I/O for packets sent by/to game clients.", "Directly affects how fast packets are forwarded through the proxy.", "The value should generally be as low as your CPU can handle." })
	public static int RW_SELECTOR_INTERVAL_GS;
	
	/** Acceptor thread sleep time for login client connections. */
	@ConfigField(name = "ConnectionIntervalForLoginClients", value = "2500", eternal = true, comment = { "How often (ms) to accept pending connections from login (authd) clients.", "In order not to waste CPU cycles, this should be set just a little below the built-in client's connection timeout." })
	public static int ACC_SELECTOR_INTERVAL_LOGIN;
	/** Acceptor thread sleep time for game client connections. */
	@ConfigField(name = "ConnectionIntervalForGameClients", value = "750", eternal = true, comment = { "How often (ms) to accept pending connections from game clients.", "In order not to waste CPU cycles, this should be set just a little below the built-in client's connection timeout." })
	@ConfigGroupEnding(name = "Performance")
	public static int ACC_SELECTOR_INTERVAL_GAME;
	
	/** Whether the proxy should read scripts from cache, if one is present. */
	@ConfigGroupBeginning(name = "Debug")
	@ConfigField(name = "DisableScriptCache", value = "false", eternal = true, comment = { "Whether to ignore the precompiled script cache, even though that will increase the application load time.", "The obvious advantadge is that you will not need to keep deleting the script cache after making changes in script source files." })
	public static boolean DISABLE_SCRIPT_CACHE;
	
	/** Whether to enable partial MMOCore activity logging. */
	@ConfigField(name = "MMOCoreDebug", value = "false", eternal = false, comment = "Whether to enable a part of MMOCore debug logging")
	@ConfigGroupEnding(name = "Debug")
	public static boolean MMO_DEBUG;
	
	/** Whether to open tabs for login connections. */
	@ConfigGroupBeginning(name = "GUI")
	@ConfigField(name = "NoTabsForLoginConnections", value = "false", eternal = false, comment = { "If true, only connections to game servers will open tabs in the GUI, pretty much like in l2phx (you can still open login packet logs as usual).", "If false, every incoming connection will automatically open a tab in the GUI, regardless of type." })
	public static boolean NO_TABS_FOR_LOGIN_CONNECTIONS;
	
	/** Amount of packets to be read from file before yielding CPU time */
	@ConfigField(name = "PacketLogLoadCpuYieldThreshold", value = "0", eternal = false, comment = { "Specifies the amount of packets read from disk, after which CPU time will be yielded for other tasks.", "0 disables yielding and will deliver the fastest loading performance. The lower the value, the slower packets will be read from disk.", "Only enable this feature if you experience system-wide stuttering during packet loading (excessive DPC latency in Win32 terms)." })
	public static int PACKET_LOG_LOADING_CPU_YIELD_THRESHOLD;
	/** Amount of CPU time to give away */
	@ConfigField(name = "PacketLogLoadCpuYieldDuration", value = "10", eternal = false, comment = { "Specifies the amount of time, in milliseconds, to yield the CPU for once the threshold is reached.", "Has no effect if threshold is zero. The higher the value, the slower packets will be read from disk." })
	@ConfigGroupEnding(name = "GUI")
	public static int PACKET_LOG_LOADING_CPU_YIELD_DURATION;
}
