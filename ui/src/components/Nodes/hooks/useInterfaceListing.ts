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

// Pure logic replicating the legacy node-list interface display, see:
// - opennms-web-api DefaultNodeListService#createModelForNodes (interface selection)
// - DefaultNodeListService.IpInterfaceComparator / SnmpInterfaceComparator (ordering)
// - opennms-webapp WEB-INF/tags/element/nodelist.tag (labels/links)
//
// Field mapping from the legacy NodeListCommand params to the Vue NodeQueryFilter:
// - legacy `maclike`               -> filter.macAddress
// - legacy `snmpParm`/`snmpParmValue`/`snmpParmMatchType` (ifAlias/ifName/ifDescr, contains/equals)
//     -> filter.extendedSearch.snmpParams.{snmpIfAlias,snmpIfName,snmpIfDescription}
//        + filter.extendedSearch.snmpParams.snmpMatchType (MatchType.Equals / MatchType.Contains)
import { IpInterface, MatchType, NodeQueryFilter, SnmpInterface } from '@/types'

export interface InterfaceListRow {
  key: string // stable unique key for v-for
  label: string // link text
  suffix?: string // rendered after the link as " : {suffix}"
  href: string // absolute-from-baseHref legacy page link
}

export type InterfaceListMode =
  | { mode: 'default' }
  | { mode: 'maclike'; mac: string }
  | { mode: 'snmpParm'; attr: 'ifAlias' | 'ifName' | 'ifDescr'; value: string; matchType: 'contains' | 'equals' }

/**
 * Decide which interface-listing mode applies for the current node query filter, replicating
 * DefaultNodeListService#createModelForNodes's mode selection: its if/else-if chain checks
 * hasSnmpParm() before hasMaclike(), so snmpParm takes priority over maclike; snmpParm only
 * applies when exactly one of ifAlias/ifName/ifDescr is set; otherwise maclike applies if a MAC
 * filter is set, and failing that the default IP-interface listing applies.
 */
export const getInterfaceListMode = (filter: NodeQueryFilter): InterfaceListMode => {
  const snmpParams = filter.extendedSearch?.snmpParams
  if (snmpParams) {
    const candidates: Array<{ attr: 'ifAlias' | 'ifName' | 'ifDescr'; value?: string }> = [
      { attr: 'ifAlias', value: snmpParams.snmpIfAlias },
      { attr: 'ifName', value: snmpParams.snmpIfName },
      { attr: 'ifDescr', value: snmpParams.snmpIfDescription }
    ]
    const nonEmpty = candidates.filter(c => !!c.value && c.value.trim().length > 0)

    // Legacy could only ever have one snmpParm set at a time. If more than one of our extended
    // search fields is populated, fall through to the maclike/default modes.
    if (nonEmpty.length === 1) {
      // Default (undefined) must resolve to 'equals' here, matching buildSnmpQuery's and
      // parseSnmpParmParams's default: `wildcard = snmpMatchType === MatchType.Contains` is false
      // when snmpMatchType is undefined, so the FIQL filter narrows nodes by exact match. If this
      // resolved to 'contains' instead, the expanded interface panel would show interfaces that
      // didn't actually qualify the node under the (exact-match) filter.
      const matchType = snmpParams.snmpMatchType === MatchType.Contains ? 'contains' : 'equals'
      return { mode: 'snmpParm', attr: nonEmpty[0].attr, value: nonEmpty[0].value as string, matchType }
    }
  }

  // Gate on the NORMALIZED value, not just a non-blank trim(): a value that trims non-empty but
  // normalizes to '' (e.g. '--', '::', punctuation-only) must fall through to default mode. If it
  // entered maclike mode instead, filterMaclikeModeSnmp's `physAddr.includes('')` would match every
  // (non-deleted) SNMP interface on every node, while buildMaclikeQuery (useNodeQuery.ts) already
  // guards on the same normalized-empty check and applies no node-narrowing filter at all — the
  // client-side panel and the server-side node list would disagree about which nodes qualify.
  if (filter.macAddress && normalizeMacSearch(filter.macAddress).length > 0) {
    return { mode: 'maclike', mac: filter.macAddress }
  }

  return { mode: 'default' }
}

/**
 * Parse an IPv4 or IPv6 address string into its raw bytes. Returns null if unparseable.
 */
const ipToBytes = (ip: string): number[] | null => {
  if (ip.includes(':')) {
    return ipv6ToBytes(ip)
  }

  const parts = ip.split('.')
  if (parts.length !== 4) {
    return null
  }

  const bytes = parts.map(p => Number(p))
  if (bytes.some(b => !Number.isInteger(b) || b < 0 || b > 255)) {
    return null
  }

  return bytes
}

/**
 * Expand an embedded IPv4 dotted-quad (the final group of an IPv4-mapped/-compatible IPv6 address,
 * e.g. the '192.168.1.1' in '::ffff:192.168.1.1') into its two equivalent hextets. Returns null if
 * it isn't a valid dotted-quad.
 */
const ipv4TailToHextets = (tail: string): string[] | null => {
  const parts = tail.split('.')
  if (parts.length !== 4) {
    return null
  }

  const bytes = parts.map(p => Number(p))
  if (bytes.some(b => !Number.isInteger(b) || b < 0 || b > 255)) {
    return null
  }

  return [
    ((bytes[0] << 8) | bytes[1]).toString(16),
    ((bytes[2] << 8) | bytes[3]).toString(16)
  ]
}

const ipv6ToBytes = (ip: string): number[] | null => {
  let hextets: string[]

  if (ip.includes('::')) {
    const sides = ip.split('::')
    if (sides.length !== 2) {
      return null
    }
    const head = sides[0].length > 0 ? sides[0].split(':') : []
    const tail = sides[1].length > 0 ? sides[1].split(':') : []

    // IPv4-mapped/-compatible form (e.g. '::ffff:192.168.1.1'): the final tail group is a dotted
    // quad standing in for the LAST TWO hextets, not one. Expand it before computing how many
    // zero groups '::' fills in below -- otherwise the fill count (and therefore every byte from
    // that point on) is off by one, and the dotted-quad string itself would fall through to
    // parseInt(str, 16) as garbage.
    if (tail.length > 0 && tail[tail.length - 1].includes('.')) {
      const expanded = ipv4TailToHextets(tail[tail.length - 1])
      if (!expanded) {
        return null
      }
      tail.splice(tail.length - 1, 1, ...expanded)
    }

    const missing = 8 - head.length - tail.length
    if (missing < 0) {
      return null
    }
    hextets = [...head, ...Array(missing).fill('0'), ...tail]
  } else {
    hextets = ip.split(':')

    // Same embedded-IPv4 handling as above, for the non-'::' (fully-written-out) form.
    if (hextets.length > 0 && hextets[hextets.length - 1].includes('.')) {
      const expanded = ipv4TailToHextets(hextets[hextets.length - 1])
      if (!expanded) {
        return null
      }
      hextets.splice(hextets.length - 1, 1, ...expanded)
    }
  }

  if (hextets.length !== 8) {
    return null
  }

  const bytes: number[] = []
  for (const hextet of hextets) {
    const value = parseInt(hextet || '0', 16)
    if (Number.isNaN(value) || value < 0 || value > 0xffff) {
      return null
    }
    bytes.push((value >> 8) & 0xff, value & 0xff)
  }

  return bytes
}

/**
 * Compare two IP address strings by their raw bytes ascending (shorter byte arrays, i.e. IPv4,
 * sort before longer ones, i.e. IPv6), matching legacy's ByteArrayComparator use in
 * IpInterfaceComparator. Does NOT use localeCompare/string comparison.
 */
export const compareIpAddressBytes = (a: string, b: string): number => {
  const aBytes = ipToBytes(a) ?? []
  const bBytes = ipToBytes(b) ?? []

  if (aBytes.length !== bBytes.length) {
    return aBytes.length - bBytes.length
  }

  for (let i = 0; i < aBytes.length; i++) {
    if (aBytes[i] !== bBytes[i]) {
      return aBytes[i] - bBytes[i]
    }
  }

  return 0
}

/**
 * Compare two nullable strings the way Java's String.compareTo would, with nulls sorted last.
 */
const compareNullableStringsNullsLast = (a: unknown, b: unknown): number => {
  const aVal = a === undefined || a === null ? null : String(a)
  const bVal = b === undefined || b === null ? null : String(b)

  if (aVal === null || bVal === null) {
    if (aVal !== null) {
      return -1
    }
    if (bVal !== null) {
      return 1
    }
    return 0
  }

  if (aVal === bVal) {
    return 0
  }

  return aVal < bVal ? -1 : 1
}

/**
 * Compare two nullable numbers ascending, with null/undefined sorted last (same nulls-last
 * convention as compareNullableStringsNullsLast above). Guards against e.g. `a.ifIndex -
 * b.ifIndex` evaluating to NaN when a value is absent, which Array#sort treats as an unstable/
 * undefined ordering rather than "sorts last".
 */
const compareNullableNumbersNullsLast = (a: number | null | undefined, b: number | null | undefined): number => {
  const aVal = a === undefined || a === null ? null : a
  const bVal = b === undefined || b === null ? null : b

  if (aVal === null || bVal === null) {
    if (aVal !== null) {
      return -1
    }
    if (bVal !== null) {
      return 1
    }
    return 0
  }

  return aVal - bVal
}

/**
 * Replicates DefaultNodeListService.SnmpInterfaceComparator: ifName (nulls last, case-sensitive)
 * -> ifDescr (nulls last) -> ifIndex (nulls last) -> id.
 */
export const compareSnmpInterfaces = (a: SnmpInterface, b: SnmpInterface): number => {
  const nameDiff = compareNullableStringsNullsLast(a.ifName, b.ifName)
  if (nameDiff !== 0) {
    return nameDiff
  }

  const descrDiff = compareNullableStringsNullsLast(a.ifDescr, b.ifDescr)
  if (descrDiff !== 0) {
    return descrDiff
  }

  // ifIndex is typed as a required number, but real API responses can omit it; a plain `a.ifIndex
  // - b.ifIndex` is NaN in that case, which is why this uses the nulls-last numeric compare. `id`,
  // by contrast, IS guaranteed present (SnmpInterface.id: number, non-optional, per types/index.ts),
  // so a plain numeric compare remains safe/correct for it.
  const ifIndexDiff = compareNullableNumbersNullsLast(a.ifIndex, b.ifIndex)
  if (ifIndexDiff !== 0) {
    return ifIndexDiff
  }

  return a.id - b.id
}

/** Find, among this node's IP interfaces, the one whose embedded snmpInterface matches by id. */
const findAssociatedIpInterface = (snmpInterface: SnmpInterface, ipInterfaces: IpInterface[]): IpInterface | undefined => {
  return ipInterfaces.find(ip => ip.snmpInterface && ip.snmpInterface.id === snmpInterface.id)
}

/**
 * Build the row for an SNMP interface shared by the maclike and snmpParm modes: label falls back
 * from the associated IP interface's address, to ifName, to ifDescr, to a formatted ifIndex; href
 * links to the associated IP interface when found, otherwise to the SNMP interface itself.
 */
const buildSnmpInterfaceRow = (
  nodeId: string,
  snmpInterface: SnmpInterface,
  ipInterfaces: IpInterface[],
  baseHref: string,
  suffix: string,
  formatIfIndexFallback: (ifIndex: number) => string
): InterfaceListRow => {
  const associatedIp = findAssociatedIpInterface(snmpInterface, ipInterfaces)

  const label = associatedIp
    ? associatedIp.ipAddress
    : snmpInterface.ifName
      ? String(snmpInterface.ifName)
      : snmpInterface.ifDescr
        ? String(snmpInterface.ifDescr)
        : formatIfIndexFallback(snmpInterface.ifIndex)

  const href = associatedIp
    ? `${baseHref}element/interface.jsp?ipinterfaceid=${associatedIp.id}`
    : `${baseHref}element/snmpinterface.jsp?node=${nodeId}&ifindex=${snmpInterface.ifIndex}`

  return {
    key: `snmp-${snmpInterface.id}`,
    label,
    suffix,
    href
  }
}

/**
 * Normalize a MAC-like search value for matching/narrowing: lowercase, strip every non-hex
 * character (not just ':' and '-' — also '.', spaces, etc). This is a DELIBERATE DIFFERENCE from
 * the legacy backend's maclike behavior, not parity with it: legacy stripped only '[:-]', so a
 * Cisco-style dotted MAC like 'aabb.ccdd' never matched anything there. Stripping every non-hex
 * character means our maclike/snmpParm matching treats 'aabb.ccdd', 'aabb:ccdd', and 'aabbccdd' as
 * the same search, which is an improvement, not a port of the old behavior. Shared by the
 * client-side maclike match here, NodesTable.vue's buildSnmpNarrowing, and useNodeQuery.ts's
 * buildMaclikeQuery so all three treat a given input identically.
 */
export const normalizeMacSearch = (mac: string): string => mac.replace(/[^0-9a-fA-F]/g, '').toLowerCase()

/**
 * Filter predicate for default-mode IP interface rows (unsorted). Shared by getDefaultModeRows
 * (panel display, sorted+mapped) and countInterfaceRowsForNode (count-only, no sort needed).
 */
const filterDefaultModeIp = (ipInterfaces: IpInterface[]): IpInterface[] =>
  ipInterfaces.filter(ip => ip.isManaged !== 'D' && ip.ipAddress !== '0.0.0.0')

const getDefaultModeRows = (ipInterfaces: IpInterface[], baseHref: string): InterfaceListRow[] => {
  // No .slice() needed before .sort(): filterDefaultModeIp's .filter() already returns a fresh
  // array, so sorting it in place can't mutate anything the caller (or another mode) holds.
  return filterDefaultModeIp(ipInterfaces)
    .sort((a, b) => compareIpAddressBytes(a.ipAddress, b.ipAddress))
    .map(ip => ({
      key: `ip-${ip.id}`,
      label: ip.ipAddress,
      href: `${baseHref}element/interface.jsp?ipinterfaceid=${ip.id}`
    }))
}

/**
 * Filter predicate for maclike-mode SNMP interface rows (unsorted). Shared by getMaclikeModeRows
 * (panel display, sorted+mapped) and countInterfaceRowsForNode (count-only, no sort needed).
 */
const filterMaclikeModeSnmp = (mac: string, snmpInterfaces: SnmpInterface[]): SnmpInterface[] => {
  const normalizedMac = normalizeMacSearch(mac)

  return snmpInterfaces
    .filter(snmp => snmp.collectFlag !== 'D' && snmp.physAddr != null && String(snmp.physAddr).toLowerCase().includes(normalizedMac))
}

const getMaclikeModeRows = (
  nodeId: string,
  mac: string,
  ipInterfaces: IpInterface[],
  snmpInterfaces: SnmpInterface[],
  baseHref: string
): InterfaceListRow[] => {
  // No .slice() needed before .sort(): filterMaclikeModeSnmp's .filter() already returns a fresh
  // array.
  return filterMaclikeModeSnmp(mac, snmpInterfaces)
    .sort(compareSnmpInterfaces)
    .map(snmp => buildSnmpInterfaceRow(nodeId, snmp, ipInterfaces, baseHref, String(snmp.physAddr), ifIndex => `ifIndex:${ifIndex}`))
}

/**
 * Build the legacy SQL-LIKE-flavored "contains" regex: lowercase the value, backslash-escape
 * every non-word character, turn escaped '%' into '.*' and '_' into '.', then wrap in '.*' on
 * both sides. Matched with full-string anchoring (replicating Java's String#matches).
 */
const buildContainsRegex = (value: string): RegExp => {
  const escaped = value.toLowerCase().replace(/(\W)/g, '\\$1')
  const withPercentWildcard = escaped.replace(/\\%/g, '.*')
  const withUnderscoreWildcard = withPercentWildcard.replace(/_/g, '.')
  return new RegExp(`^.*${withUnderscoreWildcard}.*$`)
}

const matchesSnmpParm = (value: string, attrValue: string, matchType: 'contains' | 'equals'): boolean => {
  if (matchType === 'equals') {
    return attrValue.toLowerCase() === value.toLowerCase()
  }

  return buildContainsRegex(value).test(attrValue.toLowerCase())
}

/**
 * Filter predicate for snmpParm-mode SNMP interface rows (unsorted). Shared by
 * getSnmpParmModeRows (panel display, sorted+mapped) and countInterfaceRowsForNode (count-only,
 * no sort needed).
 */
const filterSnmpParmModeSnmp = (
  mode: { attr: 'ifAlias' | 'ifName' | 'ifDescr'; value: string; matchType: 'contains' | 'equals' },
  snmpInterfaces: SnmpInterface[]
): SnmpInterface[] => {
  return snmpInterfaces.filter((snmp) => {
    if (snmp.collectFlag === 'D') {
      return false
    }
    const attrValue = snmp[mode.attr]
    if (attrValue === null || attrValue === undefined) {
      return false
    }
    return matchesSnmpParm(mode.value, String(attrValue), mode.matchType)
  })
}

const getSnmpParmModeRows = (
  nodeId: string,
  mode: { attr: 'ifAlias' | 'ifName' | 'ifDescr'; value: string; matchType: 'contains' | 'equals' },
  ipInterfaces: IpInterface[],
  snmpInterfaces: SnmpInterface[],
  baseHref: string
): InterfaceListRow[] => {
  // No .slice() needed before .sort(): filterSnmpParmModeSnmp's .filter() already returns a fresh
  // array.
  return filterSnmpParmModeSnmp(mode, snmpInterfaces)
    .sort(compareSnmpInterfaces)
    .map(snmp => buildSnmpInterfaceRow(nodeId, snmp, ipInterfaces, baseHref, String(snmp[mode.attr]), ifIndex => `ifIndex ${ifIndex}`))
}

/**
 * Compute the interface-listing rows for a single node, for the given mode.
 */
export const getInterfaceRowsForNode = (
  nodeId: string,
  mode: InterfaceListMode,
  ipInterfaces: IpInterface[], // this node's IP interfaces
  snmpInterfaces: SnmpInterface[], // this node's SNMP interfaces (empty in default mode)
  baseHref: string
): InterfaceListRow[] => {
  switch (mode.mode) {
    case 'maclike':
      return getMaclikeModeRows(nodeId, mode.mac, ipInterfaces, snmpInterfaces, baseHref)
    case 'snmpParm':
      return getSnmpParmModeRows(nodeId, mode, ipInterfaces, snmpInterfaces, baseHref)
    case 'default':
    default:
      return getDefaultModeRows(ipInterfaces, baseHref)
  }
}

/**
 * Count the interface-listing rows for a single node, for the given mode — same filter
 * predicates as getInterfaceRowsForNode, but skips sorting and row-building (label/href), which
 * are irrelevant to a count. Use this instead of getInterfaceRowsForNode(...).length when only
 * the count is needed (e.g. per-row expandability, footer totals) to avoid rebuilding/sorting the
 * full row list just to throw the ordering away.
 */
export const countInterfaceRowsForNode = (
  mode: InterfaceListMode,
  ipInterfaces: IpInterface[], // this node's IP interfaces
  snmpInterfaces: SnmpInterface[] // this node's SNMP interfaces (empty in default mode)
): number => {
  switch (mode.mode) {
    case 'maclike':
      return filterMaclikeModeSnmp(mode.mac, snmpInterfaces).length
    case 'snmpParm':
      return filterSnmpParmModeSnmp(mode, snmpInterfaces).length
    case 'default':
    default:
      return filterDefaultModeIp(ipInterfaces).length
  }
}

/**
 * Sum of countInterfaceRowsForNode(...) across the given node ids, using the two maps.
 * A node id missing from either map is treated as having no interfaces of that kind.
 */
export const countInterfacesForNodes = (
  nodeIds: string[],
  mode: InterfaceListMode,
  ipMap: Map<string, IpInterface[]>,
  snmpMap: Map<string, SnmpInterface[]>
): number => {
  return nodeIds.reduce((total, nodeId) => {
    const ipInterfaces = ipMap.get(nodeId) ?? []
    const snmpInterfaces = snmpMap.get(nodeId) ?? []
    return total + countInterfaceRowsForNode(mode, ipInterfaces, snmpInterfaces)
  }, 0)
}
