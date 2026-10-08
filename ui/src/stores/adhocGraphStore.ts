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
import { computed, ref } from 'vue'
import API from '@/services'
import { GraphMetricsPayload, GraphMetricsResponse, Node, QueryParameters, Resource, SORT } from '@/types'
import {
  AdhocDatasourceOption,
  AdhocNodeOption,
  AdhocResourceOption,
  AdhocSelectionState
} from '@/types/adhocGraph'
import {
  datasourceKey,
  nodeCriteriaOf,
  resourceShortId,
  splitDatasourceKey
} from '@/components/AdhocGraphs/utils/adhocIds'

export { nodeCriteriaOf, resourceShortId }

/**
 * Characters that are FIQL syntax rather than data: the boolean separators, the
 * comparison operators, grouping parentheses and the wildcard.
 */
const FIQL_SYNTAX = /[,;()=!<>~*]/g

/**
 * Make a free-text search box safe to interpolate into a FIQL filter.
 *
 * `label==*<term>*` is string concatenation, so a comma or semicolon typed in the
 * box used to terminate the comparison and produce a malformed filter — the
 * request failed and the picker simply went empty with nothing to explain it.
 *
 * The offending characters are dropped rather than escaped: they are FIQL
 * grammar, not values, and node labels are host names that do not contain them.
 */
export const toFiqlSearchTerm = (term: string): string =>
  term.replace(FIQL_SYNTAX, ' ').trim().replace(/\s+/g, ' ')

/**
 * What the node box holds: a filter rule, something that is probably one, or a
 * label fragment. There is no toggle; the one box takes both, and this decides.
 *
 * `rule`: the rule grammar's operators (`&`, `|`, `=`, parentheses, comparisons,
 * a leading `!`) or an infix `LIKE`/`IPLIKE`. A quote on its own is not enough:
 * `O'Brien` is a host name.
 * `guess`: the whole text is one keyword of the grammar, `catincRouters` or
 * `isSNMP`. A host name can look like that (`isLab`), so a guess that the engine
 * rejects is retried as a label search rather than reported as a parse error.
 * `search`: anything else. A fragment could be rewritten as `nodeLabel LIKE
 * '%x%'`, but SQL LIKE is case-sensitive and the label search is not.
 */
export const classifyNodeText = (text: string): 'rule' | 'guess' | 'search' => {
  const trimmed = text.trim()

  if (!trimmed) {
    return 'search'
  }

  if (/[&|=()<>]/.test(trimmed) || /^!/.test(trimmed) || /\S\s+(IPLIKE|LIKE)\s+\S/i.test(trimmed)) {
    return 'rule'
  }

  return /^(catinc\w+|notis\w+|is[A-Z]\w+)$/.test(trimmed) ? 'guess' : 'search'
}

export const looksLikeFilterRule = (text: string): boolean => classifyNodeText(text) !== 'search'

/**
 * The rule actually handed to the filter engine, or '' when the box does not hold
 * one. A pasted Grafana `nodeFilter(catincRouters, labelFormat=id:label)` is
 * unwrapped, and its `labelFormat=`/`valueFormat=` arguments dropped: they are
 * dropdown display hints with no meaning here.
 */
export const composeFilterRule = (text: string): string => {
  let rule = text.trim()
  const call = /^nodeFilter\s*\((.*)\)\s*$/s.exec(rule)

  if (call) {
    rule = call[1]
      .split(',')
      .filter(part => !/^\s*(labelFormat|valueFormat)\s*=/.test(part))
      .join(',')
      .trim()

    // `nodeFilter()` is "every node". The rule has to be one the engine can
    // evaluate against the node table alone: anything mentioning an address joins
    // ipinterface and silently drops nodes that have none.
    return rule || 'nodeid > 0'
  }

  return looksLikeFilterRule(rule) ? rule : ''
}

/**
 * A matcher for the resource and datasource boxes, compiled once per pattern.
 *
 * They share one small dialect, the one the Grafana perf datasource already uses
 * inside resource ids: `*` matches anything and `?` one character, anchored to the
 * whole field, so `interfaceSnmp[eth*]` means what it says and `ifHC*Octets` picks
 * both directions. Square brackets are literal, because resource ids are full of
 * them. A pattern with no wildcard is an ordinary case-insensitive substring match,
 * so a bare `eth0` still finds things.
 */
export const matcherFor = (pattern: string): ((fields: string[]) => boolean) => {
  const trimmed = pattern.trim()

  if (!trimmed) {
    return () => true
  }

  if (/[*?]/.test(trimmed)) {
    const expression = new RegExp(
      `^${trimmed.replace(/[.+^${}()|[\]\\]/g, '\\$&').replace(/\*/g, '.*').replace(/\?/g, '.')}$`,
      'i'
    )
    return fields => fields.some(field => expression.test(field))
  }

  const needle = trimmed.toLowerCase()
  return fields => fields.some(field => field.toLowerCase().includes(needle))
}

/** `matcherFor` applied once; for callers with a single thing to test. */
export const textMatches = (pattern: string, fields: string[]): boolean => matcherFor(pattern)(fields)

/** Page size for a label search; a rule is never paged. */
export const NODE_SEARCH_LIMIT = 100

/**
 * The most nodes a graph may be built from. A rule can match an entire estate,
 * and every effective node costs a resource request; past this the picker stops
 * and says so rather than opening thousands of connections for a graph that the
 * series cap would refuse anyway.
 */
export const MAX_GRAPH_NODES = 200

/**
 * How many resource lookups run at once. Each effective node costs one GET, so a
 * rule matching 200 nodes would otherwise open 200 sockets at once and get
 * throttled or dropped.
 */
const FETCH_CONCURRENCY = 6

const INVALID_RULE_MESSAGE = 'That filter rule could not be parsed.'
const FAILED_RULE_MESSAGE = 'The filter rule could not be evaluated.'
const FAILED_SEARCH_MESSAGE = 'The node search failed.'

/**
 * Run `worker` over `items`, at most `limit` in flight, preserving input order.
 * Each item is isolated: a worker that rejects yields `null` for that item and the
 * rest still complete, so one unreachable node costs that node, not the page.
 */
const mapWithConcurrency = async <T, R>(
  items: T[],
  limit: number,
  worker: (item: T) => Promise<R>
): Promise<(R | null)[]> => {
  const results = new Array<R | null>(items.length).fill(null)
  let cursor = 0

  const runner = async () => {
    while (cursor < items.length) {
      const index = cursor++

      try {
        results[index] = await worker(items[index])
      } catch (_error) {
        results[index] = null
      }
    }
  }

  await Promise.all(
    Array.from({ length: Math.min(limit, items.length) }, () => runner())
  )

  return results
}

/**
 * The picker's view of a node. The foreign-source pair and location ride along
 * only when the server reported them, so a node that has neither still compares
 * equal to the bare `{ id, label }` shape.
 */
const toNodeOption = (node: Node): AdhocNodeOption => {
  const option: AdhocNodeOption = { id: String(node.id), label: node.label }

  if (node.foreignSource && node.foreignId) {
    option.foreignSource = node.foreignSource
    option.foreignId = node.foreignId
  }

  if (node.location) {
    option.location = node.location
  }

  return option
}

/**
 * Every id the server might know a node by. A node found by search carries its
 * database id and, when requisitioned, its foreign-source pair; a node restored
 * from a link carries whichever form its resource ids used. The same node can
 * therefore arrive under two names, and anything that compares nodes has to
 * compare all of them.
 */
const nodeIdForms = (node: AdhocNodeOption): string[] =>
  (node.foreignSource && node.foreignId ? [node.id, `${node.foreignSource}:${node.foreignId}`] : [node.id])

/**
 * Every graphable resource under a node resource, however deep. A resource is
 * graphable when it carries attributes; the intermediate ones that merely group
 * children are skipped, as the Grafana plugin skips them.
 */
const graphableDescendants = (node: AdhocNodeOption, nodeLabel: string, resource: Resource): AdhocResourceOption[] => {
  const found: AdhocResourceOption[] = []

  for (const child of resource.children?.resource ?? []) {
    const attributes = Object.keys(child.rrdGraphAttributes ?? {}).sort()

    if (attributes.length) {
      found.push({
        id: child.id,
        label: child.label,
        name: child.name ?? '',
        typeLabel: child.typeLabel || 'Other',
        nodeId: node.id,
        nodeLabel,
        attributes
      })
    }

    found.push(...graphableDescendants(node, nodeLabel, child))
  }

  return found
}

/** A resource known only by id, from a link; filled in once its node loads. */
const resourceStandIn = (id: string): AdhocResourceOption => ({
  id,
  label: resourceShortId(id),
  name: '',
  typeLabel: '',
  nodeId: nodeCriteriaOf(id) ?? '',
  nodeLabel: '',
  attributes: []
})

const datasourceOf = (resource: AdhocResourceOption, attribute: string): AdhocDatasourceOption => ({
  key: datasourceKey(resource.id, attribute),
  resourceId: resource.id,
  resourceLabel: resource.label,
  nodeId: resource.nodeId,
  nodeLabel: resource.nodeLabel,
  attribute
})

/** A datasource known only by key, from a link; filled in once its resource loads. */
const datasourceStandIn = (key: string): AdhocDatasourceOption => {
  const { resourceId, attribute } = splitDatasourceKey(key)
  return datasourceOf(resourceStandIn(resourceId), attribute)
}

/** Items in `matches` not already present among `picked`, by any of their keys. */
const notPicked = <T>(picked: T[], matches: T[], keysOf: (item: T) => string[]): T[] => {
  const pickedKeys = new Set(picked.flatMap(keysOf))
  return matches.filter(item => !keysOf(item).some(key => pickedKeys.has(key)))
}

export const useAdhocGraphStore = defineStore('adhocGraphStore', () => {
  // ---- Nodes ---------------------------------------------------------------
  /** The node box: a filter rule, or a label fragment (see looksLikeFilterRule). */
  const nodeFilter = ref('')
  /** What the node box currently matches on the server. */
  const nodeMatches = ref<AdhocNodeOption[]>([])
  const nodesLoading = ref(false)
  /** Why the last rule produced nothing, or '' when it worked. */
  const nodeFilterError = ref('')
  /** How many more nodes a label search matched than it listed. */
  const nodeMatchOverflow = ref(0)
  /** How many nodes the rule matched when that was more than a graph may use; 0 otherwise. */
  const ruleOverflow = ref(0)
  const pickedNodes = ref<AdhocNodeOption[]>([])

  /**
   * The nodes the graph is built from: the picks when there are any, otherwise
   * everything the filter matches. An empty box matches nothing on purpose; the
   * list it shows is for browsing and picking, not an invitation to graph the
   * first hundred nodes in inventory.
   */
  const effectiveNodes = computed<AdhocNodeOption[]>(() => {
    if (pickedNodes.value.length) {
      return pickedNodes.value
    }

    return nodeFilter.value.trim() ? nodeMatches.value : []
  })

  /** Every id form of every effective node, for membership tests. */
  const effectiveNodeForms = computed<Set<string>>(() => new Set(effectiveNodes.value.flatMap(nodeIdForms)))

  /**
   * More nodes than a graph may be built from; 0 when within the limit. A rule
   * that overflowed was never fetched, so the count comes from the server; picks,
   * which can only be made from a list that fitted, override that.
   */
  const nodeLimitExceeded = computed<number>(() => {
    if (pickedNodes.value.length) {
      return pickedNodes.value.length > MAX_GRAPH_NODES ? pickedNodes.value.length : 0
    }

    if (ruleOverflow.value) {
      return ruleOverflow.value
    }

    return effectiveNodes.value.length > MAX_GRAPH_NODES ? effectiveNodes.value.length : 0
  })

  /**
   * What the node column lists: picks pinned on top, then the other matches. A
   * match that is the same node as a pick under another id form is not listed
   * again, or a legacy link's `Demo:r1` and a search's `7` would both show.
   */
  const nodeOptions = computed<AdhocNodeOption[]>(() =>
    [...pickedNodes.value, ...notPicked(pickedNodes.value, nodeMatches.value, nodeIdForms)])

  // ---- Resources -----------------------------------------------------------
  const resourceFilter = ref('')
  /** Every graphable resource under every effective node. */
  const resourceCandidates = ref<AdhocResourceOption[]>([])
  const resourcesLoading = ref(false)
  const pickedResources = ref<AdhocResourceOption[]>([])

  /**
   * fornode responses by node id, so re-running a filter does not refetch. A
   * failure is cached as null: retrying it on every keystroke would not make the
   * node answer, and the column says how many are missing instead.
   */
  const nodeResourceCache = new Map<string, { label: string, resources: AdhocResourceOption[] } | null>()
  /** Effective nodes whose resources could not be loaded. */
  const failedNodeLoads = ref(0)

  const resourceCandidateById = computed<Map<string, AdhocResourceOption>>(() =>
    new Map(resourceCandidates.value.map(resource => [resource.id, resource])))

  const resourceMatches = computed<AdhocResourceOption[]>(() => {
    const pattern = resourceFilter.value

    if (!pattern.trim()) {
      return resourceCandidates.value
    }

    const matches = matcherFor(pattern)

    return resourceCandidates.value.filter(resource =>
      matches([resourceShortId(resource.id), resource.label, resource.name, resource.typeLabel]))
  })

  /** A pick is shown with live data when there is any. */
  const freshResource = (pick: AdhocResourceOption): AdhocResourceOption =>
    resourceCandidateById.value.get(pick.id) ?? pick

  /**
   * Whether a picked resource belongs to a node that is in the graph right now.
   * Known live, by its candidate; or by its node, under any id form the node goes
   * by, which is what keeps a `nodeSource[fs:fid]` pick attached to the node that
   * search listed by database id.
   */
  const resourceIsReachable = (pick: AdhocResourceOption): boolean =>
    // A resource that belongs to no node (an old link to one) has no node to be
    // unreachable through; it is graphed straight from its id, as it always was.
    nodeCriteriaOf(pick.id) === null ||
    resourceCandidateById.value.has(pick.id) ||
    effectiveNodeForms.value.has(pick.nodeId) ||
    effectiveNodeForms.value.has(nodeCriteriaOf(pick.id) ?? '')

  /**
   * The picks that currently count: those under an effective node. The rest are
   * kept, not dropped. A pick under a node the filter stopped matching comes back
   * when the filter matches the node again; pruning here would make a mistyped
   * rule, or a 400 from the engine mid-typing, destroy a carefully built selection.
   */
  const reachablePickedResources = computed<AdhocResourceOption[]>(() =>
    pickedResources.value.filter(resourceIsReachable).map(freshResource))

  const effectiveResources = computed<AdhocResourceOption[]>(() => {
    if (pickedResources.value.length) {
      return reachablePickedResources.value
    }

    return resourceFilter.value.trim() ? resourceMatches.value : []
  })

  const resourceOptions = computed<AdhocResourceOption[]>(() => [
    ...reachablePickedResources.value,
    ...notPicked(pickedResources.value, resourceMatches.value, resource => [resource.id])
  ])

  // ---- Datasources ---------------------------------------------------------
  const datasourceFilter = ref('')
  const pickedDatasources = ref<AdhocDatasourceOption[]>([])

  /** Every attribute of every effective resource; no request needed. */
  const datasourceCandidates = computed<AdhocDatasourceOption[]>(() =>
    effectiveResources.value.flatMap(resource => resource.attributes.map(attribute => datasourceOf(resource, attribute))))

  const datasourceCandidateByKey = computed<Map<string, AdhocDatasourceOption>>(() =>
    new Map(datasourceCandidates.value.map(datasource => [datasource.key, datasource])))

  const effectiveResourceIds = computed<Set<string>>(() => new Set(effectiveResources.value.map(resource => resource.id)))

  const datasourceMatches = computed<AdhocDatasourceOption[]>(() => {
    const pattern = datasourceFilter.value

    if (!pattern.trim()) {
      return datasourceCandidates.value
    }

    const matches = matcherFor(pattern)

    return datasourceCandidates.value.filter(datasource => matches([datasource.attribute]))
  })

  const freshDatasource = (pick: AdhocDatasourceOption): AdhocDatasourceOption =>
    datasourceCandidateByKey.value.get(pick.key) ?? pick

  /** Datasource picks follow their resources the same way resource picks follow nodes. */
  const reachablePickedDatasources = computed<AdhocDatasourceOption[]>(() =>
    pickedDatasources.value
      .filter(pick => datasourceCandidateByKey.value.has(pick.key) || effectiveResourceIds.value.has(pick.resourceId))
      .map(freshDatasource))

  /** The series the graph plots, before styling. */
  const effectiveDatasources = computed<AdhocDatasourceOption[]>(() => {
    if (pickedDatasources.value.length) {
      return reachablePickedDatasources.value
    }

    return datasourceFilter.value.trim() ? datasourceMatches.value : []
  })

  const datasourceOptions = computed<AdhocDatasourceOption[]>(() => [
    ...reachablePickedDatasources.value,
    ...notPicked(pickedDatasources.value, datasourceMatches.value, datasource => [datasource.key])
  ])

  // ---- Measurements --------------------------------------------------------
  const measurements = ref<GraphMetricsResponse | null>(null)
  const queryLoading = ref(false)
  const queryError = ref('')

  // Monotonic request ids: a response is only applied when no newer request of the
  // same kind has started since it was issued. Every list here is driven by
  // typing, so out-of-order responses are the normal case, not an edge case.
  let nodeRequestId = 0
  let resourceRequestId = 0
  let queryRequestId = 0

  /**
   * Load the resources of every effective node, from cache where possible, one
   * fornode request per node otherwise. fornode already carries each child's
   * attributes, so this is the only request the resource and datasource columns
   * ever need. Over the node limit nothing is fetched: the graph cannot be built
   * from that many nodes, and the column says so.
   */
  const loadResources = async () => {
    const requestId = ++resourceRequestId
    const nodes = nodeLimitExceeded.value ? [] : [...effectiveNodes.value]
    const missing = nodes.filter(node => !nodeResourceCache.has(node.id))

    if (missing.length) {
      resourcesLoading.value = true

      const responses = await mapWithConcurrency(
        missing,
        FETCH_CONCURRENCY,
        node => API.getResourceForNode(node.id).then(resource => ({ node, resource }))
      )

      responses.forEach((response, index) => {
        if (response?.resource) {
          const label = response.resource.label || response.node.label
          nodeResourceCache.set(response.node.id, {
            label,
            resources: graphableDescendants(response.node, label, response.resource)
          })
        } else {
          nodeResourceCache.set(missing[index].id, null)
        }
      })

      if (requestId !== resourceRequestId) {
        return
      }
    }

    // One entry per resource id: the same node asked for under two id forms
    // answers twice, and a resource must never be listed, or graphed, twice.
    const seen = new Set<string>()
    resourceCandidates.value = nodes
      .flatMap(node => nodeResourceCache.get(node.id)?.resources ?? [])
      .filter(resource => (seen.has(resource.id) ? false : (seen.add(resource.id), true)))
    resourcesLoading.value = false
    failedNodeLoads.value = nodes.filter(node => nodeResourceCache.get(node.id) === null).length

    // A node restored from a link is known only by id until its resources load.
    pickedNodes.value = pickedNodes.value.map(node => (node.label === node.id && nodeResourceCache.get(node.id) ?
      { ...node, label: nodeResourceCache.get(node.id)?.label ?? node.id } :
      node))
  }

  /**
   * Re-run the node box against the server. A rule goes to the filter engine and
   * is unbounded, because a rule names a set and the graph wants all of it. A
   * label fragment (or an empty box) goes to the label search, which pages, so the
   * picker stays usable on an install with six figures of nodes.
   */
  const evaluateNodes = async () => {
    const requestId = ++nodeRequestId
    nodesLoading.value = true

    let found: AdhocNodeOption[] = []
    let error = ''
    let overflow = 0
    let tooMany = 0

    const rule = composeFilterRule(nodeFilter.value)
    let searchLabels = !rule

    if (rule) {
      const result = await API.getNodesByFilterRule(rule, MAX_GRAPH_NODES)

      if (!('error' in result)) {
        found = result.nodes.map(toNodeOption)
      } else if (result.error === 'too-many') {
        tooMany = result.count
      } else if (result.error === 'invalid' && classifyNodeText(nodeFilter.value) === 'guess') {
        // A lone word that only looked like a keyword: treat it as the host name
        // fragment it probably is.
        searchLabels = true
      } else {
        error = result.error === 'invalid' ? INVALID_RULE_MESSAGE : FAILED_RULE_MESSAGE
      }
    }

    if (searchLabels) {
      const queryParameters: QueryParameters = {
        limit: NODE_SEARCH_LIMIT,
        offset: 0,
        orderBy: 'label',
        order: SORT.ASCENDING
      }
      const searchable = toFiqlSearchTerm(nodeFilter.value)

      if (searchable) {
        queryParameters._s = `label==*${searchable}*`
      }

      const resp = await API.getNodes(queryParameters)

      if (resp) {
        found = resp.node.map(toNodeOption)
        overflow = Math.max(0, (resp.totalCount ?? found.length) - found.length)
      } else {
        error = FAILED_SEARCH_MESSAGE
      }
    }

    if (requestId !== nodeRequestId) {
      return
    }

    nodeMatches.value = found
    nodeFilterError.value = error
    nodeMatchOverflow.value = overflow
    ruleOverflow.value = tooMany
    nodesLoading.value = false

    await loadResources()
  }

  const setNodeFilter = async (text: string) => {
    nodeFilter.value = text
    await evaluateNodes()
  }

  const setResourceFilter = (text: string) => {
    resourceFilter.value = text
  }

  const setDatasourceFilter = (text: string) => {
    datasourceFilter.value = text
  }

  /**
   * Deselecting a node by hand takes its resource picks with it; that is what
   * the user meant. Neither narrowing a filter match to a first pick nor
   * clearing every pick does: in both the graph is being reshaped around the
   * filter, and picks under nodes it still matches should survive.
   */
  const setPickedNodes = async (nodes: AdhocNodeOption[]) => {
    if (pickedNodes.value.length && nodes.length) {
      const forms = new Set(nodes.flatMap(nodeIdForms))
      pickedResources.value = pickedResources.value.filter(resource =>
        forms.has(resource.nodeId) || forms.has(nodeCriteriaOf(resource.id) ?? ''))
    }

    pickedNodes.value = nodes
    await loadResources()
  }

  /** Likewise, deselecting a resource by hand drops its datasource picks. */
  const setPickedResources = (resources: AdhocResourceOption[]) => {
    if (pickedResources.value.length && resources.length) {
      const resourceIds = new Set(resources.map(resource => resource.id))
      pickedDatasources.value = pickedDatasources.value.filter(datasource => resourceIds.has(datasource.resourceId))
    }

    pickedResources.value = resources
  }

  const setPickedDatasources = (datasources: AdhocDatasourceOption[]) => {
    pickedDatasources.value = datasources
  }

  /** The selection as a link carries it. */
  const selectionState = computed<AdhocSelectionState>(() => ({
    nodeFilter: nodeFilter.value,
    resourceFilter: resourceFilter.value,
    datasourceFilter: datasourceFilter.value,
    pickedNodeIds: pickedNodes.value.map(node => node.id),
    pickedResourceIds: pickedResources.value.map(resource => resource.id),
    pickedDatasourceKeys: pickedDatasources.value.map(datasource => datasource.key)
  }))

  /**
   * Rebuild the picker from a link, then evaluate it against the server as it is
   * now. Picks arrive as bare ids and are listed as stand-ins until their data
   * loads, so a link to a resource the server no longer has still shows the series
   * (relaxed, so it comes back as NaN) instead of silently dropping it.
   */
  const restore = async (state: AdhocSelectionState) => {
    nodeFilter.value = state.nodeFilter
    resourceFilter.value = state.resourceFilter
    datasourceFilter.value = state.datasourceFilter
    pickedNodes.value = state.pickedNodeIds.map(id => ({ id, label: id }))
    pickedResources.value = state.pickedResourceIds.map(resourceStandIn)
    pickedDatasources.value = state.pickedDatasourceKeys.map(datasourceStandIn)

    await evaluateNodes()
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
    queryRequestId++

    nodeFilter.value = ''
    resourceFilter.value = ''
    datasourceFilter.value = ''
    nodeMatches.value = []
    pickedNodes.value = []
    pickedResources.value = []
    pickedDatasources.value = []
    resourceCandidates.value = []
    nodeResourceCache.clear()
    measurements.value = null
    queryError.value = ''
    nodeFilterError.value = ''
    nodeMatchOverflow.value = 0
    ruleOverflow.value = 0
    failedNodeLoads.value = 0
    nodesLoading.value = false
    resourcesLoading.value = false
    queryLoading.value = false
  }

  return {
    nodeFilter,
    nodeMatches,
    nodeOptions,
    nodesLoading,
    nodeFilterError,
    nodeMatchOverflow,
    nodeLimitExceeded,
    failedNodeLoads,
    pickedNodes,
    effectiveNodes,
    resourceFilter,
    resourceMatches,
    resourceOptions,
    resourcesLoading,
    pickedResources,
    reachablePickedResources,
    effectiveResources,
    datasourceFilter,
    datasourceMatches,
    datasourceOptions,
    pickedDatasources,
    reachablePickedDatasources,
    effectiveDatasources,
    selectionState,
    measurements,
    queryLoading,
    queryError,
    evaluateNodes,
    setNodeFilter,
    setResourceFilter,
    setDatasourceFilter,
    setPickedNodes,
    setPickedResources,
    setPickedDatasources,
    restore,
    runQuery,
    clearAll
  }
})
