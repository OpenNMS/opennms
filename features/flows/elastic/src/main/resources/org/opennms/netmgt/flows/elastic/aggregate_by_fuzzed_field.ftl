{
  "size": 0,
  "query": {
    "bool": {
      "filter": [
<#list filters as filter>${filter}<#sep>,</#list>
      ],
      "must": {
        "bool": {
          "should": [
          {
            "prefix": {
              "${field?json_string}": {
                "value": "${prefix?json_string}"
              }
            }
          },
          {
            "fuzzy": {
              "${field?json_string}": {
                "value": "${prefix?json_string}",
                "fuzziness": "AUTO",
                "max_expansions": ${N?long?c}
              }
            }
          }]
        }
      }
    }
  },
  "aggs": {
    "grouped_by": {
      "terms": {
        "field": "${field?json_string}",
<#if keyForMissingTerm?has_content>"missing": "${keyForMissingTerm?json_string}",</#if>
        "size": ${N?long?c},
        "order": {
          "_key": "asc"
        }
      }
    }
  }
}
