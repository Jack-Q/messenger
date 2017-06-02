# Peer-to-peer Communication Protocol 

This document list the packet format and behavior of applications
during the communication between peer to peer.

As a developing draft, this document is unstable.

## General Packet Type

All of the packet conforming the same basic format:

```
[Packet Type][Packet Content]
    1 byte
```

The format of `Packet Content` is defined by specific `Packet Type`.

## Specific Packet Content Format

### Atomic String Format

For UDP packet reachability detection and connection (virtual) management, the packet 
content is consisted of an atomic string encoded in UTF-8 representing the audio session
identifer.

* Packet Types:
  * `U_SYN`(`0x01`): Synchronization request;
  * `U_ACK`(`0x02`): Acknowledgement to synchronization request;
  * `U_END`(`0x04`): End of UDP connection (virtual);
* Example:
```
Hex: 01 48 79 5a 30 50 62 31 4d 5a
      |  \----------V------------/
      |   Audio Session ID (HyZ0Pb1MZ)
      |
      \-- Packet type: U_SYN (0x01)
```

### Compound Data Format

Current version of protocol defines 1 type of data.

* Packet Type: `U_DAT`(`0x03`): Data transfer;
* Data Types:
  * `audio`: audio data, defined as 
    ```
    [Packet Type][Session Id][Separator][Data Type][Separator][Audio Frame]
       1 byte    string(utf8)  1 byte     string     1 byte     binary
        0x03                    0x3a                  0x3a
    ```

    The `Audio Frame` is defined as:
    ```
    [Sequence Number][Opus Encoded]
    2 bytes Little Endian
    ```

### Implementation

* JavaScript: [udp-protocol.js](../server/src/lib/udp-protocol.js)
* Java: [PeerProtocol.java](../android/app/src/main/java/cn/jackq/messenger/network/protocol/PeerProtocol.java)