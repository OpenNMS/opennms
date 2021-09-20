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
    "input_snmp": {
      "terms": {
        "field": "netflow.input_snmp",
        "size": ${size?long?c}
      }
    },
    "output_snmp": {
      "terms": {
        "field": "netflow.input_snmp",
        "size": ${size?long?c}
      }
    }
  }
}