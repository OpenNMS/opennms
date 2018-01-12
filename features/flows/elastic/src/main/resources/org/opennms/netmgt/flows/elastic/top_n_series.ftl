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
        {
          "range": {
            "@timestamp": {
              "gte": ${start?long?c},
              "lte": ${end?long?c},
              "format": "epoch_millis"
            }
          }
        }
      ]
    }
  },
  "aggs": {
    "grouped_by": {
      "terms": {
        "field": "${groupByTerm?json_string}"
      },
      "aggs": {
        "bytes_over_time": {
          "proportional_sum": {
            "fields": [
              "netflow.first_switched",
              "netflow.last_switched",
              "netflow.bytes"
            ],
            "interval": "${step?long?c}ms",
            "start": ${start?long?c},
            "end": ${end?long?c}
          },
          "aggs": {
            "direction": {
              "terms": {
                "field": "netflow.initiator",
                "size": 2
              },
              "aggs": {
                "total_bytes": {
                  "sum": {
                    "field": "netflow.bytes"
                  }
                }
              }
            }
          }
        }
      }
    }
  }
}