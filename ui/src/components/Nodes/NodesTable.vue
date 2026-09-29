<template>
  <div class="card">
    <div>
      <div class="onms-row title-bar">
        <span class="title onms-col-4">Nodes</span>
        <div class="action-buttons-container onms-col-8">
          <NodeDownloadDropdown
            :onCsvDownload="onCsvDownload"
            :onJsonDownload="onJsonDownload"
          />
          <OnmsButton
            label="Customize Columns"
            data-test="customize-columns-button"
            @click="nodeListStore.openColumnsDrawerModal()"
          />
          <OnmsButton
            :label="nodeListStore.showInterfaces ? 'Hide interfaces' : 'Show interfaces'"
            variant="outlined"
            data-test="show-interfaces-button"
            @click="nodeListStore.setShowInterfaces(!nodeListStore.showInterfaces)"
          />
          <OnmsButton
            label="Clear Filters"
            variant="outlined"
            data-test="clear-filters-button"
            @click="nodeListStore.clearAllFiltersAndSelections()"
          />
        </div>
      </div>
      <div class="spacer-large"></div>
      <div class="spacer-large"></div>
      <div class="search-container">
        <div class="search-row">
          <div class="filter">
            <div class="search-filter-column">
              <FormField
                class="search-field"
                data-test="search-field"
              >
                <OnmsSearchInput
                  v-model="currentSearch"
                  @update:modelValue="searchFilterHandler"
                  placeholder="Search node label or full IP address"
                  aria-label="Search node label or full IP address"
                  data-test="search-input"
                />
              </FormField>
            </div>
            <div>
              <OnmsIconButton
                title="Node Search Help"
                data-test="nodes-info-icon"
                :icon="InfoIcon"
                @click="isHelpMessageDialogVisible = true"
              />
            </div>
            <div>
              <OnmsIconButton
                title="Advanced Filters"
                data-test="advanced-filters-button"
                :icon="FilterAlt"
                @click="nodeListStore.openInstancesDrawerModal()"
              />
            </div>
          </div>
          <div class="chip-container">
            <OnmsChip
              v-for="cat in nodeListStore.selectedCategories"
              :key="`cat-${cat._value}`"
              :label="`Category: ${cat._text}`"
              removable
              @remove="removeItem(cat, FilterTypeEnum.Category)"
            />
            <OnmsChip
              v-for="cat in nodeListStore.selectedCategories2"
              :key="`cat2-${cat._value}`"
              :label="`Category (2): ${cat._text}`"
              removable
              @remove="removeItem(cat, FilterTypeEnum.Category2)"
            />
            <OnmsChip
              v-for="flow in nodeListStore.selectedFlows"
              :key="`flow-${flow._value}`"
              :label="`Flows: ${flow._text}`"
              removable
              @remove="removeItem(flow, FilterTypeEnum.Flow)"
            />
            <OnmsChip
              v-for="loc in nodeListStore.queryFilter.selectedMonitoringLocations"
              :key="loc.name"
              :label="`Location: ${loc.name}`"
              removable
              @remove="removeItem(loc, FilterTypeEnum.MonitoringLocation)"
            />
            <OnmsChip
              v-for="svc in nodeListStore.selectedServices"
              :key="`svc-${svc._value}`"
              :label="`Service: ${svc._text}`"
              removable
              @remove="removeItem(svc, FilterTypeEnum.MonitoredService)"
            />
            <OnmsChip
              v-for="value in extendedSearchValues"
              :key="`extended-${value.key}`"
              :label="`${value.name} ${value.value}`"
              removable
              @remove="removeExtendedSearchItem(value)"
            />
            <OnmsChip
              v-if="nodeListStore.queryFilter.ipAddress"
              :label="`IP Pattern: ${nodeListStore.queryFilter.ipAddress}`"
              removable
              @remove="nodeListStore.removeIpAddress()"
            />
            <OnmsChip
              v-if="nodeListStore.queryFilter.macAddress"
              :label="`MAC Address: ${nodeListStore.queryFilter.macAddress}`"
              removable
              @remove="nodeListStore.removeMacAddress()"
            />
            <OnmsChip
              v-if="hasTopologySearch"
              :label="`Topology: ${topologyTerm}`"
              removable
              @remove="nodeListStore.removeTopology()"
            />
            <OnmsChip
              v-if="nodeListStore.queryFilter.nodesWithDownAggregateStatus"
              label="Down nodes only"
              removable
              @remove="nodeListStore.removeDownAggregateStatus()"
            />
            <OnmsChip
              v-if="nodeListStore.queryFilter.nodesWithOutages"
              label="Nodes with current outages"
              removable
              @remove="nodeListStore.removeNodesWithOutages()"
            />
            <OnmsChip
              v-if="nodeListStore.queryFilter.nodesWithAssets"
              label="Nodes with asset info"
              removable
              @remove="nodeListStore.removeNodesWithAssets()"
            />
            <OnmsChip
              v-for="assetFilter in (nodeListStore.queryFilter.assetFilters ?? [])"
              :key="assetFilter.column"
              :label="`Asset: ${getAssetColumnLabel(assetFilter.column)}: ${assetFilter.value}`"
              removable
              @remove="nodeListStore.removeAssetFilter(assetFilter.column)"
            />
          </div>
        </div>
      </div>
    </div>

    <div class="onms-row">
      <div class="onms-col-12">
        <OnmsTable
          lazy
          scrollable
          size="small"
          dataKey="id"
          :value="nodes"
          paginator
          :rows="pageSize"
          :first="first"
          :totalRecords="nodeStore.totalCount"
          :rowsPerPageOptions="[10, 20, 50, 100, 200]"
          :sortField="sortField"
          :sortOrder="sortOrder"
          v-model:expandedRows="expandedRows"
          class="node-table"
          data-test="nodes-table"
          @page="onPage"
          @sort="onSort"
        >
          <OnmsColumn
            v-if="nodeListStore.showInterfaces"
            style="width: var(--expander-col-width)"
          >
            <template #body="{ data }">
              <OnmsIconButton
                v-if="isRowExpandable(data)"
                :icon="isRowExpanded(data) ? RowExpandedIcon : RowCollapsedIcon"
                :aria-expanded="isRowExpanded(data)"
                :aria-label="`Toggle interfaces for ${data.label}`"
                data-test="row-expander-toggle"
                @click="toggleRowExpanded(data)"
              />
            </template>
          </OnmsColumn>
          <OnmsColumn
            v-for="col in orderedSelectedColumns"
            :key="col.id"
            :field="col.id"
            :header="col.label"
            :sortable="col.id !== 'ipaddress' && col.id !== 'flows'"
          >
            <template #body="{ data }">
              <a
                v-if="col.id === 'id' || col.id === 'label'"
                :href="computeNodeLink(data.id)"
                target="_blank"
                @click="onNodeLinkClick(data.id)"
              >{{ col.id === 'id' ? data.id : data.label }}</a>
              <ManagementIPTooltipCell
                v-else-if="col.id === 'ipaddress'"
                :computeNodeIpInterfaceLink="computeNodeIpInterfaceLink"
                :node="data"
                :nodeToIpInterfaceMap="nodeStore.nodeToIpInterfaceMap"
              />
              <span v-else-if="col.id === 'location'">{{ data.location }}</span>
              <FlowTooltipCell
                v-else-if="col.id === 'flows'"
                :node="data"
              />
              <NodeTooltipCell
                v-else
                :text="data[col.id]"
              />
            </template>
          </OnmsColumn>
          <OnmsColumn
            header="Actions"
            class="actions-cell"
            style="min-width: 8rem"
            frozen
            alignFrozen="right"
          >
            <template #body="{ data }">
              <div class="actions-cell-buttons">
                <OnmsIconButton
                  title="View Details"
                  data-test="view-details-button"
                  :icon="ViewDetails"
                  @click="onNodeLinkClick(data.id)"
                />
                <NodeActionsDropdown
                  :baseHref="mainMenu.baseHref"
                  :node="data"
                  :triggerNodeInfo="onNodeInfo"
                  class="triple-icon"
                />
              </div>
            </template>
          </OnmsColumn>
          <template #empty>
            <EmptyList
              :content="emptyListContent"
              data-test="empty-list"
            />
          </template>
          <template #expansion="{ data }">
            <!-- The expansion cell spans the full table width; offset by the expander column's
                 width so the interface list left-aligns with the first data column's content,
                 whichever column the user has placed there. -->
            <div
              class="interface-expansion"
              data-test="interface-expansion"
            >
              <NodeInterfacesPanel :node="data" />
            </div>
          </template>
        </OnmsTable>
        <div
          v-if="nodeListStore.showInterfaces"
          class="interfaces-footer"
          data-test="interfaces-footer"
        >
          {{ totalNodeCountLabel }}. {{ pageNodeCountLabel }} and {{ pageInterfaceCountLabel }} on this page
        </div>
      </div>
    </div>
  </div>

  <NodeDetailsDialog
    :computeNodeLink="computeNodeLink"
    :computeNodeIpInterfaceLink="computeNodeIpInterfaceLink"
    :visible="dialogVisible"
    :node="dialogNode"
    @close="dialogVisible = false"
  />
  <NodeAdvancedFiltersDrawer />
  <ColumnSelectionDrawer />

  <OnmsMessageDialog
    :visible="isHelpMessageDialogVisible"
    :relative="true"
    maxHeight="22em"
    maxWidth="50em"
    title="Node Search"
    @close="isHelpMessageDialogVisible = false"
  >
    <template #content>
      <div class="node-list-help">
        <p>You may search by node name or exact IP address here.</p>
        <p>Searching by name is case-insensitive and matches partial names.</p>
        <p>You can use <code>*</code> as a multiple-character wildcard within your search text. For example, searching on <code>serv</code> would find serv, Service, Reserved, NTSERV, UserVortex, etc., and <code>ser*ice</code> would find Service.</p>
        <p>For more advanced search options, please open the Advanced Filters drawer.</p>
        <h3>Show / Hide Interfaces</h3>
        <p>Clicking on the Show Interfaces button will toggle the display (expandable row) of network interfaces for each node in the list that has more than 1 interface &mdash; or, when filtering by MAC address or SNMP interface attributes, at least 1 matching interface.</p>
      </div>
    </template>
  </OnmsMessageDialog>
</template>

<script setup lang="ts">
import useSnackbar from '@/composables/useSnackbar'
import { useMenuStore } from '@/stores/menuStore'
import { useNodeStore } from '@/stores/nodeStore'
import { useNodeListStore } from '@/stores/nodeListStore'
import {
  ExtendedSearchValue,
  FilterTypeEnum,
  Node,
  NodeColumnSelectionItem,
  QueryParameters,
  UpdateModelFunction
} from '@/types'
import { MainMenu } from '@/types/mainMenu'
import { IAutocompleteItemType } from '@/types'
import {
  OnmsButton,
  OnmsChip,
  OnmsColumn,
  OnmsIconButton,
  OnmsMessageDialog,
  OnmsSearchInput,
  OnmsTable,
  type OnmsTablePageEvent,
  type OnmsTableSortEvent
} from '@opennms/onms-ui'
import FilterAlt from '@opennms/onms-ui/icons/action/FilterAlt.vue'
import ViewDetails from '@opennms/onms-ui/icons/action/ViewDetails.vue'
import InfoIcon from '@opennms/onms-ui/icons/action/Info.vue'
import RowExpandedIcon from '@opennms/onms-ui/icons/navigation/ExpandMore.vue'
import RowCollapsedIcon from '@opennms/onms-ui/icons/navigation/ChevronRight.vue'
import { SORT } from '@/types'
import { computed, nextTick, ref, watch } from 'vue'
import ColumnSelectionDrawer from './ColumnSelectionDrawer.vue'
import FlowTooltipCell from './FlowTooltipCell.vue'
import ManagementIPTooltipCell from './ManagementIPTooltipCell.vue'
import NodeActionsDropdown from './NodeActionsDropdown.vue'
import NodeAdvancedFiltersDrawer from './NodeAdvancedFiltersDrawer.vue'
import NodeDetailsDialog from './NodeDetailsDialog.vue'
import NodeDownloadDropdown from './NodeDownloadDropdown.vue'
import NodeInterfacesPanel from './NodeInterfacesPanel.vue'
import NodeTooltipCell from './NodeTooltipCell.vue'
import { useNodeExport } from './hooks/useNodeExport'
import { useNodeQuery } from './hooks/useNodeQuery'
import {
  countInterfaceRowsForNode,
  getInterfaceListMode,
  normalizeMacSearch,
  type InterfaceListMode
} from './hooks/useInterfaceListing'
import { getAssetColumnLabel } from './hooks/queryStringParser'
import EmptyList from '../Common/EmptyList.vue'
import FormField from '../Common/FormField.vue'

const menuStore = useMenuStore()
const nodeListStore = useNodeListStore()
const nodeStore = useNodeStore()
const { showSnackBar } = useSnackbar()
const { generateBlob, generateDownload, getExportData } = useNodeExport()
const { buildUpdatedNodeListQueryParameters, getExtendedSearchValues } = useNodeQuery()
const isHelpMessageDialogVisible = ref(false)

const sortField = ref('label')
const sortOrder = ref(1) // 1 = ascending, -1 = descending

const currentSearch = ref(nodeListStore.queryFilter.searchTerm || '')
const nodes = computed(() => nodeStore.nodes)
const mainMenu = computed<MainMenu>(() => menuStore.mainMenu)

const expandedRows = ref<Record<string, boolean>>({})

const dialogVisible = ref(false)
const dialogNode = ref<Node>()
const queryParameters = ref<QueryParameters>(nodeStore.nodeQueryParameters)
const pageNumber = ref(1)
const pageSize = ref(nodeStore.nodeQueryParameters.limit || 50)

const first = computed(() => (pageNumber.value - 1) * pageSize.value)

const orderedSelectedColumns = computed<NodeColumnSelectionItem[]>(() =>
  nodeListStore.columns
    .filter(col => col.selected)
    .sort((a, b) => a.order - b.order)
)

const onSort = (event: OnmsTableSortEvent) => {
  const field = (event.sortField as string) || 'label'
  if (field === 'ipaddress' || field === 'flows') {
    return
  }
  sortField.value = field
  sortOrder.value = (event.sortOrder as number) ?? 1
  const order = sortOrder.value === 1 ? SORT.ASCENDING : SORT.DESCENDING
  queryParameters.value = { ...queryParameters.value, orderBy: field, order }
  updateQuery({ orderBy: field, order })
}

const onPage = (event: OnmsTablePageEvent) => {
  if (event.rows !== pageSize.value) {
    updatePageSize(event.rows)
  } else {
    updatePageNumber(event.page + 1)
  }
}

const updatePageNumber = (page: number) => {
  pageNumber.value = page
  const size = queryParameters.value.limit || 0
  queryParameters.value = { ...queryParameters.value, offset: Math.max((page - 1) * size, 0) }
  nodeStore.setNodeQueryParameters(queryParameters.value)

  updateQuery()
}

const updatePageSize = (size: number) => {
  pageSize.value = size
  pageNumber.value = 1
  queryParameters.value = { ...queryParameters.value, limit: size, offset: 0 }
  nodeStore.setNodeQueryParameters(queryParameters.value)

  updateQuery()
}

const searchFilterHandler: UpdateModelFunction = (val = '') => {
  if (val !== nodeListStore.queryFilter.searchTerm) {
    nodeListStore.setSearchTerm(val)
  }
}

const onDownload = async (format: string) => {
  const updatedParams = buildUpdatedNodeListQueryParameters(queryParameters.value, nodeListStore.queryFilter)
  const data = await getExportData(format, updatedParams, nodeListStore.columns)

  if (!data) {
    showSnackBar({
      msg: `No data found for '${format}' download with the given search and filter configuration`,
      error: true
    })

    return
  }

  const contentType = format === 'json' ? 'application/json' : format === 'csv' ? 'text/csv' : ''

  const blob = generateBlob(data, contentType)
  generateDownload(blob, `Nodes.${format}`)
}

const onCsvDownload = async () => {
  return onDownload('csv')
}
const onJsonDownload = async () => {
  return onDownload('json')
}

const onNodeInfo = (node: Node) => {
  dialogNode.value = node
  dialogVisible.value = true
}

const computeNodeLink = (nodeId: number | string) => {
  return `${mainMenu.value.baseHref}${mainMenu.value.baseNodeUrl}${nodeId}`
}

const computeNodeIpInterfaceLink = (nodeId: number | string, ipAddress: string) => {
  return `${mainMenu.value.baseHref}element/interface.jsp?node=${nodeId}&intf=${ipAddress}`
}

const onNodeLinkClick = (nodeId: number | string) => {
  window.location.assign(computeNodeLink(nodeId))
}

const extendedSearchValues = computed(() => {
  return getExtendedSearchValues(nodeListStore.queryFilter.extendedSearch)
})

// ── "Show interfaces" mode (legacy ?listInterfaces=true parity) ────────────────

const interfaceListMode = computed(() => getInterfaceListMode(nodeListStore.queryFilter))

const pageNodeIds = computed(() => nodes.value.map(n => n.id))

// Single pass over the current page's nodes, computing each one's interface-row COUNT (not the
// rows themselves — sorting/labels/hrefs are irrelevant to a count) for the active mode. Read by
// isRowExpandable, both catch-up watchers, the auto-expand watcher, and pageInterfaceCount, so at
// 200 rows/page the page's interface lists get filtered once per render instead of twice
// (isRowExpandable and pageInterfaceCount previously each rebuilt+sorted the full row list per
// row independently).
const rowCountByNodeId = computed<Map<string, number>>(() => {
  const mode = interfaceListMode.value
  const counts = new Map<string, number>()

  nodes.value.forEach((n) => {
    const id = String(n.id)
    const ipInterfaces = nodeStore.nodeToIpInterfaceMap.get(id) ?? []
    const snmpInterfaces = nodeStore.nodeToSnmpInterfaceMap.get(id) ?? []
    counts.set(id, countInterfaceRowsForNode(mode, ipInterfaces, snmpInterfaces))
  })

  return counts
})

const pageInterfaceCount = computed(() => {
  let total = 0
  for (const count of rowCountByNodeId.value.values()) {
    total += count
  }
  return total
})

const totalNodeCountLabel = computed(() => {
  const n = nodeStore.totalCount
  return `${n} Node${n === 1 ? '' : 's'} total`
})

const pageNodeCountLabel = computed(() => {
  const p = nodes.value.length
  return `${p} node${p === 1 ? '' : 's'}`
})

const pageInterfaceCountLabel = computed(() => {
  const m = pageInterfaceCount.value
  return `${m} interface${m === 1 ? '' : 's'}`
})

// A caret only renders for rows whose expansion actually has content. In 'default' mode a single
// IP interface is already visible in the IP Address column, so the count must be > 1 for the
// caret to add anything new; in 'maclike'/'snmpParm' modes the matching interfaces aren't shown
// anywhere else, so even a single match (>= 1) is new information. Deliberately a plain function
// (not a computed) reading rowCountByNodeId directly, so it stays reactive per-row in the template
// and picks up the async SNMP/IP batches (nodeStore.nodeToSnmpInterfaceMap /
// nodeStore.nodeToIpInterfaceMap, via rowCountByNodeId) once they resolve. Only valid for nodes on
// the current page — rowCountByNodeId is built from nodes.value — which matches every call site
// (template rows and the watchers below all iterate the current page).
const isRowExpandable = (node: Node): boolean => {
  const mode = interfaceListMode.value
  const rowCount = rowCountByNodeId.value.get(String(node.id)) ?? 0
  const threshold = mode.mode === 'default' ? 1 : 0
  return rowCount > threshold
}

const isRowExpanded = (node: Node): boolean => !!expandedRows.value[node.id]

// PrimeVue's DataTable (object/dataKey expandedRows mode) treats a row as expanded whenever its
// dataKey is present in the map at all — even `{ [id]: false }` still counts as expanded (see
// DataTable's `d_rowExpanded = expandedRows?.[dataKey] !== undefined`). So collapsing a row must
// delete its key, not merely set it to false.
const toggleRowExpanded = (node: Node) => {
  const updated = { ...expandedRows.value }
  if (updated[node.id]) {
    delete updated[node.id]
  } else {
    updated[node.id] = true
  }
  expandedRows.value = updated
}

// Characters that make a value unsafe to splice raw into the FIQL attribute-narrowing term below:
// - '%' / '_' are SQL-LIKE wildcards to the server's FIQL '==*value*' match (literal), while
//   useInterfaceListing.ts's client-side matchesSnmpParm() treats them as SQL-LIKE wildcards — the
//   server narrowing would no longer be a superset of the client match.
// - ',' / ';' are FIQL set operators (OR / AND). sanitizeSearchTerm neutralizes them in other FIQL
//   builders by replacing them with spaces, but doing that here has the same superset problem as
//   '%'/'_': the narrowing sent to the server would search for something other than the exact
//   value, so it could exclude rows the client-side match still considers a hit.
// - '(' / ')' are FIQL grouping delimiters. Left in raw, they can produce an unbalanced FIQL
//   expression that fails to parse server-side — surfacing client-side as "No interfaces".
// If the value contains any of these, omit the attribute narrowing entirely; the node.id scoping
// alone still limits the fetch to the current page, and exact contains/equals semantics are
// re-applied client-side anyway (see buildSnmpNarrowing below).
const UNSAFE_NARROWING_CHARS = /[%_,;()]/

// Build the FIQL narrowing expression passed to nodeStore.getSnmpInterfacesForNodes so we only
// fetch the SNMP interfaces relevant to the active maclike/snmpParm mode (see
// getNodeSnmpInterfaceQuery).
const buildSnmpNarrowing = (mode: InterfaceListMode): string | undefined => {
  if (mode.mode === 'maclike') {
    // normalizeMacSearch strips every non-hex character, so the result can never contain any of
    // UNSAFE_NARROWING_CHARS above — no further guard needed here.
    return `physAddr==*${normalizeMacSearch(mode.mac)}*`
  }

  if (mode.mode === 'snmpParm') {
    if (UNSAFE_NARROWING_CHARS.test(mode.value)) {
      return undefined
    }

    return `${mode.attr}==*${mode.value}*`
  }

  return undefined
}

const hasTopologySearch = computed(() => {
  return !!nodeListStore.queryFilter.topology?.length
})

const topologyTerm = computed(() => {
  return nodeListStore.queryFilter.topology ?? ''
})

const removeItem = (item: IAutocompleteItemType, type: FilterTypeEnum) => {
  switch (type) {
    case FilterTypeEnum.Category:
      nodeListStore.removeCategory(item)
      break
    case FilterTypeEnum.Category2:
      nodeListStore.removeCategory2(item)
      break
    case FilterTypeEnum.Flow:
      nodeListStore.removeFlow(item)
      break
    case FilterTypeEnum.MonitoringLocation:
      nodeListStore.removeMonitoringLocation(item)
      break
    case FilterTypeEnum.MonitoredService:
      nodeListStore.removeService(item)
      break
    default:
      console.warn(`Unknown filter type: ${type}`)
  }
}

const removeExtendedSearchItem = (item: ExtendedSearchValue) => {
  nodeListStore.removeExtendedSearch(item)
}

const updateQuery = (options?: { orderBy?: string, order?: SORT }) => {
  // make sure anything setting nodeStore.nodeQueryParameters has been processed
  nextTick()

  const queryParamsToUse =
    options?.orderBy ?
      {
        ...nodeStore.nodeQueryParameters,
        orderBy: options.orderBy,
        order: options.order || SORT.ASCENDING
      }
      : nodeStore.nodeQueryParameters

  const updatedParams = buildUpdatedNodeListQueryParameters(queryParamsToUse, nodeListStore.queryFilter)
  queryParameters.value = updatedParams

  nodeStore.getNodes(updatedParams, true)
}

const emptyListContent = {
  msg: 'No results found.'
}

watch([() => nodeListStore.queryFilter], () => {
  if (nodeListStore.queryFilter.searchTerm !== currentSearch.value) {
    currentSearch.value = nodeListStore.queryFilter.searchTerm
  }

  updateQuery()
},
{ deep: true }
)

// Expand/collapse rows in response to any of the three: a new page of nodes, the toggle, or the
// active filter mode changing (e.g. editing the mac/snmpParm filter while already expanded).
// Harmless — and correct — to re-run on every one of these, since it's just a snapshot of "which
// rows are on the current page".
watch(
  [nodes, () => nodeListStore.showInterfaces, interfaceListMode],
  ([currentNodes, showInterfaces]) => {
    // Reset BOTH catch-up watchers' "already applied" flags on every input change here (new page,
    // toggle, or mode change): each such change starts a new generation whose eventual async map
    // replacement must get a fresh chance to catch up — even if the generation's key happens to
    // reproduce one that was already marked applied for a PRIOR generation. Without this, a
    // sequence like "page X catches up -> page Y (nothing qualifies, key never recorded as
    // applied) -> page X again" — or "default mode catches up -> maclike mode -> back to default,
    // same page" — leaves the OLD generation's key stuck in *CatchUpAppliedForKey, so the fresh
    // map replacement for the reproduced key is silently discarded, reproducing the exact B1
    // symptom this file otherwise fixes. This watcher always runs synchronously, strictly before
    // the corresponding async map replacement can land (nodeStore.getNodes/getSnmpInterfacesForNodes
    // are both kicked off from reactions to the same nodes/showInterfaces/mode change), so resetting
    // here is always in time. The manual-collapse guarantee (see mergeQualifyingIntoExpandedRows)
    // still holds: a manual collapse changes expandedRows directly, not nodes/showInterfaces/mode,
    // so it never runs this watcher and never triggers this reset.
    snmpCatchUpAppliedForKey = null
    ipCatchUpAppliedForKey = null

    if (!showInterfaces) {
      expandedRows.value = {}
      return
    }

    const expanded: Record<string, boolean> = {}
    currentNodes.forEach((n) => {
      if (isRowExpandable(n)) {
        expanded[n.id] = true
      }
    })
    expandedRows.value = expanded
  }
)

// Fetch the narrowed SNMP interfaces for the current page. Deliberately watches only [nodes,
// showInterfaces] — NOT interfaceListMode — and reads interfaceListMode.value fresh inside the
// callback instead. Reasoning: the pre-existing deep queryFilter watcher above is registered
// first, so on a mac/snmpParm filter edit it runs first in the same flush and kicks off
// nodeStore.getNodes(...) (async — nodeStore.nodes, and therefore `nodes` here, doesn't change
// until it resolves). If interfaceListMode were also a source here, this watcher would fire in
// that same flush with the NEW mode paired with the STALE (pre-edit) page node ids, issuing a
// throwaway fetch for interfaces of nodes about to leave the page. getSnmpInterfacesForNodes
// sequences its requests (a superseded request's response is discarded), so the stale fetch can
// no longer clobber the map on the wire — but it's still a wasted round-trip that can transiently
// populate panels from the wrong node set. Gating on `nodes`/`showInterfaces` instead means the
// fetch only ever fires once the page has actually settled to match the current filter, so the
// mode read at that point is always paired with the node ids it actually produced.
// lastSnmpFetchKey additionally dedupes back-to-back fires with an unchanged (nodeIds, narrowing)
// pair (e.g. a page re-render that doesn't actually change the result set).
const lastSnmpFetchKey = ref<string | null>(null)

watch(
  [nodes, () => nodeListStore.showInterfaces],
  ([currentNodes, showInterfaces]) => {
    // Deliberately don't reset lastSnmpFetchKey when toggling off — re-toggling on with the exact
    // same nodes/mode should stay deduped rather than re-issuing an identical request.
    if (!showInterfaces) {
      return
    }

    const mode = interfaceListMode.value
    if (mode.mode !== 'maclike' && mode.mode !== 'snmpParm') {
      return
    }

    const nodeIds = currentNodes.map(n => n.id)
    const narrowing = buildSnmpNarrowing(mode)
    const key = `${nodeIds.join(',')}|${narrowing ?? ''}`

    if (key === lastSnmpFetchKey.value) {
      return
    }

    lastSnmpFetchKey.value = key
    nodeStore.getSnmpInterfacesForNodes(nodeIds, narrowing)
  }
)

// Shared by both catch-up watchers below: additively merges `qualifying` node ids into
// expandedRows, leaving every other existing key untouched. Using this instead of a wholesale
// recompute is what lets a manual collapse stick — a wholesale recompute would re-add a
// manually-collapsed row the moment the (redundant/duplicate) map replacement it's guarded against
// fires again.
const mergeQualifyingIntoExpandedRows = (qualifying: Node[]) => {
  const updated = { ...expandedRows.value }
  qualifying.forEach((n) => {
    updated[n.id] = true
  })
  expandedRows.value = updated
}

// Catch-up for the race the two watchers above create in maclike/snmpParm mode: the auto-expand
// watcher fires synchronously off [nodes, showInterfaces, interfaceListMode] and evaluates
// isRowExpandable() there and then, but nodeToSnmpInterfaceMap is only populated later, once the
// async fetch kicked off by the watcher above resolves. So on first entry to maclike/snmpParm mode
// (or a fresh page/filter within it), the auto-expand watcher sees an empty map, isRowExpandable is
// false for every row, and expandedRows becomes {} — nothing auto-expands even though the caret
// itself appears once the map fills (its per-render reactivity has no such gate). This watcher
// re-evaluates once nodeToSnmpInterfaceMap is actually replaced and, if the current fetch
// generation (lastSnmpFetchKey) hasn't already had its catch-up applied, merges in any
// newly-qualifying rows.
//
// Guarded by generation (lastSnmpFetchKey), not just "map changed", and merges (adds keys) rather
// than recomputing expandedRows wholesale: a wholesale recompute here would also run every time the
// map reference merely happens to change again for the SAME generation (e.g. a redundant/duplicate
// resolution), forcibly re-expanding a row the user had since manually collapsed. Applying at most
// once per generation, additively, means a manual collapse always sticks.
let snmpCatchUpAppliedForKey: string | null = null

watch(
  () => nodeStore.nodeToSnmpInterfaceMap,
  () => {
    if (!nodeListStore.showInterfaces) {
      return
    }

    const mode = interfaceListMode.value
    if (mode.mode !== 'maclike' && mode.mode !== 'snmpParm') {
      return
    }

    const key = lastSnmpFetchKey.value
    if (key === null || key === snmpCatchUpAppliedForKey) {
      return
    }

    const qualifying = nodes.value.filter(n => isRowExpandable(n))
    if (qualifying.length === 0) {
      // Nothing to catch up (yet) — leave snmpCatchUpAppliedForKey alone so a later, genuine
      // resolution for this same generation still gets a chance to auto-expand.
      return
    }

    snmpCatchUpAppliedForKey = key
    mergeQualifyingIntoExpandedRows(qualifying)
  }
)

// Analogous catch-up for the B1 race in DEFAULT mode: nodeStore.getNodes(...) assigns nodes.value
// (triggering the auto-expand watcher above, synchronously, against a stale/empty
// nodeToIpInterfaceMap) and only THEN kicks off getIpInterfacesForNodes without awaiting it (see
// nodeStore.ts) — so the auto-expand watcher's synchronous isRowExpandable() check almost always
// sees an empty IP map for a freshly-arrived page and expandedRows becomes {}. This watcher
// re-evaluates once nodeToIpInterfaceMap is actually replaced (now wholesale, mirroring
// nodeToSnmpInterfaceMap — see nodeStore.ts) and merges in any newly-qualifying rows.
//
// Only relevant in 'default' mode: in maclike/snmpParm mode, qualification comes entirely from
// nodeToSnmpInterfaceMap (handled by the watcher above) — the IP batch that
// nodeStore.getNodes(..., true) fetches unconditionally alongside it only changes row LABELS
// (ManagementIPTooltipCell's IP address column), never which rows qualify to expand. Re-running
// the merge for those modes would be redundant double-handling (harmless, since merge is a no-op
// when nothing newly qualifies, but pointless), so this bails out early instead.
//
// Unlike lastSnmpFetchKey, there's no separately-tracked "key this component issued a fetch for":
// getIpInterfacesForNodes is called by nodeStore.getNodes itself, not by a watcher here (there is
// no narrowing to key on either). The current page's sorted node ids double as that generation key
// instead: it changes exactly when the page's node set changes (a new batch is genuinely
// relevant), and stays the same across redundant/duplicate map replacements for an unchanged page —
// giving the same "manual collapse sticks" guarantee as the SNMP catch-up watcher above.
// No .slice() needed before .sort(): .map() already returns a fresh array.
const currentIpFetchKey = computed(() => pageNodeIds.value.map(id => String(id)).sort().join(','))

let ipCatchUpAppliedForKey: string | null = null

watch(
  () => nodeStore.nodeToIpInterfaceMap,
  () => {
    if (!nodeListStore.showInterfaces) {
      return
    }

    if (interfaceListMode.value.mode !== 'default') {
      return
    }

    const key = currentIpFetchKey.value
    if (key === ipCatchUpAppliedForKey) {
      return
    }

    const qualifying = nodes.value.filter(n => isRowExpandable(n))
    if (qualifying.length === 0) {
      // Nothing to catch up (yet) — leave ipCatchUpAppliedForKey alone so a later, genuine
      // resolution for this same generation still gets a chance to auto-expand.
      return
    }

    ipCatchUpAppliedForKey = key
    mergeQualifyingIntoExpandedRows(qualifying)
  }
)

defineExpose({ onSort, onPage, removeItem, isRowExpandable, isRowExpanded, toggleRowExpanded })
</script>

<style lang="scss" scoped>
@use '@/styles/onms-elevation' as *;
@use '@/styles/onms-typography' as *;
@use "@/styles/onms-tokens" as variables;

.node-table {
  margin-top: 1rem;
  // Single source of truth for the expander column width, shared by the expander
  // OnmsColumn's inline style and the expansion row's alignment offset below.
  --expander-col-width: 3rem;
}

.interface-expansion {
  margin-left: var(--expander-col-width);
}

.card {
  @include onms-elevation(2);
  background: var(variables.$surface);
  padding: 30px;
}

.title {
  @include onms-headline1;
  display: block;
}

.node-list-help {
  p {
    margin: 0.5rem 0;
  }
}

.action-buttons-column {
  text-align: left;
}

.filter {
  display: flex;
  align-items: center;
  gap: 10px;

  .search-filter-column {
    // Match the SNMP Configuration Definitions search box: right-aligned.
    // OnmsSearchInput fills whatever width it is given.
    .search-field {
      width: 450px;
    }
  }

  .btn.btn-icon{
    border: 2px solid var(variables.$border-on-surface);
    border-radius: 3px;
    padding: 0 0.5rem;
    height: 3rem;
    width: 3rem;
  }
}

.chip-container {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
  align-items: center;
  padding-left: 10px;
}

.spacer-large {
  margin-bottom: 2rem;
}

.title-bar {
  align-items: center;
  padding-right: 1rem;
  padding-left: 1rem;
}

// Lay the search filter and the chip list out side by side, content-sized.
.search-row {
  display: flex;
  align-items: center;
  gap: 1.5rem;
}

.action-buttons-container {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 0.5rem;
}

// Keep the View Details + Node Actions buttons on a single line; never wrap
// when the column/viewport narrows (the column reserves min-width above).
.actions-cell-buttons {
  display: flex;
  flex-wrap: nowrap;
  align-items: center;
}

.triple-icon {
  margin-left: 7px;
}

.interfaces-footer {
  padding: 0.5rem 0;
}
</style>
