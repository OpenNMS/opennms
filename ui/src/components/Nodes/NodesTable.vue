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
            @click="nodeStructureStore.openColumnsDrawerModal()"
          />
          <OnmsButton
            :label="nodeStructureStore.showInterfaces ? 'Hide interfaces' : 'Show interfaces'"
            variant="outlined"
            data-test="show-interfaces-button"
            @click="nodeStructureStore.setShowInterfaces(!nodeStructureStore.showInterfaces)"
          />
          <OnmsButton
            label="Clear Filters"
            variant="outlined"
            data-test="clear-filters-button"
            @click="nodeStructureStore.clearAllFiltersAndSelections()"
          />
        </div>
      </div>
      <div class="spacer-large"></div>
      <div class="spacer-large"></div>
      <div class="search-container">
        <div class="search-row">
          <div class="filter">
            <div class="search-filter-column">
              <FormField class="search-field">
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
              <OnmsIcon
                :icon="InfoIcon"
                class="info-icon"
                title="Node Search Help"
                @click="isHelpMessageDialogVisible = true"
                data-test="nodes-info-icon"
              />
            </div>
            <div>
              <OnmsIconButton
                title="Advanced Filters"
                data-test="advanced-filters-button"
                :icon="FilterAlt"
                @click="nodeStructureStore.openInstancesDrawerModal()"
              />
            </div>
          </div>
          <div class="chip-container">
            <OnmsChip
              v-for="cat in nodeStructureStore.selectedCategories"
              :key="`cat-${cat._value}`"
              :label="`Category: ${cat._text}`"
              removable
              @remove="removeItem(cat, FilterTypeEnum.Category)"
            />
            <OnmsChip
              v-for="cat in nodeStructureStore.selectedCategories2"
              :key="`cat2-${cat._value}`"
              :label="`Category (2): ${cat._text}`"
              removable
              @remove="removeItem(cat, FilterTypeEnum.Category2)"
            />
            <OnmsChip
              v-for="flow in nodeStructureStore.selectedFlows"
              :key="`flow-${flow._value}`"
              :label="`Flows: ${flow._text}`"
              removable
              @remove="removeItem(flow, FilterTypeEnum.Flow)"
            />
            <OnmsChip
              v-for="loc in nodeStructureStore.queryFilter.selectedMonitoringLocations"
              :key="loc.name"
              :label="`Location: ${loc.name}`"
              removable
              @remove="removeItem(loc, FilterTypeEnum.MonitoringLocation)"
            />
            <OnmsChip
              v-for="svc in nodeStructureStore.selectedServices"
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
              v-if="nodeStructureStore.queryFilter.ipAddress"
              :label="`IP Pattern: ${nodeStructureStore.queryFilter.ipAddress}`"
              removable
              @remove="nodeStructureStore.removeIpAddress()"
            />
            <OnmsChip
              v-if="nodeStructureStore.queryFilter.macAddress"
              :label="`MAC Address: ${nodeStructureStore.queryFilter.macAddress}`"
              removable
              @remove="nodeStructureStore.removeMacAddress()"
            />
            <OnmsChip
              v-if="hasTopologySearch"
              :label="`Topology: ${topologyTerm}`"
              removable
              @remove="nodeStructureStore.removeTopology()"
            />
            <OnmsChip
              v-if="nodeStructureStore.queryFilter.nodesWithDownAggregateStatus"
              label="Down nodes only"
              removable
              @remove="nodeStructureStore.removeDownAggregateStatus()"
            />
            <OnmsChip
              v-if="nodeStructureStore.queryFilter.nodesWithAssets"
              label="Nodes with asset info"
              removable
              @remove="nodeStructureStore.removeNodesWithAssets()"
            />
            <OnmsChip
              v-if="nodeStructureStore.queryFilter.nodesWithOutages"
              label="Nodes with outages"
              removable
              @remove="nodeStructureStore.removeNodesWithOutages()"
            />
            <OnmsChip
              v-for="assetFilter in (nodeStructureStore.queryFilter.assetFilters ?? [])"
              :key="assetFilter.column"
              :label="`Asset: ${getAssetColumnLabel(assetFilter.column)}: ${assetFilter.value}`"
              removable
              @remove="nodeStructureStore.removeAssetFilter(assetFilter.column)"
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
            expander
            style="width: 3rem"
          />
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
            <NodeInterfacesPanel :node="data" />
          </template>
        </OnmsTable>
        <div
          v-if="nodeStructureStore.showInterfaces"
          class="interfaces-footer"
          data-test="interfaces-footer"
        >
          {{ nodeCountLabel }}, {{ interfaceCountLabel }} on this page
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
      <div>
        <p>You may search by node name or exact IP address here.</p>
        <p>Searching by name is a case-insensitive, inclusive search.</p>
        <p>For example, searching on serv would find any of serv, Service, Reserved, NTSERV, UserVortex, etc. The underscore character acts as a single character wildcard. The percent character acts as a multiple character wildcard.</p>
        <p>For more advanced search options, please open the Advanced Filters drawer.</p>
      </div>
    </template>
  </OnmsMessageDialog>
</template>

<script setup lang="ts">
import useSnackbar from '@/composables/useSnackbar'
import { useMenuStore } from '@/stores/menuStore'
import { useNodeStore } from '@/stores/nodeStore'
import { useNodeStructureStore } from '@/stores/nodeStructureStore'
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
  OnmsIcon,
  OnmsIconButton,
  OnmsMessageDialog,
  OnmsSearchInput,
  OnmsTable,
  type OnmsTablePageEvent,
  type OnmsTableSortEvent
} from '@opennms/onms-ui'
import FilterAlt from '@/components/icons/action/FilterAlt.vue'
import ViewDetails from '@/components/icons/action/ViewDetails.vue'
import InfoIcon from '@/components/icons/action/Info.vue'
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
import { useNodeQuery, sanitizeSearchTerm } from './hooks/useNodeQuery'
import { countInterfacesForNodes, getInterfaceListMode, type InterfaceListMode } from './hooks/useInterfaceListing'
import { getAssetColumnLabel } from './hooks/queryStringParser'
import EmptyList from '../Common/EmptyList.vue'
import FormField from '../Common/FormField.vue'

const menuStore = useMenuStore()
const nodeStructureStore = useNodeStructureStore()
const nodeStore = useNodeStore()
const { showSnackBar } = useSnackbar()
const { generateBlob, generateDownload, getExportData } = useNodeExport()
const { buildUpdatedNodeStructureQueryParameters, getExtendedSearchValues } = useNodeQuery()
const isHelpMessageDialogVisible = ref(false)

const sortField = ref('label')
const sortOrder = ref(1) // 1 = ascending, -1 = descending

const currentSearch = ref(nodeStructureStore.queryFilter.searchTerm || '')
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
  nodeStructureStore.columns
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
  if (val !== nodeStructureStore.queryFilter.searchTerm) {
    nodeStructureStore.setSearchTerm(val)
  }
}

const onDownload = async (format: string) => {
  const updatedParams = buildUpdatedNodeStructureQueryParameters(queryParameters.value, nodeStructureStore.queryFilter)
  const data = await getExportData(format, updatedParams, nodeStructureStore.columns)

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
  return getExtendedSearchValues(nodeStructureStore.queryFilter.extendedSearch)
})

// ── "Show interfaces" mode (legacy ?listInterfaces=true parity) ────────────────

const interfaceListMode = computed(() => getInterfaceListMode(nodeStructureStore.queryFilter))

const pageNodeIds = computed(() => nodes.value.map(n => n.id))

const pageInterfaceCount = computed(() =>
  countInterfacesForNodes(pageNodeIds.value, interfaceListMode.value, nodeStore.nodeToIpInterfaceMap, nodeStore.nodeToSnmpInterfaceMap)
)

const nodeCountLabel = computed(() => {
  const n = nodeStore.totalCount
  return `${n} Node${n === 1 ? '' : 's'}`
})

const interfaceCountLabel = computed(() => {
  const m = pageInterfaceCount.value
  return `${m} Interface${m === 1 ? '' : 's'}`
})

// SQL-LIKE wildcard characters ('%' any-length, '_' single-char) — see buildSnmpNarrowing below.
const SQL_WILDCARD_PATTERN = /[%_]/

// Build the FIQL narrowing expression passed to nodeStore.getSnmpInterfacesForNodes so we only
// fetch the SNMP interfaces relevant to the active maclike/snmpParm mode (see
// getNodeSnmpInterfaceQuery). Sanitized the same way other FIQL builders in useNodeQuery.ts are.
const buildSnmpNarrowing = (mode: InterfaceListMode): string | undefined => {
  if (mode.mode === 'maclike') {
    const normalizedMac = mode.mac.toLowerCase().replace(/[:-]/g, '')
    return `physAddr==*${sanitizeSearchTerm(normalizedMac)}*`
  }

  if (mode.mode === 'snmpParm') {
    // The server-side FIQL '==*value*' matches '%'/'_' literally, while useInterfaceListing.ts's
    // client-side matchesSnmpParm() treats them as SQL-LIKE wildcards. If the value contains
    // either, the server narrowing would no longer be a superset of the client match — rows the
    // client considers a match could be excluded from the fetch and silently go missing. Omit the
    // attribute narrowing entirely in that case; the node.id scoping alone still limits the fetch
    // to the current page, and exact contains/equals semantics are re-applied client-side anyway.
    if (SQL_WILDCARD_PATTERN.test(mode.value)) {
      return undefined
    }

    return `${mode.attr}==*${sanitizeSearchTerm(mode.value)}*`
  }

  return undefined
}

const hasTopologySearch = computed(() => {
  return !!nodeStructureStore.queryFilter.topology?.length
})

const topologyTerm = computed(() => {
  return nodeStructureStore.queryFilter.topology ?? ''
})

const removeItem = (item: IAutocompleteItemType, type: FilterTypeEnum) => {
  switch (type) {
    case FilterTypeEnum.Category:
      nodeStructureStore.removeCategory(item)
      break
    case FilterTypeEnum.Category2:
      nodeStructureStore.removeCategory2(item)
      break
    case FilterTypeEnum.Flow:
      nodeStructureStore.removeFlow(item)
      break
    case FilterTypeEnum.MonitoringLocation:
      nodeStructureStore.removeMonitoringLocation(item)
      break
    case FilterTypeEnum.MonitoredService:
      nodeStructureStore.removeService(item)
      break
    default:
      console.warn(`Unknown filter type: ${type}`)
  }
}

const removeExtendedSearchItem = (item: ExtendedSearchValue) => {
  nodeStructureStore.removeExtendedSearch(item)
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

  const updatedParams = buildUpdatedNodeStructureQueryParameters(queryParamsToUse, nodeStructureStore.queryFilter)
  queryParameters.value = updatedParams

  nodeStore.getNodes(updatedParams, true)
}

const emptyListContent = {
  msg: 'No results found.'
}

watch([() => nodeStructureStore.queryFilter], () => {
  if (nodeStructureStore.queryFilter.searchTerm !== currentSearch.value) {
    currentSearch.value = nodeStructureStore.queryFilter.searchTerm
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
  [nodes, () => nodeStructureStore.showInterfaces, interfaceListMode],
  ([currentNodes, showInterfaces]) => {
    if (!showInterfaces) {
      expandedRows.value = {}
      return
    }

    const expanded: Record<string, boolean> = {}
    currentNodes.forEach((n) => {
      expanded[n.id] = true
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
// throwaway-but-distinct-looking fetch that can race with — and, on the wire, arrive after — the
// correct one issued once `nodes` actually updates. getSnmpInterfacesForNodes replaces the whole
// map with no request-sequencing, so whichever response lands last wins; the stale-ids fetch
// winning would blank every expanded panel on the new page. Gating on `nodes`/`showInterfaces`
// instead means the fetch only ever fires once the page has actually settled to match the current
// filter, so the mode read at that point is always paired with the node ids it actually produced.
// lastSnmpFetchKey additionally dedupes back-to-back fires with an unchanged (nodeIds, narrowing)
// pair (e.g. a page re-render that doesn't actually change the result set).
const lastSnmpFetchKey = ref<string | null>(null)

watch(
  [nodes, () => nodeStructureStore.showInterfaces],
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

defineExpose({ onSort, onPage, removeItem })
</script>

<style lang="scss" scoped>
@use '@/styles/onms-elevation' as *;
@use '@/styles/onms-typography' as *;
@use "@/styles/onms-tokens" as variables;

.node-table {
  margin-top: 1rem;
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

.action-buttons-column {
  text-align: left;
}

.filter {
  display: flex;
  align-items: center;
  gap: 10px;

  .search-filter-column {
    // Match the SNMP Configuration Definitions search box: right-aligned,
    // enlarged search glyph inside a full-width input.
    .search-field {
      width: 450px;

      :deep(.p-iconfield) {
        display: block;
        width: 100%;
      }

      :deep(.p-inputtext) {
        width: 100%;
        padding-right: 2.75rem;
      }
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

.search-container {
  .info-icon {
    cursor: pointer;
    font-size: 1.5em;
    margin-left: 0.5em;
    color: var(variables.$primary);

    &:hover {
      opacity: 0.8;
    }
  }
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
