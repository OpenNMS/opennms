{
  "script": {
    "source": "ctx._source['@deleted-time'] = ${deletedTime?long?c}L",
    "lang": "painless"
  },
  "query": {
    "bool": {
      "must_not": [
        <#if alarmIds?has_content>
        {
          "terms": {
            "id": [<#list alarmIds as alarmId>${alarmId?long?c}<#sep>,</#list>]
          }
        },
        </#if>
        {
          "exists": {
            "field": "@deleted-time"
          }
        }
      ]
    }
  }
}
