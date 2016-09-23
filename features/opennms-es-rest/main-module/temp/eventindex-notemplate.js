{
  "opennms-events-raw-2016.08": {
    "aliases": {},
    "mappings": {
      "eventdata": {
        "properties": {
          "@timestamp": {
            "type": "date",
            "format": "strict_date_optional_time||epoch_millis"
          },
          "categories": {
            "type": "string"
          },
          "dom": {
            "type": "string"
          },
          "dow": {
            "type": "string"
          },
          "eventseverity": {
            "type": "string"
          },
          "eventseverity_text": {
            "type": "string"
          },
          "eventuei": {
            "type": "string"
          },
          "foreignid": {
            "type": "string"
          },
          "foreignsource": {
            "type": "string"
          },
          "host": {
            "type": "string"
          },
          "hour": {
            "type": "string"
          },
          "id": {
            "type": "string"
          },
          "logmsg": {
            "type": "string"
          },
          "logmsgdest": {
            "type": "string"
          },
          "nodeid": {
            "type": "string"
          },
          "nodelabel": {
            "type": "string"
          },
          "p_PiIoId": {
            "type": "string"
          },
          "p_catlabel": {
            "type": "string"
          },
          "p_eventReason": {
            "type": "string"
          },
          "p_foreignId": {
            "type": "string"
          },
          "p_foreignSource": {
            "type": "string"
          },
          "p_passwd": {
            "type": "string"
          },
          "p_reason": {
            "type": "string"
          },
          "p_url": {
            "type": "string"
          },
          "p_user": {
            "type": "string"
          }
        }
      }
    },
    "settings": {
      "index": {
        "creation_date": "1471264892058",
        "number_of_shards": "5",
        "number_of_replicas": "1",
        "uuid": "EBxzEiu-Q3yYGAX3EjUP0g",
        "version": {
          "created": "2030299"
        }
      }
    },
    "warmers": {}
  }
}