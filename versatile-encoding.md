Versatile Encoding
==================

Versatile encoding is designed with the following points of focus:

  * A general purpose encoding for a large number of applications
  * Supports the most common data types
  * Supports hierarchical data structuring
  * Space efficiency
  * Computational efficiency
  * Readability directly off the wire (especially on little endian systems)

The encoding is binary, and favors small integer data types, as smaller values tend to be used more often in practice.

Container types (list, map) can contain other objects, allowing for hierarchical data representation.



Data Types
----------

Data to be encoded must be of a well defined type. This encoding supports most major data types including strings, dates, numbers, booleans, containers, as well as raw bytes.


### Encoding

All data are encoded with a data type code, followed by a payload if the data type requires it. Little endian byte order is used for numeric types.

| Data Type           | Code | Payload                                                |
| ------------------- | ---- | ------------------------------------------------------ |
| Decimal (64-bit-B)  | 0x76 | (IEEE 754 decimal64, Binary Integer Decimal)           |
| Decimal (64-bit-D)  | 0x77 | (IEEE 754 decimal64, Densely Packed Decimal)           |
| Decimal (128-bit-B) | 0x78 | (IEEE 754 decimal128, Binary Integer Decimal)          |
| Decimal (128-bit-D) | 0x79 | (IEEE 754 decimal128, Densely Packed Decimal)          |
| Bytes               | 0x7a | (length) (octets)                                      |
| String              | 0x7b | (length) (octets: UTF-8 encoding, no BOM)              |
| Date                | 0x7c | (length) (octets: ISO 8601 encoding with timezone)     |
| True                | 0x7d |                                                        |
| False               | 0x7e |                                                        |
| Empty (no data)     | 0x7f |                                                        |
| End of Container    | 0x80 |                                                        |
| List (container)    | 0x81 | [object] ... (end of container)                        |
| Map (container)     | 0x82 | [ (key object) (value object) ] ... (end of container) |
| Float (32-bit)      | 0x83 | (IEEE 754 binary32)                                    |
| Float (64-bit)      | 0x84 | (IEEE 754 binary64)                                    |
| Float (128-bit)     | 0x85 | (IEEE 754 binary128)                                   |
| Integer (16 bit)    | 0x86 | (16-bit two's complement integer)                      |
| Integer (32 bit)    | 0x87 | (32-bit two's complement integer)                      |
| Integer (64-bit)    | 0x88 | (64-bit two's complement integer)                      |
| Integer (128-bit)   | 0x89 | (128-bit two's complement integer)                     |
| Integer (small)     |  **  |                                                        |


  * ()     = required information
  * []     = optional information
  * ...    = zero or more of the previous item
  * object = any data type except EMPTY
  * length = any positive integer type representing the number of octets to follow

The small integer type encompasses all values not used by other type codes, and is interpreted as a two's complement signed 8-bit integer representing values from -118 (0x8a) to 117 (0x75).



Illegal Encodings
-----------------

Illegal encodings must not be used, as they will cause problems or even API violations in certain languages. A decoder may discard illegal encodings.

  * Dates must not be ambiguous. Dates without timezone information are not allowed.
  * The EMPTY type must not be used in a container.
  * EMPTY, MAP, and LIST types must not be used as a key in a map.
  * Maps must not contain duplicate keys. Keys in a map must not resolve to the same value (for example: 1000 encoded as int32, and 1000 encoded as int64).
  * Map keys must all be of the same type.



Smallest Possible Size
----------------------

For efficiency's sake, when encoding, the encoder should first determine the smallest data type that will encode without data loss. Note that it is NOT an error to use a larger data type. On some systems, restricting to certain larger data types may increase processing efficiency at the cost of transmission size, and so this is exlicitly allowed. Decoders must be able to handle any data type in this specification.
