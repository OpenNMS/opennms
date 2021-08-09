{
  "size": 0,
  "query": {
    "bool": {
      "must": [
        {
          "term": {
            "grouped_by": {
              "value": "${groupedBy?json_string}"
            }
          }
        },
        {
          "term": {
            "aggregation_type": {
              "value": "TOTAL"
            }
          }
        }
        <#if (filters?size > 0)>,
        <#list filters as filter>${filter}<#sep>,</#list>
        </#if>
      ]
    }
  },
  "aggregations": {
    "bytes_ingress": {
      "sum": {
        "field": "bytes_ingress"
      }
    },
    "bytes_egress": {
      "sum": {
        "field": "bytes_egress"
      }
    },
    "congestion_encountered": {
      "max": {
        "field": "congestion_encountered"
      }
    },
    "non_ect": {
      "max": {
        "field": "non_ect"
      }
    }
  }
}
