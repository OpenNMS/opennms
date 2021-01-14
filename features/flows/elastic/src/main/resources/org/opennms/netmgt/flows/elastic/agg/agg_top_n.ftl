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
              "value": "${aggregationType?json_string}"
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
    "by_key": {
      "terms": {
        "field": "${key?json_string}",
        "size": ${N?long?c},
        "order": [
          {
            "bytes_total": "desc"
          },
          {
            "_key": "asc"
          }
        ]
      },
      "aggregations": {
        "bytes_total": {
          "sum": {
            "field": "bytes_total"
          }
        },
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
  }
}
