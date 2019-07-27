{
  "range": {
    "netflow.timeout_switched": {
      "lte": ${end?long?c},
      "format": "epoch_millis"
    }
  }
},
{
  "range": {
    "netflow.last_switched": {
      "gte": ${start?long?c},
      "format": "epoch_millis"
    }
  }
}
