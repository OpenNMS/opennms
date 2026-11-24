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

import { GraphCollectionGraph, ResolvedGraphCollection, ResolvedGraphCollectionGraph } from '@/types/graphCollection'

/**
 * A graph entry as the editor holds it: the stored fields plus what the resolved view knows about
 * the entry (resource labels, validity), and a key that survives reordering.
 */
export interface EditableGraph extends GraphCollectionGraph {
  key: number
  resourceLabel?: string
  resourceTypeLabel?: string
  parentLabel?: string
  valid?: boolean
  error?: string | null
}

let nextKey = 1

/** Pair the stored graphs with the resolved view's labels and validity, by position. */
export const toEditableGraphs = (
  graphs: GraphCollectionGraph[],
  resolved?: ResolvedGraphCollectionGraph[] | null
): EditableGraph[] =>
  graphs.map((graph, index) => {
    const info = resolved?.find(r => r.index === index)
    return {
      ...graph,
      key: nextKey++,
      // an older entry addresses its resource the pre-resourceId way; the resolved view knows
      // where that led, so the picker can open on it
      resourceId: graph.resourceId ?? info?.resourceId ?? null,
      resourceLabel: info?.resource?.label,
      resourceTypeLabel: info?.resource?.typeLabel ?? undefined,
      parentLabel: info?.resource?.parent?.label,
      valid: info ? info.valid : undefined,
      error: info?.error ?? null
    }
  })

/** The graphs of an on-the-fly collection as entries a stored collection can keep. */
export const fromResolvedCollection = (resolved: ResolvedGraphCollection): EditableGraph[] =>
  resolved.graphs.map(graph => ({
    key: nextKey++,
    kind: 'prefab',
    title: graph.title,
    resourceId: graph.resourceId ?? graph.resource?.id ?? null,
    graphtype: graph.graphtype,
    timespan: graph.timespan,
    resourceLabel: graph.resource?.label,
    resourceTypeLabel: graph.resource?.typeLabel ?? undefined,
    parentLabel: graph.resource?.parent?.label,
    valid: graph.valid,
    error: graph.error ?? null
  }))

/**
 * Strip the editor's bookkeeping back to what the API stores. The server goes by resourceId when
 * there is one, so an entry that has it sheds the pre-resourceId address it may still carry from
 * an older file instead of storing two locators. An ad-hoc entry keeps only its builder state;
 * the server refuses a resource or graphtype on it, and the state on a prefab one.
 */
export const toStoredGraph = (graph: EditableGraph): GraphCollectionGraph => {
  const kind = graph.kind ?? 'prefab'
  if (kind === 'adhoc') {
    return {
      kind,
      title: graph.title,
      resourceId: null,
      graphtype: null,
      timespan: graph.timespan,
      adhocState: graph.adhocState ?? null,
      extlink: graph.extlink ?? null,
      nodeId: null,
      nodeSource: null,
      domain: null,
      interfaceId: null
    }
  }
  const legacy = graph.resourceId ? null : graph
  return {
    kind,
    title: graph.title,
    resourceId: graph.resourceId,
    graphtype: graph.graphtype,
    timespan: graph.timespan,
    adhocState: null,
    extlink: graph.extlink ?? null,
    nodeId: legacy?.nodeId ?? null,
    nodeSource: legacy?.nodeSource ?? null,
    domain: legacy?.domain ?? null,
    interfaceId: legacy?.interfaceId ?? null
  }
}
