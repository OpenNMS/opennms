{
  "aggs": {
    "alarms_by_id": {
      "composite" : {
        "sources" : [
          { "alarms_by_id": { "terms" : { "field": "id" } } }
        ]
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