{
  "query": {
    "bool": {
      "filter": [
        {
          "terms": {
            <#if reductionKey?has_content>
              "reduction_key": ["${reductionKey?json_string}"]
            <#else> <#-- If a reduction key is not set, then assume there's an alarmId -->
              "id": [${alarmId?long?c}]
            </#if>
          }
        }
      ]
    }
  },
  "sort" : [
    { "@update_time" : {"order" : "asc"}}
  ],
  "size": ${maxResults?long?c}
}