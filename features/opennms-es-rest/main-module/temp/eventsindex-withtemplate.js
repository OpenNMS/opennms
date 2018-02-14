{
  "opennms-events-raw-2016.08": {
    "aliases": {},
    "mappings": {
      "_default_": {
        "_all": {
          "enabled": true
        },
        "dynamic_templates": [
          {
            "string_fields": {
              "mapping": {
                "index": "not_analyzed",
                "omit_norms": true,
                "type": "string",
                "fields": {
                  "raw": {
                    "ignore_above": 256,
                    "index": "not_analyzed",
                    "type": "string"
                  }
                }
              },
              "match": "*",
              "match_mapping_type": "string"
            }
          }
        ],
        "properties": {
          "@version": {
            "type": "string",
            "index": "not_analyzed"
          },
          "categories": {
            "type": "string"
          },
          "dom": {
            "type": "long"
          },
          "dow": {
            "type": "long"
          },
          "eventdescr": {
            "type": "string"
          },
          "eventlogmsg": {
            "type": "string"
          },
          "eventparms": {
            "type": "string"
          },
          "eventseverity": {
            "type": "long"
          },
          "hour": {
            "type": "long"
          }
        }
      },
      "eventdata": {
        "_all": {
          "enabled": true
        },
        "dynamic_templates": [
          {
            "string_fields": {
              "mapping": {
                "index": "not_analyzed",
                "omit_norms": true,
                "type": "string",
                "fields": {
                  "raw": {
                    "ignore_above": 256,
                    "index": "not_analyzed",
                    "type": "string"
                  }
                }
              },
              "match": "*",
              "match_mapping_type": "string"
            }
          }
        ],
        "properties": {
          "@timestamp": {
            "type": "date",
            "format": "strict_date_optional_time||epoch_millis"
          },
          "@version": {
            "type": "string",
            "index": "not_analyzed"
          },
          "categories": {
            "type": "string"
          },
          "dom": {
            "type": "long"
          },
          "dow": {
            "type": "long"
          },
          "eventdescr": {
            "type": "string"
          },
          "eventlogmsg": {
            "type": "string"
          },
          "eventparms": {
            "type": "string"
          },
          "eventseverity": {
            "type": "long"
          },
          "eventseverity_text": {
            "type": "string",
            "index": "not_analyzed",
            "fields": {
              "raw": {
                "type": "string",
                "index": "not_analyzed",
                "ignore_above": 256
              }
            }
          },
          "eventuei": {
            "type": "string",
            "index": "not_analyzed",
            "fields": {
              "raw": {
                "type": "string",
                "index": "not_analyzed",
                "ignore_above": 256
              }
            }
          },
          "foreignid": {
            "type": "string",
            "index": "not_analyzed",
            "fields": {
              "raw": {
                "type": "string",
                "index": "not_analyzed",
                "ignore_above": 256
              }
            }
          },
          "foreignsource": {
            "type": "string",
            "index": "not_analyzed",
            "fields": {
              "raw": {
                "type": "string",
                "index": "not_analyzed",
                "ignore_above": 256
              }
            }
          },
          "host": {
            "type": "string",
            "index": "not_analyzed",
            "fields": {
              "raw": {
                "type": "string",
                "index": "not_analyzed",
                "ignore_above": 256
              }
            }
          },
          "hour": {
            "type": "long"
          },
          "id": {
            "type": "string",
            "index": "not_analyzed",
            "fields": {
              "raw": {
                "type": "string",
                "index": "not_analyzed",
                "ignore_above": 256
              }
            }
          },
          "logmsg": {
            "type": "string",
            "index": "not_analyzed",
            "fields": {
              "raw": {
                "type": "string",
                "index": "not_analyzed",
                "ignore_above": 256
              }
            }
          },
          "logmsgdest": {
            "type": "string",
            "index": "not_analyzed",
            "fields": {
              "raw": {
                "type": "string",
                "index": "not_analyzed",
                "ignore_above": 256
              }
            }
          },
          "nodeid": {
            "type": "string",
            "index": "not_analyzed",
            "fields": {
              "raw": {
                "type": "string",
                "index": "not_analyzed",
                "ignore_above": 256
              }
            }
          },
          "nodelabel": {
            "type": "string",
            "index": "not_analyzed",
            "fields": {
              "raw": {
                "type": "string",
                "index": "not_analyzed",
                "ignore_above": 256
              }
            }
          },
          "p_PiIoId": {
            "type": "string",
            "index": "not_analyzed",
            "fields": {
              "raw": {
                "type": "string",
                "index": "not_analyzed",
                "ignore_above": 256
              }
            }
          },
          "p_catlabel": {
            "type": "string",
            "index": "not_analyzed",
            "fields": {
              "raw": {
                "type": "string",
                "index": "not_analyzed",
                "ignore_above": 256
              }
            }
          },
          "p_eventReason": {
            "type": "string",
            "index": "not_analyzed",
            "fields": {
              "raw": {
                "type": "string",
                "index": "not_analyzed",
                "ignore_above": 256
              }
            }
          },
          "p_foreignId": {
            "type": "string",
            "index": "not_analyzed",
            "fields": {
              "raw": {
                "type": "string",
                "index": "not_analyzed",
                "ignore_above": 256
              }
            }
          },
          "p_foreignSource": {
            "type": "string",
            "index": "not_analyzed",
            "fields": {
              "raw": {
                "type": "string",
                "index": "not_analyzed",
                "ignore_above": 256
              }
            }
          },
          "p_passwd": {
            "type": "string",
            "index": "not_analyzed",
            "fields": {
              "raw": {
                "type": "string",
                "index": "not_analyzed",
                "ignore_above": 256
              }
            }
          },
          "p_reason": {
            "type": "string",
            "index": "not_analyzed",
            "fields": {
              "raw": {
                "type": "string",
                "index": "not_analyzed",
                "ignore_above": 256
              }
            }
          },
          "p_url": {
            "type": "string",
            "index": "not_analyzed",
            "fields": {
              "raw": {
                "type": "string",
                "index": "not_analyzed",
                "ignore_above": 256
              }
            }
          },
          "p_user": {
            "type": "string",
            "index": "not_analyzed",
            "fields": {
              "raw": {
                "type": "string",
                "index": "not_analyzed",
                "ignore_above": 256
              }
            }
          }
        }
      }
    },
    "settings": {
      "index": {
        "creation_date": "1471265399372",
        "refresh_interval": "5s",
        "number_of_shards": "5",
        "number_of_replicas": "1",
        "uuid": "We0StoiiQxKG5HvD1ieKJA",
        "version": {
          "created": "2030299"
        }
      }
    },
    "warmers": {}
  }
}