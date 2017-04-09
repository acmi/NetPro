L2EMU-UNIQUE NETWORK PROTOCOL APPLICATION
(FORMERLY L2JFREE PACKET ANALYSIS & VISUALIZATION TOOL)
FOR LINEAGE II
Prelude PTS - Grand Crusade (NA)
VERSION 2.0-SNAPSHOT
March 19, 2013
Revised June 5, 2015

[1] INTRODUCTION
[2] PROTOCOL BACKGROUND
[3] FEATURES
[4] SUPPORT/TROUBLESHOOTING

[1] INTRODUCTION
====================================

An essential part of a successful emulator is to provide all features found in the main (proprietary) implementation. Most of the time, a notable part of newly introduced features cannot be
implemented by reusing previous implementations. For example, before interlude, all skills would have an object as the primary target (assume self for aura). Any new skills would reuse the
existing skill engine. When target-point skills have been introduced, certain adjustments and new implementations had to be made. The same applies to the network protocol. New features are
introduced by modifying existing and adding new packets.
Technically, the client contains code that creates every single "client to server" packet (CM) and code that handles every "server to client" packet (SM). Those can be found in unpacked
versions of nwindow and engine DLLs (usually provided by fyyre). While you can generate opcode to name matches automatically, further inspection requires knowledge about long-obsolete
Unreal Warfare Engine and its adaptation for Lineage II. However, there is an alternative way to figure out the network protocol. If you have a server and a client that both support the
same protocol, you can review packets being transmitted and infer their structure. Moreover, you can force certain situations to happen or inject generated packets to either side and review
the results. This tool provides a convenient way to intercept, review and interpret transmitted data automatically.

I am well aware that people imply that engine.dll contains packet structures. I do not see how people can blatantly ignore the fact that some packets have either missing letters or randomly
inserted letters in these strings. Of course, some packets keep being wrong until someone remembers and fixes the "structure" string. But how can you advertise this as a solution when
you have clear examples, like ItemList BEING ALWAYS WRONG in engine.dll. YOU MUST FACE THE FACTS: these strings are just bundles of random letters. They can help you understand packet
structures, but there is no direct connection between them and the way client interprets packets.
There are further reasons to disregard the textual information found in packets: opcode-only packets without a name are almost invisible (the same applies if someone 'forgets' to add
the structure string for an unnamed packet); packet names are usually misleading (e.g. 'DummyPacket' packet that always had a clear purpose and is still used today) or their name changes
taking years to come into effect, etc.

[2] PROTOCOL BACKGROUND
====================================

This application currently supports the bare Lineage II network protocol. 3rd party protocol wrappers, or 3rd party tools that negotiate the protocol state in parallel connections are not
supported at this time.

As most of your time spent using the tool will be either because a new chronicle is released or because emulator sources contain incomplete old packet structures, it is necessary to know the
basics about how traffic interception is done.

Lineage II uses a custom application-layer network protocol, unlike other, HTTP(S) based MMOs/online games. Data is transmitted in blocks of up to 64K bytes, called packets. A packet's
structure is relatively simple.
HEADER:
2 bytes (unsigned) - packet size, including this header
PAYLOAD:
0 to (64K-2) bytes - transmitted data size

All transmitted data is enciphered. There are different protocol encryption schemes for login and for game server communications.
In order to provide a live view over transmitted data this application intercepts, deciphers, possibly modifies, re-enciphers and forwards all incoming packets from/to both connection
endpoints.

Login
All login server/client packets are encrypted using a modified blowfish scheme. Each Blowfish encrypted block is 64 bits long. 
Once a client connects, server initiates communications by sending an initialization packet. This packet is encrypted with a constant blowfish key (which can be found in the client). What is
important, this packet contains the blowfish key used for further communications. This application automatically extracts the transmitted key and applies it to further communications.

Note: NetPro (just like every other app out there) does not support C4 clean install client login scheme. After all, the original C4 AuthGateD is gone forever and the 656 client seems
hypersensitive to the environment. On top of all this, the C4 GG is blacklisted at nProtect, rendering the unmodified C4 client completely useless (if either any GG file is missing OR
GG is not loaded, the client WILL NOT respond to Auth(Gate)D. So you can move to another revision (e.g. 660) or either disable hypersensitivity or inject previous AuthD/next AuthGateD logic
on top (e.g. L2Gold).

DESPITE the fact that NetPro supports legacy login server protocols, such as 30821, including its heavy modifications in C3 557/560 and fallbacks in NA C4 656, the described scheme is for
MODERN 50721:

First packet from the server:
1. Write packet data
2. Extend packet size to a multiple of 8
3. Encipher packet data by a 32-bit XOR key
4. Append 8 bytes
5. Store XOR key in the first 4 appended bytes
6. Encipher packet using a blowfish key known in advance
7. Send packet

Other packets from the server:
1. Write packet data
2. Extend packet size to a multiple of 8
3. Calculate packet checksum (simple/fast, XOR-based scheme)
4. Append 8 bytes
5. Store checksum in the first 4 appended bytes
6. Encipher packet using the blowfish key stored in the first packet
7. Send packet

Packets from the client:
1. Write packet data
2. Extend packet size to a multiple of 8
3. Calculate packet checksum (simple/fast, XOR-based scheme)
4. Append 16 bytes
5. Store checksum in the first 4 appended bytes
6. Encipher packet using the blowfish key received from the server
7. Send packet

Game
The same scheme (minor differences) was used pre- and post-C4.

All game server/client packets are enciphered using an XOR-based scheme. The initial key is made of two parts: a dynamic part given by the game server and a pre-shared part known to the
game client and server in advance. During cipher operations, the last 4 bytes (DWORD) of the dynamic key part is incremented by the amount of bytes processed.
NOTE: Legacy clients had two pre-shared key parts. The one to be selected was determined by evaluating the dynamic key part sent by server.
Once a client connects, it will immediately send an unenciphered protocol version packet. The server will respond with an unenciphered packet specifying whether the protocol is supported
and disclose the mutable key part. The server, if applicable, will also identify itself and send an initial opcode obfuscation key for the client. If the opcode obfuscation key is not 0,
client will shuffle most 1st and 2nd opcodes. Thus, the obfuscation scheme is simply shuffling opcodes around. From time to time (after a major Lineage II update), new opcodes
are added, but some of the existing ones may be excluded from the obfuscation scheme. When a client sends the protocol version packet, this application will automatically extract the
obfuscation key & protocol version and will select the proper opcode table size(s) & opcode shuffling exclusion list. HOWEVER, even after a minor update, you may find that this application
may default to a different protocol version, because the protocol version disclosed by the client is not yet supported. All you have to do is take note of the client's protocol version and
update all_known_protocols.xml accordingly. If you notice that on every connection, a subset of previously valid CLIENT packets is no longer valid, while a subset of previously invalid
CLIENT packets is now valid once again, you can be certain it is related to opcode shuffling. To help you counter these issues, this application will issue warning messages if you specify
an insufficient opcode table size. You will have to figure out opcodes that are excluded from shuffling by yourself.
NOTE: The CM obfuscation key changes each time a character is logged in.

Except for the first packet, each game server packet is transmitted by taking the following steps:
1. Write packet data
2. Encipher payload using XOR with both parts of the key
3. Update the mutable part of the key
4. Send packet
Except for the first packet, each game client packet is transmitted by taking the following steps:
1. Write packet data
2. Obfuscate opcode(s)
3. Encipher payload using XOR with both parts of the key
3. Update the mutable part of the key
4. Send packet
Game server/client packets are not padded.

In order to successfully intercept transmitted data, the following invasive operations are performed by this application:
Login
1. Blowfish key is extracted from SM SetEncryption packet
2. Game server list type is extracted from CM RequestServerList packet
3. Original SM ServerList packet is dropped; it is replaced by a packet where all game servers specify the IP & port of this application (serviceconfig.xml/<gameWorldSocket>)
4. Selected server ID is extracted from CM RequestServerLogin packet
Game
1. Protocol version is extracted from CM SendProtocolVersion packet
2. XOR and obfuscation keys are extracted from SM VersionCheck packet
3. Obfuscation key is extracted from SM CharacterSelected packet

These can be found in supplied packet definitions by searching for __INVASIVE_AUTO_ script aliases. If they are missing in your packet definitions,
then proxy will use the standard HF/GoD method to read them, which is almost guaranteed to be incompatible with other protocol versions.
NOTE: some packets are already adapted for a broader set of versions

[3] FEATURES
====================================

Incremental packet definition loading
-------------------------------------
The new, one-packet-per-file packet definition loading scheme allows you to define unchanging packets once. For example, NetPing was introduced with C1. You add NetPing.xml to the C1 folder
and keep it there. Prelude versions will not have access to this definition, but all others, starting from C1 update 1, will see this packet. This removes the need for copy-pasting and allows
you to see which packets have changed through time, which helps when maintaining definitions, especially when working on NA and KR definitions in parallel.

Automatic packet logging
------------------------
All packets that have been intercepted will be automatically logged in the [user.home]/l2emu-unique-netpro/packet_logs directory. Additional directories are added to distinguish between
different log types and servers. A packet log file is named according to the following pattern: ([type][server IP]_)[ISO date]_[ti-me]_[5 random symbols].plog
Packet logs are in a proprietary binary format. Application versions released prior to the introduction of the 6th packet log format will not be able to read packet logs with version >= 6.
The packet log format will be documented further once the application matures. Points of notice: versions 1 to 4 use the same structure. Version 5 contains minor improvements. Version 6 log
contains major improvements and breaks backward compatibility, while introducing it for later versions in a more convenient form.

Packet log review
-----------------
All automatically logged packets may be later reviewed via the NetPro UI. You may select to view only valid logs or logs that include certain packets.

Displayed packet management
---------------------------
You can select which packets should be visible in the packet log list. Invisible packets remain in cache and will be added back to the list as necessary. Moreover, you can clear the list by
clearing all visible packets or clearing the cache (which will clear both).
Clearing the cache is not allowed for reviewed packet log files - consider closing the tab instead.

Packet injection
----------------
During live interception, you can choose to send manually crafted packets. Conversion options and direction selection are provided. Obfuscation is performed and header is attached
automatically.

Network protocol and automatic interpretation
-----------------------------------------------
This application allows you to define protocol versions and assign packet definitions. Code modification is not required to support upcoming protocol versions.
Protocol versions are defined in ./config/packets/all_known_protocols.xml. Default packet definitions can be found in ./config/packets/.
XML definition format can be inferred from supplied schemata. Packet definitions may be reloaded at runtime.
In the XML definition file, each element may be assigned a 'type' attribute. The value of this attribute is a class name (relative path from net.l2emuproject.proxy.ui.interpreter), which must
implement the ValueInterpreter interface. See javadoc for details. Custom interpreters can be freely used; compile them and add to classpath.
Similarly, modifiers (the most notable being 'subtract one million to get the NPC ID') and conditions (see <branch> in XML schema) can be used/extended.
An interpreter takes an element from the packet payload and returns a RenderedImage (special case) or a string that may contain basic HTML tags. Further returned types may be supported later;
unsupported types will simply be taken as string (String#valueOf).

Analytics
---------
NetPro will maintain a substate of the game world (as reported by the game server) to help identify objects by their unique IDs. This allows to successfully examine packets such as
StatusUpdate without having to track those IDs manually. Pledge/alliance IDs and crests are tracked as well.

Proxy-only and UI-only modes
----------------------------
This application may run with or without UI. If you prefer to gather logs before reviewing, you may want to run this application as a proxy on a server-type/headless OS and later view those
logs without initializing a listener. Otherwise, you can use the default mode and review packets/update definitions as you play/test.

[4] SUPPORT/TROUBLESHOOTING
====================================
There are no FAQs... yet!

If you have ideas for additional highly useful features, would like to start writing your own UI/logging or have general questions, do not hesitate to contact:
[Just write a message in BitBucket]

Where required, please state the reason (NetPro) for me to add you.

However, do not contact me under these circumstances:
1. You cannot phrase your question in English
2. After reading this file, you still have no idea how this application works
3. You want to port/use this tool for another MMO
4. You want me to write code for your emulator
5. You will ask me to help understand current Lineage II protocol version, but all you want is me to write code for your emulator
6. You want to me to find cheats/exploits for you or help you find them
