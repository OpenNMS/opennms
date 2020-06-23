{
  "size": 1,
  "sort": [
    { "@timestamp" : { "order": "desc" }},
    { "netflow.src_addr_hostname": { "missing": "_last" }},
    { "netflow.dst_addr_hostname": { "missing": "_last" }},
    "_score"
  ],
  "_source": [
    "netflow.src_addr",
    "netflow.dst_addr",
    "netflow.src_addr_hostname",
    "netflow.dst_addr_hostname"
  ],
  "query": {
    "bool": {
      "filter": [
        <#list filters as filter>${filter}<#sep>,</#list>
      ],
      "should": [
        {
          "term": {
            "netflow.src_addr": "${host?json_string}"
          }
        },
        {
          "term": {
            "netflow.dst_addr": "${host?json_string}"
          }
        }
      ]
    }
  }
}
