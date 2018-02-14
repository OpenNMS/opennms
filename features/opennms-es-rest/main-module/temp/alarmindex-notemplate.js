{
  "opennms-alarms-2016.08": {
    "aliases": {},
    "mappings": {
      "alarmdata": {
        "properties": {
          "@timestamp": {
            "type": "date",
            "format": "strict_date_optional_time||epoch_millis"
          },
          "alarmid": {
            "type": "string"
          },
          "alarmtype": {
            "type": "string"
          },
          "categories": {
            "type": "string"
          },
          "counter": {
            "type": "string"
          },
          "description": {
            "type": "string"
          },
          "dom": {
            "type": "string"
          },
          "dow": {
            "type": "string"
          },
          "eventuei": {
            "type": "string"
          },
          "firstautomationtime": {
            "type": "date",
            "format": "strict_date_optional_time||epoch_millis"
          },
          "firsteventtime": {
            "type": "date",
            "format": "strict_date_optional_time||epoch_millis"
          },
          "foreignid": {
            "type": "string"
          },
          "foreignsource": {
            "type": "string"
          },
          "hour": {
            "type": "string"
          },
          "ipaddr": {
            "type": "string"
          },
          "lastautomationtime": {
            "type": "date",
            "format": "strict_date_optional_time||epoch_millis"
          },
          "lasteventid": {
            "type": "string"
          },
          "lasteventtime": {
            "type": "date",
            "format": "strict_date_optional_time||epoch_millis"
          },
          "logmsg": {
            "type": "string"
          },
          "nodeid": {
            "type": "string"
          },
          "nodelabel": {
            "type": "string"
          },
          "operinstruct": {
            "type": "string"
          },
          "reductionkey": {
            "type": "string"
          },
          "serviceid": {
            "type": "string"
          },
          "severity": {
            "type": "string"
          },
          "suppressedtime": {
            "type": "date",
            "format": "strict_date_optional_time||epoch_millis"
          },
          "suppresseduntil": {
            "type": "date",
            "format": "strict_date_optional_time||epoch_millis"
          },
          "systemid": {
            "type": "string"
          },
          "x733probablecause": {
            "type": "string"
          }
        }
      }
    },
    "settings": {
      "index": {
        "creation_date": "1471265175495",
        "number_of_shards": "5",
        "number_of_replicas": "1",
        "uuid": "Wwd4tzftSRCpEd2FRU4SOg",
        "version": {
          "created": "2030299"
        }
      }
    },
    "warmers": {}
  }
}