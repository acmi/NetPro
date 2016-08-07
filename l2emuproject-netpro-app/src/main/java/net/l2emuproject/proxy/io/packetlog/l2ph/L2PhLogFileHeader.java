/*
 * Copyright (c) 2012-2015, RaveN Network INC. and/or its affiliates. All rights reserved.
 * RAVEN NETWORK INC. PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * 
 * Licensed Materials - Property of RaveN Network INC.
 * Restricted Rights - Use, duplication or disclosure restricted.
 */
package net.l2emuproject.proxy.io.packetlog.l2ph;

import java.nio.file.Path;

import net.l2emuproject.proxy.network.ServiceType;

/**
 * A class that stores the basic information extracted from a l2ph/raw l2ph packet log.
 * 
 * @author _dev_
 */
public class L2PhLogFileHeader
{
	private final Path _logFile;
	private final long _logFileSize;
	private final boolean _raw;
	private final ServiceType _firstPacketServiceType;
	private final long _firstPacketArrivalTime;
	private final int _protocol;
	
	/**
	 * Constructs this header.
	 * 
	 * @param logFile path to the associated log file
	 * @param logFileSize size of the log file or {@code -1}
	 * @param raw {@code true} if raw, {@code false} if standard log
	 * @param firstPacketServiceType service type of the first packet in log file
	 * @param firstPacketArrivalTime timestamp of the first packet in log file
	 * @param protocol network protocol version (-1 if first packet is not {@code SendProtocolVersion})
	 */
	L2PhLogFileHeader(Path logFile, long logFileSize, boolean raw, ServiceType firstPacketServiceType, long firstPacketArrivalTime, int protocol)
	{
		_logFile = logFile;
		_logFileSize = logFileSize;
		_raw = raw;
		_firstPacketServiceType = firstPacketServiceType;
		_firstPacketArrivalTime = firstPacketArrivalTime;
		_protocol = protocol;
	}
	
	/**
	 * Returns the associated historical packet log file.
	 * 
	 * @return path to the associated log file
	 */
	public Path getLogFile()
	{
		return _logFile;
	}
	
	/**
	 * Returns the associated historical packet log file size.
	 * 
	 * @return size of the associated log file or {@code -1}
	 */
	public long getLogFileSize()
	{
		return _logFileSize;
	}
	
	/**
	 * Returns whether the associated log file is a raw log file.
	 * 
	 * @return {@code true} if raw, {@code false} if standard log
	 */
	public boolean isRaw()
	{
		return _raw;
	}
	
	/**
	 * Returns the service type of the packet that is first in the associated log file.
	 * 
	 * @return first log packet service type
	 */
	public ServiceType getFirstPacketServiceType()
	{
		return _firstPacketServiceType;
	}
	
	/**
	 * Returns the reception time of the packet that is first in the associated log file.
	 * 
	 * @return first log packet arrival time
	 */
	public long getFirstPacketArrivalTime()
	{
		return _firstPacketArrivalTime;
	}
	
	/**
	 * Returns the protocol revision number, as negotiated during the connection.
	 * 
	 * @return network protocol version or -1
	 */
	public int getProtocol()
	{
		return _protocol;
	}
}
