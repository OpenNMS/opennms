### JDBC Source
The jdbc source provides the ability to run an SQL-Query against an external system and interpret the result as an OpenNMS requisition.

    source = jdbc

| parameter       | required | description                                 |
|-----------------|:--------:|--------------------------------------------:|
| driver          | * |JDBC driver, e.g. org.postgresql.Driver             |
| url             | * |JDBC URL, e.g. jdbc:postgresql://host:port/database |
| selectStatement | * |SQL statement |
| user            |   |user name for database connection |
| password        |   |password for database connection |

The following column-headers will be mapped from the result set to the OpenNMS requisitoin:

| column-header    | required | description                        |
|------------------|:--------:|-----------------------------------:|
| Foreign_Id       | *        | will be interpreted as `foreignId` on the node |
| IP_Address       |          | will be interpreted as an IP address as a new interface on the node |
| MgmtType         |          | is interpreted as `snmp-primary` flag and controls how the interface can be used to communicate with the SNMP agent. Valid are `P` (Primary), `S` (Secondary) and `N` (None). |
| InterfaceStatus  |          | will be interpreted as Interface Status. Value has to be an integer. Use `1` for monitored and `3` for not monitored. |
| Node_Label       |          | will be interpreted as node label for the node identified by the `Foreign_Id`|
| Cat              |          | will be interpreted as a surveillance-category for the node identified by the `Foreign_Id`.
| Svc              |          | will be interpreted as a service on the interface of the node identified by the `Foreign_Id` and `IP_Address` field.|

This source also supports all asset-fields by using `Asset_` as a prefix followed by the `asset-field-name`. The city field of the assets can be addressed like this: `yourvalue AS Asset_City`. This is not case-sensitive.

Every row of the result set will be checked for the listed column-headers. The provided data will be added to the corresponding node. Multiple result rows with matching `Foreign_Id` will be added to the corresponding node.
