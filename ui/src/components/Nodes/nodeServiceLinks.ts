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

// The Services menu of the Node Details links row: shortcuts to a node's remote-access and web
// services, as the legacy node page's links (element/node.jsp, createLinkForService). Kept apart
// from the components so the address rules can be tested on their own.

import { NodeLinkService } from '@/types'

export type { NodeLinkService }

export interface ServiceLinkDefinition {
  // The service type name to look for on the node.
  service: string
  label: string
  prefix: string
  suffix: string
  // Web pages open in a new tab; protocol handlers (telnet, ssh, rdp) hand off to the OS.
  newTab: boolean
}

// In the legacy page's order.
export const SERVICE_LINKS: ServiceLinkDefinition[] = [
  { service: 'Telnet', label: 'Telnet', prefix: 'telnet://', suffix: '', newTab: false },
  { service: 'SSH', label: 'SSH', prefix: 'ssh://', suffix: '', newTab: false },
  { service: 'HTTP', label: 'HTTP', prefix: 'http://', suffix: '/', newTab: true },
  { service: 'HTTPS', label: 'HTTPS', prefix: 'https://', suffix: '/', newTab: true },
  { service: 'Dell-OpenManage', label: 'OpenManage', prefix: 'https://', suffix: ':1311', newTab: true },
  { service: 'MS-RDP', label: 'Microsoft RDP', prefix: 'rdp://', suffix: ':3389', newTab: false }
]

export const SERVICE_LINK_NAMES = SERVICE_LINKS.map(link => link.service)

const withoutZone = (ip: string) => ip.split('%')[0]

/** An address as bytes: 4 for IPv4, 16 for IPv6. Undefined for anything that is neither. */
export const ipAddressBytes = (ip: string): number[] | undefined => {
  const address = withoutZone(ip.trim())

  if (/^\d{1,3}(\.\d{1,3}){3}$/.test(address)) {
    const bytes = address.split('.').map(Number)

    return bytes.every(b => b <= 255) ? bytes : undefined
  }

  if (!address.includes(':')) {
    return undefined
  }

  // An IPv6 address may end in an embedded IPv4 address (::ffff:10.0.0.1): two groups' worth.
  let text = address
  const embedded = text.match(/^(.*:)(\d{1,3}(?:\.\d{1,3}){3})$/)

  if (embedded) {
    const v4 = ipAddressBytes(embedded[2])

    if (!v4) {
      return undefined
    }

    text = `${embedded[1]}${((v4[0] << 8) | v4[1]).toString(16)}:${((v4[2] << 8) | v4[3]).toString(16)}`
  }

  const halves = text.split('::')

  if (halves.length > 2) {
    return undefined
  }

  const groups = (part: string) => (part ? part.split(':') : [])
  const head = groups(halves[0])
  const tail = halves.length === 2 ? groups(halves[1]) : []
  const missing = 8 - head.length - tail.length

  if ((halves.length === 2 && missing < 1) || (halves.length === 1 && missing !== 0)) {
    return undefined
  }

  const all = [...head, ...Array(halves.length === 2 ? missing : 0).fill('0'), ...tail]

  if (!all.every(g => /^[0-9a-f]{1,4}$/i.test(g))) {
    return undefined
  }

  return all.flatMap((g) => {
    const value = parseInt(g, 16)

    return [value >> 8, value & 0xff]
  })
}

// As InetAddressUtils.getLowestInetAddress's comparator: shorter (IPv4) first, then byte by byte.
const compareBytes = (a: number[], b: number[]) => {
  if (a.length !== b.length) {
    return a.length - b.length
  }

  for (let i = 0; i < a.length; i++) {
    if (a[i] !== b[i]) {
      return a[i] - b[i]
    }
  }

  return 0
}

/** The lowest of the addresses, as the legacy page chose which one to link to. */
export const lowestAddress = (addresses: string[]): string | undefined => {
  let lowest: { ip: string, bytes: number[] } | undefined

  for (const ip of addresses) {
    const bytes = ipAddressBytes(ip)

    if (bytes && (!lowest || compareBytes(bytes, lowest.bytes) < 0)) {
      lowest = { ip, bytes }
    }
  }

  return lowest?.ip
}

/** The address as a URL host: an IPv6 address bracketed (the legacy page did not), zone id dropped. */
export const urlHost = (ip: string): string => {
  const address = withoutZone(ip)

  return address.includes(':') ? `[${address}]` : address
}

export interface NodeServiceLink {
  label: string
  url: string
  newTab: boolean
}

/**
 * One link per service the node has, to the lowest address it runs on, in the legacy order; a
 * service the node does not have gets none.
 */
export const buildServiceLinks = (services: NodeLinkService[]): NodeServiceLink[] =>
  SERVICE_LINKS.flatMap((definition) => {
    const ip = lowestAddress(services.filter(s => s.serviceName === definition.service).map(s => s.ipAddress))

    return ip ? [{ label: definition.label, url: `${definition.prefix}${urlHost(ip)}${definition.suffix}`, newTab: definition.newTab }] : []
  })
