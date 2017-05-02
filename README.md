# Messenger, Chat on multiple platforms

## Audio Encoding

### OPUS Encoding
* Implementation [http://opus-codec.org]()
* RFC 6176 [https://tools.ietf.org/html/rfc6716]()

## Network

### Structure
```
    ___________UDP___________
    |                       |
Client 1 -TCP-\     /-TCP- Client 2
              |     |
              |     |
          Central server
```

### Audio Call Process
* a normal flow without exception
```
 Client A                      Client B
----------       Server       ----------
  Caller                        Callee
=========================================
1. requestCall
   (CALL_REQ)
              2. createSession
                 initCall
                 (CALL_INIT)
3. createUdpSock             3. createUdpSock
   (U_SRV_ADDR)                 (U_SRV_ADDR)
              4. passPeerAddr
                 (CALL_ADDR)
5. synPeer                   5. synPeer
   (U_SYN)                      (U_SYN)
6. ackPeer                   6. ackPeer
   updateAddr                   updateAddr
   (U_ACK)                      (U_ACK)
7. prepCall                  7. prepCall
   (CALL_PREP)                  (CALL_PREP)
...........................................
                 [WAITING]
...........................................
                             8. ansCall
                                (CALL_ANS)
              9. connectCall
                 (CALL_CONN)
...........................................
                 [CHATTING]
                  (U_DAT)
...........................................
                            10. termCall
                                (CALL_TERM)
                                (U_TERM)
              11. endCall
                  (CALL_END)
```

### P2P over NAT
* Consider to implement some naive handling for NAT problem
* Reference: [P2P NAT](http://www.brynosaurus.com/pub/net/p2pnat/)
* Implementation consideration
  * Check and see if we can just connect (The user did manual port forwarding)
  * Use UPnP and open a port
  * Use some form of hole punching using a public server as the go between
  * Use another peer that does have ports open as a proxy for the data (a Supernode).
  * Use a server I host as a proxy to forward the data.

## Async Operation
http://www.deadcoderising.com/java8-writing-asynchronous-code-with-completablefuture/

## UI Binding 
http://jakewharton.github.io/butterknife/

## Audio Module
`MediaRecorder` DOM API:
[https://developer.mozilla.org/en-US/docs/Web/API/MediaStream_Recording_API/Using_the_MediaStream_Recording_API]()

```bash
sudo apt-get install libasound2-dev
```

### Desktop Client Process Flow
```
           Sender                  Receiver
         Microphone            Headphone/Speaker
 AudioContext |                        |
        [Float32Array]        [AudioBufferSource]
   PcmCodec   |                        |
         [Int16Array]            [AudioBuffer]
 FrameBuffer  |                        |
          [PcmFrame]              [PcmFrame]
   OpusCodec  |                        |
         [OpusFrame]              [OpusFrame]
  AudioPacker |                        |
      [AudioPacketData]        [AudioPacketData] 
 UdpProtocol  |                        |
        [UDP-Packet]             [UDP-Packet]
              |                        |
              \-[UDP Socket & Network]-/
```