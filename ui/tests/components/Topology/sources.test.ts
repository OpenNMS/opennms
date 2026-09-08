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
import {
  buildSources,
  CUSTOM_SOURCE_SLUG,
  sourceForSlug,
  isDiscoveredSlug,
  variantForKey,
  graphSourceFor
} from '@/components/Topology/sources'
import type { GraphContainerMeta } from '@/services/topologyService'

const graph = (namespace: string, label: string) => ({ namespace, label })

/**
 * The five containers a stock 36.x instance reports from GET /api/v2/graphs,
 * labels included, so the derived entries are asserted against real strings
 * rather than invented ones.
 */
const LIVE_CONTAINERS: GraphContainerMeta[] = [
  { id: 'application', label: 'Application Graph', graphs: [graph('application', 'Application Graph')] },
  { id: 'bsm', label: 'Business Service Graph', graphs: [graph('bsm', 'Business Service Graph')] },
  {
    id: 'enlinkd',
    label: 'Enlinkd Graphs',
    graphs: [
      graph('nodes', 'All'),
      graph('nodes:Bridge', 'Bridge'),
      graph('nodes:Cdp', 'Cdp'),
      graph('nodes:Isis', 'Isis'),
      graph('nodes:Layer2', 'Layer2'),
      graph('nodes:Layer3', 'Layer3'),
      graph('nodes:Lldp', 'Lldp'),
      graph('nodes:NetworkRouter', 'NetworkRouter'),
      graph('nodes:Ospf', 'Ospf'),
      graph('nodes:OspfArea', 'OspfArea'),
      graph('nodes:UserDefined', 'UserDefined')
    ]
  },
  { id: 'pathoutage', label: 'Path Outage', graphs: [graph('pathoutage', 'Path Outage')] },
  { id: 'vmware', label: 'VMware Topology Provider', graphs: [graph('vmware', 'VMware Topology Provider')] }
]

// The offline fallback, which is also the curated table as declared.
const curated = buildSources([])

describe('buildSources', () => {
  it('falls back to the curated groups when nothing is known yet', () => {
    expect(curated.map(s => s.slug)).toEqual([
      CUSTOM_SOURCE_SLUG, 'layer2', 'layer3', 'user-defined', 'all-protocols', 'pathoutage'
    ])
  })

  it('keeps curated groups first and derives the rest, sorted by label', () => {
    expect(buildSources(LIVE_CONTAINERS).map(s => s.slug)).toEqual([
      CUSTOM_SOURCE_SLUG,
      'layer2',
      'layer3',
      'user-defined',
      'all-protocols',
      'pathoutage',
      // Derived from the API, alphabetical by displayed label. No `enlinkd`
      // entry: the curated groups now claim all eleven of its namespaces.
      'application',
      'vmware'
    ])
  })

  // Business services get their own home rather than sitting among network
  // topologies, so the container is dropped even though the API serves it.
  it('excludes the bsm container', () => {
    const sources = buildSources(LIVE_CONTAINERS)
    expect(sources.some(s => s.container === 'bsm')).toBe(false)
    expect(LIVE_CONTAINERS.some(c => c.id === 'bsm')).toBe(true)
  })

  it('assigns each source a menu heading', () => {
    const sources = buildSources(LIVE_CONTAINERS)
    const byGroup = (g: string) => sources.filter(s => s.group === g).map(s => s.slug)
    expect(byGroup('discovered')).toEqual([
      'layer2', 'layer3', 'user-defined', 'all-protocols', 'vmware'
    ])
    expect(byGroup('derived')).toEqual(['pathoutage', 'application'])
    // The custom source sits above the headings, not under one.
    expect(sourceForSlug(sources, CUSTOM_SOURCE_SLUG)?.group).toBeUndefined()
  })

  // Two peers of Layer 2 / Layer 3 rather than one entry with variants, so each
  // is one click and neither needs the variant picker.
  it('offers the remaining enlinkd namespaces as their own entries', () => {
    const sources = buildSources(LIVE_CONTAINERS)
    const userDefined = sourceForSlug(sources, 'user-defined')
    // Named for enlinkd to separate it from links drawn on a custom view.
    expect(userDefined?.label).toBe('User-defined (Enlinkd)')
    expect(userDefined?.variants).toEqual([
      { key: 'default', label: 'User-defined (Enlinkd)', namespace: 'nodes:UserDefined' }
    ])

    const all = sourceForSlug(sources, 'all-protocols')
    expect(all?.label).toBe('All protocols')
    expect(all?.variants).toEqual([
      { key: 'default', label: 'All protocols', namespace: 'nodes' }
    ])
  })

  it('takes a derived label from the server unless overridden', () => {
    const sources = buildSources(LIVE_CONTAINERS)
    expect(sourceForSlug(sources, 'application')?.label).toBe('Application Graph')
    // "Topology Provider" is noise inside a topology menu.
    expect(sourceForSlug(sources, 'vmware')?.label).toBe('VMware')
  })

  // The curated groups claim every enlinkd namespace today, so this covers the
  // case that motivated deriving at all: one added by a future release, which
  // must not go silently missing.
  it('surfaces a namespace no curated group claims, still under Discovered', () => {
    const withNew = LIVE_CONTAINERS.map(c => c.id === 'enlinkd'
      ? { ...c, graphs: [...c.graphs, graph('nodes:Wireless', 'Wireless')] }
      : c)
    const leftovers = sourceForSlug(buildSources(withNew), 'enlinkd')
    expect(leftovers?.group).toBe('discovered')
    expect(leftovers?.variants).toEqual([
      { key: 'wireless', label: 'Wireless', namespace: 'nodes:Wireless' }
    ])
  })

  it('picks up a container it has never heard of', () => {
    const sources = buildSources([
      ...LIVE_CONTAINERS,
      {
        id: 'graphml:acme-sites',
        label: 'Acme Sites',
        graphs: [graph('acme:region', 'Regions'), graph('acme:site', 'Sites')]
      }
    ])
    const graphml = sourceForSlug(sources, 'graphml-acme-sites')
    expect(graphml?.label).toBe('Acme Sites')
    // Nothing classified it, so it lands under Derived rather than claiming to
    // have been discovered from the network.
    expect(graphml?.group).toBe('derived')
    expect(graphml?.container).toBe('graphml:acme-sites')
    expect(graphml?.layout).toBe('force')
    expect(graphml?.variants?.map(v => v.key)).toEqual(['region', 'site'])
  })

  it('lays out applications as a hierarchy and vmware as force', () => {
    const sources = buildSources(LIVE_CONTAINERS)
    expect(sourceForSlug(sources, 'application')?.layout).toBe('hierarchy')
    expect(sourceForSlug(sources, 'vmware')?.layout).toBe('force')
  })

  it('drops a curated group whose container is not installed', () => {
    const pathoutageOnly = LIVE_CONTAINERS.find(c => c.id === 'pathoutage')!
    const slugs = buildSources([pathoutageOnly]).map(s => s.slug)
    expect(slugs).toEqual([CUSTOM_SOURCE_SLUG, 'pathoutage'])
  })

  it('reduces a curated group to the variants that exist', () => {
    const sources = buildSources([
      { id: 'enlinkd', label: 'Enlinkd Graphs', graphs: [graph('nodes:Lldp', 'Lldp')] }
    ])
    expect(sourceForSlug(sources, 'layer2')?.variants?.map(v => v.key)).toEqual(['lldp'])
    // Layer 3 had nothing left, so it is not offered at all.
    expect(sourceForSlug(sources, 'layer3')).toBeUndefined()
  })

  it('keeps slugs unique, since a slug addresses a route', () => {
    const sources = buildSources([
      // A container whose id collides with a curated slug.
      { id: 'layer2', label: 'Impostor', graphs: [graph('impostor', 'Impostor')] },
      ...LIVE_CONTAINERS
    ])
    const slugs = sources.map(s => s.slug)
    expect(new Set(slugs).size).toBe(slugs.length)
    expect(sourceForSlug(sources, 'layer2')?.label).toBe('Layer 2')
    expect(sources.find(s => s.label === 'Impostor')?.slug).toBe('layer2-2')
  })

  it('gives every discovered source a container and variants; custom has neither', () => {
    for (const s of buildSources(LIVE_CONTAINERS)) {
      if (s.kind === 'discovered') {
        expect(s.container).toBeTruthy()
        expect(s.variants?.length).toBeGreaterThan(0)
      } else {
        expect(s.container).toBeUndefined()
        expect(s.variants).toBeUndefined()
      }
    }
  })
})

describe('curated presentation', () => {
  it('path outage is a single-variant hierarchy-laid-out source', () => {
    const pathoutage = sourceForSlug(curated, 'pathoutage')!
    expect(pathoutage.kind).toBe('discovered')
    expect(pathoutage.container).toBe('pathoutage')
    expect(pathoutage.layout).toBe('hierarchy')
    // One variant -> the page renders no variant picker.
    expect(pathoutage.variants).toHaveLength(1)
    expect(pathoutage.variants![0].namespace).toBe('pathoutage')
  })

  it('Layer 2 / Layer 3 cover the expected enlinkd namespaces as variants', () => {
    const ns = (slug: string) => sourceForSlug(curated, slug)!.variants!.map(v => v.namespace)
    expect(ns('layer2')).toEqual(['nodes:Layer2', 'nodes:Lldp', 'nodes:Cdp', 'nodes:Bridge'])
    expect(ns('layer3')).toEqual([
      'nodes:Layer3',
      'nodes:Ospf',
      'nodes:OspfArea',
      'nodes:Isis',
      'nodes:NetworkRouter'
    ])
  })

  it('classifies slugs as discovered or not', () => {
    expect(isDiscoveredSlug(curated, 'layer2')).toBe(true)
    expect(isDiscoveredSlug(curated, CUSTOM_SOURCE_SLUG)).toBe(false)
    expect(isDiscoveredSlug(curated, 'nonexistent')).toBe(false)
    expect(isDiscoveredSlug(curated, undefined)).toBe(false)
  })
})

describe('variantForKey', () => {
  const layer2 = sourceForSlug(curated, 'layer2')

  it('resolves a known variant', () => {
    expect(variantForKey(layer2, 'lldp')?.namespace).toBe('nodes:Lldp')
  })

  it('falls back to the default (variants[0]) for a missing/unknown key', () => {
    expect(variantForKey(layer2, undefined)?.namespace).toBe('nodes:Layer2')
    expect(variantForKey(layer2, 'bogus')?.namespace).toBe('nodes:Layer2')
  })

  it('returns undefined for a non-discovered source', () => {
    expect(variantForKey(sourceForSlug(curated, CUSTOM_SOURCE_SLUG), 'x')).toBeUndefined()
  })
})

describe('graphSourceFor', () => {
  it('builds the Graph API source for a (group, variant)', () => {
    expect(graphSourceFor(sourceForSlug(curated, 'layer3'), 'ospf-area')).toEqual({
      container: 'enlinkd',
      namespace: 'nodes:OspfArea'
    })
  })

  it('uses the default variant when the key is absent', () => {
    expect(graphSourceFor(sourceForSlug(curated, 'layer3'), undefined)).toEqual({
      container: 'enlinkd',
      namespace: 'nodes:Layer3'
    })
  })

  it('returns undefined for the custom source', () => {
    expect(graphSourceFor(sourceForSlug(curated, CUSTOM_SOURCE_SLUG), undefined)).toBeUndefined()
  })

  it('carries the source layout preference through (hierarchy for path outage)', () => {
    expect(graphSourceFor(sourceForSlug(curated, 'pathoutage'), undefined)).toEqual({
      container: 'pathoutage',
      namespace: 'pathoutage',
      layout: 'hierarchy'
    })
    expect(graphSourceFor(sourceForSlug(curated, 'layer2'), undefined)?.layout).toBeUndefined()
  })

  it('carries a derived source through too', () => {
    const sources = buildSources(LIVE_CONTAINERS)
    expect(graphSourceFor(sourceForSlug(sources, 'application'), undefined)).toEqual({
      container: 'application',
      namespace: 'application',
      layout: 'hierarchy'
    })
  })
})

// The `claimed` set keys a (container, namespace) pair into one string, so it
// needs a separator that cannot appear in either. Without one, a container whose
// id ends where a curated namespace begins keys to the same string as the curated
// entry and its graphs are silently dropped from the menu.
describe('buildSources claim-key separator', () => {
  it('does not drop a container whose id+namespace collides with a curated pair', () => {
    const sources = buildSources([
      { id: 'enlinkd', label: 'Enlinkd', graphs: [{ namespace: 'nodes:Layer2', label: 'Layer2' }] },
      // 'enlinkdnodes:' + 'Layer2' concatenates to 'enlinkdnodes:Layer2', the same
      // as 'enlinkd' + 'nodes:Layer2'.
      { id: 'enlinkdnodes:', label: 'Odd Container', graphs: [{ namespace: 'Layer2', label: 'L2' }] }
    ] as never)

    const odd = sources.find(s => s.container === 'enlinkdnodes:')
    expect(odd, 'the colliding container was dropped from the menu').toBeTruthy()
    expect(odd!.variants?.map(v => v.namespace)).toEqual(['Layer2'])
  })
})
