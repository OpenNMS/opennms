#### OCS Mappers
Mappers are used to map the OCS data model with computers and SNMP devices to the OpenNMS data model for provisioning with nodes, interfaces, services and assets. The OCS integration provides one default mapper for computers and one for snmp-devices out of the box. Additionally it provides script based mapping via the script mapper. The default mappers for OCS are a simple way to map computers and snmp-devices to OpenNMS nodes.

##### Computers
To use this mapper, configure your requisition config to use "ocs.computers" as mapper. This mapper requires a checksum of 4611 to get all required data. It elects one of the ip-addresses of a computer to be the management-interface of the node. This is controlled by the black- and whitelisting. The default ip-filter is used for the election. If no interface is valid, the node will have no interfaces and a corresponding log message is written. The elected management-interface is enriched with the interface description, if available. The SNMP and ICMP service are forced to the management-interface. Additionally the comment field of the node assets are used to provide a html link to the computer-page of the ocs instance. The assets for cpu and operationgSystem will be mapped from the OCS computer too. The computer name is used as foreignId and nodeLable.

###### CategoryMap
The Default mapper for Computers supports a mapping between OCS Accountinfo data from OCS to OpenNMS surveillance-categories. To use this feature add the categoryMap parameter to the requisition.properties file and reference a properties file following this syntax example:
 
     ADMINISTRATIVEDATAFILEDNAME.data=OpenNMSCategoryName
     ENVIRONMENT.Production=Production JOB.Mailserver=Mail

##### snmp-devices
To use this mapper, configure your requisition config to use `ocs.devices` as mapper. This mapper requires a `checksum` of `4099`. It validates the IP address of the `snmpDevice` verses the black- and whitelists. For the election of the default an IP filter can be used. If the IP address of the `snmpDevice` is _blocked_ a log message is written and the node will not have any interfaces. The interface has assigned _ICMP_ and _SNMP_ as services. The `foreignId` of is mapped with the _OCS id_ of the `snmpDevice`. The `nodeLabel` is provided by the _OCS name_ of the `snmpDevice`. The assets for CPU and operating system are mapped against _OCS_. Additionally a link to the OCS `snmpDevice` page is added to the asset comment field.

##### Black- and Whitelists
The OCS Integration supports Black- and Whitelists to control the selection of the management-interface for the node. OCS it self dose not define a management-interface, it just selects one ip-address as default and maintains a networks-list for every computer. For the election of the management-interface two ip-filters are implemented in the IpInterfaceHelper-class. Both read the black- and whitelist from the requisition configuration folder. Name them "blackList.properties" and "whiteList.properties". Every line in those files is interpreted as an IPLike statement to offer ranges.

###### Default ip-filter
This filter is accepting every IP address as valid that is not blacklisted. IP addresses that are white listed are preferred over not listed IP addresses.

###### Computers
The first IP address of the `ocs-networks-list` that is white listed is used. If no IP address of the `ocs-networks-list` is white listed the first not IP address that is not black-listed is elected as management interface. If no IP address of the `ocs-networks-list` qualifies, the `ocs-default-ip` is checked against the blacklist. If it is not black listed, it is elected as management interface (no interface description will be available). If it is black listed, no interface is added to the node. (`selectManagementNetwork`)

###### SnmpDevices
The IP address of a `snmpDevice` is elected as management interface as long as it is not black listed. If it is black listed no interface is added to the node. (`selectIpAddress`)

###### Strict ip-filter "WhiteAndBackOnly"
This filter is as strict black- and white list approach. Computers and `snmpDevices` are handled independently.

###### Computers
This mode is just accepting IP addresses that are white listed and not black listed. If there are multiple IP addresses listed on `ocs-networks-list` that are white listed but and not black listed, the first one is selected as management IP. If no IP address from the `ocs-networks-list` matches the black- and whitelist, the `ocs-default-ip` is tested against the black- and whitelist. If the `ocs-defaul-ip` is white listed and not black listed it is elected as management-ip. If no IP address matches the black- and whitelist, no interface is added to the node. If the `ocs-default-ip` is selected, the interface of the node will not contain any additional parameters like description. (`selectManagementNetworkWhiteAndBlackOnly`)

###### SnmpDevices
If the IP address of the `snmpDevice` is white listed and not black listed it is elected as management interface. If the IP address is not passing the lists, no interface is added to the node. (`selectIpAddressWhiteAndBlackOnly`)

###### IPLike expressions in lists
In both lists the `IPLike` syntax can be used to express IP ranges and wildcards. Follow the `IPLike` description at [IPLIKE documentaion](http://www.opennms.org/wiki/IPLIKE).
