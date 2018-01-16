{
  "size": 0,
  "query": {
    "bool": {
      "filter": [
        <#list filters as filter>${filter}<#sep>,</#list>
      ]
    }
  },
  "aggs": {
    "grouped_by": {
      "terms": {
        "field": "${groupByTerm?json_string}",
        <#if keyForMissingTerm?has_content>"missing": "${keyForMissingTerm?json_string}",</#if>
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