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

import { describe, it, expect } from 'vitest'
import { computeGhostLinks, resolveLinkBindings } from '@/components/Topology/linkHints'
import type { DiscoveredNeighbor } from '@/types/topology'

const lldp = (neighborNodeId: number, localPort?: string, remotePort?: string): DiscoveredNeighbor => ({
  neighborNodeId,
  neighborLabel: `node-${neighborNodeId}`,
  linkType: 'lldp',
  localPort,
  remotePort
})

const noLinks = () => false

describe('computeGhostLinks', () => {
  it('hints only when both endpoints are placed', () => {
    const neighbors = { 1: [lldp(2), lldp(3)] }
    const hints = computeGhostLinks(neighbors, new Set(['1', '2']), noLinks)
    expect(hints).toHaveLength(1)
    expect(hints[0]).toMatchObject({ sourceId: 'placed-1', targetId: 'placed-2' })
  })

  it('collapses an adjacency reported from both ends into one hint', () => {
    const neighbors = {
      1: [lldp(2, 'eth1', 'eth7')],
      2: [lldp(1, 'eth7', 'eth1')]
    }
    const hints = computeGhostLinks(neighbors, new Set(['1', '2']), noLinks)
    expect(hints).toHaveLength(1)
    // Orientation follows the reporting side that won the dedupe.
    expect(hints[0].binding.protocol).toBe('lldp')
  })

  it('prefers the more precise protocol when discovered several ways', () => {
    const neighbors: Record<number, DiscoveredNeighbor[]> = {
      1: [
        { neighborNodeId: 2, neighborLabel: 'n2', linkType: 'ospf' },
        { neighborNodeId: 2, neighborLabel: 'n2', linkType: 'lldp', localPort: 'eth1', remotePort: 'eth7' }
      ]
    }
    const hints = computeGhostLinks(neighbors, new Set(['1', '2']), noLinks)
    expect(hints).toHaveLength(1)
    expect(hints[0].binding).toEqual({ protocol: 'lldp', sourcePort: 'eth1', targetPort: 'eth7' })
  })

  it('suppresses hints where a link already exists (either direction)', () => {
    const neighbors = { 1: [lldp(2)] }
    const hasLink = (a: string, b: string) =>
      (a === 'placed-1' && b === 'placed-2') || (a === 'placed-2' && b === 'placed-1')
    expect(computeGhostLinks(neighbors, new Set(['1', '2']), hasLink)).toHaveLength(0)
  })

  it('ignores self-adjacencies and unplaced neighbors', () => {
    const neighbors = { 1: [lldp(1), lldp(99)] }
    expect(computeGhostLinks(neighbors, new Set(['1']), noLinks)).toHaveLength(0)
  })

  it('carries the reporting side ports into the binding orientation', () => {
    const neighbors = { 5: [lldp(9, 'ge-0/0/1', 'xe-1/0/3')] }
    const [hint] = computeGhostLinks(neighbors, new Set(['5', '9']), noLinks)
    expect(hint.sourceId).toBe('placed-5')
    expect(hint.targetId).toBe('placed-9')
    expect(hint.binding.sourcePort).toBe('ge-0/0/1')
    expect(hint.binding.targetPort).toBe('xe-1/0/3')
  })
})

describe('resolveLinkBindings', () => {
  it('returns one binding per protocol toward the target, ports oriented from the source', () => {
    const neighbors: DiscoveredNeighbor[] = [
      { neighborNodeId: 2, neighborLabel: 'b', linkType: 'lldp', localPort: 'eth1', remotePort: 'eth7' },
      { neighborNodeId: 2, neighborLabel: 'b', linkType: 'cdp', localPort: 'Gi0/1', remotePort: 'Gi0/7' },
      { neighborNodeId: 2, neighborLabel: 'b', linkType: 'lldp', localPort: 'dup', remotePort: 'dup' },
      { neighborNodeId: 9, neighborLabel: 'other', linkType: 'lldp' }
    ]
    const out = resolveLinkBindings(neighbors, 2)
    expect(out).toEqual([
      { protocol: 'lldp', sourcePort: 'eth1', targetPort: 'eth7' },
      { protocol: 'cdp', sourcePort: 'Gi0/1', targetPort: 'Gi0/7' }
    ])
  })

  it('returns [] when the pair has no discovered adjacency', () => {
    expect(resolveLinkBindings([lldp(5)], 2)).toEqual([])
  })
})
