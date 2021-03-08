# Minion Configuration Schema
The minion configuration schema describes all the configuration parameters that Minion allows to be set by container overlay via confd.
## Purpose
The purpose of the configuration schema is the following:
* Generate documentation regarding what configuration options the Minion container supports
* Act as a source for data-driven UI for setting Minion configuration
## Layout
The schema document is broken down into categories which can be nested.

Categories have the following mandatory fields:
* name
* label
* description
* config

The `config` field is a list that has at least one item.

Each item in a list has the following mandatory fields:
* name
* label
* description
* example
* type
* key

`type`s have at least a name field and may also optionally have the following fields:
* validation
* items (in the case the type is a collection)
## Input to Minion
Ultimately the configuration that Minion's confd templates expect is a set of key-value pairs. All configuration generated based on the schema must reduce down to a set of key-value pairs. All arrays and maps must be flattened as per the rules associated with the appropriate type to produce a set of key-value pairs.
## Types
### integer
A raw integer number.

Accepts `validation` in the form of a string `"N..M"` where `N` is the lower bound inclusive and `M` is the upper bound inclusive (the upper bound can be omitted, ex `0..` for any positive integer).

`N..M` forms a range where `N` <= `X` <= `M`

`N..` forms a range where `X` >= `N`

examples:
```
42
```
```
-100
```
### decimal
A raw decimal number.

Accepts `validation` in the form of a string `"N..M"` where `N` is the lower bound inclusive and `M` is the upper bound inclusive (the upper bound can be omitted, ex `0.0..` for any positive decimal).

`N..M` forms a range where `N` <= `X` <= `M`

`N..` forms a range where `X` >= `N`

examples:
```
50.42
```
```
-99.99
```
### host
A valid hostname such as a FQDN or IP address.

examples:
```
abc.def.com
```
```
1.2.3.4
```
### port
A valid port which is a number between 0 and 65535.

example:
```
8080
```
### host-with-port
A host and a port joined by a `:` character.

example:
```
abc.def.com:8080
```
### properties
A collection of key-value pairs. The keys are used to suffix the path, and the value is used as the value for that full path.

example:
The following collection of key-value pairs (properties) for the key '/foo/bar':
```
my-prop my-val
anotherProp 1234
```
Would translate to the following keys and values:
```
"/foo/bar/my-prop": "my-val"
"/foo/bar/anotherProp": 1234
```
### collection
A collection can be used to associate many value to one key. The flattened set of key-values is based on the collection's strategy.

In the case of the `concat` strategy, all the values are joined into a single string using a given 'separator' character.

Given the following values with the key `/foo/bar` and the strategy `concat` with separator `,` results in the following set of key-values.

Values:
```
val-1
val-2
val-3
```

Key-values:
```
"/foo/bar": "val-1,val-2,val-3"
```

In the case of the `indexed` strategy, all the values are set as separate keys using their order index as a key suffix to form the key's path.

Given the above values with the key `/foo/bar` and the strategy `indexed` results in the following set of key-values.

Key-values:
```
"/foo/bar/0": "val-1"
"/foo/bar/1": "val-2"
"/foo/bar/2": "val-3"
```
### string
A string is freeform it one it allows subject to optional validation being specified as part of its config.
### boolean
Valid values are `true` and `false`.
### objects
Objects allows many keys (which we can call fields) to be associated with a single key (representing the object). The key paths end up reducing down to a path which is the base key suffixed by some index followed by the field's key.

The index suffix is based on an indexing strategy referred to as the `index-type`. The only strategy in use currently is `field` where the index is derived from the given fields value.

Given a base key of `/foo/bar` and an `index-type` of `field` with the `field-name` set to `myField`, an object with the following fields would reduce down to the following key-values:

Fields:
```
myField: "kiwi"
anotherField: 1234
yetAnotherField: "minions!"
```

Reduced key-values:
```
"/foo/bar/kiwi/myField": "kiwi"
"/foo/bar/kiwi/anotherField": 1234
"/foo/bar/kiwi/yetAnotherField": "minions!"
```

The `objects` type allows an arbitrary number of objects to be associated with the given base key.
