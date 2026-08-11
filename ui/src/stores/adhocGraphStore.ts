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

import { defineStore } from 'pinia'
import { ref } from 'vue'
import API from '@/services'
import { GraphMetricsPayload, GraphMetricsResponse, QueryParameters, Resource, SORT } from '@/types'
import { AdhocDatasourceOption, AdhocNodeOption, AdhocResourceOption } from '@/types/adhocGraph'

/**
 * The node a resource belongs to, as `m_nodeDao.get()` accepts it — a bare node id
 * from `node[1].interfaceSnmp[eth0]`, or a foreign-source pair from
 * `nodeSource[Demo:1].interfaceSnmp[eth0]`.
 */
export const nodeCriteriaOf = (resourceId: string): string | null => {
  const match = /^(?:node|nodeSource)\[([^\]]+)\]/.exec(resourceId)
  return match?.[1] ?? null
}

/** Page size for the node picker's server-side search. */
const NODE_SEARCH_LIMIT = 100

/**
 * How many resource/datasource lookups run at once. Each selected resource costs
 * one GET, so a "select all" on a switch with 400 interfaces would otherwise open
 * 400 sockets at once and get throttled or dropped.
 */
const FETCH_CONCURRENCY = 6

/** Run `worker` over `items`, at most `limit` in flight, preserving input order. */
const mapWithConcurrency = async <T, R>(
  items: T[],
  limit: number,
  worker: (item: T) => Promise<R>
): Promise<R[]> => {
  const results = new Array<R>(items.length)
  let cursor = 0

  const runner = async () => {
    while (cursor < items.length) {
      const index = cursor++
      results[index] = await worker(items[index])
    }
  }

  await Promise.all(
    Array.from({ length: Math.min(limit, items.length) }, () => runner())
  )

  return results
}

export const useAdhocGraphStore = defineStore('adhocGraphStore', () => {
  const nodeOptions = ref<AdhocNodeOption[]>([])
  const selectedNodes = ref<AdhocNodeOption[]>([])
  const nodesLoading = ref(false)

  const resourceOptions = ref<AdhocResourceOption[]>([])
  const selectedResources = ref<AdhocResourceOption[]>([])
  const resourcesLoading = ref(false)

  const datasourceOptions = ref<AdhocDatasourceOption[]>([])
  const selectedDatasources = ref<AdhocDatasourceOption[]>([])
  const datasourcesLoading = ref(false)

  const measurements = ref<GraphMetricsResponse | null>(null)
  const queryLoading = ref(false)
  const queryError = ref('')

  // Monotonic request ids, following the pattern in nodeStore: a response is only
  // applied when no newer request of the same kind has started since it was issued.
  // Every list here is driven by free-text typing or multi-select clicks, so
  // out-of-order responses are the normal case, not an edge case.
  let nodeRequestId = 0
  let resourceRequestId = 0
  let datasourceRequestId = 0
  let queryRequestId = 0

  /**
   * Search nodes server-side. The picker never pulls the whole node table — an
   * install with six figures of nodes has to stay usable — so the list is always
   * the top `NODE_SEARCH_LIMIT` matches for the current term.
   */
  const searchNodes = async (term: string) => {
    const requestId = ++nodeRequestId
    nodesLoading.value = true

    const queryParameters: QueryParameters = {
      limit: NODE_SEARCH_LIMIT,
      offset: 0,
      orderBy: 'label',
      order: SORT.ASCENDING
    }

    const trimmed = term.trim()

    if (trimmed) {
      queryParameters._s = `label==*${trimmed}*`
    }

    const resp = await API.getNodes(queryParameters)

    if (requestId !== nodeRequestId) {
      return
    }

    const found = resp ? resp.node.map(node => ({ id: String(node.id), label: node.label })) : []
    const selectedIds = new Set(selectedNodes.value.map(node => node.id))

    // Selected nodes are pinned to the top and never dropped by a later search.
    // Without this, choosing a node and then searching for a different one hides
    // the first — and a restored link, whose nodes are rarely in the default page,
    // would show nothing selected at all.
    nodeOptions.value = [
      ...selectedNodes.value,
      ...found.filter(node => !selectedIds.has(node.id))
    ]
    nodesLoading.value = false
  }

  /**
   * Replace the resource list with the children of every selected node.
   *
   * Selections below this level are pruned rather than cleared: dropping one node
   * must not throw away the resources and datasources chosen on the others.
   */
  const loadResources = async () => {
    const requestId = ++resourceRequestId
    const nodes = [...selectedNodes.value]

    if (!nodes.length) {
      resourceOptions.value = []
      resourcesLoading.value = false
      await pruneResourceSelection()
      return
    }

    resourcesLoading.value = true

    const responses = await mapWithConcurrency(
      nodes,
      FETCH_CONCURRENCY,
      node => API.getResourceForNode(node.id).then(resource => ({ node, resource }))
    )

    if (requestId !== resourceRequestId) {
      return
    }

    const options: AdhocResourceOption[] = []

    for (const { node, resource } of responses) {
      const children = (resource as Resource | null)?.children?.resource ?? []

      for (const child of children) {
        options.push({
          id: child.id,
          label: child.label,
          typeLabel: child.typeLabel || 'Other',
          nodeId: node.id,
          nodeLabel: resource?.label || node.label
        })
      }
    }

    resourceOptions.value = options
    resourcesLoading.value = false
    await pruneResourceSelection()
  }

  const pruneResourceSelection = async () => {
    const available = new Set(resourceOptions.value.map(option => option.id))
    const kept = selectedResources.value.filter(resource => available.has(resource.id))

    if (kept.length !== selectedResources.value.length) {
      selectedResources.value = kept
    }

    await loadDatasources()
  }

  /** Replace the datasource list with the graphable attributes of every selected resource. */
  const loadDatasources = async () => {
    const requestId = ++datasourceRequestId
    const resources = [...selectedResources.value]

    if (!resources.length) {
      datasourceOptions.value = []
      datasourcesLoading.value = false
      pruneDatasourceSelection()
      return
    }

    datasourcesLoading.value = true

    const responses = await mapWithConcurrency(
      resources,
      FETCH_CONCURRENCY,
      resource => API.getResourceById(resource.id).then(detail => ({ resource, detail }))
    )

    if (requestId !== datasourceRequestId) {
      return
    }

    const options: AdhocDatasourceOption[] = []

    for (const { resource, detail } of responses) {
      const attributes = Object.keys((detail as Resource | null)?.rrdGraphAttributes ?? {}).sort()

      for (const attribute of attributes) {
        options.push({
          key: `${resource.id}|${attribute}`,
          resourceId: resource.id,
          resourceLabel: resource.label,
          nodeId: resource.nodeId,
          nodeLabel: resource.nodeLabel,
          attribute
        })
      }
    }

    datasourceOptions.value = options
    datasourcesLoading.value = false
    pruneDatasourceSelection()
  }

  const pruneDatasourceSelection = () => {
    const available = new Set(datasourceOptions.value.map(option => option.key))
    const kept = selectedDatasources.value.filter(datasource => available.has(datasource.key))

    if (kept.length !== selectedDatasources.value.length) {
      selectedDatasources.value = kept
    }
  }

  const setSelectedNodes = async (nodes: AdhocNodeOption[]) => {
    selectedNodes.value = nodes
    await loadResources()
  }

  const setSelectedResources = async (resources: AdhocResourceOption[]) => {
    selectedResources.value = resources
    await loadDatasources()
  }

  const setSelectedDatasources = (datasources: AdhocDatasourceOption[]) => {
    selectedDatasources.value = datasources
  }

  /**
   * Adopt datasources directly, without walking the cascade.
   *
   * Used as the fallback when a link names a resource the server no longer has:
   * the stand-in keeps the series in the picker and in the graph (the query is
   * relaxed, so it comes back as NaN) instead of silently dropping it.
   */
  const adoptDatasources = (datasources: AdhocDatasourceOption[]) => {
    const byKey = new Map(datasourceOptions.value.map(option => [option.key, option]))

    for (const datasource of datasources) {
      if (!byKey.has(datasource.key)) {
        byKey.set(datasource.key, datasource)
      }
    }

    datasourceOptions.value = [...byKey.values()]
    selectedDatasources.value = datasources.map(datasource => byKey.get(datasource.key) as AdhocDatasourceOption)
  }

  /**
   * Rebuild the whole selection from the resource/attribute pairs in a shared link.
   *
   * A link carries only resource ids and attribute names, so the node and resource
   * panes have nothing to show unless the cascade is walked backwards: derive the
   * node from each resource id, fetch it for its label and children, then mark the
   * referenced resources and datasources as selected. Anything the server no longer
   * knows about falls back to a stand-in rather than vanishing from the graph.
   */
  const restoreSelection = async (sources: { resourceId: string, attribute: string }[]) => {
    const resourceIds = [...new Set(sources.map(source => source.resourceId))]
    const criteria = [...new Set(
      resourceIds.map(nodeCriteriaOf).filter((value): value is string => Boolean(value))
    )]

    const wantedKeys = new Set(sources.map(source => `${source.resourceId}|${source.attribute}`))

    const standIns = (): AdhocDatasourceOption[] => sources.map(source => ({
      key: `${source.resourceId}|${source.attribute}`,
      resourceId: source.resourceId,
      resourceLabel: source.resourceId,
      nodeId: nodeCriteriaOf(source.resourceId) ?? '',
      nodeLabel: '',
      attribute: source.attribute
    }))

    if (!criteria.length) {
      adoptDatasources(standIns())
      return
    }

    // Claim the cascade so a search or selection made while this is in flight wins.
    const requestId = ++resourceRequestId
    resourcesLoading.value = true

    const responses = await mapWithConcurrency(
      criteria,
      FETCH_CONCURRENCY,
      criterion => API.getResourceForNode(criterion).then(resource => ({ criterion, resource }))
    )

    if (requestId !== resourceRequestId) {
      return
    }

    const nodes: AdhocNodeOption[] = []
    const options: AdhocResourceOption[] = []

    for (const { criterion, resource } of responses) {
      if (!resource) {
        continue
      }

      nodes.push({ id: criterion, label: resource.label })

      for (const child of resource.children?.resource ?? []) {
        options.push({
          id: child.id,
          label: child.label,
          typeLabel: child.typeLabel || 'Other',
          nodeId: criterion,
          nodeLabel: resource.label
        })
      }
    }

    selectedNodes.value = nodes

    // Show the restored nodes at the top of the picker. They are rarely in the
    // default first page of results, so without this the pane would look empty
    // even though the nodes are selected.
    const restoredIds = new Set(nodes.map(node => node.id))
    nodeOptions.value = [
      ...nodes,
      ...nodeOptions.value.filter(node => !restoredIds.has(node.id))
    ]

    resourceOptions.value = options
    selectedResources.value = options.filter(option => resourceIds.includes(option.id))
    resourcesLoading.value = false

    await loadDatasources()

    const available = new Map(datasourceOptions.value.map(option => [option.key, option]))
    const missing = standIns().filter(standIn => !available.has(standIn.key))

    if (missing.length) {
      for (const standIn of missing) {
        available.set(standIn.key, standIn)
      }
      datasourceOptions.value = [...available.values()]
    }

    selectedDatasources.value = [...available.values()].filter(option => wantedKeys.has(option.key))
  }

  const runQuery = async (payload: GraphMetricsPayload) => {
    const requestId = ++queryRequestId
    queryLoading.value = true
    queryError.value = ''

    const resp = await API.getGraphMetrics(payload)

    if (requestId !== queryRequestId) {
      return
    }

    if (resp) {
      measurements.value = resp
    } else {
      measurements.value = null
      queryError.value = 'Could not retrieve measurements for this selection.'
    }

    queryLoading.value = false
  }

  const clearAll = () => {
    // Bump every request id so responses still in flight are discarded rather than
    // repopulating the lists the user just cleared.
    nodeRequestId++
    resourceRequestId++
    datasourceRequestId++
    queryRequestId++

    selectedNodes.value = []
    selectedResources.value = []
    selectedDatasources.value = []
    resourceOptions.value = []
    datasourceOptions.value = []
    measurements.value = null
    queryError.value = ''
    resourcesLoading.value = false
    datasourcesLoading.value = false
    queryLoading.value = false
  }

  return {
    nodeOptions,
    selectedNodes,
    nodesLoading,
    resourceOptions,
    selectedResources,
    resourcesLoading,
    datasourceOptions,
    selectedDatasources,
    datasourcesLoading,
    measurements,
    queryLoading,
    queryError,
    searchNodes,
    loadResources,
    loadDatasources,
    setSelectedNodes,
    setSelectedResources,
    setSelectedDatasources,
    adoptDatasources,
    restoreSelection,
    runQuery,
    clearAll
  }
})
