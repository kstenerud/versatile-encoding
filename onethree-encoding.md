One-Three Encoding
==================

The One-Three encoding is used to represent unsigned integers from 0-7fffff. Multi-octet values are stored in little endian order. The low bit (0 bit) of the first (lowest) octet determines how many octets are used to encode the value.



One Octet Form
--------------

| 7 | 6 | 5 | 4 | 3 | 2 | 1 | 0 |
|---|---|---|---|---|---|---|---|
| x | x | x | x | x | x | x | 0 |

Value range: 0-7f



Three Octet Form
----------------

|           LOW OCTET           |           MED OCTET           |          HIGH OCTET           |
| 7 | 6 | 5 | 4 | 3 | 2 | 1 | 0 | 7 | 6 | 5 | 4 | 3 | 2 | 1 | 0 | 7 | 6 | 5 | 4 | 3 | 2 | 1 | 0 |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| x | x | x | x | x | x | x | 1 | x | x | x | x | x | x | x | x | x | x | x | x | x | x | x | x |

Value range: 0-7fffff

Decoding is easy with a little endian read provided 3 extra, potentially unrelated octets are always available beyond the first octet:

    masks[] = {0x7f, 0x7fffff}
    value = read_32_little_endian()
    value = (value >> 1) & masks[value & 1]
