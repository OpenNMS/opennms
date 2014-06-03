### XLS Source
The `xls` reads a _XLS_ spreadsheet file and creates an _OpenNMS requisition_ based on the worksheet content.

    source = xls

| parameter | required  | description                                     |
|-----------|:---------:|------------------------------------------------:|
| file      | *         | path of the xls file to read                    |
| encoding  |           | encoding of the xls file. Default is ISO-8859-1 |

The structure of the spreadsheet has to follow this rules. The source is reading from a sheet named like the requisition you are requesting. The first row of the sheet is reserved to column-names. This column-names have to start with certain prefixes to be recognized.

| prefixes        | required | description                        |
|-----------------|:--------:|-----------------------------------:|
| Node_           | *        | will be interpreted as node label and `foreignId`. |
| IP_             | *        | will be interpreted as an IP address as a new interface on the node. |
| MgmtType_       | *        | is interpreted as `snmp-primary` flag and controls how the interface can be used to communicate with the SNMP agent. Valid are `P` (Primary), `S` (Secondary) and `N` (None). |
| InterfaceStatus |          | will be interpreted as interface status. This is NOT a prefix. The name has to match. Use `1` for monitored and `3` for not monitored. |
| cat_            |          | will be interpreted as a surveillance-category. Multiple comma separated categories can be provided. It can be used multiple times per sheet. |
| svc_            |          | will be interpreted as a service on the interface of the node. Multiple comma separated services can be provided. It can be used multiple times per sheet. |

This source also supports all asset-fields by using `Asset_` as a prefix followed by the `asset-field-name`. The city field of the assets can be addressed like this: `Asset_City`. This is not case-sensitive.

To add a node with multiple interfaces, add an additional sequent row with the same nodelabel (Node_). This row will be added as a new interface based on the data from the  IP_, MgmtType_, svc_ columns.

The order in which the columns are arranged is irrelevant. Also additional columns can be present.