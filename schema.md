Schema Encoding
===============

Describes data structures, as well as some behavior constraints.



Types
-----

There are 7 basic types, a wildcard type, a none type, and 9 container types. Container types may contain other types (including nested containers).

Type codes fall within the printable ASCII range to make them representable in the most formats.


| Code | ASCII | Type               |
| ---- | ----- | ------------------ |
| 0x6e |   n   | None               |
| 0x61 |   a   | Any Type           |
| 0x62 |   b   | Boolean            |
| 0x69 |   i   | Integer            |
| 0x66 |   f   | Float              |
| 0x64 |   d   | Decimal            |
| 0x44 |   D   | Date               |
| 0x73 |   s   | String             |
| 0x42 |   B   | Bytes              |
| 0x4c |   L   | List               |
| 0x53 |   S   | Set                |
| 0x6f |   o   | Ordered Set        |
| 0x4d |   M   | Map                |
| 0x55 |   U   | Unique map         |
| 0x6d |   m   | Ordered map        |
| 0x75 |   u   | Ordered unique map |
| 0x4f |   O   | Object             |
| 0x45 |   E   | End of Object      |



{
    "name": "get_api_calls",
    "type": "call",
    "version": 0,
    "return": ["list", "call"],
    "parameters":
    [
        {"name": "name",    "type": ["string"],  "flags": ["optional"], "description": "Name search pattern"},
        {"name": "version", "type": ["integer"], "flags": ["optional"], "description": "Only return entries for the specified version number"}
    ],
    "description": "Get some API call schemas",
}


{
    "name": "call",
    "type": "object",
    "fields":
    [
        {"name": "name",        "type": ["string"]},
        {"name": "version",     "type": ["integer"]},
        {"name": "return",      "type": ["list", "type"]},
        {"name": "parameters",  "type": ["list", "parameter"]},
        {"name": "description", "type": ["string"],             "flags": ["optional"]}
    ]
}

{
    "name": "parameter",
    "type": "object",
    "fields":
    [
        {"name": "name",        "type": ["string"]},
        {"name": "type",        "type": ["list", "type"]},
        {"name": "flags",       "type": ["set", "flag"],       "flags": ["optional"]},
        {"name": "constraints", "type": ["set", "constraint"], "flags": ["optional"]},
        {"name": "description", "type": ["string"],            "flags": ["optional"]}
    ]
}




{
    "name": "field",
    "type": "object",
    "fields":
    [
        {"name": "name",        "type": ["string"]},
        {"name": "type",        "type": ["list", "type"]},
        {"name": "flags",       "type": ["set", "flag"],       "flags": ["optional"]},
        {"name": "constraints", "type": ["set", "constraint"], "flags": ["optional"]}
    ]
}

{
    "name": "object",
    "type": "type",
    "fields":
    [
        {"name": "name",        "type": ["string"]},
        {"name": "type",        "type": ["list", "type"]},
        {"name": "fields",      "type": ["list", "field"]},
    ]
}

{
    "name": "enum",
    "type": "type",
    "fields":
    [
        {"name": "name",        "type": ["string"]},
        {"name": "type",        "type": ["list", "type"]},
        {"name": "values",      "type": ["set", "string"]},
    ]
}

{
    "name": "type",
    "type": "enum",
    "values":
    [
        "none",
        "any",
        "boolean",
        "integer",
        "float",
        "decimal",
        "date",
        "string",
        "bytes,
        "list",
        "set",
        "ordered_set",
        "map",
        "unique_map",
        "ordered_map",
        "ordered_unique_map",
        "object"
    ]
}

{
    "name": "flag",
    "type": "enum",
    "values":
    [
        "optional",
        "streamable",
    ]
}

{
    "name": "constraint",
    "type": "enum",
    "values":
    [
        "minimum",
        "maximum"
    ]
}



Containers
----------

### Collection

A collection specification consists of a collection type followed by a specifier for the type it contains.


#### Examples

| Encoded | Meaning           |
| ------- | ----------------- |
| `Sf`   | A set of floats    |
| `La`   | A list of anything |


### Map

A map specification consists of a map type, followed by a key type and a value type.


#### Examples

| Encoded   | Meaning                                          |
| --------- | ------------------------------------------------ |
| `Msi`   | A map of integers keyed by strings                 |
| `miLD` | An ordered map of lists of dates, keyed by integers |


### Object

An object is an ordered collection of named values (fields). A field consists of a type, followed by a null terminated field name.

A name may not contain whitespace or control characters.

list of field


#### Examples

| Encoded                  | Meaning                                                      |
| ------------------------ | ------------------------------------------------------------ |
| `OBsome_field\0E`        | Object with byte field "some_field"                          |
| `OLssome_list\0E`        | Object with list of string field "some_list"                 |
| `Obfield_a\0Dfield_b\0E` | Object with boolean field "field_a" and date field "field_b" |



API Schema
----------

An API schema consists of:

  * Version number (unsigned 8-bit integer)
  * A return type
  * A null terminated function name (UTF-8)
  * An optional null terminated description (UTF-8)
  * A list of parameters

A parameter consists of:

  * A parameter type
  * A null terminated parameter name (UTF-8)
  * flags (optional, streamable)

A name may not contain whitespace or control characters.


#### Examples

| Encoded                                                                  | Call                                                                               |
| ------------------------------------------------------------------------ | ---------------------------------------------------------------------------------- |
| `0x00 b is_uppercase \0 desc \0 s str \0`                                | is_uppercase(string str) -> boolean (version 0)                                    |
| `0x00 n set_value \0 desc \0 s name \0 a value \0`                       | set_value(string name, any value) -> No return type (version 0)                    |
| `0x01 b set_value \0 desc \0 s name \0 a value \0 b replace_existing \0` | set_value(string name, any value, boolean replace_existing) -> boolean (version 1) |
