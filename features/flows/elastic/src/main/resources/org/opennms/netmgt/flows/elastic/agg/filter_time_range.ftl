{
  "range": {
    "range_start": {
      "lte": ${end?long?c},
      "format": "epoch_millis"
    }
  }
},
{
  "range": {
    "range_end": {
      "gte": ${start?long?c},
      "format": "epoch_millis"
    }
  }
}
