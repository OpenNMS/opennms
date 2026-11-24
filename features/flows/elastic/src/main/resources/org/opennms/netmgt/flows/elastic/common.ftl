<#--
    Painless script clause for dealing with unknown values in the direction field.
    Used when aggregating flows into ingress vs egress buckets.
    When the direction is unknown, we treat flow records as ingress for the matching input
    interface, and as egress for the matching output interface.

    The SNMP interface id is passed as a script parameter rather than inlined into the
    source, so Elasticsearch compiles and caches a single script regardless of which
    interface is queried. Inlining the id produced a distinct script per interface and
    could trip the dynamic script compilation rate limit on busy clusters.

    Renders the complete "script": { ... } clause, so callers drop it directly in place of
    a terms aggregation's "field" entry.
-->
<#function unknownDirectionScript snmpInterfaceId>
    <#local source>
        if (doc['netflow.direction'].value != 'unknown') {
            return doc['netflow.direction'].value;
        }

        long snmpInterfaceId = ((Number) params.snmpInterfaceId).longValue();

        if (doc['netflow.input_snmp'].value == snmpInterfaceId) {
            return 'ingress';
        }

        if (doc['netflow.output_snmp'].value == snmpInterfaceId){
            return 'egress';
        }
    </#local>
    <#return '"script": {"source": "' + source?json_string + '", "params": {"snmpInterfaceId": ' + snmpInterfaceId?long?c + '}}'>
</#function>
