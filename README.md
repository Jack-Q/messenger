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