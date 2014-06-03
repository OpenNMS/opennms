### OCS Source
_OCS-Inventory NG_ is handling computers and SNMP devices separately in its APIs. For that reason there are two different sources available to import nodes from _OCS_. Some parameters are part of both sources and described first.

#### general ocs parameters
The following parameters are **required**:

* `url` = The _URL_ of the _OCS web application_.
* `username` = A _OCS user_ with rights to access the _OCS Soap interface_.
* `password` = The password for the _OCS user_ with rights to access the _OCS Soap interface_.
* `checksum` = The `ocs.checksum` parameter controls how detailed the data is that the integration is requesting from the _OCS_. It is important to request all the data you want to map into your requisition but not to much, cause a high checksum causes the request to be significantly slower. Read the [OCS Web-Services](http://wiki.ocsinventory-ng.org/index.php/Developers:Web_services) documentation for more information. The default _checksum_ for the _default mapper_ is `4611`.

The following parameters are **optional**:

* `tags` = OCS supports tags / custom fields. If a tag is added to the ocs.tags list, just computers and `snmpDevices` that are marked with all the tags will be read from the _OCS_. This feature can be used to tag computers as `testing` or `production`.
* `target` = This parameters alows to specify a file to write the result of the source to. The resulting xml file can be used for debug or test reasons.

#### source ocs.computers
This source is reading computers from a _OCS instance_. It supports all parameters listed as general and the following additions:

* `accountinfo` = `accountinfo` data is based on custom fields managed in OCS. There are managed by the _Administrative-Data_ section of the _OCS web application_. The name of the custom field is presented in all caps. The value of the field as provided by the user. The `ocs.accountinfo` parameters supports a list of `accountinfo` that has to be present on the computer. If one of the `accountinfo` is not present the computers is skipped. To add multiple `accountinfo` they can be separated with spaces.

#### source ocs.devices
This source is reading `snmpDevices` from a _OCS instance_. It supports all parameters listed as general and no additional at the moment.

#### Mock Sources
For development and testing there are `ocs.computers.replay` and `ocs.devices.replay` sources available. This sources require a file that contains the computers or `snmpDevices` as _XML_ file. The file has also be referenced in the configuration.