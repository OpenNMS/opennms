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

import type { GraphContainerMeta } from '@/services/topologyService'
import type { DiscoveredGraphSource } from '@/types/topology'

/**
 * The "view source" dimension that sits above the Edit/View mode: what am I
 * looking at? `custom` is the hand-composed catalog; the others are discovered
 * (auto-generated, read-only) topologies served by the Graph REST API.
 *
 * The list is built at runtime from `GET /api/v2/graphs` (see `buildSources`),
 * so a provider bridged in from the legacy topology map, or an operator's
 * GraphML topology, appears without a UI change. A small curated table below
 * overrides presentation for the containers worth grouping by hand; everything
 * else is derived from what the API reports.
 *
 * Discovered sources carry a list of **variants** — different representations
 * of the same data (the combined map vs. a single protocol, OSPF adjacencies
 * vs. OSPF areas). The source is the route param (`/topology/:source`); the
 * chosen variant is a `?variant=<key>` query param, so both stay bookmarkable
 * and the menu stays short. variants[0] is the default.
 */
export interface SourceVariant {
  key: string
  label: string
  namespace: string
}

/**
 * Menu heading. Separate from `kind`, which is behavioral: everything that is
 * not `custom` is a read-only server graph regardless of heading. This is only
 * about how the topology came to exist, which is what an operator scanning the
 * menu is choosing between.
 *
 * `discovered` = found by probing the network (enlinkd link discovery, VMware
 * via vCenter). `derived` = computed from OpenNMS configuration (path outage
 * from nodeParentID, applications from their definitions).
 */
export type SourceGroup = 'discovered' | 'derived'

export interface TopologySourceOption {
  slug: string
  label: string
  kind: 'custom' | 'discovered'
  /** Menu heading; absent for the custom source, which sits above them. */
  group?: SourceGroup
  /** Graph REST API container; present for discovered sources. */
  container?: string
  /** Representations of this source; variants[0] is the default. */
  variants?: SourceVariant[]
  /**
   * Auto-layout suited to the data's shape: 'force' (default) for mesh-like
   * graphs, 'hierarchy' for rooted parent-child trees.
   */
  layout?: 'force' | 'hierarchy'
}

export const CUSTOM_SOURCE_SLUG = 'custom'

const CUSTOM_SOURCE: TopologySourceOption = {
  slug: CUSTOM_SOURCE_SLUG,
  label: 'Custom',
  kind: 'custom'
}

/**
 * Presentation overrides, applied before anything is derived from the API.
 *
 * These exist only where the raw container is worth reshaping for operators:
 * enlinkd reports eleven flat namespaces, which read far better split into
 * Layer 2 and Layer 3 with the combined map first. Slugs and variant keys here
 * are load-bearing, since they appear in bookmarked URLs.
 *
 * A curated group claims the namespaces it lists. Anything left over in the
 * same container still surfaces (see `buildSources`), so a namespace added by a
 * future release cannot go silently missing.
 */
const CURATED_SOURCES: TopologySourceOption[] = [
  {
    slug: 'layer2',
    label: 'Layer 2',
    kind: 'discovered',
    group: 'discovered',
    container: 'enlinkd',
    variants: [
      { key: 'combined', label: 'Combined (LLDP + CDP)', namespace: 'nodes:Layer2' },
      { key: 'lldp', label: 'LLDP', namespace: 'nodes:Lldp' },
      { key: 'cdp', label: 'CDP', namespace: 'nodes:Cdp' },
      { key: 'bridge', label: 'Bridge', namespace: 'nodes:Bridge' }
    ]
  },
  {
    slug: 'layer3',
    label: 'Layer 3',
    kind: 'discovered',
    group: 'discovered',
    container: 'enlinkd',
    variants: [
      { key: 'combined', label: 'Combined (OSPF + IS-IS)', namespace: 'nodes:Layer3' },
      { key: 'ospf', label: 'OSPF — adjacencies', namespace: 'nodes:Ospf' },
      { key: 'ospf-area', label: 'OSPF — by area', namespace: 'nodes:OspfArea' },
      { key: 'isis', label: 'IS-IS', namespace: 'nodes:Isis' },
      { key: 'routers', label: 'Routers & Subnets', namespace: 'nodes:NetworkRouter' }
    ]
  },
  {
    // Links an operator drew by hand. Strictly this is neither discovered nor
    // derived, but `nodes:UserDefined` is an enlinkd namespace sharing the link
    // table with the discovered ones, so it sits with them rather than getting a
    // heading of its own. The label says Enlinkd to separate these from the
    // links drawn on a custom view, which are a different thing entirely.
    slug: 'user-defined',
    label: 'User-defined (Enlinkd)',
    kind: 'discovered',
    group: 'discovered',
    container: 'enlinkd',
    variants: [{ key: 'default', label: 'User-defined (Enlinkd)', namespace: 'nodes:UserDefined' }]
  },
  {
    // Every link enlinkd knows regardless of protocol: the unfiltered peer of
    // the Layer 2 and Layer 3 entries above.
    slug: 'all-protocols',
    label: 'All protocols',
    kind: 'discovered',
    group: 'discovered',
    container: 'enlinkd',
    variants: [{ key: 'default', label: 'All protocols', namespace: 'nodes' }]
  },
  {
    // The node parent / critical-path hierarchy (nodeParentID). A rooted
    // forest, so it lays out as top-down tiers rather than force-directed.
    slug: 'pathoutage',
    label: 'Path Outage',
    kind: 'discovered',
    group: 'derived',
    container: 'pathoutage',
    layout: 'hierarchy',
    variants: [{ key: 'default', label: 'Path Outage', namespace: 'pathoutage' }]
  }
]

/**
 * Presentation for a container that is not curated. Anything absent here takes
 * the defaults: force-directed layout, the server's own label, and the
 * `derived` heading, on the grounds that a topology nobody has classified is
 * more likely computed from configuration than probed from the network.
 */
interface ContainerOverride {
  label?: string
  layout?: 'force' | 'hierarchy'
  group?: SourceGroup
}

const CONTAINER_OVERRIDES: Record<string, ContainerOverride> = {
  // Dependencies roll up, so it reads as a tree.
  application: { layout: 'hierarchy', group: 'derived' },
  // Probed from vCenter, so discovered. The server calls it "VMware Topology
  // Provider", and "Topology Provider" is noise inside a topology menu.
  vmware: { label: 'VMware', group: 'discovered' },
  // Only reached if enlinkd grows a namespace no curated group claims; it is
  // still link discovery when that happens.
  enlinkd: { group: 'discovered' }
}

/**
 * Containers deliberately kept out of this menu. Business services get their
 * own home rather than sitting among network topologies.
 */
const EXCLUDED_CONTAINERS = new Set(['bsm'])

/** URL-safe slug/key from an arbitrary container id or namespace. */
const slugify = (value: string): string =>
  value.toLowerCase().replace(/[^a-z0-9]+/g, '-').replace(/^-+|-+$/g, '') || 'source'

/**
 * A variant key from a namespace. Enlinkd namespaces are `nodes:Lldp`-shaped,
 * so the segment after the last colon is the meaningful part; anything else
 * uses the whole namespace.
 */
const variantKeyFor = (namespace: string): string =>
  slugify(namespace.includes(':') ? namespace.slice(namespace.lastIndexOf(':') + 1) : namespace)

/** Ensure a slug is unique within the menu, since it addresses a route. */
const uniqueSlug = (candidate: string, taken: Set<string>): string => {
  if (!taken.has(candidate)) {
    return candidate
  }
  let n = 2
  while (taken.has(`${candidate}-${n}`)) {
    n++
  }
  return `${candidate}-${n}`
}

/**
 * Build the source menu from the containers the Graph REST API reports.
 *
 * Curated groups come first, in their declared order, reduced to the variants
 * that actually exist. Then one entry per container for namespaces no curated
 * group claimed, so nothing the server offers is unreachable.
 *
 * Passing an empty list (the API failed, or has not been asked yet) yields the
 * curated groups as declared. That keeps the menu working offline rather than
 * emptying it, at the cost of possibly offering a provider that is not
 * installed.
 */
export const buildSources = (containers: GraphContainerMeta[]): TopologySourceOption[] => {
  if (containers.length === 0) {
    return [CUSTOM_SOURCE, ...CURATED_SOURCES]
  }

  const visible = containers.filter(c => !EXCLUDED_CONTAINERS.has(c.id))
  const namespacesByContainer = new Map(
    visible.map(c => [c.id, c.graphs] as const)
  )
  const claimed = new Set<string>()
  const sources: TopologySourceOption[] = [CUSTOM_SOURCE]

  for (const curated of CURATED_SOURCES) {
    const available = namespacesByContainer.get(curated.container ?? '')
    if (!available) {
      continue
    }
    const variants = (curated.variants ?? []).filter(v =>
      available.some(g => g.namespace === v.namespace))
    if (variants.length === 0) {
      continue
    }
    variants.forEach(v => claimed.add(`${curated.container} ${v.namespace}`))
    sources.push({ ...curated, variants })
  }

  const taken = new Set(sources.map(s => s.slug))
  const derived: TopologySourceOption[] = []

  for (const container of visible) {
    const unclaimed = container.graphs.filter(g =>
      !claimed.has(`${container.id} ${g.namespace}`))
    if (unclaimed.length === 0) {
      continue
    }
    const override = CONTAINER_OVERRIDES[container.id] ?? {}
    const slug = uniqueSlug(slugify(container.id), taken)
    taken.add(slug)
    derived.push({
      slug,
      label: override.label || container.label || container.id,
      kind: 'discovered',
      group: override.group ?? 'derived',
      container: container.id,
      layout: override.layout ?? 'force',
      variants: unclaimed.map(g => ({
        key: variantKeyFor(g.namespace),
        label: g.label || g.namespace,
        namespace: g.namespace
      }))
    })
  }

  derived.sort((a, b) => a.label.localeCompare(b.label))
  return [...sources, ...derived]
}

export const sourceForSlug = (
  sources: TopologySourceOption[],
  slug: string | undefined
): TopologySourceOption | undefined => sources.find(s => s.slug === slug)

export const isDiscoveredSlug = (
  sources: TopologySourceOption[],
  slug: string | undefined
): boolean => sourceForSlug(sources, slug)?.kind === 'discovered'

/**
 * Resolve a (source, variant key) pair to the variant to display. Falls back
 * to the source's default variant when the key is missing or unknown, so a
 * bare `/topology/layer2` or a stale `?variant=` still lands somewhere valid.
 */
export const variantForKey = (
  source: TopologySourceOption | undefined,
  key: string | undefined
): SourceVariant | undefined => {
  if (!source?.variants?.length) {
    return undefined
  }
  return source.variants.find(v => v.key === key) ?? source.variants[0]
}

/** The Graph REST API graph source for a discovered (source, variant key). */
export const graphSourceFor = (
  source: TopologySourceOption | undefined,
  key: string | undefined
): DiscoveredGraphSource | undefined => {
  const variant = variantForKey(source, key)
  if (!source?.container || !variant) {
    return undefined
  }
  return { container: source.container, namespace: variant.namespace, layout: source.layout }
}
