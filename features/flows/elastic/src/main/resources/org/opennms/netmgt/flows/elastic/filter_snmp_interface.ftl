<#-- Filter for flow records where (direction=INGRESS, input=ifIndex), (direction=EGRESS,
When filtering for a given SNMP interface also match the flow direction
       A) For ingress traffic, the interface should be the input
       B) For egress traffic, the interface should be the output
       C) For unknown traffic, the interface should be the input or output
          (The aggregation query can map the record to the ingress or egress directions using a script in an aggregation or runtime field)
-->
{
  "bool": {
    "should": [
      {
        "bool": {
          "must": [
            {
              "terms": {
                "netflow.input_snmp": [${snmpInterfaceId?long?c}]
              }
            },
            {
              "terms": {
                "netflow.direction": [
                  "ingress"
                ]
              }
            }
          ]
        }
      },{
        "bool": {
          "must": [
            {
              "terms": {
                "netflow.output_snmp": [${snmpInterfaceId?long?c}]
              }
            },
            {
              "terms": {
                "netflow.direction": [
                  "egress"
                ]
              }
            }
          ]
        }
      },{
        "bool": {
          "must": [
            {
              "terms": {
                "netflow.input_snmp": [${snmpInterfaceId?long?c}]
              }
            },
            {
              "terms": {
                "netflow.direction": [
                  "unknown"
                ]
              }
            }
          ]
        }
      },{
        "bool": {
          "must": [
            {
              "terms": {
                "netflow.output_snmp": [${snmpInterfaceId?long?c}]
              }
            },
            {
              "terms": {
                "netflow.direction": [
                  "unknown"
                ]
              }
            }
          ]
        }
      }
    ]
  }
}
