<#--
    Painless script for dealing with unknown values the direction field
    Used when aggregating flows into ingress vs egress buckets
    When the direction is unknown, we treat flow records as ingress for the matching input interface,
    and as egress to for the matching output interface.
-->
<#function unknownDirectionScript snmpInterfaceId>
    <#local script>
        if (doc['netflow.direction'].value != 'unknown') {
            return doc['netflow.direction'].value;
        }

        if (doc['netflow.input_snmp'].value == ${snmpInterfaceId?long?c}) {
            return 'ingress';
        }

        if (doc['netflow.output_snmp'].value == ${snmpInterfaceId?long?c}){
            return 'egress';
        }
    </#local>
    <#return script>
</#function>
