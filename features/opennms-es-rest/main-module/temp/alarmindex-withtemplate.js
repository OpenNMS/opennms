{
  "opennms-alarms-2016.08": {
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
      "alarmdata": {
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
          "alarmid": {
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
          "alarmtype": {
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
          "categories": {
            "type": "string"
          },
          "counter": {
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
          "description": {
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
          "firstautomationtime": {
            "type": "date",
            "format": "strict_date_optional_time||epoch_millis"
          },
          "firsteventtime": {
            "type": "date",
            "format": "strict_date_optional_time||epoch_millis"
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
          "hour": {
            "type": "long"
          },
          "ipaddr": {
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
          "lastautomationtime": {
            "type": "date",
            "format": "strict_date_optional_time||epoch_millis"
          },
          "lasteventid": {
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
          "lasteventtime": {
            "type": "date",
            "format": "strict_date_optional_time||epoch_millis"
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
          "operinstruct": {
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
          "reductionkey": {
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
          "serviceid": {
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
          "severity": {
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
          "suppressedtime": {
            "type": "date",
            "format": "strict_date_optional_time||epoch_millis"
          },
          "suppresseduntil": {
            "type": "date",
            "format": "strict_date_optional_time||epoch_millis"
          },
          "systemid": {
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
          "x733probablecause": {
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
        "creation_date": "1471265400367",
        "refresh_interval": "5s",
        "number_of_shards": "5",
        "number_of_replicas": "1",
        "uuid": "K0BKo8ttQr-nGLO_77L3Yw",
        "version": {
          "created": "2030299"
        }
      }
    },
    "warmers": {}
  }
}