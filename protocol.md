Protocol
========

This document desribes the protocol format.

Encoding Methods
--------------------

The protocol uses various binary encoding methods, based on little endian since it is the most common native encoding format in use today.


### One-Two

The One-Two encoding is used to represent unsigned integers from 0-7fff. The low bit (S bit) of the first octet determines how many octets are used to encode the value:

#### One octet form:

| 6 | 5 | 4 | 3 | 2 | 1 | 0 | S |
| - | - | - | - | - | - | - | - |
| x | x | x | x | x | x | x | 0 |

Value range: 0-7f


#### Two octet form:

|  6 |  5 |  4 |  3 |  2 |  1 |  0 |  S |
|  - |  - |  - |  - |  - |  - |  - |  - |
|  x |  x |  x |  x |  x |  x |  x |  1 |
| 14 | 13 | 12 | 11 | 10 |  9 |  8 |  7 |
|  x |  x |  x |  x |  x |  x |  x |  x |

Value range: 0-7fff

Decoding is easy with a little endian read (provided 1 extra, unrelated octet is always available beyond the encoded value):

    masks[] = {0x7f, 0x7fff}
    value = read_16_little_endian()
    value = (value >> 1) & masks[value & 1]



### One-Three

The One-Three encoding is used to represent unsigned integers from 0-7fffff. The low bit (S bit) of the first octet determines how many octets are used to encode the value:

#### One octet form:

| 6 | 5 | 4 | 3 | 2 | 1 | 0 | S |
| - | - | - | - | - | - | - | - |
| x | x | x | x | x | x | x | 0 |

Value range: 0-7f


#### Three octet form:

|  6 |  5 |  4 |  3 |  2 |  1 |  0 |  S |
|  - |  - |  - |  - |  - |  - |  - |  - |
|  x |  x |  x |  x |  x |  x |  x |  1 |
| 14 | 13 | 12 | 11 | 10 |  9 |  8 |  7 |
|  x |  x |  x |  x |  x |  x |  x |  x |
| 22 | 21 | 20 | 19 | 18 | 17 | 16 | 15 |
|  x |  x |  x |  x |  x |  x |  x |  x |

Value range: 0-7fffff

Decoding is easy with a little endian read (provided 3 extra, unrelated octes are always available beyond the encoded value):

    masks[] = {0x7f, 0x7fffff}
    value = read_32_little_endian()
    value = (value >> 1) & masks[value & 1]



### Versatile Encoding

The versatile encoding is designed to allow efficient packing of ad-hoc data, either standalone, or stored in lists or maps.

Encoding is designed to favor small integer data types, as they tend to be used more often in practice.

| Code | Meaning |
| ---- | ------- |
|  8a  | Empty (no data) |
|  89  | Float (IEEE 754 binary32) |
