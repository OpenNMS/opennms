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
  name: string
  label: string
  description: string
}

// Every daemon with a reloadDaemonConfig handler in the codebase; each handler
// subscribes independently, so this union is hand-maintained. All handlers
// match the daemonName parm case-insensitively. Provisioning adapters
// (Provisiond.*) and correlation engines also accept targeted reloads but are
// components addressed by their own name, not daemons, so they are not listed.
export const RELOADABLE_DAEMONS: ReloadableDaemon[] = [
  { name: 'Ackd', label: 'Ackd', description: 'Acknowledgment readers and their schedules.' },
  { name: 'Alarmd', label: 'Alarmd', description: 'Alarm correlation rules (Drools).' },
  { name: 'Bsmd', label: 'Bsmd', description: 'Business service hierarchy and reduction rules.' },
  { name: 'Collectd', label: 'Collectd', description: 'Performance data collection packages and schedules.' },
  { name: 'Discovery', label: 'Discovery', description: 'Discovery ranges, foreign sources, and schedules.' },
  { name: 'Enlinkd', label: 'Enlinkd', description: 'Topology link discovery protocols and schedules.' },
  { name: 'Eventd', label: 'Eventd', description: 'Event configuration (eventconf) and processing.' },
  { name: 'Notifd', label: 'Notifd', description: 'Notifications, destination paths, and notice queues.' },
  { name: 'PerspectivePoller', label: 'Perspective Poller', description: 'Monitoring perspective polling packages.' },
  { name: 'Pollerd', label: 'Pollerd', description: 'Service polling packages, thresholds, and outage handling.' },
  { name: 'Provisiond', label: 'Provisiond', description: 'Requisition import schedule.' },
  { name: 'Reportd', label: 'Reportd', description: 'Scheduled report definitions and delivery.' },
  { name: 'Scriptd', label: 'Scriptd', description: 'Event-driven scripts and script engines.' },
  { name: 'Statsd', label: 'Statsd', description: 'Statistics report definitions and schedules.' },
  { name: 'Syslogd', label: 'Syslogd', description: 'Syslog reception and matching rules.' },
  { name: 'Telemetryd', label: 'Telemetryd', description: 'Telemetry listeners, parsers, and adapters.' },
  { name: 'Threshd', label: 'Threshd', description: 'Thresholding configuration and package assignments.' },
  { name: 'Ticketd', label: 'Ticketd', description: 'Trouble-ticketing integration settings.' },
  { name: 'Tl1d', label: 'Tl1d', description: 'TL1 network element connections.' },
  { name: 'Translator', label: 'Translator', description: 'Event translation specifications.' },
  { name: 'Trapd', label: 'Trapd', description: 'SNMP trap reception configuration.' },
  { name: 'Vacuumd', label: 'Vacuumd', description: 'Database automations, triggers, and actions.' }
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
