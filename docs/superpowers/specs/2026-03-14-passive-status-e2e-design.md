# Passive Service Status E2E Test — Design Spec

**Date:** 2026-03-14
**Branch:** `eventbus-redesign`

## Goal

Validate the full passive service monitoring pipeline through Minion in Delta-V:
syslog → Minion → Syslogd → EventTranslator → passiveServiceStatus → PassiveStatusKeeper → Twin API → Minion → PassiveServiceMonitor → Outage/Alarm in PostgreSQL

## Test Pipeline

```
Host                    Minion              Syslogd             EventTranslator
  │                       │                    │                      │
  │ syslog UDP 1514       │                    │                      │
  │──────────────────────>│ Kafka Sink         │                      │
  │                       │───────────────────>│ ConvertToEvent       │
  │                       │                    │───────────────────-->│ translate to
  │                       │                    │                      │ passiveServiceStatus
  │                       │                    │                      │
                                                                      │
  PassiveStatusKeeper     Twin API            Minion                  │
       │                    │                    │                     │
       │<───────────────────│ fault-events topic │<────────────────────│
       │ setStatus()        │                    │                     │
       │ publish()──────────│───────────────────>│ subscribe()         │
       │                    │                    │ PassiveServiceMonitor.poll()
       │                    │                    │ → status = Down/Up
       │                    │                    │
  Pollerd                                    Alarmd              PostgreSQL
       │                                       │                    │
       │ poll via RPC ─────────────────────────>│                    │
       │ (status=Down)                          │ create alarm ─────>│ alarms table
       │                                        │ create outage ────>│ outages table
```

## Provisioning

### Foreign Source Definition (`cloud-services`)

Uses `LoopDetector` to auto-assign 3 passive services when a node is provisioned:

```xml
<foreign-source name="cloud-services">
  <scan-interval>1d</scan-interval>
  <detectors>
    <detector name="GoogleCloud" class="org.opennms.netmgt.provision.detector.loop.LoopDetector">
      <parameter key="ipMatch" value="*.*.*.*"/>
    </detector>
    <detector name="Azure" class="org.opennms.netmgt.provision.detector.loop.LoopDetector">
      <parameter key="ipMatch" value="*.*.*.*"/>
    </detector>
    <detector name="AWS" class="org.opennms.netmgt.provision.detector.loop.LoopDetector">
      <parameter key="ipMatch" value="*.*.*.*"/>
    </detector>
  </detectors>
</foreign-source>
```

### Requisition (`cloud-services`)

```xml
<model-import foreign-source="cloud-services">
  <node foreign-id="the-internet" node-label="The Internet">
    <interface ip-addr="10.0.0.1" managed="true" snmp-primary="N"/>
  </node>
</model-import>
```

Provisiond imports → runs LoopDetectors → creates node "The Internet" with GoogleCloud, Azure, AWS services.

## Syslog Configuration

### Syslog Match Rule (`cloud-status.syslog.xml`)

Matches messages like:
```
CLOUD-STATUS: Service AWS on 10.0.0.1 is Down
CLOUD-STATUS: Service AWS on 10.0.0.1 is Up
```

```xml
<syslogd-configuration-group>
  <ueiList>
    <ueiMatch>
      <match type="regex" expression="^.*CLOUD-STATUS: Service (\S+) on (\S+) is (Down)$"/>
      <uei>uei.opennms.org/syslogd/cloud/serviceDown</uei>
      <parameter-assignment matching-group="1" parameter-name="cloudService"/>
      <parameter-assignment matching-group="2" parameter-name="cloudIpAddr"/>
      <parameter-assignment matching-group="3" parameter-name="cloudStatus"/>
    </ueiMatch>
    <ueiMatch>
      <match type="regex" expression="^.*CLOUD-STATUS: Service (\S+) on (\S+) is (Up)$"/>
      <uei>uei.opennms.org/syslogd/cloud/serviceUp</uei>
      <parameter-assignment matching-group="1" parameter-name="cloudService"/>
      <parameter-assignment matching-group="2" parameter-name="cloudIpAddr"/>
      <parameter-assignment matching-group="3" parameter-name="cloudStatus"/>
    </ueiMatch>
  </ueiList>
</syslogd-configuration-group>
```

### Event Definitions (eventconf DB inserts)

**serviceDown event** with alarm-data:
```xml
<event>
  <uei>uei.opennms.org/syslogd/cloud/serviceDown</uei>
  <event-label>Cloud Service Down</event-label>
  <descr>Cloud service %parm[cloudService]% on %parm[cloudIpAddr]% is down</descr>
  <logmsg dest="logndisplay">Cloud service %parm[cloudService]% is down</logmsg>
  <severity>Minor</severity>
  <alarm-data reduction-key="%uei%:%dpname%:%nodeid%:%parm[cloudService]%"
              alarm-type="1" auto-clean="false"/>
</event>
```

**serviceUp event** with clear-key:
```xml
<event>
  <uei>uei.opennms.org/syslogd/cloud/serviceUp</uei>
  <event-label>Cloud Service Up</event-label>
  <descr>Cloud service %parm[cloudService]% on %parm[cloudIpAddr]% is up</descr>
  <logmsg dest="logndisplay">Cloud service %parm[cloudService]% is up</logmsg>
  <severity>Normal</severity>
  <alarm-data reduction-key="%uei%:%dpname%:%nodeid%:%parm[cloudService]%"
              alarm-type="2"
              clear-key="uei.opennms.org/syslogd/cloud/serviceDown:%dpname%:%nodeid%:%parm[cloudService]%"/>
</event>
```

### Event Translator Configuration

Translates the syslog-derived cloud events into `passiveServiceStatus` events:

```xml
<event-translation-spec uei="uei.opennms.org/syslogd/cloud/serviceDown">
  <mappings>
    <mapping>
      <assignment type="field" name="uei">
        <value type="constant" result="uei.opennms.org/services/passiveServiceStatus"/>
      </assignment>
      <assignment type="parameter" name="passiveNodeLabel">
        <value type="sql" result="SELECT nodelabel FROM node WHERE nodeid = ?">
          <value type="field" name="nodeid"/>
        </value>
      </assignment>
      <assignment type="parameter" name="passiveIpAddr">
        <value type="parameter" name="cloudIpAddr"/>
      </assignment>
      <assignment type="parameter" name="passiveServiceName">
        <value type="parameter" name="cloudService"/>
      </assignment>
      <assignment type="parameter" name="passiveStatus">
        <value type="constant" result="Down"/>
      </assignment>
    </mapping>
  </mappings>
</event-translation-spec>

<event-translation-spec uei="uei.opennms.org/syslogd/cloud/serviceUp">
  <mappings>
    <mapping>
      <assignment type="field" name="uei">
        <value type="constant" result="uei.opennms.org/services/passiveServiceStatus"/>
      </assignment>
      <assignment type="parameter" name="passiveNodeLabel">
        <value type="sql" result="SELECT nodelabel FROM node WHERE nodeid = ?">
          <value type="field" name="nodeid"/>
        </value>
      </assignment>
      <assignment type="parameter" name="passiveIpAddr">
        <value type="parameter" name="cloudIpAddr"/>
      </assignment>
      <assignment type="parameter" name="passiveServiceName">
        <value type="parameter" name="cloudService"/>
      </assignment>
      <assignment type="parameter" name="passiveStatus">
        <value type="constant" result="Up"/>
      </assignment>
    </mapping>
  </mappings>
</event-translation-spec>
```

### Poller Configuration

Add passive services to poller-configuration.xml:

```xml
<service name="GoogleCloud" interval="300000" user-defined="true" status="on"/>
<service name="Azure" interval="300000" user-defined="true" status="on"/>
<service name="AWS" interval="300000" user-defined="true" status="on"/>

<monitor service="GoogleCloud" class-name="org.opennms.netmgt.poller.monitors.PassiveServiceMonitor"/>
<monitor service="Azure" class-name="org.opennms.netmgt.poller.monitors.PassiveServiceMonitor"/>
<monitor service="AWS" class-name="org.opennms.netmgt.poller.monitors.PassiveServiceMonitor"/>
```

## Test Phases

### Phase 0: Provisioning

1. Insert foreign source definition into DB (or file-based via Provisiond overlay)
2. Insert requisition into DB (or file-based)
3. Trigger import
4. Wait for node + 3 services to appear in PostgreSQL
5. Verify: `SELECT * FROM node WHERE nodelabel = 'The Internet'`
6. Verify: 3 services exist (GoogleCloud, Azure, AWS)

### Phase 1: AWS Down (syslog Alert)

1. Send syslog message via Minion: `CLOUD-STATUS: Service AWS on 10.0.0.1 is Down`
   - PRI 129 = local0.alert
2. Wait for `passiveServiceStatus` on Kafka fault-events topic
3. Wait for Pollerd to poll the service (Twin API sync + poll cycle)
4. Verify outage created: `SELECT * FROM outages WHERE ... AND ifRegainedService IS NULL`
5. Verify alarm created: `SELECT * FROM alarms WHERE ... AND alarmtype = 1`

### Phase 2: AWS Up (syslog Info)

1. Send syslog message via Minion: `CLOUD-STATUS: Service AWS on 10.0.0.1 is Up`
   - PRI 134 = local0.info
2. Wait for `passiveServiceStatus` on Kafka fault-events
3. Wait for Pollerd to detect the status change
4. Verify outage closed: `SELECT * FROM outages WHERE ... AND ifRegainedService IS NOT NULL`
5. Verify alarm cleared: `SELECT * FROM alarms WHERE ... AND severity = 2`

## Required Services

`postgres`, `kafka`, `syslogd`, `eventtranslator`, `alarmd`, `provisiond`, `pollerd`, `minion`

## Config File Delivery

Config overlays are placed in daemon-specific overlay directories:
- **Syslogd:** `opennms-container/delta-v/syslogd-overlay/etc/syslog/cloud-status.syslog.xml`
- **EventTranslator:** update `translator-configuration.xml` in eventtranslator overlay
- **Pollerd:** update `poller-configuration.xml` in pollerd overlay
- **Provisiond:** foreign source + requisition files in provisiond overlay
- **Event definitions:** inserted into eventconf DB tables by the test script

## Test Script

`opennms-container/delta-v/test-passive-e2e.sh` — follows the established pattern from `test-syslog-e2e.sh`:
- Shell-based, uses `nc` for syslog, `psql_query` for DB assertions
- Kafka consumer watching for events
- `wait_for_kafka_event` for async verification
- Exit code 0/1 for pass/fail
