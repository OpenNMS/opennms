{
  "size": 0,
  "query": {
    "bool": {
      "filter": [
<#list filters as filter>${filter}<#sep>,</#list>
      ],
      "must_not": {
        "terms": {
          "netflow.application": [<#list applications as application>"${application?json_string}"<#sep>,</#list>]
        }
      }
    }
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