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


| Data Type         | Code | Payload                                                |
| ----------------- | ---- | ------------------------------------------------------ |
| Bytes             | 0x77 | (length) (octets)                                      |
| String            | 0x78 | (length) (octets: UTF-8 encoding, no BOM)              |
| Date              | 0x79 | (length) (octets: ISO 8601 encoding with timezone)     |
| List (container)  | 0x7a | [object] ... (end of container)                        |
| Map (container)   | 0x7b | [ (key object) (value object) ] ... (end of container) |
| End of Container  | 0x7c |                                                        |
| True              | 0x7d |                                                        |
| False             | 0x7e |                                                        |
| Empty (no data)   | 0x7f |                                                        |
| Integer (small)   |  **  |                                                        |
| Integer (16 bit)  | 0x80 | (16-bit two's complement integer, little endian)       |
| Integer (32 bit)  | 0x81 | (32-bit two's complement integer, little endian)       |
| Integer (64-bit)  | 0x82 | (64-bit two's complement integer, little endian)       |
| Integer (128-bit) | 0x83 | (128-bit two's complement integer, little endian)      |
| Float (32-bit)    | 0x84 | (IEEE 754 binary32, little endian)                     |
| Float (64-bit)    | 0x85 | (IEEE 754 binary64, little endian)                     |
| Float (128-bit)   | 0x86 | (IEEE 754 binary128, little endian)                    |
| Decimal (32-bit)  | 0x87 | (IEEE 754 decimal32, little endian)                    |
| Decimal (64-bit)  | 0x88 | (IEEE 754 decimal64, little endian)                    |
| Decimal (128-bit) | 0x89 | (IEEE 754 decimal128, little endian)                   |

  * ()     = required information
  * []     = optional information
  * ...    = zero or more of the previous item
  * object = any data type except EMPTY
  * length = any positive integer type representing the number of octets to follow

The small integer type encompasses all values not used by other type codes, and is interpreted as a two's complement signed 8-bit integer representing values from -118 (0x8a) to 118 (0x76).



Illegal Encodings
-----------------

Illegal encodings must not be used, as they will cause problems or even API violations in certain languages. A decoder may discard illegal encodings.

  * Dates must not be ambiguous. Dates without timezone information are not allowed.
  * The EMPTY type must not be used in a container.
  * EMPTY, MAP, and LIST types must not be used as a key in a map.
  * Maps must not contain duplicate keys. Keys in a map must not resolve to the same value (for example: 1000 encoded as int32, and 1000 encoded as int56).



Discouraged Encodings
---------------------

Discouraged encodings are allowed, but discouraged in order to promote maximum portability. A decoder may discard discouraged encodings if they cannot be accurately represented.

  * Maps should not contain keys of different major types (for example: integers and strings). Mixing minor types (for example: int32 and int48) is allowed and encouraged. A decoder should harmonize minor types if they would cause problems in the implementing language or environment.



Smallest Possible Size
----------------------

For efficiency's sake, when encoding, the encoder should first determine the smallest data type that will encode without data loss. Note that it is NOT an error to use a larger data type. On some systems, restricting to certain larger data types may increase processing efficiency at the cost of transmission size, and so this is exlicitly allowed. Decoders must be able to handle any data type in this specification.
