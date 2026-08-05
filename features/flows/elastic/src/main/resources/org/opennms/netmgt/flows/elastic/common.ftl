<#--
    Painless script for dealing with unknown values the direction field
    Used when aggregating flows into ingress vs egress buckets
    When the direction is unknown, we treat flow records as ingress for the matching input interface,
    and as egress to for the matching output interface.

    Every field access is guarded with containsKey() and size(), matching the ECN scripts in the same
    templates. size() covers a document that lacks the field; containsKey() covers an index whose
    mapping lacks it, where doc['x'] throws before size() is ever reached -- a restored snapshot or a
    hand-created index predating the field. Reading .value off a field a document does not
    have is an error in Painless rather than a miss -- it fails the shard, and one such document
    fails the whole search with a 400. This is not hypothetical: a record only carries the interface
    its direction implies, so a flow whose direction is unknown and which names only its output has
    no netflow.input_snmp at all. Exporters that omit the direction field produce exactly that, since
    Netflow9MessageBuilder maps a missing direction to UNKNOWN and the two interface fields are
    mapped independently of each other.

    Falling off the end returns null and leaves the record unbucketed. That stays unreachable in
    practice: filter_snmp_interface.ftl only admits an unknown-direction record when the interface
    being asked about is its input or its output, so one of the two tests below matches.
-->
<#function unknownDirectionScript snmpInterfaceId>
    <#local script>
        if (doc.containsKey('netflow.direction') && doc['netflow.direction'].size() > 0 && doc['netflow.direction'].value != 'unknown') {
            return doc['netflow.direction'].value;
        }

        if (doc.containsKey('netflow.input_snmp') && doc['netflow.input_snmp'].size() > 0 && doc['netflow.input_snmp'].value == ${snmpInterfaceId?long?c}) {
            return 'ingress';
        }

        if (doc.containsKey('netflow.output_snmp') && doc['netflow.output_snmp'].size() > 0 && doc['netflow.output_snmp'].value == ${snmpInterfaceId?long?c}) {
            return 'egress';
        }
    </#local>
    <#return script>
</#function>
