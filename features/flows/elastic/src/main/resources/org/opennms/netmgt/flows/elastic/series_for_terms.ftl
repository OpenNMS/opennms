{
  "size": 0,
  "query": {
    "bool": {
      "filter": [
        {
          "terms": {
            "${groupByTerm?json_string}": [<#list topN as topNTerm>"${topNTerm?json_string}"<#sep>,</#list>]
          }
        },
<#list filters as filter>${filter}<#sep>,</#list>
      ]
    }
  },
  "aggs": {
    "grouped_by": {
      "terms": {
        "field": "${groupByTerm?json_string}",
        "size": ${topN?size?long?c}
      },
      "aggs": {
        "direction": {
          "terms": {
            "field": "netflow.direction",
            "size": 2
          },
          "aggs": {
            "bytes": {
              "proportional_sum": {
                "fields": [
                  "netflow.first_switched",
                  "netflow.last_switched",
                  "netflow.bytes",
                  "netflow.sampling_interval"
                ],
                "interval": "${step?long?c}ms",
                "start": ${start?long?c},
                "end": ${end?long?c}
              }
            }
          }
        }
      }
    }
  }
}