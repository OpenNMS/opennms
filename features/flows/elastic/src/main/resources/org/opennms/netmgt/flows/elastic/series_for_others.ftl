{
  "size": 0,
  "query": {
    "bool": {
      <#if excludeMissing>
      "must": {
          "exists": {
            "field": "${groupByTerm?json_string}"
          }
      },
      </#if>
      "filter": [
<#-- If no SNMP interface is set, then constrain the documents to INGRESS/EGRESS -->
<#if !snmpInterfaceId??>
  {
  "terms": {
  "netflow.direction": ["ingress", "egress"]
  }
  },
</#if>
<#list filters as filter>${filter}<#sep>,</#list>
      ],
      "must_not": {
        "terms": {
          "${groupByTerm?json_string}": [<#list from as fromValue>"${fromValue?json_string}"<#sep>,</#list>]
        }
      }
    }
  },
  "aggs": {
    "direction": {
      "terms": {
        <#if snmpInterfaceId??>
          "script": "${onms.unknownDirectionScript(snmpInterfaceId)?json_string}",
        <#else>
          "field": "netflow.direction",
        </#if>
        "size": 2
      },
      "aggs": {
        "bytes": {
          "proportional_sum": {
            "fields": [
              "netflow.delta_switched",
              "netflow.last_switched",
              "netflow.bytes",
              "netflow.sampling_interval"
            ],
            "interval": "${step?long?c}ms",
            "start": ${start?long?c},
            "end": ${end?long?c}
          }
        },
        <#-- netflow.ecn is a keyword -> max aggregation not possible; string comparison required-->
        "congestion_encountered": {
          "max": {
            "script": "doc.containsKey('netflow.ecn') && doc['netflow.ecn'].size() > 0 ? (doc['netflow.ecn'].value == '3' ? true : false) : false"
          }
        },
        "non_ect": {
          "max": {
            "script": "doc.containsKey('netflow.ecn') && doc['netflow.ecn'].size() > 0 ? (doc['netflow.ecn'].value == '0' ? true : false) : false"
          }
        }
      }
    }
  }
}
