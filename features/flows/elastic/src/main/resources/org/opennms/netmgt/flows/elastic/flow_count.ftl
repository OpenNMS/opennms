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
  }
}