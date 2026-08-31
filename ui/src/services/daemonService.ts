///
/// Licensed to The OpenNMS Group, Inc (TOG) under one or more
/// contributor license agreements.  See the LICENSE.md file
/// distributed with this work for additional information
/// regarding copyright ownership.
///
/// TOG licenses this file to You under the GNU Affero General
/// Public License Version 3 (the "License") or (at your option)
/// any later version.  You may not use this file except in
/// compliance with the License.  You may obtain a copy of the
/// License at:
///
///      https://www.gnu.org/licenses/agpl-3.0.txt
///
/// Unless required by applicable law or agreed to in writing,
/// software distributed under the License is distributed on an
/// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
/// either express or implied.  See the License for the specific
/// language governing permissions and limitations under the
/// License.
///

import { rest } from './axiosInstances'

// Daemon Management (NMS-4471), reload-only scope: reloading a daemon's config
// is done by publishing the reloadDaemonConfig event through the existing v1
// events API — the same event the `opennms:reload-daemon` Karaf command sends.
// The daemon answers asynchronously with reloadDaemonConfigSuccessful/Failed.
// A status API and on-page status display are follow-up tickets.

export interface ReloadableDaemon {
  // the exact daemonName parm value the daemon matches on (casing matters)
  name: string
  label: string
  description: string
}

// Mirrors DaemonReloadEnum (core/lib), the canonical set the Karaf
// reload-daemon command completes on. Keep in sync when the enum grows.
export const RELOADABLE_DAEMONS: ReloadableDaemon[] = [
  { name: 'alarmd', label: 'Alarmd', description: 'Alarm processing: reduction, correlation, and northbound rules.' },
  { name: 'Collectd', label: 'Collectd', description: 'Performance data collection packages and schedules.' },
  { name: 'Eventd', label: 'Eventd', description: 'Event configuration (eventconf) and processing.' },
  { name: 'Notifd', label: 'Notifd', description: 'Notifications, destination paths, and notice queues.' },
  { name: 'Pollerd', label: 'Pollerd', description: 'Service polling packages, thresholds, and outage handling.' },
  { name: 'syslogd', label: 'Syslogd', description: 'Syslog reception and matching rules.' },
  { name: 'Telemetryd', label: 'Telemetryd', description: 'Telemetry listeners, parsers, and adapters.' },
  { name: 'trapd', label: 'Trapd', description: 'SNMP trap reception configuration.' }
]

const RELOAD_UEI = 'uei.opennms.org/internal/reloadDaemonConfig'

const escapeXml = (value: string): string =>
  value.replace(/[<>&'"]/g, c => ({ '<': '&lt;', '>': '&gt;', '&': '&amp;', '\'': '&apos;', '"': '&quot;' }[c] as string))

// POST /rest/events accepts the JAXB event XML; source and time are filled in
// server-side. 202 Accepted means the event reached the bus, not that the
// reload finished — that outcome arrives as a follow-up event.
export const reloadDaemon = async (daemonName: string): Promise<void> => {
  const xml = '<event xmlns="http://xmlns.opennms.org/xsd/event">'
    + `<uei>${RELOAD_UEI}</uei>`
    + '<parms><parm><parmName>daemonName</parmName>'
    + `<value type="string" encoding="text">${escapeXml(daemonName)}</value>`
    + '</parm></parms></event>'
  await rest.post('/events', xml, { headers: { 'Content-Type': 'application/xml' }})
}
