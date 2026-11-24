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
// Most daemons answer asynchronously with reloadDaemonConfigSuccessful/Failed.

// How a daemon answers a reload: 'reported' sends reloadDaemonConfigSuccessful or
// reloadDaemonConfigFailed, 'successOnly' sends Successful even when the reload
// failed, 'none' sends neither and leaves the outcome in the daemon's log only.
export type ReloadOutcome = 'reported' | 'successOnly' | 'none'

export interface ReloadableDaemon {
  name: string
  label: string
  description: string
  outcome: ReloadOutcome
}

// Every daemon with a reloadDaemonConfig handler in the codebase; each handler
// subscribes independently, so this union is hand-maintained. All of them match
// the daemonName parm case-insensitively. Components addressed by their own name
// (provisioning adapters, correlation engines, northbound interfaces, TokenAuth)
// are not daemons and are not listed.
export const RELOADABLE_DAEMONS: ReloadableDaemon[] = [
  { name: 'Ackd', label: 'Ackd', outcome: 'reported',
    description: 'Re-reads ackd-configuration.xml and restarts the acknowledgment readers.' },
  { name: 'Alarmd', label: 'Alarmd', outcome: 'reported',
    description: 'Reloads the alarm correlation rules (Drools).' },
  { name: 'Bsmd', label: 'Bsmd', outcome: 'reported',
    description: 'Reloads the business services from the database.' },
  { name: 'Collectd', label: 'Collectd', outcome: 'reported',
    description: 'Re-reads collectd-configuration.xml, resizes the collection thread pool, and rebuilds the collection schedule.' },
  { name: 'Discovery', label: 'Discovery', outcome: 'reported',
    description: 'Re-reads discovery-configuration.xml and restarts the discovery schedule.' },
  { name: 'Enlinkd', label: 'Enlinkd', outcome: 'none',
    description: 'Re-reads enlinkd-configuration.xml, resizes its thread pools, and reschedules link discovery.' },
  { name: 'Eventd', label: 'Eventd', outcome: 'reported',
    description: 'Nothing to re-read: event definitions are stored in the database and take effect when saved.' },
  { name: 'Notifd', label: 'Notifd', outcome: 'reported',
    description: 'Reloads users, groups, notifications, destination paths, and notification commands.' },
  { name: 'PerspectivePoller', label: 'Perspective Poller', outcome: 'successOnly',
    description: 'Re-reads poller-configuration.xml and restarts perspective polling.' },
  { name: 'Pollerd', label: 'Pollerd', outcome: 'reported',
    description: 'Re-reads poller-configuration.xml, resizes the poller thread pool, and reschedules every monitored service.' },
  { name: 'Provisiond', label: 'Provisiond', outcome: 'reported',
    description: 'Re-reads provisiond-configuration.xml, rebuilds the requisition import schedule, and resizes its thread pools.' },
  { name: 'Scriptd', label: 'Scriptd', outcome: 'none',
    description: 'Re-reads scriptd-configuration.xml, runs the stop scripts, re-registers the script engines, and runs the start scripts.' },
  { name: 'SnmpPoller', label: 'SNMP Interface Poller', outcome: 'reported',
    description: 'Re-reads snmp-interface-poller-configuration.xml, resizes its thread pool, and reschedules every SNMP interface.' },
  { name: 'Syslogd', label: 'Syslogd', outcome: 'reported',
    description: 'Re-reads syslogd-configuration.xml and restarts the syslog listeners.' },
  { name: 'Telemetryd', label: 'Telemetryd', outcome: 'reported',
    description: 'Re-reads telemetryd-configuration.xml and recreates its listeners, queues, and adapters.' },
  { name: 'Threshd', label: 'Threshd', outcome: 'none',
    description: 'Re-reads threshd-configuration.xml and thresholds.xml and reinitializes the thresholding state.' },
  { name: 'Ticketd', label: 'Ticketd', outcome: 'none',
    description: 'No effect with the default ticketer service layer; the Drools ticketer rebuilds its rules.' },
  { name: 'Translator', label: 'Translator', outcome: 'reported',
    description: 'Re-reads translator-configuration.xml and re-registers for the events it translates.' },
  { name: 'Trapd', label: 'Trapd', outcome: 'reported',
    description: 'Reloads the trap configuration, restarts the trap listener, and republishes the configuration to Minions.' },
  { name: 'Vacuumd', label: 'Vacuumd', outcome: 'reported',
    description: 'Re-reads vacuumd-configuration.xml and reschedules its automations.' }
]

const RELOAD_UEI = 'uei.opennms.org/internal/reloadDaemonConfig'

const escapeXml = (value: string): string =>
  value.replace(/[<>&'"]/g, c => ({ '<': '&lt;', '>': '&gt;', '&': '&amp;', '\'': '&apos;', '"': '&quot;' }[c] as string))

// POST /rest/events accepts the JAXB event XML; source and time are filled in
// server-side. 202 Accepted means the event reached the bus, not that the
// reload finished.
export const reloadDaemon = async (daemonName: string): Promise<void> => {
  const xml = '<event xmlns="http://xmlns.opennms.org/xsd/event">'
    + `<uei>${RELOAD_UEI}</uei>`
    + '<parms><parm><parmName>daemonName</parmName>'
    + `<value type="string" encoding="text">${escapeXml(daemonName)}</value>`
    + '</parm></parms></event>'
  await rest.post('/events', xml, { headers: { 'Content-Type': 'application/xml' }})
}
