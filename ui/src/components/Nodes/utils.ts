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

import {
  Node,
  NodeColumnSelectionItem,
  SnmpIfStatus,
  SnmpInterface
} from '@/types'
import { isNumber } from '@/lib/utils'
import { normalizeMacSearch, type InterfaceListMode } from './hooks/useInterfaceListing'

/**
 * Construct an array of Feather Table CSS classes for the given configured node table columns.
 * These start with 't', then ('l', 'r', 'c') for (left, right, center), then the 1 based column index.
 * e.g. 'tl1': left-align 1st column
 * 'tr7': right-align 7th colunn
 */
export const getTableCssClasses = (columns: NodeColumnSelectionItem[]) => {
  const classes: string[] = columns.filter(col => col.selected).map((col, i) => {
    let t = 'tl'

    if (col.id === 'flows') {
      t = 'tc'
    }

    // +2 : one since Feather table column classes are 1-based, one for the first action column which isn't in 'columns'
    return `${t}${i + 2}`
  })

  // add 'action' column
  return ['tl1', ...classes]
}

export const hasIngressFlow = (node: Node) => {
  return node.lastIngressFlow && isNumber(node.lastIngressFlow)
}

export const hasEgressFlow = (node: Node) => {
  return node.lastEgressFlow && isNumber(node.lastEgressFlow)
}

export const defaultColumns: NodeColumnSelectionItem[] = [
  { id: 'id', label: 'ID', selected: false, order: 0 },
  { id: 'label', label: 'Node Label', selected: true, order: 1 },
  { id: 'ipaddress', label: 'IP Address', selected: true, order: 2 },
  { id: 'location', label: 'Monitoring Location', selected: true, order: 3 },
  { id: 'foreignSource', label: 'Foreign Source', selected: true, order: 4 },
  { id: 'foreignId', label: 'Foreign ID', selected: true, order: 5 },
  { id: 'sysContact', label: 'Sys Contact', selected: false, order: 6 },
  { id: 'sysLocation', label: 'Sys Location', selected: false, order: 7 },
  { id: 'sysDescription', label: 'Sys Description', selected: false, order: 8 },
  { id: 'flows', label: 'Flows', selected: false, order: 9 }
]

export const getNodeStatusString = (node: Node) => {
  const status = (node?.type ?? ' ').toUpperCase()

  if (status.length === 0) {
    return 'Unknown'
  }

  const firstChar = status.charAt(0)

  switch (firstChar) {
    case 'A':
      return 'Active'
    case 'D':
      return 'Deleted'
    default:
      return 'Unknown'
  }
}

// Characters that make a value unsafe to splice raw into the FIQL attribute-narrowing term below:
// - '%' / '_' are SQL-LIKE wildcards to the server's FIQL '==*value*' match (literal), while
//   useInterfaceListing.ts's client-side matchesSnmpParm() treats them as SQL-LIKE wildcards — the
//   server narrowing would no longer be a superset of the client match.
// - ',' / ';' are FIQL set operators (OR / AND). sanitizeSearchTerm neutralizes them in other FIQL
//   builders by replacing them with spaces, but doing that here has the same superset problem as
//   '%'/'_': the narrowing sent to the server would search for something other than the exact
//   value, so it could exclude rows the client-side match still considers a hit.
// - '(' / ')' are FIQL grouping delimiters. Left in raw, they can produce an unbalanced FIQL
//   expression that fails to parse server-side — surfacing client-side as "No interfaces".
// If the value contains any of these, omit the attribute narrowing entirely; the node.id scoping
// alone still limits the fetch to the current page, and exact contains/equals semantics are
// re-applied client-side anyway (see buildSnmpNarrowing below).
const UNSAFE_NARROWING_CHARS = /[%_,;()]/

// Build the FIQL narrowing expression passed to nodeStore.getSnmpInterfacesForNodes so we only
// fetch the SNMP interfaces relevant to the active maclike/snmpParm mode (see
// getNodeSnmpInterfaceQuery).
export const buildSnmpNarrowing = (mode: InterfaceListMode): string | undefined => {
  if (mode.mode === 'maclike') {
    // normalizeMacSearch strips every non-hex character, so the result can never contain any of
    // UNSAFE_NARROWING_CHARS above — no further guard needed here.
    return `physAddr==*${normalizeMacSearch(mode.mac)}*`
  }

  if (mode.mode === 'snmpParm') {
    if (UNSAFE_NARROWING_CHARS.test(mode.value)) {
      return undefined
    }

    return `${mode.attr}==*${mode.value}*`
  }

  return undefined
}

const SNMP_IF_STATUS_LABELS: Record<SnmpIfStatus, string> = {
  [SnmpIfStatus.UP]: 'Up',
  [SnmpIfStatus.DOWN]: 'Down',
  [SnmpIfStatus.TESTING]: 'Testing',
  [SnmpIfStatus.UNKNOWN]: 'Unknown',
  [SnmpIfStatus.DORMANT]: 'Dormant',
  [SnmpIfStatus.NOT_PRESENT]: 'Not Present',
  [SnmpIfStatus.LOWER_LAYER_DOWN]: 'Lower Layer Down'
}

export type SnmpInterfaceStatus = 'UP' | 'DOWN' | 'UNKNOWN'

/**
 * Status shown in the SNMP interfaces table, mirroring setStylesForSnmpInterfaces()
 * in the JSP interfaces page
 * (core/web-assets/src/main/assets/js/apps/onms-interfaces/onms-interfaces-app/index.js).
 *
 * An interface that is not administratively up is UNKNOWN rather than DOWN:
 * nobody is asking it to run, so its operational status says nothing about
 * whether anything is wrong. Only when it is meant to be up does ifOperStatus
 * decide between UP and DOWN.
 *
 * Note this is NOT the isManaged/isDown rule the same file applies to IP
 * interfaces -- SNMP interfaces carry neither field.
 */
export const snmpInterfaceStatus = (snmpInterface: SnmpInterface): SnmpInterfaceStatus => {
  if (snmpInterface.ifAdminStatus !== SnmpIfStatus.UP) {
    return 'UNKNOWN'
  }

  return snmpInterface.ifOperStatus === SnmpIfStatus.UP ? 'UP' : 'DOWN'
}

/**
 * One IF-MIB status as '<value> (<label>)', e.g. '5 (Dormant)'.
 *
 * A value outside the MIB's range is shown bare rather than guessed at -- an
 * agent reporting 9 is telling us something, and labelling it 'Unknown' would
 * conflate it with ifOperStatus 4, which means exactly that. An interface the
 * poller has not reached yet carries no status at all.
 */
export const snmpIfStatusText = (status: number | undefined | null) => {
  if (status === undefined || status === null) {
    return 'N/A'
  }

  const label = SNMP_IF_STATUS_LABELS[status as SnmpIfStatus]

  return label ? `${status} (${label})` : `${status}`
}

/** Tooltip behind the status tag: the two raw IF-MIB statuses the status is derived from. */
export const snmpInterfaceStatusTooltip = (snmpInterface: SnmpInterface) =>
  [
    `Admin Status: ${snmpIfStatusText(snmpInterface.ifAdminStatus)}`,
    `Operational Status: ${snmpIfStatusText(snmpInterface.ifOperStatus)}`
  ].join('\n')

/** Tooltip behind the ifName cell, which is where ifDescr is surfaced now it has no column. */
export const snmpInterfaceNameTooltip = (snmpInterface: SnmpInterface) =>
  [
    `Name: ${snmpInterface.ifName || 'N/A'}`,
    `Description: ${snmpInterface.ifDescr || 'N/A'}`
  ].join('\n')

/**
 * Case-insensitive "does any of these values contain the term" match, for the client-side table
 * filters. `term` is expected already trimmed and lowercased (useDebouncedSearch does it once).
 *
 * Absent values simply do not match -- the tables render those cells as 'N/A', but matching that
 * placeholder would make 'a' pick up every row with a missing field.
 */
export const matchesSearchTerm = (term: string, values: Array<string | number | null | undefined>) => {
  if (!term) {
    return true
  }

  return values.some(value =>
    value !== null && value !== undefined && String(value).toLowerCase().includes(term))
}

// Thresholds and unit names from SIUtils.getHumanReadableIfSpeed
// (opennms-util/src/main/java/org/opennms/core/utils/SIUtils.java), which is the platform's
// canonical rendering of an ifSpeed. Decimal, not binary: a megabit is 1000000 bits.
const IF_SPEED_UNITS = [
  { divisor: 1000000000, units: 'Gbps' },
  { divisor: 1000000, units: 'Mbps' },
  { divisor: 1000, units: 'kbps' }
]

/**
 * An ifSpeed in bits per second as a readable string -- '100 Mbps', '2.5 Gbps', '0 bps'.
 *
 * Follows SIUtils.getHumanReadableIfSpeed: an exact multiple of the unit prints with no decimals
 * at all (DecimalFormat "0"), anything else with one to three (DecimalFormat "0.0##"), and
 * grouping is off in both, so a very large value reads '5000 Gbps' rather than '5,000 Gbps'.
 *
 * This is display only. The column still sorts on the raw number, since sorting these strings
 * would put '1 Gbps' before '100 Mbps'.
 */
export const formatIfSpeed = (ifSpeed: number | null | undefined) => {
  if (ifSpeed === null || ifSpeed === undefined) {
    return 'N/A'
  }

  for (const { divisor, units } of IF_SPEED_UNITS) {
    if (ifSpeed >= divisor) {
      const scaled = ifSpeed / divisor

      const text = ifSpeed % divisor === 0
        ? String(scaled)
        : scaled.toLocaleString('en-US', {
          minimumFractionDigits: 1,
          maximumFractionDigits: 3,
          useGrouping: false
        })

      return `${text} ${units}`
    }
  }

  return `${ifSpeed} bps`
}
