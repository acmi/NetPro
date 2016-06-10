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
package net.l2emuproject.lang;

/**
 * Specifies priorities of various threads to be used in NetPro.
 * 
 * @author _dev_
 */
public interface NetProThreadPriority
{
	int STARTUP = Thread.MAX_PRIORITY;
	
	int ACCEPTOR_AUTH = Thread.NORM_PRIORITY, CONNECTOR_AUTH = Thread.NORM_PRIORITY, NETWORK_IO_AUTH = Thread.NORM_PRIORITY;
	int ACCEPTOR_GAME = Thread.NORM_PRIORITY, CONNECTOR_GAME = Thread.NORM_PRIORITY, NETWORK_IO_GAME = Thread.NORM_PRIORITY;
	
	int TP_SCHEDULED = Thread.NORM_PRIORITY, TP_INSTANT = Thread.NORM_PRIORITY, TP_LONG = Thread.NORM_PRIORITY;
	int ASYNC_PACKET_NOTIFIER = Thread.NORM_PRIORITY;
	
	int HISTORICAL_LOG_IO = Thread.NORM_PRIORITY;
}
