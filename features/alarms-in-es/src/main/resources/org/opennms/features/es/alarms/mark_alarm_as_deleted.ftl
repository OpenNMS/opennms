{
  "script": {
    "source": "ctx._source['@deleted-time'] = ${deletedTime?long?c}L",
    "lang": "painless"
  },
  "query": {
    "term": {
      "id": ${alarmId?long?c}
    }
  }
}