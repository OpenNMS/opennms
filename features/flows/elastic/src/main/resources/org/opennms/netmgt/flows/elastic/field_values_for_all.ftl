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
        "my_buckets": {
            "composite": {
                "sources": [
                    { "${field}": { "terms": { "field": "${field}" } } }
                ]
            }
        }
    }
}
