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
                "size": ${fieldSize?long?c},
                "sources": [
                    { "${field}": { "terms": { "field": "${field}" } } }
                ]
            }
        }
    }
}
