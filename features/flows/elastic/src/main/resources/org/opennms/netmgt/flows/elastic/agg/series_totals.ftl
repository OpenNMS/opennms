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
    "bytes_in": {
      "proportional_sum": {
        "fields": [
          "range_start",
          "range_end",
          "bytes_ingress"
        ],
        "interval": "${step?long?c}ms",
        "start": ${start?long?c},
        "end": ${end?long?c}
      }
    },
    "bytes_out": {
      "proportional_sum": {
        "fields": [
          "range_start",
          "range_end",
          "bytes_egress"
        ],
        "interval": "${step?long?c}ms",
        "start": ${start?long?c},
        "end": ${end?long?c}
      }
    }
  }
}
