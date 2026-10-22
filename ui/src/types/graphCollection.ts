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

// Graph Collections (formerly KSC reports): the shapes served by /api/v2/graph-collections.

/** `prefab` is the only kind stored today; `adhoc` is reserved and rejected by the server. */
export type GraphCollectionGraphKind = 'prefab' | 'adhoc'

/** One stored graph entry, as it is read and written. */
export interface GraphCollectionGraph {
  kind?: GraphCollectionGraphKind
  title: string
  resourceId: string | null
  graphtype: string
  timespan: string
  extlink?: string | null
  // the pre-resourceId way of addressing a resource, found in older files; carried through unchanged
  nodeId?: string | null
  nodeSource?: string | null
  domain?: string | null
  interfaceId?: string | null
}

/** A stored collection. `id` is server assigned and absent on a new one. */
export interface GraphCollection {
  id?: number
  title: string
  showTimespanButton: boolean
  showGraphtypeButton: boolean
  /** 0 means the system default. */
  graphsPerLine: number
  graphs: GraphCollectionGraph[]
}

/** A named timespan a graph can use, e.g. `7_day` labelled `7 day`. */
export interface GraphCollectionTimespan {
  id: string
  label: string
}

export interface GraphCollectionResource {
  id: string
  label: string
  typeName: string | null
  typeLabel: string | null
  /** A legacy page link relative to the webapp root, or null. */
  link: string | null
  parent?: GraphCollectionResource | null
}

/** One graph of the resolved view, ready to render. */
export interface ResolvedGraphCollectionGraph {
  /** Position in the stored graph list, for editing links. */
  index: number
  kind: GraphCollectionGraphKind
  title: string
  /** Effective values, after any override. */
  timespan: string
  graphtype: string
  resourceId: string | null
  resource?: GraphCollectionResource | null
  prefabGraphTitle?: string | null
  /** False when the resource is gone, the graph is not available for it, or the entry would fail validation. */
  valid: boolean
  error?: string | null
  /** Epoch milliseconds. */
  start: number
  end: number
  extlink?: string | null
}

/**
 * A collection prepared for rendering. `type` is `custom` for a stored collection and `node` for one
 * built on the fly for a node, node source or domain.
 */
export interface ResolvedGraphCollection {
  id?: number | null
  type: 'custom' | 'node'
  title: string
  showTimespanButton: boolean
  showGraphtypeButton: boolean
  /** Always at least 1: the server applies the system default. */
  graphsPerLine: number
  /** The override in effect, or null. */
  timespan?: string | null
  graphtype?: string | null
  /** Every prefabricated graph available to the resolved resources, for the graph type override. */
  graphTypes: string[]
  graphs: ResolvedGraphCollectionGraph[]
}

/** A collection together with the ETag it was read with, for If-Match on a later write. */
export interface GraphCollectionWithEtag<T = GraphCollection> {
  collection: T
  etag: string | null
}

/** Which synthetic collection the resolved node views build. */
export type SyntheticGraphCollectionKind = 'node' | 'nodeSource' | 'domain'

export interface ResolveOptions {
  timespan?: string | null
  graphtype?: string | null
}
