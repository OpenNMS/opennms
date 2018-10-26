{
  "aggs": {
    "alarms_by_id": {
      "terms": {
        "field": "id",
        "size": ${numMaxAlarms?long?c}
      },
      "aggs": {
        "latest_alarm": {
          "top_hits": {
            "sort": [
              {
                "@update-time": {
                  "order": "desc"
                }
              }
            ],
            "size" : 1
          }
        }
      }
    }
  }
}