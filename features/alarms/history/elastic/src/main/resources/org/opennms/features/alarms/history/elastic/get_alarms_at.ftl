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
            "@update-time": {
              "lte": ${time?long?c},
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
          { "alarms_by_id": { "terms" : { "field": "id" } } }
	    ],
        "size": 10000
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
                "@update-time": {
                  "order": "desc"
                }
              }
            ],
            "size" : 1
          }
        },
        "any_deletes": {
            "sum": {
                "field": "@deleted-time"
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