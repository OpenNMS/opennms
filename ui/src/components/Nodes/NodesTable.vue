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
          <Button
            label="Customize Columns"
            data-test="customize-columns-button"
            @click="nodeStructureStore.openColumnsDrawerModal()"
          />
          <Button
            label="Clear Filters"
            outlined
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
                <IconField>
                  <InputText
                    v-model="currentSearch"
                    @update:modelValue="searchFilterHandler"
                    placeholder="Search node label or full IP address"
                    aria-label="Search node label or full IP address"
                    data-test="search-input"
                  />
                  <InputIcon>
                    <FeatherIcon :icon="Search" />
                  </InputIcon>
                </IconField>
              </FormField>
            </div>
            <div>
              <FeatherIcon
                :icon="InfoIcon"
                class="info-icon"
                title="Node Search Help"
                @click="isHelpMessageDialogVisible = true"
                data-test="nodes-info-icon"
              />
            </div>
            <div>
              <Button
                text
                title="Advanced Filters"
                data-test="advanced-filters-button"
                @click="nodeStructureStore.openInstancesDrawerModal()"
              >
                <FeatherIcon
                  :icon="FilterAlt"
                  class="advanced-filters-icon"
                />
              </Button>
            </div>
          </div>
          <div class="chip-container">
            <Chip
              v-for="cat in nodeStructureStore.selectedCategories"
              :key="`cat-${cat._value}`"
              :label="`Category: ${cat._text}`"
              removable
              @remove="removeItem(cat, FilterTypeEnum.Category)"
            />
            <Chip
              v-for="cat in nodeStructureStore.selectedCategories2"
              :key="`cat2-${cat._value}`"
              :label="`Category (2): ${cat._text}`"
              removable
              @remove="removeItem(cat, FilterTypeEnum.Category2)"
            />
            <Chip
              v-for="flow in nodeStructureStore.selectedFlows"
              :key="`flow-${flow._value}`"
              :label="`Flows: ${flow._text}`"
              removable
              @remove="removeItem(flow, FilterTypeEnum.Flow)"
            />
            <Chip
              v-for="loc in nodeStructureStore.queryFilter.selectedMonitoringLocations"
              :key="loc.name"
              :label="`Location: ${loc.name}`"
              removable
              @remove="removeItem(loc, FilterTypeEnum.MonitoringLocation)"
            />
            <Chip
              v-for="svc in nodeStructureStore.selectedServices"
              :key="`svc-${svc._value}`"
              :label="`Service: ${svc._text}`"
              removable
              @remove="removeItem(svc, FilterTypeEnum.MonitoredService)"
            />
            <Chip
              v-for="value in extendedSearchValues"
              :key="`extended-${value.key}`"
              :label="`${value.name} ${value.value}`"
              removable
              @remove="removeExtendedSearchItem(value)"
            />
            <Chip
              v-if="nodeStructureStore.queryFilter.ipAddress"
              :label="`IP Pattern: ${nodeStructureStore.queryFilter.ipAddress}`"
              removable
              @remove="nodeStructureStore.removeIpAddress()"
            />
            <Chip
              v-if="nodeStructureStore.queryFilter.macAddress"
              :label="`MAC Address: ${nodeStructureStore.queryFilter.macAddress}`"
              removable
              @remove="nodeStructureStore.removeMacAddress()"
            />
            <Chip
              v-if="hasTopologySearch"
              :label="`Topology: ${topologyTerm}`"
              removable
              @remove="nodeStructureStore.removeTopology()"
            />
            <Chip
              v-if="nodeStructureStore.queryFilter.nodesWithDownAggregateStatus"
              label="Down nodes only"
              removable
              @remove="nodeStructureStore.removeDownAggregateStatus()"
            />
            <Chip
              v-if="nodeStructureStore.queryFilter.nodesWithAssets"
              label="Nodes with asset info"
              removable
              @remove="nodeStructureStore.removeNodesWithAssets()"
            />
            <Chip
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
        <DataTable
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
          class="node-table"
          data-test="nodes-table"
          @page="onPage"
          @sort="onSort"
        >
          <Column
            v-for="col in orderedSelectedColumns"
            :key="col.id"
            :field="col.id"
            :header="col.label"
            :sortable="col.id !== 'ipaddress'"
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
          </Column>
          <Column
            header="Actions"
            class="actions-cell"
            style="min-width: 8rem"
            frozen
            alignFrozen="right"
          >
            <template #body="{ data }">
              <div class="actions-cell-buttons">
                <Button
                  text
                  title="View Details"
                  class="view-details-icon"
                  data-test="view-details-button"
                  @click="onNodeLinkClick(data.id)"
                >
                  <FeatherIcon
                    :icon="ViewDetails"
                    title="View Details"
                  />
                </Button>
                <NodeActionsDropdown
                  :baseHref="mainMenu.baseHref"
                  :node="data"
                  :triggerNodeInfo="onNodeInfo"
                  class="triple-icon"
                />
              </div>
            </template>
          </Column>
          <template #empty>
            <EmptyList
              :content="emptyListContent"
              data-test="empty-list"
            />
          </template>
        </DataTable>
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

  <MessageDialog
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
  </MessageDialog>
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
import { IAutocompleteItemType } from '@featherds/autocomplete'
import { FeatherIcon } from '@featherds/icon'
import FilterAlt from '@featherds/icon/action/FilterAlt'
import Search from '@featherds/icon/action/Search'
import ViewDetails from '@featherds/icon/action/ViewDetails'
import InfoIcon from '@featherds/icon/action/Info'
import { SORT } from '@featherds/table'
import Button from 'primevue/button'
import Chip from 'primevue/chip'
import Column from 'primevue/column'
import DataTable, { type DataTablePageEvent, type DataTableSortEvent } from 'primevue/datatable'
import IconField from 'primevue/iconfield'
import InputIcon from 'primevue/inputicon'
import InputText from 'primevue/inputtext'
import MessageDialog from '../Common/MessageDialog.vue'
import { computed, nextTick, ref, watch } from 'vue'
import ColumnSelectionDrawer from './ColumnSelectionDrawer.vue'
import FlowTooltipCell from './FlowTooltipCell.vue'
import ManagementIPTooltipCell from './ManagementIPTooltipCell.vue'
import NodeActionsDropdown from './NodeActionsDropdown.vue'
import NodeAdvancedFiltersDrawer from './NodeAdvancedFiltersDrawer.vue'
import NodeDetailsDialog from './NodeDetailsDialog.vue'
import NodeDownloadDropdown from './NodeDownloadDropdown.vue'
import NodeTooltipCell from './NodeTooltipCell.vue'
import { useNodeExport } from './hooks/useNodeExport'
import { useNodeQuery } from './hooks/useNodeQuery'
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

const onSort = (event: DataTableSortEvent) => {
  const field = (event.sortField as string) || 'label'
  if (field === 'ipaddress') {
    return
  }
  sortField.value = field
  sortOrder.value = (event.sortOrder as number) ?? 1
  const order = sortOrder.value === 1 ? SORT.ASCENDING : SORT.DESCENDING
  queryParameters.value = { ...queryParameters.value, orderBy: field, order }
  updateQuery({ orderBy: field, order })
}

const onPage = (event: DataTablePageEvent) => {
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

defineExpose({ onSort, onPage, removeItem })
</script>

<style lang="scss" scoped>
@use "@featherds/styles/mixins/elevation" as elevation;
@use "@featherds/styles/mixins/typography" as typography;
@use "@featherds/styles/themes/variables" as variables;

.node-table {
  margin-top: 1rem;
}

.card {
  @include elevation.elevation(2);
  background: var(variables.$surface);
  padding: 30px;
}

.title {
  @include typography.headline1;
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

      :deep(.p-inputicon) {
        font-size: 1.75rem;
        right: 0.625rem;
        margin-top: -0.875rem;
      }
    }
  }

  // The Advanced Filters trigger icon read too small; bump it to ~1.5rem
  // (FeatherIcon scales with font-size).
  .advanced-filters-icon {
    font-size: 1.5rem;
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

.actions-cell {
  .view-details-icon {
    svg {
      font-size: 1.5rem !important;
    }
  }
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
</style>
