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
          <#-- Defer the per-key proportional sums to the top keys; depth_first would run them for every key. -->
          "collect_mode": "breadth_first",
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
        "bytes_in": ${proportionalSumIngress},
        "bytes_out": ${proportionalSumEgress},
        "bytes_total": {
          "sum": {
            "field": "bytes_total"
          }
        }
    }
  }
}
}
