<#-- When filtering for a given SNMP interface also match the flow direction if strictIngressEgress is enabled:
       A) For ingress traffic, the interface should be the input
       B) For egress traffic, the interface should be the output
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
            }
            <#if strictIngressEgress>
              ,{
                "terms": {
                  "netflow.direction": [
                  "ingress"
                  ]
                }
              }
            </#if>
          ]
        }
      },
      {
        "bool": {
          "must": [
            {
              "terms": {
                "netflow.output_snmp": [${snmpInterfaceId?long?c}]
              }
            }
            <#if strictIngressEgress>
              ,{
                "terms": {
                  "netflow.direction": [
                    "egress"
                  ]
                }
              }
            </#if>
          ]
        }
      }
    ]
  }
}