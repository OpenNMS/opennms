{
  "size": 0,
  "query": {
    "bool": {
      "filter": [
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
        "field": "${groupByTerm?json_string}",
        "size": ${N?long?c},
        "order": {
          "total_bytes": "desc"
        }
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