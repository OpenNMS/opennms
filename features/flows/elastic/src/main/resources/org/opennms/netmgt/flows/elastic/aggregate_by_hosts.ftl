{
  "size": 0,
  "query": {
    "bool": {
      "filter": [
<#list filters as filter>${filter}<#sep>,</#list>
      ],
      "must": 
      {
        "regexp": {
          "hosts": {
            "value": "${regex?json_string}"
          }
        }
      }
    }
  },
  "aggs": {
    "grouped_by": {
      "terms": {
        "field": "hosts",
        "include": "${regex?json_string}",
        "size": ${limit?long?c},
        "order": {
          "_key": "asc"
        }
      }
    }
  }
}
