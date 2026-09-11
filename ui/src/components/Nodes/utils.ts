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
  NodeColumnSelectionItem
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
