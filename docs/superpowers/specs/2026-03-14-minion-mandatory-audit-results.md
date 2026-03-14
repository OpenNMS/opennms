# Minion-Mandatory Architecture: Monitor/Collector Audit Results

**Date:** 2026-03-14
**Branch:** `eventbus-redesign`
**Phase:** 1 — Audit (research only, no code changes)

## Summary

| Category | Monitors | Collectors | Total |
|----------|----------|------------|-------|
| Distributable | 59 | 14 | 73 |
| Non-distributable (needs refactoring) | 1 | 0 | 1 |
| Self-monitoring anti-pattern (delete) | 2 | 0 | 2 |
| Deprecated (delete) | 2 | 0 | 2 |
| **Total** | **64** | **14** | **78** |

**Verdict:** The overwhelming majority of monitors and collectors are already Minion-compatible. Only 5 monitors require action (2 self-monitoring deletes, 2 deprecated deletes, 1 refactoring).

---

## ServiceMonitor Classification

### Self-Monitoring Anti-Pattern — DELETE

These monitors test Delta-V infrastructure health rather than managed nodes. In the containerized architecture, platform health is an external concern (Docker healthchecks, K8s probes, Spring Boot actuators).

| # | Monitor | Module | Signal | Action |
|---|---------|--------|--------|--------|
| 1 | **MinionHeartbeatMonitor** | `features/poller/monitors/core` | `getEffectiveLocation()` → `DEFAULT_MONITORING_LOCATION_ID`; queries `MinionDao`/`NodeDao` for heartbeat timestamps | Delete class + ServiceLoader registration + config |
| 2 | **MinionRpcMonitor** | `features/poller/monitors/core` | `getEffectiveLocation()` → `DEFAULT_MONITORING_LOCATION_ID`; sends `EchoRequest` via `RpcClientFactory` to test Minion connectivity | Delete class + ServiceLoader registration + config |

### Non-Distributable — REFACTOR

| # | Monitor | Module | Signal | Proposed Fix |
|---|---------|--------|--------|-------------|
| 1 | **PassiveServiceMonitor** | `opennms-services` | `getEffectiveLocation()` → `DEFAULT_MONITORING_LOCATION_ID`; reads `PassiveStatusKeeper.getInstance()` static singleton | Twin API: publish PassiveStatusKeeper state to Minion, remove `getEffectiveLocation()` override. See design spec for component details. |

### Deprecated — DELETE

| # | Monitor | Module | Signal | Replacement |
|---|---------|--------|--------|-------------|
| 1 | **GpMonitor** | `features/poller/monitors/core` | `@Deprecated` annotation; logs deprecation warning on every poll | `SystemExecuteMonitor` (already exists, fully distributable) |
| 2 | **JschSshMonitor** | `features/poller/monitors/core` | `@deprecated` javadoc tag | `SshMonitor` (already exists, fully distributable) |

### Distributable — ICMP/Ping (3 monitors)

All inherit default `AbstractServiceMonitor.getEffectiveLocation()` which passes location through unchanged.

| # | Monitor | Module | I/O Type |
|---|---------|--------|----------|
| 1 | IcmpMonitor | `features/poller/monitors/core` | ICMP ping via PingerFactory |
| 2 | StrafePingMonitor | `features/poller/monitors/core` | Strafing ICMP pings |
| 3 | AvailabilityMonitor | `features/poller/monitors/core` | `InetAddress.isReachable()` |

### Distributable — TCP/Socket (7 monitors)

| # | Monitor | Module | I/O Type |
|---|---------|--------|----------|
| 1 | TcpMonitor | `features/poller/monitors/core` | Raw TCP socket connection |
| 2 | SmtpMonitor | `features/poller/monitors/core` | SMTP protocol over TCP |
| 3 | Pop3Monitor | `features/poller/monitors/core` | POP3 protocol over TCP |
| 4 | ImapMonitor | `features/poller/monitors/core` | IMAP protocol over TCP |
| 5 | ImapsMonitor | `features/poller/monitors/core` | IMAP over TLS |
| 6 | CitrixMonitor | `features/poller/monitors/core` | Citrix ICA protocol |
| 7 | DominoIIOPMonitor | `features/poller/monitors/core` | IBM Domino IIOP |

### Distributable — HTTP/HTTPS (5 monitors)

| # | Monitor | Module | I/O Type |
|---|---------|--------|----------|
| 1 | HttpMonitor | `features/poller/monitors/core` | HTTP client |
| 2 | HttpPostMonitor | `features/poller/monitors/core` | HTTP POST |
| 3 | HttpsMonitor | `features/poller/monitors/core` | HTTPS |
| 4 | WebMonitor | `features/poller/monitors/core` | Web page content validation |
| 5 | PageSequenceMonitor | `features/poller/monitors/core` | Multi-step HTTP page sequence |

### Distributable — SNMP (15 monitors)

All use SNMP protocol operations which are proxied to Minion via `LocationAwareSnmpClient` (protocol-level RPC).

| # | Monitor | Module | I/O Type |
|---|---------|--------|----------|
| 1 | SnmpMonitor | `features/poller/monitors/core` | SNMP GET/WALK |
| 2 | BgpSessionMonitor | `features/poller/monitors/core` | BGP session status via SNMP |
| 3 | CiscoIpSlaMonitor | `features/poller/monitors/core` | Cisco IP SLA via SNMP |
| 4 | CiscoPingMibMonitor | `features/poller/monitors/core` | Cisco PING-MIB via SNMP |
| 5 | DiskUsageMonitor | `features/poller/monitors/core` | Disk usage via SNMP |
| 6 | DskTableMonitor | `features/poller/monitors/core` | UCD-SNMP dskTable |
| 7 | HostResourceSwRunMonitor | `features/poller/monitors/core` | hrSWRunTable via SNMP |
| 8 | LaTableMonitor | `features/poller/monitors/core` | UCD-SNMP laTable (load avg) |
| 9 | LogMatchTableMonitor | `features/poller/monitors/core` | UCD-SNMP logMatchTable |
| 10 | NetScalerGroupHealthMonitor | `features/poller/monitors/core` | NetScaler via SNMP |
| 11 | OmsaStorageMonitor | `features/poller/monitors/core` | Dell OMSA storage via SNMP |
| 12 | OpenManageChassisMonitor | `features/poller/monitors/core` | Dell OpenManage via SNMP |
| 13 | PercMonitor | `features/poller/monitors/core` | Dell PERC RAID via SNMP |
| 14 | PrTableMonitor | `features/poller/monitors/core` | UCD-SNMP prTable (process) |
| 15 | PtpMonitor | `features/poller/monitors/core` | IEEE 1588 PTP via SNMP |

### Distributable — DNS (2 monitors)

| # | Monitor | Module | I/O Type |
|---|---------|--------|----------|
| 1 | DnsMonitor | `features/poller/monitors/core` | DNS query via dnsjava |
| 2 | DNSResolutionMonitor | `features/poller/monitors/core` | DNS resolution |

### Distributable — SSH (2 monitors)

| # | Monitor | Module | I/O Type |
|---|---------|--------|----------|
| 1 | SshMonitor | `features/poller/monitors/core` | SSH connection test |
| 2 | MinaSshMonitor | `features/poller/monitors/core` | SSH via Apache MINA SSHD |

### Distributable — Database (3 monitors)

| # | Monitor | Module | I/O Type |
|---|---------|--------|----------|
| 1 | JDBCMonitor | `features/poller/monitors/core` | JDBC connection test |
| 2 | JDBCQueryMonitor | `features/poller/monitors/core` | JDBC query execution |
| 3 | JDBCStoredProcedureMonitor | `features/poller/monitors/core` | JDBC stored procedure |

### Distributable — LDAP (2 monitors)

| # | Monitor | Module | I/O Type |
|---|---------|--------|----------|
| 1 | LdapMonitor | `features/poller/monitors/core` | LDAP query |
| 2 | LdapsMonitor | `features/poller/monitors/core` | LDAP over TLS |

### Distributable — JMX/Management (3 monitors)

| # | Monitor | Module | I/O Type |
|---|---------|--------|----------|
| 1 | JMXMonitor | `features/poller/monitors/core` | JMX connection |
| 2 | Jsr160Monitor | `features/poller/monitors/core` | JSR-160 JMX remote |
| 3 | JolokiaBeanMonitor | `features/poller/monitors/core` | Jolokia REST-to-JMX bridge |

### Distributable — Mail (1 monitor)

| # | Monitor | Module | I/O Type |
|---|---------|--------|----------|
| 1 | MailTransportMonitor | `features/poller/monitors/core` | SMTP send + POP3/IMAP receive |

### Distributable — Other Network Protocols (9 monitors)

| # | Monitor | Module | I/O Type |
|---|---------|--------|----------|
| 1 | FtpMonitor | `features/poller/monitors/core` | FTP protocol |
| 2 | NtpMonitor | `features/poller/monitors/core` | NTP time sync |
| 3 | TrivialTimeMonitor | `features/poller/monitors/core` | TCP time service |
| 4 | SmbMonitor | `features/poller/monitors/core` | SMB/NetBIOS |
| 5 | ActiveMQMonitor | `features/poller/monitors/core` | ActiveMQ broker (managed nodes) |
| 6 | MemcachedMonitor | `features/poller/monitors/core` | Memcached protocol |
| 7 | NrpeMonitor | `features/poller/monitors/core` | Nagios NRPE agent |
| 8 | SSLCertMonitor | `features/poller/monitors/core` | SSL/TLS certificate check |
| 9 | Win32ServiceMonitor | `features/poller/monitors/core` | Windows service via SNMP |

### Distributable — WS-Man/WMI/VMware (4 monitors)

| # | Monitor | Module | I/O Type |
|---|---------|--------|----------|
| 1 | WsManMonitor | `features/wsman` | WS-Management protocol |
| 2 | WmiMonitor | `opennms-wmi` | Windows WMI queries |
| 3 | VmwareMonitor | `integrations/opennms-vmware` | VMware vCenter API |
| 4 | VmwareCimMonitor | `integrations/opennms-vmware` | VMware CIM queries |

### Distributable — Protocol Modules (3 monitors)

| # | Monitor | Module | I/O Type |
|---|---------|--------|----------|
| 1 | JCifsMonitor | `protocols/cifs` | CIFS/SMB file share access |
| 2 | NsclientMonitor | `protocols/nsclient` | NSClient++ agent |
| 3 | RadiusAuthMonitor | `protocols/radius` | RADIUS UDP authentication |

### Distributable — External (2 monitors)

| # | Monitor | Module | I/O Type |
|---|---------|--------|----------|
| 1 | AsteriskSIPPeerMonitor | `opennms-asterisk` | Asterisk AMI protocol |
| 2 | SeleniumMonitor | `protocols/selenium-monitor` | Selenium browser automation |

### Distributable — Script/Process Execution (2 monitors)

These run on the Minion host. Behavior depends on scripts/commands deployed there.

| # | Monitor | Module | I/O Type |
|---|---------|--------|----------|
| 1 | SystemExecuteMonitor | `features/poller/monitors/core` | OS process execution |
| 2 | BSFMonitor | `features/poller/monitors/core` | Bean Scripting Framework |

### Distributable — Synthetic (1 monitor)

No network I/O, no local state dependencies. Purely algorithmic.

| # | Monitor | Module | I/O Type |
|---|---------|--------|----------|
| 1 | LoopMonitor | `features/poller/monitors/core` | IP pattern match (no network I/O) |

### API Layer Adapter (1 — pass-through, not a standalone monitor)

| # | Monitor | Module | Notes |
|---|---------|--------|-------|
| 1 | ServicePollerImpl | `features/api-layer/common` | Integration API adapter; `getEffectiveLocation()` → `null` (delegates to wrapped poller) |

---

## ServiceCollector Classification

### Distributable — Collector-Level RPC (11 collectors)

These extend `AbstractRemoteServiceCollector`, which passes location through and supports parameter marshaling for Kafka RPC transport. The entire collector is serialized and sent to Minion for execution.

| # | Collector | Module | I/O Type |
|---|-----------|--------|----------|
| 1 | HttpCollector | `features/collection/collectors` | HTTP/HTTPS |
| 2 | JMXCollector | `features/collection/collectors` | JMX remote |
| 3 | Jsr160Collector | `features/collection/collectors` | JSR-160 JMX (backward compat wrapper) |
| 4 | JdbcCollector | `features/jdbc-collector` | JDBC database queries |
| 5 | PrometheusCollector | `features/prometheus-collector` | Prometheus exposition HTTP |
| 6 | WsManCollector | `features/wsman` | WS-Management protocol |
| 7 | VmwareCollector | `integrations/opennms-vmware` | VMware vCenter API |
| 8 | VmwareCimCollector | `integrations/opennms-vmware` | VMware CIM queries |
| 9 | WmiCollector | `opennms-wmi` | Windows WMI |
| 10 | NSClientCollector | `protocols/nsclient` | NSClient++ agent |
| 11 | XmlCollector | `protocols/xml` | HTTP/XML generic |

### Distributable — Protocol-Level RPC (3 collectors)

These extend `AbstractServiceCollector` (returns `DEFAULT_LOCATION_NAME`, throws `UnsupportedOperationException` for `marshalParameters()`). However, SNMP operations are proxied to Minion via `LocationAwareSnmpClient` at the protocol level. Network I/O still flows through Minion — the collector code runs locally but the SNMP packets originate from Minion.

| # | Collector | Module | I/O Type | Notes |
|---|-----------|--------|----------|-------|
| 1 | SnmpCollector | `features/collection/snmp-collector` | SNMP via LocationAwareSnmpClient | Legacy implementation |
| 2 | SnmpCollectorNG | `features/collection/snmp-collector` | SNMP via LocationAwareSnmpClient | Next-gen implementation |
| 3 | TcaCollector | `features/juniper-tca-collector` | SNMP (Juniper TCA specialization) | Uses SNMP proxy |

### API Layer Adapter (1 — pass-through, not a standalone collector)

| # | Collector | Module | Notes |
|---|-----------|--------|-------|
| 1 | ServiceCollectorImpl | `features/api-layer/common` | Integration API adapter; `getEffectiveLocation()` → `null` |

---

## Deletion List

### Source Files to Delete

| File | Reason |
|------|--------|
| `features/poller/monitors/core/src/main/java/.../MinionHeartbeatMonitor.java` | Self-monitoring anti-pattern |
| `features/poller/monitors/core/src/main/java/.../MinionRpcMonitor.java` | Self-monitoring anti-pattern |
| `features/poller/monitors/core/src/main/java/.../GpMonitor.java` | Deprecated; replaced by SystemExecuteMonitor |
| `features/poller/monitors/core/src/main/java/.../JschSshMonitor.java` | Deprecated; replaced by SshMonitor |
| `features/poller/monitors/core/src/test/java/.../MinionHeartbeatMonitorTest.java` | Test for deleted monitor (if exists) |
| `features/poller/monitors/core/src/test/java/.../MinionRpcMonitorTest.java` | Test for deleted monitor (if exists) |

### ServiceLoader Registrations to Update

Remove from `features/poller/monitors/core/src/main/resources/META-INF/services/org.opennms.netmgt.poller.ServiceMonitor`:
- `org.opennms.netmgt.poller.monitors.MinionHeartbeatMonitor`
- `org.opennms.netmgt.poller.monitors.MinionRpcMonitor`
- `org.opennms.netmgt.poller.monitors.GpMonitor`
- `org.opennms.netmgt.poller.monitors.JschSshMonitor`

### Configuration Cleanup

**Files to modify** (all 3 copies of `poller-configuration.xml`):
1. `opennms-base-assembly/src/main/filtered/etc/poller-configuration.xml`
2. `opennms-container/delta-v/pollerd-daemon-overlay/etc/poller-configuration.xml`
3. `opennms-container/delta-v/perspectivepollerd-overlay/etc/poller-configuration.xml`

**Changes per file:**
- Remove entire `<package name="Minion">` block (lines 278–308) — this package exists solely to monitor Minion infrastructure health
- Remove `<monitor service="Minion-Heartbeat" .../>` registration (line 354)
- Remove `<monitor service="Minion-RPC" .../>` registration (line 355)
- Remove `<monitor service="JMX-Minion" .../>` registration (line 365)
- Consider removing `<monitor service="OpenNMS-JVM" .../>` (line 364) — self-monitoring via JMX; should use Spring Boot actuators
- Consider removing `<monitor service="JMX-Kafka" .../>` (line 366) — monitoring Kafka infrastructure, not managed nodes

**Note on OpenNMS-JVM and JMX-Kafka:** These use `Jsr160Monitor` which is a legitimate distributable monitor. The issue is not the monitor class but the *service configuration* — they're configured to monitor Delta-V infrastructure (OpenNMS JVM, Kafka broker) rather than managed nodes. The monitor class itself stays; only the config entries for self-monitoring services are removed.

---

## Refactoring List

### PassiveServiceMonitor → Twin API

**Current state:** `PassiveServiceMonitor.poll()` calls `PassiveStatusKeeper.getInstance().getStatus(...)` — reads from a static singleton in the Pollerd JVM. `getEffectiveLocation()` forces Default location.

**Target state:** Use Twin API to publish PassiveStatusKeeper state to Minion. The monitor runs on Minion, reads from a Twin-backed status provider.

**Components to implement:**
1. `PassiveStatusTwinPublisher` (Pollerd side) — watches PassiveStatusKeeper for changes, publishes via `TwinPublisher.register()`
2. `PassiveStatusTwinSubscriber` (Minion side) — subscribes via `TwinSubscriber.subscribe()`, maintains local copy
3. Refactor `PassiveServiceMonitor` — read from Twin-backed provider instead of singleton; remove `getEffectiveLocation()` override

**Reference pattern:** `SnmpV3UserTwinPublisher` / `SnmpV3UserTwinSubscriber` in the codebase.

---

## Architecture Notes

### Two Patterns for Minion Delegation

1. **Monitor/Collector-level RPC** — The entire monitor/collector is serialized and sent to Minion via Kafka RPC. Used by all monitors (via `LocationAwarePollerClientImpl`) and by collectors extending `AbstractRemoteServiceCollector` (via `LocationAwareCollectorClientImpl`).

2. **Protocol-level RPC** — The collector code runs locally but individual protocol operations (SNMP gets/walks) are proxied to Minion via `LocationAwareSnmpClient`. Used by `SnmpCollector`, `SnmpCollectorNG`, and `TcaCollector`.

Both patterns satisfy the Minion-mandatory principle: no daemon container opens network connections to managed nodes.

### `getEffectiveLocation()` Contract

- **Default** (`AbstractServiceMonitor`): Returns location unchanged → inherently distributable
- **Override → `DEFAULT_MONITORING_LOCATION_ID`**: Forces local execution → non-distributable signal
- **Override → `null`** (`ServicePollerImpl`/`ServiceCollectorImpl`): API layer adapter, delegates to wrapped implementation

Only 3 monitors override to force Default: PassiveServiceMonitor, MinionHeartbeatMonitor, MinionRpcMonitor. All others are inherently distributable.
