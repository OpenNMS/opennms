{
  "aggs": {
    "alarms_by_id": {
      "composite" : {
        "sources" : [
          { "alarm_id": { "terms" : { "field": "id" } } }
        ],
<#if afterAlarmWithId?has_content>
        "after": { "alarm_id": ${afterAlarmWithId?long?c} },
</#if>
        "size": 1000 <#-- This is the maximum number of buckets that can be processed in one request.
                          Subsequent requests should be made to page through the results -->
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