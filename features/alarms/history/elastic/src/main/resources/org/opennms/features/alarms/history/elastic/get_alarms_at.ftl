{
  "size": 0,
  "query": {
    "bool" : {
      <#if alarmIdsToExclude?has_content>
      "must_not": [
        {
          "terms": {
            "id": [<#list alarmIdsToExclude as alarmId>${alarmId?long?c}<#sep>,</#list>]
          }
        }
      ],
      </#if>
      "filter": [
        {
          "range": {
            "@update_time": {
              "gte": ${fromMillis?long?c},
              "format": "epoch_millis"
            }
          }
        },
        {
          "range": {
            "@update_time": {
              "lte": ${toMillis?long?c},
              "format": "epoch_millis"
            }
          }
        }
      ]
    }
  },
  "aggs": {
    "alarms_by_id": {
	  "composite" : {
        "sources" : [
          { "alarm_id": { "terms" : { "field": "id" } } }
	    ],
<#if afterAlarmWithId?has_content>
        "after": { "alarm_id": ${afterAlarmWithId?long?c} },
</#if>
        "size": ${maxBuckets?long?c} <#-- This is the maximum number of buckets that can be processed in one request.
                          Subsequent requests should be made to page through the results -->

       },
      "aggs": {
        "latest_alarm": {
          "top_hits": {
            <#if idOnly>
            "_source": {
                "includes": [ "id" ]
            },
            </#if>
            "sort": [
              {
                "@update_time": {
                  "order": "desc"
                }
              }
            ],
            "size" : 1
          }
        },
        "any_deletes": {
            "sum": {
                "field": "@deleted_time"
            }
        },
        "delete_bucket_filter": {
            "bucket_selector": {
                "buckets_path": {
                  "anyDeletes": "any_deletes"
                },
                "script": "params.anyDeletes < 1"
            }
        }
      }
    }
  }
}