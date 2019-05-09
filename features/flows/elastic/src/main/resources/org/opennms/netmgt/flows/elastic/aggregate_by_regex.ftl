{
  "size": 0,
  "query": {
    "bool": {
      "filter": [
<#list filters as filter>${filter}<#sep>,</#list>
      ],
      "must": {
        "regexp": {
          "${field?json_string}": {
            "value": "${regex?json_string}"
          }
        }
      }
    }
  },
  "aggs": {
    "grouped_by": {
      "terms": {
        "field": "${field?json_string}",
        "include": "${regex?json_string}",
        "size": ${limit?long?c},
        "order": {
          "_key": "asc"
        }
      }
    }
  }
}
