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
    "criterias": {
      "terms": {
        "field": "node_exporter.node_id",
        "size": ${size?long?c}
      }
    }
  }
}