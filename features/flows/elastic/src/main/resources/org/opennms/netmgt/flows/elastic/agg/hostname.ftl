{
  "size": 1,
  "sort": [
    { "@timestamp" : { "order": "desc" }},
    { "host_address": { "missing": "_last" }},
    "_score"
  ],
  "_source": [
    "host_address",
    "host_name"
  ],
  "query": {
    "bool": {
      "filter": [
        <#list filters as filter>${filter},</#list>
        {
          "bool": {
            "must_not": {
              "term": {
                "host_name": ""
              }
            }
          }
        },
        {
          "term": {
            "host_address": "${host?json_string}"
          }
        }
      ]
    }
  }
}
