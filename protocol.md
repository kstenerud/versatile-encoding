Protocol
========

This document desribes the protocol format.



Encoding
--------

The protocol uses various binary data encodings:

  * onetwo-encoding.md
  * onethree-encoding.md
  * versatile-encoding.md


### Message Encoding

    (contents length) (message type) [parameter] ...

  * contents length: onethree
  * message type: onetwo
  * parameter: versatile



Protocol
--------

| Client Message  | Server Response             |
| --------------- | --------------------------- |
| Request         | Status, Resource, Exception |
| Resource        | Exception                   |


Resource ID placeholders


Type.BOOLEAN
Type.LIST.content(Type.INTEGER)
Type.MAP.key(Type.STRING).value(Type.ANY)
