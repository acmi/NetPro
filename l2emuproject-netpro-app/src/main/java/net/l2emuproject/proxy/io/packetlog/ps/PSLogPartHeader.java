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
package net.l2emuproject.proxy.io.packetlog.ps;

import java.net.InetAddress;
import java.nio.file.Path;

/**
 * A class that stores a PacketSamurai/YAL packet log header.
 * 
 * @author _dev_
 */
public class PSLogPartHeader
{
	private final Path _logFile;
	private final long _logFileSize;
	private final int _version;
	private final int _packets;
	private final boolean _multipart;
	private final int _partNumber;
	private final int _servicePort;
	private final InetAddress _clientIP, _serverIP;
	private final String _protocolName, _comments, _serverType;
	private final long _analyzerBitset, _sessionID;
	private final boolean _enciphered;
	private final int _protocol, _headerSize;
	
	/**
	 * Constructs this header.
	 * 
	 * @param logFile path to the associated log file
	 * @param logFileSize size of the log file or {@code -1}
	 * @param version log file format version
	 * @param packets amount of packets within the file (-1 for legacy format logs)
	 * @param multipart if the log consists of multiple files
	 * @param partNumber multipart log file index
	 * @param servicePort connection destination port
	 * @param clientIP connection source IP
	 * @param serverIP connection destination IP
	 * @param protocolName user friendly protocol name
	 * @param comments logfile comments
	 * @param serverType unknown
	 * @param analyzerBitset unknown/not used in PS
	 * @param sessionID [connection source port] × [connection destination port]
	 * @param enciphered {@code true} if stream has been pre-deciphered, {@code false} otherwise
	 * @param protocol protocol version or {@code -1}
	 */
	PSLogPartHeader(Path logFile, long logFileSize, int version, int packets, boolean multipart, int partNumber, int servicePort, InetAddress clientIP, InetAddress serverIP,
			String protocolName, String comments, String serverType, long analyzerBitset, long sessionID, boolean enciphered, int protocol)
	{
		_logFile = logFile;
		_logFileSize = logFileSize;
		_version = version;
		_packets = packets;
		_multipart = multipart;
		_partNumber = partNumber;
		_servicePort = servicePort;
		_clientIP = clientIP;
		_serverIP = serverIP;
		_protocolName = protocolName;
		_comments = comments;
		_serverType = serverType;
		_analyzerBitset = analyzerBitset;
		_sessionID = sessionID;
		_enciphered = enciphered;
		_protocol = protocol;
		
		_headerSize = 1 + 4 + 1 + 2 + 2 + 4 + 4 + ((_protocolName.length() + 1) << 1) + ((_comments.length() + 1) << 1) + ((_serverType.length() + 1) << 1) + 8 + 8 + 1;
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
	 * Returns the packet log file format version.
	 * 
	 * @return log file format version
	 */
	public int getVersion()
	{
		return _version;
	}
	
	/**
	 * Returns the amount of packets contained in the associated log file.
	 * 
	 * @return packet count
	 */
	public int getPackets()
	{
		return _packets;
	}
	
	/**
	 * Returns whether this packet log consists of multiple files.
	 * 
	 * @return is one file of many
	 */
	public boolean isMultipart()
	{
		return _multipart;
	}
	
	/**
	 * For a multipart packet log file, this specifies the part number.
	 * 
	 * @return log file index
	 */
	public int getPartNumber()
	{
		return _partNumber;
	}
	
	/**
	 * Returns the connection destination port.
	 * 
	 * @return destination port
	 */
	public int getServicePort()
	{
		return _servicePort;
	}
	
	/**
	 * Returns the connection source address.
	 * 
	 * @return source address
	 */
	public InetAddress getClientIP()
	{
		return _clientIP;
	}
	
	/**
	 * Returns the connection destination address.
	 * 
	 * @return source address
	 */
	public InetAddress getServerIP()
	{
		return _serverIP;
	}
	
	/**
	 * A user-friendly description of the protocol used during the connection.
	 * 
	 * @return protocol name
	 */
	public String getProtocolName()
	{
		return _protocolName;
	}
	
	/**
	 * Additional comments about the connection.
	 * 
	 * @return comments
	 */
	public String getComments()
	{
		return _comments;
	}
	
	/**
	 * Returns unknown string.
	 * 
	 * @return unknown
	 */
	public String getServerType()
	{
		return _serverType;
	}
	
	/**
	 * Returns unknown bitmask.
	 * 
	 * @return unknown
	 */
	public long getAnalyzerBitset()
	{
		return _analyzerBitset;
	}
	
	/**
	 * Returns [connection source port] × {@link #getServicePort()}.
	 * 
	 * @return session ID (unique for unique concurrent sessions on the same machine)
	 */
	public long getSessionID()
	{
		return _sessionID;
	}
	
	/**
	 * Returns {@code false}, if the connection data stream is provided exactly as it was observed, {@code true} if it is aready deciphered.
	 * 
	 * @return is not pre-deciphered
	 */
	public boolean isEnciphered()
	{
		return _enciphered;
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
	
	/**
	 * Returns the size of the log file header (metadata) in bytes.
	 * 
	 * @return header size in bytes
	 */
	public int getHeaderSize()
	{
		return _headerSize;
	}
	
	@Override
	public String toString()
	{
		return _logFile.toString();
	}
}
