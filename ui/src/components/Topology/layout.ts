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

import { forceCenter, forceCollide, forceLink, forceManyBody, forceSimulation, stratify, tree } from 'd3'
import type { SimulationLinkDatum, SimulationNodeDatum } from 'd3'
import Graph from 'graphology'
import forceAtlas2 from 'graphology-layout-forceatlas2'
import type { CanvasLink, CanvasNode } from '@/types/topology'

/**
 * Auto-layout for discovered topologies. Unlike custom views, a discovered
 * graph carries no stored positions (the Graph REST API returns x/y = 0), so
 * we compute them with a d3-force simulation: nodes repel, linked nodes are
 * pulled together, and a centering force keeps the result around the origin
 * (sigma's camera then fits it).
 *
 * The simulation is run synchronously for a fixed number of ticks and stopped
 * -- we only want the final positions, not an animated layout. d3-force seeds
 * unset positions on a deterministic phyllotaxis spiral, so for a given graph
 * the layout is stable across runs (good for tests and for not having nodes
 * jump around on reload).
 */

interface SimNode extends SimulationNodeDatum {
  id: string
}
type SimLink = SimulationLinkDatum<SimNode>

interface LayoutOptions {
  /** Target distance between linked nodes. */
  linkDistance?: number
  /** Repulsion strength (more negative = more spread). */
  chargeStrength?: number
  /** Minimum separation so node glyphs don't overlap. */
  collideRadius?: number
  /** Simulation ticks to run before reading positions. */
  ticks?: number
}

const DEFAULTS: Required<LayoutOptions> = {
  linkDistance: 100,
  chargeStrength: -280,
  collideRadius: 30,
  ticks: 400
}

/**
 * Above this size d3-force needs several seconds to untangle a graph (its
 * 400 ticks take ~7s at 2000 nodes, and fewer ticks visibly under-converge),
 * so larger graphs lay out with ForceAtlas2 instead -- the sigma ecosystem's
 * standard for big graphs (typed-array implementation, Barnes-Hut): ~2s at
 * 2000 nodes with better cluster separation. Small and medium graphs keep
 * d3-force, whose collision spacing suits them well.
 */
const FORCEATLAS2_THRESHOLD = 300

const GOLDEN_ANGLE = Math.PI * (3 - Math.sqrt(5))

const layoutWithForceAtlas2 = (
  nodes: CanvasNode[],
  links: CanvasLink[],
  opts: Required<LayoutOptions>
): CanvasNode[] => {
  const g = new Graph()
  nodes.forEach((n, i) => {
    if (g.hasNode(n.id)) {
      return
    }
    // Deterministic phyllotaxis seeding (same spiral d3-force uses), so a
    // given graph lays out the same way on every load. Computed rather than
    // written out: the golden angle's decimal expansion is longer than a
    // double can hold.
    const angle = i * GOLDEN_ANGLE
    const radius = opts.collideRadius * Math.sqrt(i)
    g.addNode(n.id, { x: radius * Math.cos(angle), y: radius * Math.sin(angle) })
  })
  for (const e of links) {
    if (
      g.hasNode(e.sourceId) &&
      g.hasNode(e.targetId) &&
      e.sourceId !== e.targetId &&
      !g.hasEdge(e.sourceId, e.targetId)
    ) {
      g.addEdge(e.sourceId, e.targetId)
    }
  }
  forceAtlas2.assign(g, {
    iterations: 300,
    // inferSettings only enables Barnes-Hut above 2000 nodes; everything on
    // this path is large enough to want it.
    settings: { ...forceAtlas2.inferSettings(g), barnesHutOptimize: true }
  })
  // Rescale so the mean linked-pair distance matches the d3 path's
  // linkDistance: downstream consumers think in those units (the curvature
  // clearance, node pixel size relative to spacing).
  let total = 0
  let count = 0
  g.forEachEdge((_e, _attrs, _s, _t, sa, ta) => {
    total += Math.hypot((sa.x as number) - (ta.x as number), (sa.y as number) - (ta.y as number))
    count++
  })
  const scale = count > 0 && total > 0 ? opts.linkDistance / (total / count) : 1
  return nodes.map(n =>
    g.hasNode(n.id)
      ? {
        ...n,
        x: (g.getNodeAttribute(n.id, 'x') as number) * scale,
        y: (g.getNodeAttribute(n.id, 'y') as number) * scale
      }
      : { ...n, x: 0, y: 0 }
  )
}

/**
 * Return a new node array with computed x/y positions. Input nodes are not
 * mutated; edges are read-only. Nodes referenced only by id in edges that
 * don't exist as nodes are ignored by the link force (we pass the node set as
 * the source of truth).
 */
export const layoutDiscoveredGraph = (
  nodes: CanvasNode[],
  links: CanvasLink[],
  options: LayoutOptions = {}
): CanvasNode[] => {
  const opts = { ...DEFAULTS, ...options }
  if (nodes.length === 0) {
    return []
  }
  if (nodes.length > FORCEATLAS2_THRESHOLD) {
    return layoutWithForceAtlas2(nodes, links, opts)
  }

  const simNodes: SimNode[] = nodes.map(n => ({ id: n.id }))
  const ids = new Set(simNodes.map(n => n.id))
  const simLinks: SimLink[] = links
    .filter(e => ids.has(e.sourceId) && ids.has(e.targetId))
    .map(e => ({ source: e.sourceId, target: e.targetId }))

  const simulation = forceSimulation<SimNode>(simNodes)
    .force(
      'link',
      forceLink<SimNode, SimLink>(simLinks)
        .id(d => d.id)
        .distance(opts.linkDistance)
    )
    .force('charge', forceManyBody<SimNode>().strength(opts.chargeStrength))
    .force('center', forceCenter(0, 0))
    // iterations > 1 enforces non-overlap more strictly (important for dense
    // discovered graphs with many leaf nodes at the larger node size).
    .force('collide', forceCollide<SimNode>(opts.collideRadius).iterations(3))
    .stop()

  simulation.tick(opts.ticks)

  const posById = new Map(simNodes.map(n => [n.id, n]))
  return nodes.map((n) => {
    const p = posById.get(n.id)
    return { ...n, x: p?.x ?? 0, y: p?.y ?? 0 }
  })
}

/**
 * Decide which links should render curved instead of straight. A straight
 * edge that passes under a third node (common in dense graphs -- e.g. a
 * dual-homed fabric where dist-to-far-core lines cross the near core) is both
 * hard to see and impossible to click where it is covered. For each link,
 * any non-endpoint node within `clearance` of the segment marks it as
 * obstructed; the link is then bent away from the side most offenders sit on.
 * This also separates collinear links that share an endpoint, since the third
 * node of the shorter run lies on the longer run's segment.
 *
 * Returns link id -> curvature (the @sigma/edge-curve attribute; sign picks
 * the bend side). Links with no obstruction are absent and stay straight.
 */
/** ~2M node-link tests, about 55ms by the measurement above. */
const CURVATURE_BUDGET = 2_000_000

export const computeEdgeCurvatures = (
  nodes: CanvasNode[],
  links: CanvasLink[],
  clearance: number
): Map<string, number> => {
  const CURVATURE = 0.25
  const out = new Map<string, number>()
  // Every link is tested against every node, so cost is links x nodes: measured
  // 325ms at 3454 nodes / 3501 links. Skipped past this budget, where links are
  // hairline anyway and curving them buys no clarity.
  if (links.length * nodes.length > CURVATURE_BUDGET) {
    return out
  }
  const pos = new Map(nodes.map(n => [n.id, { x: n.x, y: n.y }]))
  for (const link of links) {
    const a = pos.get(link.sourceId)
    const b = pos.get(link.targetId)
    if (!a || !b || link.sourceId === link.targetId) {
      continue
    }
    const dx = b.x - a.x
    const dy = b.y - a.y
    const len2 = dx * dx + dy * dy
    if (len2 === 0) {
      continue
    }
    let obstructions = 0
    let sideSum = 0
    for (const n of nodes) {
      if (n.id === link.sourceId || n.id === link.targetId) {
        continue
      }
      const p = pos.get(n.id)!
      // Closest point on the segment (clamped); only interior hits count --
      // a node merely near an endpoint doesn't obscure the link.
      const t = ((p.x - a.x) * dx + (p.y - a.y) * dy) / len2
      if (t <= 0.05 || t >= 0.95) {
        continue
      }
      const ox = a.x + t * dx - p.x
      const oy = a.y + t * dy - p.y
      if (ox * ox + oy * oy > clearance * clearance) {
        continue
      }
      obstructions++
      const cross = dx * (p.y - a.y) - dy * (p.x - a.x)
      sideSum += cross >= 0 ? 1 : -1
    }
    if (obstructions > 0) {
      out.set(link.id, sideSum > 0 ? -CURVATURE : CURVATURE)
    }
  }
  return out
}

interface HierarchyLayoutOptions {
  /** Vertical distance between tiers (parent row to child row). */
  levelSpacing?: number
  /** Horizontal distance between adjacent siblings. */
  siblingSpacing?: number
}

const HIERARCHY_DEFAULTS: Required<HierarchyLayoutOptions> = {
  levelSpacing: 110,
  siblingSpacing: 70
}

/**
 * Tiered top-down layout for rooted parent-child data (e.g. the path-outage
 * node-parent hierarchy), where the point is to *see the tiers* -- force
 * layout would render the same data as an undifferentiated blob. Links are
 * read as parent (source) -> child (target). The data is usually a forest,
 * so all roots are hung under a synthetic super-root, laid out with d3's
 * tidy-tree algorithm, and the synthetic level is stripped back off.
 *
 * Defensive cases: a node with several incoming links keeps its first parent
 * (the data model -- one nodeParentID -- can't produce this; another provider
 * could). If the links don't form a tree at all (a cycle), d3 stratify throws
 * and we fall back to the force layout rather than render nothing.
 */
export const layoutHierarchyGraph = (
  nodes: CanvasNode[],
  links: CanvasLink[],
  options: HierarchyLayoutOptions = {}
): CanvasNode[] => {
  const opts = { ...HIERARCHY_DEFAULTS, ...options }
  if (nodes.length === 0) {
    return []
  }

  const ids = new Set(nodes.map(n => n.id))
  const parentById = new Map<string, string>()
  for (const link of links) {
    if (!ids.has(link.sourceId) || !ids.has(link.targetId)) {
      continue
    }
    if (link.sourceId === link.targetId) {
      continue
    }
    if (!parentById.has(link.targetId)) {
      parentById.set(link.targetId, link.sourceId)
    }
  }

  const SUPER_ROOT = '__hierarchy_root__'
  type Datum = { id: string; parentId?: string }
  const data: Datum[] = [
    { id: SUPER_ROOT },
    ...nodes.map(n => ({ id: n.id, parentId: parentById.get(n.id) ?? SUPER_ROOT }))
  ]

  let root
  try {
    root = stratify<Datum>()
      .id(d => d.id)
      .parentId(d => d.parentId)(data)
  } catch {
    return layoutDiscoveredGraph(nodes, links)
  }

  // nodeSize (not size) so spacing stays constant regardless of tree breadth;
  // the canvas camera fits whatever extent results.
  const laidOut = tree<Datum>().nodeSize([opts.siblingSpacing, opts.levelSpacing])(root)

  const posById = new Map<string, { x: number; y: number }>()
  laidOut.each((n) => {
    if (n.data.id === SUPER_ROOT) {
      return
    }
    // depth 1 is the real top tier; pull it up to y=0 (the synthetic root's
    // row). Negative per tier because sigma's graph y-axis points up -- deeper
    // tiers must sit at smaller y to render below their parent. `|| 0`
    // normalizes the top tier's -0 to 0.
    posById.set(n.data.id, { x: n.x, y: -(n.depth - 1) * opts.levelSpacing || 0 })
  })

  return nodes.map((n) => {
    const p = posById.get(n.id)
    return { ...n, x: p?.x ?? 0, y: p?.y ?? 0 }
  })
}
