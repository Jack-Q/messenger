# Client Server Communication Protocol 

This document list the package format and behavior of ends 
during the communication of the client and server.

As a developing draft, this document is unstable.

## Package Format

### General Package Type

All of the package following the same basic format for checking

```
            0               8              15
            | - - - | - - - | - - - | - - - |
header    00|      0x4a     |      0x51     |
          16|      0x49     |      0x4d     |
ver,type  32|   <version>   |     <type>    |
length    48|        <package-length>       |
payload   64|        <payload-begin>        |
                           ...
    <len>-15|         <payload-end>         |
```


### Specific Payloads


## Timeline and Sequential Convention

