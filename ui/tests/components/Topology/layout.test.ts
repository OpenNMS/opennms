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
import { computeEdgeCurvatures, layoutDiscoveredGraph, layoutHierarchyGraph } from '@/components/Topology/layout'
import type { CanvasLink, CanvasNode } from '@/types/topology'

const node = (id: string): CanvasNode => ({ id, label: id, x: 0, y: 0 })
const edge = (s: string, t: string): CanvasLink => ({ id: `${s}-${t}`, sourceId: s, targetId: t, origin: 'discovered' })

describe('layoutDiscoveredGraph', () => {
  it('returns an empty array for no nodes', () => {
    expect(layoutDiscoveredGraph([], [])).toEqual([])
  })

  it('assigns finite, spread-out positions to every node', () => {
    const nodes = ['a', 'b', 'c', 'd', 'e'].map(node)
    const edges = [edge('a', 'b'), edge('a', 'c'), edge('a', 'd'), edge('a', 'e')]
    const out = layoutDiscoveredGraph(nodes, edges, { ticks: 100 })

    expect(out).toHaveLength(nodes.length)
    for (const n of out) {
      expect(Number.isFinite(n.x)).toBe(true)
      expect(Number.isFinite(n.y)).toBe(true)
    }
    // Not all stacked at the origin -- the layout produced spread.
    const distinct = new Set(out.map(n => `${Math.round(n.x)},${Math.round(n.y)}`))
    expect(distinct.size).toBe(nodes.length)
  })

  it('preserves node identity and other fields, only setting x/y', () => {
    const nodes: CanvasNode[] = [{ id: 'placed-1', nodeId: 1, label: 'core', x: 0, y: 0, icon: 'linkd.system' }]
    const out = layoutDiscoveredGraph(nodes, [])
    expect(out[0].id).toBe('placed-1')
    expect(out[0].nodeId).toBe(1)
    expect(out[0].label).toBe('core')
    expect(out[0].icon).toBe('linkd.system')
  })

  it('does not mutate the input nodes', () => {
    const nodes = [node('a'), node('b')]
    layoutDiscoveredGraph(nodes, [edge('a', 'b')])
    expect(nodes.every(n => n.x === 0 && n.y === 0)).toBe(true)
  })

  it('ignores edges that reference unknown nodes', () => {
    const out = layoutDiscoveredGraph([node('a')], [edge('a', 'ghost')])
    expect(out).toHaveLength(1)
    expect(Number.isFinite(out[0].x)).toBe(true)
  })
})

describe('layoutHierarchyGraph', () => {
  const byId = (out: CanvasNode[]) => new Map(out.map(n => [n.id, n]))

  it('returns an empty array for no nodes', () => {
    expect(layoutHierarchyGraph([], [])).toEqual([])
  })

  it('places children one tier below their parent, siblings on the same row', () => {
    // root -> (a, b); a -> leaf. Sigma's graph y-axis points up, so deeper
    // tiers sit at smaller (negative) y.
    const nodes = ['root', 'a', 'b', 'leaf'].map(node)
    const links = [edge('root', 'a'), edge('root', 'b'), edge('a', 'leaf')]
    const out = byId(layoutHierarchyGraph(nodes, links, { levelSpacing: 100, siblingSpacing: 50 }))

    expect(out.get('root')!.y).toBe(0)
    expect(out.get('a')!.y).toBe(-100)
    expect(out.get('b')!.y).toBe(-100)
    expect(out.get('leaf')!.y).toBe(-200)
    // Siblings are spread horizontally, not stacked.
    expect(out.get('a')!.x).not.toBe(out.get('b')!.x)
  })

  it('lays out a forest with every root on the top row', () => {
    const nodes = ['r1', 'c1', 'r2', 'c2'].map(node)
    const links = [edge('r1', 'c1'), edge('r2', 'c2')]
    const out = byId(layoutHierarchyGraph(nodes, links))

    expect(out.get('r1')!.y).toBe(0)
    expect(out.get('r2')!.y).toBe(0)
    expect(out.get('r1')!.x).not.toBe(out.get('r2')!.x)
  })

  it('keeps the first parent when a node has several incoming links', () => {
    const nodes = ['p1', 'p2', 'child'].map(node)
    const links = [edge('p1', 'child'), edge('p2', 'child')]
    const out = byId(layoutHierarchyGraph(nodes, links, { levelSpacing: 100 }))

    expect(out.get('p1')!.y).toBe(0)
    expect(out.get('p2')!.y).toBe(0)
    expect(out.get('child')!.y).toBe(-100)
  })

  it('falls back to the force layout when the links contain a cycle', () => {
    const nodes = ['a', 'b'].map(node)
    const links = [edge('a', 'b'), edge('b', 'a')]
    const out = layoutHierarchyGraph(nodes, links)

    expect(out).toHaveLength(2)
    for (const n of out) {
      expect(Number.isFinite(n.x)).toBe(true)
      expect(Number.isFinite(n.y)).toBe(true)
    }
  })

  it('preserves node identity and other fields, only setting x/y', () => {
    const nodes: CanvasNode[] = [{ id: 'placed-1', nodeId: 1, label: 'core', x: 0, y: 0, icon: 'linkd.system' }]
    const out = layoutHierarchyGraph(nodes, [])
    expect(out[0]).toMatchObject({ id: 'placed-1', nodeId: 1, label: 'core', icon: 'linkd.system' })
  })

  it('does not mutate the input nodes', () => {
    const nodes = [node('a'), node('b')]
    layoutHierarchyGraph(nodes, [edge('a', 'b')])
    expect(nodes.every(n => n.x === 0 && n.y === 0)).toBe(true)
  })
})

describe('computeEdgeCurvatures', () => {
  const at = (id: string, x: number, y: number): CanvasNode => ({ id, label: id, x, y })

  it('curves a link whose segment passes under a third node', () => {
    // b sits exactly on the straight a-c run (the "link under the near core" case)
    const nodes = [at('a', 0, 0), at('b', 100, 0), at('c', 200, 0)]
    const links = [edge('a', 'c'), edge('a', 'b'), edge('b', 'c')]
    const out = computeEdgeCurvatures(nodes, links, 20)
    expect(out.has('a-c')).toBe(true)
    expect(out.get('a-c')).not.toBe(0)
    // the short runs have no interior obstruction and stay straight
    expect(out.has('a-b')).toBe(false)
    expect(out.has('b-c')).toBe(false)
  })

  it('bends away from the obstructing node', () => {
    // obstructor slightly above the segment -> curve should pick the side
    // away from it (opposite sign to one placed below)
    const above = computeEdgeCurvatures(
      [at('a', 0, 0), at('x', 100, 5), at('c', 200, 0)],
      [edge('a', 'c')],
      20
    ).get('a-c')
    const below = computeEdgeCurvatures(
      [at('a', 0, 0), at('x', 100, -5), at('c', 200, 0)],
      [edge('a', 'c')],
      20
    ).get('a-c')
    expect(above).toBeDefined()
    expect(below).toBeDefined()
    expect(Math.sign(above!)).toBe(-Math.sign(below!))
  })

  it('leaves links alone when nothing is within clearance', () => {
    const nodes = [at('a', 0, 0), at('b', 200, 0), at('far', 100, 80)]
    const out = computeEdgeCurvatures(nodes, [edge('a', 'b')], 20)
    expect(out.size).toBe(0)
  })

  it('ignores nodes hovering near an endpoint rather than the interior', () => {
    const nodes = [at('a', 0, 0), at('b', 200, 0), at('hub', 5, 5)]
    const out = computeEdgeCurvatures(nodes, [edge('a', 'b')], 20)
    expect(out.size).toBe(0)
  })

  it('skips self-loops and links with missing endpoints', () => {
    const nodes = [at('a', 0, 0), at('b', 100, 0)]
    const links = [edge('a', 'a'), edge('a', 'ghost')]
    expect(computeEdgeCurvatures(nodes, links, 20).size).toBe(0)
  })
})

describe('layoutDiscoveredGraph (ForceAtlas2 path, > 300 nodes)', () => {
  it('lays out a large graph with finite, spread, deterministic positions', () => {
    const nodes = Array.from({ length: 350 }, (_, i) => node(`n${i}`))
    // tree-ish wiring: every node hangs off an earlier one
    const edges = Array.from({ length: 349 }, (_, i) => edge(`n${i + 1}`, `n${Math.floor(i / 3)}`))
    const out = layoutDiscoveredGraph(nodes, edges)
    expect(out).toHaveLength(350)
    for (const n of out) {
      expect(Number.isFinite(n.x)).toBe(true)
      expect(Number.isFinite(n.y)).toBe(true)
    }
    const distinct = new Set(out.map(n => `${Math.round(n.x)},${Math.round(n.y)}`))
    expect(distinct.size).toBeGreaterThan(340)
    // deterministic: a second run reproduces the same layout
    const again = layoutDiscoveredGraph(nodes, edges)
    expect(again.map(n => [Math.round(n.x), Math.round(n.y)])).toEqual(
      out.map(n => [Math.round(n.x), Math.round(n.y)])
    )
  })

  it('preserves non-position fields and node order', () => {
    const nodes = Array.from({ length: 301 }, (_, i) =>
      ({ id: `n${i}`, nodeId: i, label: `node ${i}`, x: 0, y: 0 }))
    const out = layoutDiscoveredGraph(nodes, [])
    expect(out[42].id).toBe('n42')
    expect(out[42].nodeId).toBe(42)
    expect(out[42].label).toBe('node 42')
  })
})

// Cost is links x nodes -- 325ms measured at 3454 nodes / 3501 links -- so it is
// skipped past a budget, where links are hairline and curving them buys nothing.
describe('computeEdgeCurvatures budget', () => {
  const graph = (n: number, e: number) => ({
    nodes: Array.from({ length: n }, (_, i) => ({
      id: 'n' + i, label: 'n' + i, x: Math.cos(i) * 500, y: Math.sin(i * 1.7) * 500
    })),
    links: Array.from({ length: e }, (_, i) => ({
      id: 'e' + i, sourceId: 'n' + (i % n), targetId: 'n' + ((i * 7 + 1) % n),
      origin: 'discovered' as const
    }))
  })

  it('still curves an ordinary graph', () => {
    // A dense small graph: some link must pass close enough to a third node.
    const g = graph(40, 120)
    expect(computeEdgeCurvatures(g.nodes, g.links, 200).size).toBeGreaterThan(0)
  })

  it('skips a graph past the budget rather than spending seconds on it', () => {
    const g = graph(3454, 3501)
    const t0 = Date.now()
    const out = computeEdgeCurvatures(g.nodes, g.links, 200)
    expect(out.size).toBe(0)
    expect(Date.now() - t0).toBeLessThan(50)
  })
})
