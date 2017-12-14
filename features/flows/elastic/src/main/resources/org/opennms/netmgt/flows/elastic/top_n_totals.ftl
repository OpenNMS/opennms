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