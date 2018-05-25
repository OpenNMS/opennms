{
  "size": 0,
  "query": {
    "bool": {
      "filter": [
<#list filters as filter>${filter}<#sep>,</#list>
      ],
      "must_not": {
        "exists": {
          "field": "${groupByTerm?json_string}"
        }
      }
    }
  },
  "aggs": {
    "grouped_by": {
      "terms": {
        "field": "${groupByTerm?json_string}",
        "missing": "${keyForMissingTerm?json_string}"
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