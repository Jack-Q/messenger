# Client Server Communication Protocol 

This document list the package format and behavior of applications
during the communication of the client and server.

As a developing draft, this document is unstable.

## General Package Type

All of the package following the same basic format for checking

```
            0               8              15
            | - - - | - - - | - - - | - - - |
header    00|      0x4a     |      0x51     |
          16|      0x49     |      0x4d     |
ver,type  32|   <version>   |     <type>    |
length    48|         <packet-length>       |
payload   64|         <payload-begin>       |
                           ...
    <len>-15|         <payload-end>         |
```

* `header`: 4 magic bytes indicating the packet may generated from a peer conforming 
            this series of protocol. This field won't be changed in later version;
* `version`: 1 byte indicating the version of protocol. 
            Current version byte is `0x81` (in hexadecimal);
* `type`: 1 byte indicating the type of payload. 
          All valid values are defined and enumerated in next section;
* `packet-length`: the length of the whole packet including the header 
                   fields (i.e. header, version, type, and length). 
                   This field is measured in byte and represented in little endian byte order.
* `payload`: the data payload of current packet. 

Note: 
Current version of protocol provides no security layer, which may 
later be included into the packet format by wrapping the payload.

## Specific Payloads Format

For simple or atomic message transmission, the payload maybe raw string 
encoded in UTF-8, while to support more extensibility, compound messages
are encapsulated by standard JSON format with extra defined schemas.

01. `SERVER_CHECK`(`0x01`)

    * **Sender**: Client
    * **Description**: Check the status of server. This should be the first packet sent by client after establishment of TCP connection to server.
    * **Format**: No specific requirement defined. The length of this field must no longer then 30 bytes.
    * **Sample**:
        ```txt
        PING
        ```

02. `SERVER_STATUS`(`0x02`)

    * **Sender**: Server
    * **Description**: A short description of server. This should be sent by server as the response to packet `SERVER_CHECK` from specific client.
    * **Format**: No specific requirement defined. The length of this field must no longer then 30 bytes.
    * **Sample**:
        ```txt
        Default Localhost Server
        ```

03. `USER_ADD_REQ`(`0x03`)

    * **Sender**: Client
    * **Description**: Request to reserve an identifier (i.e. user name) with token. 
                Any identifiers must be reserved before first activation.
    * **Format**: JSON format. 
        ```typescript
        {
            n: string, // User name as identifier
            t: string, // User token for authentication
        }
        ```
    * **Sample**:
        ```json
        {"n":"JackQ","t":"sesame"}
        ```

04. `USER_ADD_RESP`(`0x04`)

    * **Sender**: Server
    * **Description**: Response to identifier reservation. 
                Response status (`s`) of `true` for successful operation.
                Otherwise, response status (`s`) of `false` for any failures.
                This operation only registers identifier with given token. The identifier is not activated without explicit login 
                request (`USER_LOGIN_REQ`).
    * **Format**: JSON format.
        ```typescript
        {
            s: boolean, // Server Response Status
            m: string,  // Server Message 
        }
        ```
    * **Sample**:
        ```json
        {"s":true,"m":"ok"}
        ```

05. `USER_LOGIN_REQ`(`0x05`)

    * **Sender**: Client
    * **Description**: Request to active an identifier (i.e. user name) 
                with token. Any identifiers must be reserved before first activation.
    * **Format**: JSON format. (identical to `USER_ADD_REQ`)
        ```typescript
        {
            n: string, // User name as identifier
            t: string, // User token for authentication
        }
        ```
    * **Sample**:
        ```json
        {"n":"JackQ","t":"sesame"}
        ```

06. `USER_LOGIN_RESP`(`0x06`)

    * **Sender**: Server
    * **Description**: Response to identifier activation. 
                Response status (`s`) of `true` for successful operation.
                Otherwise, response status (`s`) of `false` for any failures.
                A connection identifier of string must be included for 
                successful operation.
    * **Format**: JSON format.
        ```typescript
        {
            s: boolean, // Server Response Status
            m: string,  // Server Message 
            k: string,  // Connection Identifier
        }
        ```
    * **Sample**:
        ```json
        {"s":true,"m":"ok","k":"r1nArgyMb"}
        ```

07. `INFO_QUERY`(`0x07`)

    * **Sender**: Client
    * **Description**: Initiate a query of certain type of information from                         server. Current version only defines `buddy-list` query                      with no query parameter.
    * **Format**: JSON format.
        ```typescript
        {
            q: "buddy-list", // Type of query
            p: object,       // Parameters of query, defined by specific type of query 
        }
        ```
      Subtypes and parameters:
      * `buddy-list`: `p: {}` (no parameter)

    * **Sample**:
        ```json
        {"q":"buddy-list","p":{}}
        ```

08. `INFO_RESP`(`0x08`)

    * **Sender**: Server
    * **Description**: Send information to client (no requirement for precedent                     request from client)
                Response status (`s`) of `true` for successful operation.
                Otherwise, response status (`s`) of `false` for any failures.
                A connection identifier of string must be included for 
                successful operation.
    * **Format**: JSON format.
        ```typescript
        {
            s: boolean, // Server Response Status
            m: string,  // Server Message 
            t: string,  // Type of query
            p: object|array, // Payload, defined by specific type of query
        }
        ```
        For 
        * `buddy-list`:
            ```
            [{
                id: string,   // Connection Identifier
                name: string, // User name
                ip: string,   // IP address
            }]
            ```
    * **Sample**:
        ```json
        {"s":true,"m":"ok","p":[{"id":"S1MPxbJGZ","name":"JackQ","ip":"127.0.0.1"}],"t":"buddy-list"}
        ```

09. `MSG_SEND`(`0x09`)

    * **Sender**: Client
    * **Description**: Send a plain text message to activated peer.
    * **Format**: JSON format.
        ```typescript
        {
            u: string, // User name of peer
            c: string, // Connection identifier of peer
            m: string, // Content of message
        }
        ```

    * **Sample**:
        ```json
        {"u":"JackQ","c":"S1MPxbJGZ","m":"Hello!"}
        ```

10. `MSG_RECV`(`0x0a`)

    * **Sender**: Server
    * **Description**: Push message to client (after server received message     
                       from another client).
    * **Format**: JSON format.
        ```typescript
        {
            s: boolean, // Server Response Status
            m: string,  // Server Message for failure; Peer message for success
            u: string,  // User name of peer
            c: string,  // Connection Identifier of peer
        }
        ```
    * **Sample**:
        ```json
        {"s":true,"u":"JackQ","c":"S1MPxbJGZ","m":"Hi!"}
        ```

11. `CALL_REQ`(`0x11`)

    * **Sender**: Client (Caller)
    * **Description**: Request an audio session.
    * **Format**: JSON format.
        ```typescript
        {
            u: string, // User name of peer
            c: string, // Connection identifier of peer
        }
        ```

    * **Sample**:
        ```json
        {"u":"jack","c":"S1MPxbJGZ"}
        ```

12. `CALL_INIT`(`0x12`)

    * **Sender**: Server
    * **Description**: Initiate audio session. This packet should be sent to
                       caller and callee, respectively.
    * **Format**: JSON format.
        ```typescript
        {
            s: boolean, // Server Response Status
            m: string,  // Server Message
            i: string,  // Session identifier
            a: string,  // Host name of server (UDP service)
            p: number,  // UDP port number of server
            f: string,  // Connection identifier of caller (only included in packet sent to callee)
        }
        ```
    * **Sample**:
        ```json
        {"s":true,"m":"ok","i":"ByP-O-kGZ","a":"192.168.1.102","p":36360}
        ```


13. `CALL_ADDR`(`0x13`)

    * **Sender**: Server
    * **Description**: The UDP address (host and port) of peer.
                       This packet should be sent to caller and callee, respectively.
    * **Format**: JSON format.
        ```typescript
        {
            s: boolean, // Server Response Status
            m: string,  // Server Message
            i: string,  // Session identifier
            a: string,  // Host name of server (UDP service)
            p: number,  // UDP port number of server
        }
        ```
    * **Sample**:
        ```json
        {"s":true,"m":"ok","i":"ByP-O-kGZ","a":"192.168.1.220","p":32122}
        ```


14. `CALL_PREP`(`0x14`)

    * **Sender**: Client (Caller and callee)
    * **Description**: Indicating the establishment of peer-to-peer UDP 
                       communication connection (acknowledgement of UDP
                       packet received).
    * **Format**: JSON format.
        ```typescript
        {
            i: string,  // Session identifier
        }
        ```

    * **Sample**:
        ```json
        {"i":"ByP-O-kGZ"}
        ```

15. `CALL_ANS`(`0x15`)

    * **Sender**: Client (Callee)
    * **Description**: Callee accepts the audio session request.
    * **Format**: JSON format.
        ```typescript
        {
            i: string,  // Session identifier
        }
        ```

    * **Sample**:
        ```json
        {"i":"ByP-O-kGZ"}
        ```

16. `CALL_CONN`(`0x16`)

    * **Sender**: Server
    * **Description**: Connection of audio session.
                       This packet should be sent to caller and callee, respectively.
    * **Format**: JSON format.
        ```typescript
        {
            s: boolean, // Server Response Status
            m: string,  // Server Message
            i: string,  // Session identifier
        }
        ```
    * **Sample**:
        ```json
        {"s":true,"m":"ok","i":"ByP-O-kGZ"}
        ```


17. `CALL_TERM`(`0x17`)

    * **Sender**: Client (Caller or callee)
    * **Description**: Terminate an audio session. This packet may be sent by
                       either caller or callee.
    * **Format**: JSON format.
        ```typescript
        {
            i: string,  // Session identifier
        }
        ```

    * **Sample**:
        ```json
        {"i":"ByP-O-kGZ"}
        ```
18. `CALL_END`(`0x18`)

    * **Sender**: Server
    * **Description**: End of audio session.
                       This packet should be sent to caller and callee, respectively.
    * **Format**: JSON format.
        ```typescript
        {
            s: boolean, // Server Response Status
            m: string,  // Server Message
            i: string,  // Session identifier
        }
        ```
    * **Sample**:
        ```json
        {"s":true,"m":"ok","i":"ByP-O-kGZ"}
        ```


## Timeline and Sequential Convention

