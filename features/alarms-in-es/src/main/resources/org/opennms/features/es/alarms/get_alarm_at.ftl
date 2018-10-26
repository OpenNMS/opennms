{
  "query": {
    "bool": {
      "filter": [
        {
          "terms": {
            "id": [${alarmId?long?c}]
          }
        },
        {
          "range": {
            "@update-time": {
              "lte": ${time?long?c},
              "format": "epoch_millis"
            }
          }
        }
      ]
    }
  },
  "sort" : [
    { "@update-time" : {"order" : "desc"}}
  ],
  "size": 1
}