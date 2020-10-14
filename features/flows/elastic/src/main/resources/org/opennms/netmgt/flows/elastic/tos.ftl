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
        "tos": {
            "terms": {
                "field": "netflow.tos"
            }
        }
    }
}