One-Two Encoding
================

The One-Two encoding is used to represent unsigned integers from 0-7fff. Multi-octet values are stored in little endian order. The low bit (0 bit) of the first (lowest) octet determines how many octets are used to encode the value.



One Octet Form
--------------

| 7 | 6 | 5 | 4 | 3 | 2 | 1 | 0 |
|---|---|---|---|---|---|---|---|
| x | x | x | x | x | x | x | 0 |

Value range: 0-7f



Two Octet Form
--------------

|           LOW OCTET           |          HIGH OCTET           |
| 7 | 6 | 5 | 4 | 3 | 2 | 1 | 0 | 7 | 6 | 5 | 4 | 3 | 2 | 1 | 0 |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| x | x | x | x | x | x | x | 1 | x | x | x | x | x | x | x | x |

Value range: 0-7fff

Decoding is easy with a little endian read provided 1 extra, potentially unrelated octet is always available beyond the first octet:

    masks[] = {0x7f, 0x7fff}
    value = read_16_little_endian()
    value = (value >> 1) & masks[value & 1]
