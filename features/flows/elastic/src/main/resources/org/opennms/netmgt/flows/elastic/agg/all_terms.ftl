{
  "size": 0,
  "query": {
    "bool": {
      "must": [
        { "term": { "grouped_by": "${groupedBy?json_string}" } }
      ],
      "filter": [
        <#list filters as filter>${filter}<#sep>,</#list>
      ]
    }
  },
  "aggs": {
    "my_buckets": {
      "composite": {
        "size": ${fieldSize?long?c},
        "sources": [
           { "term": { "terms": { "field": "${groupedByField?json_string}" } } }
        ]
      }
    }
  }
}
